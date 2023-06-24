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

package com.hablutzel.spwing.annotations;

import com.hablutzel.spwing.view.factory.reflective.ReflectiveViewFactory;
import com.hablutzel.spwing.view.factory.svwf.SVWFResourceViewFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;


/**
 * Defines a model object in the Spwing application framework.
 * Model objects represent the state of document, and are there
 * for declared at document scope meaning they will be singleton
 * instances for each new document.
 *
 * @author Bob Hablutzel
 */
@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Handler
@Scope("document")
@Service
public @interface Model {

    /**
     * Allow the user to specify a bean name for the bean when it
     * is created. Since this is a document-scope bean, it will
     * be a singleton in the document scope and this bean name will
     * be unique in that scope.
     *
     * @return The bean name alias
     */
    String alias() default "model";

    /**
     * Defines this as the "primary" model. In applications with only a single
     * model, this setting will be ignored. In applications with multiple models,
     * the first model encountered that is "primary" will be used as the default
     * model to open if cmdOpen isn't intercepted.
     * @return TRUE for the primary model
     */
    boolean primary() default false;


    /**
     * Defines the controller class associated with this model. This class,
     * if specified, will automatically be opened when the model is opened
     * (after creating the new document scope). The class referenced should be
     * annotated with {@link Controller}. If this is not specified, but the
     * model class name ends in "Model", then the framework will attempt
     * to find a similarly named view class that ends in "Controller" instead.
     *
     * @return The controller class.
     */
    Class<?> controller() default Object.class;

    /**
     * Used to define a view factory for this object. If not
     * defined, then the default is the {@link SVWFResourceViewFactory}. <br>
     * Applications that have been ported over from existing Swing code
     * might want to look at {@link ReflectiveViewFactory}, which
     * gives the option of specifying a method that returns
     * Swing components.
     */
    Class<?> viewFactory() default SVWFResourceViewFactory.class;

    /**
     * Defines the file extensions associated with this model. If no extensions
     * are defined, the model will have to implement a handlers for cmdOpen
     * and cmdSave. If these are defined, the framework will handle the open
     * and (if the model implements {@link com.hablutzel.spwing.model.Saveable} save dialogs.
     * If the model also implements {@link java.io.Serializable} or {@link java.io.Externalizable}
     * the framework can also handle the reading and writing of the model to the
     * associated file. However, the model always has the option of implementing
     * the <br>
     * The framework will maintain the files as a document scope bean named "fileStorage".
     * Implementations may access this file, but generally should not have to do so.<br>
     * File extensions are defined without delimiters. For example, a ".txt" file would be
     * specified as "txt", a ".gif" file as "gif", etc.<br>
     * When opening the file, any file extension in the list is acceptable. When saving
     * a new file, the default extension will be the first one in the list.<br>
     * The descriptions of the file extensions are, by convention, in the
     * properties file used by the {@link org.springframework.context.MessageSource} class.
     * For extension XXX, the file extension description will be named desc_XXX.
     *
     * @return A list of file extension strings, without the delimiter.
     */
    String[] extensions() default { };


}
