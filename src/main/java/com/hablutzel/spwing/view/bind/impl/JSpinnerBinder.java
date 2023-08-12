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
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.Date;
import java.util.List;


/**
 * Binds properties to a {@link JComboBox}. This binder is used to
 * bind the "items" of the combo box, so that the items can be specified
 * as a model property.
 */
@Service
@Slf4j
public class JSpinnerBinder extends BaseBinder {

    private enum SpinnerType { Date, List, Number, Unknown };

    public static final String VALUE = "value";
    private final static String MODEL = "model";
    @Override
    public boolean binds(@NonNull final BeanWrapper componentWrapper,
                         @NonNull final String propertyName,
                         @NonNull final Accessor authoritativeValueAccessor) {


        // We only bind JSpinners with a SpinnerDateModel, SpinnerListModel, or
        // SpinnerNumberModel. This allows the user to define their own binders
        // for custom spinner models. For these, we block the binding to the model
        // property so it can't be changed (easily).
        Object unwrapped = componentWrapper.getWrappedInstance();
        if (unwrapped instanceof JSpinner spinner) {
            return classifySpinnerModel(spinner.getModel()) != SpinnerType.Unknown &&
                    (VALUE.equals(propertyName) || MODEL.equals(propertyName));
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

        // Get our element as a combobox
        final Object element = wrappedTargetObject.getWrappedInstance();
        if (element instanceof JSpinner spinner) {

            final SpinnerModel spinnerModel = spinner.getModel();
            final SpinnerType spinnerType = classifySpinnerModel(spinnerModel);
            final ConversionService conversionService = applicationContext.getBean(ConversionService.class);

            // See if we're binding the VALUE property
            if (VALUE.equals(propertyName)) {

                Accessor accessor = switch (spinnerType) {
                    case Date -> buildDateAccessor(spinnerModel, conversionService);
                    case Number -> buildAccessor(authoritativeValueAccessor, conversionService, Number.class);
                    case List -> buildAccessor(authoritativeValueAccessor, conversionService, List.class);
                    default -> throw new IllegalStateException("Unexpected value: " + spinnerType);
                };
                bindWithRefresh(accessor, authoritativeValueAccessor, triggers, applicationContext);

                spinnerModel.addChangeListener( evt -> authoritativeValueAccessor.set(spinnerModel.getValue()));
            } else {
                log.error("Cannot bind to the model of a spinner");
            }
        }
    }


    private Accessor buildDateAccessor( final SpinnerModel spinnerModel, final ConversionService conversionService ) {
        if (spinnerModel instanceof SpinnerDateModel spinnerDateModel) {
            return new Accessor() {
                @Override
                public boolean isWriteable() {
                    return true;
                }

                @Override
                public Object get() {
                    return spinnerDateModel.getDate();
                }

                @Override
                public void set(Object value) {
                    spinnerDateModel.setValue(value);
                }

                @Override
                public boolean canSupply(Class<?> targetClass) {
                    return conversionService.canConvert(Date.class, targetClass);
                }
            };
        } else {
            return null;
        }
    }


    private Accessor buildAccessor(final Accessor authoritativeValueAccessor,
                                   final ConversionService conversionService,
                                   final Class<?> spinnerTargetClass) {
        if (authoritativeValueAccessor.canSupply(spinnerTargetClass)) {
            return authoritativeValueAccessor;
        } else {

            return new Accessor() {
                @Override
                public boolean isWriteable() {
                    return authoritativeValueAccessor.isWriteable();
                }

                @Override
                public Object get() {
                    return conversionService.convert(authoritativeValueAccessor.get(), spinnerTargetClass);
                }

                @Override
                public void set(Object value) {
                    authoritativeValueAccessor.set(conversionService.convert(value, spinnerTargetClass));
                }

                @Override
                public boolean canSupply(Class<?> targetClass) {
                    return authoritativeValueAccessor.canSupply(targetClass) ||
                            conversionService.canConvert(spinnerTargetClass, targetClass);
                }
            };
        }

    }



    private SpinnerType classifySpinnerModel( final SpinnerModel spinnerModel ) {
        if (spinnerModel instanceof SpinnerDateModel) {
            return SpinnerType.Date;
        } else if (spinnerModel instanceof  SpinnerListModel) {
            return SpinnerType.List;
        } else if (spinnerModel instanceof SpinnerNumberModel) {
            return SpinnerType.Number;
        } else {
            return SpinnerType.Unknown;
        }
    }

}
