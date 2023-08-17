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

package com.hablutzel.spwing.view.factory.component;

import com.hablutzel.spwing.view.adapter.JComboBoxEventAdapter;
import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.RefreshTrigger;
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.swing.JComboBox;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Create a new {@link JComboBox} instance, including registering an
 * event adapter for that instance with the current document event dispatcher.
 *
 * @author Bob Hablutzel
 */
@Service
@Scope("singleton")
@Slf4j
@SuppressWarnings("rawtypes")
public final class JComboBoxFactory extends AbstractViewComponentFactory<JComboBox> {


    @Override
    public Cocoon<JComboBox> build(String name, ConversionService conversionService) {
        JComboBox comboBox = new JComboBox();
        registerAdapter(comboBox, name, JComboBoxEventAdapter::new);
        return new Cocoon<>(comboBox, this, conversionService) {

            @Override
            public boolean canSetProperty(String propertyName) {
                return "items".equals(propertyName) ||
                        "selected".equals(propertyName) ||
                        super.canSetProperty(propertyName);
            }

            @Override
            public AllowedBindings allowedBindings(String propertyName) {
                log.info( "Checking property {}", propertyName);
                return switch (propertyName) {
                    case "items" -> AllowedBindings.FROM_MODEL;
                    case "selected" -> AllowedBindings.BIDIRECTIONAL;
                    default -> super.allowedBindings(propertyName);
                };
            }


            @Override
            @SuppressWarnings("unchecked")
            public void setProperty(String propertyName, Object value) {
                if ("items".equals(propertyName)) {
                    if (value instanceof Collection<?> collection) {
                        collection.forEach(comboBox::addItem);
                    } else if (value instanceof Class<?> valueAsClass && valueAsClass.isEnum()) {
                        Arrays.stream(valueAsClass.getEnumConstants()).forEach(comboBox::addItem);
                    }
                } else if ("selected".equals(propertyName)) {
                    comboBox.setSelectedItem(value);
                } else {
                    super.setProperty(propertyName, value);
                }
            }

            @Override
            public void bindProperty(String propertyName, Accessor externalState, List<RefreshTrigger> refreshTriggers) {
                super.bindProperty(propertyName, externalState, refreshTriggers);
                if ("selected".equals(propertyName) && externalState.isWriteable()) {
                    comboBox.addActionListener( evt -> {
                        Object selectedItem = comboBox.getSelectedItem();
                        if (null != selectedItem && !selectedItem.equals(externalState.get())) {
                            externalState.set(selectedItem);
                        }
                    });
                }
            }
        };
    }

    @Override
    public String alias() {
        return "JComboBox";
    }

}
