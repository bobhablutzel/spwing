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
import javax.swing.text.JTextComponent;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class JTextComponentEventAdapter extends JComponentEventAdapter {

    private static final Set<String> knownEventNames = Set.of( "caretUpdate" );

    private final JTextComponent textComponent;


    public JTextComponentEventAdapter(JTextComponent textComponent) {
        super(textComponent);
        this.textComponent = textComponent;
    }


    @Override
    public void attachListener(String eventName, Invoker invoker) {
        if ("caretUpdate".equals(eventName)) {
            textComponent.addCaretListener(caretEvent -> this.callInvokerForAction(caretEvent, invoker));
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
        return Map.of(JTextComponent.class, () -> textComponent);
    }


}