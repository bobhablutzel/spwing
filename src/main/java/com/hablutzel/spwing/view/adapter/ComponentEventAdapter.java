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

import com.hablutzel.spwing.context.EventAdapter;
import com.hablutzel.spwing.invoke.Invoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


@RequiredArgsConstructor
@Slf4j
public abstract class ComponentEventAdapter implements EventAdapter {

    /**
     * The event names understood at this level
     */
    private static final Set<String> knownEventNames = Set.of(
            "mouseClicked", "mousePressed", "mouseReleased", "mouseEntered", "mouseExited", "mouseWheelMoved", "mouseMoved",
            "componentResized", "componentMoved", "componentShown", "componentHidden", "focusGained", "focusLost",
            "hierarchyAncestorMoved", "ancestorResized", "hierarchyChanged",
            "caretPositionChanged", "inputMethodTextChanged", "keyTyped", "keyPressed", "keyReleased"
    );

    /**
     * The {@link Component} being adapted
     */
    private final Component component;

    /**
     * When the {@link Invoker} is called, we want to be able to
     * provide at minimum the component that is being watched. This
     * method gives the adapter an opportunity to provide those
     * additional parameters to inject into the {@link Invoker} before
     * the method is called.
     * @return A map of classes to value {@link Supplier} instances.
     */
    protected abstract Map<Class<?>,Supplier<Object>> getParameterMap();


    /**
     * See if we can attach a listener for the given event.
     *
     * @param eventName The event name
     * @param invoker The {@link Invoker} to call when the event occurs
     */
    @Override
    public void attachListener(String eventName, Invoker invoker) {
        log.debug( "In ComponentEventAdapter (component {}) for event {}", component, eventName);
        switch (eventName) {
            case "mouseClicked" -> component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker);
                }
            });
            case "mousePressed" -> component.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent mouseEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker);
                }
            });
            case "mouseReleased" -> component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent mouseEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker);
                }
            });
            case "mouseEntered" -> component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent mouseEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker);
                }
            });
            case "mouseExited" -> component.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent mouseEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker);
                }
            });
            case "mouseWheelMoved" -> component.addMouseWheelListener(mouseEvent ->
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker));
            case "mouseDragged" -> component.addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent mouseEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker);
                }
            });
            case "mouseMoved" -> component.addMouseMotionListener(new MouseAdapter() {
                    @Override
                    public void mouseMoved(MouseEvent mouseEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(mouseEvent, invoker);
                }
            });
            case "componentResized" -> component.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent componentEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(componentEvent, invoker);
                }
            });
            case "componentMoved" -> component.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent componentEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(componentEvent, invoker);
                }
            });
            case "componentShown" -> component.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent componentEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(componentEvent, invoker);
                }
            });
            case "componentHidden" -> component.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent componentEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(componentEvent, invoker);
                }
            });
            case "focusGained" -> component.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent focusEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(focusEvent, invoker);
                }
            });
            case "focusLost" -> component.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent focusEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(focusEvent, invoker);
                }
            });
            case "hierarchyAncestorMoved" -> component.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
                @Override
                public void ancestorMoved(HierarchyEvent hierarchyEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(hierarchyEvent, invoker);
                }
            });
            case "ancestorResized" -> component.addHierarchyBoundsListener(new HierarchyBoundsAdapter() {
                @Override
                public void ancestorResized(HierarchyEvent hierarchyEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(hierarchyEvent, invoker);
                }
            });
            case "hierarchyChanged" ->
                    component.addHierarchyListener(hierarchyEvent -> ComponentEventAdapter.this.callInvokerForAction(hierarchyEvent, invoker));
            case "caretPositionChanged" -> component.addInputMethodListener(new InputMethodListenerAdapter() {
                @Override
                public void caretPositionChanged(InputMethodEvent event) {
                    ComponentEventAdapter.this.callInvokerForAction(event, invoker);
                }
            });
            case "inputMethodTextChanged" -> component.addInputMethodListener(new InputMethodListenerAdapter() {
                @Override
                public void inputMethodTextChanged(InputMethodEvent event) {
                    ComponentEventAdapter.this.callInvokerForAction(event, invoker);
                }
            });
            case "keyTyped" -> component.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(keyEvent, invoker);
                }
            });
            case "keyPressed" -> component.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent keyEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(keyEvent, invoker);
                }
            });
            case "keyReleased" -> component.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent keyEvent) {
                    ComponentEventAdapter.this.callInvokerForAction(keyEvent, invoker);
                }
            });
            default -> {
                if (eventName.startsWith("Property")) {
                    String propertyName = eventName.substring("Property".length());
                    if (propertyName.isBlank()) {
                        component.addPropertyChangeListener(propertyChangedEvent -> ComponentEventAdapter.this.callInvokerForAction(propertyChangedEvent, invoker));
                    } else {
                        component.addPropertyChangeListener(propertyName, propertyChangedEvent -> ComponentEventAdapter.this.callInvokerForAction(propertyChangedEvent, invoker));
                    }
                } else {
                    log.warn("Unknown event registered for AWT Component: {} - ignored", eventName);
                }
            }
        }
    }


    /**
     * Returns TRUE if we can understand the given event
     *
     * @param eventName The event name
     * @return TRUE if we understand the event
     */
    @Override
    public boolean understands(String eventName) {
        return knownEventNames.contains(eventName) || eventName.startsWith("Property");
    }


    /**
     * Routine to call the {@link Invoker} in response to an event
     * being signalled by the {@link Component} we are tracking. This
     * method gives the adapter a chance to provide parameters that
     * should be added to the available parameter list; this is
     * minimally the {@link Component} being watched and the actual
     * event payload (if any) from the AWT event listener.
     *
     * @param eventObject The object that generated the event
     * @param invoker The invoker
     */
    protected void callInvokerForAction(EventObject eventObject, Invoker invoker) {
        invoker.registerParameterSupplier(eventObject.getClass(), () -> eventObject);
        getParameterMap().forEach(invoker::registerParameterSupplier);
        invoker.invoke();
    }


    /**
     * AWT doesn't provide an adapter for the {@link InputMethodListener}
     * interface with default values, so that is provided here.
     *
     * @author Bob Hablutzel
     */
    private static class InputMethodListenerAdapter implements InputMethodListener {

        /**
         * Default implementation does nothing.
         *
         * @param event the event to be processed
         */
        @Override
        public void inputMethodTextChanged(InputMethodEvent event) {
        }

        /**
         * Default implementation does nothing.
         *
         * @param event the event to be processed
         */
        @Override
        public void caretPositionChanged(InputMethodEvent event) {
        }
    }

}
