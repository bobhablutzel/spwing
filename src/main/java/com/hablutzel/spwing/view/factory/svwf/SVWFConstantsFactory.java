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

import com.hablutzel.spwing.util.Colors;
import org.springframework.stereotype.Service;

import javax.swing.*;


@Service
public class SVWFConstantsFactory implements SVWFComponentFactory {

    @Override
    public void addComponents(SVWFParseContext parseContext) {
        parseContext.addComponent("$CENTER", SwingConstants.CENTER);
        parseContext.addComponent("$TOP", SwingConstants.TOP);
        parseContext.addComponent("$LEFT", SwingConstants.LEFT);
        parseContext.addComponent("$BOTTOM", SwingConstants.BOTTOM);
        parseContext.addComponent("$RIGHT", SwingConstants.RIGHT);
        parseContext.addComponent("$NORTH", SwingConstants.NORTH);
        parseContext.addComponent("$NORTH_EAST", SwingConstants.NORTH_EAST);
        parseContext.addComponent("$EAST", SwingConstants.EAST);
        parseContext.addComponent("$SOUTH_EAST", SwingConstants.SOUTH_EAST);
        parseContext.addComponent("$SOUTH", SwingConstants.SOUTH);
        parseContext.addComponent("$SOUTH_WEST", SwingConstants.SOUTH_WEST);
        parseContext.addComponent("$WEST", SwingConstants.WEST);
        parseContext.addComponent("$NORTH_WEST", SwingConstants.NORTH_WEST);
        parseContext.addComponent("$HORIZONTAL", SwingConstants.HORIZONTAL);
        parseContext.addComponent("$VERTICAL", SwingConstants.VERTICAL);
        parseContext.addComponent("$LEADING", SwingConstants.LEADING);
        parseContext.addComponent("$TRAILING", SwingConstants.TRAILING);
        parseContext.addComponent("$NEXT", SwingConstants.NEXT);
        parseContext.addComponent("$PREVIOUS", SwingConstants.PREVIOUS);
    }
}
