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

package com.hablutzel.spwing.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;

import java.util.Set;


/**
 * Utilities for scanning for classes in the active class path.
 * Wrapper around {@link ClassPathScanningCandidateComponentProvider}
 * functionality.
 *
 * @author Bob Hablutzel
 */
public class ClassUtils {


    /**
     * Scan for target classes within the class hierarchy rooted
     * in the root class. The target class will be anywhere in the
     * same or any child package as the root class.
     * @param rootClass The root class
     * @param targetClass The target class being searched for
     * @return The set of {@link BeanDefinition} that represent the target class
     */
    public static @NonNull Set<BeanDefinition> find(Class<?> rootClass, Class<?> targetClass) {
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false, new StandardEnvironment());
        provider.addIncludeFilter(new AssignableTypeFilter(targetClass));
        return provider.findCandidateComponents(rootClass.getPackageName());
    }
}
