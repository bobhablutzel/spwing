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

package com.hablutzel.spwing.invoke;

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.util.FlexpressionParser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


@Slf4j
@RequiredArgsConstructor
public abstract class Invoker {

    /**
     * {@link Invoker} is designed to be as independent of framework (other than
     * Spring) as possible. In order to allow the automatic injection of parameters types
     * from other frameworks, you can register an adapter than has the opportunity to
     * register suppliers for framework types.
     *
     * @see #registerFrameworkAdapter(Function)
     */
    private static Function<ParameterDescription, Object> frameworkAdapter = null;
    /**
     * When invoking the method, if a parameter cannot be resolved it is added
     * to this list. The list must be empty for the invocation to continue.
     */
    protected final List<ParameterDescription> unresolvedParameters = new ArrayList<>();
    @Getter
    private final ApplicationContext context;
    /**
     * A set of "last chance" suppliers for types that are known before
     * the method is called, but are not beans. {@link java.awt.event.ActionEvent}
     * instances are a good example of this case, but it is also used
     * for some framework internal classes
     */
    private final Map<Class<?>, Supplier<?>> parameterSupplierMap = new HashMap<>();
    /**
     * The resolvers we use to attempt to find an argument for a parameter.
     */
    private final List<Function<ParameterDescription, Object>> resolvers = List.of(
            this::beanFromFramework,
            this::beanFromValue,
            this::beanByNameAndType,
            this::beanByType,
            this::beanFromSupplier
    );

    @Setter
    private boolean throwOnInvalidParameter = false;

    public static void registerFrameworkAdapter(Function<ParameterDescription, Object> adapter) {
        Invoker.frameworkAdapter = adapter;
    }

    /**
     * The framework provides some arguments to parameters based only on the
     * class of the argument (without the argument having to be a bean). This
     * is generally for framework supplied entities but can also be used for
     * pass through entities such as an {@link java.awt.event.ActionEvent} instance.<br>
     * If desired, the caller can register new supplies <i>before</i> invoking the
     * method.
     *
     * @param clazz             The class that will be supplied
     * @param parameterSupplier The supplier
     * @see #invoke(AllowedResult[])
     * @see #invoke(Class)
     * @see #invoke(Consumer, AllowedResult[])
     */
    public void registerParameterSupplier(Class<?> clazz, Supplier<Object> parameterSupplier) {
        parameterSupplierMap.put(mapPrimitive(clazz), parameterSupplier);
    }

    /**
     * Register the built-in suppliers - known entities in the framework
     */
    protected void registerBuildInParameterSuppliers() {
        registerParameterSupplier(ApplicationContext.class, () -> context);
    }


    private Object beanFromFramework(ParameterDescription parameterDescription) {
        return Objects.nonNull(frameworkAdapter) ? frameworkAdapter.apply(parameterDescription) : null;
    }

    /**
     * If the method parameter is annotated with a {@link Value} annotation,
     * then resolve the embedded expression and use that as the value. This
     * uses the {@link FlexpressionParser} class to evaluate the expression.
     *
     * @param parameterDescription The parameter
     * @return The evaluated expression, or null if the annotation is missing or the
     * expression is invalid
     * @see FlexpressionParser
     */
    private Object beanFromValue(ParameterDescription parameterDescription) {
        if (parameterDescription.getParameterAnnotation(Value.class) instanceof Value value) {

            // Get the expression text from the value, and attempt to parse it
            String expression = value.value();
            FlexpressionParser flexpressionParser = new FlexpressionParser(context);

            // Once parsed, resolve it and use that value.
            final Object resolvedValue = flexpressionParser.evaluate(expression);
            log.debug("Resolving expression {} to {}", expression, resolvedValue);
            return resolvedValue;
        }
        return null;
    }

