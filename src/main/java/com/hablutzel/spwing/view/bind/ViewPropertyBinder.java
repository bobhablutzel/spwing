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

import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.model.PropertyChangeModel;
import com.hablutzel.spwing.util.FlexpressionParser;
import com.hablutzel.spwing.view.bind.controllers.ButtonGroupController;
import com.hablutzel.spwing.view.bind.impl.DocumentEventRefreshTrigger;
import com.hablutzel.spwing.view.bind.impl.GenericPropertyBinder;
import com.hablutzel.spwing.view.bind.impl.PropertyListenerRefreshTrigger;
import com.hablutzel.spwing.view.bind.impl.RefreshTrigger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
public class ViewPropertyBinder {

    private final ApplicationContext applicationContext;
    private final ConversionService conversionService;

    private final Map<Object, Object> valueMap = new HashMap<>();

    private final Map<Object, BeanWrapper> beanWrapperMap = new HashMap<>();
    private final List<Binder> binders = new ArrayList<>();



    /**
     * Create a new {@link ViewPropertyBinder}. This class has the responsibility
     * of performing bindings between properties of two classes, generally
     * between a Swing component and a model object.
     *
     * @param applicationContext The {@link ApplicationContext}
     */
    public ViewPropertyBinder(final ApplicationContext applicationContext ) {
        this.applicationContext = applicationContext;
        this.conversionService = applicationContext.getBean(ConversionService.class);

        // Get all the binder services, and add the (non-service) last chance binder
        // by hand.
        binders.addAll(applicationContext.getBeansOfType(Binder.class).values());
        binders.add(new GenericPropertyBinder());
    }



    /**
     * Get a BeanWrapper for the specified object
     */
    public BeanWrapper beanWrapperFor(Object object) {
        if (!beanWrapperMap.containsKey(object)) {
            beanWrapperMap.put(object, new BeanWrapperImpl(object));
        }
        return beanWrapperMap.get(object);
    }


    /**
     * Define the value for a component. This is used for some bindings
     * @param component The component to remember a value for
     * @param value The value to remember
     */
    public void setValue(@NonNull final Object component, @NonNull final Object value) {
        valueMap.put(component, value);
    }

    /**
     * Takes the given expression (a property path, SPEL expression, literal, etc.)
     * and turns it into an {@link Accessor}. The expression will first be evaluated
     * as a property path on the supplied bean. If it is a readable property, then
     * a {@link PropertyAccessor} will be created. If not, the expression will be
     * evaluated to see if it can be handled by a {@link FlexpressionParser}
     * (which can include system properties, SPEL expression, static method calls,
     * arbitrary beans, and more). In this case, a {@link FlexpressionAccessor}
     * will be created. If it is neither a property path nor a Flexpression,
     * it will be take as a literal.
     *
     * @param targetObject The object to act as a property source for property paths
     * @param valueExpression The expression
     * @return A new accessor
     */

    public Accessor toAccessor(@Nullable final Object targetObject, final String valueExpression) {

        // First preference: A property, as this will allow read/write
        if (null != targetObject) {
            BeanWrapper targetBeanWrapper = beanWrapperFor(targetObject);
            if (targetBeanWrapper.isReadableProperty(valueExpression)) {
                return new PropertyAccessor(targetBeanWrapper, valueExpression, conversionService);
            }
        }

        log.warn( "{} is not a property of {} and will read-only", valueExpression, targetObject );
        // Not a property, try a SPEL expression. Allows more flexibility than a property
        // but isn't writeable
        try {
            SpelExpressionParser parser = new SpelExpressionParser();
            Expression spelExpression = parser.parseExpression(valueExpression);
            StandardEvaluationContext context = new StandardEvaluationContext(targetObject);
            return new SpelExpressionAccessor(spelExpression, context, conversionService);
        } catch (ParseException e) {
            log.debug( "Unable to treat {} as an expression", valueExpression, e);
        }

        // Next try is a flexpression, which can be used to pull in application properties
        if (FlexpressionParser.appearsToBeFlexpression(valueExpression)) {
            return new FlexpressionAccessor(valueExpression, applicationContext, conversionService);
        }

        // Last chance, a literal
        return  new LiteralAccessor(valueExpression, conversionService);
    }


