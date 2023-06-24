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


import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.util.EnumerationStream;
import com.hablutzel.spwing.util.FlexpressionParser;
import com.hablutzel.spwing.util.ResourceUtils;
import com.hablutzel.spwing.view.adapter.JLabelEventAdapter;
import com.hablutzel.spwing.view.adapter.JTextFieldEventAdapter;
import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.ViewPropertyBinder;
import com.hablutzel.spwing.view.factory.ComponentFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * {@link SVWFListener} provides the functionality of the SVWF parser.
 * It walks the parse tree, reacting to the elements in the tree and
 * taking appropriate action.
 *
 * @author Bob Hablutzel
 */
@Slf4j
@RequiredArgsConstructor
public class SVWFListener extends SpwingViewFileBaseListener {
    private final Object modelObject;
    private final Object controllerObject;
    private final ApplicationContext applicationContext;
    private final ConversionService conversionService;
    private final DocumentEventDispatcher documentEventDispatcher;
    private final SVWFParseContext parseContext;

    private SpwingViewFileParser.SvwfFileContext svwfFileContext = null;

    @Getter
    private boolean cleanParse = true;

    private Container layoutContainer = null;

    /**
     * Routine to get text from a {@link ParserRuleContext} instance.
     *
     * @param ctx The ParserRuleContext instance
     * @return The text for that context. Note this might include
     * whitespace including linefeeds, so be ready for that
     */
    public static String textFromContext(ParserRuleContext ctx) {

        // You would think that we could just call ctx.getText().
        // However, that doesn't work because any tokens that are
        // diverted to the hidden channel (e.g. whitespace) will
        // be excluded; therefore the text would not look as it did
        // to the author.
        //
        // To address this, we get the start and stop tokens, and
        // get the text between those two (inclusive)
        Token start = ctx.getStart();
        Token stop = ctx.getStop();

        if (start == null || stop == null) {
            return "";
        }

        // Get the underlying character stream
        CharStream stream = start.getInputStream();

        // Create an interval that start at the beginning of the
        // start token and continues through the end of the stop token
        Interval interval = new Interval(start.getStartIndex(), stop.getStopIndex());

        // Get the text and return it. (Note it might contain whitespace like
        // linefeeds, so be careful of that!)
        return stream.getText(interval);
    }

    /**
     * Routine to get the contexts of a string literal, which
     * strips the quotes and unescapes the characters
     */
    public static String stripStringLiteral(String text) {
        // Remove quotes and unescape the text (using Java rules)
        text = text.substring(1, text.length() - 1);
        return StringEscapeUtils.unescapeJava(text);
    }

    /**
     * Routine to get the contexts of a string literal, which
     * strips the quotes and unescapes the characters
     */
    @SuppressWarnings("unused")
    public static String stripStringLiteral(Token token) {
        return stripStringLiteral(token.getText());
    }

    /**
     * Return a long value from a Integer_Literal token
     * This will deal with both decimal and hex values
     */
    @SuppressWarnings("unused")
    public static long getLongValue(TerminalNode node, long defaultValue) {
        int radix = 10;
        int offset = 0;
        if (node != null) {
            String text = node.getText();
            if (text.startsWith("0x")) {
                radix = 16;
                offset = 2;
            }
            return Long.parseLong(text, offset, text.length(), radix);
        } else {
            return defaultValue;
        }
    }

    @Override
    public void enterSvwfFile(SpwingViewFileParser.SvwfFileContext ctx) {
        this.svwfFileContext = ctx;
    }

    @Override
    public void enterInvokeStatement(SpwingViewFileParser.InvokeStatementContext ctx) {
        String methodName = ctx.methodName.getText();

        final boolean isModel = tokenEquals("model", ctx.target);
        final Object targetObject = isModel ? modelObject : controllerObject;

        // See if we can find that method on the target
        if (Objects.nonNull(targetObject)) {
            ReflectiveInvoker.invoke(applicationContext, targetObject, methodName, Map.of(
                    SVWFListener.class, () -> this,
                    SpwingViewFileParser.SvwfFileContext.class, () -> svwfFileContext,
                    SVWFParseContext.class, () -> parseContext
            ));
        } else {
            log.warn("{} object not found, invoke skipped", ctx.target.getText());
        }
    }


