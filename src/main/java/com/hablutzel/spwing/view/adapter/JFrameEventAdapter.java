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

package com.hablutzel.spwing.view.adapter;

import javax.swing.*;
import java.util.Map;
import java.util.function.Supplier;


public class JFrameEventAdapter extends FrameEventAdapter {

    private final JFrame frame;

    public JFrameEventAdapter(JFrame frame) {
        super(frame);
        this.frame = frame;

    }
    @Override
    protected Map<Class<?>, Supplier<Object>> getParameterMap() {
        return Map.of(JFrame.class, () -> frame);
    }


}
