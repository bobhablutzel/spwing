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

package com.hablutzel.spwing.view.bind;


import org.springframework.beans.BeanWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Binder instances watch Swing components, watching
 * for changes in the state of those components. When those
 * changes occur, the current value of the target control is
 * pushed to the access.<br>
 * Some binders need to know the "value" of the control;
 * this is the pseudo-property associated with the control
 * when it is created. This value is immutable and passed to the binder
 * at the time the binder is bound.
 * Note: This binding mechanism is no longer (as of Spwing 0.6.2) used
 * for individual components, only for button groups. A near future
 * release will likely remove this mechanism completely.
 *
 * @author Bob Hablutzel
 */
public interface Binder {

    /**
     * Bind a property beteeen a bean wrapper and an {@link Accessor}
     *
     * @param componentWrapper The {@link BeanWrapper} around the Swing component
     * @param propertyName The Swing component property name
     * @param authoritativeValueAccessor The authoritative (model) value accessor
     * @return TRUE for successful binding
     */
    boolean binds(@NonNull final BeanWrapper componentWrapper,
                  @NonNull final String propertyName,
                  @NonNull final Accessor authoritativeValueAccessor);


    /**
     * Bind a property beteeen a bean wrapper and an {@link Accessor}
     *
     * @param wrappedTargetObject The {@link BeanWrapper} around the Swing component
     * @param propertyName The Swing component property name
     * @param targetObjectValue The component wrapped by the wrappedTargetObject
     * @param authoritativeValueAccessor The authoritative (model) value accessor
     * @param triggers A list of {@link RefreshTrigger} instances
     * @param applicationContext The active {@link ApplicationContext} instance
     */
    void bind(@NonNull final BeanWrapper wrappedTargetObject,
              @NonNull final String propertyName,
              @NonNull final Object targetObjectValue,
              @NonNull final Accessor authoritativeValueAccessor,
              @NonNull final List<RefreshTrigger> triggers,
              @NonNull final ApplicationContext applicationContext);

}
