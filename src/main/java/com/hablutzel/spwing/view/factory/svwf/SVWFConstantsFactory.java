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

package com.hablutzel.spwing.view.factory.svwf;

import com.hablutzel.spwing.constants.ContextualConstant;
import org.springframework.stereotype.Service;


@Service
public class SVWFConstantsFactory implements SVWFComponentFactory {

    @Override
    public void addComponents(SVWFParseContext parseContext) {
        parseContext.addComponent("$CENTER", ContextualConstant.CENTER);
        parseContext.addComponent("$TOP", ContextualConstant.TOP);
        parseContext.addComponent("$LEFT", ContextualConstant.LEFT);
        parseContext.addComponent("$BOTTOM", ContextualConstant.BOTTOM);
        parseContext.addComponent("$RIGHT", ContextualConstant.RIGHT);
        parseContext.addComponent("$NORTH", ContextualConstant.NORTH);
        parseContext.addComponent("$NORTH_EAST", ContextualConstant.NORTH_EAST);
        parseContext.addComponent("$EAST", ContextualConstant.EAST);
        parseContext.addComponent("$SOUTH_EAST", ContextualConstant.SOUTH_EAST);
        parseContext.addComponent("$SOUTH", ContextualConstant.SOUTH);
        parseContext.addComponent("$SOUTH_WEST", ContextualConstant.SOUTH_WEST);
        parseContext.addComponent("$WEST", ContextualConstant.WEST);
        parseContext.addComponent("$NORTH_WEST", ContextualConstant.NORTH_WEST);
        parseContext.addComponent("$HORIZONTAL", ContextualConstant.HORIZONTAL);
        parseContext.addComponent("$VERTICAL", ContextualConstant.VERTICAL);
        parseContext.addComponent("$LEADING", ContextualConstant.LEADING);
        parseContext.addComponent("$TRAILING", ContextualConstant.TRAILING);
        parseContext.addComponent("$NEXT", ContextualConstant.NEXT);
        parseContext.addComponent("$PREVIOUS", ContextualConstant.PREVIOUS);
        parseContext.addComponent("$LEFT_ALIGNMENT", ContextualConstant.LEFT_ALIGNMENT);
        parseContext.addComponent("$RIGHT_ALIGNMENT", ContextualConstant.RIGHT_ALIGNMENT);
        parseContext.addComponent("$CENTER_ALIGNMENT", ContextualConstant.CENTER_ALIGNMENT);
        parseContext.addComponent("$BOTTOM_ALIGNMENT", ContextualConstant.BOTTOM_ALIGNMENT);
        parseContext.addComponent("$TOP_ALIGNMENT", ContextualConstant.TOP_ALIGNMENT);
        parseContext.addComponent("$RELATIVE", ContextualConstant.RELATIVE);
        parseContext.addComponent("$REMAINDER", ContextualConstant.REMAINDER);
        parseContext.addComponent("$NONE", ContextualConstant.NONE);
        parseContext.addComponent("$BOTH", ContextualConstant.BOTH);
        parseContext.addComponent("$PAGE_START", ContextualConstant.PAGE_START);
        parseContext.addComponent("$PAGE_END", ContextualConstant.PAGE_END);
        parseContext.addComponent("$LINE_START", ContextualConstant.LINE_START);
        parseContext.addComponent("$LINE_END", ContextualConstant.LINE_END);
        parseContext.addComponent("$FIRST_LINE_START", ContextualConstant.FIRST_LINE_START);
        parseContext.addComponent("$FIRST_LINE_END", ContextualConstant.FIRST_LINE_END);
        parseContext.addComponent("$LAST_LINE_START", ContextualConstant.LAST_LINE_START);
        parseContext.addComponent("$LAST_LINE_END", ContextualConstant.LAST_LINE_END);
        parseContext.addComponent("$BASELINE", ContextualConstant.BASELINE);
        parseContext.addComponent("$BASELINE_LEADING", ContextualConstant.BASELINE_LEADING);
        parseContext.addComponent("$BASELINE_TRAILING", ContextualConstant.BASELINE_TRAILING);
        parseContext.addComponent("$ABOVE_BASELINE", ContextualConstant.ABOVE_BASELINE);
        parseContext.addComponent("$ABOVE_BASELINE_LEADING", ContextualConstant.ABOVE_BASELINE_LEADING);
        parseContext.addComponent("$ABOVE_BASELINE_TRAILING", ContextualConstant.ABOVE_BASELINE_TRAILING);
        parseContext.addComponent("$BELOW_BASELINE", ContextualConstant.BELOW_BASELINE);
        parseContext.addComponent("$BELOW_BASELINE_LEADING", ContextualConstant.BELOW_BASELINE_LEADING);
        parseContext.addComponent("$BELOW_BASELINE_TRAILING", ContextualConstant.BELOW_BASELINE_TRAILING);
    }
}
