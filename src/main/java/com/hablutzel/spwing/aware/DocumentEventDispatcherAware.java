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

package com.hablutzel.spwing.aware;

import com.hablutzel.spwing.events.DocumentEventDispatcher;

/**
 * Used for classes that need to know about the {@link DocumentEventDispatcher}
 * for the current document. Classes with this annotation will have the
 * document event dispatcher injected at bean creation time.
 *
 * @author Bob Hablutzel
 */
public interface DocumentEventDispatcherAware {

    /**
     * Required method to accept the document event dispatcher
     * @param documentEventDispatcher The {@link DocumentEventDispatcher} instance
     */
    void setDocumentEventDispatcher( DocumentEventDispatcher documentEventDispatcher );
}
