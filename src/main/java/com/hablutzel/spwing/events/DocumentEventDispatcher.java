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

@Slf4j
public class DocumentEventDispatcher {

    private final Map<String, Set<Invoker>> consumerMap = new HashMap<>();

    @Getter
    private final Map<String, EventAdapter> eventAdapterMap = new HashMap<>();


    public static DocumentEventDispatcher get(ApplicationContext applicationContext) {
        DocumentScopeManager documentScopeManager = applicationContext.getBean("documentScopeManager", DocumentScopeManager.class);
        return documentScopeManager.getDocumentEventDispatcher();
    }

    @EventListener
    public void handle(DocumentEvent documentEvent) {

        // If we have a consumer for the event, dispatch it
        if (consumerMap.containsKey(documentEvent.getEventName())) {
            consumerMap.get(documentEvent.getEventName()).forEach(invoker -> {
                invoker.registerParameterResolver(ParameterResolution.forClass(DocumentEvent.class, documentEvent));
                invoker.invoke();
            });
        }
    }


    public void registerListener(String trigger, Invoker invoker) {
        if (!consumerMap.containsKey(trigger)) {
            consumerMap.put(trigger, new HashSet<>());
        }
        consumerMap.get(trigger).add(invoker);
    }

    public void registerEventAdapter(String name, EventAdapter eventAdapter) {
        eventAdapterMap.put(name, eventAdapter);
    }

}
