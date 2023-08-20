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


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;


/**
 * DirectInvoker is used in cases where the actual target method for
 * an invoker can be known at compile time. In this case, the code
 * can participate in the {@link Invoker} functionality while not
 * incurring the overhead of reflective calls. Subclasses will have
 * to override {@link Invoker#doInvoke(Object[])} in order to call
 * the right method.
 *
 * @author Bob Hablutzel
 */
@Getter
@Slf4j
public abstract class DirectInvoker extends Invoker {

    /**
     * The method name. Needed to meet the {@link Invoker} contract
     */
    private final String methodName;

    /**
     * The list of parameter descriptions. Needed to meet the {@link Invoker} contract
     */
    private final List<DirectParameterDescription> parameterDescriptions;


    /**
     * Constructor
     * @param applicationContext The {@link ApplicationContext} instance
     * @param methodName The method name
     * @param parameterDescriptions The parameters
     */
    public DirectInvoker(final ApplicationContext applicationContext,
                         final String methodName,
                         final List<DirectParameterDescription> parameterDescriptions ) {
        super(applicationContext);
        this.methodName = methodName;
        this.parameterDescriptions = parameterDescriptions;
    }


    /**
     * Getter for the parameter list. Has to be directly coded
     * to accommodate the generic type change
     * @return The list of parameter descriptors
     */
    @Override
    protected List<? extends ParameterDescription> getParameterDescriptions() {
        return parameterDescriptions;
    }
}
