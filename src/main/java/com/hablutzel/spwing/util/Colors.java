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

package com.hablutzel.spwing.util;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Colors {

    public static Map<String, Color> htmlColors;

    static {
        Map<String,Color> htmlColors = new HashMap<>();
        htmlColors.put( "IndianRed", new Color(205, 92, 92));
        htmlColors.put( "LightCoral", new Color(240, 128, 128));
        htmlColors.put( "Salmon", new Color(250, 128, 114));
        htmlColors.put( "DarkSalmon", new Color(233, 150, 122));
        htmlColors.put( "LightSalmon", new Color(255, 160, 122));
        htmlColors.put( "Crimson", new Color(220, 20, 60));
        htmlColors.put( "Red", new Color(255, 0, 0));
        htmlColors.put( "FireBrick", new Color(178, 34, 34));
        htmlColors.put( "DarkRed", new Color(139, 0, 0));
        htmlColors.put( "Pink", new Color(255, 192, 203));
        htmlColors.put( "LightPink", new Color(255, 182, 193));
        htmlColors.put( "HotPink", new Color(255, 105, 180));
        htmlColors.put( "DeepPink", new Color(255, 20, 147));
        htmlColors.put( "MediumVioletRed", new Color(199, 21, 133));
        htmlColors.put( "PaleVioletRed", new Color(219, 112, 147));
        htmlColors.put( "Coral", new Color(255, 127, 80));
        htmlColors.put( "Tomato", new Color(255, 99, 71));
        htmlColors.put( "OrangeRed", new Color(255, 69, 0));
        htmlColors.put( "DarkOrange", new Color(255, 140, 0));
        htmlColors.put( "Orange", new Color(255, 165, 0));
        htmlColors.put( "Gold", new Color(255, 215, 0));
        htmlColors.put( "Yellow", new Color(255, 255, 0));
        htmlColors.put( "LightYellow", new Color(255, 255, 224));
        htmlColors.put( "LemonChiffon", new Color(255, 250, 205));
        htmlColors.put( "LightGoldenrodYellow", new Color(250, 250, 210));
        htmlColors.put( "PapayaWhip", new Color(255, 239, 213));
        htmlColors.put( "Moccasin", new Color(255, 228, 181));
        htmlColors.put( "PeachPuff", new Color(255, 218, 185));
        htmlColors.put( "PaleGoldenrod", new Color(238, 232, 170));
        htmlColors.put( "Khaki", new Color(240, 230, 140));
        htmlColors.put( "DarkKhaki", new Color(189, 183, 107));
        htmlColors.put( "Lavender", new Color(230, 230, 250));
        htmlColors.put( "Thistle", new Color(216, 191, 216));
        htmlColors.put( "Plum", new Color(221, 160, 221));
        htmlColors.put( "Violet", new Color(238, 130, 238));
        htmlColors.put( "Orchid", new Color(218, 112, 214));
        htmlColors.put( "Fuchsia", new Color(255, 0, 255));
        htmlColors.put( "Magenta", new Color(255, 0, 255));
        htmlColors.put( "MediumOrchid", new Color(186, 85, 211));
        htmlColors.put( "MediumPurple", new Color(147, 112, 219));
        htmlColors.put( "RebeccaPurple", new Color(102, 51, 153));
        htmlColors.put( "BlueViolet", new Color(138, 43, 226));
        htmlColors.put( "DarkViolet", new Color(148, 0, 211));
        htmlColors.put( "DarkOrchid", new Color(153, 50, 204));
        htmlColors.put( "DarkMagenta", new Color(139, 0, 139));
        htmlColors.put( "Purple", new Color(128, 0, 128));
        htmlColors.put( "Indigo", new Color(75, 0, 130));
        htmlColors.put( "SlateBlue", new Color(106, 90, 205));
        htmlColors.put( "DarkSlateBlue", new Color(72, 61, 139));
        htmlColors.put( "GreenYellow", new Color(173, 255, 47));
        htmlColors.put( "Chartreuse", new Color(127, 255, 0));
        htmlColors.put( "LawnGreen", new Color(124, 252, 0));
        htmlColors.put( "Lime", new Color(0, 255, 0));
        htmlColors.put( "LimeGreen", new Color(50, 205, 50));
        htmlColors.put( "PaleGreen", new Color(152, 251, 152));
        htmlColors.put( "LightGreen", new Color(144, 238, 144));
        htmlColors.put( "MediumSpringGreen", new Color(0, 250, 154));
        htmlColors.put( "SpringGreen", new Color(0, 255, 127));
        htmlColors.put( "MediumSeaGreen", new Color(60, 179, 113));
        htmlColors.put( "SeaGreen", new Color(46, 139, 87));
        htmlColors.put( "ForestGreen", new Color(34, 139, 34));
        htmlColors.put( "Green", new Color(0, 128, 0));
        htmlColors.put( "DarkGreen", new Color(0, 100, 0));
        htmlColors.put( "YellowGreen", new Color(154, 205, 50));
        htmlColors.put( "OliveDrab", new Color(107, 142, 35));
        htmlColors.put( "Olive", new Color(128, 128, 0));
        htmlColors.put( "DarkOliveGreen", new Color(85, 107, 47));
        htmlColors.put( "MediumAquamarine", new Color(102, 205, 170));
        htmlColors.put( "DarkSeaGreen", new Color(143, 188, 139));
        htmlColors.put( "LightSeaGreen", new Color(32, 178, 170));
        htmlColors.put( "DarkCyan", new Color(0, 139, 139));
        htmlColors.put( "Teal", new Color(0, 128, 128));
        htmlColors.put( "Aqua", new Color(0, 255, 255));
        htmlColors.put( "Cyan", new Color(0, 255, 255));
        htmlColors.put( "LightCyan", new Color(224, 255, 255));
        htmlColors.put( "PaleTurquoise", new Color(175, 238, 238));
        htmlColors.put( "Aquamarine", new Color(127, 255, 212));
        htmlColors.put( "Turquoise", new Color(64, 224, 208));
        htmlColors.put( "MediumTurquoise", new Color(72, 209, 204));
        htmlColors.put( "DarkTurquoise", new Color(0, 206, 209));
        htmlColors.put( "CadetBlue", new Color(95, 158, 160));
        htmlColors.put( "SteelBlue", new Color(70, 130, 180));
        htmlColors.put( "LightSteelBlue", new Color(176, 196, 222));
        htmlColors.put( "PowderBlue", new Color(176, 224, 230));
        htmlColors.put( "LightBlue", new Color(173, 216, 230));
        htmlColors.put( "SkyBlue", new Color(135, 206, 235));
        htmlColors.put( "LightSkyBlue", new Color(135, 206, 250));
        htmlColors.put( "DeepSkyBlue", new Color(0, 191, 255));
        htmlColors.put( "DodgerBlue", new Color(30, 144, 255));
        htmlColors.put( "CornflowerBlue", new Color(100, 149, 237));
        htmlColors.put( "MediumSlateBlue", new Color(123, 104, 238));
        htmlColors.put( "RoyalBlue", new Color(65, 105, 225));
        htmlColors.put( "Blue", new Color(0, 0, 255));
        htmlColors.put( "MediumBlue", new Color(0, 0, 205));
        htmlColors.put( "DarkBlue", new Color(0, 0, 139));
        htmlColors.put( "Navy", new Color(0, 0, 128));
        htmlColors.put( "MidnightBlue", new Color(25, 25, 112));
        htmlColors.put( "Cornsilk", new Color(255, 248, 220));
        htmlColors.put( "BlanchedAlmond", new Color(255, 235, 205));
        htmlColors.put( "Bisque", new Color(255, 228, 196));
        htmlColors.put( "NavajoWhite", new Color(255, 222, 173));
        htmlColors.put( "Wheat", new Color(245, 222, 179));
        htmlColors.put( "BurlyWood", new Color(222, 184, 135));
        htmlColors.put( "Tan", new Color(210, 180, 140));
        htmlColors.put( "RosyBrown", new Color(188, 143, 143));
        htmlColors.put( "SandyBrown", new Color(244, 164, 96));
        htmlColors.put( "Goldenrod", new Color(218, 165, 32));
        htmlColors.put( "DarkGoldenrod", new Color(184, 134, 11));
        htmlColors.put( "Peru", new Color(205, 133, 63));
        htmlColors.put( "Chocolate", new Color(210, 105, 30));
        htmlColors.put( "SaddleBrown", new Color(139, 69, 19));
        htmlColors.put( "Sienna", new Color(160, 82, 45));
        htmlColors.put( "Brown", new Color(165, 42, 42));
        htmlColors.put( "Maroon", new Color(128, 0, 0));
        htmlColors.put( "White", new Color(255, 255, 255));
        htmlColors.put( "Snow", new Color(255, 250, 250));
        htmlColors.put( "HoneyDew", new Color(240, 255, 240));
        htmlColors.put( "MintCream", new Color(245, 255, 250));
        htmlColors.put( "Azure", new Color(240, 255, 255));
        htmlColors.put( "AliceBlue", new Color(240, 248, 255));
        htmlColors.put( "GhostWhite", new Color(248, 248, 255));
        htmlColors.put( "WhiteSmoke", new Color(245, 245, 245));
        htmlColors.put( "SeaShell", new Color(255, 245, 238));
        htmlColors.put( "Beige", new Color(245, 245, 220));
        htmlColors.put( "OldLace", new Color(253, 245, 230));
        htmlColors.put( "FloralWhite", new Color(255, 250, 240));
        htmlColors.put( "Ivory", new Color(255, 255, 240));
        htmlColors.put( "AntiqueWhite", new Color(250, 235, 215));
        htmlColors.put( "Linen", new Color(250, 240, 230));
        htmlColors.put( "LavenderBlush", new Color(255, 240, 245));
        htmlColors.put( "MistyRose", new Color(255, 228, 225));
        htmlColors.put( "Gainsboro", new Color(220, 220, 220));
        htmlColors.put( "LightGray", new Color(211, 211, 211));
        htmlColors.put( "Silver", new Color(192, 192, 192));
        htmlColors.put( "DarkGray", new Color(169, 169, 169));
        htmlColors.put( "Gray", new Color(128, 128, 128));
        htmlColors.put( "DimGray", new Color(105, 105, 105));
        htmlColors.put( "LightSlateGray", new Color(119, 136, 153));
        htmlColors.put( "SlateGray", new Color(112, 128, 144));
        htmlColors.put( "DarkSlateGray", new Color(47, 79, 79));
        htmlColors.put( "Black", new Color(0, 0, 0));
        Colors.htmlColors = Collections.unmodifiableMap(htmlColors);
    }
}
