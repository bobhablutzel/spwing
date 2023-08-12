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

import com.hablutzel.spwing.view.adapter.JButtonEventAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.swing.JButton;

/**
 * Create a new {@link JButton} instance, including registering an
 * event adapter for that instance with the current document event dispatcher.
 *
 * @author Bob Hablutzel
 */
@Service
@Scope("singleton")
@Slf4j
public final class JButtonFactory extends AbstractViewComponentFactory<JButton> {

    @Override
    public JButton build(final String name) {
        return registerAdapter(new JButton(), name, JButtonEventAdapter::new);
    }

    @Override
    public String alias() {
        return "JButton";
    }

}
