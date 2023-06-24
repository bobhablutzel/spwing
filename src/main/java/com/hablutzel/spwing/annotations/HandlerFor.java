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

package com.hablutzel.spwing.annotations;


import java.lang.annotation.*;


/**
 * HandleFor allows for the explicit annotation of a method
 * as a handler for a command, thus allowing the method to
 * have a name that does not follow the normal naming
 * convention for handler methods. <br>
 * For the command "cmdXXX", the default name of a handler
 * method is "handleXXX".
 * @author Bob Hablutzel
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HandlerFor {

    /**
     * The command to handle, given as the complete command name
     * (e.g. cm_Save)
     * @return The complete command name
     */
    String value();
}