    /**
     * A root clause defines the root of an expression. This can be
     * the model object (which is the default of the root clause is
     * omitted), the controller object, or an arbitrary bean given by
     * name
     * @param rootClauseContext
     * @return
     */
    private Object getRootClauseObject(SpwingViewFileParser.RootClauseContext rootClauseContext) {
        if (Objects.nonNull(rootClauseContext)) {

            // The target can be either a keyword (model or controller) or a String
            if (Objects.nonNull(rootClauseContext.m)) {
                return modelObject;
            } else if (Objects.nonNull(rootClauseContext.c)) {
                return controllerObject;
            } else if (Objects.nonNull(rootClauseContext.l)) {
                final String beanName = stripStringLiteral(rootClauseContext.l);
                try {
                    return applicationContext.getBean(beanName);
                } catch (BeansException e) {
                    log.error("Bean {} was not found in the context.", beanName);
                    cleanParse = false;
                    return null;
                }
            } else {
                // The root clause was specified as "()". This denotes no
                // root for the expression (which will likely be a literal or
                // flexpression). In this case, we use null as the root
                // Generally speaking this won't be needed, as the literals
                // or flexpressions will ignore the root object, but in some
                // cases there might be a need, so we support it.
                return null;
            }
        } else {
            return modelObject;
        }
    }



    // Perform the binding for a single object.
    private void bindSingleObject( final SpwingViewFileParser.SingleTargetClauseContext ctx,
                                    final Accessor accessor,
                                    final List<String> triggers ) {
        String identifier = ctx.target.getText();
        String property = ctx.property.getText();

        // See if we know this identifier
        Object viewObject = parseContext.getKnownComponents().get(identifier);
        if (viewObject instanceof Component component) {

            BeanWrapper viewBeanWrapper = parseContext.getViewPropertyBinder().beanWrapperFor(viewObject);
            if (viewBeanWrapper.isWritableProperty(property)) {
                if ("name".equals(property)) {
                    log.error( "\"name\" property is not eligible for binding");
                    cleanParse = false;
                } else {
                    parseContext.getViewPropertyBinder().bind(component, property, accessor, triggers);
                }
            } else {
                log.error("Cannot bind to property {}.{}", identifier, property);
                cleanParse = false;
            }
        } else {
            log.error("Component {} not found for, or is not a component", identifier);
            cleanParse = false;
        }
    }


    /**
     * Get a list of triggers from the context, by looking for all the
     * string elements, getting the element token, and stripping the
     * quotes from that token text. This will return an empty list of
     * the clause was omitted.
     * @param ctx The contextx
     * @return The (possibly empty) list of triggers.
     */
    private List<String> getTriggers(SpwingViewFileParser.TriggerClauseContext ctx) {
        if (Objects.isNull(ctx)) {
            return List.of();
        } else {
            return ctx.stringElement().stream()
                    .map(stringElementContext -> stringElementContext.element)
                    .map(SVWFListener::stripStringLiteral)
                    .toList();
        }
    }



    private void bindGroup( final SpwingViewFileParser.GroupTargetClauseContext ctx,
                            final Accessor accessor,
                            final List<String> triggers ) {

        // Get the property. This should be something shared across all the
        // components being referenced
        String targetProperty = ctx.property.getText();
        List<String> identifiedElements = ctx.identifierElement().stream()
                .map(identifierCtx -> identifierCtx.element)
                .map(Token::getText)
                .toList();

        // Now translate the identified elements to components
        final List<Component> targetElements = new ArrayList<>();
        identifiedElements.forEach( element -> {
            if (parseContext.getKnownComponents().get(element) instanceof Component component) {
                targetElements.add(component);
            } else {
                log.error("Unknown or invalid group component: {}", element);
                cleanParse = false;
            }
        });

        // If they are all buttons, create a button group
        if (targetElements.stream().allMatch(component -> component instanceof AbstractButton)) {
            final List<AbstractButton> buttonList = targetElements.stream().map(component -> (AbstractButton) component).toList();
            parseContext.getViewPropertyBinder().bindButtonGroup(buttonList, targetProperty, accessor, triggers );
        }
    }


