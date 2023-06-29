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

package com.hablutzel.spwing.view.bind.watch;

import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.PropertyAccessor;
import com.hablutzel.spwing.view.bind.Binder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.Objects;




/**
 * Binds properties to a {@link JTextComponent} subclass. This sets the value,
 * and for writeable authoritative state and "text" properties will also catch
 * both changes to the document and changes to the document component itself.
 * When changes occur, the {@link Accessor} (which must be writable - see
 * {@link Accessor#isWriteable()} will be called to change the value. Typically
 * this will be a {@link PropertyAccessor} and will therefore change the
 * described property.
 */
@Service
@Slf4j
public class JTextComponentBinder extends BaseBinder {

    @Override
    public boolean binds(@NonNull final BeanWrapper componentWrapper,
                         @NonNull final String propertyName,
                         @NonNull final Accessor authoritativeValueAccessor) {
        return JTextComponent.class.isAssignableFrom(componentWrapper.getWrappedInstance().getClass()) &&
                "text".equals(propertyName);
    }

    @Override
    public void bind(@NonNull final BeanWrapper wrappedTargetObject,
                     @NonNull final String propertyName,
                     @NonNull final Object targetObjectValue,
                     @NonNull final Accessor authoritativeValueAccessor,
                     @NonNull final List<String> triggers,
                     @NonNull final ApplicationContext applicationContext) {

        log.info( "Binding {} to {}", wrappedTargetObject.getWrappedInstance(), authoritativeValueAccessor.get(String.class));
        super.bind(wrappedTargetObject, propertyName, targetObjectValue, authoritativeValueAccessor, triggers, applicationContext);

        if (wrappedTargetObject.getWrappedInstance() instanceof JTextComponent textComponent) {
            this.bindValueToControl(wrappedTargetObject, propertyName, authoritativeValueAccessor, triggers, applicationContext);
            if (authoritativeValueAccessor.isWriteable()) {
                watchForChangesToDocument(textComponent, authoritativeValueAccessor);
            }
        }
    }


    private void watchForChangesToDocument(JTextComponent textComponent, Accessor accessor) {
        TextComponentListener textComponentListener = new TextComponentListener(accessor, textComponent);
        textComponent.addPropertyChangeListener("document", (
                PropertyChangeEvent e) -> {
            Document oldDocument = (Document) e.getOldValue();
            Document newDocument = (Document) e.getNewValue();
            if (Objects.nonNull(oldDocument)) oldDocument.removeDocumentListener(textComponentListener);
            if (Objects.nonNull(newDocument)) newDocument.addDocumentListener(textComponentListener);
            textComponentListener.changedUpdate(null);
        });
        Document document = textComponent.getDocument();
        if (document != null) document.addDocumentListener(textComponentListener);
    }
}
