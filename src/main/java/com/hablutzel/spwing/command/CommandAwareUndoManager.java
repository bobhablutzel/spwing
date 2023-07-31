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


    /**
     * Get the presentation name from the command that is to be undone
     * @return The presentation name for the command to be undone
     */
    @Override
    public synchronized String getUndoPresentationName() {
        return editToBeUndone().getPresentationName();
    }


    /**
     * Get the presentation name from the command that is to be redone
     * @return The presentation name from the command to be redone
     */
    @Override
    public synchronized String getRedoPresentationName() {
        return editToBeRedone().getPresentationName();
    }

    /**
     * Undo the last done command. This will enforce the
     * guard that ignores Swing generated edits during the
     * undo of the command, and will manage the last
     * change time for determining of the document has changed
     * since the last checkpoint.
     * @throws CannotUndoException If the command cannot be undone
     */
    @Override
    public void undo() throws CannotUndoException {
        UndoableEdit undoneEdit = editToBeUndone();
        withChangeGuard(undoneEdit, super::undo);
        lastChangeTimestamp = lastChangeBeforeEditWasExecuted.get(undoneEdit);
    }


    /**
     * Redo the last undone command. This will enforce the
     * guard that ignores Swing generated edits during the
     * redo of the command, and will manage the last
     * change time for determining of the document has changed
     * since the last checkpoint.
     * @throws CannotRedoException If the command cannot be redone
     */
    @Override
    public void redo() throws CannotRedoException {
        UndoableEdit redoneEdit = editToBeRedone();
        withChangeGuard(redoneEdit, super::redo);
        lastChangeBeforeEditWasExecuted.put(redoneEdit, lastChangeTimestamp);
        lastChangeTimestamp = Instant.now();

    }


    /**
     * Imposes a change guard if the undoableEdit is an instance of
     * {@link ChangeCommand}. All application level changes should
     * be derived from {@link ChangeCommand}, so that the Swing
     * generated undoable events will be ignored during the execution
     * or undoing of those commands.
     * @param undoableEdit The undoable edit
     * @param action The action to take
     */
    private void withChangeGuard(final UndoableEdit undoableEdit,
                                 final Runnable action) {
        if (undoableEdit instanceof ChangeCommand) {
            withGuard(action);
        } else {
            action.run();
        }
    }


    /**
     * Executes an action with the change guard enabled. The
     * change guard discards any Swing component generated
     * undoable edits during the scope of a {@link ChangeCommand}
     * so that the change command, not the Swing generated
     * edits, are the ones that are presented to the user.
     * @param action The action to take (a {@link Runnable})
     */
    private void withGuard(final Runnable action) {
        try {
            inChangeCommand = true;
            action.run();
        } finally {
            inChangeCommand = false;
        }
    }


    /**
     * Discard all the edits in the undo manager. This allows
     * an undo stack to be reset, notably after the views are
     * setup for the first time upon opening a document. In
     * addition to discarding the edits, this routine resets
     * the last checkpoint time.
     */
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


    /**
     * Resets the checkpoint time to the current time. The
     * checkpoint time is the last time the document was opened
     * or saved and is used for determining of the document is
     * dirty. So long as all changes to the document are performed
     * through undoable edits, the checkpoint mechanism will enable
     * the framework to determine if the document is dirty
     */
    public void checkpoint() {
        lastCheckpoint = Instant.now();

        // We still want to be able to undo edits after the checkpoint,
        // but now they have to be recognized as changes. So any
        // commands that can still be undone have to have timestamps
        // later than the last checkpoint
        Instant refreshedInstance = lastCheckpoint.plusMillis(1);
        lastChangeBeforeEditWasExecuted.replaceAll( (key, value) -> refreshedInstance);

    }


    /**
     * When edits are trimmed, we need to discard the change timestamp
     * we are tracking for them. This routine does that.
     * @param from the minimum index to remove
     * @param to the maximum index to remove
     */
    @Override
    protected void trimEdits(int from, int to) {
        super.trimEdits(from, to);

        List<UndoableEdit> liveEdits = lastChangeBeforeEditWasExecuted.keySet().stream()
                .filter(Predicate.not(edits::contains))
                .toList();
        liveEdits.forEach(lastChangeBeforeEditWasExecuted::remove);
    }
}
