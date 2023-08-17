/*
 * Copyright © 2023. Hablutzel Consulting, LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License";
}

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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.awt.Color;


/**
 * Service that defines the HTML colors as bean definitions
 *
 * @author Bob Hablutzel
 */
@Service
@Scope("singleton")
public class SVWFColorFactory {

    @Bean
    @Scope("singleton")
    public Color indianRed() {
        return new Color(205, 92, 92);
    }

    @Bean
    @Scope("singleton")
    public Color lightCoral() {
        return new Color(240, 128, 128);
    }

    @Bean
    @Scope("singleton")
    public Color salmon() {
        return new Color(250, 128, 114);
    }

    @Bean
    @Scope("singleton")
    public Color darkSalmon() {
        return new Color(233, 150, 122);
    }

    @Bean
    @Scope("singleton")
    public Color lightSalmon() {
        return new Color(255, 160, 122);
    }

    @Bean
    @Scope("singleton")
    public Color crimson() {
        return new Color(220, 20, 60);
    }

    @Bean
    @Scope("singleton")
    public Color red() {
        return new Color(255, 0, 0);
    }

    @Bean
    @Scope("singleton")
    public Color fireBrick() {
        return new Color(178, 34, 34);
    }

    @Bean
    @Scope("singleton")
    public Color darkRed() {
        return new Color(139, 0, 0);
    }

    @Bean
    @Scope("singleton")
    public Color pink() {
        return new Color(255, 192, 203);
    }

    @Bean
    @Scope("singleton")
    public Color lightPink() {
        return new Color(255, 182, 193);
    }

    @Bean
    @Scope("singleton")
    public Color hotPink() {
        return new Color(255, 105, 180);
    }

    @Bean
    @Scope("singleton")
    public Color deepPink() {
        return new Color(255, 20, 147);
    }

    @Bean
    @Scope("singleton")
    public Color mediumVioletRed() {
        return new Color(199, 21, 133);
    }

    @Bean
    @Scope("singleton")
    public Color paleVioletRed() {
        return new Color(219, 112, 147);
    }

    @Bean
    @Scope("singleton")
    public Color coral() {
        return new Color(255, 127, 80);
    }

    @Bean
    @Scope("singleton")
    public Color tomato() {
        return new Color(255, 99, 71);
    }

    @Bean
    @Scope("singleton")
    public Color orangeRed() {
        return new Color(255, 69, 0);
    }

    @Bean
    @Scope("singleton")
    public Color darkOrange() {
        return new Color(255, 140, 0);
    }

    @Bean
    @Scope("singleton")
    public Color orange() {
        return new Color(255, 165, 0);
    }

    @Bean
    @Scope("singleton")
    public Color gold() {
        return new Color(255, 215, 0);
    }

    @Bean
    @Scope("singleton")
    public Color yellow() {
        return new Color(255, 255, 0);
    }

    @Bean
    @Scope("singleton")
    public Color lightYellow() {
        return new Color(255, 255, 224);
    }

    @Bean
    @Scope("singleton")
    public Color lemonChiffon() {
        return new Color(255, 250, 205);
    }

    @Bean
    @Scope("singleton")
    public Color lightGoldenrodYellow() {
        return new Color(250, 250, 210);
    }

    @Bean
    @Scope("singleton")
    public Color papayaWhip() {
        return new Color(255, 239, 213);
    }

    @Bean
    @Scope("singleton")
    public Color moccasin() {
        return new Color(255, 228, 181);
    }

    @Bean
    @Scope("singleton")
    public Color peachPuff() {
        return new Color(255, 218, 185);
    }

    @Bean
    @Scope("singleton")
    public Color paleGoldenrod() {
        return new Color(238, 232, 170);
    }

    @Bean
    @Scope("singleton")
    public Color khaki() {
        return new Color(240, 230, 140);
    }

    @Bean
    @Scope("singleton")
    public Color darkKhaki() {
        return new Color(189, 183, 107);
    }

    @Bean
    @Scope("singleton")
    public Color lavender() {
        return new Color(230, 230, 250);
    }

    @Bean
    @Scope("singleton")
    public Color thistle() {
        return new Color(216, 191, 216);
    }

    @Bean
    @Scope("singleton")
    public Color plum() {
        return new Color(221, 160, 221);
    }

