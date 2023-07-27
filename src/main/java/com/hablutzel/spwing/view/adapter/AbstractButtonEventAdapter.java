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

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


/**
 * An event adapter for abstract buttons. This
 * class handles single abstract buttons, see the
 * {@link com.hablutzel.spwing.view.bind.impl.ButtonGroupSelectedBinder}
 * class for button groups.
 *
 * @author Bob Hablutzel
 */
@Slf4j
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
     * @param spwing The framework instance
     */
    public AbstractButtonEventAdapter(final AbstractButton abstractButton,
                                      final Spwing spwing) {
        super(abstractButton, spwing);
        this.abstractButton = abstractButton;

        // Add a handler for the action event that fires commands as required
        // We do some checking here, because the default in Swing is to set the
        // action command to the text of the button. That generally won't be a
        // command in the Spwing world, so we check to see that the action command
        // is present, starts with a cmd, or at least doesn't equal the button text.
        abstractButton.addActionListener(actionEvent -> {
            String actionCommand = actionEvent.getActionCommand();
            if (null != actionCommand &&
                    !actionCommand.isBlank() &&
                    (actionCommand.startsWith("cmd") || !actionCommand.equals(abstractButton.getText()))) {
                log.debug( "Firing button command: {}", actionCommand);
                fireCommand(actionCommand, actionEvent);
            }
        });

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
    protected Set<Function<ParameterDescription, ParameterResolution>> getInjectedParameters() {
        HashSet<Function<ParameterDescription, ParameterResolution>> result = new HashSet<>(super.getInjectedParameters());
        result.add( ParameterResolution.forClass(AbstractButton.class, abstractButton));
        result.add( ParameterResolution.forClass(Boolean.class,abstractButton.isSelected()));
        return result;
    }

}
