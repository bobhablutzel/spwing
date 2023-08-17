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
 * Watch binder instances watch Swing components, watching
 * for changes in the state of those components. When those
 * changes occur, the current value of the target control is
 * pushed to the access.<br>
 * Some binders need to know the "value" of the control;
 * this is the pseudo-property associated with the control
 * when it is created. This value is immutable and passed to the binder
 * at the time the binder is bound.
 * @author Bob Hablutzel
 */
public interface Binder {

    boolean binds(@NonNull final BeanWrapper componentWrapper,
                  @NonNull final String propertyName,
                  @NonNull final Accessor authoritativeValueAccessor);


    void bind(@NonNull final BeanWrapper wrappedTargetObject,
              @NonNull final String propertyName,
              @NonNull final Object targetObjectValue,
              @NonNull final Accessor authoritativeValueAccessor,
              @NonNull final List<RefreshTrigger> triggers,
              @NonNull final ApplicationContext applicationContext);

}
