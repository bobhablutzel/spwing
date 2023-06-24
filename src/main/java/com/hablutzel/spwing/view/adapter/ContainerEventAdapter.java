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
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


@Slf4j
public class ContainerEventAdapter extends ComponentEventAdapter {

    private static final Set<String> knownEventNames = Set.of(
            "componentAdded", "componentRemoved", "componentShown", "componentHidden" );

    private final Container container;

    public ContainerEventAdapter(Container container) {
        super(container);
        this.container = container;
    }

    @Override
    public void attachListener(String eventName, Invoker invoker) {
        log.debug( "In ContainerEventAdapter for event {}", eventName);
        switch (eventName) {
            case "componentAdded" -> container.addContainerListener( new ContainerAdapter() {
                @Override
                public void componentAdded(ContainerEvent containerEvent) {
                    ContainerEventAdapter.this.callInvokerForAction(containerEvent, invoker);
                }
            });
            case "componentRemoved" -> container.addContainerListener( new ContainerAdapter() {
                @Override
                public void componentRemoved(ContainerEvent containerEvent) {
                    ContainerEventAdapter.this.callInvokerForAction(containerEvent, invoker);
                }
            });
            case "componentShown" -> container.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentShown(ComponentEvent componentEvent) {
                    ContainerEventAdapter.this.callInvokerForAction(componentEvent, invoker);
                }
            });
            case "componentHidden" -> container.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentHidden(ComponentEvent componentEvent) {
                    ContainerEventAdapter.this.callInvokerForAction(componentEvent, invoker);
                }
            });
            default -> super.attachListener(eventName, invoker);
        }
    }

    @Override
    public boolean understands(String eventName) {
        return knownEventNames.contains(eventName) || super.understands(eventName);
    }

    @Override
    protected Map<Class<?>, Supplier<Object>> getParameterMap() {
        return Map.of(Container.class, () -> container);
    }
}
