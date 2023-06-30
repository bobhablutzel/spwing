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

package com.hablutzel.spwing.view.adapter;

import com.hablutzel.spwing.invoke.Invoker;

import javax.swing.*;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


/**
 * An event adapter for abstract buttons. This
 * class handles single abstract buttons, see the
 * {@link com.hablutzel.spwing.view.bind.watch.ButtonGroupSelectedBinder}
 * class for button groups.
 *
 * @author Bob Hablutzel
 */
public class AbstractButtonEventAdapter extends JComponentEventAdapter {

    /**
     * The handled event names
     */
    private static final Set<String> knownEventNames = Set.of(
            "actionPerformed", "stateChanged", "itemStateChanged", "clicked" );

    /**
     * The button we are adapting
     */
    private final AbstractButton abstractButton;

    /**
     * Constructor
     *
     * @param abstractButton The abstract button
     */
    public AbstractButtonEventAdapter(AbstractButton abstractButton) {
        super(abstractButton);
        this.abstractButton = abstractButton;
    }


    /**
     * Called to attach listeners for the given event name. Checks
     * to see if this is an event name we handle, and if not passes
     * to the superclass. If we do, binds the event to the given
     * {@link Invoker}
     *
     * @param eventName The event name
     * @param invoker The {@link Invoker}
     */
    @Override
    public void attachListener(String eventName, Invoker invoker) {
        if ("actionPerformed".equals(eventName)) {
            abstractButton.addActionListener(actionEvent -> this.callInvokerForAction(actionEvent, invoker));
        } else if ("clicked".equals(eventName)) {
            abstractButton.addActionListener(actionEvent -> this.callInvokerForAction(actionEvent, invoker));
        } else if ("stateChanged".equals(eventName)) {
            abstractButton.addChangeListener(changeEvent -> this.callInvokerForAction(changeEvent, invoker));
        } else if ("itemStateChanged".equals(eventName)) {
            abstractButton.addItemListener(itemEvent -> this.callInvokerForAction(itemEvent, invoker));
        } else {
            super.attachListener(eventName, invoker);
        }
    }

    /**
     * Checks to see if we understand the given event name
     * @param eventName The event name
     * @return TRUE if we (or the superclass) understand the event
     */
    @Override
    public boolean understands(String eventName) {
        return knownEventNames.contains(eventName) || super.understands(eventName);
    }


    /**
     * Gets the parameters that can be passed to the {@link Invoker}
     * associated with our events when it is called. This gives the
     * most class-specific parameter passing.
     *
     * @return The map of suppliers.
     */
    @Override
    protected Map<Class<?>, Supplier<Object>> getParameterMap() {
        return Map.of(AbstractButton.class, () -> abstractButton,
                Boolean.class, abstractButton::isSelected);
    }

}
