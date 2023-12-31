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

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


@Slf4j
public class WindowEventAdapter extends ContainerEventAdapter {

    private final Window window;

    public WindowEventAdapter(final Window window,
                              final Spwing spwing) {
        super(window, spwing);
        this.window = window;
    }

    private static final Set<String> knownEventNames = Set.of(
            "windowGainedFocus", "windowLostFocus",             // WindowFocusListener
            "windowActivated", "windowClosed", "windowClosing", "windowDeactivated", "windowDeiconified",
            "windowIconified", "windowOpened", // WindowListener
            "windowStateChanged" // WindowStateListener
    );

    @Override
    public void attachListener(String eventName, Invoker invoker) {
        log.debug( "In WindowEventAdapter (window {}) for event {}", window, eventName);
        switch (eventName) {
            case "windowGainedFocus" -> window.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowGainedFocus(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowLostFocus" -> window.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowLostFocus(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowActivated" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowClosed" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowClosing" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowDeactivated" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowDeactivated(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowDeiconified" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowIconified" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowIconified(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowOpened" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            case "windowStateChanged" -> window.addWindowStateListener(new WindowAdapter() {
                @Override
                public void windowStateChanged(WindowEvent windowEvent) {
                    WindowEventAdapter.this.callInvokerForAction(windowEvent, invoker);
                }
            });
            default -> super.attachListener(eventName, invoker);
        }
    }

    @Override
    public boolean understands(String eventName) {
        return knownEventNames.contains(eventName);
    }

    @Override
    protected Set<Function<ParameterDescription, ParameterResolution>> getInjectedParameters() {
        Set<Function<ParameterDescription,ParameterResolution>> result = new HashSet<>(super.getInjectedParameters());
        result.add(ParameterResolution.forClass(Window.class, window));
        return result;
    }


}