    @Bean
    @Scope("singleton")
    public Color violet() {
        return new Color(238, 130, 238);
    }

    @Bean
    @Scope("singleton")
    public Color orchid() {
        return new Color(218, 112, 214);
    }

    @Bean
    @Scope("singleton")
    public Color fuchsia() {
        return new Color(255, 0, 255);
    }

    @Bean
    @Scope("singleton")
    public Color magenta() {
        return new Color(255, 0, 255);
    }

    @Bean
    @Scope("singleton")
    public Color mediumOrchid() {
        return new Color(186, 85, 211);
    }

    @Bean
    @Scope("singleton")
    public Color mediumPurple() {
        return new Color(147, 112, 219);
    }

    @Bean
    @Scope("singleton")
    public Color rebeccaPurple() {
        return new Color(102, 51, 153);
    }

    @Bean
    @Scope("singleton")
    public Color blueViolet() {
        return new Color(138, 43, 226);
    }

    @Bean
    @Scope("singleton")
    public Color darkViolet() {
        return new Color(148, 0, 211);
    }

    @Bean
    @Scope("singleton")
    public Color darkOrchid() {
        return new Color(153, 50, 204);
    }

    @Bean
    @Scope("singleton")
    public Color darkMagenta() {
        return new Color(139, 0, 139);
    }

    @Bean
    @Scope("singleton")
    public Color purple() {
        return new Color(128, 0, 128);
    }

    @Bean
    @Scope("singleton")
    public Color indigo() {
        return new Color(75, 0, 130);
    }

    @Bean
    @Scope("singleton")
    public Color slateBlue() {
        return new Color(106, 90, 205);
    }

    @Bean
    @Scope("singleton")
    public Color darkSlateBlue() {
        return new Color(72, 61, 139);
    }

    @Bean
    @Scope("singleton")
    public Color greenYellow() {
        return new Color(173, 255, 47);
    }

    @Bean
    @Scope("singleton")
    public Color chartreuse() {
        return new Color(127, 255, 0);
    }

    @Bean
    @Scope("singleton")
    public Color lawnGreen() {
        return new Color(124, 252, 0);
    }

    @Bean
    @Scope("singleton")
    public Color lime() {
        return new Color(0, 255, 0);
    }

    @Bean
    @Scope("singleton")
    public Color limeGreen() {
        return new Color(50, 205, 50);
    }

    @Bean
    @Scope("singleton")
    public Color paleGreen() {
        return new Color(152, 251, 152);
    }

    @Bean
    @Scope("singleton")
    public Color lightGreen() {
        return new Color(144, 238, 144);
    }

    @Bean
    @Scope("singleton")
    public Color mediumSpringGreen() {
        return new Color(0, 250, 154);
    }

    @Bean
    @Scope("singleton")
    public Color springGreen() {
        return new Color(0, 255, 127);
    }

    @Bean
    @Scope("singleton")
    public Color mediumSeaGreen() {
        return new Color(60, 179, 113);
    }

    @Bean
    @Scope("singleton")
    public Color seaGreen() {
        return new Color(46, 139, 87);
    }

    @Bean
    @Scope("singleton")
    public Color forestGreen() {
        return new Color(34, 139, 34);
    }

    @Bean
    @Scope("singleton")
    public Color green() {
        return new Color(0, 128, 0);
    }

    @Bean
    @Scope("singleton")
    public Color darkGreen() {
        return new Color(0, 100, 0);
    }

    @Bean
    @Scope("singleton")
    public Color yellowGreen() {
        return new Color(154, 205, 50);
    }

    @Bean
    @Scope("singleton")
    public Color oliveDrab() {
        return new Color(107, 142, 35);
    }

    @Bean
    @Scope("singleton")
    public Color olive() {
        return new Color(128, 128, 0);
    }

    @Bean
    @Scope("singleton")
    public Color darkOliveGreen() {
        return new Color(85, 107, 47);
    }

    @Bean
    @Scope("singleton")
    public Color mediumAquamarine() {
        return new Color(102, 205, 170);
    }

    @Bean
    @Scope("singleton")
    public Color darkSeaGreen() {
        return new Color(143, 188, 139);
    }

