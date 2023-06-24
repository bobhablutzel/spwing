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

package com.hablutzel.spwing.converter;

import com.hablutzel.spwing.util.ResourceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Converts a string to an image. The image
 * might be local resource (with platform aware semantics)
 * or a URL.
 *
 * @author Bob Hablutzel
 */
@Slf4j
public class StringToImageConverter extends BaseStringToImageConverter implements Converter<String, Image> {

    public StringToImageConverter(Class<?> contextRoot, ResourceUtils resourceUtils) {
        super(contextRoot, resourceUtils);
    }

    @Override
    @Nullable
    public Image convert(@NonNull final String imageName) {
        return getimage(imageName);
    }

}
