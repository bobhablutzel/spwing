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

package com.hablutzel.spwing.model;


import com.hablutzel.spwing.view.factory.svwf.SVWFResourceViewFactory;

/**
 * The ModelConfiguration class provides framework information
 * for models of type T. Instances of this interface should
 * defines beans of well known types or names that can be hooked
 * into by the framework. The current beans that can be defined
 * by the configuration include
 * <ul>
 *     <li>name: fileExtension - defines the file extension for file-based documents. If not defined,
 *     no default file based processing will be enabled.</li>
 *     <li>type: {@link ModelFactory<T>}&lt;T&gt; - defines the ModelFactory instance to use. If not defined a
 *     {@link ProxyModelFactory} will be created on the fly. The generic type must match the model class</li>
 *     <li>name: viewFactoryClass - A class defining the view factory. If not defined, {@link SVWFResourceViewFactory} will be
 *     used by default. NOTE: This is the class, not the instance, to allow for injection of parameter. The framework
 *     will look for a bean of this class.</li>
 * </ul>
 * @param <T> The model type
 * @author Bob Hablutzel
 */
public interface ModelConfiguration<T> {
}


