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

package com.hablutzel.spwing.view.factory.cocoon;

import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.RefreshTrigger;
import com.hablutzel.spwing.view.factory.RefreshListener;
import com.hablutzel.spwing.view.factory.component.ViewComponentFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;

import java.awt.Component;
import java.util.List;


/**
 * {@link Cocoon} instances provide an environment where
 * a newly created Swing component can continue to evolve
 * toward the final state. The Cocoon is responsible for
 * knowing what properties can be bound, which can be
 * set directly, and which need special processing in order
 * to be changed (e.g. JSpinner editor format values)
 *
 * @param <T> The {@link Component} subclass
 * @author Bob Hablutzel
 */
@Slf4j
public class Cocoon<T extends Component> {


    /**
     * The component being incubated
     */
    @Getter
    private final T component;

    /**
     * The associated {@link ViewComponentFactory} that created the component
     */
    @Getter
    private final ViewComponentFactory<T> factory;

    /**
     * A {@link BeanWrapper} for the component used to set generic properties
     */
    private final BeanWrapper beanWrapper;

    /**
     * Conversion service for changing values from the external state to the expected
     * component type
     */
    private final ConversionService conversionService;

    /**
     * A {@link RefreshListener} used for listening to changes to the generic properties
     */
    private RefreshListener refreshListener = null;

    public enum AllowedBindings { NONE, FROM_COMPONENT, FROM_MODEL, BIDIRECTIONAL}


    /**
     * Constructor
     * @param component The component
     * @param factory The {@link ViewComponentFactory}
     * @param conversionService The {@link Component}
     */
    public Cocoon(final T component,
                  final ViewComponentFactory<T> factory,
                  final ConversionService conversionService) {
        this.component = component;
        this.factory = factory;
        this.conversionService = conversionService;
        this.beanWrapper = new BeanWrapperImpl(component);
    }

    /**
     * Get the refresh trigger that can listener to
     * property changes
     *
     * @return The {@link RefreshListener}.
     */
    public @NonNull RefreshListener getRefreshListener() {
        if (null == refreshListener) {
            refreshListener = new RefreshListener();
            component.addPropertyChangeListener(refreshListener);
        }
        return refreshListener;
    }


    /**
     * Returns the allowable bindings between the model and the component.
     *
     * @param propertyName The property name
     * @return The allowable binding denoted by {@link AllowedBindings}
     */
    public AllowedBindings allowedBindings(final String propertyName) {
        return this.canSetProperty(propertyName) && !propertyName.equals("name")
                ? AllowedBindings.BIDIRECTIONAL
                : AllowedBindings.NONE;
    }


    /**
     * Returns TRUE if the property can be set - that is, that it
     * can be changed during the creation process
     * @param propertyName The property name
     * @return TRUE if the property can be set
     */
    public boolean canSetProperty(final String propertyName) {
        return beanWrapper.isWritableProperty(propertyName);
    }


    /**
     * Set a literal value.
     * @param propertyName The property name
     * @param value The value to set
     */
    public void setProperty(final String propertyName,
                            final Object value ) {
        if (canSetProperty(propertyName)) {

            // Get the expected state
            Class<?> componentPropertyType = beanWrapper.getPropertyType(propertyName);
            if (null != componentPropertyType) {
                beanWrapper.setPropertyValue(propertyName, value);
            }
        }
    }

    /**
     * Set the property to the value supplied by the external state
     * {@link Accessor}. This is a one-time change during the evolution
     * of the component state.
     *
     * @param propertyName The property name
     * @param externalState The external state {@link Accessor}
     */
    public void setFromAccessor(final String propertyName,
                                final Accessor externalState) {

        if (canSetProperty(propertyName)) {

            // Get the expected type - if it's null (hasn't been set yet) get the default type
            Class<?> componentPropertyType = beanWrapper.getPropertyType(propertyName);
            Class<?> targetType = null == componentPropertyType ? getDefaultModelPropertyType() : componentPropertyType;

            // Get the external state and set it
            Object value = getExternalState(externalState, targetType, false );
            setProperty(propertyName, value);
        }
    }


    public Class<?> getDefaultModelPropertyType() {
        return Object.class;
    }


    /**
     * Get the external state, throwing if it is null. This is equivalent
     * to calling {@link #getExternalState(Accessor, Class, boolean)} passing TRUE
     *
     * @param accessor The external state {@link Accessor}
     * @param clazz The expected type
     *
     */
    protected <X> X getExternalState( final @NonNull Accessor accessor,
                                      final @NonNull Class<X> clazz ) {
        return getExternalState(accessor, clazz, true );
    }


    /**
     * Get the external state as the expected type
     * @param accessor The external state {@link Accessor}
     * @param clazz The expected type
     * @param <X> The expected type parameter
     */
    protected <X> X getExternalState(final @NonNull Accessor accessor,
                                     final @NonNull Class<X> clazz,
                                     final boolean throwOnNull ) {
        Object externalState = accessor.get();
        if (null == externalState) {
            if (throwOnNull) {
                log.error("Got a null but null was disallowed" );
                throw new RuntimeException("Got a disallowed null value" );
            }
            return null;
        } else {
            if (clazz.isAssignableFrom(externalState.getClass())) {
                return clazz.cast(externalState);
            } else {
                if (conversionService.canConvert(externalState.getClass(), clazz)) {
                    return conversionService.convert(externalState, clazz);
                } else {
                    log.error("Cannot convert {} to {}", externalState.getClass().getName(), clazz.getName());
                    throw new RuntimeException("Unable to convert value to required class" );
                }
            }
        }
    }


    protected <X> X getAs( final Object value,
                           final @NonNull Class<X> clazz ) {
        if (null == value) {
            return null;
        } else if (conversionService.canConvert(value.getClass(), clazz)) {
            return conversionService.convert(value, clazz);
        } else {
            log.error( "Unable to convert a {} to {}", value.getClass().getName(), clazz.getName());
            throw new RuntimeException("Unable to convert to required type");
        }
    }


    /**
     * Bind the property. This changes the property value immediately, but also
     * attempts to maintain consistency between the value and the external state
     * during runtime. This binding can be bidirectional, assuming that the property
     * can be changed at runtime and that the external state accessor is writable.
     * @param propertyName The property name
     * @param externalState The external state {@link Accessor}
     * @param refreshTriggers The list of {@link RefreshTrigger} that trigger a refresh
     *                        of the external state. The model will initiate these.
     */
    public void bindProperty(final String propertyName,
                             final Accessor externalState,
                             final List<RefreshTrigger> refreshTriggers) {
        AllowedBindings allowedBindings = allowedBindings(propertyName);
        if (allowedBindings == AllowedBindings.FROM_MODEL || allowedBindings == AllowedBindings.BIDIRECTIONAL) {
            setFromAccessor(propertyName, externalState);
            refreshTriggers.forEach( refreshTrigger ->
                refreshTrigger.onRefresh( () -> setFromAccessor(propertyName, externalState))
            );
        }

        if (allowedBindings == AllowedBindings.FROM_COMPONENT || allowedBindings == AllowedBindings.BIDIRECTIONAL) {
            RefreshListener listener = getRefreshListener();
            listener.addEventConsumer(propertyName, event -> {
                // Short-circuit cyclic refreshes if the value is already set.
                if (!event.getNewValue().equals(externalState.get())) {
                    externalState.set(event.getNewValue());
                }
            });
        }
    }
}
