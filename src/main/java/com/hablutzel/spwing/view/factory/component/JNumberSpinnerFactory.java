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

package com.hablutzel.spwing.view.factory.component;

import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.RefreshTrigger;
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import java.util.List;

/**
 * Create a new number {@link JSpinner} instance.
 *
 * @author Bob Hablutzel
 */
@Service
@Scope("singleton")
@Slf4j
public class JNumberSpinnerFactory extends AbstractSpinnerFactory {

    @Override
    public String alias() {
        return "JNumberSpinner";
    }

    @Override
    protected Cocoon<JSpinner> buildCocoon(final ConversionService conversionService) {

        final SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel();
        final JSpinner spinner = new JSpinner(spinnerNumberModel);
        final JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(spinner);
        spinner.setEditor(numberEditor);

        return new JNumberSpinnerCocoon(spinner, conversionService, spinnerNumberModel);
    }

    /**
     * The cocoon for JNumberSpinners
     *
     * @author Bob Hablutzel
     */
    private class JNumberSpinnerCocoon extends Cocoon<JSpinner> {

        private final JSpinner spinner;
        private final SpinnerNumberModel spinnerNumberModel;

        public JNumberSpinnerCocoon(JSpinner spinner, ConversionService conversionService, SpinnerNumberModel spinnerNumberModel) {
            super(spinner, JNumberSpinnerFactory.this, conversionService);
            this.spinner = spinner;
            this.spinnerNumberModel = spinnerNumberModel;
        }

        /**
         * Add support for the date spinner specific properties
         *
         * @param propertyName The property name
         * @return TRUE if the property can be set
         */
        @Override
        public boolean canSetProperty(String propertyName) {
            return switch (propertyName) {
                case "model" -> false;
                case "format", "min", "max" -> true;
                default -> super.canSetProperty(propertyName);
            };
        }


        /**
         * Set the property from the specified value
         *
         * @param propertyName The property name
         * @param value The value to set
         */
        @Override
        public void setProperty(String propertyName, Object value) {
            switch (propertyName) {
                case "format" -> spinner.setEditor(new JSpinner.NumberEditor(spinner, getAs(value, String.class)));
                case "min" -> spinnerNumberModel.setMinimum(getAs(value, Integer.class));
                case "max" -> spinnerNumberModel.setMaximum(getAs(value, Integer.class));
                default -> super.setProperty(propertyName, value);
            }
        }

        /**
         * Set our custom properties
         * @param propertyName The property name
         * @param externalState The external state {@link Accessor}
         */
        @Override
        public void setFromAccessor(String propertyName, Accessor externalState) {
            switch (propertyName) {
                case "format" -> spinner.setEditor(new JSpinner.NumberEditor(spinner, getExternalState(externalState, String.class)));
                case "min" -> spinnerNumberModel.setMinimum(getExternalState(externalState, Comparable.class));
                case "max" -> spinnerNumberModel.setMaximum(getExternalState(externalState, Comparable.class));
                default -> super.setFromAccessor(propertyName, externalState);
            }
        }

        /**
         * Get the allowable bindings for our custom properties
         * @param propertyName The property name
         * @return The allowable bindings
         */
        @Override
        public AllowedBindings allowedBindings(String propertyName) {
            return switch (propertyName) {
                case "model" -> AllowedBindings.NONE;
                case "format", "min", "max" -> AllowedBindings.FROM_MODEL;
                default -> super.allowedBindings(propertyName);
            };
        }
        /**
         * Add special case processing for the value property
         * @param propertyName The property name
         * @param externalState The external state {@link Accessor}
         * @param refreshTriggers The list of {@link RefreshTrigger} that trigger a refresh
         *                        of the external state. The model will initiate these.
         */
        @Override
        public void bindProperty(String propertyName, Accessor externalState, List<RefreshTrigger> refreshTriggers) {
            super.bindProperty(propertyName, externalState, refreshTriggers);
            if ("value".equals(propertyName) && externalState.isWriteable()) {
                spinnerNumberModel.addChangeListener(evt -> {
                    if (!spinnerNumberModel.getValue().equals(externalState.get())) {
                        externalState.set(spinnerNumberModel.getValue());
                    }
                });
            }
        }


    }
}
