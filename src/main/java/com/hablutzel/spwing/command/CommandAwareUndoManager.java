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

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * An undo manager that is aware of {@link ChangeCommand} instances.
 * The {@link ChangeCommand} provides a base capability for changes
 * that are triggered by an action and may impact the UI directly. During
 * the execution of a {@link ChangeCommand}, Swing generated undoable
 * events are ignored. For example, if you have a button that recalculates
 * and changes the text of a {@link javax.swing.text.JTextComponent} subclass,
 * using a {@link ChangeCommand} will ensure that the undo commands will
 * undo the changes atomically, rather than as individual text field actions.
 * @author Bob Hablutzel
 */
@Slf4j
public class CommandAwareUndoManager extends UndoManager {

    /**
     * TRUE while executing a change command
     */
    private boolean inChangeCommand = false;

    private Instant lastChangeTimestamp = null;
    private Instant lastCheckpoint = Instant.now();

    private final Map<UndoableEdit,Instant> lastChangeBeforeEditWasExecuted = new HashMap<>();


    /**
     * Add a new {@link ChangeCommand} to the undo stack.
     * @param undoableEdit the edit to be added
     * @return Whether we took this command
     */
    @Override
    public synchronized boolean addEdit(UndoableEdit undoableEdit) {
        if (!inChangeCommand) {
            if (undoableEdit instanceof ChangeCommand changeCommand) {
                withGuard(changeCommand::added);
            }
            boolean result = super.addEdit(undoableEdit);
            lastChangeBeforeEditWasExecuted.put(undoableEdit, lastChangeTimestamp);
            lastChangeTimestamp = Instant.now();
            return result;
        } else {
            log.debug( "Ignored {} because we are executing a command", undoableEdit.getPresentationName());
            return true;
        }
    }


    @Override
    public synchronized String getUndoPresentationName() {
        return editToBeUndone().getPresentationName();
    }

    @Override
    public synchronized String getRedoPresentationName() {
        return editToBeRedone().getPresentationName();
    }

    @Override
    public void undo() throws CannotUndoException {
        UndoableEdit undoneEdit = editToBeUndone();
        withLastGuard(super::undo);
        lastChangeTimestamp = lastChangeBeforeEditWasExecuted.get(undoneEdit);
    }



    @Override
    public void redo() throws CannotRedoException {
        UndoableEdit redoneEdit = editToBeRedone();
        withLastGuard(super::redo);
        lastChangeBeforeEditWasExecuted.put(redoneEdit, lastChangeTimestamp);
        lastChangeTimestamp = Instant.now();
    }


    private void withLastGuard(Runnable action) {
        if (lastEdit() instanceof ChangeCommand) {
            withGuard(action);
        } else {
            action.run();
        }
    }


    private void withGuard(Runnable action) {
        try {
            inChangeCommand = true;
            action.run();
        } finally {
            inChangeCommand = false;
        }
    }


    @Override
    public synchronized void discardAllEdits() {
        checkpoint();
        super.discardAllEdits();
    }

    /**
     * Find out if there are undoable commands on the stack.
     * @return TRUE if there are undoable commands
     */
    public boolean changesHaveOccurredSinceLastCheckpoint() {
        return null != lastChangeTimestamp && lastCheckpoint.isBefore(lastChangeTimestamp);
    }


    public void checkpoint() {
        lastCheckpoint = Instant.now();

        // We still want to be able to undo edits after the checkpoint,
        // but now they have to be recognized as changes. So any
        // commands that can still be undone have to have timestamps
        // later than the last checkpoint
        Instant refreshedInstance = lastCheckpoint.plusMillis(1);
        lastChangeBeforeEditWasExecuted.replaceAll( (key, value) -> refreshedInstance);

    }


    @Override
    protected void trimEdits(int from, int to) {
        super.trimEdits(from, to);

        List<UndoableEdit> liveEdits = lastChangeBeforeEditWasExecuted.keySet().stream()
                .filter(Predicate.not(edits::contains))
                .toList();
        liveEdits.forEach(lastChangeBeforeEditWasExecuted::remove);
    }
}
