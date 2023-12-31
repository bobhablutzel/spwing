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

package com.hablutzel.spwing.component;

import com.hablutzel.spwing.Spwing;
import com.hablutzel.spwing.annotations.EnablerFor;
import com.hablutzel.spwing.annotations.Handler;
import com.hablutzel.spwing.annotations.HandlerFor;
import com.hablutzel.spwing.annotations.ListenerFor;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.events.EventNameDeterminant;
import com.hablutzel.spwing.invoke.ReflectiveInvoker;
import com.hablutzel.spwing.view.adapter.EventAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * {@link CommandMethodsScanner} has the responsibility of
 * scanning the document component stack to find method of note -
 * command handlers, command enablers, and event listeners.<br>
 * Handlers are methods that can be called in response to commands.
 * Enabler are methods that determine if a command is enabled (and must
 * return boolean). Listeners react to either document or AWT events.<br>
 * For each of the method types, there are two ways the method can be
 * identified as a matching method. First, the annotations {@link HandlerFor},
 * {@link EnablerFor}, and {@link ListenerFor} can be used. {@link ListenerFor}
 * can be repeated for multiple event types; the other types cannot. Alternatively.
 * a convention for method naming can be used. Handler methods begin with "handle";
 * enabler methods begin with "enable", and listener methods begin with "on". The
 * name of the command or event followed. For handler and enables, the name is
 * prepended with <code>cmd</code>, so <code>handleSave</code> is the handler for
 * <code>cmdSave</code>. For listeners, the method name can contain one or two
 * parts. If the second part is present, the two parts are separated by an underscore
 * character ("_"). The first part is optional; if present it gives the name of an
 * AWT target component that will emit the event. The second part is not optional and defines
 * the event. <br>
 * There are two event families: AWT and document. The AWT events align to the method
 * name of the listener object that would have been added to the AWT component. So typical
 * AWT event names might be <code>mouseMoved</code> for {@link java.awt.event.MouseMotionListener#mouseMoved(MouseEvent)}
 * and so forth. The code is forgiving with respect to the first letter of the event when
 * using the method name convention (but not the {@link ListenerFor} annotation), so
 * a method called <code>onMouseMoved</code> will be recognized as reacting to mouseMoved
 * events even though the first letter is capitalized. (You could, of course, also call the
 * method <code>onmouseMoved</code>). Similarly, the first letter of the target, if supplied,
 * can be case insensitive. Be careful, however, about naming two components almost the same thing
 * except the case of the first letter.<br>
 * Document events by convention begin with "evt" (which no AWT event does). The application
 * author can provide a bean of type {@link EventNameDeterminant} if another convention is preferred.
 *
 * @author Bob Hablutzel
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Scope("singleton")
public class CommandMethodsScanner {

    /**
     * Regex to match method names. This is used for methods that are
     * not annotated.
     */
    private static final Pattern methodNamePattern = Pattern.compile("(enable|handle|on)((([A-Za-z][A-Za-z0-9]+)_)?([A-Za-z][A-Za-z0-9]+))");

    /**
     * The (potentially null) {@link EventNameDeterminant}
     */
    private final @Nullable EventNameDeterminant eventNameDeterminant;


    /**
     * Scan the document components. The list will be provided in
     * least to most specific, so commands will overwrite commands from
     * earlier document components.
     *
     * @param spwing The {@link Spwing} instance
     * @param documentComponents The document components
     * @return A mapping from command names to {@link CommandMethods} instances
     */
    public Map<String, CommandMethods> scanDocumentComponents(final Spwing spwing,
                                                              final List<Object> documentComponents) {

        log.debug("Scanning document components {}", documentComponents);

        Scanner scanner = new Scanner(spwing);
        return scanner.doScan(documentComponents);
    }


    /**
     * Class for scanning individual classes for command handlers,
     * enablers, and listeners. As document classes (controllers, models)
     * and application classes (root context classes, built in commands) are
     * added to framework, they are maintained in a hierarchy arranged from
     * the least customized to the most customized - that is, the built in
     * commands will be in the list before the document controller. This
     * allows the document specific classes to override the built-in
     * functionality just be defining a new method that provides the funtionality.
     *
     * @author Bob Hablutzel
     */
    private final class Scanner {

        /**
         * The resulting map of command names to {@link CommandMethods}
         */
        private final Map<String, CommandMethods> commandMethodsMap = new HashMap<>();

        /**
         * The document event dispatcher will be determined from the active session
         */
        private final DocumentEventDispatcher documentEventDispatcher;

        /**
         * The {@link ApplicationContext} instance
         */
        private final ApplicationContext applicationContext;


        /**
         * Constructor.
         * @param spwing The {@link Spwing} instance
         */
        Scanner(Spwing spwing) {
            this.applicationContext = spwing.getApplicationContext();
            this.documentEventDispatcher = spwing.getDocumentScopeManager().getDocumentEventDispatcher();
        }


        /**
         * Helper routine to lower case the first letter of a string
         *
         * @param input The input string
         * @return The input string with the first letter moved to lower case
         */
        private static String lowerCaseFirst(final String input) {
            return input.substring(0, 1).toLowerCase() + input.substring(1);
        }

        /**
         * Scan a list of components. These components represent objects
         * that can potentially provide handlers, enablers, and listeners and
         * are comprised of the application, the model, the controller, and
         * framework components such as the built-in commands.
         *
         * @param documentComponents The list of document components
         * @return The map of command names to {@link CommandMethods}
         */
        public Map<String, CommandMethods> doScan(final List<Object> documentComponents) {

            // Scan the components. We will get the components in least- to most-important
            // order, so more important components will automatically override functionality
            // in less important ones. This allows us to provide default behavior and have
            // the implementation override that behavior.
            documentComponents.forEach(this::scanDocumentComponent);

            // Debug information about the action table
            log.debug("After rebuilding the action set: ");
            commandMethodsMap.forEach((k, v) -> log.debug("   {} => {}", k, v));
            return commandMethodsMap;
        }

        /**
         * Scan a single document component for methods of interest
         *
         * @param documentComponent The component to scan
         */
        private void scanDocumentComponent(final Object documentComponent) {

            log.debug("Scanning document component class {}", documentComponent.getClass().getName());

            // Scan all the methods of this class. We include
            // classes from the superclass. Each of the methods will be evaluated
            // to see if it is a handler or enabler for a command, or a listener for an event
            final Method[] methods = documentComponent.getClass().getMethods();
            Arrays.stream(methods)
                    .filter(method -> !Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()))
                    .forEach(method -> evaluateAsTargetMethod(documentComponent, method));
        }

        /**
         * Evaluates this method as a handler, enabler, or listener. There are two ways
         * that a method can be determined to be a target method: (1) they can be
         * annotated (by {@link HandlerFor}, {@link EnablerFor} or {@link ListenerFor}), or (2)
         * they can be named via convention. Methods with names that match handleXXX or
         * enableXXX are taken as the handler or enabler for cmdXXX (respectively), while
         * methods that match onXXX are taken as event listeners<br>
         * Note that a handler method can return any value; if the value returned
         * is a {@link Runnable} the framework will execute it after the command handler
         * returns via {@link SwingUtilities#invokeLater(Runnable)}; the quit/close/save processing
         * expects booleans to be returned to denote whether to return; any other return
         * types will generally be ignored. The enable method must return a boolean value.
         * Listener methods do not return a value. Methods that do not match this requirement are rejected. <br>
         * The methods can take any arguments that are available as beans, special annotations,
         * expressions, etc. See {@link ReflectiveInvoker} for more details.
         *
         * @param documentComponent The component object - an instance of a class annotated with {@link Handler}
         * @param method            The method of that component object under evaluation
         */
        private void evaluateAsTargetMethod(final Object documentComponent,
                                            final Method method) {

            // Analyze the method name
            final String methodName = method.getName();
            final Matcher matcher = methodNamePattern.matcher(methodName);
            String methodFlag = matcher.matches() ? matcher.group(1) : "";
            String methodNameSuffix = matcher.matches() ? matcher.group(2) : "";

            // Look for annotations
            HandlerFor handlerFor = AnnotatedElementUtils.getMergedAnnotation(method, HandlerFor.class);
            EnablerFor enablerFor = AnnotatedElementUtils.getMergedAnnotation(method, EnablerFor.class);
            Set<ListenerFor> listenerForSet = AnnotatedElementUtils.getMergedRepeatableAnnotations(method, ListenerFor.class);

            // We have a handler if the methodFlag is "handler" or the method has a HandlerFor
            // annotation
            final ReflectiveInvoker reflectiveInvoker = new ReflectiveInvoker(applicationContext, documentComponent, method);
            if ("handle".equals(methodFlag) || null != handlerFor) {
                // It is a handler, link this to the handler in the action list
                defineHandler(methodNameSuffix, handlerFor, reflectiveInvoker);
            } else if ("enable".equals(methodFlag) || null != enablerFor) {
                // It is an enabler, associated it with the command action.
                defineEnabler(methodNameSuffix, enablerFor, reflectiveInvoker);
            } else if (!listenerForSet.isEmpty()) {

                // Listen for all the events that this method listens for
                listenerForSet.forEach(listenerFor -> defineListener(reflectiveInvoker, listenerFor.event(), listenerFor.target(), listenerFor.determination()));
            } else if ("on".equals(methodFlag)) {

                // Deal with case insensitivity for the first letter of the target (first) and event.
                // We do the target first so we can identify the target, if any, when understanding
                // whether the event name is understood.
                String targetName = sanitizeTargetName(matcher.group(4));
                String eventName = sanitizeEventName(matcher.group(5), targetName);
                defineListener(reflectiveInvoker, eventName, targetName, EventFamily.Introspection);
            }
        }

        /**
         * Deal with case insensitivity for event names. This
         * only applies to AWT event names, because the document
         * event name convention assumes the proposed event name
         * will be upper case. For AWT event names, the actual event
         * name is lower case so it has to be adjusted.
         *
         * @param proposedEventName The proposed event name
         * @param targetName        The target of the event (could be null)
         * @return The event name to use
         */
        private String sanitizeEventName(final String proposedEventName,
                                         final String targetName) {

            // This can only be null when no view is open - meaning no AWT events.
            // Nothing to do here in that case
            if (null != documentEventDispatcher) {

                // Get the event adapter map from the document event dispatcher.
                // This knows about all the names AWT components.
                final Map<String, EventAdapter> eventAdapterMap = documentEventDispatcher.getEventAdapterMap();

                // If we have a target name specified, get that specific adapter
                if (null != targetName && !targetName.isBlank()) {
                    EventAdapter eventAdapter = eventAdapterMap.get(targetName);

                    // If the event adapter already understands the event name, use it.
                    // Otherwise try the lower-case version
                    if (null != eventAdapter && !eventAdapter.understands(proposedEventName)) {
                        String testName = lowerCaseFirst(proposedEventName);
                        if (eventAdapter.understands(testName)) {
                            return testName;
                        }
                    }
                } else {

                    // No specific target named. See if any target can deal with this event type
                    boolean someoneUnderstands = eventAdapterMap.values().stream()
                            .anyMatch(eventAdapter -> eventAdapter.understands(proposedEventName));
                    if (!someoneUnderstands) {

                        // No target understood that name, try the lower-case version
                        String testName = lowerCaseFirst(proposedEventName);
                        boolean someoneUnderstandsTestName = eventAdapterMap.values().stream()
                                .anyMatch(eventAdapter -> eventAdapter.understands(testName));
                        if (someoneUnderstandsTestName) {
                            return testName;
                        } else {
                            // No target specified, and no AWT event handler knows this method.
                            // Treat it as a document event, which means adding the "evt" tag
                            return toDocumentEvent(proposedEventName);
                        }
                    }
                }
            }
            return proposedEventName;
        }

        /**
         * When the target name is inferred from the method name, it could be
         * specified with a leading upper case letter rather than a lower-case
         * letter. In this case, we have to see if there is a match as it is, and
         * if not attempt it with a lower case.
         *
         * @param proposedName The proposed name
         * @return The actual sanitized name
         */
        private String sanitizeTargetName(String proposedName) {
            if (null != proposedName && !proposedName.isBlank() && null != documentEventDispatcher) {
                final Map<String, EventAdapter> eventAdapterMap = documentEventDispatcher.getEventAdapterMap();
                if (!eventAdapterMap.containsKey(proposedName)) {
                    String testName = lowerCaseFirst(proposedName);
                    if (eventAdapterMap.containsKey(testName)) {
                        return testName;
                    } else {
                        // Can't find it - good luck!
                        log.warn("Target name {} is not found as an AWT component name", proposedName);
                    }
                }
            }
            return proposedName;
        }

        /**
         * Add a new enabler to the command methods being built
         *
         * @param methodNameSuffix The method name suffix in case we have to build a command name on the fly
         * @param enablerFor The {@link EnablerFor} annotation if specified
         * @param reflectiveInvoker The {@link ReflectiveInvoker} representing how to call the method
         */
        private void defineEnabler(final String methodNameSuffix,
                                   final EnablerFor enablerFor,
                                   final ReflectiveInvoker reflectiveInvoker) {
            String commandID = null != enablerFor ? enablerFor.value() : methodNameToCommandName(methodNameSuffix);
            getCommandMethods(commandID).setEnabler(reflectiveInvoker);
        }


        /**
         * Defines a handler method. Adds the handler method to the command
         * method for the command associated with the method name or annotation
         * @param methodNameSuffix The method name suffix for inferred command names
         * @param handlerFor The {@link HandlerFor} annotation providing the command name
         * @param reflectiveInvoker The {@link ReflectiveInvoker} used to call the method
         */
        private void defineHandler(String methodNameSuffix, HandlerFor handlerFor, ReflectiveInvoker reflectiveInvoker) {
            String commandID = null != handlerFor ? handlerFor.value() : methodNameToCommandName(methodNameSuffix);
            getCommandMethods(commandID).setHandler(reflectiveInvoker);
        }


        /**
         * Convert a method name suffix to a command name
         * @param methodNameSuffix The method name suffix
         * @return The corresponding command name
         */
        private String methodNameToCommandName(String methodNameSuffix) {
            return String.format("cmd%s", methodNameSuffix);
        }


        /**
         * Define a listener function. Adds the listener to the
         * document event dispatcher that is associated with the current
         * document so that when events are dispatched, the dispatcher
         * knows what methods to call
         *
         * @param reflectiveInvoker The {@link ReflectiveInvoker} to call the listener function
         * @param eventName The event name
         * @param target The target object. This is the name of the view component
         *               that the event will be received from, and could be null or blank
         *               in which case the event will come from any active view component
         * @param eventFamily The event family - used to determine if the event was
         *                    by convention or annotation
         */
        private void defineListener(final ReflectiveInvoker reflectiveInvoker,
                                    final String eventName,
                                    final String target,
                                    final EventFamily eventFamily) {

            // Make sure there is an document event dispatch available
            if (null != documentEventDispatcher) {
                // Use the family passed in, or attempt to guess if needed.
                EventFamily family = eventFamily == EventFamily.Introspection
                        ? determineEventFamily(eventName)
                        : eventFamily;

                // For AWT events, get the adapter map from the document event
                // dispatcher. This lets us find the specific event adapter for the
                // target of the event, or in the case the target is specified all
                // the component event adapters.
                if (family == EventFamily.AWT) {
                    Map<String, EventAdapter> eventAdapterMap = documentEventDispatcher.getEventAdapterMap();
                    if (null != target && !target.isBlank()) {

                        // This is attempting to target a specific component. See
                        // that we have that component
                        EventAdapter eventAdapter = eventAdapterMap.get(target);

                        // We have it - can it understand the event
                        if (null != eventAdapter && eventAdapter.understands(eventName)) {
                            eventAdapter.attachListener(eventName, reflectiveInvoker);
                        } else {
                            log.warn("Unknown AWT event {} ignored - no listener available", eventName);
                        }
                    } else {
                        // This applies to all the elements that can emit this event type
                        eventAdapterMap.values().stream()
                                .filter(eventAdapter -> eventAdapter.understands(eventName))
                                .forEach(eventAdapter -> eventAdapter.attachListener(eventName, reflectiveInvoker));
                    }
                } else {

                    // This is a document event that we're listening for
                    documentEventDispatcher.registerListener(eventName, reflectiveInvoker);
                }
            }
        }


        /**
         * Changes a string from a name into a document event. If it is
         * already in the format of a document event, return it unchanged
         * @param name The input string
         * @return The name as a document event
         */
        private String toDocumentEvent(final String name) {
            return null != eventNameDeterminant
                    ? eventNameDeterminant.toDocumentEvent(name)
                    : String.format("evt%s", name);
        }


        /**
         * Determine if an event is an AWT or a document event
         *
         * @param eventName The event name
         * @return The {@link EventFamily} associated with the event
         */
        private EventFamily determineEventFamily(final String eventName) {
            EventFamily result = null != eventNameDeterminant
                    ? eventNameDeterminant.examineName(eventName)
                    : EventFamily.Introspection;

            return result == EventFamily.Introspection
                    ? eventName.startsWith("evt") ? EventFamily.Document : EventFamily.AWT
                    : result;
        }


        /**
         * Retrieve the {@link CommandMethods} for the command id, creating it
         * if necessary.
         *
         * @param commandID The command ID
         * @return The {@link CommandMethods} associated with command ID
         */
        private CommandMethods getCommandMethods(String commandID) {
            if (!commandMethodsMap.containsKey(commandID)) {
                CommandMethods commandMethods = new CommandMethods();
                commandMethodsMap.put(commandID, commandMethods);
                return commandMethods;
            } else {
                return commandMethodsMap.get(commandID);
            }
        }

    }
}
