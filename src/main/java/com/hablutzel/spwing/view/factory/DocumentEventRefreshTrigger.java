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

package com.hablutzel.spwing.view.factory;


import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.events.DocumentEventInvoker;
import com.hablutzel.spwing.view.bind.RefreshTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;


/**
 * {@link RefreshTrigger} specialized to listen to document events.
 * If the document event is signalled, the refresh will occur.
 * Note: There is currently no SVWF syntax for these refresh events.
 * This needs to be put back in.
 * @author Bob Hablutzel
 * TODO SVWF syntax for this type of refresh
 */
@RequiredArgsConstructor
public class DocumentEventRefreshTrigger implements RefreshTrigger {

    /**
     * The {@link ApplicationContext} instance
     */
    private final ApplicationContext applicationContext;

    /**
     * The {@link DocumentEventDispatcher} associated with the document
     */
    private final DocumentEventDispatcher documentEventDispatcher;

    /**
     * The document event name
     */
    private final String trigger;


    /**
     * Installs the runnable to be called when the document
     * event is signalled.
     *
     * @param runnable The {@link Runnable} to call
     */
    @Override
    public void onRefresh(final Runnable runnable) {
        documentEventDispatcher.registerListener(trigger, DocumentEventInvoker.from(applicationContext, runnable));
    }
}
