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
import com.hablutzel.spwing.view.factory.reflective.ReflectiveViewFactory;
import com.hablutzel.spwing.view.factory.svwf.SVWFResourceViewFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Application
@Model(primary = true)
@Controller
@Documented
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Handler()
@Component
@Scope("document")
public @interface AllInOneApplication {

    /**
     * Defines a menu event that should be triggered when the UI is
     * started. By default, this performs a new. You can choose
     * a different command or cmdNOP to do nothing.
     */
    @AliasFor(annotation = Application.class, attribute = "onStart")
    String onStart() default "cmdNew";

    /**
     * Allow the user to specify a bean name for the bean when it
     * is created. Since this is a singleton bean, the name will
     * be unique for all scopes in the application
     *
     * @return The bean name alias
     */
    @AliasFor(annotation = Application.class, attribute = "alias")
    String alias() default "application";

    /**
     * Allow the user to define the application name. This will default to
     * empty string, which will then use the name of the application class
     */
    @AliasFor(annotation = Application.class, attribute = "applicationName")
    String applicationName() default "";

    @AliasFor(annotation = Application.class, attribute = "copyrightStatement" )
    String copyrightStatement() default "";

    /**
     * Define the look and feel for the application. Defaults to "Metal".
     * On MacOS, highly recommend FlatMac look and feel
     */
    @AliasFor(annotation = Application.class, attribute = "lookAndFeel")
    Class<? extends LookAndFeelFactory> lookAndFeel() default DefaultLookAndFeelFactory.class;

    @AliasFor(annotation = Model.class, attribute="extensions")
    String[] extensions() default {};


    /**
     * Used to define a view factory for this object. If not
     * defined, then the default is the {@link ReflectiveViewFactory}. <br>
     * Note this is different for all-in-one applications, which
     * prefer the Reflective view factory, rather than the
     * {@link SVWFResourceViewFactory} used by other applications.
     */
    @AliasFor(annotation = Model.class, attribute = "viewFactory")
    Class<?> viewFactory() default ReflectiveViewFactory.class;
}
