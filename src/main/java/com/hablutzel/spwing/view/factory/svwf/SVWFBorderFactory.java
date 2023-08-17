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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;


@Service
@Scope("singleton")
public class SVWFBorderFactory {

    @Bean
    public LineBorder onePixelBlackLineBorder() {
        return new LineBorder(Color.black, 1 );
    }

    @Bean
    public LineBorder twoPixelBlackLineBorder() {
        return new LineBorder(Color.black, 2 );
    }

    @Bean
    public LineBorder threePixelBlackLineBorder() {
        return new LineBorder(Color.black, 3 );
    }


    @Bean
    public LineBorder fourPixelBlackLineBorder() {
        return new LineBorder(Color.black, 4 );
    }


    @Bean
    public LineBorder onePixelWhiteLineBorder() {
        return new LineBorder(Color.white, 1 );
    }

    @Bean
    public LineBorder twoPixelWhiteLineBorder() {
        return new LineBorder(Color.white, 2 );
    }

    @Bean
    public LineBorder threePixelWhiteLineBorder() {
        return new LineBorder(Color.white, 3 );
    }


    @Bean
    public LineBorder fourPixelWhiteLineBorder() {
        return new LineBorder(Color.white, 4 );
    }


    @Bean
    public EmptyBorder onePixelEmptyBorder() { return new EmptyBorder(1, 1, 1, 1 ); }

    @Bean
    public EmptyBorder fivePixelEmptyBorder() { return new EmptyBorder(5, 5, 5, 5 ); }

    @Bean
    public EmptyBorder tenPixelEmptyBorder() { return new EmptyBorder(10, 0, 10, 10 ); }

}
