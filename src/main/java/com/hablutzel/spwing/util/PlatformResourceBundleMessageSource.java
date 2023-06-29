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

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.lang.NonNull;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


@Slf4j
public class PlatformResourceBundleMessageSource extends ResourceBundleMessageSource {

    @Override
    @NonNull
    protected ResourceBundle doGetBundle(@NonNull final String basename, @NonNull final Locale locale) throws MissingResourceException {
        for (String platformBaseName: PlatformResourceUtils.platformAndBaseNames(basename)) {
            try {
                return super.doGetBundle(platformBaseName, locale);
            } catch (MissingResourceException e) {
                log.debug( "{} was not found", platformBaseName );
            }
        }
        return new ResourceBundle() {
            @Override
            protected Object handleGetObject(String key) {
                return key;
            }

            @Override
            public Enumeration<String> getKeys() {
                return new Enumeration<>() {
                    @Override
                    public boolean hasMoreElements() {
                        return false;
                    }

                    @Override
                    public String nextElement() {
                        return null;
                    }
                };
            }
        };
    }
}