    public List<RefreshTrigger> getRefreshTriggers(final ApplicationContext applicationContext,
                                                      final Object targetObject,
                                                      final String propertyName,
                                                      final List<String> triggers ) {

        // If the list of triggers is non-empty, then we need to look for document events
        if (!triggers.isEmpty()) {
            DocumentEventDispatcher documentEventDispatcher = DocumentEventDispatcher.get(applicationContext);

            return triggers.stream()
                    .map(trigger -> new DocumentEventRefreshTrigger(applicationContext, documentEventDispatcher, trigger))
                    .map(RefreshTrigger.class::cast)
                    .toList();
        } else if (targetObject instanceof PropertyEditor propertyEditor) {

            // No defined triggers, so we're going to see if we can use property listeners
            // This branch works for models that are derived from PropertyEditor instances.
            PropertyListenerRefreshTrigger refreshTrigger = new PropertyListenerRefreshTrigger(propertyName);
            propertyEditor.addPropertyChangeListener(refreshTrigger);
            return List.of(refreshTrigger);
        } else if (targetObject instanceof PropertyChangeModel propertyChangeModel) {

            // No defined triggers, so we're going to see if we can use property listeners
            // This branch works for models that are derived from PropertyEditor instances.
            PropertyListenerRefreshTrigger refreshTrigger = new PropertyListenerRefreshTrigger(propertyName);
            propertyChangeModel.addPropertyChangeListener(refreshTrigger);
            return List.of(refreshTrigger);
        } else {
            // Reflective approach
            Optional<Method> optionalAddListenerMethod = Arrays.stream(targetObject.getClass().getMethods())
                    .filter(method -> !Modifier.isStatic(method.getModifiers()) &&
                            !Modifier.isAbstract(method.getModifiers()) &&
                            method.getName().equals("addPropertyChangeListener") &&
                            method.getParameters().length == 1 &&
                            method.getParameters()[0].getType().equals(PropertyChangeListener.class))
                    .findFirst();
            if (optionalAddListenerMethod.isPresent()) {
                PropertyListenerRefreshTrigger refreshTrigger = new PropertyListenerRefreshTrigger(propertyName);
                final Method addListenerMethod = optionalAddListenerMethod.get();
                try {
                    addListenerMethod.invoke(targetObject, refreshTrigger );
                    return List.of(refreshTrigger);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.warn("Method {} of {} could not be invoked, no refresh trigger generated. ", addListenerMethod.getName(), targetObject.getClass().getName());
                }
            }
            return List.of();
        }
    }





    public void bindButtonGroup( final List<AbstractButton> buttons,
                                  final String property,
                                  final Accessor accessor,
                                  final List<RefreshTrigger> triggers ) {

        // Create the new button group, add all the buttons
        ButtonGroup buttonGroup = new ButtonGroup();
        buttons.forEach(buttonGroup::add);

        // Build the list of values from the buttons
        Map<AbstractButton, Object> buttonValues = buttons.stream().collect(Collectors.toMap(
                element -> element,
                valueMap::get
        ));

        ButtonGroupController buttonGroupController = new ButtonGroupController(buttonGroup);

        // Now bind the group
        bind(buttonGroupController, property, buttonValues, accessor, triggers);
    }

    /**
     * Binds a property of a view {@link Component} to an authoritative value. The view property
     * can be any settable property of the component - often something like 
     * {@link javax.swing.JLabel#setText(String)} or {@link JCheckBox#setSelected(boolean)}, but could
     * be other properties ({@link JComponent#setFont(Font)} for example. The view
     * properties are specified as a property path on the given object: "text", "selected", and
     * "font" respectively for the above examples. The properties must be readable and writable
     * on the view object. <br>
     * The authoritative value represents the value that the view property is bound to. It
     * is often a {@link PropertyAccessor} representing a property of a model or controller object,
     * but could be a property of another bean (such as the application), or an expression or literal.
     * The authoritative value must be readable, and <i>may</i> be writable. If it is writeable,
     * changes by the user to the component will be written to the authoritative state. This is less
     * useful for things like {@link JComponent#setFont(Font)}; it is generally used with the primary
     * value represented by the {@link Component} - {@link JLabel#setText(String)}, {@link JCheckBox#setSelected(boolean)},
     * etc. <br>
     * The binding also allows the authoritative value to change outside the control of the bound
     * {@link Component}. This supports values that are dependent on other model values, read from databases,
     * or other use cases. The binding allows for changes in the authoritative value to be signalled by
     * one or more events, given by the triggers.
     *
     * @param viewObject              A object to bind a property against (typically a view object)
     * @param viewObjectProperty      The property to bind against
     * @param authoritativeValue      An {@link Accessor} for the authorative value
     * @param triggers                A list of triggers for updating the property by re-evaluating the expression
     * @return TRUE if the bind was successful
     */
    public boolean bind(final Object viewObject,
                        final String viewObjectProperty,
                        final Accessor authoritativeValue,
                        final List<RefreshTrigger> triggers) {
        return bind(viewObject, viewObjectProperty, valueMap.get(viewObjectProperty), authoritativeValue, triggers);
    }


