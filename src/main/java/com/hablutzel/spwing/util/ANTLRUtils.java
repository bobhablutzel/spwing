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

package com.hablutzel.spwing.util;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * A set of utilities for use with the ANTLR
 * parser generation system
 *
 * @author Bob Hablutzel
 */
public class ANTLRUtils {
    /**
     * Routine to get text from a {@link ParserRuleContext} instance.
     *
     * @param ctx The ParserRuleContext instance
     * @return The text for that context. Note this might include
     * whitespace including line feeds, so be ready for that
     */

    @SuppressWarnings("unused")
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
        // line feeds, so be careful of that!)
        return stream.getText(interval);
    }

    /**
     * Routine to get the contexts of a string literal, which
     * strips the quotes and unescapes the characters.
     * @param text  The string literal to strip
     * @return The string literal stripped of enclosing quotes
     */
    public static String stripStringLiteral(final String text) {
        // Remove quotes and unescape the text (using Java rules)
        final String strippedText = text.substring(1, text.length() - 1);
        return StringEscapeUtils.unescapeJava(strippedText);
    }

    /**
     * Routine to get the contexts of a string literal, which
     * strips the quotes and unescapes the characters
     *
     * @param token The ANTLR token. Text will be obtained from this and stripped
     * @return The string literal stripped of enclosing quotes
     */
    @SuppressWarnings("unused")
    public static String stripStringLiteral(final Token token) {
        return stripStringLiteral(token.getText());
    }

    /**
     * Return a long value from an Integer_Literal token
     * This will deal with both decimal and hex values
     *
     * @param node The node to get the text from and decode
     * @param defaultValue A default value if the parse fails
     * @return A decoded numeric value
     */
    @SuppressWarnings("unused")
    public static long getLongValue(final TerminalNode node, final long defaultValue) {
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

    /**
     * Helper routine to check the value of a token against a known
     * value; returns true if the token is non-null and text matches.
     *
     * @param value The value to check against
     * @param token The token
     * @return TRUE if the token is non-null and textually matches
     */
    public static boolean tokenEquals(@NonNull final String value, @Nullable final Token token) {
        return null != token && value.equals(token.getText());
    }

    /**
     * Return an int value from an integer literal token.
     * This will deal with both decimal and hex values
     *
     * @param token The token to decode the int value from
     * @return The decoded integer value
     */
    @SuppressWarnings("unused")
    public static int getIntValue(final Token token) {
        int radix = 10;
        int offset = 0;
        String text = token.getText();
        if (text.startsWith("0x")) {
            radix = 16;
            offset = 2;
        }
        return Integer.parseInt(text, offset, text.length(), radix);
    }


    /**
     * Get the value of a token matching "true" or "false" as a
     * boolean value.
     * @param token The token representing "true" or "false"
     * @return A boolean denoting whether the token was "true"
     */
    public static boolean getBooleanValue(final Token token) {
        return tokenEquals("true", token);
    }


    /**
     * Get the value of a token representing a floating point number
     *
     * @param token The token. Text from the token will be decoded
     * @return The decoded float value
     */
    public static float getFloatValue(final Token token) {
        return Float.parseFloat(token.getText());
    }
}
