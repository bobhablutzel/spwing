/*
 * Copyright © 2023. Hablutzel Consulting, LLC. All rights reserved.
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
 *
 */

package com.hablutzel.spwing.view.factory;


import com.hablutzel.spwing.context.EventAdapter;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.view.adapter.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.lang.NonNull;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyDescriptor;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class ComponentFactory {

    private final Object modelObject;
    private final Object controllerObject;
    private final DocumentEventDispatcher documentEventDispatcher;
    private final Map<String,Object> createdObjects;


    /**
     * The {@link ComponentFactory} tracks all the created objects.
     * When the build of the view is complete, the view factory
     * can then inject those created objects into either the
     * model or controller object (if provided). See {@link #injectCreatedComponentsInto(Object)}
     * for more details.
     */
    public void injectCreatedComponents() {
        injectCreatedComponentsInto(modelObject);
        injectCreatedComponentsInto(controllerObject);
    }


    /**
     * Inject created objects into the specified object. This will create
     * a BeanWrapper for the object, and use the settable properties
     * that match the object name and type for the created objects.
     * Objects have to have a name in order to be injectable.
     *
     * @param targetObject The object to inject into
     */
    private void injectCreatedComponentsInto(Object targetObject) {

        // Make sure we have an object, and create the wrapper
        if (Objects.nonNull(targetObject)) {
            BeanWrapper model = new BeanWrapperImpl(targetObject);

            // Loop through each descriptor, check to make sure it's writeable
            for (PropertyDescriptor propertyDescriptor : model.getPropertyDescriptors()) {
                final String descriptorName = propertyDescriptor.getName();
                if (model.isWritableProperty(descriptorName)) {

                    // If it's writable and there is an object with that name, see if the
                    // object is type assignable.
                    if (createdObjects.containsKey(descriptorName)) {
                        final Object createdObject = createdObjects.get(descriptorName);
                        if (propertyDescriptor.getPropertyType().isAssignableFrom(createdObject.getClass())) {

                            // If we get here, then set the property value.
                            model.setPropertyValue(descriptorName, createdObject);
                        }
                    }
                }
            }
        }
    }


    public JPanel newJPanel(String name) {
        return registerAdapter(new JPanel(), name, JPanelEventAdapter::new);
    }

    public JLabel newJLabel(String name) {
        return registerAdapter( new JLabel(), name, JLabelEventAdapter::new);
    }

    public JTextField newJTextField( @NonNull final String name) {
        return registerAdapter( new JTextField(name), name, JTextFieldEventAdapter::new);
    }

    public JButton newJButton(@NonNull final String name) {
        return registerAdapter( new JButton(name), name, JButtonEventAdapter::new);
    }
    public JCheckBox newJCheckBox(@NonNull final String name) {
        return registerAdapter( new JCheckBox(name), name, JCheckboxEventAdapter::new);
    }

    public JRadioButton newJRadioButton(@NonNull final String name) {
        return registerAdapter( new JRadioButton(name), name, JRadioButtonEventAdapter::new);
    }

    public ButtonGroup newButtonGroup(final String name) {
        ButtonGroup buttonGroup = new ButtonGroup();
        log.info( "Adding button group {}", name );
        createdObjects.put(name, buttonGroup);
        return buttonGroup;
    }

    public ButtonGroup newAnonymousButtonGroup() {
        return new ButtonGroup();
    }


    public JFrame newJFrame(@NonNull final String name) {
        return registerAdapter( new JFrame(name), name, JFrameEventAdapter::new);
    }

    public Container newContainer(final String name) {
        return registerAdapter( new Container(), name, ContainerEventAdapter::new);
    }

    public ImageIcon newImageIcon( @SuppressWarnings("unused") final String name) {
        return new ImageIcon();
    }

    public <T> T registerAdapter(@NonNull final T instance,
                                 @NonNull final String name,
                                 final Function<T, EventAdapter> createAdapter ) {
        if (!name.isBlank()) {
            if (instance instanceof Component component) {
                component.setName(name);
            }

            createdObjects.put(name, instance);

            if (Objects.nonNull(createAdapter)) {
                documentEventDispatcher.registerEventAdapter(name, createAdapter.apply(instance));
            }
        }
        return instance;
    }





}
