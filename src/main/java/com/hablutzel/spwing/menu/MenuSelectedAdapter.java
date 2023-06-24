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

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.util.Map;


@RequiredArgsConstructor
public class MenuSelectedAdapter implements MenuListener {

    private final Spwing spwing;
    private final String menuID;
    private final JMenu menu;
    private final Map<String, JMenuItem> mappedMenuItems;
    private final boolean dynamicMenu;

    @Override
    public void menuSelected(MenuEvent e) {
        spwing.aboutToDisplayMenu(menuID, menu, mappedMenuItems);
        if (dynamicMenu) {
            spwing.populateMenu(menuID, menu);
        }
    }

    @Override
    public void menuCanceled(MenuEvent e) {

    }

    @Override
    public void menuDeselected(MenuEvent e) {

    }
}
