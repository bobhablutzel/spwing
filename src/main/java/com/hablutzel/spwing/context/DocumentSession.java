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
import com.hablutzel.spwing.annotations.Handler;
import com.hablutzel.spwing.command.CommandAwareUndoManager;
import com.hablutzel.spwing.component.CommandMethods;
import com.hablutzel.spwing.component.CommandMethodsScanner;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.model.ModelConfiguration;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Stores the state of a single "document". A document is a
 * tuple of [model, view, controller] and generally appears
 * as a separate window to the user. Documents maintain their
 * own individual state for key elements - the undo/redo stack,
 * the document event dispatchers, the document scoped beans,
 * and so forth.
 *
 * @author Bob Hablutzel
 */
@Data
@Slf4j
public class DocumentSession {

    private UUID id;

    /**
     * The model instance
     */
    private Object model;

    /**
     * The {@link ModelConfiguration} instance
     */
    private ModelConfiguration<?> modelConfiguration;

    private Class<?> modelClass;

    /**
     * The controller
     */
    private Object controller;

    private Class<?> controllerClass;

    private JFrame frame;

    /**
     * The associated file, if any.
     */
    private File associatedFile;

    /**
     * The document scoped beans.
     * @see DocumentScope
     * @see DocumentScopeManager
     */
    private final Map<String,Object> scopedBeans = new HashMap<>();

    /**
     * The document event dispatcher instance
     */
    private final DocumentEventDispatcher documentEventDispatcher = new DocumentEventDispatcher();

    /**
     * The document-local undo manager
     */
    private final CommandAwareUndoManager undoManager = new CommandAwareUndoManager();

    /**
     * The global application context
     */
    private final ApplicationContext applicationContext;


    /**
     * The active handler list
     */
    @Getter
    private final List<Object> availableHandlers = new ArrayList<>();


    /**
     * Command methods for this document scope
     * @see CommandMethods
     */
    @Getter
    private Map<String, CommandMethods> commandMethodsMap;


    /**
     * Constructor
     * @param applicationContext The global application context
     * @param applicationScopeHandlers The application level handlers
     */
    public DocumentSession(final ApplicationContext applicationContext,
                           final List<Object> applicationScopeHandlers ) {
        this.applicationContext = applicationContext;
        this.availableHandlers.addAll(applicationScopeHandlers);
    }

    /**
     * Establishes the model for the session
     * @param model The model
     */
    public void setModel(Object model) {
        this.model = model;
        processHandlerBean(model, classToBeanName(modelClass));
    }


    /**
     * Establishes the controller for the session
     * @param controller The controller
     */
    public void setController(Object controller) {
        this.controller = controller;
        processHandlerBean(controller, classToBeanName(controllerClass));
    }


    private String classToBeanName(Class<?> clazz) {
        String className = clazz.getSimpleName();
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }


    /**
     * Adds a handler bean to the handler set. This is called
     * when the controller and model are added to the session
     * @param handlerBean The handler bean
     */
    private void processHandlerBean(Object handlerBean, String beanName) {

        // Add to the handler set
        this.availableHandlers.add(handlerBean);

        // If the model class implements any of the framework aware classes, make sure
        // they are handled here (the normal post-create factory is skipped for these model objects)
        simulatePostCreateBehaviors(handlerBean, beanName);

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
     * @param handlerBean The model object
     */
    private void simulatePostCreateBehaviors(Object handlerBean, String beanName) {
        applicationContext.getAutowireCapableBeanFactory()
                .applyBeanPostProcessorsAfterInitialization(handlerBean, beanName);
    }



    public void addBeanToScope( final Class<?> beanClass,
                                final Object bean ) {
        addBeanToScope(classToBeanName(beanClass), bean);
    }

    /**
     * Add a new document-scope bean
     * @param beanName The bean name
     * @param bean The bean
     */
    public void addBeanToScope( final String beanName,
                                final Object bean ) {
        scopedBeans.put(beanName, bean);
    }

    /**
     * Add a newly found handler, and look through the Fields of that
     * instance to see if there are embedded handlers.
     *
     * @param bean The bean
     * @param beanClass The bean class
     */
    private void addHandler(Object bean, Class<?> beanClass) {

        // Don't add one already in the list
        if (!availableHandlers.contains(bean)) {

            // Mark this as a handler
            availableHandlers.add(bean);

            // Recurse on that bean to see if it contains embedded handlers.
            Arrays.stream(beanClass.getDeclaredFields())
                    .filter(field -> (field.getModifiers() & Modifier.FINAL) == Modifier.FINAL)
                    .filter(field -> AnnotatedElementUtils.hasAnnotation(field.getType(), Handler.class))
                    .forEach(field -> evaluatePotentialHandler(bean, field));
        }
    }


    /**
     * Look at a field from the new handler to see if there is the need for that
     * to be stored as a handler.
     *
     * @param bean The bean
     * @param field The field of the bean
     */
    private void evaluatePotentialHandler(Object bean, Field field) {
        log.debug("Evaluating field {} on {}", field.getName(), bean.getClass().getName());
        try {
            field.setAccessible(true);
            Object embeddedValue = field.get(bean);
            if (null != embeddedValue) {
                addHandler(bean, bean.getClass());
            }
        } catch (IllegalAccessException e) {
            log.warn("Skipping embedded handler {} as it is inaccessible", field.getName());
        }
    }

    public Object getBean(String beanName) {
        return scopedBeans.get(beanName);
    }

    public Object forgetBean(String beanName) {
        Object theBean = scopedBeans.remove(beanName);
        availableHandlers.remove(theBean);
        return theBean;
    }

    public void scanHandlers(Spwing spwing, CommandMethodsScanner commandMethodsScanner) {
        this.commandMethodsMap = commandMethodsScanner.scanDocumentComponents(spwing, availableHandlers);
    }


}
