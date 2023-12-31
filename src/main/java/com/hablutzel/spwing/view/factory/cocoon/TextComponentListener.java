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

package com.hablutzel.spwing.view.factory.cocoon;

import com.hablutzel.spwing.view.bind.Accessor;
import lombok.RequiredArgsConstructor;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

@RequiredArgsConstructor
public class TextComponentListener implements DocumentListener {
    private final Accessor externalState;
    private final JTextComponent textComponent;
    private int lastChange;
    private int lastNotifiedChange;

    @Override
    public void insertUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changedUpdate(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        lastChange++;
        SwingUtilities.invokeLater(() -> {
            if (lastNotifiedChange != lastChange) {
                lastNotifiedChange = lastChange;
                String text = textComponent.getText();
                if (!text.equals(externalState.get())) {
                    externalState.set(textComponent.getText());
                }
            }
        });
    }
}
