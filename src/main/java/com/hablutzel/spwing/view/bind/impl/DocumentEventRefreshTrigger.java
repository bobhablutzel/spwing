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

package com.hablutzel.spwing.view.bind.impl;


import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.events.DocumentEventInvoker;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

@RequiredArgsConstructor
public class DocumentEventRefreshTrigger implements RefreshTrigger {

    private final ApplicationContext applicationContext;
    private final DocumentEventDispatcher documentEventDispatcher;
    private final String trigger;

    @Override
    public void onRefresh(final Runnable runnable) {
        documentEventDispatcher.registerListener(trigger, DocumentEventInvoker.from(applicationContext, runnable));
    }
}
