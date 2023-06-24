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

package com.hablutzel.spwing.view.factory.reflective;

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.annotations.Controller;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.view.factory.ComponentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;


/**
 * Implementation a view factory that searches the
 * view object for a method called "buildSwingComponents", and invokes
 * that method to create the components. Note the Component
 * returned can be a JFrame, which would be used as-is, or any
 * other component which would be embedded in a dynamically
 * created JFrame by the framework.
 *
 * @author Bob Hablutzel
 */
@Slf4j
@Service
public class ReflectiveViewFactory {

    /**
     * Subclasses can change the name of the target method by
     * overriding this method.
     *
     * @return The name of the method.
     */
    public String targetMethodName() { return "buildSwingComponents"; }

    public Component build(final Spwing spwing,
                           final @Model Object model,
                           final @Controller Object controller,
                           final DocumentEventDispatcher documentEventDispatcher,
                           final ConversionService conversionService) {
        Method[] declaredMethods = controller.getClass().getDeclaredMethods();
        Optional<Method> methodOptional = Arrays.stream(declaredMethods)
                .filter(method -> method.getName().equals(targetMethodName()))
                .findFirst();
        if (methodOptional.isPresent()) {
            ComponentFactory componentFactory = new ComponentFactory(model, controller, documentEventDispatcher, new HashMap<>());
            Invoker invoker = new ReflectiveInvoker(spwing.getApplicationContext(), controller, methodOptional.get());
            invoker.registerParameterSupplier(ComponentFactory.class, () -> componentFactory);
            return invoker.invoke(Component.class);
        } else {
            log.warn("ReflectiveViewFactory could not find {} method", targetMethodName());
            return null;
        }
    }
}
