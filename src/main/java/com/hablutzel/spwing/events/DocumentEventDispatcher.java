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

package com.hablutzel.spwing.events;


import com.hablutzel.spwing.context.DocumentScopeManager;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterResolution;
import com.hablutzel.spwing.view.adapter.EventAdapter;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DocumentEventDispatchers are used for two different event
 * types supported by the Spwing framework: AWT events and document
 * events.<br/>
 * AWT events are emitted by the AWT/Swing components when specific
 * activities happen: mouse movements, etc. The document event dispatch
 * class maintains the list of {@link EventAdapter} instances that are created
 * during the view creation process; each component that is created
 * gets an event adapter that can be asked to listen to AWT events and
 * call an invoker if that happens. The event adapters don't listen for
 * the events until and unless there is a listener method discovered.<br/>
 * Document events are rarer than AWT events and are specific to the model
 * or controller logic. These are often not needed as property change listeners
 * provide enough functionality, but are available to handle events that
 * are disconnected from model properties or AWT/Swing events.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public class DocumentEventDispatcher {

    /**
     * The map of {@link Invoker} instance for the event names
     */
    private final Map<String, Set<Invoker>> consumerMap = new HashMap<>();

    /**
     * The event adapters used for AWT events
     */
    @Getter
    private final Map<String, EventAdapter> eventAdapterMap = new HashMap<>();


    /**
     * Get the {@link DocumentEventDispatcher} for the current document. This
     * finds the current document session and the dispatcher from that session
     *
     * @param applicationContext The {@link ApplicationContext} instance
     * @return The (possibly null) {@link DocumentEventDispatcher}
     */
    public static DocumentEventDispatcher get(ApplicationContext applicationContext) {
        DocumentScopeManager documentScopeManager = applicationContext.getBean("documentScopeManager", DocumentScopeManager.class);
        return documentScopeManager.getDocumentEventDispatcher();
    }


    /**
     * Listens for a document event. Document events are built on top of
     * the Spring event functionality but are document aware.
     *
     * @param documentEvent The {@link DocumentEvent} being signalled
     */
    @EventListener
    public void handle(final DocumentEvent documentEvent) {

        // If we have a consumer for the event, dispatch it
        if (consumerMap.containsKey(documentEvent.getEventName())) {
            consumerMap.get(documentEvent.getEventName()).forEach(invoker -> {
                invoker.registerParameterResolver(ParameterResolution.forClass(DocumentEvent.class, documentEvent));
                invoker.invoke();
            });
        }
    }


    /**
     * Register a listener for an event.
     * @param trigger The event name
     * @param invoker The {@link Invoker} to invoke
     */
    public void registerListener(String trigger, Invoker invoker) {
        if (!consumerMap.containsKey(trigger)) {
            consumerMap.put(trigger, new HashSet<>());
        }
        consumerMap.get(trigger).add(invoker);
    }


    /**
     * Register an event adapter during view creation
     *
     * @param name The name of the component
     * @param eventAdapter The {@link EventAdapter} instance
     */
    public void registerEventAdapter(final String name,
                                     final EventAdapter eventAdapter) {
        eventAdapterMap.put(name, eventAdapter);
    }

}