    @Override
    public void enterBindStatement( final SpwingViewFileParser.BindStatementContext ctx) {

        // Deal with the common parts of the bind statement: the root object (if specified),
        // the expression, and the potential triggers
        Object rootObject = getRootClauseObject(ctx.rootClause());
        String expression = stripStringLiteral(ctx.expression);
        List<String> triggers = getTriggers(ctx.triggerClause());

        // Get the property accessor for this element
        Accessor accessor = parseContext.getViewPropertyBinder().toAccessor(rootObject, expression);

        // See if we have a group or single binding statement
        if (Objects.nonNull(ctx.target.single)) {
            bindSingleObject( ctx.target.single, accessor, triggers );
        } else {
            bindGroup( ctx.target.group, accessor, triggers );
        }

    }

    private boolean tokenEquals(String value, Token token) {
        return Objects.nonNull(token) && value.equals(token.getText());
    }

    @Override
    public void enterDefaultStatement(SpwingViewFileParser.DefaultStatementContext ctx) {
        String classAlias = ctx.classAlias.getText();
        if (parseContext.getDefinitionMap().containsKey(classAlias)) {
            SVWFParseContext.ElementDefinition elementDefinition = parseContext.getDefinitionMap().get(classAlias);
            Class<?> targetClass = elementDefinition.elementClass();

            Map<String, Object> defaultMap = new HashMap<>();
            parseContext.getDefaultValues().put(targetClass, defaultMap);
            // Create a dummy instance so we can get the right property types
            ctx.kvPair().forEach(kvPairContext -> handleKVPairForDefault(kvPairContext, targetClass, defaultMap));
        }
    }

    private void handleKVPairForDefault(final SpwingViewFileParser.KvPairContext kvPairContext,
                                        final Class<?> targetClass,
                                        final Map<String, Object> defaultMap) {
        final String propertyName = kvPairContext.k.getText();
        final DeclaredValue declaredValue = new DeclaredValue(kvPairContext.v);

        defaultMap.put(propertyName, declaredValue.getValue(getPropertyTypeFromClass(targetClass, propertyName)));
    }

    private Class<?> getPropertyTypeFromClass(Class<?> targetClass, String propertyName) {
        String potentialMethodName = "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        return Arrays.stream(targetClass.getMethods())
                .filter(method -> method.getName().equals(potentialMethodName) &&
                        method.getParameterCount() == 1)
                .map(method -> method.getParameterTypes()[0])
                .findFirst()
                .orElse(null);
    }

    @Override
    public void enterColorStatement(SpwingViewFileParser.ColorStatementContext ctx) {
        String colorName = ctx.name.getText();
        SpwingViewFileParser.ColorDefinitionContext colorDefinitionContext = ctx.colorDefinition();
        if (Objects.nonNull(colorDefinitionContext.intColorSpec())) {
            defineIntColor(colorName, colorDefinitionContext.intColorSpec());
        } else if (Objects.nonNull(colorDefinitionContext.floatColorSpec())) {
            defineFloatColor(colorName, colorDefinitionContext.floatColorSpec());
        } else {
            defineBitfieldColor(colorName, colorDefinitionContext.bitFieldColorSpec());
        }
    }

    private void defineIntColor(final String colorName,
                                final SpwingViewFileParser.IntColorSpecContext intColorSpecContext) {
        if (Objects.nonNull(intColorSpecContext.alpha)) {
            parseContext.getKnownComponents().put(colorName,
                    new Color(getIntValue(intColorSpecContext.red),
                            getIntValue(intColorSpecContext.green),
                            getIntValue(intColorSpecContext.blue),
                            getIntValue(intColorSpecContext.alpha)));
        } else {
            parseContext.getKnownComponents().put(colorName,
                    new Color(getIntValue(intColorSpecContext.red),
                            getIntValue(intColorSpecContext.green),
                            getIntValue(intColorSpecContext.blue)));
        }
    }

    private void defineFloatColor(final String colorName,
                                  final SpwingViewFileParser.FloatColorSpecContext floatColorSpecContext) {
        if (Objects.nonNull(floatColorSpecContext.alphaf)) {
            parseContext.getKnownComponents().put(colorName,
                    new Color(getFloatValue(floatColorSpecContext.redf),
                            getFloatValue(floatColorSpecContext.greenf),
                            getFloatValue(floatColorSpecContext.bluef),
                            getFloatValue(floatColorSpecContext.alphaf)));
        } else {
            parseContext.getKnownComponents().put(colorName,
                    new Color(getFloatValue(floatColorSpecContext.redf),
                            getFloatValue(floatColorSpecContext.greenf),
                            getFloatValue(floatColorSpecContext.bluef)));
        }
    }

