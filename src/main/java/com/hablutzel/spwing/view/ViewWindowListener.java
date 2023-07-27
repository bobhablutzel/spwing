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

package com.hablutzel.spwing.view;

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.context.DocumentScopeManager;
import com.hablutzel.spwing.invoke.ParameterResolution;
import com.hablutzel.spwing.util.BeanUtils;
import com.hablutzel.spwing.util.WindowUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;


@RequiredArgsConstructor
@Slf4j
public class ViewWindowListener extends WindowAdapter {
    private final Spwing spwing;
    private final DocumentScopeManager documentScopeManager;
    private final UUID documentScopeID;

    @Getter
    private final JFrame frame;


    @Override
    public void windowActivated(WindowEvent e) {
        log.debug( "Window {} activated", e.getWindow().getName());
        documentScopeManager.activateScope(documentScopeID);

        final ApplicationContext context = spwing.getApplicationContext();
        BeanUtils.pushBean(context, "jFrame", frame);
        BeanUtils.pushBean(context, "window", e.getWindow());
    }

    @Override
    public void windowClosed(WindowEvent e) {
        documentScopeManager.disposeDocumentScope(documentScopeID);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        log.debug("User clicked close button for window {}", WindowUtils.getWindowTitle(e.getWindow()));
        spwing.fireCommand("cmdClose",
                ParameterResolution.forClass(Window.class, e.getWindow()),
                ParameterResolution.forClass(WindowEvent.class, e));
    }

}
