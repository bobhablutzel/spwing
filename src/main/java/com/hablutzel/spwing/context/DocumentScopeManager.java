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


import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.component.CommandMethods;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.util.BeanUtils;
import com.hablutzel.spwing.util.StampLockedObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The DocumentScopeManager class provides a mechanism for tracking the
 * current document scope, and for "document" scope objects that are
 * singletons within each document scope. The document scope
 * is used to provide the document-centric model where each document
 * maintains its own set of beans. This class also allows the activation
 * and deactivation of the document scopes in response to window events.
 *
 * @author Bob Hablutzel
 */

@Slf4j
@RequiredArgsConstructor
public class DocumentScopeManager {


    private final ApplicationContext applicationContext;

    /**
     * The current document ID. This is managed in a thread-safe manner for the off
     * chance that there is an attempt to manipulate the active document scope outside
     * the main execution thread.
     */
    private final StampLockedObject<UUID> currentDocumentID = new StampLockedObject<>();


    /**
     * Contains the document id to bean storage map.
     */
    private final Map<UUID, DocumentSession> documentSessionMap = new HashMap<>();

    @Setter
    private Spwing spwing;



    /**
     * Get the bean from the active document bean store
     *
     * @param beanName The bean name
     * @return The (potentiall null) bean
     */
    public Object getBeanFromActiveDocumentBeanStore(final String beanName, final Supplier<Object> supplier) {
        return currentDocumentID.withOptimisticRead(id -> {

            // If we don't have an active document scope, then it's null by definition
            if (id == null) {
                return null;
            } else {

                // Get the active session, see if the bean is in the map
                DocumentSession documentSession = documentSessionMap.get(id);
                final Map<String, Object> scopedBeans = documentSession.getScopedBeans();

                // Return it if we have it, otherwise use the supplier to create it, store it, and return that
                if (scopedBeans.containsKey(beanName)) {
                    return scopedBeans.get(beanName);
                } else {
                    Object newBean = supplier.get();
                    documentSession.addBeanToScope(beanName, newBean);
                    return newBean;
                }
            }
        });
    }



    /**
     * Get the model object from the current document session
     *
     * @return The model object.
     */
    public Object getActiveModel() {
        return currentDocumentID.withOptimisticRead(id -> id == null ? null : documentSessionMap.get(id).getModel());
    }

    public Object getActiveController() {
        return currentDocumentID.withOptimisticRead(id -> id == null ? null : documentSessionMap.get(id).getController());
    }



    public DocumentSession getActiveSession() {
        return currentDocumentID.withOptimisticRead( id -> id == null ? null : documentSessionMap.get(id));
    }


    public UUID getActiveDocumentID() {
        return currentDocumentID.withOptimisticRead(id -> id);
    }

    public Map<String, CommandMethods> getCommandMethodsMap() {
        return currentDocumentID.withOptimisticRead( id -> id == null ? null : documentSessionMap.get(id).getCommandMethodsMap());
    }

    public DocumentEventDispatcher getDocumentEventDispatcher() {
        return currentDocumentID.withOptimisticRead( id -> id == null ? null : documentSessionMap.get(id).getDocumentEventDispatcher());
    }

    /**
     * Remove the named bean from the document store
     *
     * @param beanName The bean name
     * @return The removed object.
     */
    public Object removeBeanFromActiveDocumentBeanStore(String beanName) {

        // Since this has a side effect, we cannot use an optimistic read that might retry.
        return currentDocumentID.withRead(id -> id == null ? null : documentSessionMap.get(id).forgetBean(beanName));
    }


    public void resetScope(final UUID documentScopeID) {
        currentDocumentID.replace(id -> documentScopeID);
    }



    public void activateScope(final UUID documentScopeID) {
        currentDocumentID.replace( currentDocumentID -> {
            if (documentScopeID.equals(currentDocumentID)) {
                return currentDocumentID;
            } else {

                // Remove the current document scope (if any)
                if (null != currentDocumentID) {
                    deactivateScopeUnderLock();
                }

                // Give Spwing the heads up so it can rebuild the menus if needed
                DocumentSession documentSession = documentSessionMap.get(documentScopeID);
                if (null != spwing) {
                    spwing.documentSessionChanged(documentSession.getAvailableHandlers());
                }

                return documentScopeID;
            }
        });
    }


    /**
     * Called to deactivate a scope, knowing that the {@link #currentDocumentID}
     * is under a write lock.
     */
    private void deactivateScopeUnderLock() {
        spwing.documentSessionChanged(null);
        BeanUtils.removeBeanByClass(applicationContext, DocumentSession.class);
    }


    /**
     * Called when the model is completely deactivated, typically when the
     * window associated with the model view closes.
     *
     * @param documentID The document ID
     */
    public void disposeDocumentScope(UUID documentID ) {
        documentSessionMap.remove(documentID);
        currentDocumentID.replace( currentID -> {
            if (currentID.equals(documentID)) {
                deactivateScopeUnderLock();
                return null;
            } else {
                return currentID;
            }
        } );
    }


    public boolean hasActiveSession() {
        return currentDocumentID.withOptimisticRead(Objects::nonNull);
    }


    /**
     * Create a new document session with the given model object
     * populated.
     *
     * @param documentSession The newly created {@link DocumentSession} instance
     */
    public UUID establishSession(final DocumentSession documentSession) {

        // Create a new document session ID
        UUID result = UUID.randomUUID();
        documentSession.setId(result);
        documentSessionMap.put(result, documentSession);

        // Activate our new scope
        this.activateScope(result);

        log.debug( "Created new document session {} ", result );
        return result;
    }



}

