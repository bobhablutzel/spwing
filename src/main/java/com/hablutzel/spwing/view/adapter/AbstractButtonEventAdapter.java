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

import com.hablutzel.spwing.invoke.Invoker;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;


public class AbstractButtonEventAdapter extends JComponentEventAdapter {

    private static final Set<String> knownEventNames = Set.of(
            "actionPerformed", "stateChanged", "itemStateChanged", "clicked" );

    private final AbstractButton abstractButton;

    public AbstractButtonEventAdapter(AbstractButton abstractButton) {
        super(abstractButton);
        this.abstractButton = abstractButton;
    }


    @Override
    public void attachListener(String eventName, Invoker invoker) {
        if ("actionPerformed".equals(eventName)) {
            abstractButton.addActionListener(actionEvent -> this.callInvokerForAction(actionEvent, invoker));
        } else if ("clicked".equals(eventName)) {
            abstractButton.addActionListener(actionEvent -> this.callInvokerForAction(actionEvent, invoker));
        } else if ("stateChanged".equals(eventName)) {
            abstractButton.addChangeListener(changeEvent -> this.callInvokerForAction(changeEvent, invoker));
        } else if ("itemStateChanged".equals(eventName)) {
            abstractButton.addItemListener(itemEvent -> this.callInvokerForAction(itemEvent, invoker));
        } else {
            super.attachListener(eventName, invoker);
        }
    }

    @Override
    public boolean understands(String eventName) {
        return knownEventNames.contains(eventName) || super.understands(eventName);
    }


    @Override
    protected Map<Class<?>, Supplier<Object>> getParameterMap() {
        return Map.of(AbstractButton.class, () -> abstractButton,
                Boolean.class, abstractButton::isSelected);
    }

}