    private void defineBitfieldColor(final String colorName,
                                     final SpwingViewFileParser.BitFieldColorSpecContext bitFieldColorSpecContext) {
        int colorValue = getIntValue(bitFieldColorSpecContext.bitField);
        parseContext.getKnownComponents().put(colorName,
                new Color(colorValue, getBooleanValue(bitFieldColorSpecContext.hasAlpha)));
    }

    @Override
    public void enterImageStatement(SpwingViewFileParser.ImageStatementContext ctx) {
        String imageName = ctx.name.getText();
        if (Objects.nonNull(ctx.imageSpec().resourceName)) {
            String resourceName = stripStringLiteral(ctx.imageSpec().resourceName);
            Object target = (Objects.nonNull(ctx.imageSpec().target) && "controller".equals(ctx.imageSpec().target.getText()))
                    ? controllerObject
                    : modelObject;

            ResourceUtils resourceUtils = applicationContext.getBean(ResourceUtils.class);

            String baseName = FilenameUtils.getBaseName(resourceName);
            String extension = FilenameUtils.getExtension(resourceName);

            try (InputStream in = resourceUtils.getPlatformResource(target.getClass(), baseName, extension)) {
                BufferedImage image = ImageIO.read(in);
                parseContext.getKnownComponents().put(imageName, image);
            } catch (Exception e) {
                log.warn("Unable to read resource file for image {}", resourceName, e);
            }
        } else {
            String urlName = stripStringLiteral(ctx.imageSpec().url);
            try {
                URL url = new URL(urlName);
                parseContext.getKnownComponents().put(imageName, ImageIO.read(url));
            } catch (Exception e) {
                log.warn("Unable to read resource file for image {}", urlName, e);
            }
        }
        super.enterImageStatement(ctx);
    }

    @Override
    public void enterComponentStatement(SpwingViewFileParser.ComponentStatementContext ctx) {
        final String componentName = ctx.componentName.getText();
        final String classAlias = ctx.classAlias.getText();
        if (parseContext.getKnownComponents().containsKey(componentName)) {
            log.warn("In SWVF file, {} is already defined. Additional definition ignored", componentName);
        } else {
            SVWFParseContext.ElementDefinition elementDefinition = parseContext.getDefinitionMap().get(classAlias);
            if (Objects.nonNull(elementDefinition)) {
                final Function<String, Object> creationFunction = elementDefinition.createFunction();
                final Object createdElement = creationFunction.apply(componentName);
                final Class<?> definedObjectType = createdElement.getClass();
                parseContext.getKnownComponents().put(componentName, createdElement);

                BeanWrapper activeComponent = new BeanWrapperImpl(createdElement);
                activeComponent.setConversionService(conversionService);

                registerAdapter(componentName, createdElement);

                // Apply defaults for any class that is in the target class hierarchy
                parseContext.getDefaultValues().forEach((clazz, defaults) -> {
                    if (clazz.isAssignableFrom(definedObjectType)) {
                        defaults.forEach(activeComponent::setPropertyValue);
                    }
                });

                ctx.kvPair().forEach(kvPairContext -> {
                    handleKVPairForComponent(activeComponent, kvPairContext);
                });
            } else {
                log.error("Cannot create {}, no such class or class is abstract", classAlias);
                cleanParse = false;
            }
        }
    }

    private void handleKVPairForComponent(BeanWrapper activeComponent, SpwingViewFileParser.KvPairContext kvPairContext) {
        final String propertyName = kvPairContext.k.getText();
        final DeclaredValue declaredValue = new DeclaredValue(kvPairContext.v);

        // Special case the "value" property - we track that internally
        if ("value".equals(propertyName)) {
            parseContext.getValueMap().put(activeComponent.getWrappedInstance(), declaredValue::getValue);
        } else {
            final Class<?> propertyType = activeComponent.getPropertyType(propertyName);
            activeComponent.setPropertyValue(propertyName, declaredValue.getValue(propertyType));
        }
    }


    @RequiredArgsConstructor
    private final class DeclaredValue {

