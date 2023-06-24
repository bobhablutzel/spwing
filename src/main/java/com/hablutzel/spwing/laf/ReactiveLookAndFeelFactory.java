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

import com.jthemedetecor.OsThemeDetector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


/**
 * The DefaultLookAndFeelFactory returns a {@link MetalLookAndFeel}
 * on all platforms except on MacOS, it attempts to use the
 * "FlatMacDarkLAF" if it is available.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public abstract class ReactiveLookAndFeelFactory implements LookAndFeelFactory {

    @Override
    public LookAndFeel get() {
        final OsThemeDetector detector = OsThemeDetector.getDetector();

        // Listen to changes in the system dark state, and react
        // on the fly if possible.
        detector.registerListener( isDark -> {
            SwingUtilities.invokeLater( () -> {
                try {

                    // Set the new look & feel
                    log.debug( "Reacting to OS change" );
                    UIManager.setLookAndFeel(get(isDark));

                    // Refresh all the existing windows to match the new look & feel
                    Arrays.stream(Window.getWindows()).forEach(SwingUtilities::updateComponentTreeUI);

                } catch (UnsupportedLookAndFeelException e) {
                    log.error("Unable to set look and feel reactively", e);
                }
            });
        });

        // Set the initial look and feel
        return get(detector.isDark());
    }


    public abstract String getLightThemeName();
    public abstract String getDarkThemeName();


    private LookAndFeel get(boolean isDarkThemeUsed) {
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {

            try {

                String themeName = isDarkThemeUsed ? getDarkThemeName() : getLightThemeName();
                log.debug( "Theme name: {}", themeName);

                Class<?> flatMacClass = Class.forName(themeName);
                Object instance = flatMacClass.getDeclaredConstructor().newInstance();
                if (instance instanceof LookAndFeel lookAndFeel) {
                    return lookAndFeel;
                }
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                log.warn("For MacOS, attempt to use the FlatMac Dark look and feel failed", e);
            }
        }
        return new MetalLookAndFeel();
    }
}
