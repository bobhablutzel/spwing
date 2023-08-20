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

import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.Window;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;


/**
 * The ReactiveLookAndFeelFactory implements a {@link LookAndFeelFactory}
 * that is aware of both the OS and the dark/light state of the user settings.
 * For all platforms but MacOS, uses {@link MetalLookAndFeel} by default
 * unless overridden. For MacOS, uses separate light and dark themes provided
 * by the subclass, and reacts to changes in the light/dark state of the user
 * UI settings.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public abstract class ReactiveLookAndFeelFactory implements LookAndFeelFactory {

    /**
     * Get the {@link LookAndFeel} to use
     * @return The {@link LookAndFeel} instance
     */
    @Override
    public LookAndFeel get() {
        final OsThemeDetector detector = OsThemeDetector.getDetector();

        // Listen to changes in the system dark state, and react
        // on the fly if possible.
        detector.registerListener( isDark -> SwingUtilities.invokeLater(() -> reactToThemeChange(isDark)));

        // Set the initial look and feel
        return get(detector.isDark());
    }


    /**
     * Routine to react to a theme change. If the user changes their
     * environment from light to dark, this routine will react to that
     * change and reset the look and feel
     * @param isDark TRUE if the user now uses dark mode
     */
    private void reactToThemeChange(final Boolean isDark) {
        try {

            // Set the new look & feel
            log.debug("Reacting to OS change");
            UIManager.setLookAndFeel(get(isDark));

            // Refresh all the existing windows to match the new look & feel
            Arrays.stream(Window.getWindows()).forEach(SwingUtilities::updateComponentTreeUI);

        } catch (UnsupportedLookAndFeelException e) {
            log.error("Unable to set look and feel reactively", e);
        }
    }


    /**
     * Routine to get the light theme
     * @return The light theme class name
     */
    public abstract String getLightThemeName();

    /**
     * Routine to get the dark theme name
     * @return The dark theme class name
     */
    public abstract String getDarkThemeName();


    /**
     * Get the look and theme in a platform specific way
     *
     * @param isDarkThemeUsed TRUE if the dark theme should be used
     * @return The {@link LookAndFeel} to use
     */
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

        // Default to metal
        return new MetalLookAndFeel();
    }
}
