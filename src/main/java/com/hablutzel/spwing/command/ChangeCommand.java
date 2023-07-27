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

import javax.swing.undo.UndoableEdit;


/**
 * ChangeCommand provides the base functionality for
 * undoable commands in the Spwing framework. More specifically,
 * ChangeCommands coordinate with the {@link CommandAwareUndoManager}
 * to suppress any Swing generated undoable commands that might
 * be created while the ChangeCommand is working. This allows
 * the change command to change model properties that are bound
 * to view objects without having the resulting view undo changes
 * pollute the undo stack.
 *
 * @author Bob Hablutzel
 */
@RequiredArgsConstructor
public abstract class ChangeCommand implements UndoableEdit {

    /**
     * The name for undo / redo menu support
     */
    protected final String presentationName;


    /**
     * Called by the undo manager when this is added to the undo
     * set. By default will trigger the activities of the command by
     * invoking the "redo" function. If the redo functions has other
     * side effects other than simply performing the action (or
     * conversely the initial action needs to have other side effects)
     * the user can override this method to directly perform the actions.
     * This will be called once, when the undo is added.
     */
    public void added() {
        redo();
    }


    /**
     * Get the presentation name
     *
     * @return The presentation name
     */
    @Override
    public String getPresentationName() {
        return presentationName;
    }

    @Override
    public String getUndoPresentationName() {
        return null;
    }

    @Override
    public String getRedoPresentationName() {
        return null;
    }
}
