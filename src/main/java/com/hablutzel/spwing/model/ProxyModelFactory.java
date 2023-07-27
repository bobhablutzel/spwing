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
import com.hablutzel.spwing.invoke.ParameterResolution;
import com.hablutzel.spwing.util.FileChooserUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.io.File;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class ProxyModelFactory<T> extends ModelFactory<T> {
    private final Class<T> modelClass;
    private final ApplicationContext applicationContext;
    private final Invoker open;
    private final Invoker createMethod;
    private final List<String> fileExtensions;

    @Override
    public T open(DocumentSession documentSession) {

        if (!fileExtensions.isEmpty()) {
            File modelFile = promptUserForFile();

            // Associate this file with document session
            documentSession.setAssociatedFile(modelFile);

            // We're about to invoke the open method, register the parameter supplier for the file
            open.registerParameterResolver(ParameterResolution.forClass(File.class, modelFile));

            // Invoke the open method and return the result
            T theModelObject = open.invoke(modelClass);
            documentSession.addBeanToScope(modelClass, theModelObject);
            return theModelObject;
        } else {
            return open.invoke(modelClass);
        }
    }


    /**
     * For file based models (i.e. those that define a "fileExtension" bean
     * in their configuration instance), prompt the user for a file of the
     * appropriate type. Return that file; throw a runtime exception if the
     * user cancels the operation
     *
     * @return The file to open
     */
    private File promptUserForFile() {
        JFileChooser fileChooser = new JFileChooser();

        // Get the FileChooserUtils service and apply the file filters from the
        // model class.
        FileChooserUtils fileChooserUtils = applicationContext.getBean(FileChooserUtils.class);
        fileChooserUtils.buildFileExtensionFilters(fileChooser, fileExtensions);

        // Prompt the user file a file
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        } else {
            log.debug("Cancelling open because user did not approve the open");
            throw new RuntimeException("User cancelled open" );
        }
    }

    @Override
    public T create(DocumentSession documentSession) {
        if (null != createMethod) {
            return createMethod.invoke(modelClass);
        } else {
            log.error("static create method not defined for class {}", modelClass.getName());
            throw new UnsupportedOperationException("open");
        }
    }
}
