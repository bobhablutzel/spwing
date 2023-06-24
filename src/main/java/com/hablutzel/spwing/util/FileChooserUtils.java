/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hablutzel.spwing.util;

import com.hablutzel.spwing.annotations.Model;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileChooserUtils implements MessageSourceAware {


    @Setter
    private MessageSource messageSource;

    public void buildFileFiltersForModel(JFileChooser fileChooser, Object modelClass ) {
        buildFileFiltersForModelClass(fileChooser, modelClass.getClass());
    }


    public void buildFileFiltersForModelClass(JFileChooser fileChooser, Class<?> modelClass) {
        Model model = AnnotatedElementUtils.getMergedAnnotation(modelClass, Model.class);
        if (Objects.nonNull(model) && Objects.nonNull(model.extensions()) && model.extensions().length != 0) {

            FileFilter[] fileFilters = Arrays.stream(model.extensions())
                    .map(this::buildFileFilterForExtension)
                    .toArray(FileFilter[]::new);

            applyFileFilters(fileChooser, fileFilters);
        }
    }



    public FileFilter buildFileFilterForExtension( String extension) {
        String messageKey = String.format("desc_%s", extension);
        String defaultMessage = String.format( "define message %s to describe file extension %s", messageKey, extension);
        String description = messageSource.getMessage(messageKey, null, defaultMessage, Locale.getDefault());
        return new FileNameExtensionFilter(description, extension);
    }



    public void applyFileFilters(JFileChooser fileChooser, Object fileFilters) {
        // Apply the file filters (if any)
        if (fileFilters instanceof FileFilter fileFilter) {
            fileChooser.setFileFilter(fileFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
        } else if (fileFilters instanceof FileFilter[] filters) {
            Arrays.stream(filters).forEach(fileChooser::addChoosableFileFilter);
            fileChooser.setAcceptAllFileFilterUsed(false);
        }
        ensureFileExtensionOnApproval(fileChooser);
    }


    /**
     * Watch for file chooser approvals. When that happens, make sure that the
     * file has an extension appropriate for the selected file filter (if
     * that file filter is a FileNameExtensionFilter). This ensures that the
     * caller doesn't have to append the file extension in most cases.
     *
     * @param fileChooser The {@link JFileChooser}
     */
    public void ensureFileExtensionOnApproval(final JFileChooser fileChooser) {
        fileChooser.addActionListener( e -> {
            if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
                File selectedFile = fileChooser.getSelectedFile();
                if (fileChooser.getFileFilter() instanceof FileNameExtensionFilter fileFilter) {

                    String selectedFileName = selectedFile.getName();
                    String activeExtension = Arrays.stream(fileFilter.getExtensions())
                            .filter(selectedFileName::endsWith)
                            .findFirst()
                            .orElse("");
                    if (activeExtension.isBlank()) {
                        final String fileNameWithExtension = String.format("%s.%s", selectedFile.getName(), fileFilter.getExtensions()[0]);
                        final File fileWithExtension = new File(selectedFile.getParent(), fileNameWithExtension);
                        fileChooser.setSelectedFile(fileWithExtension);
                    }
                }
            }
        });
    }
}
