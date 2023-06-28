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

package com.hablutzel.spwing.converter;

import com.hablutzel.spwing.util.Colors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;


/**
 * Converts a string into a color, using the HTML standard
 * color definitions.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public class StringToColorConverter implements Converter<String, Color> {

    @Override
    @Nullable
    public Color convert(@NonNull final String colorName) {
        return Colors.htmlColors.getOrDefault(colorName, Color.black);
    }
}
