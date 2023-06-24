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

import com.hablutzel.spwing.laf.DefaultLookAndFeelFactory;
import com.hablutzel.spwing.laf.LookAndFeelFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;


/**
 * Denotes the main application of a Spwing application.
 * This application is expected to handle certain commands by default
 * such as about box, but can also act as a handler for any
 * arbitrary command.
 * @author Bob Hablutzel
 */
@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Handler
@Component
@Scope("singleton")
public @interface Application {

    /**
     * Defines a menu event that should be triggered when the UI is
     * started. By default, this performs a new. You can choose
     * a different command or cmdNOP to do nothing.
     */
    String onStart() default "cmdNew";

    /**
     * Allow the user to specify a bean name for the bean when it
     * is created. Since this is a singleton bean, the name will
     * be unique for all scopes in the application
     *
     * @return The bean name alias
     */
    String alias() default "application";

    /**
     * Allow the user to define the application name. This will default to
     * empty string, which will then use the name of the application class
     */
    String applicationName() default "";

    /**
     * Define the look and feel for the application. Defaults to "Metal".
     * On MacOS, highly recommend FlatMac look and feel
     */
    Class<? extends LookAndFeelFactory> lookAndFeel() default DefaultLookAndFeelFactory.class;

    /**
     * Any copyright statement that should be associated with the application.
     * Used when auto-generating About boxes.
     * @return The copyright statement
     */
    String copyrightStatement() default "";
}
