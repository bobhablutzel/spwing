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
import com.hablutzel.spwing.view.bind.Binder;
import com.hablutzel.spwing.view.bind.PropertyAccessor;
import com.hablutzel.spwing.view.bind.RefreshTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;

import java.util.List;


@Slf4j
public abstract class BaseBinder implements Binder {

    @Override
    public void bind(@NonNull final BeanWrapper wrappedTargetObject,
                     @NonNull final String propertyName,
                     @NonNull final Object targetObjectValue,
                     @NonNull final Accessor authoritativeValueAccessor,
                     @NonNull final List<RefreshTrigger> triggers,
                     @NonNull final ApplicationContext applicationContext) {

        // Get the refresh triggers
        this.bindValueToControl(wrappedTargetObject, propertyName, authoritativeValueAccessor, triggers, applicationContext);
        checkForSuspiciousConditions(wrappedTargetObject, propertyName, authoritativeValueAccessor);
    }




    protected void checkForSuspiciousConditions(@NonNull final BeanWrapper wrappedTargetObject,
                                                @NonNull final String propertyName,
                                                @NonNull final Accessor authoritativeValueAccessor) {
    }


    public void bindValueToControl(@NonNull final BeanWrapper viewObjectBeanWrapper,
                                   @NonNull final String viewObjectProperty,
                                   @NonNull final Accessor authoritativeValue,
                                   @NonNull final List<RefreshTrigger> triggers,
                                   @NonNull final ApplicationContext applicationContext) {

        // Get the conversion service
        final ConversionService conversionService = applicationContext.getBean(ConversionService.class);

        // Get the view object bean wrapper. Make sure the property is writable
        if (viewObjectBeanWrapper.isWritableProperty(viewObjectProperty)) {

            // Get the property type. Validate that the accessor can give a valid value
            // for that property
            PropertyAccessor viewPropertyAccessor = new PropertyAccessor(viewObjectBeanWrapper, viewObjectProperty, conversionService);
            Class<?> viewPropertyType = viewObjectBeanWrapper.getPropertyType(viewObjectProperty);
            if (authoritativeValue.canSupply(viewPropertyType)) {

                // Get a consumer for writing to this property, and bind it to the accessor
                bindWithRefresh(viewPropertyAccessor, authoritativeValue, triggers);

            }
        }
    }

    /**
     * Used to bind an accessor (typically a {@link PropertyAccessor})
     * to an consumer (typically a setter from a Swing component). The
     * accessor provides a mechanism for getting the value from the
     * model, while the setter provides the means for setting the value
     * in the view (Swing component). The triggers are the list of event
     * names that signal that the accessor needs to be re-evaluated. Triggers
     * allow for changes that occur to the model outside the view to be
     * recognized and reflected in the view; when the trigger events are
     * processed the accessor will be re-evaluated for the current value
     * and the setter will be re-invoked.
     *
     * @param viewPropertyAccessor  The view accessor
     * @param authoritativeAccessor The accessor (typically a {@link PropertyAccessor} for a model property
     * @param triggers              The trigger list for re-evaluating the accessor
     */
    protected void bindWithRefresh(final Accessor viewPropertyAccessor,
                                 final Accessor authoritativeAccessor,
                                 final List<RefreshTrigger> triggers) {


        // Set the value
        Object initialAuthoritativeValue = authoritativeAccessor.get();
        viewPropertyAccessor.set(initialAuthoritativeValue);

        triggers.forEach( refreshTrigger -> refreshTrigger.onRefresh( () -> {
            Object newValue = authoritativeAccessor.get();
            if (!viewPropertyAccessor.get().equals(newValue)) {
                viewPropertyAccessor.set(newValue);
            }
        } ));
    }
}
