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

import org.springframework.stereotype.Service;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;


@Service
public class SVWFBorderFactory implements SVWFComponentFactory {

    @Override
    public void addComponents(final SVWFListener svwfListener) {
        svwfListener.addComponent("onePixelBlackLineBorder", new LineBorder(Color.black, 1));
        svwfListener.addComponent("twoPixelBlackLineBorder", new LineBorder(Color.black, 2));
        svwfListener.addComponent("threePixelBlackLineBorder", new LineBorder(Color.black, 3));
        svwfListener.addComponent("fourPixelBlackLineBorder", new LineBorder(Color.black, 4));
        svwfListener.addComponent("onePixelWhiteLineBorder", new LineBorder(Color.white, 1));
        svwfListener.addComponent("twoPixelWhiteLineBorder", new LineBorder(Color.white, 2));
        svwfListener.addComponent("threePixelWhiteLineBorder", new LineBorder(Color.white, 3));
        svwfListener.addComponent("fourPixelWhiteLineBorder", new LineBorder(Color.white, 4));
        svwfListener.addComponent("onePixelEmptyBorder", new EmptyBorder(1, 1, 1, 1));
        svwfListener.addComponent("fivePixelEmptyBorder", new EmptyBorder(5, 5, 5, 5));
        svwfListener.addComponent("tenPixelEmptyBorder", new EmptyBorder(10, 10, 10, 10));
    }
}
