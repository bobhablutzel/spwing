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

package com.hablutzel.spwing.util;


import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.aware.DocumentEventDispatcherAware;
import com.hablutzel.spwing.aware.DocumentEventPublisherAware;
import com.hablutzel.spwing.aware.DocumentIDAware;
import com.hablutzel.spwing.aware.SpwingAware;
import com.hablutzel.spwing.context.DocumentScopeManager;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.events.DocumentEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Objects;


/**
 * This bean post processor looks for beans that implement interfaces
 * that make them aware of framework components:
 * <ul>
 *     <li>{@link DocumentEventDispatcherAware}</li>
 *     <li>{@link DocumentEventPublisherAware}</li>
 *     <li>{@link SpwingAware}</li>
 * </ul>
 *
 * @author Bob Hablutzel
 */
@Slf4j
@RequiredArgsConstructor
public class KnownObjectsInjector implements BeanPostProcessor {

    private final DocumentScopeManager documentScopeManager;

    @Setter
    private Spwing spwing;

    /**
     * A new bean was created; see if we need to inject an object.
     *
     * @param bean     The bean
     * @param beanName The bean name
     * @return The bean
     * @throws BeansException A bean exception (if anything happens)
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof SpwingAware spwingAware) {
            spwingAware.setUi(spwing);
        }

        DocumentEventDispatcher documentEventDispatcher = documentScopeManager.getDocumentEventDispatcher();
        if (Objects.nonNull(documentEventDispatcher)) {
            if (bean instanceof DocumentEventDispatcherAware documentEventDispatcherAware) {
                documentEventDispatcherAware.setDocumentEventDispatcher(documentEventDispatcher);
            }
            if (bean instanceof DocumentEventPublisherAware documentEventPublisherAware) {
                documentEventPublisherAware.setDocumentEventPublisher(new DocumentEventPublisher(documentEventDispatcher));
            }
        }

        // See if there is an active session and a DocumentIDAware bean
        if (bean instanceof DocumentIDAware documentIDAware) {
            documentIDAware.setDocumentID(documentScopeManager.getActiveDocumentID());
        }
        return bean;
    }
}

