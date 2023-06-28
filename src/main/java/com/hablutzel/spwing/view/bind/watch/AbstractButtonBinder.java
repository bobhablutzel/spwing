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

package com.hablutzel.spwing.view.bind.watch;

import com.hablutzel.spwing.view.bind.Accessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.List;


/**
 * Binds an property of an {@link AbstractButton}. If that
 * property is the {@link AbstractButton#isSelected()} property, also looks for
 * changes to that and reflects those changes in the
 * authoritative state.
 *
 * @author Bob Hablutzel
 */
@Service
@Slf4j
public class AbstractButtonBinder extends BaseBinder {

    @Override
    public boolean binds(@NonNull final BeanWrapper componentWrapper,
                         @NonNull final String propertyName,
                         @NonNull final Accessor authoritativeValueAccessor) {
        return AbstractButton.class.isAssignableFrom(componentWrapper.getWrappedClass());
    }

    @Override
    public void bind(@NonNull final BeanWrapper wrappedTargetObject,
                     @NonNull final String propertyName,
                     @NonNull final Object targetObjectValue,
                     @NonNull final Accessor authoritativeValueAccessor,
                     @NonNull final List<String> triggers,
                     @NonNull final ApplicationContext applicationContext) {

        super.bind(wrappedTargetObject, propertyName, targetObjectValue, authoritativeValueAccessor, triggers, applicationContext);

        // Validate we got the class we expect and convert it
        if (wrappedTargetObject.getWrappedInstance() instanceof AbstractButton abstractButton) {

            // If the property is the "selected" property, watch for changes
            if ("selected".equals(propertyName) && authoritativeValueAccessor.isWriteable()) {
                abstractButton.addChangeListener(e -> {
                    boolean isButtonSelected = abstractButton.isSelected();
                    if (authoritativeValueAccessor.get(Boolean.class) instanceof Boolean isModelSelected) {
                        if (isButtonSelected != isModelSelected) {
                            authoritativeValueAccessor.set(isButtonSelected);
                        }
                    } else {
                        log.error( "Got an unexpected value from the accessor: {}", authoritativeValueAccessor.get(Boolean.class));
                    }
                });
            }
        }
    }
}