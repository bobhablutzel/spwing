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

package com.hablutzel.spwing.view.factory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


/**
 * Refresh listeners are used to bridge between component properties
 * and model objects. This allows there to be a single refresh listener
 * for all the bound properties of a component.
 *
 * @author Bob Hablutzel
 */
public class RefreshListener implements PropertyChangeListener {

    /**
     * The map of consumers for property changes
     */
    private final Map<String, Consumer<PropertyChangeEvent>> consumers = new HashMap<>();


    /**
     * Handle a property change by finding the property change event
     * consumer.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *          and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (consumers.containsKey(evt.getPropertyName())) {
            consumers.get(evt.getPropertyName()).accept(evt);
        }
    }


    /**
     * Add a new property change event consumer.
     *
     * @param propertyName The property name
     * @param consumer The consumer of the property change event
     */
    public void addEventConsumer( final String propertyName, Consumer<PropertyChangeEvent> consumer ) {
        consumers.put(propertyName, consumer );
    }

}
