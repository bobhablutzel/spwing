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

package com.hablutzel.spwing.events;

import com.hablutzel.spwing.invoke.DirectInvoker;
import com.hablutzel.spwing.invoke.DirectParameterDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;


@Slf4j
public abstract class DocumentEventInvoker extends DirectInvoker {

    public DocumentEventInvoker(ApplicationContext applicationContext) {
        super(applicationContext, "listener", List.of(
                new DirectParameterDescription("e", DocumentEvent.class, false, 0, false )
        ));
    }

    @Override
    protected Object doInvoke(Object[] dynamicArguments) {
        if (dynamicArguments[0] instanceof DocumentEvent documentEvent) {
            this.handleDocumentEvent(documentEvent);
        } else {
            log.error("Unexpected argument for invocation of DocumentEventInvoker: {}", dynamicArguments[0].getClass().getName());
        }
        return null;
    }

    protected abstract void handleDocumentEvent(DocumentEvent documentEvent);
}
