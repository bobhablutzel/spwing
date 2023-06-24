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

package com.hablutzel.spwing.model;

import com.hablutzel.spwing.context.DocumentSession;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.util.FileChooserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.io.File;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
class ProxyModelFactory<T> extends ModelFactory<T> {
    private final Class<T> modelClass;
    private final ApplicationContext applicationContext;
    private final Invoker open;
    private final Invoker createMethod;
    private final boolean openTakesAFile;

    @Override
    public T open(DocumentSession documentSession) {

        if (openTakesAFile) {
            File modelFile = promptUserForFile();
            if (Objects.nonNull(modelFile)) {

                // Associate this file with document session
                documentSession.setAssociatedFile(modelFile);

                // We're about to invoke the open method, register the parameter supplier for the file
                open.registerParameterSupplier(File.class, () -> modelFile);

                // Invoke the open method and return the result
                return open.invoke(modelClass);
            } else {
                return null;
            }
        } else {
            return open.invoke(modelClass);
        }
    }

    private File promptUserForFile() {
        JFileChooser fileChooser = new JFileChooser();

        // Get the FileChooserUtils service and apply the file filters from the
        // model class.
        FileChooserUtils fileChooserUtils = applicationContext.getBean(FileChooserUtils.class);
        fileChooserUtils.buildFileFiltersForModelClass(fileChooser, modelClass);

        // Prompt the user file a file
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            log.debug("Cancelling open because user did not approve the open");
            return null;
        }
    }

    @Override
    public T create(DocumentSession documentSession) {
        if (Objects.nonNull(createMethod)) {
            return createMethod.invoke(modelClass);
        } else {
            log.error("static create method not defined for class {}", modelClass.getName());
            throw new UnsupportedOperationException("open");
        }
    }
}
