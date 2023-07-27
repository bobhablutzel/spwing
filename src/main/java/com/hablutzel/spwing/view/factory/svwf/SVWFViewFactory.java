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
import com.hablutzel.spwing.annotations.Model;
import com.hablutzel.spwing.events.DocumentEventDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;


@Slf4j
public class SVWFViewFactory {
    /**
     * The {@link #fromStream(Object, Spwing, InputStream)}
     * method creates a set of Swing components as described by the SVWF file accessed by the given
     * input stream.
     *
     * @param model                   The model associated with the view
     * @param spwing                  The framework instance
     * @param viewDescriptionFile     The input stream containing the SVWF file
     * @return A new root component for the view
     * @throws IOException If the stream cannot be read
     */
    public Component fromStream(final @Model Object model,
                                final Spwing spwing,
                                final InputStream viewDescriptionFile) throws IOException {

        if (null != viewDescriptionFile) {

            // Get the conversion service and document event dispatcher
            final ApplicationContext applicationContext = spwing.getApplicationContext();
            final DocumentEventDispatcher documentEventDispatcher = DocumentEventDispatcher.get(applicationContext);
            final ConversionService conversionService = applicationContext.getBean(ConversionService.class);

            // Create a component map for this parse, and include all the predefined components.
            SVWFParseContext svwfParseContext = new SVWFParseContext( spwing, applicationContext, documentEventDispatcher);

            // Create a listener for the parse tree. This will control the actual logic
            // for taking the actions implied by the parse tree
            final SVWFListener listener = new SVWFListener(
                    model, spwing, applicationContext,
                    conversionService, documentEventDispatcher, svwfParseContext);

            // Parse the input stream using the ANTLR parser
            final CharStream charStream = CharStreams.fromStream(viewDescriptionFile);
            SpwingViewFileLexer lexer = new SpwingViewFileLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            SpwingViewFileParser parser = new SpwingViewFileParser(tokens);
            final SpwingViewFileParser.SvwfFileContext svwfFileContext = parser.svwfFile();

            // Walk the resulting parse tree to create the Swing components
            ParseTreeWalker.DEFAULT.walk(listener, svwfFileContext);
            return listener.isCleanParse() ? listener.rootComponent() : null;
        } else {
            log.warn("Invalid input stream");
            throw new RuntimeException("Null input stream provided");
        }
    }

}
