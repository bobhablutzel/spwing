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


import com.hablutzel.spwing.annotations.Application;
import com.hablutzel.spwing.annotations.Controller;
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Function;


/**
 * The key beans in the framework - applications, models, and controllers -
 * are generally known to the client code by type. The {@link ReflectiveInvoker}
 * will match those classes and find the beans that are active in the
 * context. However, in some cases the code needs to be more generic, and
 * just find the "document" bean. In order to do this, the {@link ReflectiveInvoker}
 * can also match parameters by bean name. This class creates the appropriate
 * alias for the beans after they are created. By default, these names are
 * <code>application</code>, <code>model</code>, and <code>controller</code> respectively
 * but can be changed with the <code>alias</code> parameter in each of the respective
 * annotations.
 *
 * @author Bob Hablutzel
 */
@Slf4j
@RequiredArgsConstructor
public class KeyBeanAliasProvider implements BeanPostProcessor {

    private final ApplicationContext applicationContext;

    /**
     * A new bean was created; see if we need to create an alias for it.
     *
     * @param bean The bean
     * @param beanName The bean name
     * @return The bean
     * @throws BeansException A bean exception (if anything happens)
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        testFor( bean, beanName, Application.class, Application::alias);
        testFor( bean, beanName, Model.class, Model::alias);
        testFor( bean, beanName, Controller.class, Controller::alias);
        return bean;
    }

    /**
     * Register the bean alias
     *
     * @param beanName The bean name
     * @param aliasName The alias name
     */
    private void registerBeanAlias(String beanName, String aliasName) {
        if (Objects.nonNull(beanName) && Objects.nonNull(aliasName) && !beanName.equals(aliasName)) {
            if (applicationContext.getAutowireCapableBeanFactory() instanceof ConfigurableListableBeanFactory factory) {
                factory.registerAlias(beanName, aliasName);
                log.debug( "{} is now also called {}", beanName, aliasName );
            }
        }
    }


    /**
     * Test a bean to see if it is one of the well known beans
     * @param bean The bean
     * @param beanName The bean name
     * @param clazz The annotation class being tested
     * @param name The alias name provider
     * @param <T> The annotation class
     */
    private <T extends Annotation> void testFor( Object bean, String beanName, Class<T> clazz, Function<T,String> name) {
        T annotation = AnnotatedElementUtils.findMergedAnnotation(bean.getClass(), clazz);
        if (annotation != null) {
            registerBeanAlias(beanName, name.apply(annotation));
        }
    }

}
