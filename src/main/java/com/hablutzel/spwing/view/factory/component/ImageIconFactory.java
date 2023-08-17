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

package com.hablutzel.spwing.view.factory.component;

import com.hablutzel.spwing.view.adapter.JLabelEventAdapter;
import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.swing.Icon;
import javax.swing.JLabel;



@Service
@Scope("singleton")
@Slf4j
public class ImageIconFactory extends AbstractViewComponentFactory<JLabel> {

    @Override
    public Cocoon<JLabel> build(String name, ConversionService conversionService) {
        JLabel result = new JLabel();
        registerAdapter(result, name, JLabelEventAdapter::new);
        return new Cocoon<>(result, this, conversionService) {

            @Override
            public AllowedBindings allowedBindings(String propertyName) {
                return "image".equals(propertyName) || "altText".equals(propertyName)
                        ? AllowedBindings.FROM_MODEL
                        : AllowedBindings.NONE;
            }

            @Override
            public boolean canSetProperty(String propertyName) {
                return "image".equals(propertyName) || "altText".equals(propertyName);
            }

            @Override
            public void setProperty(String propertyName, Object value) {
                if ("image".equals(propertyName)) {
                    result.setIcon(conversionService.convert(value, Icon.class));
                } else {
                    result.setToolTipText(conversionService.convert(value, String.class));
                }
            }

            @Override
            public void setFromAccessor(String propertyName, Accessor externalState) {
                if ("image".equals(propertyName)) {
                    result.setIcon(getAs(externalState.get(), Icon.class));
                } else {
                    result.setToolTipText(getAs(externalState.get(), String.class));
                }
            }
        };
    }

    @Override
    public String alias() {
        return "ImageIcon";
    }
}
