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

import com.hablutzel.spwing.util.FlexpressionParser;
import lombok.ToString;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;

@ToString
public class FlexpressionAccessor extends Accessor {

    private final Object flexpressionValue;
    @ToString.Exclude
    private final ConversionService conversionService;

    public FlexpressionAccessor(final String flexpression,
                                final ApplicationContext applicationContext,
                                final ConversionService conversionService) {
        FlexpressionParser flexpressionParser = new FlexpressionParser(applicationContext);
        this.flexpressionValue = flexpressionParser.evaluate(flexpression);
        this.conversionService = conversionService;
    }

    public boolean isWriteable() {
        return false;
    }

    public void set(Object value) {
        throw new UnsupportedOperationException();
    }

    public Object get(Class<?> targetClass) {
        return conversionService.convert(flexpressionValue, targetClass);
    }

    @Override
    public boolean canSupply(Class<?> targetClass) {
        return conversionService.canConvert(flexpressionValue.getClass(), targetClass);
    }
}
