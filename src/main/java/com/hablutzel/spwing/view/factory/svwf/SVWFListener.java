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


import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.constants.ConstantContext;
import com.hablutzel.spwing.constants.ContextualConstant;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.invoke.ParameterResolution;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.util.PlatformResourceUtils;
import com.hablutzel.spwing.view.adapter.JLabelEventAdapter;
import com.hablutzel.spwing.view.bind.Accessor;
import com.hablutzel.spwing.view.bind.impl.RefreshTrigger;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    public static final Pattern GRID_COORDINATE_PATTERN = Pattern.compile("([a-zA-Z]+)(\\d+)");
    private final Object modelObject;
    private final Spwing spwing;
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
        if (null != targetObject) {
            ReflectiveInvoker.invoke(applicationContext, targetObject, methodName,
                    ParameterResolution.forClass(SVWFListener.class, this),
                    ParameterResolution.forClass(SpwingViewFileParser.SvwfFileContext.class, svwfFileContext),
                    ParameterResolution.forClass(SVWFParseContext.class, parseContext));
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
        if (null != rootClauseContext) {

            // The target can be either a keyword (model) or a String
            if (null != rootClauseContext.m) {
                return modelObject;
            } else if (null != rootClauseContext.b) {
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
                                    final List<RefreshTrigger> triggers ) {

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
        if (null == ctx) {
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
                            final List<RefreshTrigger> triggers ) {

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
            if (null != ctx.groupName) {

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
        Accessor accessor = parseContext.getViewPropertyBinder().toAccessor(rootObject, expression);

        List<String> triggers = getTriggers(ctx.triggerClause());
        List<RefreshTrigger> refreshTriggers = parseContext.getViewPropertyBinder()
                .getRefreshTriggers(applicationContext, rootObject, expression, triggers);

        // Get the property accessor for this element
        if (null != accessor) {

            // See if we have a group or single binding statement
            if (null != ctx.target.single) {
                bindSingleObject(ctx.target.single, accessor, refreshTriggers);
            } else {
                bindGroup(ctx.target.group, accessor, refreshTriggers);
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
        return null != token && value.equals(token.getText());
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
        if (null != colorDefinitionContext.intColorSpec()) {
            defineIntColor(colorName, colorDefinitionContext.intColorSpec());
        } else if (null != colorDefinitionContext.floatColorSpec()) {
            defineFloatColor(colorName, colorDefinitionContext.floatColorSpec());
        } else {
            defineBitfieldColor(colorName, colorDefinitionContext.bitFieldColorSpec());
        }
    }

    private void defineIntColor(final String colorName,
                                final SpwingViewFileParser.IntColorSpecContext intColorSpecContext) {
        if (null != intColorSpecContext.alpha) {
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
        if (null != floatColorSpecContext.alphaf) {
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
        if (null != ctx.imageSpec().resourceName) {
            String resourceName = stripStringLiteral(ctx.imageSpec().resourceName);
            Object target = getRootClauseObject(ctx.imageSpec().root);
            if (null != target) {

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
            if (null != elementDefinition) {

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
                ctx.kvPair().forEach(kvPairContext -> handleKVPairForComponent(activeComponent, kvPairContext));
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
            if (null != propertyType) {

                // Push the value to the component.
                Object unconvertedValue = declaredValue.get();
                if (unconvertedValue instanceof ContextualConstant contextualConstant) {
                    unconvertedValue = contextualConstant.get(ConstantContext.SwingConstants);
                }
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
            if (null != valueContext.bool) {
                return valueContext.bool.getText().equals("true");
            } else if (null != valueContext.string) {
                return stripStringLiteral(valueContext.string);
            } else if (null != valueContext.floatVal) {
                return getFloatValue(valueContext.floatVal);
            } else if (null != valueContext.size) {
                return parseDimension(valueContext.size);
            } else if (null != valueContext.in) {
                return new Insets( getIntValue(valueContext.in.top), getIntValue(valueContext.in.left),
                        getIntValue(valueContext.in.bottom), getIntValue(valueContext.in.right));
            } else if (null != valueContext.id) {
                return parseContext.getKnownComponents().get(valueContext.id.getText());
            } else {
                return getIntValue(valueContext.integer);
            }
        }

        <T> T as(Class<T> clazz) {
            return conversionService.convert(get(), clazz);
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
        if (null == element && "filler".equals(token.getText())) {
            consumer.accept(new JPanel());
        } else {
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
    }

    @Override
    public void enterFlowLayoutDescription(SpwingViewFileParser.FlowLayoutDescriptionContext ctx) {
        if (null != layoutContainer) {
            if (layoutContainer instanceof JFrame frame) {
                frame.getContentPane().setLayout(new FlowLayout());
            } else {
                layoutContainer.setLayout(new FlowLayout());
            }

            ctx.identifierElement().forEach(flowElementContext -> onNamedComponent(flowElementContext.element, layoutContainer::add, false));
        }
    }


    @Override
    public void enterButtonBarLayoutDescription(SpwingViewFileParser.ButtonBarLayoutDescriptionContext ctx) {
        if (null != layoutContainer) {

            // Create the box layout - always horizontal for button bars
            setLayout(container -> new BoxLayout(container, BoxLayout.X_AXIS));

            // Start by adding a right glue element
            layoutContainer.add(Box.createHorizontalGlue());

            // For each of the identified elements, add the element and a rigid area
            // between to the right
            ctx.identifierElement().forEach( identifierElementContext -> {
                onNamedComponent(identifierElementContext.element, layoutContainer::add, false);
                layoutContainer.add(Box.createRigidArea(new Dimension(10, 10)));
            });
        }
    }


    /**
     * Implements the "gridBagLayout" functionality
     * @param ctx the parse tree
     */
    @Override
    public void enterGridBagLayoutDescription(SpwingViewFileParser.GridBagLayoutDescriptionContext ctx) {

        // Make sure we have a container to layout
        if (null != layoutContainer) {

            // Add the grid bag layout to the container
            final GridBagLayout gridBagLayout = new GridBagLayout();
            layoutContainer.setLayout(gridBagLayout);

            ctx.gridBagElementDescription().forEach( gridBagElementDescriptionContext -> {

                // Create a new GridBagConstraints
                GridBagConstraints gridBagConstraints = new GridBagConstraints();

                // Handle the modifiers for this element
                if (null != gridBagElementDescriptionContext.modifiers) {
                    gridBagElementDescriptionContext.modifiers.kvPair().forEach( kvPair -> {
                        String propertyName = kvPair.k.getText();
                        DeclaredValue declaredValue = new DeclaredValue(kvPair.v);

                        Object unconvertedValue = declaredValue.get();
                        if (unconvertedValue instanceof ContextualConstant contextualConstant) {
                            unconvertedValue = contextualConstant.get(ConstantContext.GridBagConstants);
                        }

                        switch (propertyName) {
                            case "gridx" -> gridBagConstraints.gridx = doConvert(unconvertedValue, Integer.class, 0);
                            case "gridy" -> gridBagConstraints.gridy = doConvert(unconvertedValue, Integer.class, 0);
                            case "weightx" -> gridBagConstraints.weightx = doConvert(unconvertedValue, Float.class, 0.0f);
                            case "weighty" -> gridBagConstraints.weighty = doConvert(unconvertedValue, Float.class, 0.0f);
                            case "gridwidth" -> gridBagConstraints.gridwidth = doConvert(unconvertedValue, Integer.class, 0);
                            case "gridheight" -> gridBagConstraints.gridheight = doConvert(unconvertedValue, Integer.class, 0);
                            case "fill" -> gridBagConstraints.fill = doConvert(unconvertedValue, Integer.class, 0);
                            case "anchor" -> gridBagConstraints.anchor = doConvert(unconvertedValue, Integer.class, 0);
                            case "ipadx" -> gridBagConstraints.ipadx = doConvert(unconvertedValue, Integer.class, 0);
                            case "ipady" -> gridBagConstraints.ipady = doConvert(unconvertedValue, Integer.class, 0);
                            case "insets" -> gridBagConstraints.insets = doConvert(unconvertedValue, Insets.class, new Insets(0, 0, 0, 0));
                            default -> log.warn("Ignored unknown modifier {}", propertyName);
                        }
                    });
                }

                // If there isn't a placement, then we use a default placement
                if (null != gridBagElementDescriptionContext.placement) {

                    // We need to decode the height and width. To make the parser
                    // simpler, these are specified as general identifiers, but need
                    // to fit a more constrained patter of letters followed by numbers;
                    // moreover the letters and numbers have to translate into
                    // valid coordinates. Columns are specified by the initial letter(s),
                    // with A being column 1, B being column 2, AA being column 27, etc.
                    // (In other words, spreadsheet cell syntax). The numbers are the
                    // rows, with the first row being 1. A rectangular range can be specified
                    // by specifying a topLeft and bottomRight separated by a colon (again
                    // spreadsheet syntax) with each constrained as above and the additional
                    // constraint that the bottomRight must be at least as top and at least as
                    // left as the topLeft (in other words, the range must include at least
                    // one cell and cannot be specified backwards).
                    final String topLeftSpec = gridBagElementDescriptionContext.placement.topLeft.getText();
                    final Dimension topLeft = decodeGridCoordinate(topLeftSpec);
                    gridBagConstraints.gridx = topLeft.width;
                    gridBagConstraints.gridy = topLeft.height;
                    if (null != gridBagElementDescriptionContext.placement.botRight) {
                        final String botRightSpec = gridBagElementDescriptionContext.placement.botRight.getText();
                        Dimension botRight = decodeGridCoordinate(botRightSpec);
                        gridBagConstraints.gridwidth = botRight.width - topLeft.width + 1;
                        gridBagConstraints.gridheight = botRight.height - topLeft.height + 1;
                    } else {
                        gridBagConstraints.gridwidth = 1;
                        gridBagConstraints.gridheight = 1;
                    }
                }

                // Add this element to the specified cell
                onNamedComponent(gridBagElementDescriptionContext.element.element,
                        component -> layoutContainer.add(component, gridBagConstraints),
                        true );

            });
        }
    }


    private <T> T doConvert(Object unconvertedValue, Class<T> clazz, T defaultValue ) {
        if (null != unconvertedValue && conversionService.canConvert(unconvertedValue.getClass(), clazz)) {
            return conversionService.convert(unconvertedValue, clazz);
        } else {
            return defaultValue;
        }
    }



    /**
     * Translate a spreadsheet style grid coordinate (A2, B5) into a
     * corresponding {@link Dimension} specification.
     * <ul>
     *     <li>A1 => Dimension(1,1)</li>
     *     <li>A2 => Dimension(1,2)</li>
     *     <li>B1 => Dimension(2,1)</li>
     *     etc.
     * </ul>
     * @param gridCoordinate The spreadsheet style coordinate specification
     * @return The corresponding dimension.
     */
    private Dimension decodeGridCoordinate(String gridCoordinate) {
        Matcher matcher = GRID_COORDINATE_PATTERN.matcher(gridCoordinate);
        if (matcher.matches()) {
            return new Dimension(
                    decodeColumn(matcher.group(1).toUpperCase()),
                    Integer.parseInt(matcher.group(2)) - 1);
        } else {
            log.error( "{} is not a valid grid coordinate specification", matcher.group(1));
            cleanParse = false;
            return new Dimension(0, 0);
        }
    }


    /**
     * Decode a column specification 'A', 'B', etc into a
     * corresponding row: 1, 2
     * <ul>
     *     <li>A => 1</li>
     *     <li>B => 2</li>
     *     <li>AA => 27</li>
     *     <li>etc</li>
     * </ul>
     * @param columnSpecification
     * @return
     */
    public int decodeColumn(String columnSpecification) {
        int columnNumber = 0;
        for (int i = 0; i < columnSpecification.length(); ++i) {
            columnNumber = columnNumber * 26 + (columnSpecification.charAt(i) - 'A');
        }
        return columnNumber;
    }

    /**
     * Handle a boxLayout layout description
     *
     * @param ctx the parse tree
     */
    @Override
    public void enterBoxLayoutDescription(SpwingViewFileParser.BoxLayoutDescriptionContext ctx) {

        // Make sure we have a container to layout
        if (null != layoutContainer) {

            // Determine whether we are arranging horizontally or vertically and create the layout
            int axis = tokenEquals("vertical", ctx.orientation) ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS;
            setLayout(container -> new BoxLayout(container, axis));

            ctx.boxElement().forEach(boxElementContext -> {

                if (null != boxElementContext.identifierElement()) {
                    onNamedComponent(boxElementContext.identifierElement().element, layoutContainer::add, false);
                } else if (null != boxElementContext.horizontalGlue) {
                    layoutContainer.add(Box.createHorizontalGlue());
                } else if (null != boxElementContext.verticalGlue) {
                    layoutContainer.add(Box.createVerticalGlue());
                } else if (null != boxElementContext.rigidArea) {
                    layoutContainer.add(Box.createRigidArea(parseDimension(boxElementContext.size)));
                } else if (null != boxElementContext.filler) {

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
        if (null != layoutContainer) {
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
        documentEventDispatcher.registerEventAdapter(componentName, new JLabelEventAdapter(result, spwing));
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
