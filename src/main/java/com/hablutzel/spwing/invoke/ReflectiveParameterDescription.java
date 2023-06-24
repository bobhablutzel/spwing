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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.core.annotation.SynthesizingMethodParameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;


@RequiredArgsConstructor
@Slf4j
class ReflectiveParameterDescription implements ParameterDescription {
    private final SynthesizingMethodParameter parameter;
    private final ApplicationContext applicationContext;
    private final Executable executable;

    private String resolvedName = null;

    /**
     * Get the name of a parameter. The parameter name is inferred by a
     * hierarchical decision:
     * <ul>
     *     <li>First, if there is an {@link Qualifier} annotation on the parameter, that name is used</li>
     *     <li>Second, if there is a bean of type {@link ParameterNameDiscoverer} that knows about the
     *     target method, that name is used</li>
     *     <li>Finally, a {@link DefaultParameterNameDiscoverer} is used.</li>
     * </ul>
     * If none of those options work, the parameter name is null and name-based discovery is skipped.
     *
     * @return The bean name, or null of it cannot be discovered.
     */
    @Override
    public String getName() {

        if (Objects.isNull(resolvedName)) {
            resolvedName = resolveName();
        }
        return resolvedName;
    }


    private String resolveName() {

        String syntheticName = parameter.getParameterName();
        if (Objects.nonNull(syntheticName) && !syntheticName.isBlank()) {
            return syntheticName;
        }

        // See if there is a Qualifier annotation on the parameter that
        // gives us the name of the target bean.
        Qualifier qualifier = parameter.getParameterAnnotation(Qualifier.class);
        if (Objects.nonNull(qualifier)) {
            return qualifier.value();
        }

        // Attempt to find a ParameterNameDiscoverer for the parameter, and
        // use that name if it exists
        Map<String, ParameterNameDiscoverer> discovererMap = applicationContext.getBeansOfType(ParameterNameDiscoverer.class);
        for (ParameterNameDiscoverer discoverer : discovererMap.values()) {
            String[] parameterNames = getParameterNames(discoverer);
            if (Objects.nonNull(parameterNames) && parameterNames.length > parameter.getParameterIndex()) {
                return parameterNames[parameter.getParameterIndex()];
            }
        }

        // Allow parameter name discovery based on the usage of the "-parameters" javac switch
        StandardReflectionParameterNameDiscoverer nameDiscoverer = new StandardReflectionParameterNameDiscoverer();
        String[] parameterNamesFromDiscoverer = getParameterNames(nameDiscoverer);
        if (Objects.nonNull(parameterNamesFromDiscoverer) &&
                parameterNamesFromDiscoverer.length > parameter.getParameterIndex()) {
            return parameterNamesFromDiscoverer[parameter.getParameterIndex()];
        }

        return String.format("param_%d", parameter.getParameterIndex()); // Useful for errors
    }


    @Override
    public Class<?> getType() {
        return parameter.getParameterType();
    }

    @Override
    public boolean isVarArgs() {
        return parameter.getParameter().isVarArgs();
    }

    @Override
    public int getIndex() {
        return parameter.getParameterIndex();
    }

    @Override
    public boolean isOptional() {
        return parameter.isOptional();
    }

    @Override
    public boolean hasParameterAnnotation(Class<? extends Annotation> clazz) {
        return parameter.hasParameterAnnotation(clazz);
    }

    @Override
    public Annotation getParameterAnnotation(Class<? extends Annotation> clazz) {
        return parameter.getParameterAnnotation(clazz);
    }


    private String[] getParameterNames(ParameterNameDiscoverer discoverer) {
        String[] parameterNames;
        if (executable instanceof Method method) {
            parameterNames = discoverer.getParameterNames(method);
        } else if (executable instanceof Constructor<?> constructor) {
            parameterNames = discoverer.getParameterNames(constructor);
        } else {
            parameterNames = new String[0];
        }
        return parameterNames;
    }

}
