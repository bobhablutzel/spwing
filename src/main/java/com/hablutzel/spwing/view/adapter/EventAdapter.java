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

import com.hablutzel.spwing.invoke.Invoker;

import javax.swing.event.AncestorListener;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.TextEvent;

/**
 * Event adapters are used to associate Spwing
 * {@link Invoker} instances to AWT component events.
 * In the Spwing model, events are given names; the
 * names of AWT events are the same as the name of the
 * method that would be called on the event listener in the
 * AWT native system. For example, a {@link java.awt.event.MouseListener#mouseClicked(MouseEvent)}
 * event will be translated into an event named "mouseClicked",
 * {@link java.awt.event.TextListener#textValueChanged(TextEvent)} to
 * "textValueChanged", etc. Exceptions to this rule are rare and
 * only when there are conflicts - e.g. {@link java.awt.event.HierarchyBoundsListener#ancestorMoved(HierarchyEvent)}
 * is mapped to "hierarchyAncestorMoved" in order to avoid conflicts
 * with the more Swing specific on from {@link javax.swing.JComponent#addAncestorListener(AncestorListener)}
 *
 * @author Bob Hablutzel
 */
public interface EventAdapter {


    /**
     * Attach a listener for the given name, if the name
     * maps to the events that are handled by this adapter.
     * @param eventName The event name
     * @param invoker The {@link Invoker} to call when the event occurs
     */
    void attachListener( String eventName, Invoker invoker);


    /**
     * Returns TRUE if this adapter can understand the specified
     * event.
     * @param eventName The event name
     * @return TRUE if this adapter can understand that event name
     */
    boolean understands(String eventName);


    /**
     * Fire a command to be handled via the handler stack (as opposed
     * to the event processing mechanisms
     * @param commandName The command name (from the actionCommand}
     * @param actionEvent The action event associated with the actionCommand
     */
    void fireCommand(String commandName, ActionEvent actionEvent);


}