    @Bean
    @Scope("singleton")
    public Color lightSeaGreen() {
        return new Color(32, 178, 170);
    }

    @Bean
    @Scope("singleton")
    public Color darkCyan() {
        return new Color(0, 139, 139);
    }

    @Bean
    @Scope("singleton")
    public Color teal() {
        return new Color(0, 128, 128);
    }

    @Bean
    @Scope("singleton")
    public Color aqua() {
        return new Color(0, 255, 255);
    }

    @Bean
    @Scope("singleton")
    public Color cyan() {
        return new Color(0, 255, 255);
    }

    @Bean
    @Scope("singleton")
    public Color lightCyan() {
        return new Color(224, 255, 255);
    }

    @Bean
    @Scope("singleton")
    public Color paleTurquoise() {
        return new Color(175, 238, 238);
    }

    @Bean
    @Scope("singleton")
    public Color aquamarine() {
        return new Color(127, 255, 212);
    }

    @Bean
    @Scope("singleton")
    public Color turquoise() {
        return new Color(64, 224, 208);
    }

    @Bean
    @Scope("singleton")
    public Color mediumTurquoise() {
        return new Color(72, 209, 204);
    }

    @Bean
    @Scope("singleton")
    public Color darkTurquoise() {
        return new Color(0, 206, 209);
    }

    @Bean
    @Scope("singleton")
    public Color cadetBlue() {
        return new Color(95, 158, 160);
    }

    @Bean
    @Scope("singleton")
    public Color steelBlue() {
        return new Color(70, 130, 180);
    }

    @Bean
    @Scope("singleton")
    public Color lightSteelBlue() {
        return new Color(176, 196, 222);
    }

    @Bean
    @Scope("singleton")
    public Color powderBlue() {
        return new Color(176, 224, 230);
    }

    @Bean
    @Scope("singleton")
    public Color lightBlue() {
        return new Color(173, 216, 230);
    }

    @Bean
    @Scope("singleton")
    public Color skyBlue() {
        return new Color(135, 206, 235);
    }

    @Bean
    @Scope("singleton")
    public Color lightSkyBlue() {
        return new Color(135, 206, 250);
    }

    @Bean
    @Scope("singleton")
    public Color deepSkyBlue() {
        return new Color(0, 191, 255);
    }

    @Bean
    @Scope("singleton")
    public Color dodgerBlue() {
        return new Color(30, 144, 255);
    }

    @Bean
    @Scope("singleton")
    public Color cornflowerBlue() {
        return new Color(100, 149, 237);
    }

    @Bean
    @Scope("singleton")
    public Color mediumSlateBlue() {
        return new Color(123, 104, 238);
    }

    @Bean
    @Scope("singleton")
    public Color royalBlue() {
        return new Color(65, 105, 225);
    }

    @Bean
    @Scope("singleton")
    public Color blue() {
        return new Color(0, 0, 255);
    }

    @Bean
    @Scope("singleton")
    public Color mediumBlue() {
        return new Color(0, 0, 205);
    }

    @Bean
    @Scope("singleton")
    public Color darkBlue() {
        return new Color(0, 0, 139);
    }

    @Bean
    @Scope("singleton")
    public Color navy() {
        return new Color(0, 0, 128);
    }

    @Bean
    @Scope("singleton")
    public Color midnightBlue() {
        return new Color(25, 25, 112);
    }

    @Bean
    @Scope("singleton")
    public Color cornsilk() {
        return new Color(255, 248, 220);
    }

    @Bean
    @Scope("singleton")
    public Color blanchedAlmond() {
        return new Color(255, 235, 205);
    }

    @Bean
    @Scope("singleton")
    public Color bisque() {
        return new Color(255, 228, 196);
    }

    @Bean
    @Scope("singleton")
    public Color navajoWhite() {
        return new Color(255, 222, 173);
    }

    @Bean
    @Scope("singleton")
    public Color wheat() {
        return new Color(245, 222, 179);
    }

    @Bean
    @Scope("singleton")
    public Color burlyWood() {
        return new Color(222, 184, 135);
    }

    @Bean
    @Scope("singleton")
    public Color tan() {
        return new Color(210, 180, 140);
    }

    @Bean
    @Scope("singleton")
    public Color rosyBrown() {
        return new Color(188, 143, 143);
    }