    private Class<?> mapPrimitive(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz.equals(Boolean.TYPE)) {
                return Boolean.class;
            } else if (clazz.equals(Byte.TYPE)) {
                return Byte.class;
            } else if (clazz.equals(Character.TYPE)) {
                return Character.class;
            } else if (clazz.equals(Float.TYPE)) {
                return Float.class;
            } else if (clazz.equals(Integer.TYPE)) {
                return Integer.class;
            } else if (clazz.equals(Long.TYPE)) {
                return Long.class;
            } else if (clazz.equals(Short.TYPE)) {
                return Short.class;
            } else if (clazz.equals(Double.TYPE)) {
                return Double.TYPE;
            }
        }
        return clazz;
    }


    /**
     * Routine to get a bean from a supplier, for the types that have
     * special suppliers registered. These are generally for well known
     * {@link Spwing} members (or the UI itself).
     *
     * @param parameterDescription The parameter
     * @return The resulting object (or null)
     */
    private Object beanFromSupplier(ParameterDescription parameterDescription) {
        final Class<?> parameterDescriptionType = parameterDescription.getType();
        Class<?> beanClass = mapPrimitive(parameterDescriptionType);
        return parameterSupplierMap.entrySet().stream()
                .filter(e -> beanClass.isAssignableFrom(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(() -> null)
                .get();
    }

    /**
     * Get a bean from the context by name and type. Returns null of there isn't a
     * match.
     *
     * @param parameterDescription The parameter
     * @return The resulting object (or null)
     */
    private Object beanByNameAndType(ParameterDescription parameterDescription) {
        String parameterName = parameterDescription.getName();
        if (Objects.isNull(parameterName) || parameterName.isBlank()) {
            return null;
        } else {

            // Attempt to get the bean with inferred name from the context. If we can get
            // one (and it's the right type), then we use that bean as the argument
            try {
                return context.getBean(parameterName, parameterDescription.getType());
            } catch (BeansException e) {
                return null;
            }
        }
    }

    /**
     * Get a bean from the context by type. Returns null of there isn't a
     * match. For vararg arguments, it will return all the beans of the
     * given type; for non-vararg argument it will return either the
     * unique bean or the unique bean annotated with {@link Primary} (or null).
     * Note that for vararg arguments the argument list provided might be empty.
     *
     * @param parameterDescription The parameter
     * @return The resulting object (or null)
     */
    private Object beanByType(ParameterDescription parameterDescription) {
        final boolean isVarArgs = parameterDescription.isVarArgs();
        if (!parameterDescription.getType().equals(Object.class)) {
            if (isVarArgs) {

                // Get the underlying component type, and all the beans for that type.
                // Convert the beans into an array of the appropriate type, and return that
                final Class<?> componentType = parameterDescription.getType().getComponentType();
                Map<String, ?> beans = context.getBeansOfType(componentType);
                Object targetArrayType = Array.newInstance(componentType, 0);
                return beans.values().toArray((Object[]) targetArrayType);
            } else {

                // Get the beans for the parameter type. If it's unique, return the unique value;
                // otherwise attempt to get the primary bean
                Map<String, ?> beans = context.getBeansOfType(parameterDescription.getType());
                return switch (beans.size()) {
                    case 0 -> null;
                    case 1 -> beans.values().iterator().next();
                    default -> getPrimary(beans);
                };
            }
        } else {
            return null;
        }
    }

    /**
     * Attempt to a single bean in the map that is marked as {@link Primary}
     *
     * @param beans The map of beans
     * @return The single bean, or null if there isn't one and only one
     */
    private Object getPrimary(Map<String, ?> beans) {

        List<String> primaryNames = beans.keySet().stream()
                .filter(name -> Objects.nonNull(context.findAnnotationOnBean(name, Primary.class)))
                .toList();
        return primaryNames.size() == 1 ? beans.get(primaryNames.get(0)) : null;
    }

    /**
     * Build the argument for the specified parameter. This argument will be
     * guaranteed to either be assignable to the parameter type, or null.<br>
     * The method works by first attempting to build the name of
     * each parameter of the object. The name can be obtained
     * from a number of authorities - in order:
     * <ul>
     *     <li>A {@link Qualifier} annotation on the parameter</li>
     *     <li>A bean of type {@link ParameterNameDiscoverer} that recognizes the target method</li>
     *     <li>A {@link DefaultParameterNameDiscoverer} instance, which in turn uses reflection.</li>
     * </ul>
     * Note that for the {@link DefaultParameterNameDiscoverer} to work, the parameter name has
     * to be available at runtime. This implies compiling with -debug or, more preferably, -parameter
     * so that the parameter names are maintained.<br>
     * Once the parameter name and type are established, the hierarchy for finding an argument is
     * <ul>
     *     <li>A specific supplier for the class (limited for specific framework classes)</li>
     *     <li>A bean that matches the parameter name and type</li>
     *     <li>A bean that matches the parameter name and is assignable to the parameter type</li>
     *     <li>A bean that matches the parameter type and is either
     *         <ul>
     *             <li>The only such bean</li>
     *             <li>Marked with the {@link Primary} annotation</li>
     *         </ul>
     *     </li>
     *     <li>When all else fails, null is retunred.</li>
     * </ul>
     *
     * @param parameterDescription The parameter to build an argument for
     * @return The object that matches the parameter.
     */
    protected Object buildArgumentForParameter(ParameterDescription parameterDescription) {
        final Class<?> parameterType = parameterDescription.getType();
        final Class<?> mappedParameterType = mapPrimitive(parameterType);

        // Find the first resolvable argument that works, or null if one can't be found
        return resolvers.stream()
                .map(resolver -> resolver.apply(parameterDescription))
                .filter(Objects::nonNull)
                .filter(o -> mappedParameterType.isAssignableFrom(mapPrimitive(o.getClass())))
                .findFirst()
                .orElseGet(() -> handleUnresolvedParameter(parameterDescription));
    }


    /**
     * Invoke the method, expecting a specific class to be returned.
     * This method will enforce the correct type of the result, and will
     * throw a {@link RuntimeException} wrapping a {@link ClassCastException}
     * if not.
     *
     * @param clazz The class required as return
     * @param <T>   The class expected for return
     * @return An instance of T, or null
     * @see #invoke(AllowedResult[])
     * @see #invoke(Consumer, AllowedResult[])
     */
    public <T> T invoke(Class<T> clazz) {
        try {

            // Get the dynamically built arguments
            Object[] dynamicArguments = buildDynamicArguments();

            // See if we had missing parameters - ones we could not resolve and which are not optional
            if (unresolvedParameters.isEmpty()) {
                return clazz.cast(doInvoke(dynamicArguments));
            } else {
                log.error("Could not invoke {}; the following parameters were unmapped. Consider making them nullable (@Nullable)", getMethodName());
                unresolvedParameters.stream()
                        .map(p -> String.format("  Parameter # %s (potential name: %s, type: %s)",
                                p.getIndex(), p.getName(), p.getType().getName()))
                        .forEach(log::error);
                if (throwOnInvalidParameter) {
                    throw new RuntimeException("Unmappable parameter(s)");
                }
                return null;
            }
        } catch (ClassCastException e) {
            log.error("Invoked method {} returned an invalid result type, expected {}", this.getMethodName(), clazz.getName());
            throw new RuntimeException(e);
        }
    }


    /**
     * Invoke the method, providing one or more {@link AllowedResult} instances
     * that define how expected return types should be handled. If the return
     * type does not match any of these, it will be ignored.
     *
     * @param potentialResults The {@link AllowedResult} instances describing how to
     *                         handle various different return types
     */
    public void invoke(AllowedResult<?>... potentialResults) {
        invoke(o -> log.debug("Ignoring unexpected result type {}", o.getClass().getName()), potentialResults);
    }


    /**
     * Invoke the method, providing one or more {@link AllowedResult} instances
     * that define how expected return types should be handled. If the return
     * type does not match any of these, the default (Object) consumer will be
     * used. Note that if there is an {@link AllowedResult} taking {@link Object}
     * in the list, it will be used in preference to the default consumer.
     *
     * @param defaultConsumer  The default consumer if no other consumer is found.
     * @param potentialResults The {@link AllowedResult} instances describing how to
     *                         handle various different return types
     */
    public void invoke(Consumer<Object> defaultConsumer, AllowedResult<?>... potentialResults) {
        Object result = invoke(Object.class);
        if (result != null) {
            Arrays.stream(potentialResults)
                    .filter(allowedResult -> allowedResult.resultType.isAssignableFrom(result.getClass()))
                    .findFirst()
                    .ifPresentOrElse(
                            allowedResult -> allowedResult.process(result),
                            () -> defaultConsumer.accept(result));
        }
    }


    /**
     * Perform the actual method invocation. Determines the arguments to
     * provide to the method, calls the method, and returns the raw object
     * returned.
     *
     * @return The raw object resulting from the call (could be null)
     */
    private Object[] buildDynamicArguments() {

        // Register build-in parameter suppliers. The caller might have added
        // other parameter suppliers as well, and that's fine.
        registerBuildInParameterSuppliers();

        // Clear out any unresolved parameters from previous calls.
        this.unresolvedParameters.clear();

        // Get the parameter descriptions
        List<? extends ParameterDescription> parameterDescriptions = this.getParameterDescriptions();

        // Build the argument list
        return parameterDescriptions.stream()
                .map(this::buildArgumentForParameter)
                .toArray();

    }

    protected abstract List<? extends ParameterDescription> getParameterDescriptions();

    protected abstract Object doInvoke(Object[] dynamicArguments);

    protected abstract String getMethodName();

    /**
     * Return the default value for an unresolved parameter (null) and mark
     * the parameter as missing if it is a required parameter.
     *
     * @param parameterDescription The parameter description
     * @return null
     */
    private Object handleUnresolvedParameter(ParameterDescription parameterDescription) {
        // That might be OK, if we have an optional argument. In this case,
        // use null as the result. Otherwise, add this to the list of
        // unresolved parameters so that we know we can't continue forward.
        if (!parameterDescription.isOptional()) {
            unresolvedParameters.add(parameterDescription);
        }
        return null;
    }


    /**
     * Describes an allowable result from invoking this method.
     * The AllowedResult record provides for a flexible model
     * for getting return types; the caller can provide a
     * consumer for every type of class that the called routine
     * is allowed to produce.
     *
     * @param resultType The expected type
     * @param consumer   A consumer for that type
     * @param <T>        The type
     * @author Bob Hablutzel
     */
    public record AllowedResult<T>(Class<T> resultType, Consumer<T> consumer) {
        void process(Object object) {
            consumer.accept(resultType.cast(object));
        }
    }
}