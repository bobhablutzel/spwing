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
    public void addComponents(final SVWFListener svwfListener) {
        svwfListener.addComponent("$CENTER", ContextualConstant.CENTER);
        svwfListener.addComponent("$TOP", ContextualConstant.TOP);
        svwfListener.addComponent("$LEFT", ContextualConstant.LEFT);
        svwfListener.addComponent("$BOTTOM", ContextualConstant.BOTTOM);
        svwfListener.addComponent("$RIGHT", ContextualConstant.RIGHT);
        svwfListener.addComponent("$NORTH", ContextualConstant.NORTH);
        svwfListener.addComponent("$NORTH_EAST", ContextualConstant.NORTH_EAST);
        svwfListener.addComponent("$EAST", ContextualConstant.EAST);
        svwfListener.addComponent("$SOUTH_EAST", ContextualConstant.SOUTH_EAST);
        svwfListener.addComponent("$SOUTH", ContextualConstant.SOUTH);
        svwfListener.addComponent("$SOUTH_WEST", ContextualConstant.SOUTH_WEST);
        svwfListener.addComponent("$WEST", ContextualConstant.WEST);
        svwfListener.addComponent("$NORTH_WEST", ContextualConstant.NORTH_WEST);
        svwfListener.addComponent("$HORIZONTAL", ContextualConstant.HORIZONTAL);
        svwfListener.addComponent("$VERTICAL", ContextualConstant.VERTICAL);
        svwfListener.addComponent("$LEADING", ContextualConstant.LEADING);
        svwfListener.addComponent("$TRAILING", ContextualConstant.TRAILING);
        svwfListener.addComponent("$NEXT", ContextualConstant.NEXT);
        svwfListener.addComponent("$PREVIOUS", ContextualConstant.PREVIOUS);
        svwfListener.addComponent("$LEFT_ALIGNMENT", ContextualConstant.LEFT_ALIGNMENT);
        svwfListener.addComponent("$RIGHT_ALIGNMENT", ContextualConstant.RIGHT_ALIGNMENT);
        svwfListener.addComponent("$CENTER_ALIGNMENT", ContextualConstant.CENTER_ALIGNMENT);
        svwfListener.addComponent("$BOTTOM_ALIGNMENT", ContextualConstant.BOTTOM_ALIGNMENT);
        svwfListener.addComponent("$TOP_ALIGNMENT", ContextualConstant.TOP_ALIGNMENT);
        svwfListener.addComponent("$RELATIVE", ContextualConstant.RELATIVE);
        svwfListener.addComponent("$REMAINDER", ContextualConstant.REMAINDER);
        svwfListener.addComponent("$NONE", ContextualConstant.NONE);
        svwfListener.addComponent("$BOTH", ContextualConstant.BOTH);
        svwfListener.addComponent("$PAGE_START", ContextualConstant.PAGE_START);
        svwfListener.addComponent("$PAGE_END", ContextualConstant.PAGE_END);
        svwfListener.addComponent("$LINE_START", ContextualConstant.LINE_START);
        svwfListener.addComponent("$LINE_END", ContextualConstant.LINE_END);
        svwfListener.addComponent("$FIRST_LINE_START", ContextualConstant.FIRST_LINE_START);
        svwfListener.addComponent("$FIRST_LINE_END", ContextualConstant.FIRST_LINE_END);
        svwfListener.addComponent("$LAST_LINE_START", ContextualConstant.LAST_LINE_START);
        svwfListener.addComponent("$LAST_LINE_END", ContextualConstant.LAST_LINE_END);
        svwfListener.addComponent("$BASELINE", ContextualConstant.BASELINE);
        svwfListener.addComponent("$BASELINE_LEADING", ContextualConstant.BASELINE_LEADING);
        svwfListener.addComponent("$BASELINE_TRAILING", ContextualConstant.BASELINE_TRAILING);
        svwfListener.addComponent("$ABOVE_BASELINE", ContextualConstant.ABOVE_BASELINE);
        svwfListener.addComponent("$ABOVE_BASELINE_LEADING", ContextualConstant.ABOVE_BASELINE_LEADING);
        svwfListener.addComponent("$ABOVE_BASELINE_TRAILING", ContextualConstant.ABOVE_BASELINE_TRAILING);
        svwfListener.addComponent("$BELOW_BASELINE", ContextualConstant.BELOW_BASELINE);
        svwfListener.addComponent("$BELOW_BASELINE_LEADING", ContextualConstant.BELOW_BASELINE_LEADING);
        svwfListener.addComponent("$BELOW_BASELINE_TRAILING", ContextualConstant.BELOW_BASELINE_TRAILING);
    }
}
