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

package com.hablutzel.spwing.converter;

import com.hablutzel.spwing.util.ResourceUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;


@RequiredArgsConstructor
@Slf4j
public class BaseStringToImageConverter {
    private static final BufferedImage deadImage;
    private final Class<?> contextRoot;
    private final ResourceUtils resourceUtils;

    static {
        deadImage = new BufferedImage( 64, 64, BufferedImage.TYPE_INT_RGB );
        Graphics2D g = deadImage.createGraphics();

        g.setColor(Color.BLACK);
        g.setBackground(Color.WHITE);
        g.clearRect(0, 0, 64, 64);
        g.drawRect(0, 0, 64, 64);
        g.drawLine(0, 0, 64, 64 );
        g.drawLine(0, 64, 64, 0 );
    }

    protected BufferedImage getimage(String imageName) {
        // See if the image is available as a resource
        String baseName = FilenameUtils.getBaseName(imageName);
        String extension = FilenameUtils.getExtension(imageName);
        try (InputStream in = resourceUtils.getPlatformResource(contextRoot, baseName, extension)) {
            if (Objects.nonNull(in)) {
                return ImageIO.read(in);
            }
        } catch (IOException io) {

            // We got the input stream, but couldn't read it
            BaseStringToImageConverter.log.error("Unable to read resource image {}", imageName);
            return BaseStringToImageConverter.deadImage;
        }

        // Since we couldn't find it as a resource, try a URL
        try {
            URL url = new URL(imageName);
            return ImageIO.read(url);
        } catch (IOException e) {
            return BaseStringToImageConverter.deadImage;
        }
    }
}
