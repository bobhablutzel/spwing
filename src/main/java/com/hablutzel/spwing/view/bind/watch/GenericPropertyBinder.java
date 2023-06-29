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
import org.springframework.beans.BeansException;
import org.springframework.lang.NonNull;

import javax.swing.*;
import java.util.Objects;


/**
 * "Last chance" binder. Binds to the given property but
 * does not write to the authoritative value. Should
 * come last in the binder list.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public class GenericPropertyBinder extends BaseBinder {

    @Override
    public boolean binds(@NonNull final BeanWrapper componentWrapper,
                         @NonNull final String propertyName,
                         @NonNull final Accessor authoritativeValueAccessor) {
        try {
            return componentWrapper.isWritableProperty(propertyName);
        } catch (BeansException e) {
            log.info( "{} cannot be handled (not a writeable property", propertyName );
            return false;
        }
    }

    protected void checkForSuspiciousConditions(@NonNull final BeanWrapper wrappedTargetObject,
                                                @NonNull final String propertyName,
                                                @NonNull final Accessor authoritativeValueAccessor) {

        if (wrappedTargetObject.getWrappedInstance() instanceof JLabel &&
            "text".equals(propertyName)) {
            if (Objects.isNull(authoritativeValueAccessor.get(Object.class))) {
                log.warn( "Mapping JLabel text value to a null value" );
            }
        }
    }

}
