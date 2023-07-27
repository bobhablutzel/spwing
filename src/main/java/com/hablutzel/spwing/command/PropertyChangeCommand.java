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

import lombok.Setter;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class PropertyChangeCommand<T> extends ChangeCommand {

    private final T originalValue;
    private final T newValue;
    private final Consumer<T> setter;

    @Setter
    private boolean significant = true;

    public PropertyChangeCommand(final T newValue,
                                 final Supplier<T> getter,
                                 final Consumer<T> setter,
                                 final String commandName) {
        super(commandName);
        this.originalValue = getter.get();
        this.newValue = newValue;
        this.setter = setter;
    }


    @Override
    public void undo() throws CannotUndoException {
        setter.accept(originalValue);
    }

    @Override
    public boolean canUndo() {
        return true;
    }

    @Override
    public void redo() throws CannotRedoException {
        setter.accept(newValue);
    }

    @Override
    public boolean canRedo() {
        return true;
    }

    @Override
    public void die() {
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        return false;
    }

    @Override
    public boolean replaceEdit(UndoableEdit anEdit) {
        return false;
    }

    @Override
    public boolean isSignificant() {
        return significant;
    }

}
