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

package com.hablutzel.spwing.command;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;

import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.util.Locale;


/**
 * Subclass of {@link AggregateEventListener} specialized for
 * {@link javax.swing.text.AbstractDocument.DefaultDocumentEvent} instances
 *
 * @author Bob Hablutzel
 */
@Slf4j
public class DocumentEventListener extends AggregateEventListener<AbstractDocument.DefaultDocumentEvent> {

    private String lastEditName;

    private MessageSource messageSource;
    private JTextComponent textComponent;

    public DocumentEventListener(final JTextComponent textComponent,
                                 final MessageSource messageSource,
                                 final UndoManager undoManager) {
        super(undoManager);
        this.textComponent = textComponent;
        this.messageSource = messageSource;
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent undoableEditEvent) {

        // Make sure we have the type we expect
        UndoableEdit undoableEdit = undoableEditEvent.getEdit();
        if (undoableEdit instanceof AbstractDocument.DefaultDocumentEvent documentEvent) {

            // We have to determine if we need to start a edit. This can happen for
            // a variety of reasons - the new edit is beyond the document, the presentation
            // name doesn't match, or simply because the current edit is null. If any
            // of these are true, we reset and create a new aggregate edit.
            final int start = documentEvent.getOffset();
            final int len = documentEvent.getLength();
            final boolean becauseOfOffset = start + len > documentEvent.getDocument().getLength();
            final boolean becauseOfName = !documentEvent.getPresentationName().equals(lastEditName);
            this.lastEditName = documentEvent.getPresentationName();
            final boolean becauseNull = null == current;
            if (becauseOfOffset || becauseOfName || becauseNull) {

                // Get the name of the component
                String name = textComponent.getName();

                // Create a name for the aggregate command
                String presentationName = messageSource.getMessage("textComponentEdit",
                        new String[] { name },
                        Locale.getDefault());
                createNewAggregateEdit(presentationName);
            }
            current.append(documentEvent);
        }
    }

}