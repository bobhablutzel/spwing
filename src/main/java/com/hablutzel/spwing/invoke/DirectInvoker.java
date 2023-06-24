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

@Slf4j
public abstract class DirectInvoker extends Invoker {

    @Getter
    private final String methodName;
    private final List<DirectParameterDescription> parameterDescriptions;


    public DirectInvoker(ApplicationContext applicationContext, final String methodName, List<DirectParameterDescription> parameterDescriptions ) {
        super(applicationContext);
        this.methodName = methodName;
        this.parameterDescriptions = parameterDescriptions;
    }



    @Override
    protected List<? extends ParameterDescription> getParameterDescriptions() {
        return parameterDescriptions;
    }
}
