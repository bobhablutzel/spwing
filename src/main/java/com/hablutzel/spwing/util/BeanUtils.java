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

package com.hablutzel.spwing.util;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;


/**
 * Routines to manage bean operations in the an {@link ApplicationContext}.
 * Used by the framework to push values into the context that are created
 * outside the normal bean description mechanism.
 *
 * @author Bob Hablutzel
 */
public class BeanUtils {


    /**
     * Push a bean into the application context
     *
     * @param context The {@link ApplicationContext} instance
     * @param beanName The bean name
     * @param bean The bean
     */
    public static void pushBean(final ApplicationContext context,
                                final String beanName,
                                final Object bean) {

        // Remove any old ones
        removeBeanByName(context, beanName);

        // Make sure we have a bean registry we can use
        if (context instanceof BeanDefinitionRegistry beanDefinitionRegistry) {

            // Push the bean
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(bean.getClass().getName());
            AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
            beanDefinition.setInstanceSupplier(() -> bean);
            beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);
        }
    }


    /**
     * Used to remove a bean from the application context by class
     *
     * @param context The {@link ApplicationContext} instance
     * @param beanClass The bean class
     */
    public static void removeBeanByClass(final ApplicationContext context, final Class<?> beanClass) {
        removeBeanByName(context, beanClass.getSimpleName());
    }

    /**
     * Removes a bean from the context by name
     * @param context The {@link ApplicationContext} instance
     * @param beanName The bean name
     */
    private static void removeBeanByName(final ApplicationContext context, final String beanName) {
        if (context instanceof BeanDefinitionRegistry beanDefinitionRegistry &&
                beanDefinitionRegistry.containsBeanDefinition(beanName)) {
            beanDefinitionRegistry.removeBeanDefinition(beanName);
        }
    }

}
