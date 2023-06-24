/*
 * Copyright © 2023, Hablutzel Consulting, LLC.
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
 */

package com.hablutzel.spwing.invoke;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;

@RequiredArgsConstructor
public class DirectParameterDescription implements ParameterDescription {

    @Getter
    private final String name;

    @Getter
    private final Class<?> type;

    @Getter
    private final boolean varArgs;

    @Getter
    private final int index;

    @Getter
    private final boolean optional;

    @Override
    public boolean hasParameterAnnotation(Class<? extends Annotation> clazz) {
        return false;
    }

    @Override
    public Annotation getParameterAnnotation(Class<? extends Annotation> clazz) {
        return null;
    }
}
