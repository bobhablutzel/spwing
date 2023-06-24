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

package com.hablutzel.spwing.view.bind;

import com.hablutzel.spwing.events.DocumentEvent;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import com.hablutzel.spwing.events.DocumentEventInvoker;
import com.hablutzel.spwing.util.FlexpressionParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;


@Slf4j
public class ViewPropertyBinder {

    private final ApplicationContext applicationContext;
    private final DocumentEventDispatcher documentEventDispatcher;
    private final ConversionService conversionService;

    private final Map<Object, Function<Class<?>, Object>> valueSet;

    private final Map<Object, BeanWrapper> beanWrapperMap = new HashMap<>();
    private final Set<WriteBinder> writeBinders = new HashSet<>();



    public record WriteBinder(Class<?> targetClass, String propertyName, BiConsumer<Object, Accessor> bindFunction ) {
        public boolean isFor(Class<?> theClass, String propertyName) {
            return this.propertyName.equals(propertyName) && targetClass.isAssignableFrom(theClass);
        }
    }


    public ViewPropertyBinder(final ApplicationContext applicationContext,
                              final DocumentEventDispatcher documentEventDispatcher,
                              final ConversionService conversionService,
                              final Map<Object, Function<Class<?>, Object>> valueSet ) {
        this.applicationContext = applicationContext;
        this.documentEventDispatcher = documentEventDispatcher;
        this.conversionService = conversionService;
        this.valueSet = valueSet;
        initializeWriteBinders();
    }

    /**
     * Used to bind an accessor (typically a {@link PropertyAccessor})
     * to an consumer (typically a setter from a Swing component). The
     * accessor provides a mechanism for getting the value from the
     * model, while the setter provides the means for setting the value
     * in the view (Swing component). The triggers are the list of event
     * names that signal that the accessor needs to be re-evaluated. Triggers
     * allow for changes that occur to the model outside the view to be
     * recognized and reflected in the view; when the trigger events are
     * processed the accessor will be re-evaluated for the current value
     * and the setter will be re-invoked.
     *
     * @param viewPropertyAccessor The view accessor
     * @param authoritativeAccessor The accessor (typically a {@link PropertyAccessor} for a model property
     * @param triggers The trigger list for re-evaluating the accessor
     * @param expectedClass      The expected type being accessed.
     */
    private void bindWithRefresh(final Accessor viewPropertyAccessor,
                                final Accessor authoritativeAccessor,
                                final List<String> triggers,
                                final Class<?> expectedClass) {
        viewPropertyAccessor.set(authoritativeAccessor.get(expectedClass));
        if (Objects.nonNull(triggers)) {
            triggers.forEach(trigger -> {
                documentEventDispatcher.registerListener(trigger, new DocumentEventInvoker(applicationContext) {
                    @Override
                    protected void handleDocumentEvent(DocumentEvent documentEvent) {
                        if (viewPropertyAccessor.isIcon()) {
                            log.info( "Setting icon to {} (a {})", authoritativeAccessor.get(expectedClass), expectedClass.getName());
                        }
                        Object newValue = authoritativeAccessor.get(expectedClass);
                        if (!viewPropertyAccessor.get(expectedClass).equals(newValue)) {
                            viewPropertyAccessor.set(newValue);
                        }
                    }
                });
            });
        }
    }


