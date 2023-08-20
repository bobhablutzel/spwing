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
import org.springframework.core.ResolvableType;

import java.lang.annotation.Annotation;


/**
 * Parameter description for a directly called function
 *
 * @author Bob Hablutzel
 */
@Getter
@RequiredArgsConstructor
public class DirectParameterDescription implements ParameterDescription {

    /**
     * The parameter name
     */
    private final String name;

    /**
     * The parameter type
     */
    private final ResolvableType type;

    /**
     * TRUE for varargs parameters
     */
    private final boolean varArgs;

    /**
     * The index of the parameter in the parameter list
     */
    private final int index;

    /**
     * TRUE for an optional parameter (nullable)
     */
    private final boolean optional;


    /**
     * Constructor
     * @param name The parameter name
     * @param clazz The class of the parameter
     * @param varArgs TRUE for varargs
     * @param index The index of the parameter
     * @param optional TRUE for optional parameters
     */
    public DirectParameterDescription(
            final String name,
            final Class<?> clazz,
            final boolean varArgs,
            final int index,
            final boolean optional) {
        this(name, ResolvableType.forClass(clazz), varArgs, index, optional );
    }

    /**
     * Is the parameter annotated with a specified annotation
     * @param clazz The target annotation
     * @return Always returns false
     */
    @Override
    public boolean hasParameterAnnotation(Class<? extends Annotation> clazz) {
        return false;
    }

    /**
     * Get the annotation for the parameter
     * @param clazz The annotation class
     * @return Always returns null
     */
    @Override
    public Annotation getParameterAnnotation(Class<? extends Annotation> clazz) {
        return null;
    }
}
