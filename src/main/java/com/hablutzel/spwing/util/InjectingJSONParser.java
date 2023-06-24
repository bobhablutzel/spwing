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

package com.hablutzel.spwing.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.function.Consumer;

public class InjectingJSONParser {

    public static ObjectMapper create(Map<Class<?>, Consumer<Object>> injectionPoints) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.setDeserializerModifier(new BeanDeserializerModifier()
        {
            @Override public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer)
            {
                if (injectionPoints.keySet().stream()
                        .anyMatch( c -> c.isAssignableFrom(beanDesc.getBeanClass()))) {
                    return new InjectingDeserializer<>(deserializer, beanDesc.getBeanClass(), injectionPoints);
                } else {
                    return deserializer;
                }
            }
        });
        mapper.registerModule(module);
        return mapper;
    }

    @Slf4j
    public static class InjectingDeserializer<T> extends StdDeserializer<T> implements ResolvableDeserializer {
        private final JsonDeserializer<?> defaultDeserializer;
        private final Class<T> targetClass;
        private final Map<Class<?>, Consumer<Object>> injectionPoints;

        public InjectingDeserializer(JsonDeserializer<?> defaultDeserializer, Class<T> targetClass, Map<Class<?>, Consumer<Object>> injectionPoints) {
            super(targetClass);
            this.defaultDeserializer = defaultDeserializer;
            this.targetClass = targetClass;
            this.injectionPoints = injectionPoints;
        }

        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            T deserializedInstance = targetClass.cast(defaultDeserializer.deserialize(jp, ctxt));
            injectionPoints.forEach((type,consumer) -> {
                if (type.isAssignableFrom(deserializedInstance.getClass())) {
                    log.debug("Injecting into {}", deserializedInstance.getClass().getName());
                    consumer.accept(deserializedInstance);
                }
            });
            return deserializedInstance;
        }

        // for some reason you have to implement ResolvableDeserializer when modifying BeanDeserializer
        // otherwise deserializing throws JsonMappingException??
        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
        }
    }
}