    /**
     * Get a BeanWrapper for the specified object
     */
    public BeanWrapper beanWrapperFor(Object object) {
        if (!beanWrapperMap.containsKey(object)) {
            beanWrapperMap.put(object, new BeanWrapperImpl(object));
        }
        return beanWrapperMap.get(object);
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
     * it will be take as a literal.
     *
     * @param targetObject The object to act as a property source for property paths
     * @param valueExpression The expression
     * @return A new accessor
     */

    public Accessor toAccessor(@Nullable final Object targetObject, final String valueExpression) {
        if (Objects.nonNull(targetObject)) {
            BeanWrapper targetBeanWrapper = beanWrapperFor(targetObject);
            if (targetBeanWrapper.isReadableProperty(valueExpression)) {
                return new PropertyAccessor(targetBeanWrapper, valueExpression, conversionService);
            } else {
                SpelExpressionParser parser = new SpelExpressionParser();
                try {
                    Expression spelExpression = parser.parseExpression(valueExpression);
                    StandardEvaluationContext context = new StandardEvaluationContext(targetObject);
                    return new SpelExpressionAccessor(spelExpression, context, conversionService);
                } catch (SpelParseException e) {
                    log.info( "{} is not a SPEL expression", valueExpression);
                }
            }
        }

        // Not a property, see if it's an expression or a literal
        return FlexpressionParser.appearsToBeFlexpression(valueExpression)
                ? new FlexpressionAccessor(valueExpression, applicationContext, conversionService)
                : new LiteralAccessor(valueExpression, conversionService);
    }

    /**
     * Watch for changes in a {@link JTextComponent} subclass. This will catch
     * both changes to the document and changes to the document component itself.
     * When changes occur, the {@link Accessor} (which must be writable - see
     * {@link Accessor#isWriteable()} will be called to change the value. Typically
     * this will be a {@link PropertyAccessor} and will therefore change the
     * described property.
     *
     * @param textComponent The {@link JTextComponent} subclass instance
     * @param accessor      The accessor - typically a {@link PropertyAccessor}
     */
    public void watchText(JTextComponent textComponent, Accessor accessor) {
        TextComponentListener textComponentListener = new TextComponentListener(accessor, textComponent);
        textComponent.addPropertyChangeListener("document", (
                PropertyChangeEvent e) -> {
            Document oldDocument = (Document) e.getOldValue();
            Document newDocument = (Document) e.getNewValue();
            if (Objects.nonNull(oldDocument)) oldDocument.removeDocumentListener(textComponentListener);
            if (Objects.nonNull(newDocument)) newDocument.addDocumentListener(textComponentListener);
            textComponentListener.changedUpdate(null);
        });
        Document document = textComponent.getDocument();
        if (document != null) document.addDocumentListener(textComponentListener);
    }

    public void watchButton( AbstractButton button, Accessor accessor ) {
        button.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                boolean isButtonSelected = button.isSelected();
                boolean isModelSelected = accessor.get(Boolean.class);
                if (isButtonSelected != isModelSelected) {
                    accessor.set(isButtonSelected);
                }
            }
        });
    }



    public void bindButtonGroup( final List<AbstractButton> buttons,
                                  final String property,
                                  final Accessor accessor,
                                  final List<String> triggers ) {

        ButtonGroup buttonGroup = new ButtonGroup();
        buttons.forEach(buttonGroup::add);
        buttons.forEach( button -> {
            buttonGroup.add(button);
            button.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    Object value = valueSet.containsKey(button) ? valueSet.get(button).apply(Object.class) : null;
                    log.info( "Clicked on a button named {}, value {}", button.getName(), value);
                }
            });
        });
    }

    /**
     * Binds a property of a view {@link Component} to an authoritative value. The view property
     * can be any settable property of the component - often something like 
     * {@link javax.swing.JLabel#setText(String)} or {@link JCheckBox#setSelected(boolean)}, but could
     * be other properties ({@link JComponent#setFont(Font)} for example. The view
     * properties are specified as a property path on the given object: "text", "selected", and
     * "font" respectively for the above examples. The properties must be readable and writable
     * on the view object. <br>
     * The authoritative value represents the value that the view property is bound to. It
     * is often a {@link PropertyAccessor} representing a property of a model or controller object,
     * but could be a property of another bean (such as the application), or an expression or literal.
     * The authoritative value must be readable, and <i>may</i> be writable. If it is writeable,
     * changes by the user to the component will be written to the authoritative state. This is less
     * useful for things like {@link JComponent#setFont(Font)}; it is generally used with the primary
     * value represented by the {@link Component} - {@link JLabel#setText(String)}, {@link JCheckBox#setSelected(boolean)},
     * etc. <br>
     * The binding also allows the authoritative value to change outside the control of the bound
     * {@link Component}. This supports values that are dependent on other model values, read from databases,
     * or other use cases. The binding allows for changes in the authoritative value to be signalled by
     * one or more events, given by the triggers.
     *
     * @param viewObject              A object to bind a property against (typically a view object)
     * @param viewObjectProperty      The property to bind against
     * @param authoritativeValue      An {@link Accessor} for the authorative value
     * @param triggers                A list of triggers for updating the property by re-evaluating the expression
     * @return TRUE if the bind was successful
     */
    public boolean bind(final Component viewObject,
                        final String viewObjectProperty,
                        final Accessor authoritativeValue,
                        final List<String> triggers) {

        // Get the view object bean wrapper. Make sure the property is writable
        BeanWrapper viewObjectBeanWrapper = beanWrapperFor(viewObject);
        if (viewObjectBeanWrapper.isWritableProperty(viewObjectProperty)) {

            // Get the property type. Validate that the accessor can give a valid value
            // for that property
            PropertyAccessor viewPropertyAccessor = new PropertyAccessor(viewObjectBeanWrapper, viewObjectProperty, conversionService);
            Class<?> viewPropertyType = viewObjectBeanWrapper.getPropertyType(viewObjectProperty);
            if (authoritativeValue.canSupply(viewPropertyType)) {

                // Get a consumer for writing to this property, and bind it to the accessor
                bindWithRefresh(viewPropertyAccessor, authoritativeValue, triggers, viewPropertyType);

                // See if the authoritative value is writable
                if (authoritativeValue.isWriteable()) {
                    Class<?> viewObjectClass = viewObject.getClass();

                    // Generally speaking, we only want certain view object properties
                    // to write to the authoritative model. For JTextComponents, we want
                    // to watch when the text changes. For AbstractButtons, it is the button
                    // state, and so forth. The write binders know about the properties
                    // that we want to watch, and can do the right adaptation for
                    // changes to those properties.
                    writeBinders.stream()
                            .filter(writeBinder -> writeBinder.isFor(viewObjectClass, viewObjectProperty))
                            .findFirst()
                            .ifPresent( writeBinder -> writeBinder.bindFunction.accept(viewObject, authoritativeValue));
                } else {
                    // If we can't write the value, then prevent the user from changing it.
                    // It can still be changed by trigger events
                    viewObject.setEnabled(false);
                }
            } else {
                log.info( "Conversion service: {}", conversionService);
                log.error( "The expression {} cannot provide a {}", authoritativeValue,  viewPropertyType );
            }
            return true;

        } else {
            return false;
        }
    }


    public void initializeWriteBinders() {
        addWriteBinder(JTextComponent.class, "text", this::watchText );
        addWriteBinder(AbstractButton.class, "selected", this::watchButton);
    }

    public <T> void addWriteBinder( Class<T> targetClass, String propertyName, BiConsumer<T, Accessor> theBinder ) {
        writeBinders.add(new WriteBinder( targetClass, propertyName, (object, accessor) -> {
            if (targetClass.isInstance(object)) {
                theBinder.accept((T) object, accessor);
            }
        } ));
    }
}
