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

import com.hablutzel.spwing.view.adapter.ContainerEventAdapter;
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import java.awt.Container;


/**
 * Create a new {@link Container} instance, including registering an
 * event adapter for that instance with the current document event dispatcher.
 *
 * @author Bob Hablutzel
 */
@Service
@Scope("singleton")
@Slf4j
public final class ContainerFactory extends AbstractViewComponentFactory<Container> {

    @Override
    public Cocoon<Container> build(String name, ConversionService conversionService) {
        Container container = new Container();
        registerAdapter(container, name, ContainerEventAdapter::new);
        return new Cocoon<>(container, this, conversionService);
    }

    @Override
    public String alias() {
        return "Container";
    }
}
