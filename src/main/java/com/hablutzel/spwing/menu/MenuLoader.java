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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.Handler;
import com.hablutzel.spwing.annotations.MenuSource;
import com.hablutzel.spwing.util.PlatformResourceUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;

import javax.swing.*;
import java.io.InputStream;
import java.util.*;

/**
 * MenuLoader translates a JSON representation of a menu into a
 * Swing menu bar. The menu bar is then associated with the
 * application, and from there with the {@link Handler} chain.<br>
 * The JSON structure is an array of menu nodes. Each JSON menu node
 * takes the following attributes:<br><br>
 * <pre>     id           required     The menu ID (see below)</pre>
 * <pre>     items        optional     The menu items; missing or empty for dynamic menus (see below)</pre><br>
 * <br>
 * The menu ID begins with the prefix "m_" followed by a single
 * capitalized name (e.g. "m_File", "m_Window"). This identifies
 * the node as a menu node and is required syntax.<br>
 * The menu id has two purposes. First, it is used to find the name of the
 * menu via {@link org.springframework.context.MessageSource}. The properties
 * files used for localization should have an entry for each menu providing the
 * display text to use - example {@code m_File = File}. Secondly, the menu id
 * provides the name of the menu for looking up the methods associated with the
 * menu; see for details
 * If the items attribute is specified, it can either be empty or contain
 * menu item nodes. A menu item item node is either another menu node,
 * denoting a sub-menu, an empty node denoting a separator, a node containing
 * only the id attribute with value "-" denoting a separator, or a menu item node.
 * <br>
 * The JSON structure for menu item nodes takes the following attributes:<br><br>
 * <pre>     id           required     The command ID (see below)</pre><br>
 * <pre>     accelerator  optional     The accelerator for the menu</pre><br>
 * <br>
 * Similarly to the menu ID, the command ID has a required prefix "cmd". This is
 * followed by one or more words in camel case, with the first word being
 * capitalized. As with the menu id, the command ID should have an entry in the
 * localization property files to provide the display name of the command. Also
 * similarly to the menu ID, the command ID is used to find the appropriate handler
 * methods.<br>
 * If the items attribute is specified on a menu as an empty array, or is omitted,
 * then the menu items are dynamically populated. In this case, the handler must
 * specify a population method that will be invoked in order to supply the menu items.
 * <br>
 * @author Bob Hablutzel
 */
