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
import com.hablutzel.spwing.view.bind.RefreshTrigger;
import com.hablutzel.spwing.view.factory.component.ViewComponentFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;

import javax.swing.JTextField;
import javax.swing.text.Document;
import java.util.List;


@Slf4j
public class JTextComponentCocoon<T extends JTextField> extends Cocoon<T> {

    private final JTextField textField;

    public JTextComponentCocoon(T textField, ViewComponentFactory<T> factory, ConversionService conversionService) {
        super(textField, factory, conversionService);
        this.textField = textField;
    }


    @Override
    public void bindProperty(String propertyName, Accessor externalState, List<RefreshTrigger> refreshTriggers) {
        super.bindProperty(propertyName, externalState, refreshTriggers);
        if ("text".equals(propertyName) && externalState.isWriteable()) {
            Document document = textField.getDocument();
            if (document != null) {
                TextComponentListener textComponentListener = new TextComponentListener(externalState, textField);
                textField.addPropertyChangeListener("document", e -> {
                    Document oldDocument = (Document) e.getOldValue();
                    Document newDocument = (Document) e.getNewValue();
                    if (null != oldDocument) oldDocument.removeDocumentListener(textComponentListener);
                    if (null != newDocument) newDocument.addDocumentListener(textComponentListener);
                    textComponentListener.changedUpdate(null);
                });
                document.addDocumentListener(textComponentListener);
            }
        }
    }
}
