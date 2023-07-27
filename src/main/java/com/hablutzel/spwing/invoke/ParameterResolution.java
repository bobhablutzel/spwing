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

package com.hablutzel.spwing.invoke;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.function.Function;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class ParameterResolution {

    @Getter
    private final boolean resolved;

    @Getter
    private final Object value;

    public static ParameterResolution unresolved() {
        return new ParameterResolution(false, null);
    }

    public static ParameterResolution of(final Object value) {
        return new ParameterResolution(true, value);
    }


    public static Function<ParameterDescription,ParameterResolution> forClass(final Class<?> clazz,
                                                                       final Object instance ) {
        return parameterDescription -> parameterDescription.getType().isAssignableFrom(clazz)
                ? ParameterResolution.of(instance)
                : ParameterResolution.unresolved();
    }
}
