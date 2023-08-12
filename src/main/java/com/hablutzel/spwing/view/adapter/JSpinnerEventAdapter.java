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

package com.hablutzel.spwing.view.adapter;

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


/**
 * Event adapter for the JCheckBox class. Mostly
 * a pass through to the {@link AbstractButtonEventAdapter}
 * @author Bob Hablutzel
 */
public final class JSpinnerEventAdapter extends JComponentEventAdapter {

    /**
     * The JCheckBox
     */
    private final JSpinner spinner;

    /**
     * Constructor.
     * @param spinner The spinner
     * @param spwing The Spwing framework instance
     */
    public JSpinnerEventAdapter(final JSpinner spinner,
                                final Spwing spwing) {
        super(spinner, spwing);
        this.spinner = spinner;
    }


    /**
     * Inject the spinner into the parameters
     *
     * @return A map with the check box supplier
     */
    @Override
    protected Set<Function<ParameterDescription, ParameterResolution>> getInjectedParameters() {
        Set<Function<ParameterDescription, ParameterResolution>> result = new HashSet<>(super.getInjectedParameters());
        result.add(ParameterResolution.forClass(JSpinner.class, spinner));
        result.add(ParameterResolution.forClass(SpinnerModel.class, spinner.getModel()));
        return result;
    }

}
