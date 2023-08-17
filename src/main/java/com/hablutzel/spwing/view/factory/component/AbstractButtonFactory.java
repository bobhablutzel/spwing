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

import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.RefreshTrigger;
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import java.util.List;

/**
 * Create a new {@link JCheckBox} instance, including registering an
 * event adapter for that instance with the current document event dispatcher.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public abstract class AbstractButtonFactory<T extends AbstractButton> extends AbstractViewComponentFactory<T> {


    protected static class ButtonCocoon<T extends AbstractButton> extends Cocoon<T> {

        private final AbstractButton abstractButton;
        public ButtonCocoon(final T component,
                            final ViewComponentFactory<T> factory,
                            final ConversionService conversionService) {
            super(component, factory, conversionService);
            this.abstractButton = component;
        }

        @Override
        public void bindProperty(String propertyName, Accessor externalState, List<RefreshTrigger> refreshTriggers) {
            super.bindProperty(propertyName, externalState, refreshTriggers);
            if ("selected".equals(propertyName) && externalState.isWriteable()) {
                abstractButton.addChangeListener( e -> {
                    if (abstractButton.isSelected() != getAs(externalState.get(), Boolean.class)) {
                        externalState.set(abstractButton.isSelected());
                    }
                });
            }
        }
    }

}
