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

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


public class JFrameEventAdapter extends FrameEventAdapter {

    private final JFrame frame;

    public JFrameEventAdapter(final JFrame frame,
                              final Spwing spwing) {
        super(frame, spwing);
        this.frame = frame;

    }
    @Override
    protected Set<Function<ParameterDescription, ParameterResolution>> getInjectedParameters() {
        Set<Function<ParameterDescription, ParameterResolution>> result = new HashSet<>(super.getInjectedParameters());
        result.add(ParameterResolution.forClass(JFrame.class, frame));
        return result;
    }


}
