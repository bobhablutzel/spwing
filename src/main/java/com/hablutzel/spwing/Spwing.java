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

package com.hablutzel.spwing;


import com.hablutzel.spwing.annotations.*;
import com.hablutzel.spwing.command.CommandAwareUndoManager;
import com.hablutzel.spwing.component.BuiltInCommands;
import com.hablutzel.spwing.component.CommandMethods;
import com.hablutzel.spwing.component.CommandMethodsScanner;
import com.hablutzel.spwing.context.DocumentScope;
import com.hablutzel.spwing.context.DocumentScopeManager;
import com.hablutzel.spwing.context.DocumentSession;
import com.hablutzel.spwing.converter.*;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.events.DocumentEventPublisher;
import com.hablutzel.spwing.invoke.Invoker;
import com.hablutzel.spwing.invoke.ParameterDescription;
import com.hablutzel.spwing.invoke.ParameterResolution;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.laf.DefaultLookAndFeelFactory;
import com.hablutzel.spwing.laf.LookAndFeelFactory;
import com.hablutzel.spwing.menu.MenuLoader;
import com.hablutzel.spwing.model.ControllerFor;
import com.hablutzel.spwing.model.ModelConfiguration;
import com.hablutzel.spwing.model.ModelFactory;
import com.hablutzel.spwing.util.ClassUtils;
import com.hablutzel.spwing.util.DocumentElementPostBeanProcessor;
import com.hablutzel.spwing.util.PlatformResourceBundleMessageSource;
import com.hablutzel.spwing.util.PlatformResourceUtils;
import com.hablutzel.spwing.view.ViewWindowListener;
import com.hablutzel.spwing.view.factory.svwf.SVWFResourceViewFactory;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitResponse;
import java.awt.event.ActionEvent;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Main class for the Spwing application framework. The {@link Spwing}
 * class takes responsibility for initializing the {@link ApplicationContext}
 * instance, managing the menus, dispatching events, and similar
 * activities.<br>
 * Generally speaking the framework is launched by invoking
 * {@link #launch(Class)}. This routine takes a root object, which should
 * be in the package that contains the main application instance or
 * configuration object, and frequently <i>is</i> the application class
 * or configuration class. Spwing uses that class to see the application
 * context, so having that class in the root package of the application
 * is generally a good idea.<br>
 * The Spwing framework builds on the concept of a document component, which
 * is an arbitrary Java class annotated with {@link Handler} or
 * one of the annotations that includes {@link Handler} via meta
 * annotation, such as {@link Model}, {@link Controller}, or {@link Application}.
 * These classes are scanned to find methods that provide application-specific
 * functionality, including
 * <ul>
 *     <li>Command handlers, which respond to commands (notably menu commands)</li>
 *     <li>Command enablers, which denote which commands are active</li>
 *     <li>Event listeners, which respond to events from the application or framework</li>
 * </ul>
 * One of the primary concepts in the Spwing framework is the concept of
 * a command handler. Handlers are methods associated with commands; the handler
 * method will be invoked when the command is fired (see {@link #fireCommand(String, Function...)}
 * and {@link #fireCommandWithResult(String, Class, Object, Function...)}).
 * Commands are fired automatically based on menu events; the identifier
 * for the menu item is the command associated with the menu item. The application
 * code can also fire commands directly to get the same functionality.
 * Handler methods are identified either by naming convention or annotation.
 * By convention, the handler for cmdXXX is named "handleXXX". If that
 * naming convention does not work, then the {@link HandlerFor} annotation
 * can be used to explicitly mark a method as a handler for a command.<br>
 * By default, commands that have a handler are enabled. You can provide an
 * enabler method in the same class as the handler class to change this behavior
 * and make it more sensitive to application state. Enabler methods can also
 * be identified by either naming convention or annotation; in this case the
 * naming convention is "enableXXX" for command cmdXXX, and {@link EnablerFor}
 * for explicit annotation.<br>
 * Enabler functions are expected to return a boolean value. Returning anything
 * else will disable the command. Handlers generally return either void
 * or a {@link Runnable} instance. If a Runnable instance is returned, it will be
 * scheduled for later execution via {@link SwingUtilities#invokeLater(Runnable)}.
 * However, some commands may return other values. A notable case of this are the 
 * handlers for cmdSave and cmdClose, which return a boolean that denotes whether
 * a multi-window close process (including in response to a cmdQuit) should proceed.<br>
 * The handler methods can take a flexible set of parameters. These parameters can be
 * <ul>
 *     <li>An {@link ActionEvent} class (available only for some commands, may be null)</li>
 *     <li>A {@link Spwing} class instance; this will be the UI created at startup time</li>
 *     <li>An {@link ApplicationContext} instance representing the current Spring application context</li>
 *     <li>A {@link JMenuBar} instance representing the current menu bar</li>
 *     <li>The application, model, or controller class, matched either by class or by annotation</li>
 *     <li>Any named bean, so long as the parameter name and bean type match</li>
 * </ul>
 * This functionality is provided by the {@link ReflectiveInvoker} and {@link com.hablutzel.spwing.util.FlexpressionParser}
 * classes. Also see {@link #resolveInvokerParameter(ParameterDescription)} which provides resolution
 * for the framework parameter types.
 * The UI instance will be returned by {@link #launch(Class)} )}
 * @author Bob Hablutzel
 */
@Slf4j
@RequiredArgsConstructor
@Service
@Scope("singleton")
public class Spwing implements ApplicationContextAware  {

    /**
     * The {@link DocumentScopeManager} used to manage the application context
     * scope for items with {@link Scope} defined as "document"
     */
    @Getter
    private final DocumentScopeManager documentScopeManager;

    private final CommandMethodsScanner commandMethodsScanner;


    /**
     * The {@link ApplicationContext} instance
     */
    @Getter
    @Setter
    private ApplicationContext applicationContext;

    /**
     * The preCommandHook will be check when firing a command
     * (see {@link #fireCommand(String, Function...)}). If this
     * hook is not present, or the hook returns TRUE, the command
     * will fire. If the hook is present and returns false, the
     * command will not fire. This is a general hook for all
     * commands.
     */
    @Setter
    private BiFunction<Spwing, String, Boolean> preCommandHook = null;

    /**
     * The postCommandHook will be invoked after a command is
     * executed. The UI instance and command that was invoked
     * will be passed to the hook. This is an informational
     * call and does not impact the execution of the command.
     */
    @Setter
    private BiConsumer<Spwing, String> postCommandHook = null;

    @Getter
    @Setter
    private Class<?> contextRoot;

    /**
     * The {@link JMenuBar} used to control the main application
     * menu instance
     */
    @Getter
    private final JMenuBar menuBar = new JMenuBar();

    private final List<Object> applicationScopeHandlers = new ArrayList<>();

    /**
     * Map of menu command names to {@link CommandMethods} instances for the application scope handlers
     */
    private Map<String, CommandMethods> applicationCommandMethodsMap;

    /**
     * Main entry point ot the Spwing UI system. Creates the
     * application context that configures the application,
     * the localization helper, and similar classes. It also
     * finds the main application and fires the initial menu
     * event as needed.
     *
     * @param contextRoot The context root for the application.
     *                    This should be a class at the root of the
     *                    application hierarchy and is often either
     *                    the application class itself or a configuration
     *                    instance.
     */
    public static Spwing launch(final Class<?> contextRoot) {

        // Build the message source for the UI class; this contains all the framework messages
        PlatformResourceBundleMessageSource rootMessageSource = new PlatformResourceBundleMessageSource();
        rootMessageSource.setBundleClassLoader(Spwing.class.getClassLoader());
        rootMessageSource.addBasenames(Spwing.class.getName());
        rootMessageSource.setUseCodeAsDefaultMessage(true);

        ResourceBundleMessageSource registeredMessageSource = rootMessageSource;
        // Attempt to find a resource bundle for the application
        try {
            // See if the application resource bundle can be loaded
            testForApplicationBundle(contextRoot); // Trigger the exception if not available
            PlatformResourceBundleMessageSource applicationMessageSource = new PlatformResourceBundleMessageSource();
            applicationMessageSource.setBundleClassLoader(contextRoot.getClassLoader());
            applicationMessageSource.addBasenames(contextRoot.getName());
            applicationMessageSource.setParentMessageSource(rootMessageSource);
            registeredMessageSource = applicationMessageSource;
        } catch (MissingResourceException e) {
            log.warn("No resource bundle for the application could be found, based on {}", contextRoot.getName());
            log.warn("Application specific resource bundle will not be created.");
        }


        // Some OS-specific configuration has to be performed prior to the application context
        // being loaded. For example, in MacOS, we have to set the application name before any
        // Swing classes are loaded, which will (likely) occur when the application context
        // refresh call is made below. In order to ensure that we can perform those configurations
        // property, we perform some configuration now, before any Spring environment is set up
        // (which might trigger the creation of the Swing classes)
        performBootstrapConfiguration(contextRoot, registeredMessageSource);

        // Signal that we are initializing the framework.er
        log.info( registeredMessageSource.getMessage("SpwingInit", null, Locale.getDefault()));

        // We use a parent bean factory to register our pre-defined items. This includes the
        // application context to start, but will also contain the document scope manager
        // once we are able to create it (we have to create it after the application context).
        DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();
        parentBeanFactory.registerSingleton("messageSource", registeredMessageSource);
        parentBeanFactory.registerSingleton("conversionService",getConversionService(contextRoot) );

        // Build the application context. We also need to create a new document scope manager
        // which will handle the "document" scope. This scope creates the equivalent of
        // a web session for the desktop applications, so that document scoped items are created
        // as singletons for each document (and kept unique and separate from each other).
        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(parentBeanFactory);
        DocumentScopeManager documentScopeManager = new DocumentScopeManager(applicationContext);
        parentBeanFactory.registerSingleton("documentScopeManager", documentScopeManager );
        applicationContext.getBeanFactory().registerScope("document", new DocumentScope(documentScopeManager));

        // Add a new bean post-processor that handles the document-specific instances - the event publisher
        // and dispatcher, document ID, etc.
        final DocumentElementPostBeanProcessor documentElementPostBeanProcessor = new DocumentElementPostBeanProcessor(documentScopeManager);
        applicationContext.getBeanFactory().addBeanPostProcessor(documentElementPostBeanProcessor);

        // Scan and refresh the context
        applicationContext.scan(contextRoot.getPackageName(), Spwing.class.getPackageName());
        try {
            applicationContext.refresh();
        } catch (BeanCreationException bce) {
            log.error( "Error creating bean at startup time", bce);
            log.error( "If the root cause is that the container bean is null, this is probably a document scope issue.");
            log.error( "If the bean that failed is on a document-scoped object - for example, an instance of");
            log.error( "ModelConfiguration<> consider either making the bean itself document scoped or static" );
            throw bce;
        }

        // At this point we have the Spwing singleton bean.
        Spwing spwing = applicationContext.getBean(Spwing.class);
        spwing.setContextRoot(contextRoot);

        // Pass that to the document scope manager so we can be alerted when document scope changes
        documentScopeManager.setSpwing(spwing);

        // Register some framework classes with the invoker so they can be injected into arguments
        Invoker.registerFrameworkAdapter( spwing::resolveInvokerParameter );

        // Allow it to be injected
        documentElementPostBeanProcessor.setSpwing(spwing);

        // Prepare the UI instance
        spwing.prepare();

        // Launch the application
        spwing.startApplication();
        return spwing;
    }


    /**
     * The user has the option (but not obligation) to provide an application
     * resource bundle that will be used for localization, etc. This routine
     * tests to see if the resource bundle is available, using platform aware
     * semantics.
     * @param contextRoot The context root of the application.
     * @see PlatformResourceUtils#platformAndBaseNames(String)
     */

    private static void testForApplicationBundle(final Class<?> contextRoot ) {

        // Loop through all the possible application resource bundle names.
        // We want these to be platform specific, so we build the platform
        // names and loop through that. For each, we attempt to load the
        // resource bundle (map statement), catching the exception that gets
        // thrown if not loadable and returning null. Find the first non-null
        // instance, and, if there is one, we know that we have a resource bundle.
        PlatformResourceUtils.platformAndBaseNames(contextRoot.getName()).stream()
                .map( name -> {
                    try {
                        return ResourceBundle.getBundle(name, Locale.getDefault(), contextRoot.getClassLoader());
                    } catch (Exception e) {
                        log.debug( "{} not found", name );
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresentOrElse( resourceBundle -> log.debug( "Found an application resource bundle" ),
                        () -> { throw new MissingResourceException("bundle", contextRoot.getName(), "*"); });
    }


    /**
     * The {@link Invoker} allows for frameworks to impose special parameters that will be
     * injected into parameter slots based on criteria. This routine performs that functionality
     * for the Spwing framework, injecting framework specific classes such as the application,
     * model, controller, etc.
     *
     * @param parameterDescription The parameter description to resolver
     * @return The resolved object, or null if not resolved.
     */

    private ParameterResolution resolveInvokerParameter(ParameterDescription parameterDescription) {
        ResolvableType parameterResolvableType = parameterDescription.getType();
        Class<?> parameterTargetType = parameterResolvableType.getRawClass();
        if (null != parameterTargetType) {
            Object activeApplication = getActiveApplication();
            final DocumentSession activeSession = documentScopeManager.getActiveSession();
            if (Window.class.isAssignableFrom(parameterTargetType)) {
                return ParameterResolution.of(KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow());
            } else if (null != activeApplication && activeApplication.getClass().isAssignableFrom(parameterTargetType)) {
                return ParameterResolution.of(activeApplication);
            } else if (parameterDescription.hasParameterAnnotation(Application.class)) {
                return ParameterResolution.of(activeApplication);
            } else if (JFrame.class.isAssignableFrom(parameterTargetType)) {
                return ParameterResolution.of(null != activeSession ? activeSession.getFrame() : ParameterResolution.unresolved());
            } else if (DocumentSession.class.isAssignableFrom(parameterTargetType)) {
                return ParameterResolution.of(activeSession);
            } else if (DocumentEventDispatcher.class.isAssignableFrom(parameterTargetType)) {
                return ParameterResolution.of(null != activeSession ? documentScopeManager.getDocumentEventDispatcher() : null);
            } else if (DocumentEventPublisher.class.isAssignableFrom(parameterTargetType)) {
                return ParameterResolution.of(null != activeSession ? new DocumentEventPublisher(documentScopeManager.getDocumentEventDispatcher()) : null);
            } else if (UndoManager.class.equals(parameterTargetType) ||
                    CommandAwareUndoManager.class.isAssignableFrom(parameterTargetType)) {
                return ParameterResolution.of(null != activeSession ? activeSession.getUndoManager() : null);
            } else if (null != activeSession && null != activeSession.getModelClass() && parameterTargetType.isAssignableFrom(activeSession.getModelClass())) {
                return ParameterResolution.of(activeSession.getModel());
            } else if (null != activeSession && null != activeSession.getControllerClass() && parameterTargetType.isAssignableFrom(activeSession.getControllerClass())) {
                return ParameterResolution.of(activeSession.getController());
            } else if (parameterDescription.hasParameterAnnotation(Model.class)) {
                return ParameterResolution.of(activeSession != null ? activeSession.getModel() : null);
            } else if (parameterDescription.hasParameterAnnotation(Controller.class)) {
                return ParameterResolution.of(activeSession != null ? activeSession.getController() : null);
            }
        }

        return ParameterResolution.unresolved();
    }


    /**
     * Find the application - a bean in the application context with the
     * {@link Application} annotation. If there are more than one such bean,
     * one is selected at random.
     *
     * @return The application, or null
     */
    public Object getActiveApplication() {
        // Get the objects that are annotated with Application.
        // Pick the first one
        Map<String,Object> applicationBeans = applicationContext.getBeansWithAnnotation(Application.class);
        return applicationBeans.isEmpty() ? null : applicationBeans.values().iterator().next();
    }


    /**
     * Perform configuration that needs to be performed before the Swing or Spring
     * environments are created.
     *
     * @param contextRoot The context root class
     * @param messageSource The {@link MessageSource} instance for getting application names, etc.
     */
    private static void performBootstrapConfiguration(final Class<?> contextRoot,
                                                      final MessageSource messageSource) {

        // Find the look and feel to load
        // TODO allow selectable LAF
        ClassUtils.find(contextRoot, LookAndFeel.class).stream()
                .map(BeanDefinition::getBeanClassName)
                .forEach(log::info);

        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            performMacOSSpecificBootstrapConfiguration(contextRoot, messageSource);
        } else if (SystemUtils.IS_OS_WINDOWS) {
            performWindowsSpecificBootstrapConfiguration(contextRoot, messageSource);
        }
    }


    /**
     * Windows specific bootstrap configuration. Sets the default LookAndFeel
     * @param contextRoot The context root
     * @param messageSource The {@link MessageSource} instance for getting application names, etc.
     */
    private static void performWindowsSpecificBootstrapConfiguration(final Class<?> contextRoot,
                                                                     final MessageSource messageSource) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException e) {
            log.warn("Unable to set the operating system look and feel" );
        }
    }


    /**
     * Routine to get the application name. The application name should be defined
     * in the property file for the context root class as a string named "applicationName";
     * if this is not available the simple name of the context root will be used.
     * @return The application name
     */
    public String getApplicationName() {
        return applicationContext.getMessage("applicationName", null, contextRoot.getSimpleName(), Locale.getDefault());
    }

    /**
     * Perform pre-Swing and pre-Spring environment configuration for MacOS
     *
     * @param contextRoot The context root class
     */
    private static void performMacOSSpecificBootstrapConfiguration(final Class<?> contextRoot,
                                                                   final MessageSource messageSource) {

        // Find the application name to use for the Apple menu. This has to be
        // set before the Spring classes are loaded.
        String applicationName = messageSource.getMessage("applicationName", null, contextRoot.getSimpleName(), Locale.getDefault() );

        // Perform general property setting to adopt the MacOS look & feel
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        if (null != applicationName) {
            System.setProperty("apple.awt.application.name", applicationName);
        }
        System.setProperty("apple.awt.application.appearance", "system");

    }



    /**
     * Prepare the environment for execution. This happens after the
     * Spring environment is created, so it has access to and uses
     * the application context to find beans. This routine
     * <ul>
     *     <li>Creates the "application-scope" handlers:
     *         <ul>
     *             <li>Adds the built-in commands as a last-change handler</li>
     *             <li>Finds any beans that are of the class of the context root and adds them as handlers.</li>
     *             <li>Finds any beans that are annotated with {@link Application} and adds them as handlers.</li>
     *         </ul>
     *     </li>
     *     <li>Sets up an initial menu bar</li>
     *     <li>Sets up the look and feel</li>
     * </ul>
     *
     */
    public void prepare() {

        // Get the built-in commands, and add it to the list of handlers.
        this.applicationScopeHandlers.add(applicationContext.getBean(BuiltInCommands.class));

        // The context root object can, if defined as a bean, contain handler methods.
        // In this case, we add it to the application scope handlers
        Map<String,?> contextBeans = applicationContext.getBeansOfType(contextRoot);
        applicationScopeHandlers.addAll(contextBeans.values());

        // Build the command methods for the application scope handlers
        applicationCommandMethodsMap = commandMethodsScanner.scanDocumentComponents(this, applicationScopeHandlers);

        // Add any beans annotated with @Application
        applicationScopeHandlers.addAll(applicationContext.getBeansWithAnnotation(Application.class).values());

        // Get the look & feel for the application. By default this will be the
        // DefaultLookAndFeelFactory, but applications can create a bean of type
        // LookAndFeelFactory. If there is more than one, the first will be used.
        String[] lookAndFeelBeanNames = applicationContext.getBeanNamesForType(LookAndFeelFactory.class);
        LookAndFeelFactory lookAndFeelFactory = lookAndFeelBeanNames.length > 0
                ? applicationContext.getBean(lookAndFeelBeanNames[0], LookAndFeelFactory.class)
                : new DefaultLookAndFeelFactory();
        try {
            UIManager.setLookAndFeel(lookAndFeelFactory.get());
        } catch (UnsupportedLookAndFeelException e) {
            log.error("Look and feel {} could not be used", lookAndFeelFactory.getClass().getName(), e);
        }

        // Build the initial menu bar
        buildMenuBar(applicationScopeHandlers);

        // OS specific work
        prepareOSSpecific();
    }



    /**
     * Perform OS specific initializations. This will determine the
     * active OS and dispatch the OS specific logic.
     * TODO support Linux.
     */
    private void prepareOSSpecific() {
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            prepareMacOS();
        } else if (SystemUtils.IS_OS_WINDOWS) {
            prepareWindowOS();
        }
    }



    private void prepareWindowOS() {
        log.debug("Inside prepareWindowsOS, version: {}", PlatformResourceUtils.getOSInfo());
    }

    /**
     * Routine to prepare the application to run in a MacOS
     * environment. This performs the necessary actions to give
     * the application a native look and feel.
     * TODO support different look and feels (other than FlatMac)
     * TODO support About box
     * TODO support Preferences handling
     */
    private void prepareMacOS() {

        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(aboutEvent -> fireCommand("cmdAbout",
                ParameterResolution.forClass(AboutEvent.class,aboutEvent)));
        desktop.setPreferencesHandler(preferencesEvent -> fireCommand("cmdPreferences",
                ParameterResolution.forClass(PreferencesEvent.class,preferencesEvent)));
        desktop.setQuitHandler((quitEvent, quitResponse) -> fireCommand("cmdQuit",
                ParameterResolution.forClass(QuitEvent.class, quitEvent),
                ParameterResolution.forClass(QuitResponse.class,quitResponse )));

        // Ask the desktop to use our menu bar as the system menu bar
        Desktop.getDesktop().setDefaultMenuBar(this.menuBar);
    }


    /**
     * startApplication is called to launch the application. This finds the
     * {@link Application} annotation from the application class, which contains
     * information about any initial commands to fire when the application starts.
     * If there is an initial application event, it is fired. Note that there
     * is a default initial command to open the default model. This works well for
     * applications that have a single {@link Model}, or at least one that makes
     * sense to open when the application starts. If the application does not
     * need this functionality, the initial command can be set to cmdNOP on the
     * {@link Application} annotation.
     */
    protected void startApplication() {

        // Find the application annotation and make sure it is not null.
        // It really, really shouldn't be but best to check. Once we have
        // it, get the initial command, and see if that is specified. If the
        // initial command is not null or blank, then fire that command.
        if (applicationContext.containsBean("launchCommand")) {
            try {
                String initialCommand = applicationContext.getBean("launchCommand", String.class);
                if (!initialCommand.isBlank()) {
                    log.debug( "Firing initial command {}", initialCommand );
                    final ActionEvent actionEvent = new ActionEvent(this, 0, initialCommand);
                    fireCommand(initialCommand, ParameterResolution.forClass(ActionEvent.class, actionEvent));
                }
            } catch (BeansException e) {
                log.warn( "Was expecting \"launchCommand\" bean to be of (or convertable to) type String." );
            }
        }
    }


    /**
     * The framework supports a set of implicit conversions - from String to Font,
     * for example - that are used in SVWF files and the like. This routine defines
     * the conversion service that includes that specialized conversion classses.
     * @return A new {@link ConversionService} with specialized converters
     */
    private static ConversionService getConversionService(final Class<?> contextRoot) {
        ConversionServiceFactoryBean factory = new ConversionServiceFactoryBean();
        Set<Converter<?, ?>> convSet = new HashSet<>();
        convSet.add(new StringToFontConverter());
        convSet.add(new StringToColorConverter());
        convSet.add(new ImageToIconConverter());
        convSet.add(new StringToImageConverter(contextRoot));
        convSet.add(new StringToIconConverter(contextRoot));
        convSet.add(new DateToCalendarConverter());
        convSet.add(new CalendarToDateConverter());
        convSet.add(new DateToInstantConverter());
        convSet.add(new InstantToDateConverter());
        factory.setConverters(convSet);
        factory.afterPropertiesSet();
        return factory.getObject();
    }


    private Map<String,CommandMethods> getActiveCommandMethodsMap() {
        return documentScopeManager.hasActiveSession()
                ? documentScopeManager.getCommandMethodsMap()
                : applicationCommandMethodsMap;
    }



    public boolean isCommandEnabled(String commandID) {
        Map<String,CommandMethods> activeCommandsMap = getActiveCommandMethodsMap();
        return activeCommandsMap.containsKey(commandID) && activeCommandsMap.get(commandID).isEnabled(null);
    }


    public void documentSessionChanged(final List<Object> handlers) {
        rebuildMenubarForDocumentHandlers(handlers);
    }

    private void rebuildMenubarForDocumentHandlers(List<Object> handlers) {
        buildMenuBar(null == handlers ? applicationScopeHandlers : handlers);
    }


    /**
     * Method to fire a command. This finds the right action handler
     * instance for the command and invokes it.
     *
     * @param command     The command to fire
     * @param injectedObjects The parameters to inject into the invocation
     */
    @SafeVarargs
    public final void fireCommand(final String command,
                                  final Function<ParameterDescription, ParameterResolution>... injectedObjects) {
        Runnable runnable = fireCommandWithResult(command, Runnable.class, null, injectedObjects );
        if (null != runnable) {
            SwingUtilities.invokeLater(runnable);
        }
    }


    /**
     * Fire a command, allow the command handler to return a result. This routine will pass
     * back whatever result the command handler does (which could be null or void).
     *
     * @param command The command to fire
     * @param clazz The class of the result (or {@link Void#getClass()} for no result
     * @param defaultValue The default value (or null)
     * @param injectedObjects Any objects to be "injected" into the call for flexible parameter passing
     * @return The result of the call
     * @param <T> The expected type of the call
     */
    public <T> T fireCommandWithResult( final String command,
                                        final Class<T> clazz,
                                        final T defaultValue,
                                        final Function<ParameterDescription,ParameterResolution>... injectedObjects ) {

        // Make sure we can fire the command.
        Map<String,CommandMethods> activeCommandMethodsMap = getActiveCommandMethodsMap();
        if (null != activeCommandMethodsMap && activeCommandMethodsMap.containsKey(command)) {

            // Get the command methods to invoke for the command
            final CommandMethods commandMethods = activeCommandMethodsMap.get(command);

            // Fire the pre-command hook (if any)
            boolean fire = null == preCommandHook || preCommandHook.apply(this, command);

            // If we are still firing, then invoke the command
            if (fire) {

                T result = commandMethods.fireCommandWithResult(clazz, defaultValue, injectedObjects);

                // Call the post-command hook (if any)
                if (null != postCommandHook) {
                    postCommandHook.accept(this, command);
                }
                return result;
            }
        } else {
            log.debug( "No handler for {}", command );
            log.debug( "Known commands: {}", null == activeCommandMethodsMap ? "null" : activeCommandMethodsMap.keySet() );
        }
        return defaultValue;

    }


    /**
     * Get the active model object.
     *
     * @return A (potentially null) model object
     */
    public Object getActiveModel() {
        return documentScopeManager.getActiveModel();
    }


    /**
     * Called to create a new model from a model factory. First this creates
     * the model factory, and then calls the create method.
     *
     * @param modelClass The model class
     * @param <T> The class of the model class
     */
    public <T> void newModel(Class<T> modelClass) {
        createModelDocumentScope(modelClass, ModelFactory::create);
    }

    /**
     * Called to read a model from a model factory. First this creates
     * the model factory, and then calls the open method.
     *
     * @param modelClass The model class
     * @param <T> The class of the model class
     */
    public <T> void openModel(Class<T> modelClass) {
        createModelDocumentScope(modelClass, ModelFactory::open);
    }


    /**
     * Create a new model from a supplier, and (assuming the model is actually
     * annotated with {@link Model} creates a new document scope for this model instance.
     * After that, attempt to create the associated controller if one is specified.
     *
     * @param modelClass The model class
     * @param modelCreator   The supplier for the model
     */
    private void createModelDocumentScope(final Class<?> modelClass,
                                          final BiFunction<ModelFactory<?>,DocumentSession,Object> modelCreator) {

        UUID activeDocumentBeforeCreation = documentScopeManager.getActiveDocumentID();
        try {

            // Create a new document session
            DocumentSession documentSession = new DocumentSession( applicationContext, applicationScopeHandlers );
            UUID documentScopeID = documentScopeManager.establishSession(documentSession);
            documentSession.setModelClass(modelClass);

            // Attempt to get a model configuration for this model class. This provides a hook for
            // model specific information. The user does this by creating beans of well known
            // types or names on the configuration bean. For example, the user can create a
            // bean with the name "fileExtension" for models that are saved to file based documents,
            // and the Spwing framework will automatically honor those file extensions. Similarly,
            // the configuration can create a ModelFactory bean that takes the responsibility of
            // creating the models. See the ModelConfiguration class for more information
            ResolvableType modelConfigurationType = ResolvableType.forClassWithGenerics(ModelConfiguration.class, modelClass );
            String[] beanNames = applicationContext.getBeanNamesForType(modelConfigurationType);
            if (beanNames.length != 0) {
                ModelConfiguration<?> modelConfiguration = (ModelConfiguration<?>) applicationContext.getBean(beanNames[0]);
                documentSession.setModelConfiguration(modelConfiguration);
            }

            // Now see if there is a factory defined in the context. If there is a bean in the
            // configuration of type ModelFactory<T> it will be used rather than building one
            ModelFactory<?> modelFactory = ModelFactory.forModelClass(applicationContext, modelClass);

            // Get a new model factory for the model, based on the static methods open and create
            Object modelInstance = modelCreator.apply(modelFactory,documentSession);
            documentSession.setModel(modelInstance);

            // Get the view factory class to use
            Class<?> viewFactoryClass = applicationContext.containsBean("viewFactoryClass" )
                    ? applicationContext.getBean("viewFactoryClass").getClass()
                    : SVWFResourceViewFactory.class;

            // Get the view factory associated with the model, and open the view
            openView(documentScopeID, documentSession, viewFactoryClass);

            // Get the associated controller.
            Object controllerInstance = createControllerForModel(documentSession, modelClass, modelInstance);
            documentSession.setController(controllerInstance);

            // Inject the Swing components into the controller as needed
            if (null != controllerInstance && null != documentSession.getFrame()) {
                injectComponentsIntoController(controllerInstance, documentSession.getFrame());
            }

            // Get rid of any undo events that were spuriously created when setting
            // the initial value of the view elements
            documentSession.getUndoManager().discardAllEdits();

            // Have the document session scan for command handlers, listeners, etc.
            // Do this after the view is created so we can map the listeners attached
            // to view elements. We do this even if the view failure fails, so that
            // there are command methods available (Quit, for example)
            documentSession.scanHandlers(this, commandMethodsScanner);

            // Rebuild the menu bar, now that we have the controller, etc.
            rebuildMenubarForDocumentHandlers(documentSession.getAvailableHandlers());
        } catch (Exception e) {

            // Something went sideways. Log the error, restore the previous session,
            // and reset
            log.warn("Unable to open document", e );
            documentScopeManager.resetScope(activeDocumentBeforeCreation);
            DocumentSession restoredSession = documentScopeManager.getActiveSession();
            if (null != restoredSession) {
                restoredSession.scanHandlers(this, commandMethodsScanner);
                rebuildMenubarForDocumentHandlers(restoredSession.getAvailableHandlers());
            }
        }
    }


    private void injectComponentsIntoController( final Object controllerInstance,
                                                 final JFrame frame ) {

        BeanWrapperImpl controllerWrapper = new BeanWrapperImpl(controllerInstance);
        injectComponent( controllerWrapper, frame );
    }

    private void injectComponent(final BeanWrapper controller,
                                 final Component component ) {
        String name = component.getName();
        if (null != name && !name.isBlank()) {
            Class<?> propertyType = controller.getPropertyType(name);
            if (controller.isWritableProperty(name) &&
                    null != propertyType &&
                    propertyType.isAssignableFrom(component.getClass())) {
                controller.setPropertyValue(name, component);
            }
        }

        if (component instanceof Container container) {
            Arrays.stream(container.getComponents())
                    .forEach(child -> injectComponent(controller, child));
        }
    }


    /**
     * Figure out the {@link Controller} associated with this model (if any) and open it.
     *
     * @param modelClass      The class for the model instance
     * @param modelInstance   The model instance
     */
    private Object createControllerForModel(
            final DocumentSession documentSession,
            final Class<?> modelClass,
            final Object modelInstance) {

        // Look for a ControllerFor bean
        ResolvableType controllerForType = ResolvableType.forClassWithGenerics(ControllerFor.class, modelClass);
        String[] controllerForBeanNames = applicationContext.getBeanNamesForType(controllerForType);
        if (controllerForBeanNames.length > 0) {

            if (controllerForBeanNames.length > 1) {
                log.warn( "Multiple controller beans defined for model class {}, arbitrarily choosing {}",
                        modelClass.getName(), controllerForBeanNames[0]);
            }
            Object controllerBean = applicationContext.getBean(controllerForBeanNames[0]);
            documentSession.setControllerClass(controllerBean.getClass());
            return controllerBean;
        }

        // If the model is also the controller (supported for simple and ported applications)
        // then there is no need to create a new controller instance; use the model.
        if (null != AnnotatedElementUtils.getMergedAnnotation(modelClass, Controller.class)) {
            // The model is also the controller
            documentSession.setControllerClass(documentSession.getModelClass());
            return modelInstance;
        } else {
            // We need to infer the controller class name from the model name.
            // For the class named XXXModel, look for a controller named XXXController
            String modelClassName = modelInstance.getClass().getName();
            if (modelClassName.endsWith("Model" )) {
                String truncatedName = modelClassName.substring(0, modelClassName.length() - 5 );
                String candidateClassName = truncatedName + "Controller";

                try {
                    final Class<?> controllerClass = Class.forName(candidateClassName);
                    documentSession.setControllerClass(controllerClass);
                    return applicationContext.getBean(controllerClass);
                } catch (ClassNotFoundException e) {
                    log.warn( "Tee model class {} did not specify a controller class, and the assumed class {} could not be found",
                            modelClassName, candidateClassName);
                }
            }
        }
        return null;
    }


    /**
     * Create a view by class. This is called following the creation of a model
     * and controller instances.
     *
     * @param documentScopeID The document scope to add the view to
     * @param viewFactoryClass The view factory class
     */
    public void openView(final UUID documentScopeID,
                         final DocumentSession documentSession,
                         final @NonNull Class<?> viewFactoryClass) {

        // There should be a bean of the view factory class. (The standard factory classes
        // are marked as services, which is a good idea if defining a new factory class, or
        // create a bean via configuration.
        //
        // This routine finds the first method named "build" in the view factory class,
        // and invoking that class. Because we use the Invoker here, we get all kinds of
        // automatic parameter passing behavior - the build method can be whatever is appropriate
        // for the factory. The build method should return a Component. Note that we explicitly
        // choose NOT to use an interface for this class; this allows the build method to take
        // an arbitrary set of parameters resolved by the Invoker.
        //
        // If the Component returned is not a JFrame, a JFrame will be created automatically
        Object viewFactory = applicationContext.getBean(viewFactoryClass);
        Optional<Method> buildMethod = Arrays.stream(viewFactory.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals("build") && Component.class.isAssignableFrom(m.getReturnType()))
                .findFirst();

        // If we have the build method, then invoke it
        if (buildMethod.isPresent()) {

            // Build the invoker
            Invoker invoker = new ReflectiveInvoker(applicationContext, viewFactory, buildMethod.get() );
            Component component = invoker.invoke(Component.class);
            if (component != null) {

                if (component instanceof JFrame frame) {
                    setUpFrame(documentScopeID, documentSession, frame);
                } else {
                    // Build a default JFrame around this component
                    JFrame frame = new JFrame(getApplicationName());
                    frame.setMinimumSize(new Dimension(200, 100));

                    // add the component
                    frame.getContentPane().add(component);
                    frame.pack();

                    setUpFrame(documentScopeID, documentSession, frame);
                }
            } else {
                throw new RuntimeException(String.format("Called the build method for %s, but it returned null", viewFactoryClass.getName()));
            }
        } else {
            throw new RuntimeException(String.format("View factory %s did not have a build method", viewFactoryClass.getName()));
        }
    }

    /**
     * Spwing handles the top level frame specially - it is available
     * as an injectable parameter for Invokers, and we have
     * window listeners for when window state changes - losing and gaining
     * focus, etc.
     *
     * @param documentScopeID The current scope ID
     * @param frame The frame
     */
    private void setUpFrame(final UUID documentScopeID,
                            final DocumentSession documentSession,
                            final JFrame frame) {

        // Save the frame in the document scope
        documentSession.setFrame(frame);

        // Set the title
        frame.setTitle( null != documentSession.getAssociatedFile() ? documentSession.getAssociatedFile().getName() : "Untitled" );

        // We add a window listener to the view so we know when it
        // gains or loses focus. We also add our menu bar as the menu bar
        // for the frame so it will display properly. Then bring it to front
        final ViewWindowListener viewWindowListener = new ViewWindowListener(this, documentScopeManager, documentScopeID, frame);
        frame.addWindowListener(viewWindowListener);

        // Do nothing on the close. The ViewWindowListener will deal with it
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Establish the current menu bar
        frame.setJMenuBar(menuBar);
        bringViewToFront(frame);
    }


    /**
     * Bring the specified frame to the front
     *
     * @param frame The frame to bring to front
     */
    private void bringViewToFront(JFrame frame) {
        frame.setVisible(true);
        frame.toFront();
    }

    
    /**
     * buildMenuBar is called following a change to the document component stack
     * to rebuild the active menu bar. It walks the stack looking for the
     * first document component that provides a menu bar description, and then adapts
     * to that description (if any).
     */
    protected void buildMenuBar(final List<Object> handlers) {

        List<Object> handlersReversed = new ArrayList<>(handlers);
        Collections.reverse(handlersReversed);

        // Find the first document component that provides a menu bar, and use it
        // to update the menu bar. This replaces the menu bar but does
        // nothing in terms of updating the items. For many applications
        // the application itself will provide the menu bar; for applications
        // with view-sensitive menus it might make sense for the view
        // or model to provide the menu bar instead. Since those are
        // higher in the document component stack, they will take precedence.
        handlersReversed.stream()
                .map(MenuLoader::build)
                .filter(Objects::nonNull)
                .findFirst()
                .ifPresentOrElse(this::menuLoaderFound,
                        () -> log.error("Internal error - no document component annotated with @MenuSource, even the last change document component"));
    }


    /**
     * Called when we have a menu loader when rebuilding the menu bar.
     * This performs the actual menu bar rebuild, as well as remembering
     * the action set so that we can rebuild the action document components later
     *
     * @param menuLoader The menu loader
     */
    private void menuLoaderFound(MenuLoader menuLoader) {

        // Rebuild the menu bar, and get all the actions from
        // the new menubar.
        // TODO dynamic menu support
        menuLoader.rebuildMenuBar(this);
    }


    /**
     * Calle when a user selects a menu item
     *
     * @param actionEvent The action event
     * @param menuItem The menu item
     * @param menuID The menu ID
     */
    public void menuItemSelected(ActionEvent actionEvent, JMenuItem menuItem, String menuID) {
        log.debug("User selected menu item {} (command ID {})", menuItem.getText(), menuID);
        fireCommand(menuID, ParameterResolution.forClass(ActionEvent.class, actionEvent));
    }


    /**
     * Called when a menu is about to be displayed. Used to update
     * the enable/disable state of menu items in the menu. The menu
     * item map passed in is the menu items for the menu being displayed.
     *
     * @param mappedMenuItems The menu items
     */
    public void aboutToDisplayMenu(Map<String,JMenuItem> mappedMenuItems) {

        // Enable all the menu items in this menu that have
        // active command methods; disable the others. For those that
        // have active command methods, delegate the enablement
        Map<String,CommandMethods> activeCommandMethodsMap = getActiveCommandMethodsMap();
        mappedMenuItems.forEach(
                (commandID, menuItem ) -> menuItem.setEnabled(
                        activeCommandMethodsMap.containsKey(commandID) &&
                        activeCommandMethodsMap.get(commandID).isEnabled(menuItem)));
    }


    /**
     * Stub functionality for dynamic menus. Not implemented yet
     *
     * @param menuID The menu ID
     * @param menu The menu
     */
    public void populateMenu(String menuID, JMenu menu) {
        log.info("Need to populate dynamic menu {}, name {}", menuID, menu.getText());
    }


    /**
     * Can be called to post an error dialog to the user.
     *
     * @param key The message key
     * @param arguments The arguments to the message
     */
    public void postErrorDialog(String key, Object... arguments) {
        String messageKey = String.format("%s_msg", key);
        String titleKey = String.format("%s_title", key);

        String defaultMessage = String.format( "Define a message %s for error %s", messageKey, key );
        String defaultTitle = String.format( "Define a message %s for title of error %s", titleKey, key);
        Locale defaultLocale = Locale.getDefault();

        String message = applicationContext.getMessage(messageKey, arguments, defaultMessage, defaultLocale);
        String title = applicationContext.getMessage(titleKey, arguments, defaultTitle, defaultLocale);
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

}
