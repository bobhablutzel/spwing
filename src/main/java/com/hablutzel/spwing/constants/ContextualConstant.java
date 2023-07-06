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

package com.hablutzel.spwing.constants;

import org.springframework.lang.NonNull;

import javax.swing.*;
import java.awt.*;

public abstract class ContextualConstant {

    @NonNull
    public abstract Object get(@NonNull ConstantContext context);

    public static ContextualConstant CENTER = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.CENTER : SwingConstants.CENTER;
        }
    };

    public static ContextualConstant TOP = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.TOP;
        }
    };

    public static ContextualConstant LEFT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.LEFT;
        }
    };

    public static ContextualConstant BOTTOM = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.BOTTOM;
        }
    };

    public static ContextualConstant RIGHT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.RIGHT;
        }
    };

    public static ContextualConstant NORTH = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.NORTH : SwingConstants.NORTH;
        }
    };

    public static ContextualConstant NORTH_EAST = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.NORTHEAST : SwingConstants.NORTH_EAST;
        }
    };

    public static ContextualConstant EAST = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.EAST : SwingConstants.EAST;
        }
    };

    public static ContextualConstant SOUTH_EAST = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.SOUTHEAST : SwingConstants.SOUTH_EAST;
        }
    };

    public static ContextualConstant SOUTH = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.SOUTH : SwingConstants.SOUTH;
        }
    };

    public static ContextualConstant SOUTH_WEST = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.SOUTHWEST : SwingConstants.SOUTH_WEST;
        }
    };

    public static ContextualConstant WEST = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.WEST : SwingConstants.WEST;
        }
    };

    public static ContextualConstant NORTH_WEST = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.NORTHWEST : SwingConstants.NORTH_WEST;
        }
    };

    public static ContextualConstant HORIZONTAL = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.HORIZONTAL : SwingConstants.HORIZONTAL;
        }
    };

    public static ContextualConstant VERTICAL = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return context == ConstantContext.GridBagConstants ? GridBagConstraints.VERTICAL : SwingConstants.VERTICAL;
        }
    };

    public static ContextualConstant LEADING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.LEADING;
        }
    };

    public static ContextualConstant TRAILING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.TRAILING;
        }
    };

    public static ContextualConstant NEXT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.NEXT;
        }
    };

    public static ContextualConstant PREVIOUS = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return SwingConstants.PREVIOUS;
        }
    };

    public static ContextualConstant LEFT_ALIGNMENT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return Component.LEFT_ALIGNMENT;
        }
    };

    public static ContextualConstant RIGHT_ALIGNMENT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return Component.RIGHT_ALIGNMENT;
        }
    };

    public static ContextualConstant CENTER_ALIGNMENT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return Component.CENTER_ALIGNMENT;
        }
    };

    public static ContextualConstant BOTTOM_ALIGNMENT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return Component.BOTTOM_ALIGNMENT;
        }
    };

    public static ContextualConstant TOP_ALIGNMENT = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return Component.TOP_ALIGNMENT;
        }
    };

    public static ContextualConstant RELATIVE = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.RELATIVE;
        }
    };

    public static ContextualConstant REMAINDER = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.REMAINDER;
        }
    };

    public static ContextualConstant NONE = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.NONE;
        }
    };

    public static ContextualConstant BOTH = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.BOTH;
        }
    };


    public static ContextualConstant PAGE_START = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.PAGE_START;
        }
    };

    public static ContextualConstant PAGE_END = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.PAGE_END;
        }
    };

    public static ContextualConstant LINE_START = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.LINE_START;
        }
    };

    public static ContextualConstant LINE_END = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.LINE_END;
        }
    };

    public static ContextualConstant FIRST_LINE_START = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.FIRST_LINE_START;
        }
    };

    public static ContextualConstant FIRST_LINE_END = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.FIRST_LINE_END;
        }
    };

    public static ContextualConstant LAST_LINE_START = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.LAST_LINE_START;
        }
    };

    public static ContextualConstant LAST_LINE_END = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.LAST_LINE_END;
        }
    };

    public static ContextualConstant BASELINE = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.BASELINE;
        }
    };

    public static ContextualConstant BASELINE_LEADING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.BASELINE_LEADING;
        }
    };

    public static ContextualConstant BASELINE_TRAILING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.BASELINE_TRAILING;
        }
    };

    public static ContextualConstant ABOVE_BASELINE = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.ABOVE_BASELINE;
        }
    };

    public static ContextualConstant ABOVE_BASELINE_LEADING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.ABOVE_BASELINE_LEADING;
        }
    };

    public static ContextualConstant ABOVE_BASELINE_TRAILING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.ABOVE_BASELINE_TRAILING;
        }
    };

    public static ContextualConstant BELOW_BASELINE = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.BELOW_BASELINE;
        }
    };

    public static ContextualConstant BELOW_BASELINE_LEADING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.BELOW_BASELINE_LEADING;
        }
    };

    public static ContextualConstant BELOW_BASELINE_TRAILING = new ContextualConstant() {
        @Override
        @NonNull
        public Object get(@NonNull ConstantContext context) {
            return GridBagConstraints.BELOW_BASELINE_TRAILING;
        }
    };
}
