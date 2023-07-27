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

package com.hablutzel.spwing.view.adapter;

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


@Slf4j
public class JComboBoxEventAdapter extends JComponentEventAdapter {

    /**
     * The handled event names
     */
    private static final Set<String> knownEventNames = Set.of(
            "actionPerformed",
            "itemStateChanged",
            "popupMenuCanceled", "popupMenuWillBecomeInvisible", "popupMenuWillBecomeVisible");


    /**
     * The JComboBox we are attached to
     */
    private final JComboBox<Object> comboBox;


    /**
     * Constructor
     *
     * @param comboBox The combo box
     * @param spwing   The framework instance
     */
    public JComboBoxEventAdapter(JComboBox<Object> comboBox, Spwing spwing) {
        super(comboBox, spwing);
        this.comboBox = comboBox;

        // Add a handler for the action event that fires commands as required
        comboBox.addActionListener(actionEvent -> {
            String actionCommand = actionEvent.getActionCommand();
            if (null != actionCommand && !actionCommand.isBlank()) {
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
     * @param invoker   The {@link Invoker}
     */
    @Override
    public void attachListener(String eventName, Invoker invoker) {
        if ("actionPerformed".equals(eventName)) {
            comboBox.addActionListener(actionEvent -> this.callInvokerForAction(actionEvent, invoker));
        } else if ("itemStateChanged".equals(eventName)) {
            comboBox.addItemListener(itemEvent -> this.callInvokerForAction(itemEvent, invoker));
        } else if ("popupMenuCanceled".equals(eventName)) {
            comboBox.addPopupMenuListener(new PopupMenuAdapter() {
                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    JComboBoxEventAdapter.this.callInvokerForAction(e, invoker);
                }
            });
        } else if ("popupMenuWillBecomeInvisible".equals(eventName)) {
            comboBox.addPopupMenuListener(new PopupMenuAdapter() {
                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    JComboBoxEventAdapter.this.callInvokerForAction(e, invoker);
                }
            });
        } else if ("popupMenuWillBecomeVisible".equals(eventName)) {
            comboBox.addPopupMenuListener(new PopupMenuAdapter() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    JComboBoxEventAdapter.this.callInvokerForAction(e, invoker);
                }
            });
        } else {
            super.attachListener(eventName, invoker);
        }
    }

    /**
     * Checks to see if we understand the given event name
     *
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
        Set<Function<ParameterDescription, ParameterResolution>> result = new HashSet<>(super.getInjectedParameters());
        result.add(ParameterResolution.forClass(JComboBox.class, comboBox));
        return result;
    }


    private static class PopupMenuAdapter implements PopupMenuListener {
        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {
        }
    }
}
