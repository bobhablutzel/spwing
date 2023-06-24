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

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


@Slf4j
public class JComponentEventAdapter extends ContainerEventAdapter {

    private static final Set<String> knownEventNames = Set.of(
            "ancestorAdded", "ancestorRemoved", "ancestorMoved" );

    private final JComponent component;

    public JComponentEventAdapter(JComponent component) {
        super(component);
        this.component = component;
    }


    @Override
    public void attachListener(String eventName, Invoker invoker) {
        log.debug( "In JComponentEventAdapter for event {}", eventName);
        switch (eventName) {
            case "ancestorAdded" -> component.addAncestorListener(new AncestorListenerAdapter() {
                @Override
                public void ancestorAdded(AncestorEvent event) {
                    JComponentEventAdapter.this.callInvokerForAction(event, invoker);

                }
            });
            case "ancestorRemoved" -> component.addAncestorListener(new AncestorListenerAdapter() {
                @Override
                public void ancestorRemoved(AncestorEvent event) {
                    JComponentEventAdapter.this.callInvokerForAction(event, invoker);

                }
            });
            case "ancestorMoved" -> component.addAncestorListener(new AncestorListenerAdapter() {
                @Override
                public void ancestorMoved(AncestorEvent event) {
                    JComponentEventAdapter.this.callInvokerForAction(event, invoker);

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
        return Map.of(JComponent.class, () -> component);
    }


    private static class AncestorListenerAdapter implements AncestorListener {

        @Override
        public void ancestorAdded(AncestorEvent event) {}
            @Override
        public void ancestorRemoved(AncestorEvent event) {}

        @Override
        public void ancestorMoved(AncestorEvent event) {}
    }



}
