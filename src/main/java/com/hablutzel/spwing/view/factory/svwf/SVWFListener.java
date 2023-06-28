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
import com.hablutzel.spwing.util.PlatformResourceUtils;
import com.hablutzel.spwing.view.adapter.JLabelEventAdapter;
import com.hablutzel.spwing.view.adapter.JTextFieldEventAdapter;
import com.hablutzel.spwing.view.bind.Accessor;
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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

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

    // TODO change layout processing
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

        final Object targetObject = getRootClauseObject(ctx.root);

        // See if we can find that method on the target
        if (Objects.nonNull(targetObject)) {
            ReflectiveInvoker.invoke(applicationContext, targetObject, methodName, Map.of(
                    SVWFListener.class, () -> this,
                    SpwingViewFileParser.SvwfFileContext.class, () -> svwfFileContext,
                    SVWFParseContext.class, () -> parseContext
            ));
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
            } else if (Objects.nonNull(rootClauseContext.b)) {
                final String beanName = rootClauseContext.b.getText();
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


    /**
     * Bind a single Swing component to a model / controller property.
     * This will <ul>
     *     <li>Immediately set the value of the component based on the model</li>
     *     <li>Set up an update of the control when events fire (if the trigger list is non-empty)</li>
     *     <li>Set up a watcher on the component to update the model when the component
     *     changes value (if the model property is writeable).</li>
     * </ul>
     * @param ctx The parse tree
     * @param accessor The accessor for the model / controller property
     * @param triggers The list of event triggers for an update of the control based on property changes.
     */
    private void bindSingleObject( final SpwingViewFileParser.SingleTargetClauseContext ctx,
                                    final Accessor accessor,
                                    final List<String> triggers ) {

        // Get the identifier and property. The identifier should
        // reference a component created earlier by name - it is the
        // name of the created component
        String identifier = ctx.target.getText();
        String property = ctx.property.getText();

        // See if we know this identifier and make sure it's a Swing component
        Object viewObject = parseContext.getKnownComponents().get(identifier);
        if (viewObject instanceof Component component) {

            // Attempt to bind the property. We bind even if the parse is already
            // invalid, but update based on the new parse.
            cleanParse = parseContext.getViewPropertyBinder().bind( component, property, accessor, triggers) && cleanParse;
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


    /**
     * Bind a group of controls to a property of an object (generally a model/controller
     * object).
     * @param ctx The parse tree
     * @param accessor The model/controller property accessor
     * @param triggers The (possibly empty) list of triggers that signal a change to the property state
     */

    private void bindGroup( final SpwingViewFileParser.GroupTargetClauseContext ctx,
                            final Accessor accessor,
                            final List<String> triggers ) {

        // Get the property. This should be something shared across all the
        // components being referenced
        String targetProperty = ctx.property.getText();

        // Get the name of the elements in the group.
        List<String> identifiedElements = ctx.identifierElement().stream()
                .map(identifierCtx -> identifierCtx.element)
                .map(Token::getText)
                .toList();

        // Find any that are not within the known component map; if there
        // are any log the problem and kill the parse
        final Map<String, Object> knownComponents = parseContext.getKnownComponents();
        List<String> invalidIdentifiers = identifiedElements.stream()
                .filter(name -> !knownComponents.containsKey(name) ||
                        !(knownComponents.get(name) instanceof Component))
                .toList();
        if (invalidIdentifiers.isEmpty()) {

            // Now translate the identified elements to components
            List<Object> targetElements = identifiedElements.stream()
                    .map(knownComponents::get)
                    .toList();

            // See if the group was named. If so, and all elements are components, save this target element list
            if (Objects.nonNull(ctx.groupName)) {

                boolean allComponents = targetElements.stream().allMatch(c -> c instanceof Component);
                if (allComponents) {
                    String groupName = ctx.groupName.getText();
                    knownComponents.put(groupName, new Group(targetElements.stream().map(c -> (Component)c).toList()));
                } else {
                    log.warn("Did not create named group for group that was not all descended from Component" );
                }

            }

            // Bind that group
            parseContext.getViewPropertyBinder().bindGroup( targetElements, targetProperty, accessor, triggers );

        } else {
            log.error("Elements {} were not found for the group.", invalidIdentifiers);
            cleanParse = false;
        }
    }


    /**
     * Handle a "bind" statement, which binds a component to a property
     * of the model (or controller).
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterBindStatement( final SpwingViewFileParser.BindStatementContext ctx) {

        // Deal with the common parts of the bind statement: the root object (if specified),
        // the expression, and the potential triggers
        Object rootObject = getRootClauseObject(ctx.rootClause());
        String expression = stripStringLiteral(ctx.expression);
        List<String> triggers = getTriggers(ctx.triggerClause());

        // Get the property accessor for this element
        Accessor accessor = parseContext.getViewPropertyBinder().toAccessor(rootObject, expression);
        if (Objects.nonNull(accessor)) {

            // See if we have a group or single binding statement
            if (Objects.nonNull(ctx.target.single)) {
                bindSingleObject(ctx.target.single, accessor, triggers);
            } else {
                bindGroup(ctx.target.group, accessor, triggers);
            }
        } else {
            this.cleanParse = false;
        }

    }


    /**
     * Helper routine to check the value of a token against a known
     * value; returns true if the token is non-null and text matches.
     *
     * @param value The value to check against
     * @param token The token
     * @return TRUE if the token is non-null and textually matches
     */
    private boolean tokenEquals(@NonNull final String value, @Nullable final Token token) {
        return Objects.nonNull(token) && value.equals(token.getText());
    }

    @Override
    public void enterDefaultStatement(SpwingViewFileParser.DefaultStatementContext ctx) {
        String classAlias = ctx.classAlias.getText();
        if (parseContext.getDefinitionMap().containsKey(classAlias)) {
            SVWFParseContext.ElementDefinition elementDefinition = parseContext.getDefinitionMap().get(classAlias);
            Class<?> targetClass = elementDefinition.elementClass();

            final Map<String, Object> defaultMap = new HashMap<>();
            parseContext.getDefaultValues().put(targetClass, defaultMap);

            // For each defined default, remember the values
            ctx.kvPair().forEach(kvPairContext -> {
                final String propertyName = kvPairContext.k.getText();
                final DeclaredValue declaredValue = new DeclaredValue(kvPairContext.v);

                defaultMap.put(propertyName, declaredValue.get());
            });
        }
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
            Object target = getRootClauseObject(ctx.imageSpec().root);
            if (Objects.nonNull(target)) {

                String baseName = FilenameUtils.getBaseName(resourceName);
                String extension = FilenameUtils.getExtension(resourceName);

                try (InputStream in = PlatformResourceUtils.getPlatformResource(target.getClass(), baseName, extension)) {
                    BufferedImage image = ImageIO.read(in);
                    parseContext.addComponent(imageName, image);
                } catch (Exception e) {
                    log.warn("Unable to read resource file for image {}", resourceName, e);
                }
            } else {
                log.error("Can't get image from non-existent place");
                cleanParse = false;
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


    /**
     * Handle the "component" statement, which defines a single
     * component. The component will have a name, a class (given by
     * alias), and a set of property values.
     * @param ctx the parse tree
     */

    @Override
    public void enterComponentStatement(SpwingViewFileParser.ComponentStatementContext ctx) {

        // Get the name and the class alias
        final String componentName = ctx.componentName.getText();
        final String classAlias = ctx.classAlias.getText();

        // If we already have a component with this name, we ignore this
        // definition.
        if (parseContext.getKnownComponents().containsKey(componentName)) {
            log.warn("In SWVF file, {} is already defined. Additional definition ignored", componentName);
        } else {

            // Get the definition of the element based on the alias. This will give
            // us the function for creating the new alias
            SVWFParseContext.ElementDefinition elementDefinition = parseContext.getDefinitionMap().get(classAlias);
            if (Objects.nonNull(elementDefinition)) {

                // We have the alias, create the instance and get a class for what we just defined.
                final Function<String, Object> creationFunction = elementDefinition.createFunction();
                final Object createdElement = creationFunction.apply(componentName);
                final Class<?> definedObjectType = createdElement.getClass();

                // Add this new component to the list
                parseContext.getKnownComponents().put(componentName, createdElement);

                // Set the initial "value" of the component to the name. This can be
                // changed by specifying a "value" property, which will be intercepted and
                // interpreted as a new value
                parseContext.getViewPropertyBinder().setValue(createdElement, componentName);

                // In order to set properties of this component, we create a bean
                // wrapper and set the conversion service
                BeanWrapper activeComponent = parseContext.getViewPropertyBinder().beanWrapperFor(createdElement);
                activeComponent.setConversionService(conversionService);

                // Apply defaults for any class that is in the target class hierarchy
                parseContext.getDefaultValues().forEach((clazz, defaults) -> {
                    if (clazz.isAssignableFrom(definedObjectType)) {
                        defaults.forEach(activeComponent::setPropertyValue);
                    }
                });

                // Walk through all the key/value pairs for this component.
                ctx.kvPair().forEach(kvPairContext -> {
                    handleKVPairForComponent(activeComponent, kvPairContext);
                });
            } else {
                log.error("Cannot create {}, no such class or class is abstract", classAlias);
                cleanParse = false;
            }
        }
    }


    /**
     * Called to handle a single key/value pair when creating a component.
     *
     * @param activeComponent The component being created
     * @param kvPairContext The KV pair to process
     */
    private void handleKVPairForComponent(BeanWrapper activeComponent, SpwingViewFileParser.KvPairContext kvPairContext) {

        // Get the property name and declared value.
        final String propertyName = kvPairContext.k.getText();
        final DeclaredValue declaredValue = new DeclaredValue(kvPairContext.v);

        // Special case the "value" property - we track that internally.
        // TODO special case CSS stylesheet and style clauses
        if ("value".equals(propertyName)) {
            parseContext.getViewPropertyBinder().setValue(activeComponent.getWrappedInstance(), declaredValue.get());
        } else {

            // Get the property type for this component, based on the name given
            final Class<?> propertyType = activeComponent.getPropertyType(propertyName);
            if (Objects.nonNull(propertyType)) {

                // Push the value to the component.
                final Object unconvertedValue = declaredValue.get();
                final Object convertedValue = conversionService.convert(unconvertedValue, propertyType);
                activeComponent.setPropertyValue(propertyName, convertedValue);
            } else {

                // Bad property name given. Kill the parse.
                log.error("Unable to set property {} of unknown type", propertyName);
                cleanParse = false;
            }
        }
    }


    @RequiredArgsConstructor
    private final class DeclaredValue {

        private final SpwingViewFileParser.PairValueContext valueContext;

        public Object get() {
            if (Objects.nonNull(valueContext.bool)) {
                return valueContext.bool.getText().equals("true");
            } else if (Objects.nonNull(valueContext.string)) {
                return stripStringLiteral(valueContext.string);
            } else if (Objects.nonNull(valueContext.size)) {
                return parseDimension(valueContext.size);
            } else if (Objects.nonNull(valueContext.id)) {
                return parseContext.getKnownComponents().get(valueContext.id.getText());
            } else {
                return getIntValue(valueContext.integer);
            }
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
        } else if (element instanceof Group group) {
            if (wrapButtonGroup) {
                JPanel panel = parseContext.getComponentFactory().newJPanel("_anon_" + token.getText());
                group.components.forEach(panel::add);
                consumer.accept(panel);
            } else {
                group.components.forEach(consumer);
            }
        } else {
            log.warn("Could not find element {} as a component or group", token.getText());
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

                if (Objects.nonNull(boxElementContext.identifierElement())) {
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



    private record Group(List<Component> components){}
}