        private final SpwingViewFileParser.PairValueContext valueContext;

        public Object getValue(Class<?> expectedType) {
            if (Objects.nonNull(valueContext.bool)) {
                return conversionService.convert(valueContext.bool.getText().equals("true"), expectedType);
            } else if (Objects.nonNull(valueContext.string)) {
                return conversionService.convert(stripStringLiteral(valueContext.string), expectedType);
            } else if (Objects.nonNull(valueContext.size)) {
                return parseDimension(valueContext.size);
            } else if (Objects.nonNull(valueContext.id)) {
                return conversionService.convert(parseContext.getKnownComponents().get(valueContext.id.getText()), expectedType);
            } else {
                return conversionService.convert(getIntValue(valueContext.integer), expectedType);
            }
        }
    }

    private void registerAdapter(final String componentName, final Object component) {
        if (component instanceof JLabel label) {
            documentEventDispatcher.registerEventAdapter(componentName, new JLabelEventAdapter(label));
        } else if (component instanceof JTextField textField) {
            documentEventDispatcher.registerEventAdapter(componentName, new JTextFieldEventAdapter(textField));
        }
    }

    private Dimension parseDimension(SpwingViewFileParser.DimensionContext dimensionContext) {
        return new Dimension(getIntValue(dimensionContext.width), getIntValue(dimensionContext.height));
    }

    @Override
    public void enterLayoutStatement(SpwingViewFileParser.LayoutStatementContext ctx) {
        String componentToLayout = ctx.component.getText();
        if (parseContext.getKnownComponents().get(componentToLayout) instanceof Container container) {
            this.layoutContainer = container;
        } else {
            log.warn("Component {} is not present or not a container", componentToLayout);
        }
    }

    @Override
    public void exitLayoutStatement(SpwingViewFileParser.LayoutStatementContext ctx) {
        if (this.layoutContainer instanceof JFrame jFrame) {
            jFrame.pack();
        }
        this.layoutContainer = null;
    }

    private Object getNamedElement(Token elementToken) {
        final String elementName = elementToken.getText();
        Object rawElement = parseContext.getKnownComponents().get(elementName);

        // Special case unwrapped icons
        return rawElement instanceof Icon ? wrapIcon(elementName, (Icon) rawElement) : rawElement;
    }

    private void onNamedComponent(Token token, Consumer<Component> consumer, boolean wrapButtonGroup) {
        final Object element = getNamedElement(token);
        if (element instanceof Component component) {
            consumer.accept(component);
        } else if (element instanceof ButtonGroup buttonGroup) {
            if (wrapButtonGroup) {
                JPanel panel = parseContext.getComponentFactory().newJPanel("_anon_" + token.getText());
                EnumerationStream.stream(buttonGroup.getElements()).forEach(panel::add);
                consumer.accept(panel);
            } else {
                EnumerationStream.stream(buttonGroup.getElements()).forEach(consumer);
            }
        } else {
            log.warn("Could not find element {} as a component or group", token.getText());
        }
    }

    @Override
    public void enterGroupStatement(SpwingViewFileParser.GroupStatementContext ctx) {
        String groupName = ctx.groupName.getText();
        ComponentFactory componentFactory = parseContext.getComponentFactory();
        Map<String, Object> knownComponents = parseContext.getKnownComponents();
        ButtonGroup buttonGroup = componentFactory.newButtonGroup(groupName);

        for (SpwingViewFileParser.IdentifierElementContext identifierElementContext : ctx.identifierElement()) {
            String elementName = identifierElementContext.element.getText();
            if (knownComponents.containsKey(elementName) &&
                    knownComponents.get(elementName) instanceof AbstractButton button) {
                buttonGroup.add(button);
            } else {
                log.error("{} is not present or not a button", elementName);
            }
        }
    }

    @Override
    public void enterFlowLayoutDescription(SpwingViewFileParser.FlowLayoutDescriptionContext ctx) {
        if (Objects.nonNull(layoutContainer)) {
            if (layoutContainer instanceof JFrame frame) {
                frame.getContentPane().setLayout(new FlowLayout());
            } else {
                layoutContainer.setLayout(new FlowLayout());
            }

            ctx.identifierElement().forEach(flowElementContext -> {
                onNamedComponent(flowElementContext.element, layoutContainer::add, false);
            });
        }
    }

