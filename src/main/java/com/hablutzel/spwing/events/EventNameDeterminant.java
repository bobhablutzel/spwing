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

import com.hablutzel.spwing.component.EventFamily;


/**
 * By convention, event names that begin with <code>evt</code> are considered
 * document events, and all other events are considered AWT events. The
 * application can define a bean of type {@link EventNameDeterminant} to
 * change this behavior. If such a bean exists, it will be used to to attempt
 * to determine the event classification. Implementations of this interface
 * should return {@link EventFamily#AWT} for AWT events,
 * {@link EventFamily#Document} for document events, or
 * {@link EventFamily#Introspection} for the default behavior.<br>
 * @author Bob Hablutzel
 */
public interface EventNameDeterminant {

    default EventFamily examineName(String name) {
        return name.startsWith("evt") ? EventFamily.Document : EventFamily.AWT;
    }

    default String toDocumentEvent( String name) {
        return String.format( "evt%s", name );
    }
}