    /**
     * Binds a property of a view {@link Component} to an authoritative value. The view property
     * can be any settable property of the component - often something like
     * {@link javax.swing.JLabel#setText(String)} or {@link JCheckBox#setSelected(boolean)}, but could
     * be other properties ({@link JComponent#setFont(Font)} for example. The view
     * properties are specified as a property path on the given object: "text", "selected", and
     * "font" respectively for the above examples. The properties must be readable and writable
     * on the view object. <br>
     * The authoritative value represents the value that the view property is bound to. It
     * is often a {@link PropertyAccessor} representing a property of a model or controller object,
     * but could be a property of another bean (such as the application), or an expression or literal.
     * The authoritative value must be readable, and <i>may</i> be writable. If it is writeable,
     * changes by the user to the component will be written to the authoritative state. This is less
     * useful for things like {@link JComponent#setFont(Font)}; it is generally used with the primary
     * value represented by the {@link Component} - {@link JLabel#setText(String)}, {@link JCheckBox#setSelected(boolean)},
     * etc. <br>
     * The binding also allows the authoritative value to change outside the control of the bound
     * {@link Component}. This supports values that are dependent on other model values, read from databases,
     * or other use cases. The binding allows for changes in the authoritative value to be signalled by
     * one or more events, given by the triggers.
     *
     * @param viewObject              A object to bind a property against (typically a view object)
     * @param viewObjectProperty      The property to bind against
     * @param authoritativeValue      An {@link Accessor} for the authorative value
     * @param triggers                A list of triggers for updating the property by re-evaluating the expression
     * @return TRUE if the bind was successful
     */
    public boolean bind(@NonNull final Object viewObject,
                        @NonNull final String viewObjectProperty,
                        @NonNull final Object viewObjectValue,
                        @NonNull final Accessor authoritativeValue,
                        @NonNull final List<RefreshTrigger> triggers) {


        // Block bindings to the name property
        if (invalidTargetProperty(viewObjectProperty)) return false;

        // Get the view object bean wrapper.
        BeanWrapper viewObjectBeanWrapper = beanWrapperFor(viewObject);

        // See if there is a binder for this property given the source type
        Optional<Binder> found = binders.stream()
                .filter(binder -> binder.binds(viewObjectBeanWrapper, viewObjectProperty, authoritativeValue))
                .findFirst();

        if (found.isPresent()) {
            Binder binder = found.get();
            binder.bind(viewObjectBeanWrapper, viewObjectProperty, viewObjectValue, authoritativeValue, triggers, applicationContext);
            return true;
        } else {
            log.error("Unable to find a binding for property {}", viewObjectProperty);
            return false;
        }
    }



    public boolean bindGroup( @NonNull final List<Object> targetElements,
                              @NonNull final String targetProperty,
                              @NonNull final Accessor modelAccessor,
                              @NonNull final List<RefreshTrigger> triggers ) {

        // Make sure we aren't binding to "name"
        if (invalidTargetProperty(targetProperty)) return false;

        // Get the bean wrappers for the elements
        List<BeanWrapper> beanWrappers = targetElements.stream().map(this::beanWrapperFor).toList();

        try {

            // Ensure that all the bean wrappers have the property in question. If one doesn't
            // this request will throw.
            beanWrappers.forEach(beanWrapper -> beanWrapper.getPropertyDescriptor(targetProperty));

            // If they are all buttons, create a button group
            if (targetElements.stream().allMatch(component -> component instanceof AbstractButton)) {

                final List<AbstractButton> buttonList = targetElements.stream().map(component -> (AbstractButton) component).toList();
                bindButtonGroup(buttonList, targetProperty, modelAccessor, triggers);
                return true;
            }
        } catch (BeansException e) {
            log.error("Property {} not available for all members of the group", targetProperty);
        }
        return false;
    }

    private static boolean invalidTargetProperty(String targetProperty) {
        if ("name".equals(targetProperty)) {
            log.error("Cannot bind to the name property");
            return true;
        }
        return false;
    }
}
