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

package com.hablutzel.spwing.laf;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;


/**
 * The DefaultLookAndFeelFactory is a reactive theme that uses
 * a default window manager for all platforms except MacOS, and
 * uses a the FlatMac themes on MacOS
 * @author Bob Hablutzel
 */
@Slf4j
public class DefaultLookAndFeelFactory extends ReactiveLookAndFeelFactory {

    @Override
    public String getDarkThemeName() {
        return SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC
                ? "com.formdev.flatlaf.themes.FlatMacDarkLaf"
                : UIManager.getSystemLookAndFeelClassName();
    }

    @Override
    public String getLightThemeName() {
        return SystemUtils.IS_OS_MAC_OSX || SystemUtils.IS_OS_MAC
                ? "com.formdev.flatlaf.themes.FlatMacLightLaf"
                : UIManager.getSystemLookAndFeelClassName();
    }
}
