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

package com.hablutzel.spwing.util;


import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FlexpressionParsers provide a flexible, unified expression parsing
 * mechanism for both SPEL and property placeholder expression.<br>
 * SPEl expressions are used to access general Java language elements,
 * including Beans. SPEL expression are denoted by a leading '#'
 * character and are enclosed in curly braces - e.g.
 * <ul>
 *     <li>#{"String value"} : a literal string value</li>
 *     <li>#{&#064;bean} : A reference to a bean named "bean"</li>
 *     <li>#{&#064;bean.foo()} : A call to method foo() of a bean named "bean"</li>
 *     <li>#{T(java.lang.Math).random()} : A static member invocation</li>
 *     <li>#{systemProperties['java.home']} : The system property "java.home'</li>
 *     <li>#{systemEnvironment['HOME']} : The system environment variable HOME</li>
 * </ul>
 * By contrast, placeholder expressions are used to access property values
 * from the application property file. This defaults to "application.property" but
 * can be any property file denoted by an {@link PropertySource} annotation. Placeholder
 * expressions are denoted by a leading '$' character and are, again, enclosed
 * in curly braces. Examples could be:
 * <ul>
 *     <li>${someProperty} : the property 'someProperty'</li>
 *     <li>${foo.bar} : the property 'foo.bar'</li>
 * </ul>
 * In both cases, any leading whitespace between the curly braces is trimmed off.
 * @author Bob Hablutzel
 */
@RequiredArgsConstructor
@Slf4j
public class FlexpressionParser {

    /**
     * The pattern used to recognize an expression
     */
    private static final Pattern EXPRESSION_PARSER = Pattern.compile("\\s*([#$])\\{(.+)}");

    /**
     * The application context
     */
    private final ApplicationContext context;

    /**
     * A default value if the expression cannot be parsed
     * or evaluated. Defaults to null.
     */
    @Setter
    private Object defaultValue = null;


    /**
     * Allows a called to determine if the given string appears to be a
     * valid Flexpression or not
     * @param candidate The candidate string
     * @return TRUE for Flexpressions, false otherwise.
     */
    public static boolean appearsToBeFlexpression(String candidate) {
        return EXPRESSION_PARSER.matcher(candidate).matches();
    }


    /**
     * Evaluate an expression and return the string resulting. If the
     * expression results in a null, an error message will be returned
     * in place of the string.
     * @param expression The expression to evaluate
     * @return The resulting string, or an error message
     */
    public String evaluateAsString(String expression) {
        return evaluateAsString(expression, (e) -> String.format("%s evaluated to null", e) );
    }


    /**
     * Evaluate an expression and return the string resulting. If the
     * expression results in a null, the default function will be used
     * to provide the value.
     * @param expression The expression to evaluate
     * @param defaultValue A {@link Function} to provide the default value for an expression
     * @return The resulting string, or an error message
     */
    public String evaluateAsString(String expression, Function<String, String> defaultValue ) {
        Object rawResult = evaluate(expression);
        return Objects.isNull(rawResult) ? defaultValue.apply(expression) : rawResult.toString();
    }


    /**
     * Evaluate the given expression. This attempts to recognize
     * the expression as either a SPEL expression (denoted by #{})
     * or a placeholder expression (denoted by ${}). The resulting
     * value is either the {@link #defaultValue} if the expression
     * cannot be parsed or evaluated, or the result of the evaluation.
     * @param expression The expression
     * @return Either {@link #defaultValue} or the result of the parse and evaluate
     */
    public Object evaluate(String expression) {

        // Do we recognize this?
        Matcher matcher = EXPRESSION_PARSER.matcher(expression);
        if (matcher.matches()) {

            // We do. Get the actual embedded expression
            String actualExpression = matcher.group(2).trim();

            // Dispatch based on the leading symbol
            return switch (matcher.group(1)) {
                case "#" -> evaluateSpelExpression(actualExpression);
                case "$" -> evaluatePlaceholderExpression(actualExpression);
                default -> throw new IllegalStateException("Unexpected value: " + matcher.group(1));
            };
        } else {

            // Didn't match, return the default value
            log.info("The expression {} could not be evaluated", expression);
            return defaultValue;
        }
    }


    /**
     * Evaluate a SPEL expression.
     *
     * @param expression The expression
     * @return The resulting value, or {@link #defaultValue}
     */
    private Object evaluateSpelExpression(String expression) {
        try {

            // Get the expression parse, bean resolver, and evaluation context.
            SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
            BeanResolver beanResolver = new BeanFactoryResolver(context.getAutowireCapableBeanFactory());
            StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
            evaluationContext.setBeanResolver(beanResolver);

            // In order to handle expressions of the type 'systemProperties[xxx]' or 'systemEnvironment[xxx]'
            // we need to be able to resolve the 'systemProperties' and 'systemEnvironment' keywords. We
            // delegate that to the BuildInPropertiesAccessor.
            evaluationContext.setPropertyAccessors(List.of(
                    new BuiltInPropertiesAccessor()
            ));

            // Get the expression and evaluate it.
            Expression spelExpression = spelExpressionParser.parseExpression(expression);
            return spelExpression.getValue(evaluationContext);
        } catch (IllegalStateException | ParseException | EvaluationException e) {
            log.warn( "SPEL expression evaluation failed for {}. Message {}", expression, e.getMessage());
            return defaultValue;
        }
    }


    /**
     * Evaluate the placeholder expression. This will get properties from any available
     * property file - see {@link PropertySource} for how to add a property file.
     *
     * @param expression The placeholder expression
     * @return The result, or the {@link #defaultValue}
     */
    private Object evaluatePlaceholderExpression(String expression) {
        try {

            // We minimally need a ConfigurableBeanFactory to get the resolveEmbeddedValue method
            if (context.getAutowireCapableBeanFactory() instanceof ConfigurableBeanFactory factory) {

                // Note that in order to evaluate the expression, we have to re-wrap it in the ${} notation,
                // but that we have eliminated any whitespace that is allowed by the FlexpressionParser
                // but not allowed by resolveEmbeddedValue
                return factory.resolveEmbeddedValue(String.format("${%s}", expression));
            } else {
                log.warn( "Unable to use the current context factory for evaluating placeholder {}", expression );
                return defaultValue;
            }
        } catch (IllegalStateException e) {
            log.warn( "Placeholder expression evaluation failed for {}. Message {}", expression, e.getMessage());
            return defaultValue;
        }
    }



    /**
     * BuiltInPropertiesAccessor is used to resolve the special
     * SPEL expression keywords 'systemProperties' and 'systemEnvironment'
     *
     * @author Bob Hablutzel
     */
    private static final class BuiltInPropertiesAccessor implements PropertyAccessor {

        private static final Map<String,TypedValue> BUILT_INS = Map.of(
                "systemProperties", new TypedValue(System.getProperties()),
                "systemEnvironment", new TypedValue(System.getenv())
        );

        @Override
        public Class<?>[] getSpecificTargetClasses() {
            return null;
        }

        @Override
        public boolean canRead(EvaluationContext context, Object target, String name) throws AccessException {
            return BUILT_INS.containsKey(name);
        }

        @Override
        public TypedValue read(EvaluationContext context, Object target, String name) throws AccessException {
            return BUILT_INS.get(name);
        }

        @Override
        public boolean canWrite(EvaluationContext context, Object target, String name) throws AccessException {
            return false;
        }

        @Override
        public void write(EvaluationContext context, Object target, String name, Object newValue) throws AccessException {
        }
    }
}