    @Bean
    @Scope("singleton")
    public Color sandyBrown() {
        return new Color(244, 164, 96);
    }

    @Bean
    @Scope("singleton")
    public Color goldenrod() {
        return new Color(218, 165, 32);
    }

    @Bean
    @Scope("singleton")
    public Color darkGoldenrod() {
        return new Color(184, 134, 11);
    }

    @Bean
    @Scope("singleton")
    public Color peru() {
        return new Color(205, 133, 63);
    }

    @Bean
    @Scope("singleton")
    public Color chocolate() {
        return new Color(210, 105, 30);
    }

    @Bean
    @Scope("singleton")
    public Color saddleBrown() {
        return new Color(139, 69, 19);
    }

    @Bean
    @Scope("singleton")
    public Color sienna() {
        return new Color(160, 82, 45);
    }

    @Bean
    @Scope("singleton")
    public Color brown() {
        return new Color(165, 42, 42);
    }

    @Bean
    @Scope("singleton")
    public Color maroon() {
        return new Color(128, 0, 0);
    }

    @Bean
    @Scope("singleton")
    public Color white() {
        return new Color(255, 255, 255);
    }

    @Bean
    @Scope("singleton")
    public Color snow() {
        return new Color(255, 250, 250);
    }

    @Bean
    @Scope("singleton")
    public Color honeyDew() {
        return new Color(240, 255, 240);
    }

    @Bean
    @Scope("singleton")
    public Color mintCream() {
        return new Color(245, 255, 250);
    }

    @Bean
    @Scope("singleton")
    public Color azure() {
        return new Color(240, 255, 255);
    }

    @Bean
    @Scope("singleton")
    public Color aliceBlue() {
        return new Color(240, 248, 255);
    }

    @Bean
    @Scope("singleton")
    public Color ghostWhite() {
        return new Color(248, 248, 255);
    }

    @Bean
    @Scope("singleton")
    public Color whiteSmoke() {
        return new Color(245, 245, 245);
    }

    @Bean
    @Scope("singleton")
    public Color seaShell() {
        return new Color(255, 245, 238);
    }

    @Bean
    @Scope("singleton")
    public Color beige() {
        return new Color(245, 245, 220);
    }

    @Bean
    @Scope("singleton")
    public Color oldLace() {
        return new Color(253, 245, 230);
    }

    @Bean
    @Scope("singleton")
    public Color floralWhite() {
        return new Color(255, 250, 240);
    }

    @Bean
    @Scope("singleton")
    public Color ivory() {
        return new Color(255, 255, 240);
    }

    @Bean
    @Scope("singleton")
    public Color antiqueWhite() {
        return new Color(250, 235, 215);
    }

    @Bean
    @Scope("singleton")
    public Color linen() {
        return new Color(250, 240, 230);
    }

    @Bean
    @Scope("singleton")
    public Color lavenderBlush() {
        return new Color(255, 240, 245);
    }

    @Bean
    @Scope("singleton")
    public Color mistyRose() {
        return new Color(255, 228, 225);
    }

    @Bean
    @Scope("singleton")
    public Color gainsboro() {
        return new Color(220, 220, 220);
    }

    @Bean
    @Scope("singleton")
    public Color lightGray() {
        return new Color(211, 211, 211);
    }

    @Bean
    @Scope("singleton")
    public Color silver() {
        return new Color(192, 192, 192);
    }

    @Bean
    @Scope("singleton")
    public Color darkGray() {
        return new Color(169, 169, 169);
    }

    @Bean
    @Scope("singleton")
    public Color gray() {
        return new Color(128, 128, 128);
    }

    @Bean
    @Scope("singleton")
    public Color dimGray() {
        return new Color(105, 105, 105);
    }

    @Bean
    @Scope("singleton")
    public Color lightSlateGray() {
        return new Color(119, 136, 153);
    }

    @Bean
    @Scope("singleton")
    public Color slateGray() {
        return new Color(112, 128, 144);
    }

    @Bean
    @Scope("singleton")
    public Color darkSlateGray() {
        return new Color(47, 79, 79);
    }

    @Bean
    @Scope("singleton")
    public Color black() {
        return new Color(0, 0, 0);
    }

}
