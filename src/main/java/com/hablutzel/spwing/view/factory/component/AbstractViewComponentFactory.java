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


import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.view.adapter.EventAdapter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

import java.awt.Component;
import java.util.function.BiFunction;

/**
 * Defines an abstract implementation of a view factory. This provides
 * some helper functions for adding {@link com.hablutzel.spwing.view.adapter.EventAdapter}
 * instances to the newly created component.
 * @param <T> A class derived from {@link java.awt.Component}
 * @author Bob Hablutzel
 */
@RequiredArgsConstructor
@Slf4j
public abstract class AbstractViewComponentFactory<T extends Component> implements ViewComponentFactory<T>, ApplicationContextAware {


    @Setter
    private ApplicationContext applicationContext;

    /**
     * Applies an event adapter to the newly created object.
     * @param component The newly created component
     * @param name The name of the component. This is needed for registering the event adapter to allow
     *             for name-based discovery.
     * @param createAdapter A creation function, taking an component and a {@link Spwing} component and returning
     *                      an {@link EventAdapter}
     */
    protected void registerAdapter(@NonNull final T component,
                                   @NonNull final String name,
                                   @NonNull final BiFunction<T, Spwing, EventAdapter> createAdapter ) {

        // Make sure the name is valid
        if (name.isBlank()) {
            log.error("Ignoring an attempt to register a component with a blank name" );
        } else {

            // Set the component name
            component.setName(name);

            // Get the Spwing bean, and from there the current document event dispatcher
            final Spwing spwing = applicationContext.getBean(Spwing.class);
            DocumentEventDispatcher dispatcher = spwing.getDocumentScopeManager().getDocumentEventDispatcher();

            // Get the event adapter for this object
            dispatcher.registerEventAdapter(name, createAdapter.apply(component, spwing));
        }
    }
}
