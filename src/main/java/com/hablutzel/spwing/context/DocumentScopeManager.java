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
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.component.CommandMethods;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.util.BeanUtils;
import com.hablutzel.spwing.util.StampLockedObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.*;
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
     * Store a bean in the current document scope.
     *
     * @param beanName The bean name
     * @param bean     The bean
     */
    public void storeBeanInActiveDocumentBeanStore(String beanName, Object bean) {
        currentDocumentID.withOptimisticRead(documentID -> {
            if (Objects.nonNull(documentID)) {
                final DocumentSession documentSession = documentSessionMap.get(documentID);
                documentSession.addBeanToScope(beanName, bean);
            } else {
                log.error("No active document when storing into bean store");
            }
        });
    }


    /**
     * Returns the active handlers for this document session. These
     * are objects that are annotated with {@link com.hablutzel.spwing.annotations.Handler}
     * (or meta-annotated, for example {@link Model} and {@link com.hablutzel.spwing.annotations.Controller}
     * classes, that should be scanned for methods that are can handle commands or listen
     * to events.
     *
     * @return The active handler list
     */
    public List<Object> getActiveHandlers() {
        return currentDocumentID.withOptimisticRead(id -> id == null ? null : documentSessionMap.get(id).getAvailableHandlers());
    }



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



    public void activateScope(final UUID documentScopeID) {
        currentDocumentID.replace( currentDocumentID -> {
            if (documentScopeID.equals(currentDocumentID)) {
                return currentDocumentID;
            } else {

                // Remove the current document scope (if any)
                if (Objects.nonNull(currentDocumentID)) {
                    deactivateScopeUnderLock();
                }

                // Push this scope in the context
                DocumentSession documentSession = documentSessionMap.get(documentScopeID);
                BeanUtils.pushBean(applicationContext, documentSession);

                // Give Spwing the heads up so it can rebuild the menus if needed
                if (Objects.nonNull(spwing)) {
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
        currentDocumentID.replace( currentID -> {
            if (currentID.equals(documentID)) {
                deactivateScopeUnderLock();
                documentSessionMap.remove(documentID);
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
     * @param modelObject The model object to seed the document scope
     */
    public UUID establishSession(final DocumentSession documentSession, final Object modelObject) {

        // Create a new document session ID
        UUID result = UUID.randomUUID();

        // Set up a new document session for this model object
        documentSession.addBeanToScope(modelObject.getClass().getSimpleName(), modelObject);
        documentSessionMap.put(result, documentSession);

        // Activate our new scope
        this.activateScope(result);

        // If the model class implements any of the framework aware classes, make sure
        // they are handled here (the normal post-create factory is skipped for these model objects)
        simulatePostCreateBehaviors(modelObject);

        log.debug( "Created new document session {} based on {}", result, modelObject );
        return result;
    }



    /**
     * When creating a new document session, we use a model object as a
     * seed for the session. That object is created in the framework
     * code and passed in. That's fine, except some key activities that
     * occur when a new object is created - notably calling
     * the bean post-initialization process and stamping the bean with
     * framework injected values doesn't happen. This routine
     * simulates that automatic behavior on behalf of the created bean.
     *
     * @param modelObject The model object
     */
    private void simulatePostCreateBehaviors(Object modelObject) {
        applicationContext.getAutowireCapableBeanFactory()
                .applyBeanPostProcessorsAfterInitialization(modelObject, modelObject.getClass().getSimpleName());
    }

}

