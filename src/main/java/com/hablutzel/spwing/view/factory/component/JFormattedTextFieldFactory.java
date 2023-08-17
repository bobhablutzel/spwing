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

package com.hablutzel.spwing.view.factory.component;

import com.hablutzel.spwing.view.adapter.JFormattedTextFieldAdapter;
import com.hablutzel.spwing.view.factory.cocoon.Cocoon;
import com.hablutzel.spwing.view.factory.cocoon.JTextComponentCocoon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;

import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.DateFormatter;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Create a new {@link JTextField} instance, including registering an
 * event adapter for that instance with the current document event dispatcher.
 *
 * @author Bob Hablutzel
 */
@Service
@Scope("singleton")
@Slf4j
public final class JFormattedTextFieldFactory extends AbstractViewComponentFactory<JFormattedTextField> {


    @Bean
    @Scope("singleton" )
    public DateFormat dateFormat() {
        return DateFormat.getDateInstance();
    }

    @Bean
    @Scope("singleton" )
    public DateFormat timeFormat() {
        return DateFormat.getTimeInstance();
    }

    @Bean
    @Scope("singleton" )
    public DateFormat dateTimeFormat() {
        return DateFormat.getDateTimeInstance();
    }

    @Bean
    @Scope("singleton" )
    public DateFormat utcFormat() {
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return simpleDateFormat;
    }

    @Bean
    @Scope("singleton")
    public NumberFormat numberFormatPercent() {
        return NumberFormat.getPercentInstance();
    }


    @Bean
    @Scope("singleton")
    public NumberFormat numberFormat() {
        return NumberFormat.getNumberInstance();
    }

    @Bean
    @Scope("singleton")
    public NumberFormat numberFormatCurrency() {
        return NumberFormat.getCurrencyInstance();
    }

    @Bean
    @Scope("singleton")
    public NumberFormat numberFormatCompact() {
        return NumberFormat.getCompactNumberInstance();
    }

    @Bean
    @Scope("singleton")
    public NumberFormat numberFormatInteger() {
        return NumberFormat.getIntegerInstance();
    }

    @Override
    public Cocoon<JFormattedTextField> build(String name, ConversionService conversionService) {
        JFormattedTextField textField = new JFormattedTextField();
        registerAdapter(textField, name, JFormattedTextFieldAdapter::new);
        return new JTextComponentCocoon<>(textField, this, conversionService) {

            @Override
            public boolean canSetProperty(String propertyName) {
                return "format".equals(propertyName) || super.canSetProperty(propertyName);
            }

            @Override
            public void setProperty(String propertyName, Object value) {
                if ("format".equals(propertyName)) {

                    // The below code deals with a lot of flexibility in the setting
                    // of the format - it could be a format, a format factory, or even
                    // a string. It would be great if the conversion service could
                    // lend a hand here, but there is a bit too much nuance so we we
                    // handle things individually
                    if (value instanceof JFormattedTextField.AbstractFormatterFactory formatterFactory) {
                        textField.setFormatterFactory(formatterFactory);
                    } else if (value instanceof NumberFormat numberFormat) {
                        textField.setFormatterFactory(new BridgeFormatterFactory(new NumberFormatter(numberFormat)));
                    } else if (value instanceof DateFormat dateFormat) {
                        textField.setFormatterFactory(new BridgeFormatterFactory(new DateFormatter(dateFormat)));
                    } else if (value instanceof String maskFormatString) {
                        try {
                            final MaskFormatter maskFormatter = new MaskFormatter(maskFormatString);
                            log.info( "Before setting for format to {}, value is {}", maskFormatString, textField.getValue());
                            textField.setFormatterFactory(new BridgeFormatterFactory(maskFormatter));
                            log.info( "After setting for format to {}, value is {}", maskFormatString, textField.getValue());
                        } catch (ParseException e) {
                            log.error( "Attempted to use {} as a mask format, but it could not be parsed.", maskFormatString );
                        }
                    } else if (value instanceof JFormattedTextField.AbstractFormatter formatter) {
                        textField.setFormatterFactory(new BridgeFormatterFactory(formatter));
                    } else {
                        log.error("Cannot use {} as a format for a formatted text field", value);
                    }
                } else {
                    super.setProperty(propertyName, value);
                }
            }

            @Override
            public AllowedBindings allowedBindings(String propertyName) {
                return switch (propertyName) {
                    case "format" -> AllowedBindings.FROM_MODEL;
                    case "value" -> AllowedBindings.BIDIRECTIONAL;
                    default -> super.allowedBindings(propertyName);
                };
            }
        };
    }

    @Override
    public String alias() {
        return "JFormattedTextField";
    }



    @RequiredArgsConstructor
    private static class BridgeFormatterFactory extends JFormattedTextField.AbstractFormatterFactory {
        private final JFormattedTextField.AbstractFormatter formatter;

        public JFormattedTextField.AbstractFormatter getFormatter(final JFormattedTextField formattedTextField) {
            formatter.install(formattedTextField);
            return formatter;
        }
    }

}
