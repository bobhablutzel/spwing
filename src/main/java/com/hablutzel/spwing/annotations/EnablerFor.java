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
 * EnablerFor annotates a method to be an enabler method
 * for a command. This method does not have to be in the
 * same class as the handler for the command; for example
 * a model class might provide functionality to determine
 * when the model needs to be saved, but leave the default
 * save command processing in place.<br>
 * The method must return a boolean value which will
 * be used to determine if the handler method is active
 * or not. Note that an inactive handler method will
 * mean the command is disabled; there is no provision
 * currently for delegating an inactive command in one
 * handler to an active handler lower in the handler stack.<br>
 * For the command "cmdXXX", the default name of an enabler
 * method is "enableXXX".
 * @author Bob Hablutzel
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnablerFor {
    /**
     * The command to enable, given as the complete command name
     * (e.g. cmdSave)
     * @return The complete command name
     */
    String value();
}
