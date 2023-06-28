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

package com.hablutzel.spwing.view.factory.svwf;

import com.hablutzel.spwing.annotations.Controller;
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.util.PlatformResourceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.function.Supplier;


/**
 * Implementation of a view factory uses a SVWF description
 * file to create the Swing components. <br>
 * The SVWF (Spring VieW File format) provides a declarative
 * syntax for view files. This file format promotes rapid
 * generation of complex view files, while maintaining all the
 * power of the native Swing implementations.
 *
 * @author Bob Hablutzel
 */
@Slf4j
@Service
@Scope("document")
public class SVWFResourceViewFactory extends SVWFViewFactory {



    /**
     * Get the Swing components from the controller object via a SVWF file.
     * This method attempts to find a resource using the following search
     * criteria:
     * <ul>
     *     <li>Search for a resource with the same name as the simple name of the model class,
     *     in the resource directory for the model class</li>
     *     <li>If the model class simple name ends with "Model", search for a resource with the model
     *     class simple name but replacing the trailing "Model" with "View" in the resource directory
     *     for the model</li>
     *     <li>Search for a resource with the same name as the simple name of the controller class
     *     in the resource directory for the controller class</li>
     *     <li>If the controller class simple name ends with "Controller", search for a resource with the
     *     controller class simple name but replacing the trailing "Controller" with "View" in the
     *     resource directory for the controller class.</li>
     * </ul>
     * In all cases, the resource is search using platform awareness, see {@link PlatformResourceUtils#getPlatformResource(Class, String, String)}
     * and will have the extension ".svwf"
     *
     */
    public Component build(final @Model Object model,
                           final @Controller Object controller,
                           final ApplicationContext applicationContext) {

        final List<Supplier<InputStream>> nameMappers = new ArrayList<>();

        // Make sure we have a model. We should, but never hurts to be careful
        if (Objects.nonNull(model)) {
            final Class<?> modelClass = model.getClass();
            final String modelClassName = modelClass.getSimpleName();
            final String modelNameToViewName = modelClassName.endsWith("Model")
                    ? modelClassName.substring(0, modelClassName.length()- "Model".length() ) + "View"
                    : "";

            log.debug( "Adding check for resource {}", modelClassName);
            nameMappers.add(() -> PlatformResourceUtils.getPlatformResource(modelClass, modelClassName, "svwf"));
            log.debug( "Adding check for resource {}", modelNameToViewName);
            nameMappers.add(() -> PlatformResourceUtils.getPlatformResource(modelClass, modelNameToViewName, "svwf"));
        }

        // Same approach with the controller. Make sure we have one
        if (Objects.nonNull(controller)) {
            final Class<?> controllerClass = controller.getClass();
            final String controllerClassName = controllerClass.getSimpleName();
            final String controllerNameToViewName = controllerClassName.endsWith("Controller" )
                    ? controllerClassName.substring(0, controllerClassName.length() - "Controller".length()) + "View"
                    : "";

            log.debug( "Adding check for resource {}", controllerClassName);
            nameMappers.add(() -> PlatformResourceUtils.getPlatformResource(controllerClass, controllerClassName, "svwf"));
            log.debug( "Adding check for resource {}", controllerNameToViewName);
            nameMappers.add(() -> PlatformResourceUtils.getPlatformResource(controllerClass, controllerNameToViewName, "svwf"));

        }

        try (InputStream swvfStream = nameMappers.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null)) {

            return fromStream(model, controller, applicationContext, swvfStream);
        } catch (IOException e) {
            log.error("Error reading SVWF file", e);
            throw new RuntimeException("Error reading SVWF file", e);
        }
    }


}
