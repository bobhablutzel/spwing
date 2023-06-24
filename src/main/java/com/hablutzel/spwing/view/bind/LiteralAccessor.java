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
import org.springframework.core.convert.ConversionService;

@RequiredArgsConstructor
@ToString
public class LiteralAccessor extends Accessor {
    private final String value;
    private final ConversionService conversionService;

    public boolean isWriteable() {
        return false;
    }

    public void set(Object value) {
        throw new UnsupportedOperationException();
    }

    public <T> T get(Class<T> clazz) {
        return conversionService.convert(value, clazz);
    }

    @Override
    public boolean canSupply(Class<?> targetClass) {
        return conversionService.canConvert(String.class, targetClass);
    }
}
