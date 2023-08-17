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

import com.hablutzel.spwing.view.bind.controllers.ButtonGroupController;
import com.hablutzel.spwing.view.bind.impl.GenericPropertyBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;

import javax.swing.*;
import java.awt.Component;
import java.awt.Font;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class ViewPropertyBinder {

    private final ApplicationContext applicationContext;
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
