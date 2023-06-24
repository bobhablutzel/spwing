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

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.context.DocumentSession;
import com.hablutzel.spwing.invoke.DirectInvoker;
import com.hablutzel.spwing.invoke.DirectParameterDescription;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Slf4j
public abstract class ModelFactory<T> {

    /**
     * Builds a ModelFactory instance for the given class. The
     * class is expected to have two static methods, one named
     * create that returns a new instance of the model, and one
     * named open that returns from an existing file. If the
     * open method takes a File instance, the framework will
     * automatically prompt the user for the File instance before
     * calling open, based on the file extensions provided
     * in the {@link Model} annotation
     * on the model class.
     *
     * @param modelClass The model class (a class annotated with {@link Model}
     * @return A new instance of the model class
     * @implNote This will create a new "document" scope containing this
     * model.
     */
    public static <T> ModelFactory<T> forModelClass(final ApplicationContext applicationContext, final Class<T> modelClass) {

        // Attempt to get the annotation for this class
        final Model model = AnnotatedElementUtils.getMergedAnnotation(modelClass, Model.class);
        if (Objects.isNull(model)) {
            return null;
        }

        // Find the create method
        final Executable createMethod = Arrays.stream(modelClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("create") && Modifier.isStatic(method.getModifiers()))
                .findFirst()
                .orElse(null); // Can't use the constructor here because it's looking for a Method
        Executable newExecutable = Objects.nonNull(createMethod)
                ? createMethod
                : getPublicConstructor(modelClass);

        // Find the open method. If the model defines file extensions, then the open method
        // needs to take a file.
        final boolean isExpectedToTakeAFile = model.extensions().length > 0;
        final Invoker open = Arrays.stream(modelClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("open") && Modifier.isStatic(method.getModifiers())
                        && (!isExpectedToTakeAFile || Arrays.stream(method.getParameterTypes()).anyMatch(parameterClass -> parameterClass.isAssignableFrom(File.class))))
                .<Invoker>map(method -> new ReflectiveInvoker(applicationContext, null, method))
                .findFirst()
                .orElseGet(() -> new DefaultReadFileFunctionality(applicationContext));

        Invoker create = new ReflectiveInvoker(applicationContext, null, newExecutable);
        // Return the new ModelFactory
        return new ProxyModelFactory<>(modelClass, applicationContext, open, create, isExpectedToTakeAFile);
    }


    private static Executable getPublicConstructor(Class<?> modelClass) {
        if (modelClass.getConstructors().length > 0) {
            return modelClass.getConstructors()[0];
        } else {
            log.error("Model class {} has no public constructors, and no static create() method", modelClass.getName());
            throw new RuntimeException("Invalid model class");
        }
    }


    public abstract T open(DocumentSession documentSession);

    public abstract T create(DocumentSession documentSession);


    private static class DefaultReadFileFunctionality extends DirectInvoker {
        public DefaultReadFileFunctionality(ApplicationContext applicationContext) {
            super(applicationContext, "readFromFile",
                    List.of(
                            new DirectParameterDescription("file", File.class, false, 0, false),
                            new DirectParameterDescription("ui", Spwing.class, false, 1, false)));
        }

        @Override
        protected Object doInvoke(Object... dynamicArguments) {
            if (dynamicArguments[0] instanceof File file) {
                log.debug("In default open functionality");
                try (FileInputStream streamIn = new FileInputStream(file);
                     ObjectInputStream objectInputStream = new ObjectInputStream(streamIn)) {
                    return objectInputStream.readObject();
                } catch (IOException | ClassNotFoundException e) {

                    if (dynamicArguments[1] instanceof Spwing spwing) {
                        spwing.postErrorDialog("err_FileCannotBeRead", file.getName(), e.getLocalizedMessage());
                    }
                    throw new RuntimeException(e);
                }
            } else {
                throw new IllegalArgumentException("Expecting file");
            }
        }
    }
}
