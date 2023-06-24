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

package com.hablutzel.spwing.view.factory.svwf;

import com.hablutzel.spwing.annotations.SVWFComponentFactory;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.view.bind.ViewPropertyBinder;
import com.hablutzel.spwing.view.factory.ComponentFactory;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.ConversionService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;


public final class SVWFParseContext {

    public record ElementDefinition(Class<?> elementClass, Function<String,Object> createFunction ) {}


    @Getter
    private final ApplicationContext applicationContext;

    @Getter(AccessLevel.MODULE)
    private final Map<String, Object> knownComponents = new HashMap<>();

    @Getter(AccessLevel.MODULE)
    private final Map<String, ElementDefinition> definitionMap = new HashMap<>();

    @Getter(AccessLevel.MODULE)
    private final Map<Class<?>, Map<String, Object>> defaultValues = new HashMap<>();

    @Getter(AccessLevel.MODULE)
    private final Map<Object, Function<Class<?>, Object>> valueMap = new HashMap<>();

    @Getter
    private final ComponentFactory componentFactory;

    @Getter
    private final ViewPropertyBinder viewPropertyBinder;

    public SVWFParseContext(final Object modelObject,
                            final Object controllerObject,
                            final ApplicationContext applicationContext,
                            final DocumentEventDispatcher documentEventDispatcher,
                            final ConversionService conversionService) {
        this.applicationContext = applicationContext;
        this.componentFactory = new ComponentFactory(modelObject, controllerObject, documentEventDispatcher, knownComponents);
        this.viewPropertyBinder = new ViewPropertyBinder(applicationContext, documentEventDispatcher, conversionService, valueMap);

        this.registerKnownSwingClasses();
        this.registerPredefinedComponents();

    }


    public void registerKnownSwingClasses() {
        definitionMap.put("AbstractButton", new ElementDefinition(AbstractButton.class, null));
//        definitionMap.put("Box", javax.swing.Box.class);
//        definitionMap.put("CellRendererPane", javax.swing.CellRendererPane.class);
        definitionMap.put("Component", new ElementDefinition(Component.class, null ));
        definitionMap.put("Container", new ElementDefinition(Container.class, componentFactory::newContainer ));
//        definitionMap.put("DateEditor", javax.swing.JSpinner.DateEditor.class);
//        definitionMap.put("DefaultEditor", javax.swing.JSpinner.DefaultEditor.class);
//        definitionMap.put("DefaultListCellRenderer", javax.swing.DefaultListCellRenderer.class);
        definitionMap.put("Frame", new ElementDefinition(Frame.class, null ));
        definitionMap.put("ImageIcon", new ElementDefinition(ImageIcon.class, componentFactory::newImageIcon ));
        definitionMap.put("JButton", new ElementDefinition(JButton.class, componentFactory::newJButton));
        definitionMap.put("JCheckBox", new ElementDefinition(JCheckBox.class, componentFactory::newJCheckBox ));
//        definitionMap.put("JCheckBoxMenuItem", javax.swing.JCheckBoxMenuItem.class);
//        definitionMap.put("JColorChooser", javax.swing.JColorChooser.class);
//        definitionMap.put("JComboBox", javax.swing.JComboBox.class);
//        definitionMap.put("JDesktopIcon", javax.swing.JInternalFrame.JDesktopIcon.class);
//        definitionMap.put("JDesktopPane", javax.swing.JDesktopPane.class);
//        definitionMap.put("JEditorPane", javax.swing.JEditorPane.class);
//        definitionMap.put("JFileChooser", javax.swing.JFileChooser.class);
//        definitionMap.put("JFormattedTextField", javax.swing.JFormattedTextField.class);
        definitionMap.put("JFrame", new ElementDefinition(JFrame.class, componentFactory::newJFrame));
//        definitionMap.put("JInternalFrame", javax.swing.JInternalFrame.class);
        definitionMap.put("JLabel", new ElementDefinition(JLabel.class, componentFactory::newJLabel));
//        definitionMap.put("JLayer", javax.swing.JLayer.class);
//        definitionMap.put("JLayeredPane", javax.swing.JLayeredPane.class);
//        definitionMap.put("JList", javax.swing.JList.class);
//        definitionMap.put("JMenu", javax.swing.JMenu.class);
//        definitionMap.put("JMenuBar", javax.swing.JMenuBar.class);
//        definitionMap.put("JMenuItem", javax.swing.JMenuItem.class);
//        definitionMap.put("JOptionPane", javax.swing.JOptionPane.class);
        definitionMap.put("JPanel", new ElementDefinition(JPanel.class, componentFactory::newJPanel));
//        definitionMap.put("JPasswordField", javax.swing.JPasswordField.class);
//        definitionMap.put("JPopupMenu", javax.swing.JPopupMenu.class);
//        definitionMap.put("JProgressBar", javax.swing.JProgressBar.class);
        definitionMap.put("JRadioButton", new ElementDefinition(JRadioButton.class, componentFactory::newJRadioButton));
//        definitionMap.put("JRadioButtonMenuItem", javax.swing.JRadioButtonMenuItem.class);
//        definitionMap.put("JRootPane", javax.swing.JRootPane.class);
//        definitionMap.put("JScrollBar", javax.swing.JScrollBar.class);
//        definitionMap.put("JScrollPane", javax.swing.JScrollPane.class);
//        definitionMap.put("JSeparator", javax.swing.JSeparator.class);
//        definitionMap.put("JSlider", javax.swing.JSlider.class);
//        definitionMap.put("JSpinner", javax.swing.JSpinner.class);
//        definitionMap.put("JSplitPane", javax.swing.JSplitPane.class);
//        definitionMap.put("JTabbedPane", javax.swing.JTabbedPane.class);
//        definitionMap.put("JTable", javax.swing.JTable.class);
//        definitionMap.put("JTextArea", javax.swing.JTextArea.class);
        definitionMap.put("JTextComponent", new ElementDefinition(JTextComponent.class, null));
        definitionMap.put("JTextField", new ElementDefinition(JTextField.class, componentFactory::newJTextField));
//        definitionMap.put("JTextPane", javax.swing.JTextPane.class);
//        definitionMap.put("JToggleButton", javax.swing.JToggleButton.class);
//        definitionMap.put("JToolBar", javax.swing.JToolBar.class);
//        definitionMap.put("JToolTip", javax.swing.JToolTip.class);
//        definitionMap.put("JTree", javax.swing.JTree.class);
//        definitionMap.put("JViewport", javax.swing.JViewport.class);
//        definitionMap.put("ListEditor", javax.swing.JSpinner.ListEditor.class);
//        definitionMap.put("NumberEditor", javax.swing.JSpinner.NumberEditor.class);
//        definitionMap.put("PopupSeparator", javax.swing.JPopupMenu.Separator.class);
//        definitionMap.put("ToolbarSeparator", javax.swing.JToolBar.Separator.class);
//        definitionMap.put("UIResource", javax.swing.DefaultListCellRenderer.UIResource.class);
        definitionMap.put("Window", new ElementDefinition(Window.class, null ));
    }

