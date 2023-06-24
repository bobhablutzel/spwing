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
import com.hablutzel.spwing.annotations.Controller;
import com.hablutzel.spwing.annotations.Handler;
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.component.CommandMethods;
import com.hablutzel.spwing.component.CommandMethodsScanner;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Data
@Slf4j
public class DocumentSession {

    private Object model;
    private Model modelAnnotation;
    private Object controller;
    private Controller controllerAnnotation;
    private File associatedFile;
    private final Map<String,Object> scopedBeans = new HashMap<>();
    private final DocumentEventDispatcher documentEventDispatcher = new DocumentEventDispatcher();

    @Getter
    private final List<Object> availableHandlers = new ArrayList<>();


    @Getter
    private Map<String, CommandMethods> commandMethodsMap;

    public DocumentSession( final List<Object> knownHandlers ) {
        this.availableHandlers.addAll(knownHandlers);
    }


    public void addBeanToScope( String beanName, Object bean ) {
        scopedBeans.put(beanName, bean);

        // If the bean is either a Model or a Controller, then add it to the handler stack
        // Also look for any embedded values, and add them to the stack (if they are not
        // already in the stack). This gives the embedded handlers a slightly higher
        // priority than the model or controller.
        final Class<?> beanClass = bean.getClass();
        checkForKeyDocumentScopeClasses(bean, beanClass);
    }

    private void checkForKeyDocumentScopeClasses(Object bean, Class<?> beanClass) {
        // Check for our key document-scope classes
        Model model = AnnotatedElementUtils.findMergedAnnotation(beanClass, Model.class);
        if (Objects.nonNull(model)) {
            // If this is the model, then add it as a handler.
            // Also store it as the model, and store the model annotation
            addHandler(bean, beanClass);
            this.model = bean;
            this.modelAnnotation = model;
        }

        // See if it's the controller (it could be both a model and controller)
        Controller controllerAnnotation = AnnotatedElementUtils.findMergedAnnotation(beanClass, Controller.class);
        if (Objects.nonNull(controllerAnnotation)){

            // It's the controller. We do have to set the controller and controller annotation
            // field in addition to adding this as a handler
            addHandler(bean, beanClass);

            log.debug( "Saving controller bean {}", bean);
            this.controller = bean;
            this.controllerAnnotation = controllerAnnotation;
        }
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
            if (Objects.nonNull(embeddedValue)) {
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