@Slf4j
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class MenuLoader {

    /**
     * The object that is the source of our menu
     */
    private final Class<?> menuSourceClass;

    /**
     * The name of the menu JSON file to look for
     */
    private final String menuName;

    /**
     * The command IDs that come from this loader
     */
    @Getter
    private final Map<JMenuItem, String> commandMap = new HashMap<>();

    /**
     * The menu IDs that come from this loader.
     */
    @Getter
    private final Set<String> menuIDs = new HashSet<>();


    /**
     * Dynamic top level menus, so we don't delete the menu
     * when it is empty
     */
    private final Set<JMenu> dynamicMenus = new HashSet<>();

    /**
     * Build a MenuLoader instance from the specified object, which
     * should have a {@link MenuSource} annotation.
     *
     * @param documentComponent The document component instance
     * @return The MenuLoader, or null if it could not be built
     */
    public static MenuLoader build(final Object documentComponent) {

        // See if the object has a menu source annotation. If not,
        // return null. Check to see if the user passed in a class
        // rather than an object; if they did then use that as the
        // target class.
        Class<?> documentComponentClass = documentComponent.getClass();

        // Find the menu - either specified by the annotation, or
        // default to the class name
        String menuSourceClassName = documentComponentClass.getSimpleName();
        MenuSource menuSource = AnnotatedElementUtils.getMergedAnnotation(documentComponentClass, MenuSource.class);
        if (Objects.nonNull(menuSource)) {
            String menuName = menuSource.menuName().isBlank()
                    ? menuSourceClassName
                    : menuSource.menuName();
            return new MenuLoader(documentComponentClass, menuName);
        } else {
            return null;
        }
    }


    /**
     * Rebuilds the menu bar based on the menu specification for this menu
     * loader. The menu bar from the {@link Spwing#getMenuBar()} will be used.
     * The result of this method will be a {@link java.util.Set} of {@link String}
     * denoting the actions that are found in the menu bar.
     *
     * @param spwing The {@link Spwing} to use
     * @return A Set of action ids from the menu bar
     */
    public void rebuildMenuBar(Spwing spwing) {

        // Load the JSON representation of the menu. If this cannot
        // be loaded, then the menu cannot be rebuilt and will
        // be left untouched from the preview state.
        try(InputStream in= PlatformResourceUtils.getPlatformResource(menuSourceClass, menuName, "json" )) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(in, JsonNode.class);

            // We have a root node. We will use it to rebuild the menus.
            // We start by resetting the contents of any existing menus to
            // empty; we will then add items back to the menu. If we need to add
            // a new menu, we'll do that at the end of the menu bar. If at the
            // end of the rebuild process a menu is still empty, we will remove it
            // from the menu bar. This approach minimizes menu bar flicker.
            JMenuBar menuBar = spwing.getMenuBar();
            for (int i = 0; i < menuBar.getMenuCount(); ++i) {
                menuBar.getMenu(i).removeAll();
            }

            // Populate all the menus described in the JSON
            rootNode.forEach( node -> addMenuBarNode(spwing, menuBar, node ));

            // Remove any menus from the menu bar that are still empty unless
            // they are dynamic menus
            for (int i = menuBar.getMenuCount() - 1; i >= 0; --i) {
                final JMenu menu = menuBar.getMenu(i);
                if (menu.getItemCount() == 0 && !dynamicMenus.contains(menu)) {
                    menuBar.remove(i);
                }
            }

        } catch(Exception e) {
            log.warn( "Building menu for {} (from class {}) failed, menu bar untouched",
                    menuName,
                    menuSourceClass.getName() );
            throw new RuntimeException(e);
        }
    }


    /**
     * Get the ID from the node. Default to the empty string, which
     * is valid only in separator items.
     *
     * @param node The node
     * @return The ID
     */
    private String getId(JsonNode node) {
        if (node.has("id")) {
            return node.get("id").asText();
        } else {
            return "";
        }
    }


    /**
     * Get the menu from the existing menu bar. This allows
     * us to repopulate an existing menu, rather than recreating
     * it. This minimizes flicker on the menu bar when changing
     * menu contents.
     *
     * @param menuBar The menu bar
     * @param menuName The menu to find
     * @return The menu, if it exists
     */
    private JMenu getMenu(JMenuBar menuBar, String menuName) {
        for (int i = 0; i < menuBar.getMenuCount(); ++i) {
            final JMenu menu = menuBar.getMenu(i);
            if (menu.getText().equals(menuName)) {
                return menu;
            }
        }
        return null;
    }


    /**
     * Add a menu bar node. This has to be a menu node.
     *
     * @param spwing The {@link Spwing} instance
     * @param menuBar The menu bar we're populating
     * @param node The menu node
     */
    private void addMenuBarNode(Spwing spwing, JMenuBar menuBar, JsonNode node) {

        // Get the id, make sure it's a valid menu name
        String menuID = getId(node);
        if (!menuID.startsWith("m_")) {
            log.error("Malformed menu node id; should start with \"m_\"");
        } else {
            String menuName = spwing.getApplicationContext().getMessage(menuID, null, menuID, Locale.getDefault());
            JMenu existingMenuBarMenu = getMenu(menuBar, menuName);
            JMenu menu = Objects.nonNull(existingMenuBarMenu) ? existingMenuBarMenu : addNewMenu(menuBar, menuName);

            // Populate the menu
            buildMenu(spwing, menuID, menu, node);
        }
    }


    /**
     * Add a new menu to the menu bar
     * @param menuBar The menu bar
     * @param menuName The menu name
     * @return The created menu
     */
    private JMenu addNewMenu(JMenuBar menuBar, String menuName) {
        JMenu menu;
        menu = new JMenu(menuName);
        menuBar.add(menu);
        return menu;
    }


    /**
     * Add the items to a menu.
     *
     * @param spwing The {@link Spwing} instance
     * @param menu
     * @param node
     */
    private void buildMenu(Spwing spwing, String menuID, JMenu menu, JsonNode node) {

        Map<String,JMenuItem> mappedMenuItems = new HashMap<>();

        // See if the menu is dynamic
        boolean isDynamicMenu = !node.has("items") ||
                !node.get("items").isArray() ||
                node.get("items").size() == 0;

        if (isDynamicMenu) {
            dynamicMenus.add(menu);
        } else {
            node.get("items").forEach(itemNode -> {
                String itemID = getId(itemNode);
                if (Objects.isNull(itemID) || itemID.isBlank() || itemID.equals("-")) {
                    menu.addSeparator();
                } else {
                    if (itemID.startsWith("m_")) {
                        String itemName = spwing.getApplicationContext().getMessage(itemID, null, itemID, Locale.getDefault());
                        JMenu subMenu = new JMenu(itemName);
                        menu.add(subMenu);
                        buildMenu(spwing, itemID, subMenu, itemNode);
                    } else {
                        final JMenuItem menuItem = buildItem(spwing, itemNode);
                        menu.add(menuItem);
                        mappedMenuItems.put(itemID,menuItem);
                    }
                }
            });
        }


        // Add the listener for the menu
        menu.addMenuListener( new MenuSelectedAdapter(spwing, menuID, menu, mappedMenuItems, isDynamicMenu));

        // Remember this menu id
        menuIDs.add(menuID);
    }


    /**
     * Build an item within a menu.
     *
     * @param spwing
     * @param node
     * @return
     */
    private JMenuItem buildItem(Spwing spwing, JsonNode node) {
        JMenuItem menuItem = new JMenuItem();
        String id = getId(node);
        String itemName = spwing.getApplicationContext().getMessage(id, null, id, Locale.getDefault());
        menuItem.setText(itemName);
        if (node.has("accelerator")) {
            String accelerator = node.get("accelerator").asText();
            menuItem.setAccelerator(KeyStroke.getKeyStroke(accelerator));
        }
        commandMap.put(menuItem, id);
        menuItem.addActionListener(new MenuItemActionAdapter(spwing, id));
        return menuItem;
    }
}
