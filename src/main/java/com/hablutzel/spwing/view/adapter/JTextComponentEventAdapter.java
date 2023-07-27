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
import com.hablutzel.spwing.command.DocumentEventListener;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;
import lombok.extern.slf4j.Slf4j;

import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


@Slf4j
public class JTextComponentEventAdapter extends JComponentEventAdapter {

    private static final Set<String> knownEventNames = Set.of( "caretUpdate" );

    private final JTextComponent textComponent;


    public JTextComponentEventAdapter(final JTextComponent textComponent,
                                      final Spwing spwing) {
        super(textComponent, spwing);
        this.textComponent = textComponent;

        // Add a new document event listener to the document. This captures document
        // events and pipes them to the document undo manager.
        UndoManager undoManager = spwing.getDocumentScopeManager().getActiveSession().getUndoManager();
        final DocumentEventListener documentEventListener = new DocumentEventListener(textComponent, spwing.getApplicationContext(), undoManager);
        textComponent.getDocument().addUndoableEditListener(documentEventListener);

        // If the user clicks in that text field, we close any open aggregate edits and
        // start a new one. This handles the case where the user clicks into a different
        // part of the field to start editing and ensures that's a separate undo event.
        textComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                documentEventListener.closeCurrent();
            }
        });
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
    protected Set<Function<ParameterDescription, ParameterResolution>> getInjectedParameters() {
        Set<Function<ParameterDescription, ParameterResolution>> result = new HashSet<>(super.getInjectedParameters());
        result.add(ParameterResolution.forClass(JTextComponent.class, textComponent));
        return result;
    }


}