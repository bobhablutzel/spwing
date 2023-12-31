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
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


public class JTextFieldEventAdapter extends JTextComponentEventAdapter {

    private static final Set<String> knownEventNames = Set.of( "actionPerformed" );

    private final JTextField textField;

    public JTextFieldEventAdapter(final JTextField textField,
                                  final Spwing spwing) {
        super(textField, spwing);
        this.textField = textField;
    }


    @Override
    public void attachListener(String eventName, Invoker invoker) {
        if ("actionPerformed".equals(eventName)) {
            textField.addActionListener(actionEvent -> this.callInvokerForAction(actionEvent, invoker));
        } else {
            super.attachListener(eventName, invoker);
        }
    }

    @Override
    public boolean understands(String eventName) {
        return knownEventNames.contains(eventName) || super.understands(eventName);
    }

    @Override
    protected Set<Function<ParameterDescription, ParameterResolution>> getInjectedParameters() {
        Set<Function<ParameterDescription, ParameterResolution>> result = new HashSet<>(super.getInjectedParameters());
        result.add(ParameterResolution.forClass(JTextField.class, textField));
        return result;
    }


}
