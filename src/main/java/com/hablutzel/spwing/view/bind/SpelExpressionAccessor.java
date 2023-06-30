/*
 * Copyright © 2023. Hablutzel Consulting, LLC. All rights reserved.
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
 *
 */

package com.hablutzel.spwing.view.bind;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;


@RequiredArgsConstructor
@Slf4j
@ToString
public class SpelExpressionAccessor extends Accessor {

    private final Expression spelExpression;
    private final EvaluationContext evaluationContext;

    @ToString.Exclude
    private final ConversionService conversionService;

    @Override
    public boolean isWriteable() {
        return spelExpression.isWritable(evaluationContext);
    }

    @Override
    public Object get() {
        return spelExpression.getValue(evaluationContext);
    }

    @Override
    public void set(Object value) {
        if (spelExpression.isWritable(evaluationContext)) {
            spelExpression.setValue(evaluationContext, value);
        }
    }

    @Override
    public boolean canSupply(Class<?> targetClass) {
        return conversionService.canConvert(spelExpression.getValueType(evaluationContext), targetClass);
    }
}
