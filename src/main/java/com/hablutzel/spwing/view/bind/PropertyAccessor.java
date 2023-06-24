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

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.core.convert.ConversionService;

@RequiredArgsConstructor
@ToString
@Slf4j
public class PropertyAccessor extends Accessor {

    private final BeanWrapper beanWrapper;
    private final String propertyDescription;
    private final ConversionService conversionService;

    public boolean isIcon() { return propertyDescription.equals("icon" ); }

    public boolean isWriteable() {
        return beanWrapper.isWritableProperty(propertyDescription);
    }

    public void set(Object value) {
        beanWrapper.setPropertyValue(propertyDescription, value);
    }

    public <T> T get(Class<T> clazz) {
        return conversionService.convert(beanWrapper.getPropertyValue(propertyDescription), clazz);
    }

    @Override
    public boolean canSupply(Class<?> targetClass) {
        return conversionService.canConvert(beanWrapper.getPropertyType(propertyDescription), targetClass);
    }
}
