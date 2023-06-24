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

package com.hablutzel.spwing;


import com.hablutzel.spwing.annotations.Application;
import com.hablutzel.spwing.annotations.Controller;
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.laf.DefaultLookAndFeelFactory;
import com.hablutzel.spwing.laf.LookAndFeelFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.lang.reflect.InvocationTargetException;
import java.util.*;


@Slf4j
public final class ApplicationConfiguration {

    @Getter
    private final Class<?> contextRoot;

    @Getter
    private final Calendar launchTime = Calendar.getInstance();

    private final Set<BeanDefinition> knownModels = new HashSet<>();

    @Getter
    private Application applicationAnnotation = null;

    @Getter
    private Class<?> applicationClass = null;


    public ApplicationConfiguration(final Class<?> contextRoot) {
        this.contextRoot = contextRoot;
        scanForApplications();
    }

    public void scanForApplications() {

        // Scan the class path for any classes annotated with @Application
        // (which will also include @AllInOneApplication through meta-annotation).
        // If there is more than one, randomly pick it. If there is more than
        // one, pick one randomly.
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false, new StandardEnvironment());
        provider.addIncludeFilter(new AnnotationTypeFilter(Application.class));
        Set<BeanDefinition> beanDefinitions = provider.findCandidateComponents(contextRoot.getPackageName());
        log.debug( "Application configuration bean definitions: {}", beanDefinitions);
        if (beanDefinitions.size() > 1) {
            log.warn("Multiple beans are marked as applications, count = {}, selecting at random", beanDefinitions.size());
            beanDefinitions.stream().map(BeanDefinition::getBeanClassName).forEach(log::warn);
        }

        if (!beanDefinitions.isEmpty()) {
            BeanDefinition applicationBeanDefinition = beanDefinitions.iterator().next();
            log.debug("Selected {} as the application", applicationBeanDefinition.getBeanClassName());

            try {
                applicationClass = Class.forName(applicationBeanDefinition.getBeanClassName());
                applicationAnnotation = AnnotatedElementUtils.findMergedAnnotation(applicationClass, Application.class);
            } catch (ClassNotFoundException e) {

                // Something went wickedly wrong here.
                log.error("Could not load class for identified application class {}", applicationBeanDefinition.getBeanClassName());
                System.exit(1);
            }
        }
    }



    /**
     * Find the application name from the (unique) class annotated with
     * {@link Application}. Default to the name of the context root class
     *
     * @return The application name
     */
    public String getApplicationName() {
        Application application = getApplicationAnnotation();
        String suppliedName = Objects.nonNull(application) ? application.applicationName() : "";
        return suppliedName.isBlank() ? contextRoot.getSimpleName() : suppliedName;
    }


    /**
     * Find the look and feel for the application
     *
     * @return The application name
     */
    public LookAndFeel getLookAndFeel() {
        Application application = getApplicationAnnotation();
        Class<? extends LookAndFeelFactory> factoryClass = Objects.nonNull(application)
                ? application.lookAndFeel()
                : DefaultLookAndFeelFactory.class;

        try {
            LookAndFeelFactory factory = factoryClass.getDeclaredConstructor().newInstance();
            return factory.get();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            log.error("Look and feel factory {} could not be loaded", factoryClass.getName());
            return new MetalLookAndFeel();
        }
    }



    public Set<BeanDefinition> getKnownModels() {

        // We do a lazy retrieve of the known models.
        if (knownModels.isEmpty()) {
            populateKnownModels();
        }
        return knownModels;
    }



    private void populateKnownModels() {
        // Find all the classes that are annotated with the target annotation. We have
        // a chicken-and-egg problem here. Since the model should be scoped
        // as "document", it will not yet be created by definition, since the
        // creation of the model is what creates the document scope. So
        // rather than looking at the available beans, we have to look at the
        // available bean definitions.
        //
        // In order to look for available bean definition that are annotated
        // with the "@Model" annotation, we scan the application class path
        // starting at the context path that was passed to the UI.launch method.
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                false, new StandardEnvironment());
        provider.addIncludeFilter(new AnnotationTypeFilter(Model.class));
        knownModels.addAll(provider.findCandidateComponents(contextRoot.getPackageName()));

        // Validate that each of the definitions is scoped to "document"
        knownModels.forEach( this::validateDocumentScope);

    }




    /**
     * Routine to check the bean definitions found with {@link Controller} or {@link Model}
     * annotation to make sure that they also have a {@link Scope} annotation
     * scoping the bean to "document" scope. This is important because
     * they are a part of, and should be only part of, the document scope.
     *
     * @param beanDefinition The bean definition
     */
    private void validateDocumentScope(BeanDefinition beanDefinition) {
        try {

            // Load the class with given definition, and find the Scope annotation (if any)
            Class<?> beanClass = Class.forName(beanDefinition.getBeanClassName());
            Scope scope = AnnotatedElementUtils.getMergedAnnotation(beanClass, Scope.class );

            // Validate it exists and is scoped to "document"
            if (Objects.nonNull(scope) && !"document".equals(scope.scopeName())) {
                log.warn("Class {} should be annotation with document scope (@Scope(\"document\").",
                        beanDefinition.getBeanClassName());
            }
        } catch (ClassNotFoundException e) {
            log.error("Internal error attempting to load bean defined {}", beanDefinition.getBeanClassName());
            throw new RuntimeException(e);
        }
    }

    /**
     * Find the "primary" model. This is a model that is either unique
     * (there is only one bean with the {@link com.hablutzel.spwing.annotations.Model} annotation, or, if there are
     * multiple models, there is one and only one where {@link Model#primary()} is true
     *
     * @return The primary bean definition
     */
    public BeanDefinition getPrimaryModel() {

        // Get the model bean definitions
        Set<BeanDefinition> models = getKnownModels();

        // If there is just one, then open it. Otherwise see if one is primary
        switch (models.size()) {
            case 0 -> {
                log.error("No models have been defined");
                return null;
            }
            case 1 -> {
                return models.iterator().next();
            }
            default -> {
                // See if we can find a primary model
                final List<BeanDefinition> primaryModels = models.stream()
                        .filter(this::isPrimaryModel)
                        .toList();
                if (primaryModels.size() == 1) {
                    return primaryModels.get(0);
                } else {
                    log.error("Multiple models, but none marked as primary");
                    return null;
                }
            }
        }

    }

    /**
     * Test to see if the object is marked as a primary model
     *
     * @param modelDefinition The definition to test.
     * @return TRUE if the object (a) has a {@link Model} annotation, and
     * (b) that annotation has the primary flag set.
     */
    private boolean isPrimaryModel(BeanDefinition modelDefinition) {
        Class<?> clazz = modelDefinition.getClass();
        Model modelAnnotation = clazz.getAnnotation(Model.class);
        return modelAnnotation != null && modelAnnotation.primary();
    }


}
