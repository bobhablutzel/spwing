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

package com.hablutzel.spwing.view.bind.impl;

import com.hablutzel.spwing.view.bind.Accessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Binds properties to a {@link JComboBox}. This binder is used to
 * bind the "items" of the combo box, so that the items can be specified
 * as a model property.
 */
@Service
@Slf4j
public class JComboBoxBinder extends BaseBinder {

    public static final String ITEMS = "items";
    private final static String SELECTED = "selected";
    @Override
    public boolean binds(@NonNull final BeanWrapper componentWrapper,
                         @NonNull final String propertyName,
                         @NonNull final Accessor authoritativeValueAccessor) {

        return JComboBox.class.isAssignableFrom(componentWrapper.getWrappedInstance().getClass()) &&
                (ITEMS.equals(propertyName) || SELECTED.equals(propertyName));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bind(@NonNull final BeanWrapper wrappedTargetObject,
                     @NonNull final String propertyName,
                     @NonNull final Object targetObjectValue,
                     @NonNull final Accessor authoritativeValueAccessor,
                     @NonNull final List<RefreshTrigger> triggers,
                     @NonNull final ApplicationContext applicationContext) {

        // Get our element as a combobox
        final Object element = wrappedTargetObject.getWrappedInstance();
        if (element instanceof JComboBox<?>) {
            JComboBox<Object> comboBox = (JComboBox<Object>) element;

            // See if we're binding the ITEMS property
            if (ITEMS.equals(propertyName)) {
                bindItems(authoritativeValueAccessor, triggers, comboBox);
            } else {
                // If not ITEMS, then SELECTED
                Accessor comboSelectedAccessor = new Accessor() {
                    @Override
                    public boolean isWriteable() {
                        return true;
                    }

                    @Override
                    public Object get() {
                        return comboBox.getSelectedItem();
                    }

                    @Override
                    public void set(Object value) {
                        comboBox.setSelectedItem(value);
                    }

                    @Override
                    public boolean canSupply(Class<?> targetClass) {
                        return targetClass.isAssignableFrom(String.class);
                    }
                };
                bindWithRefresh(comboSelectedAccessor, authoritativeValueAccessor, triggers, applicationContext);
            }
        }
    }

    private void bindItems(final Accessor authoritativeValueAccessor,
                           final List<RefreshTrigger> triggers,
                           final JComboBox<Object> comboBox) {

        Object modelValues = authoritativeValueAccessor.get();
        Class<?> modelValueClass = modelValues.getClass();
        if (modelValues instanceof Collection) {

            final Collection<Object> collection = (Collection<Object>) modelValues;
            collection.forEach(comboBox::addItem);

            triggers.forEach( trigger -> trigger.onRefresh(() -> {
                comboBox.removeAllItems();
                final Collection<Object> newValues = (Collection<Object>) modelValues;
                newValues.forEach(comboBox::addItem);
            }));

        } else if (modelValueClass.isEnum()) {
            Arrays.stream(modelValueClass.getEnumConstants()).forEach(comboBox::addItem);
        }
    }
}
