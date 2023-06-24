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

package com.hablutzel.spwing.menu;


import com.hablutzel.spwing.Spwing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@RequiredArgsConstructor
@Slf4j
public class MenuItemActionAdapter implements ActionListener {

    private final Spwing spwing;

    private final String menuId;


    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof JMenuItem menuItem) {
            spwing.menuItemSelected(actionEvent, menuItem, menuId);
        }
    }

}
