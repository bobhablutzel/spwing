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
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.swing.JLabel;

/**
 * Create a new {@link JLabel} instance, including registering an
 * event adapter for that instance with the current document event dispatcher.
 *
 * @author Bob Hablutzel
 */
@Service
@Scope("singleton")
@Slf4j
public final class JLabelFactory extends AbstractViewComponentFactory<JLabel> {


    @Override
    public Cocoon<JLabel> build(String name, ConversionService conversionService) {
        JLabel label = new JLabel();
        registerAdapter(label, name, JLabelEventAdapter::new);
        return new Cocoon<>(label, this, conversionService );
    }

    @Override
    public String alias() {
        return "JLabel";
    }

}
