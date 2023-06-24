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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Defines a controller in the Spwing application. Controllers are the
 * active part of the application. {@link Model} defines the
 * data (often a POJO / bean), the Swing components define the View,
 * and bean instances marked with {@link Controller}
 *
 * @author Bob Hablutzel
 */
@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Handler()
@Scope("document")
@Component
public @interface Controller {

    /**
     * Allow the user to specify a bean name for the bean when it
     * is created. Since this is a document-scope bean, it will
     * be a singleton in the document scope and this bean name will
     * be unique in that scope.
     *
     * @return The bean name alias
     */
    String alias() default "controller";

}
