/*
 * Copyright © 2023. Hablutzel Consulting, LLC. All rights reserved.
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
 *
 */

package com.hablutzel.spwing.view.factory.component;

import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import org.springframework.core.convert.ConversionService;

import java.awt.Component;

/**
 * Defines the interface for {@link org.springframework.stereotype.Service} instances
 * that create components in the views. Spwing defines a set of these
 * for standard non-abstract Swing components, but users can define their
 * own instances (even at the document scope level) for integrating
 * custom elements.
 *
 * @param <T> A class
 * @author Bob Hablutzel
 */
public interface ViewComponentFactory<T extends Component> {

    /**
     * Build a new instance with the supplied name. Starting with
     * 0.6.2, this returns a {@link Cocoon} instance that can be used
     * to continue to evolve the component during the view build process.
     *
     * @param name The name
     * @return A new {@link Cocoon<T>} instance
     */
    Cocoon<T> build(final String name, final ConversionService conversionService);


    /**
     * Provide the alias for this factory. This is used (for example)
     * to provide the name used by the SVWF definition file. This can
     * be any name but cannot contain spaces or other whitespace.
     * @return The alias name
     */
    String alias();

}
