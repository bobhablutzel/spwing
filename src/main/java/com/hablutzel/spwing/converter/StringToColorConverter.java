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

    private final Map<String,Color> colorMap = new HashMap<>();

    public StringToColorConverter() {
        colorMap.put( "IndianRed", new Color(205, 92, 92));
        colorMap.put( "LightCoral", new Color(240, 128, 128));
        colorMap.put( "Salmon", new Color(250, 128, 114));
        colorMap.put( "DarkSalmon", new Color(233, 150, 122));
        colorMap.put( "LightSalmon", new Color(255, 160, 122));
        colorMap.put( "Crimson", new Color(220, 20, 60));
        colorMap.put( "Red", new Color(255, 0, 0));
        colorMap.put( "FireBrick", new Color(178, 34, 34));
        colorMap.put( "DarkRed", new Color(139, 0, 0));
        colorMap.put( "Pink", new Color(255, 192, 203));
        colorMap.put( "LightPink", new Color(255, 182, 193));
        colorMap.put( "HotPink", new Color(255, 105, 180));
        colorMap.put( "DeepPink", new Color(255, 20, 147));
        colorMap.put( "MediumVioletRed", new Color(199, 21, 133));
        colorMap.put( "PaleVioletRed", new Color(219, 112, 147));
        colorMap.put( "Coral", new Color(255, 127, 80));
        colorMap.put( "Tomato", new Color(255, 99, 71));
        colorMap.put( "OrangeRed", new Color(255, 69, 0));
        colorMap.put( "DarkOrange", new Color(255, 140, 0));
        colorMap.put( "Orange", new Color(255, 165, 0));
        colorMap.put( "Gold", new Color(255, 215, 0));
        colorMap.put( "Yellow", new Color(255, 255, 0));
        colorMap.put( "LightYellow", new Color(255, 255, 224));
        colorMap.put( "LemonChiffon", new Color(255, 250, 205));
        colorMap.put( "LightGoldenrodYellow", new Color(250, 250, 210));
        colorMap.put( "PapayaWhip", new Color(255, 239, 213));
        colorMap.put( "Moccasin", new Color(255, 228, 181));
        colorMap.put( "PeachPuff", new Color(255, 218, 185));
        colorMap.put( "PaleGoldenrod", new Color(238, 232, 170));
        colorMap.put( "Khaki", new Color(240, 230, 140));
        colorMap.put( "DarkKhaki", new Color(189, 183, 107));
        colorMap.put( "Lavender", new Color(230, 230, 250));
        colorMap.put( "Thistle", new Color(216, 191, 216));
        colorMap.put( "Plum", new Color(221, 160, 221));
        colorMap.put( "Violet", new Color(238, 130, 238));
        colorMap.put( "Orchid", new Color(218, 112, 214));
        colorMap.put( "Fuchsia", new Color(255, 0, 255));
        colorMap.put( "Magenta", new Color(255, 0, 255));
        colorMap.put( "MediumOrchid", new Color(186, 85, 211));
        colorMap.put( "MediumPurple", new Color(147, 112, 219));
        colorMap.put( "RebeccaPurple", new Color(102, 51, 153));
        colorMap.put( "BlueViolet", new Color(138, 43, 226));
        colorMap.put( "DarkViolet", new Color(148, 0, 211));
        colorMap.put( "DarkOrchid", new Color(153, 50, 204));
        colorMap.put( "DarkMagenta", new Color(139, 0, 139));
        colorMap.put( "Purple", new Color(128, 0, 128));
        colorMap.put( "Indigo", new Color(75, 0, 130));
        colorMap.put( "SlateBlue", new Color(106, 90, 205));
        colorMap.put( "DarkSlateBlue", new Color(72, 61, 139));
        colorMap.put( "GreenYellow", new Color(173, 255, 47));
        colorMap.put( "Chartreuse", new Color(127, 255, 0));
        colorMap.put( "LawnGreen", new Color(124, 252, 0));
        colorMap.put( "Lime", new Color(0, 255, 0));
        colorMap.put( "LimeGreen", new Color(50, 205, 50));
        colorMap.put( "PaleGreen", new Color(152, 251, 152));
        colorMap.put( "LightGreen", new Color(144, 238, 144));
        colorMap.put( "MediumSpringGreen", new Color(0, 250, 154));
        colorMap.put( "SpringGreen", new Color(0, 255, 127));
        colorMap.put( "MediumSeaGreen", new Color(60, 179, 113));
        colorMap.put( "SeaGreen", new Color(46, 139, 87));
        colorMap.put( "ForestGreen", new Color(34, 139, 34));
        colorMap.put( "Green", new Color(0, 128, 0));
        colorMap.put( "DarkGreen", new Color(0, 100, 0));
        colorMap.put( "YellowGreen", new Color(154, 205, 50));
        colorMap.put( "OliveDrab", new Color(107, 142, 35));
        colorMap.put( "Olive", new Color(128, 128, 0));
        colorMap.put( "DarkOliveGreen", new Color(85, 107, 47));
        colorMap.put( "MediumAquamarine", new Color(102, 205, 170));
        colorMap.put( "DarkSeaGreen", new Color(143, 188, 139));
        colorMap.put( "LightSeaGreen", new Color(32, 178, 170));
        colorMap.put( "DarkCyan", new Color(0, 139, 139));
        colorMap.put( "Teal", new Color(0, 128, 128));
        colorMap.put( "Aqua", new Color(0, 255, 255));
        colorMap.put( "Cyan", new Color(0, 255, 255));
        colorMap.put( "LightCyan", new Color(224, 255, 255));
        colorMap.put( "PaleTurquoise", new Color(175, 238, 238));
        colorMap.put( "Aquamarine", new Color(127, 255, 212));
        colorMap.put( "Turquoise", new Color(64, 224, 208));
        colorMap.put( "MediumTurquoise", new Color(72, 209, 204));
        colorMap.put( "DarkTurquoise", new Color(0, 206, 209));
        colorMap.put( "CadetBlue", new Color(95, 158, 160));
        colorMap.put( "SteelBlue", new Color(70, 130, 180));
        colorMap.put( "LightSteelBlue", new Color(176, 196, 222));
        colorMap.put( "PowderBlue", new Color(176, 224, 230));
        colorMap.put( "LightBlue", new Color(173, 216, 230));
        colorMap.put( "SkyBlue", new Color(135, 206, 235));
        colorMap.put( "LightSkyBlue", new Color(135, 206, 250));
        colorMap.put( "DeepSkyBlue", new Color(0, 191, 255));
        colorMap.put( "DodgerBlue", new Color(30, 144, 255));
        colorMap.put( "CornflowerBlue", new Color(100, 149, 237));
        colorMap.put( "MediumSlateBlue", new Color(123, 104, 238));
        colorMap.put( "RoyalBlue", new Color(65, 105, 225));
        colorMap.put( "Blue", new Color(0, 0, 255));
        colorMap.put( "MediumBlue", new Color(0, 0, 205));
        colorMap.put( "DarkBlue", new Color(0, 0, 139));
        colorMap.put( "Navy", new Color(0, 0, 128));
        colorMap.put( "MidnightBlue", new Color(25, 25, 112));
        colorMap.put( "Cornsilk", new Color(255, 248, 220));
        colorMap.put( "BlanchedAlmond", new Color(255, 235, 205));
        colorMap.put( "Bisque", new Color(255, 228, 196));
        colorMap.put( "NavajoWhite", new Color(255, 222, 173));
        colorMap.put( "Wheat", new Color(245, 222, 179));
        colorMap.put( "BurlyWood", new Color(222, 184, 135));
        colorMap.put( "Tan", new Color(210, 180, 140));
        colorMap.put( "RosyBrown", new Color(188, 143, 143));
        colorMap.put( "SandyBrown", new Color(244, 164, 96));
        colorMap.put( "Goldenrod", new Color(218, 165, 32));
        colorMap.put( "DarkGoldenrod", new Color(184, 134, 11));
        colorMap.put( "Peru", new Color(205, 133, 63));
        colorMap.put( "Chocolate", new Color(210, 105, 30));
        colorMap.put( "SaddleBrown", new Color(139, 69, 19));
        colorMap.put( "Sienna", new Color(160, 82, 45));
        colorMap.put( "Brown", new Color(165, 42, 42));
        colorMap.put( "Maroon", new Color(128, 0, 0));
        colorMap.put( "White", new Color(255, 255, 255));
        colorMap.put( "Snow", new Color(255, 250, 250));
        colorMap.put( "HoneyDew", new Color(240, 255, 240));
        colorMap.put( "MintCream", new Color(245, 255, 250));
        colorMap.put( "Azure", new Color(240, 255, 255));
        colorMap.put( "AliceBlue", new Color(240, 248, 255));
        colorMap.put( "GhostWhite", new Color(248, 248, 255));
        colorMap.put( "WhiteSmoke", new Color(245, 245, 245));
        colorMap.put( "SeaShell", new Color(255, 245, 238));
        colorMap.put( "Beige", new Color(245, 245, 220));
        colorMap.put( "OldLace", new Color(253, 245, 230));
        colorMap.put( "FloralWhite", new Color(255, 250, 240));
        colorMap.put( "Ivory", new Color(255, 255, 240));
        colorMap.put( "AntiqueWhite", new Color(250, 235, 215));
        colorMap.put( "Linen", new Color(250, 240, 230));
        colorMap.put( "LavenderBlush", new Color(255, 240, 245));
        colorMap.put( "MistyRose", new Color(255, 228, 225));
        colorMap.put( "Gainsboro", new Color(220, 220, 220));
        colorMap.put( "LightGray", new Color(211, 211, 211));
        colorMap.put( "Silver", new Color(192, 192, 192));
        colorMap.put( "DarkGray", new Color(169, 169, 169));
        colorMap.put( "Gray", new Color(128, 128, 128));
        colorMap.put( "DimGray", new Color(105, 105, 105));
        colorMap.put( "LightSlateGray", new Color(119, 136, 153));
        colorMap.put( "SlateGray", new Color(112, 128, 144));
        colorMap.put( "DarkSlateGray", new Color(47, 79, 79));
        colorMap.put( "Black", new Color(0, 0, 0));
    }

    @Override
    @Nullable
    public Color convert(@NonNull final String colorName) {
        return colorMap.getOrDefault(colorName, Color.black);
    }
}