    @Override
    public void enterBoxLayoutDescription(SpwingViewFileParser.BoxLayoutDescriptionContext ctx) {
        if (Objects.nonNull(layoutContainer)) {
            int axis = tokenEquals("vertical", ctx.orientation) ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS;
            setLayout(container -> new BoxLayout(container, axis));

            ctx.boxElement().forEach(boxElementContext -> {

                if (Objects.nonNull(boxElementContext.identifierElement().element)) {
                    onNamedComponent(boxElementContext.identifierElement().element, layoutContainer::add, false);
                } else if (Objects.nonNull(boxElementContext.horizontalGlue)) {
                    layoutContainer.add(Box.createHorizontalGlue());
                } else if (Objects.nonNull(boxElementContext.verticalGlue)) {
                    layoutContainer.add(Box.createVerticalGlue());
                } else if (Objects.nonNull(boxElementContext.rigidArea)) {
                    layoutContainer.add(Box.createRigidArea(parseDimension(boxElementContext.size)));
                } else if (Objects.nonNull(boxElementContext.filler)) {

                    Map<String, Dimension> dimensions = new java.util.HashMap<>(Map.of(
                            "minSize", new Dimension(0, 0),
                            "maxSize", new Dimension(0, 0),
                            "prefSize", new Dimension(0, 0)
                    ));

                    boxElementContext.fillerSpec().forEach(fillerSpecContext -> {
                        String name = fillerSpecContext.name.getText();
                        Dimension size = parseDimension(fillerSpecContext.size);
                        dimensions.put(name, size);
                    });

                    layoutContainer.add(new Box.Filler(dimensions.get("minSize"),
                            dimensions.get("prefSize"),
                            dimensions.get("maxSize")));
                }

            });

        }
    }

    @Override
    public void enterBorderLayoutDescription(SpwingViewFileParser.BorderLayoutDescriptionContext ctx) {
        if (Objects.nonNull(layoutContainer)) {
            setLayout(container -> new BorderLayout());

            ctx.borderElement().forEach(borderElementContext -> {
                String direction = borderElementContext.direction.getText();

                // Special case - if we get an Icon, then wrap it in a label for display
                Object element = getNamedElement(borderElementContext.identifierElement().element);
                onNamedComponent(borderElementContext.identifierElement().element, component -> {
                    switch (direction) {
                        case "east" -> layoutContainer.add(component, BorderLayout.EAST);
                        case "west" -> layoutContainer.add(component, BorderLayout.WEST);
                        case "north" -> layoutContainer.add(component, BorderLayout.NORTH);
                        case "south" -> layoutContainer.add(component, BorderLayout.SOUTH);
                        case "center" -> layoutContainer.add(component, BorderLayout.CENTER);
                    }
                }, true);
            });
        }
    }

    private void setLayout(Function<Container, LayoutManager> layoutManagerCreationFunction) {
        if (layoutContainer instanceof JFrame frame) {
            frame.getContentPane().setLayout(layoutManagerCreationFunction.apply(frame.getContentPane()));
        } else {
            layoutContainer.setLayout(layoutManagerCreationFunction.apply(layoutContainer));
        }
    }

    private JLabel wrapIcon(final String componentName, final Icon icon) {
        JLabel result = new JLabel();
        result.setIcon(icon);
        result.setName(componentName);
        documentEventDispatcher.registerEventAdapter(componentName, new JLabelEventAdapter(result));
        return result;
    }

    /**
     * Return an int value from a Integer_Literal token.
     * This will deal with both decimal and hex values
     */
    @SuppressWarnings("unused")
    public int getIntValue(Token token) {
        int radix = 10;
        int offset = 0;
        String text = token.getText();
        if (text.startsWith("0x")) {
            radix = 16;
            offset = 2;
        }
        return Integer.parseInt(text, offset, text.length(), radix);
    }


    public boolean getBooleanValue(Token token) {
        return tokenEquals("true", token);
    }

    public float getFloatValue(Token token) {
        return Float.parseFloat(token.getText());
    }


    public Component rootComponent() {
        return parseContext.getKnownComponents().values().stream()
                .filter(component -> component instanceof JFrame)
                .map(component -> (JFrame) component)
                .findFirst()
                .orElse(null);
    }

}
