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

package com.hablutzel.spwing.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * Simple helper class for classes that want to use property
 * change events for binding. Users can subclass this document,
 * and call {@link #signalChange(String, Object, Object)}
 * when a property changes.
 * @author Bob Hablutzel
 */
public class PropertyChangeModel implements Serializable {


    /**
     * The serialization serialization number
     */
    @Serial
    private static final long serialVersionUID = 0x47696e6e79446f67L;


    /**
     * The set of listeners
     */
    private transient Set<PropertyChangeListener> listeners = new HashSet<>();


    /**
     * Add a new property listener
     * @param propertyChangeListener The listener
     */
    public void addPropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        if (null == listeners) {
            listeners = new HashSet<>();
        }
        this.listeners.add(propertyChangeListener);
    }


    /**
     * Remove a listener. Safe to call if the listener is not present
     * @param propertyChangeListener The listener
     */
    public void removePropertyChangeListener(final PropertyChangeListener propertyChangeListener) {
        if (null != listeners) {
            listeners.remove(propertyChangeListener);
        }
    }


    /**
     * Signal a property change.
     *
     * @param propertyName The property name
     * @param oldValue The old value
     * @param newValue The new value
     */
    public void signalChange(final String propertyName, Object oldValue, Object newValue ) {
        if (null != listeners) {
            PropertyChangeEvent changeEvent = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
            listeners.forEach(listeners -> listeners.propertyChange(changeEvent));
        }
    }


}
