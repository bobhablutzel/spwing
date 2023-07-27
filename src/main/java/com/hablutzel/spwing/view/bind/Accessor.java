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

package com.hablutzel.spwing.view.bind;


/**
 * Accessor classes give read, and potentially write,
 * access to a value. Accessors are used when binding
 * between view objects and model objects and provide
 * an abstraction across bean properties, SPEL expression
 * evaluation, Flexpression evaluations, and literal values.
 *
 * @author Bob Hablutzel
 */
public abstract class Accessor {

    /**
     * Determine if this is a writeable value
     *
     * @return TRUE for a writable value
     */
    public abstract boolean isWriteable();

    /**
     * Get the value
     *
     * @return The value
     */
    public abstract Object get();

    /**
     * Set the value
     * @param value The new value
     */
    public abstract void set(Object value);

    /**
     * Returns TRUE if this accessor can provide the target class
     * @param targetClass The desired class
     * @return TRUE if this can supply that class
     */
    public abstract boolean canSupply(Class<?> targetClass);


}
