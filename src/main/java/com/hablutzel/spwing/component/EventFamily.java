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

package com.hablutzel.spwing.component;

/**
 * Generally speaking, events need to be in one of two families: the AWT events
 * and document events. Also generally speaking, the name of the event denotes
 * what kind of event it is. However, that determination can be forced if
 * need be. By default, the determinationMode for the event will be Introspection,
 * but can be changed to Document or AWT to force the event into one family or
 * another.
 */
public enum EventFamily {Introspection, Document, AWT}