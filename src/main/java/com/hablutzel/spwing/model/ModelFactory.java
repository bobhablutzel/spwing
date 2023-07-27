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
import com.hablutzel.spwing.context.DocumentSession;
import com.hablutzel.spwing.invoke.DirectInvoker;
import com.hablutzel.spwing.invoke.DirectParameterDescription;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.util.FileChooserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;


@Slf4j
public abstract class ModelFactory<T> {


    /**
     * Called to open a document and create the model from the contents
     * of that document. This needs to prompt the
     * user for the location and read the data from that location
     *
     * @param documentSession The document session instance
     * @return A new model object
     */
    public abstract T open(DocumentSession documentSession);


    /**
     * Called to create a new, empty model representing the default
     * new state of a document
     *
     * @param documentSession The document session instance
     * @return The new model object.
     */
    public abstract T create(DocumentSession documentSession);



    /**
     * Builds a ModelFactory instance for the given class. The
     * class is expected to have two static methods, one named
     * create that returns a new instance of the model, and one
     * named open that returns from an existing file. If the
     * open method takes a File instance, the framework will
     * automatically prompt the user for the File instance before
     * calling open, based on the file extensions provided by
     * specifying a fileExtension bean.
     *
     * @param modelClass The model class
     * @return A new instance of the model class
     */
    @SuppressWarnings("unchecked")
    public static <T> ModelFactory<T> forModelClass(final ApplicationContext applicationContext, final Class<T> modelClass) {

        // See if there is a ModelFactory instance defined as a bean in the context
        ResolvableType modelFactoryType = ResolvableType.forClassWithGenerics(ModelFactory.class, modelClass );
        String[] beanNames = applicationContext.getBeanNamesForType(modelFactoryType);
        if (beanNames.length != 0) {
            log.debug( "Returning bean {}", beanNames[0]);
            return (ModelFactory<T>) applicationContext.getBean(beanNames[0]);
        }

        // We don't have a model factory bean, so we will build a ProxyFactory on the fly.
        // We look to see if there is a static create method in the model class itself that
        // can create a model instance. If not, we'll use the application context to look for
        // the model as a bean. In the latter case the model bean can be created by the
        // model configuration.
        final Invoker create = Arrays.stream(modelClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("create") &&
                                  Modifier.isStatic(method.getModifiers()) &&
                                  modelClass.isAssignableFrom(method.getReturnType()))
                .findFirst()
                .map(method -> (Invoker) new ReflectiveInvoker(applicationContext, null, method))
                .orElse(new ContextBeanInvoker(applicationContext, modelClass));

        // Find the open method. If the model defines file extensions, then the open method
        // needs to take a file.
        final List<String> fileExtensions = FileChooserUtils.getActiveFileExtensions(applicationContext);
        final Invoker open = Arrays.stream(modelClass.getDeclaredMethods())
                .filter(method -> method.getName().equals("open") &&
                                  Modifier.isStatic(method.getModifiers()) &&
                                  modelClass.isAssignableFrom(method.getReturnType()) &&
                                  (!fileExtensions.isEmpty() || Arrays.stream(method.getParameterTypes()).anyMatch(parameterClass -> parameterClass.isAssignableFrom(File.class))))
                .<Invoker>map(method -> new ReflectiveInvoker(applicationContext, null, method))
                .findFirst()
                .orElse(new DefaultReadFileFunctionality(applicationContext));

        // Return the new ModelFactory
        return new ProxyModelFactory<>(modelClass, applicationContext, open, create, fileExtensions);
    }


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



    private static class ContextBeanInvoker extends Invoker {
        private final ApplicationContext applicationContext;
        private final Class<?> modelClass;

        public ContextBeanInvoker(ApplicationContext applicationContext, Class<?> modelClass) {
            super(applicationContext);
            this.applicationContext = applicationContext;
            this.modelClass = modelClass;
        }

        @Override
        protected List<? extends ParameterDescription> getParameterDescriptions() {
            return List.of();
        }

        @Override
        protected Object doInvoke(Object[] dynamicArguments) {
            return applicationContext.getBean(modelClass);
        }

        @Override
        protected String getMethodName() {
            return "<get from application context>";
        }
    }
}
