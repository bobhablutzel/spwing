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

import com.hablutzel.spwing.util.EnumerationStream;
import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.Binder;
import com.hablutzel.spwing.view.bind.controllers.ButtonGroupController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class ButtonGroupSelectedBinder implements Binder {
    private enum BindingTo { Enum, Boolean, String }

    private BindingTo bindingTo = BindingTo.Enum;

    @Override
    public boolean binds(@NonNull final BeanWrapper componentWrapper,
                         @NonNull final String propertyName,
                         @NonNull final Accessor authoritativeValueAccessor) {
        if (componentWrapper.getWrappedInstance() instanceof ButtonGroupController buttonGroupController) {
            ButtonGroup buttonGroup = buttonGroupController.buttonGroup();
            if ("selected".equals(propertyName)) {
                if (authoritativeValueAccessor.canSupply(Enum.class)) {
                    bindingTo = BindingTo.Enum;
                    return true;
                } else {
                    int buttonCount = buttonGroup.getButtonCount();
                    if (authoritativeValueAccessor.canSupply(Boolean.class)) {
                        bindingTo = BindingTo.Boolean;
                        return buttonCount == 2;
                    } else {
                        bindingTo = BindingTo.String;
                        return (authoritativeValueAccessor.canSupply(String.class));
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void bind(@NonNull final BeanWrapper wrappedTargetObject,
                     @NonNull final String propertyName,
                     @NonNull final Object targetObjectValue,
                     @NonNull final Accessor authoritativeValueAccessor,
                     @NonNull final List<RefreshTrigger> triggers,
                     @NonNull final ApplicationContext applicationContext) {

        ConversionService conversionService = applicationContext.getBean(ConversionService.class);
        if (wrappedTargetObject.getWrappedInstance() instanceof ButtonGroupController buttonGroupController) {
            ButtonGroup buttonGroup = buttonGroupController.buttonGroup();

            // Set the initial value of the button group
            setComponentValue(targetObjectValue, authoritativeValueAccessor, conversionService, buttonGroup);

            // Set up listeners so that the buttons get updated if any of the events are fired.
            triggers.forEach( trigger -> trigger.onRefresh( () ->
                    setComponentValue(targetObjectValue, authoritativeValueAccessor, conversionService, buttonGroup)));

            // Watch the buttons.
            ItemListener itemListener = event -> {
                if (event.getSource() instanceof AbstractButton abstractButton) {
                    if (abstractButton.isSelected()) {
                        Object value = getButtonValue(abstractButton, targetObjectValue);
                        authoritativeValueAccessor.set(value);
                    }
                }
            };
            EnumerationStream.stream(buttonGroup.getElements())
                    .forEach( button -> button.addItemListener(itemListener));

        }
    }

    private Object getButtonValue( AbstractButton button, Object targetObjectValue ) {
        if (targetObjectValue instanceof Map<?, ?> buttonValues) {
            return buttonValues.get(button);
        } else {
            return null;
        }
    }


    private void setComponentValue(Object targetObjectValue, Accessor authoritativeValueAccessor, ConversionService conversionService, ButtonGroup buttonGroup) {
        if (targetObjectValue instanceof Map<?,?> buttonValues) {
            switch (bindingTo) {
                case String, Enum -> {
                    String value = conversionService.convert(authoritativeValueAccessor.get(), String.class);
                    if (null != value) {
                        EnumerationStream.stream(buttonGroup.getElements()).forEach(button -> {
                            Object buttonValue = buttonValues.get(button);
                            buttonGroup.setSelected(button.getModel(), value.equals(buttonValue));
                        });
                    }
                }
                case Boolean -> {
                    Object rawValue = authoritativeValueAccessor.get();
                    if (rawValue instanceof Boolean value){
                        buttonGroup.getElements().nextElement().setSelected(value);
                        buttonGroup.getElements().nextElement().setSelected(!value);
                    }
                }
            }
        }
    }
}
