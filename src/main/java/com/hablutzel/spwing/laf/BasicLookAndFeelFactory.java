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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.lang.reflect.InvocationTargetException;


@RequiredArgsConstructor
@Slf4j
public class BasicLookAndFeelFactory implements LookAndFeelFactory {

    private final String lookAndFeelClassName;

    @Override
    public LookAndFeel get() {
        return buildLookAndFeel(lookAndFeelClassName);
    }


    public LookAndFeel buildLookAndFeel(String lookAndFeelClassName) {
        try {
            Class<?> lafClass = Class.forName(lookAndFeelClassName);
            Object instance = lafClass.getDeclaredConstructor().newInstance();
            if (instance instanceof LookAndFeel lookAndFeel) {
                log.debug( "Look and feel used: {}", lookAndFeel);
                return lookAndFeel;
            } else {
                log.error( "Class {} does not implement LookAndFeel, defaulting to Metal look and feel", lookAndFeelClassName);
                return new MetalLookAndFeel();
            }
        } catch (ClassNotFoundException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException
                 | NoSuchMethodException e) {

            log.error("Look and feel {} could not be loaded", lookAndFeelClassName, e);
            return new MetalLookAndFeel();
        }
    }
}
