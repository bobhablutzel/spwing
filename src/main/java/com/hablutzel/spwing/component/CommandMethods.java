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

package com.hablutzel.spwing.component;

import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.Arrays;
import java.util.function.Function;


/**
 * The {@link CommandMethods} class is used to encapsulate the
 * {@link Invoker} instances that enable and handle commands.
 *
 * @author Bob Hablutzel
 */
@Slf4j
@ToString
public class CommandMethods {

    /**
     * The handler invoker. This is called to process the command
     */
    @Setter
    private Invoker handler;

    /**
     * The (potentially null) enabler invoker, used to enable the command if present.
     */
    @Setter
    private Invoker enabler;


    /**
     * The menu item resolver used to allow the enable function to change the menu item
     */
    private Function<ParameterDescription, ParameterResolution> menuItemResolver = null;


    /**
     * Commands are enabled if (a) they have an enabler, and the
     * enabler returns true, or (b) they do not have an enabler but
     * do have a handler.
     * @return TRUE for enabled commands.
     */
    public boolean isEnabled(final JMenuItem menuItem) {
        if (null != enabler) {
            enabler.registerParameterResolver(getParameterSupplier(menuItem));
            return enabler.invoke(Boolean.class);
        } else {
            return null != handler;
        }
    }

    /**
     * Get the parameter supplier for the menu item
     *
     * @param menuItem The menu item
     * @return The parameter resolver function
     */
    private Function<ParameterDescription, ParameterResolution> getParameterSupplier(final JMenuItem menuItem) {
        if (null == menuItemResolver) {
            menuItemResolver = ParameterResolution.forClass(JMenuItem.class, menuItem);
        }
        return menuItemResolver;
    }


    /**
     * Fire the command in question.
     * @param clazz The expected result type
     * @param defaultValue The default value to return
     * @param injectedValues Values that can be injected into the invocation context
     */
    @SafeVarargs
    public final <T> T fireCommandWithResult(final Class<T> clazz,
                                             final T defaultValue,
                                             final Function<ParameterDescription, ParameterResolution>... injectedValues) {

        // Allow the injected values to be used as a parameter to handler methods (might be none)
        Arrays.stream(injectedValues).forEach(handler::registerParameterResolver);

        // Invoke the method, and return the result or default if null.
        T result = handler.invoke(clazz);
        return null != result ? result : defaultValue;
    }
}
