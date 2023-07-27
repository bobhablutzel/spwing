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


import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.view.adapter.ContainerEventAdapter;
import com.hablutzel.spwing.view.adapter.EventAdapter;
import com.hablutzel.spwing.view.adapter.JButtonEventAdapter;
import com.hablutzel.spwing.view.adapter.JCheckboxEventAdapter;
import com.hablutzel.spwing.view.adapter.JComboBoxEventAdapter;
import com.hablutzel.spwing.view.adapter.JFrameEventAdapter;
import com.hablutzel.spwing.view.adapter.JLabelEventAdapter;
import com.hablutzel.spwing.view.adapter.JPanelEventAdapter;
import com.hablutzel.spwing.view.adapter.JRadioButtonEventAdapter;
import com.hablutzel.spwing.view.adapter.JTextFieldEventAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.function.BiFunction;

@RequiredArgsConstructor
@Slf4j
public class ComponentFactory {

    private final Spwing spwing;
    private final DocumentEventDispatcher documentEventDispatcher;
    private final Map<String,Object> createdObjects;

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

    public JComboBox<?> newJComboBox(@NonNull final String name) {
        final JComboBox<Object> comboBox = new JComboBox<>();
        comboBox.setActionCommand(null);
        return registerAdapter(comboBox, name, JComboBoxEventAdapter::new);
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
                                 final BiFunction<T, Spwing, EventAdapter> createAdapter ) {
        if (!name.isBlank()) {
            if (instance instanceof Component component) {
                component.setName(name);
            }

            createdObjects.put(name, instance);

            if (null != createAdapter) {
                documentEventDispatcher.registerEventAdapter(name, createAdapter.apply(instance, spwing));
            }
        }
        return instance;
    }





}
