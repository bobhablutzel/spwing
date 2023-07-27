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

package com.hablutzel.spwing.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.NonNull;


/**
 * DocumentScope implements the document-scope mechanism
 * for the Spwing framework (equivalent to session state
 * in a web framework). With this scope, there is a scope
 * associated with each "document" that the framework is
 * manipulating; the scope generally contains at least
 * a model and controller, but could have any number of beans
 * that are marked with "document" scope.
 *
 * @author Bob Hablutzel
 */
@Slf4j
@RequiredArgsConstructor
public class DocumentScope implements Scope {

    /**
     * The manager that actually contains the scope-level
     * beans and manages the mapping to active windows.
     */
    private final DocumentScopeManager documentScopeManager;

    /**
     * See if we have a bean in this scope with the given name. If
     * not, the bean will be created. The existance is deferred to the
     * {@link #documentScopeManager} which is tracking the active
     * document scope.
     *
     * @param name The bean name
     * @param objectFactory The object factory if the bean does not exist
     * @return The bean
     */
    @Override
    public @NonNull Object get( @NonNull String name, @NonNull ObjectFactory<?> objectFactory) {

        // Check for the bean in the active document scope. If not found
        // then we have to create, store, and return it.
        // Create, stamp, and return the new object.
        return documentScopeManager.getBeanFromActiveDocumentBeanStore(name, objectFactory::getObject);
    }

    /**
     * Remove the bean from the current document scope.
     * @param name The name to remove
     * @return The bean that was removed
     */
    @Override
    public Object remove( @NonNull String name) {
        return documentScopeManager.removeBeanFromActiveDocumentBeanStore(name);
    }


    @Override
    public void registerDestructionCallback( @NonNull String name, @NonNull  Runnable callback) {
        // No implementation
    }

    @Override
    public Object resolveContextualObject( @NonNull String key) {
        // No implementation
        return null;
    }

    @Override
    public String getConversationId() {
        // No implementation
        return null;
    }
}
