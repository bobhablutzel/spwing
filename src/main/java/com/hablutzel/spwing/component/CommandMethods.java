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

package com.hablutzel.spwing.component;

import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.util.ResultHolder;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@ToString
public class CommandMethods {

    @Setter
    private Invoker handler;

    @Setter
    private Invoker enabler;



    public void doEnable(Consumer<Boolean> setEnabled ) {
        if (Objects.isNull(enabler)) {
            setEnabled.accept(Objects.nonNull(handler));
        } else {
            setEnabled.accept(enabler.invoke(Boolean.class));
        }
    }


    /**
     * Fire the command in question.
     * @param clazz The expected result type
     * @param defaultValue The default value to return
     * @param injectedValues Values that can be injected into the invocation context
     */
    public <T> T fireCommandWithResult(Class<T> clazz, T defaultValue, Object... injectedValues) {

        ResultHolder<T> result = new ResultHolder<>(defaultValue);

        Invoker.AllowedResult<?> [] allowedResults = {
                new Invoker.AllowedResult<>(clazz, result::set)
        };

        // Allow the injected values to be used as a parameter to handler methods (might be none)
        Arrays.stream(injectedValues)
                .filter(Objects::nonNull)
                .forEach( o ->  handler.registerParameterSupplier(o.getClass(), () -> o));

        // Invoke the method
        handler.invoke( allowedResults );

        return result.get();
    }
}
