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
import com.hablutzel.spwing.model.PropertyChangeModel;
import com.hablutzel.spwing.util.ANTLRUtils;
import com.hablutzel.spwing.util.FlexpressionParser;
import com.hablutzel.spwing.util.PlatformResourceUtils;
import com.hablutzel.spwing.view.adapter.JLabelEventAdapter;
import com.hablutzel.spwing.view.bind.*;
import com.hablutzel.spwing.view.factory.PropertyListenerRefreshTrigger;
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import com.hablutzel.spwing.view.factory.component.ViewComponentFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.List;
import java.util.*;
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
    
    @Getter
    private final ApplicationContext applicationContext;
    private final ConversionService conversionService;
    private final DocumentEventDispatcher documentEventDispatcher;
    private final ViewPropertyBinder viewPropertyBinder;

    /**
     * Map of the component factories we use. {@link ViewComponentFactory} instances
     * all for the creation of new Swing components based on a short name (an alias)
     * which is defined by the factory. This map gets loaded when building the view
     */
    private final Map<String, ViewComponentFactory<?>> viewComponentFactoryMap = new HashMap<>();

    /**
     * Map of the components that this listener knows about
     */
    private final Map<String, Object> knownComponents = new HashMap<>();


    private final Map<String, Cocoon<?>> knownCocoons = new HashMap<>();


    private final Map<Object,BeanWrapper> beanWrapperFor = new HashMap<>();

    /**
     * Defined default values
     */
    private final Map<String, Map<String, Object>> defaultValues = new HashMap<>();



    private SpwingViewFileParser.SvwfFileContext svwfFileContext = null;

    @Getter
    private boolean cleanParse = true;

    // TODO change layout processing
    private Container layoutContainer = null;


    /**
     * Build a new listener for the given model and {@link Spwing} instance
     *
     * @param spwing      The Spwing instance
     * @param modelObject The model object
     * @return The new listener
     */
    public static SVWFListener build(final Spwing spwing, final Object modelObject ) {

        // Get the conversion service and document event dispatcher
        final ApplicationContext applicationContext = spwing.getApplicationContext();
        final DocumentEventDispatcher documentEventDispatcher = DocumentEventDispatcher.get(applicationContext);
        final ConversionService conversionService = applicationContext.getBean(ConversionService.class);

        // Create a component map for this parse, and include all the predefined components.
        return new SVWFListener(modelObject, spwing, applicationContext, conversionService, documentEventDispatcher, new ViewPropertyBinder(applicationContext) );
    }

    @Override
    public void enterSvwfFile(SpwingViewFileParser.SvwfFileContext ctx) {
        this.svwfFileContext = ctx;

        // Entering the file. We need to load the components that we know about
        // from the beans that are of type ViewComponentFactory. Note that
        // we explicitly don't specify the generic class parameter, because we
        // want to get all the ViewComponentFactory instances.
        applicationContext.getBeansOfType(ViewComponentFactory.class)
                .values()
                .forEach(factoryBean -> viewComponentFactoryMap.put(factoryBean.alias(), factoryBean));

        // Define known components - known colors, fonts, etc.
        Collection<SVWFComponentFactory> factoryBeans = applicationContext.getBeansOfType(SVWFComponentFactory.class).values();
        factoryBeans.forEach(svwfComponentFactory -> svwfComponentFactory.addComponents(this));
    }

    @Override
    public void enterInvokeStatement(SpwingViewFileParser.InvokeStatementContext ctx) {
        String methodName = ctx.methodName.getText();

        final Object targetObject = getRootClauseObject(ctx.root);

        // See if we can find that method on the target
        if (null != targetObject) {
            ReflectiveInvoker.invoke(applicationContext, targetObject, methodName,
                    ParameterResolution.forClass(SVWFListener.class, this),
                    ParameterResolution.forClass(SpwingViewFileParser.SvwfFileContext.class, svwfFileContext));
        }
    }


    /**
     * A root clause defines the root of an expression. This can be
     * the model object (which is the default of the root clause is
     * omitted), the controller object, or an arbitrary bean given by
     * name
     * @param rootClauseContext The root clause context
     * @return The object that is the root of the expression
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

        // See if we have a cocoon to use for binding
        if (knownCocoons.containsKey(identifier)) {

            Cocoon<?> cocoon = knownCocoons.get(identifier);
            if (cocoon.allowedBindings(property) != Cocoon.AllowedBindings.NONE) {
                cocoon.bindProperty(property, accessor, triggers);
            } else {
                log.error("Component property {} cannot be bound", property);
                cleanParse = false;
            }
        } else {
            log.error("Component {} not found, or ineligible for binding", identifier);
            cleanParse = false;
        }
    }


    /**
     * Get a list of triggers from the context, by looking for all the
     * string elements, getting the element token, and stripping the
     * quotes from that token text. This will return an empty list of
     * the clause was omitted.
     * @param ctx The context
     * @return The (possibly empty) list of triggers.
     */
    private List<String> getTriggeringProperties(final String targetProperty,
                                                 final SpwingViewFileParser.TriggerClauseContext ctx) {
        if (null == ctx) {
            return null == targetProperty ? List.of() : List.of(targetProperty);
        } else {
            return ctx.stringElement().stream()
                    .map(stringElementContext -> stringElementContext.element)
                    .map(ANTLRUtils::stripStringLiteral)
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
        List<String> invalidIdentifiers = identifiedElements.stream()
                .filter(name -> !knownComponents.containsKey(name) || !(knownComponents.get(name) instanceof Component))
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
           viewPropertyBinder.bindGroup( targetElements, targetProperty, accessor, triggers );

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
        String expression = ANTLRUtils.stripStringLiteral(ctx.expression);
        Accessor accessor = toAccessor(rootObject, expression);
        String defaultTrigger = accessor instanceof PropertyAccessor ? expression : null;

        List<String> triggers = getTriggeringProperties(defaultTrigger, ctx.triggerClause());
        List<RefreshTrigger> refreshTriggers = convertPropertyNamesIntoRefreshTriggers(rootObject, triggers);

        // See if we have a group or single binding statement
        if (null != ctx.target.single) {
            bindSingleObject(ctx.target.single, accessor, refreshTriggers);
        } else {
            bindGroup(ctx.target.group, accessor, refreshTriggers);
        }
    }


    private List<RefreshTrigger> convertPropertyNamesIntoRefreshTriggers(final Object targetObject,
                                                                         final List<String> propertyNames ) {

        // Add the refresh triggers required
        Consumer<PropertyChangeListener> addListenerFunc = getAddChangeListener(targetObject);
        if (null != addListenerFunc) {
            final List<RefreshTrigger> resultingTriggers = new ArrayList<>();
            propertyNames.forEach( propertyTrigger -> {
                PropertyListenerRefreshTrigger refreshTrigger = buildRefreshTrigger(targetObject, propertyTrigger);
                addListenerFunc.accept(refreshTrigger);
                resultingTriggers.add(refreshTrigger);
            });
            return resultingTriggers;
        } else {
            return List.of();
        }
    }

    /**
     * Build a {@link PropertyListenerRefreshTrigger} for the object and property.
     * This will also add a property listener, if needed, to the
     * leaf node property so that the leaf node signalling a change will be
     * propagated to the target object.
     *
     * @param targetObject The target object
     * @param propertyName The property name (or expression)
     * @return A {@link PropertyListenerRefreshTrigger} instance
     */
    private PropertyListenerRefreshTrigger buildRefreshTrigger( final Object targetObject,
                                                                final String propertyName ) {

        final BeanWrapper beanWrapper = beanWrapperFor.computeIfAbsent(targetObject, BeanWrapperImpl::new);
        final PropertyDescriptor propertyDescriptor = beanWrapper.getPropertyDescriptor(propertyName);

        // There are two potential cases here. First, the property could be a simple
        // property on the target object. In this case, we can just build a property
        // listener and use that. However, in some cases the property name passed in
        // is actually a property expression, and therefore the leaf property name
        // and the expression will differ. In this case, we need to add a bridge listener
        final String leafName = propertyDescriptor.getName();
        final PropertyListenerRefreshTrigger result = new PropertyListenerRefreshTrigger(propertyName);
        if (!propertyName.equals(leafName)) {

            // This is the case where the property name is a property path, so we can
            // listen to changes on the parent. We chop off the end of the property
            // path in order to get the property path that references the parent
            final String parentPath = propertyName.substring(0, propertyName.length() - leafName.length() - 1 );

            final Object parentObject = beanWrapper.getPropertyValue(parentPath);
            if (null != parentObject) {
                Consumer<PropertyChangeListener> addParentListener = getAddChangeListener(parentObject);
                if (null != addParentListener) {
                    addParentListener.accept(evt -> {
                        if (evt.getPropertyName().equals(leafName)) {
                            result.propertyChange(new PropertyChangeEvent(parentObject, propertyName, evt.getOldValue(), evt.getNewValue()));
                        }
                    });
                } else {
                    log.error("Parent property defined by {} does not emit property events", parentPath);
                }
            } else {
                log.error( "Could not resolve parent property {}", parentPath );
            }
        }

        return result;


    }


    /**
     * Get the function that adds a property change listener to an object.
     *
     * @param targetObject The target object
     * @return A (potentially null) {@link Consumer<PropertyChangeListener>}
     */
    private Consumer<PropertyChangeListener> getAddChangeListener(final Object targetObject ) {
        if (targetObject instanceof PropertyEditor propertyEditor) {
            return propertyEditor::addPropertyChangeListener;
        } else if (targetObject instanceof PropertyChangeModel propertyChangeModel) {
            return propertyChangeModel::addPropertyChangeListener;
        } else {
            // Reflective approach
            Optional<Method> optionalAddListenerMethod = Arrays.stream(targetObject.getClass().getMethods())
                    .filter(method -> !Modifier.isStatic(method.getModifiers()) &&
                            !Modifier.isAbstract(method.getModifiers()) &&
                            method.getName().equals("addPropertyChangeListener") &&
                            method.getParameters().length == 1 &&
                            method.getParameters()[0].getType().equals(PropertyChangeListener.class))
                    .findFirst();
            if (optionalAddListenerMethod.isPresent()) {
                final Method addListenerMethod = optionalAddListenerMethod.get();
                return propertyChangeListener -> {
                    try {
                        addListenerMethod.invoke(targetObject, propertyChangeListener);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        log.warn("Method {} of {} could not be invoked, no refresh trigger. ",
                                addListenerMethod.getName(), targetObject.getClass().getName());
                    }
                };
            }
        }
        return null;
    }


    @Override
    public void enterDefaultStatement(SpwingViewFileParser.DefaultStatementContext ctx) {
        String classAlias = ctx.classAlias.getText();
        if (viewComponentFactoryMap.containsKey(classAlias)) {

            // Create a new default value map and associate it with the alias
            // If the map already exists, add to it

            final Map<String, Object> defaultMap = defaultValues.computeIfAbsent(classAlias, s -> new HashMap<>());
            defaultValues.put(classAlias, defaultMap);

            // For each defined default, remember the values
            ctx.fixedOnlyKVPair().forEach(kvPairContext -> {
                final String propertyName = kvPairContext.k.getText();
                final Object declaredValue = getDeclaredValue(kvPairContext.fixedValue().v);
                if (null != declaredValue) {
                    defaultMap.put(propertyName, declaredValue);
                }
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
            knownComponents.put(colorName,
                    new Color(ANTLRUtils.getIntValue(intColorSpecContext.red),
                            ANTLRUtils.getIntValue(intColorSpecContext.green),
                            ANTLRUtils.getIntValue(intColorSpecContext.blue),
                            ANTLRUtils.getIntValue(intColorSpecContext.alpha)));
        } else {
            knownComponents.put(colorName,
                    new Color(ANTLRUtils.getIntValue(intColorSpecContext.red),
                            ANTLRUtils.getIntValue(intColorSpecContext.green),
                            ANTLRUtils.getIntValue(intColorSpecContext.blue)));
        }
    }

    private void defineFloatColor(final String colorName,
                                  final SpwingViewFileParser.FloatColorSpecContext floatColorSpecContext) {
        if (null != floatColorSpecContext.alphaf) {
            knownComponents.put(colorName,
                    new Color(ANTLRUtils.getFloatValue(floatColorSpecContext.redf),
                            ANTLRUtils.getFloatValue(floatColorSpecContext.greenf),
                            ANTLRUtils.getFloatValue(floatColorSpecContext.bluef),
                            ANTLRUtils.getFloatValue(floatColorSpecContext.alphaf)));
        } else {
            knownComponents.put(colorName,
                    new Color(ANTLRUtils.getFloatValue(floatColorSpecContext.redf),
                            ANTLRUtils.getFloatValue(floatColorSpecContext.greenf),
                            ANTLRUtils.getFloatValue(floatColorSpecContext.bluef)));
        }
    }

    private void defineBitfieldColor(final String colorName,
                                     final SpwingViewFileParser.BitFieldColorSpecContext bitFieldColorSpecContext) {
        int colorValue = ANTLRUtils.getIntValue(bitFieldColorSpecContext.bitField);
        knownComponents.put(colorName,
                new Color(colorValue, ANTLRUtils.getBooleanValue(bitFieldColorSpecContext.hasAlpha)));
    }

    @Override
    public void enterImageStatement(SpwingViewFileParser.ImageStatementContext ctx) {
        String imageName = ctx.name.getText();
        if (null != ctx.imageSpec().resourceName) {
            String resourceName = ANTLRUtils.stripStringLiteral(ctx.imageSpec().resourceName);
            Object target = getRootClauseObject(ctx.imageSpec().root);
            if (null != target) {

                String baseName = FilenameUtils.getBaseName(resourceName);
                String extension = FilenameUtils.getExtension(resourceName);

                try (InputStream in = PlatformResourceUtils.getPlatformResource(target.getClass(), baseName, extension)) {
                    BufferedImage image = ImageIO.read(in);
                    this.addComponent(imageName, image);
                } catch (Exception e) {
                    log.warn("Unable to read resource file for image {}", resourceName, e);
                }
            } else {
                log.error("Can't get image from non-existent place");
                cleanParse = false;
            }
        } else {
            String urlName = ANTLRUtils.stripStringLiteral(ctx.imageSpec().url);
            try {
                URL url = new URL(urlName);
                knownComponents.put(imageName, ImageIO.read(url));
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
        if (knownComponents.containsKey(componentName)) {
            log.warn("In SVWF file, {} is already defined. Additional definition ignored", componentName);
        } else {

            // Get the definition of the element based on the alias. This will give
            // us the function for creating the new alias
            ViewComponentFactory<?> viewComponentFactory = viewComponentFactoryMap.get(classAlias);
            if (null != viewComponentFactory) {

                // We have the alias, create the instance and get a class for what we just defined.
                final Cocoon<?> cocoon = viewComponentFactory.build(componentName, conversionService);

                // Add this new component to the list
                knownComponents.put(componentName, cocoon.getComponent());
                knownCocoons.put(componentName, cocoon );

                // Set the initial "value" of the component to the name. This can be
                // changed by specifying a "value" property, which will be intercepted and
                // interpreted as a new value
                cocoon.setProperty("name", componentName);

                // Apply defaults for any class that is in the target class hierarchy
                Map<String,Object> defaultValuesForComponent = defaultValues.get(componentName);
                if (null != defaultValuesForComponent) {
                    defaultValuesForComponent.forEach(cocoon::setProperty);
                }

                // Walk through all the key/value pairs for this component.
                ctx.kvPair().forEach(kvPairContext -> handleKVPairForComponent(cocoon, kvPairContext));
            } else {
                log.error("Cannot create {}, no such class or class is abstract", classAlias);
                cleanParse = false;
            }
        }
    }


    /**
     * Called to handle a single key/value pair when creating a component.
     *
     * @param cocoon The component cocoon
     * @param kvPairContext The KV pair to process
     */
    private void handleKVPairForComponent(final Cocoon<?> cocoon,
                                          final SpwingViewFileParser.KvPairContext kvPairContext) {

        // Get the property name and declared value.
        final String propertyName = kvPairContext.k.getText();
        if (null != kvPairContext.fixedValue()) {
            // It's a fixed value. Handle it as such
            handleFixedComponentValue(cocoon, propertyName, kvPairContext.fixedValue());
        } else {
            handleBoundComponentValue(cocoon, propertyName, kvPairContext.boundValue());
        }
    }


    /**
     * Handles inline bindings (the '=>' operator inside a component
     * definition). This creates a binding between the Swing value and
     * the model property value.
     * @param cocoon The component cocoon
     * @param propertyName The Swing component property being bound
     * @param boundValueContext The parser context
     */
    private void handleBoundComponentValue(final Cocoon<?> cocoon,
                                           final String propertyName,
                                           final SpwingViewFileParser.BoundValueContext boundValueContext )
    {

        // Get the accessor for the model object property being used. Inline
        // binding expressions (part of the component definition) can only
        // reference the model object. For more complex bindings - groups,
        // other objects, etc. - use the 'bind' statement
        String expression = ANTLRUtils.stripStringLiteral(boundValueContext.e);
        Accessor externalState = toAccessor(modelObject, expression);

        final Cocoon.AllowedBindings allowedBindings = cocoon.allowedBindings(propertyName);
        if (externalState instanceof PropertyAccessor && allowedBindings != Cocoon.AllowedBindings.NONE) {
            final List<RefreshTrigger> refreshTriggers = convertPropertyNamesIntoRefreshTriggers(modelObject, List.of(expression));
            cocoon.bindProperty(propertyName, externalState, refreshTriggers);
        } else if (!externalState.isWriteable() && (allowedBindings == Cocoon.AllowedBindings.BIDIRECTIONAL || allowedBindings == Cocoon.AllowedBindings.FROM_COMPONENT)) {
            log.error( "Cannot bind {} to {}", expression, propertyName);
            cleanParse = false;
        } else {
            cocoon.bindProperty(propertyName, externalState, List.of());
        }
    }



    /**
     * Takes the given expression (a property path, SPEL expression, literal, etc.)
     * and turns it into an {@link Accessor}. The expression will first be evaluated
     * as a property path on the supplied bean. If it is a readable property, then
     * a {@link PropertyAccessor} will be created. If not, the expression will be
     * evaluated to see if it can be handled by a {@link FlexpressionParser}
     * (which can include system properties, SPEL expression, static method calls,
     * arbitrary beans, and more). In this case, a {@link FlexpressionAccessor}
     * will be created. If it is neither a property path nor a Flexpression,
     * it will be taken as a literal.
     *
     * @param targetObject The object to act as a property source for property paths
     * @param valueExpression The expression
     * @return A new accessor
     */

    private Accessor toAccessor(@Nullable final Object targetObject, final String valueExpression) {

        // First preference: A property, as this will allow read/write
        if (null != targetObject) {
            BeanWrapper targetBeanWrapper = beanWrapperFor.computeIfAbsent(targetObject, BeanWrapperImpl::new);
            if (targetBeanWrapper.isReadableProperty(valueExpression)) {
                return new PropertyAccessor(targetBeanWrapper, valueExpression, conversionService);
            }

            // Try a enum in the same package
            try {
                String packageName = targetObject.getClass().getPackageName();
                String potentialEnumName = String.format( "%s.%s", packageName, valueExpression );
                log.info( "Trying {}", potentialEnumName);
                Class<?> enumClass = Class.forName(potentialEnumName);
                if (enumClass.isEnum()) {
                    return new EnumAccessor(enumClass);
                }
            } catch (ClassNotFoundException e) {
                log.debug( "Test for enum failed, moving on" );
            }

            // If not a property, could it be an enum?
            try {
                Class<?> targetClass = targetObject.getClass();
                String potentialEnumName = String.format( "%s$%s", targetClass.getCanonicalName(), valueExpression );
                log.info( "Trying {}", potentialEnumName);
                Class<?> enumClass = Class.forName(potentialEnumName);
                if (enumClass.isEnum()) {
                    return new EnumAccessor(enumClass);
                }
            } catch (ClassNotFoundException e) {
                log.debug( "Test for enum failed, moving on" );
            }

        }

        // Not a property, try a SPEL expression. Allows more flexibility than a property
        // but isn't writeable
        try {
            SpelExpressionParser parser = new SpelExpressionParser();
            Expression spelExpression = parser.parseExpression(valueExpression);
            StandardEvaluationContext context = new StandardEvaluationContext(targetObject);
            SpelExpressionAccessor spelExpressionAccessor = new SpelExpressionAccessor(spelExpression, context, conversionService);
            // Make sure it can be accessed ... this will throw if not
            return spelExpressionAccessor.validate(context);
        } catch (ParseException | SpelEvaluationException e) {
            log.debug( "Unable to treat {} as an expression", valueExpression, e);
        }

        // Next try is a flexpression, which can be used to pull in application properties
        if (FlexpressionParser.appearsToBeFlexpression(valueExpression)) {
            return new FlexpressionAccessor(valueExpression, applicationContext, conversionService);
        }

        // Last chance, a literal
        return  new LiteralAccessor(valueExpression, conversionService);
    }



    private void handleFixedComponentValue(final Cocoon<?> cocoon,
                                           final String propertyName,
                                           final SpwingViewFileParser.FixedValueContext fixedValueContext  )
    {
        // Get the declared value
        final Object declaredValue = getDeclaredValue(fixedValueContext.v);
        if (declaredValue instanceof ContextualConstant contextualConstant) {
            final Object constantValue = contextualConstant.get(ConstantContext.SwingConstants);
            cocoon.setFromAccessor(propertyName, new LiteralAccessor(constantValue, conversionService) );
        } else if (null != declaredValue) {
            cocoon.setFromAccessor(propertyName, new LiteralAccessor(declaredValue, conversionService));
        }
    }


    /**
     * Get the declared value for a fixed value declaration. Fixed values are
     * denoted by being specified by the "=" operator (not the "=>" operator)
     * and can be determined at the time the SVWF file is parsed. This is
     * required for default values and available for component values.
     *
     * @param valueContext The {@link com.hablutzel.spwing.view.factory.svwf.SpwingViewFileParser.PairValueContext}
     * @return The found value
     */
    private Object getDeclaredValue(SpwingViewFileParser.PairValueContext valueContext ) {
        if (null != valueContext.bool) {
            return ANTLRUtils.getBooleanValue(valueContext.bool);
        } else if (null != valueContext.string) {
            return ANTLRUtils.stripStringLiteral(valueContext.string);
        } else if (null != valueContext.floatVal) {
            return ANTLRUtils.getFloatValue(valueContext.floatVal);
        } else if (null != valueContext.size) {
            return new Dimension(ANTLRUtils.getIntValue(valueContext.size.width), ANTLRUtils.getIntValue(valueContext.size.height));
        } else if (null != valueContext.in) {
            return new Insets( ANTLRUtils.getIntValue(valueContext.in.top), ANTLRUtils.getIntValue(valueContext.in.left),
                    ANTLRUtils.getIntValue(valueContext.in.bottom), ANTLRUtils.getIntValue(valueContext.in.right));
        } else if (null != valueContext.id) {
            return knownComponents.get(valueContext.id.getText());
        } else if (null != valueContext.bean) {
            String beanID = valueContext.bean.getText();
            try {
                return applicationContext.getBean(beanID);
            } catch (BeansException e) {
                log.error("Bean {} was not found in the context", beanID );
                cleanParse = false;
                return null;
            }
        } else {

            // We know it has to be an integer; it can't be anything else or the parse would fail.
            return ANTLRUtils.getIntValue(valueContext.integer);
        }
    }


    private Dimension parseDimension(SpwingViewFileParser.DimensionContext dimensionContext) {
        return new Dimension(ANTLRUtils.getIntValue(dimensionContext.width), ANTLRUtils.getIntValue(dimensionContext.height));
    }

    @Override
    public void enterLayoutStatement(SpwingViewFileParser.LayoutStatementContext ctx) {
        String componentToLayout = ctx.component.getText();
        if (knownComponents.get(componentToLayout) instanceof Container container) {
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
        Object rawElement = knownComponents.get(elementName);

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
                    JPanel panel = new JPanel();
                    panel.setName("_anon_" + token.getText());
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

                        Object unconvertedValue = getDeclaredValue(kvPair.fixedValue().v);
                        if (null != unconvertedValue) {
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
     * corresponding column number: 1, 2
     * <ul>
     *     <li>A => 1</li>
     *     <li>B => 2</li>
     *     <li>AA => 27</li>
     *     <li>etc</li>
     * </ul>
     * @param columnSpecification The column specification
     * @return The column number
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
            int axis = ANTLRUtils.tokenEquals("vertical", ctx.orientation) ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS;
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


    public Component rootComponent() {
        return knownComponents.values().stream()
                .filter(component -> component instanceof JFrame)
                .map(component -> (JFrame) component)
                .findFirst()
                .orElse(null);
    }


    /**
     * Add a new component to the set the listener knows about
     *
     * @param componentName The component name
     * @param component The component (often, but not always, a Swing component)
     */
    public void addComponent(String componentName, Object component) {
        knownComponents.put(componentName, component);
    }



    private record Group(List<Component> components){}
}