    public void registerPredefinedComponents() {
        knownComponents.put("onePixelBlackLineBorder", new LineBorder(Color.black, 1));
        knownComponents.put("twoPixelBlackLineBorder", new LineBorder(Color.black, 2));
        knownComponents.put("threePixelBlackLineBorder", new LineBorder(Color.black, 3));
        knownComponents.put("fourPixelBlackLineBorder", new LineBorder(Color.black, 4));
        knownComponents.put("onePixelWhiteLineBorder", new LineBorder(Color.white, 1));
        knownComponents.put("twoPixelWhiteLineBorder", new LineBorder(Color.white, 2));
        knownComponents.put("threePixelWhiteLineBorder", new LineBorder(Color.white, 3));
        knownComponents.put("fourPixelWhiteLineBorder", new LineBorder(Color.white, 4));
        knownComponents.put("onePixelEmptyBorder", new EmptyBorder(1, 1, 1, 1));
        knownComponents.put("fivePixelEmptyBorder", new EmptyBorder(5, 5, 5, 5));
        knownComponents.put("tenPixelEmptyBorder", new EmptyBorder(10, 10, 10, 10));

        // If there are any SVWFComponentFactory instances available, call them to add any
        // document or application specific components to the predefined component list
        this.addToPredefinedComponents(applicationContext);
    }



    private void addToPredefinedComponents(final ApplicationContext applicationContext) {

        Collection<Object> factoryBeans = applicationContext.getBeansWithAnnotation(SVWFComponentFactory.class).values();
        for (Object factoryBean : factoryBeans) {

            SVWFComponentFactory svwfComponentFactory = AnnotatedElementUtils.findMergedAnnotation(factoryBean.getClass(), SVWFComponentFactory.class);
            if (Objects.nonNull(svwfComponentFactory)) {
                String factoryMethodName = svwfComponentFactory.componentMethod();
                ReflectiveInvoker.invoke(applicationContext, factoryBean, factoryMethodName, Map.of(
                        SVWFParseContext.class, () -> this
                ));
            }
        }
    }

    public void addComponent(String componentName, Object component) {
        knownComponents.put(componentName, component);
    }

    public void addDefinition(String definitionName, ElementDefinition elementDefinition) {
        definitionMap.put(definitionName, elementDefinition);
    }
}
