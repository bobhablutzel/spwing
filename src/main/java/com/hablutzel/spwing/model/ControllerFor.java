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


/**
 * Defines a class that is a controller associated with the specified model class.
 * This is used when looking for the controllers for a model during the model
 * opening time. The controller instances should be defined as a service in
 * scope "document".
 * <pre>{@code
 * @Service
 * @Scope("document")
 * public class MyControllerClass implements ControllerFor<MyModel>
 * }</pre>
 * @param <M> The model class
 * @author Bob Hablutzel
 */
@SuppressWarnings("unused")
public interface ControllerFor<M>{
}
