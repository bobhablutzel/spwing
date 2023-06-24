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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class SafeConsumer<T> implements Consumer<T> {
    private final Consumer<T> consumer;
    private final Supplier<T> supplier;

    @Override
    public void accept(T t) {
        if (Objects.isNull(t)) {
            consumer.accept(t);
        } else {
            T currentValue = supplier.get();
            if (!t.equals(currentValue)) {
                consumer.accept(t);
            }
        }
    }
}
