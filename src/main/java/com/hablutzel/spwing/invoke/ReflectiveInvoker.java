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


import com.hablutzel.spwing.util.ResultHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.SynthesizingMethodParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


/**
 * Invoker is used to invoke methods or constructors, along with flexible
 * argument substitution, via reflection. This is the main method dispatch
 * functionality for the framework.<br>
 * The class works by first attempting to build the name of
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
 *     <li>A {@link Value} expression associated with the parameter, which will be evaluated (if possible)</li>
 *     <li>A bean that matches the parameter name and type</li>
 *     <li>A bean that matches the parameter type and is either
 *         <ul>
 *             <li>The only such bean</li>
 *             <li>The only such bean marked with the {@link Primary} annotation</li>
 *         </ul>
 *     </li>
 *     <li>A specific supplier for the class (limited for specific framework classes)</li>
 * </ul>
 * If the argument cannot be found, a null will be used if the parameter is optional (see {@link MethodParameter#isOptional()})
 * and the invocation will fail if not.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public class ReflectiveInvoker extends Invoker {

    @Getter
    private final Executable executable;

    @Getter
    private final Object target;


    public ReflectiveInvoker(ApplicationContext context, Object target, Executable executable) {
        super(context);
        this.executable = executable;
        this.target = target;
        executable.setAccessible(true);
    }


    /**
     * Find and invoke a method on the target class. The caller
     * supplies a set of additional parameter suppliers that can be
     * used to provide specialized class instances to the caller.
     * The return type is expected to be void. This is equivalent
     * to calling {@link #invoke(ApplicationContext, Object, String, Function[])}
     * with Void.TYPE
     * @param applicationContext The application context
     * @param target The target object
     * @param methodName The method name
     * @param parameterSuppliers A map of class names to parameter suppliers
     */
    @SafeVarargs
    public static void invoke(final ApplicationContext applicationContext,
                              final Object target,
                              final String methodName,
                              final Function<ParameterDescription,ParameterResolution>... parameterSuppliers) {
        invoke(applicationContext, target, methodName, Void.TYPE, null, parameterSuppliers );
    }


    /**
     * Invoke a method by name on a target. This routine will find the target
     * method (directly declared or inherited) and attempt to invoke the first
     * such method encountered that returns a type compatible with the specified
     * result type. The caller may include a map of classes to parameter suppliers
     * that will augment the normal invoker dynamic parameter resolution by
     * specifying the instance to use for any parameters of the specified classes.
     * @param applicationContext The application context
     * @param target The target object
     * @param methodName The method name
     * @param clazz The expected return type
     * @param defaultValue The default value of the method
     * @param parameterSuppliers A map of classes to parameter suppliers
     * @return The resulting value
     * @param <T> The result type
     */
    @SafeVarargs
    public static <T> T invoke(final ApplicationContext applicationContext,
                               final Object target,
                               final String methodName,
                               final Class<T> clazz,
                               final T defaultValue,
                               final Function<ParameterDescription,ParameterResolution>... parameterSuppliers) {

        // Create a holder for our result with the default value. This value will be
        // returned if no matching method is found.
        ResultHolder<T> resultHolder = new ResultHolder<>(defaultValue);

        // Get the target class, and the associated methods
        Class<?> targetClass = target.getClass();
        Method[] methods = targetClass.getMethods();

        // Look over the methods. Find the first where the name matches
        // and the return type is compatible
        Arrays.stream(methods)
                .filter( method -> method.getName().equals(methodName) &&
                        clazz.isAssignableFrom(method.getReturnType()))
                .findFirst()
                .ifPresent( method -> {

                    // Create a new reflective invoker for that method
                    ReflectiveInvoker reflectiveInvoker = new ReflectiveInvoker(applicationContext, target, method );

                    // Add any overriding parameter suppliers
                    Arrays.stream(parameterSuppliers).forEach(reflectiveInvoker::registerParameterResolver);

                    // Invoke with the allowable results
                    AllowedResult<T> allowedResult = new AllowedResult<>(clazz, resultHolder::set);
                    reflectiveInvoker.invoke(allowedResult);
                });

        // Return the result.
        return resultHolder.get();
    }



    private static boolean paramsMatch( Parameter[] parameters, Object... arguments ) {
        if (parameters.length == arguments.length) {
            for (int i = 0; i < parameters.length; ++i) {
                if (!parameters[i].getType().isAssignableFrom(arguments[i].getClass())) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }




    /**
     * Perform the actual method invocation. Determines the arguments to
     * provide to the method, calls the method, and returns the raw object
     * returned.
     *
     * @return The raw object resulting from the call (could be null)
     */
    @Override
    protected Object doInvoke(Object[] dynamicArguments) {
        try {
            if (executable instanceof Method method) {
                return method.invoke(target, dynamicArguments);
            } else if (executable instanceof Constructor<?> constructor ) {
                return constructor.newInstance(dynamicArguments);
            } else {
                return null;
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            log.warn( "Invocation of {} failed.", executable.getName());
            throw new RuntimeException(e);
        }
    }



    protected List<ParameterDescription> getParameterDescriptions() {
        // Get the parameters for the method as SynthesizingMethodParameters and wrap them
        Parameter[] parameterTypes = executable.getParameters();
        return Arrays.stream(parameterTypes)
                .map(SynthesizingMethodParameter::forParameter)
                .map(this::wrapParameter)
                .toList();
    }


    private ParameterDescription wrapParameter(final SynthesizingMethodParameter parameter ) {

        return new ReflectiveParameterDescription(parameter, getContext(), executable);
    }

    @Override
    protected String getMethodName() {
        return executable.getName();
    }


    @Override
    public String toString() {
        return String.format( "invoke[%s.%s]",
                null != target ? target.getClass().getName() : "[static]",
                executable.getName());
    }

}