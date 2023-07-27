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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * CompositeEdits are used for classes, such as the {@link javax.swing.text.JTextComponent}
 * classes, that generate individual events that should be aggregated into a single event.
 * This class is conceptually similar to the Swing class {@link AggregateEdit}, but
 * differs in that it explicitly does not attempt to combine the edits but rather treats
 * them as a collection to be managed collectively.
 *
 * @param <T> The edit type
 * @author Bob Hablutzel
 */
@RequiredArgsConstructor
@Slf4j
public class AggregateEdit<T extends UndoableEdit> extends AbstractUndoableEdit {

    private final String presentationName;
    private final AggregateEventListener<T> aggregateEventListener;
    private final List<T> edits = new ArrayList<>();
    boolean isUnDone = false;

    public boolean isEmpty() {
        return edits.isEmpty();
    }


    @Override
    public String getPresentationName() {
        return presentationName;
    }

    @Override
    public void die() {
        super.die();
        aggregateEventListener.noLongerCurrent(this);
    }

    public void undo() throws CannotUndoException {
        List<T> reverse = new ArrayList<>(edits);
        Collections.reverse(reverse);
        reverse.forEach(UndoableEdit::undo);
        aggregateEventListener.noLongerCurrent(this);
        isUnDone = true;

    }

    public void redo() throws CannotUndoException {
        edits.forEach(UndoableEdit::redo);
        isUnDone = false;
    }

    public boolean canUndo() {
        return edits.size() > 0 && !isUnDone;
    }

    public boolean canRedo() {
        return edits.size() > 0 && isUnDone;
    }


    public void append(T targetEvent) {
        edits.add(targetEvent);
    }
}
