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
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.menu.MenuLoader;
import com.hablutzel.spwing.model.ModelFactory;
import com.hablutzel.spwing.util.*;
import com.hablutzel.spwing.view.ViewWindowListener;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.*;
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
 * method will be invoked when the command is fired (see {@link #fireCommand(String, Object...)}
 * and {@link #fireCommandWithResult(String, Class, Object, Object...)}).
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

    /**
     * The {@link ApplicationConfiguration} describing how the application is configured.
     */
    private final ApplicationConfiguration applicationConfiguration;


    private final CommandMethodsScanner commandMethodsScanner;


    /**
     * The {@link ApplicationContext} instance
     */
    @Getter
    @Setter
    private ApplicationContext applicationContext;

    /**
     * The preCommandHook will be check when firing a command
     * (see {@link #fireCommand(String, Object...)}). If this
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

    /**
     * The {@link JMenuBar} used to control the main application
     * menu instance
     */
    @Getter
    private final JMenuBar menuBar = new JMenuBar();

    /**
     * The application object. This can be any class, but needs
     * to be annotated with {@link Application}
     */
    private Object application;


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

        // Create a configuration for the application, based on the context root.
        ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration(contextRoot);

        // Some OS-specific configuration has to be performed prior to the application context
        // being loaded. For example, in MacOS, we have to set the application name before any
        // Swing classes are loaded, which will (likely) occur when the application context
        // refresh call is made below. In order to ensure that we can perform those configurations
        // property, we perform some configuration now, before any Spring environment is set up
        // (which might trigger the creation of the Swing classes)
        performBootstrapConfiguration(applicationConfiguration);

        // Build the message source for the UI class; this contains all the framework messages
        PlatformResourceBundleMessageSource rootMessageSource = new PlatformResourceBundleMessageSource();
        rootMessageSource.setBundleClassLoader(Spwing.class.getClassLoader());
        rootMessageSource.addBasenames(Spwing.class.getName());
        rootMessageSource.setUseCodeAsDefaultMessage(true);

        ResourceBundleMessageSource registeredMessageSource = rootMessageSource;
        // Attempt to find a resource bundle for the application
        try {
            // See if the application resource bundle can be loaded
            getApplicationBundle(contextRoot); // Trigger the exception if not available
            PlatformResourceBundleMessageSource applicationMessageSource = new PlatformResourceBundleMessageSource();
            applicationMessageSource.setBundleClassLoader(contextRoot.getClassLoader());
            applicationMessageSource.addBasenames(contextRoot.getName());
            applicationMessageSource.setParentMessageSource(rootMessageSource);
            registeredMessageSource = applicationMessageSource;
        } catch (MissingResourceException e) {
            log.warn("No resource bundle for the application could be found, based on {}", contextRoot.getName());
            log.warn("Application specific resource bundle will not be created.");
        }

        // Signal that we are initializing the framework.er
        log.info( registeredMessageSource.getMessage("SpwingInit", null, Locale.getDefault()));

        // We use a parent bean factory to register our pre-defined items. This includes the
        // application context to start, but will also contain the document scope manager
        // once we are able to create it (we have to create it after the application context).
        DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();
        parentBeanFactory.registerSingleton("applicationConfiguration", applicationConfiguration );
        parentBeanFactory.registerSingleton("messageSource", registeredMessageSource);

        // Build the application context. We also need to create a new document scope manager
        // which will handle the "document" scope. This scope creates the equivalent of
        // a web session for the desktop applications, so that document scoped items are created
        // as singletons for each document (and kept unique and seperate from each other).
        final AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(parentBeanFactory);
        DocumentScopeManager documentScopeManager = new DocumentScopeManager(applicationContext);
        parentBeanFactory.registerSingleton("documentScopeManager", documentScopeManager );
        applicationContext.getBeanFactory().registerScope("document", new DocumentScope(documentScopeManager));

        // We allow for aliases to be defined for the beans that are tagged as application, model, or controller
        applicationContext.getBeanFactory().addBeanPostProcessor(new KeyBeanAliasProvider( applicationContext ));
        final KnownObjectsInjector knownObjectsInjector = new KnownObjectsInjector(documentScopeManager);
        applicationContext.getBeanFactory().addBeanPostProcessor(knownObjectsInjector);

        // Scan and refresh the context
        applicationContext.scan(contextRoot.getPackageName(), Spwing.class.getPackageName());
        applicationContext.refresh();

        // At this point we have the Spwing singleton bean.
        Spwing spwing = applicationContext.getBean(Spwing.class);

        // Pass that to the document scope manager so we can be alerted when document scope changes
        documentScopeManager.setSpwing(spwing);

        // Register some framework classes with the invoker so they can be injected into arguments
        Invoker.registerFrameworkAdapter( spwing::resolveInvokerParameter );

        // Allow it to be injected
        knownObjectsInjector.setSpwing(spwing);

        // Prepare the UI instance
        spwing.prepare();

        // Launch the application
        spwing.startApplication();
        return spwing;
    }


    private static ResourceBundle getApplicationBundle( final Class<?> contextRoot ) {
        return PlatformResourceUtils.platformAndBaseNames(contextRoot.getName()).stream()
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
                .orElse(null);
    }


    private Object resolveInvokerParameter(ParameterDescription parameterDescription) {
        Class<?> parameterTargetType = parameterDescription.getType();
        Object activeModel = documentScopeManager.getActiveModel();
        Object activeController = documentScopeManager.getActiveController();

        if (Window.class.isAssignableFrom(parameterTargetType)) {
            return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
        } else if (ApplicationConfiguration.class.isAssignableFrom(parameterTargetType)) {
            return applicationConfiguration;
        } else if (DocumentSession.class.isAssignableFrom(parameterTargetType)) {
            return documentScopeManager.getActiveSession();
        } else if (DocumentEventDispatcher.class.isAssignableFrom(parameterTargetType)) {
            return documentScopeManager.getDocumentEventDispatcher();
        } else if (DocumentEventPublisher.class.isAssignableFrom(parameterTargetType)) {
            return new DocumentEventPublisher(documentScopeManager.getDocumentEventDispatcher());
        } else if (Objects.nonNull(activeModel) && activeModel.getClass().isAssignableFrom(parameterTargetType)) {
            return activeModel;
        } else if (Objects.nonNull(activeController) && activeController.getClass().isAssignableFrom(parameterTargetType)) {
            return activeController;
        } else if (Objects.nonNull(this.application) && this.application.getClass().isAssignableFrom(parameterTargetType)) {
            return this.application;
        } else if (parameterDescription.hasParameterAnnotation(Application.class)) {
            return this.application;
        } else if (parameterDescription.hasParameterAnnotation(Model.class)) {
            return activeModel;
        } else if (parameterDescription.hasParameterAnnotation(Controller.class)) {
            return activeController;
        } else {
            return null;
        }
    }


    /**
     * Perform configuration that needs to be performed before the Swing or Spring
     * environments are created.
     *
     * @param applicationConfiguration The {@link ApplicationConfiguration} instance
     */
    private static void performBootstrapConfiguration(ApplicationConfiguration applicationConfiguration) {

        // Find the look and feel to load
        // TODO allow selectable LAF
        ClassUtils.find(applicationConfiguration.getContextRoot(), LookAndFeel.class).stream()
                .map(BeanDefinition::getBeanClassName)
                .forEach(log::info);

        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            performMacOSSpecificBootstrapConfiguration(applicationConfiguration);
        }

    }


    /**
     * Perform pre-Swing and pre-Spring environment configuration for MacOS
     *
     * @param applicationConfiguration The {@link ApplicationConfiguration} instance
     */
    private static void performMacOSSpecificBootstrapConfiguration(ApplicationConfiguration applicationConfiguration) {

        // Find the application name to use for the Apple menu
        String applicationName = applicationConfiguration.getApplicationName();

        // Perform general property setting to adopt the MacOS look & feel
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("apple.awt.application.name", applicationName);
        System.setProperty("apple.awt.application.appearance", "system");

    }



    /**
     * Prepare the environment for execution. This will find the
     * application instance and register it as a document component. The
     * application is found in the following sequence:
     * - First the context root is checked to see if it has
     * and {@link Application} annotation. If so, it is
     * the application instance
     * - If the context root is not the application, then a bean
     * with the name "application" is looked for. This allows
     * a configuration object to create the application bean.
     * - If both fail, the context is scanned for any bean
     * with an {@link Application} annotation, and the first one
     * encountered is used. This is non-predictive and should be
     * avoided, although it will generally work as there will
     * generally only be a single class annotation with {@link Application}
     */
    public void prepare() {

        // Get the built-in commands, and add it to the list of handlers.
        this.applicationScopeHandlers.add(applicationContext.getBean(BuiltInCommands.class));

        // We don't need to have an application bean per se; the application bean is
        // needed only if there is functionality at the application level. However,
        // we cannot have more than one application bean.
        // If there is an application bean, then remember it as an application scope handler
        final Class<?> applicationClass = applicationConfiguration.getApplicationClass();
        if (Objects.nonNull(applicationClass)) {
            // Get the scope for the application. For All-in-one applications, the
            // application annotation is only present for the configuration information
            // (application name, copyright, etc) and the class itself will be in document
            // scope. Ensure that the scope here isn't "document" before trying to create the
            // bean.
            Scope scope = AnnotatedElementUtils.getMergedAnnotation(applicationClass, Scope.class);
            if (Objects.isNull(scope) || !"document".equals(scope.scopeName())) {
                this.application = applicationContext.getBean(applicationClass);
                this.applicationScopeHandlers.add(this.application);
            }
        }

        // Build the command methods for the application scope handlers
        log.debug( "Application scope handlers: {}", applicationScopeHandlers);
        applicationCommandMethodsMap = commandMethodsScanner.scanDocumentComponents(this, applicationScopeHandlers);

        // Build the initial menu bar
        buildMenuBar(applicationScopeHandlers);

        // Get the look & feel for the application
        LookAndFeel lookAndFeel = applicationConfiguration.getLookAndFeel();
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (UnsupportedLookAndFeelException e) {
            log.error( "Look and feel {} could not be used", lookAndFeel.getName(), e);
        }

        // Create adapters for the about / preferences / quit events
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(aboutEvent -> fireCommand("cmdAbout", aboutEvent));
        desktop.setPreferencesHandler(preferencesEvent -> fireCommand("cmdPreferences", preferencesEvent));
        desktop.setQuitHandler((quitEvent, quitResponse) -> fireCommand("cmdQuit", quitEvent, quitResponse ));

        // OS specific work
        prepareOSSpecific();
    }

    /**
     * Perform OS specific initializations. This will determine the
     * active OS and dispatch the OS specific logic.
     * TODO support Linux & Windows.
     */
    private void prepareOSSpecific() {
        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            prepareMacOS();
        }
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
        Application annotation = applicationConfiguration.getApplicationAnnotation();
        if (Objects.nonNull(annotation)) {
            String initialCommand = annotation.onStart();
            if (Objects.nonNull(initialCommand) && !initialCommand.isBlank()) {
                log.debug( "Firing initial command {}", initialCommand );
                fireCommand(initialCommand, new ActionEvent(this, 0, initialCommand));
            }
        }
    }

    @Bean
    ConversionService conversionService() {
        ConversionServiceFactoryBean factory = new ConversionServiceFactoryBean();
        Set<Converter<?, ?>> convSet = new HashSet<>();
        convSet.add(new StringToFontConverter());
        convSet.add(new StringToColorConverter());
        convSet.add(new ImageToIconConverter());
        convSet.add(new StringToImageConverter(applicationConfiguration.getContextRoot()));
        convSet.add(new StringToIconConverter(applicationConfiguration.getContextRoot()));
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
        ResultHolder<Boolean> resultHolder = new ResultHolder<>(false);
        Map<String,CommandMethods> activeCommandsMap = getActiveCommandMethodsMap();
        if (activeCommandsMap.containsKey(commandID)) {
            activeCommandsMap.get(commandID).doEnable(resultHolder::set);
        }
        return resultHolder.get();
    }


    public void documentSessionChanged(final List<Object> handlers) {
        rebuildMenubarForDocumentHandlers(handlers);
    }

    private void rebuildMenubarForDocumentHandlers(List<Object> handlers) {
        buildMenuBar(Objects.isNull(handlers) ? applicationScopeHandlers : handlers);
    }


    /**
     * Method to fire a command. This finds the right action handler
     * instance for the command and invokes it.
     *
     * @param command     The command to fire
     * @param injectedObjects The parameters to inject into the invocation
     */
    public void fireCommand(String command, Object... injectedObjects) {
        Runnable runnable = fireCommandWithResult(command, Runnable.class, null, injectedObjects );
        if (Objects.nonNull(runnable)) {
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
    public <T> T fireCommandWithResult( String command, Class<T> clazz, T defaultValue, Object... injectedObjects ) {

        // Make sure we can fire the command.
        Map<String,CommandMethods> activeCommandMethodsMap = getActiveCommandMethodsMap();
        if (activeCommandMethodsMap.containsKey(command)) {

            // Get the command methods to invoke for the command
            final CommandMethods commandMethods = activeCommandMethodsMap.get(command);

            // Fire the pre-command hook (if any)
            boolean fire = Objects.isNull(preCommandHook) || preCommandHook.apply(this, command);

            // If we are still firing, then invoke the command
            if (fire) {

                T result = commandMethods.fireCommandWithResult(clazz, defaultValue, injectedObjects);

                // Call the post-command hook (if any)
                if (Objects.nonNull(postCommandHook)) {
                    postCommandHook.accept(this, command);
                }
                return result;
            }
        } else {
            log.warn( "No handler for {}", command );
            log.debug( "Known commands: {}", activeCommandMethodsMap.keySet() );
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
        ModelFactory<T> modelFactory = ModelFactory.forModelClass(applicationContext, modelClass);
        if (Objects.nonNull(modelFactory)) {
            createModelDocumentScope(modelClass, modelFactory::create);
        }
    }

    /**
     * Called to read a model from a model factory. First this creates
     * the model factory, and then calls the open method.
     *
     * @param modelClass The model class
     * @param <T> The class of the model class
     */
    public <T> void openModel(Class<T> modelClass) {

        // Get a new model factory for the model, based on the static methods open and create
        ModelFactory<T> modelFactory = ModelFactory.forModelClass(applicationContext, modelClass);
        if (Objects.nonNull(modelFactory)) {
            createModelDocumentScope(modelClass, modelFactory::open);
        }
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
                                          final Function<DocumentSession,Object> modelCreator) {

        // Create a new document session
        DocumentSession documentSession = new DocumentSession( applicationScopeHandlers );

        // Get a new model factory for the model, based on the static methods open and create
        Object modelInstance = modelCreator.apply(documentSession);
        Model model = AnnotatedElementUtils.getMergedAnnotation(modelClass, Model.class);
        if (Objects.nonNull(modelInstance) && Objects.nonNull(model)) {

            // Create a new document scope for this newly created model object
            UUID documentScopeID = documentScopeManager.establishSession(documentSession, modelInstance);

            // Get the associated controller. Make sure it's annotated properly. If not, it will
            // not be properly added to the document scope handler list
            Object controllerInstance = createControllerForModel(modelClass, model, modelInstance);
            if (Objects.nonNull(controllerInstance)) {
                final Class<?> controllerClass = controllerInstance.getClass();
                Controller controller = AnnotatedElementUtils.getMergedAnnotation(controllerClass, Controller.class);
                if (Objects.isNull(controller)) {
                    log.warn("Class {} is not annotated as a controller", controllerClass.getName());
                }
            }

            try {
                // Get the view factory associated with the model, and open the view
                openView(documentScopeID, model.viewFactory());
            } finally {

                // Have the document session scan for command handlers, listeners, etc.
                // Do this after the view is created so we can map the listeners attached
                // to view elements. We do this even if the view failure fails, so that
                // there are command methods available (Quit, for example)
                documentSession.scanHandlers(this, commandMethodsScanner);

                // Rebuild the menu bar, now that we have the controller, etc.
                rebuildMenubarForDocumentHandlers(documentSession.getAvailableHandlers());

            }
        }
    }


    /**
     * Figure out the {@link Controller} associated with this model (if any) and open it.
     *
     * @param modelClass      The class for the model instance
     * @param model           The {@link Model} annotation
     * @param modelInstance   The model instance
     */
    private Object createControllerForModel(
            final Class<?> modelClass,
            final Model model,
            final Object modelInstance) {

        // If the model is also the controller (supported for simple and ported applications)
        // then there is no need to create a new controller instance; use the model.
        if (Objects.nonNull(AnnotatedElementUtils.getMergedAnnotation(modelClass, Controller.class))) {
            // The model is also the controller
            return modelInstance;
        } else if (!Object.class.equals(model.controller())) {
            // The model annotation denotes a controller class, use that one
            // Let the application factory create it as needed
            return applicationContext.getBean(model.controller());
        } else {
            // We need to infer the controller class name from the model name.
            // For the class named XXXModel, look for a controller named XXXController
            String modelClassName = modelInstance.getClass().getName();
            if (modelClassName.endsWith("Model" )) {
                String truncatedName = modelClassName.substring(0, modelClassName.length() - 5 );
                String candidateClassName = truncatedName + "Controller";

                try {
                    return applicationContext.getBean(Class.forName(candidateClassName));
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
     * object if the {@link Model#controller()} value is specified. The class specified
     * is checked to make sure it has the {@link Controller} annotation on it; if not the
     * class is ignored.
     *
     * @param documentScopeID The document scope to add the view to
     * @param viewFactoryClass The view factory class
     */
    public void openView(UUID documentScopeID, @NonNull Class<?> viewFactoryClass) {

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
                    setUpFrame(documentScopeID, frame);
                } else {
                    // Build a default JFrame around this component
                    JFrame frame = new JFrame(applicationConfiguration.getApplicationName());
                    frame.setMinimumSize(new Dimension(200, 100));

                    // add the component
                    frame.getContentPane().add(component);
                    frame.pack();

                    setUpFrame(documentScopeID, frame);
                }
            } else {
                log.error( "Called the build method on {}, but it returned null.", viewFactoryClass.getName());
            }
        }
    }

    private void setUpFrame(UUID documentScopeID, JFrame frame) {
        // Save the frame in the document scope
        documentScopeManager.storeBeanInActiveDocumentBeanStore("jFrame", frame);

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


    public void menuItemSelected(ActionEvent actionEvent, JMenuItem menuItem, String menuID) {
        log.debug("User selected menu item {} (command ID {})", menuItem.getText(), menuID);
        fireCommand(menuID, actionEvent);
    }

    public void aboutToDisplayMenu(String menuID, JMenu menu, Map<String,JMenuItem> mappedMenuItems) {

        // Enable all the menu items in this menu that have
        // active command methods; disable the others. For those that
        // have active command methods, delegate the enablement
        Map<String,CommandMethods> activeCommandMethodsMap = getActiveCommandMethodsMap();
        mappedMenuItems.forEach( (commandID, menuItem ) -> {
            if (activeCommandMethodsMap.containsKey(commandID)) {
                activeCommandMethodsMap.get(commandID).doEnable(menuItem::setEnabled);
            } else {
                menuItem.setEnabled(false);
            }
        });
    }

    public void populateMenu(String menuID, JMenu menu) {
        log.info("Need to populate dynamic menu {}, name {}", menuID, menu.getText());
    }

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

    @Configuration
    @PropertySource("classpath:application.properties")
    public static class PropertiesWithJavaConfig {
    }
    // TODO document the need for application properties

}
