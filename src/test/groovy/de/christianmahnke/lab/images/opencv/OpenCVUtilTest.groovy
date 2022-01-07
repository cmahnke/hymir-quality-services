/**
 * IIIF Image Services
 * Copyright (C) 2022  Christian Mahnke
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.christianmahnke.lab.images.opencv

import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.opencv.core.Mat
import org.springframework.util.ResourceUtils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

@TypeChecked
@Slf4j
class OpenCVUtilTest {

    File file
    File transparent


    @BeforeEach
    void setup() {
        this.file = ResourceUtils.getFile("classpath:images/record_DE-MUS-062622_kenom_127703/vs.jpg")
        this.transparent = ResourceUtils.getFile("classpath:transparent.png")
    }

    @Test
    void loadImageImageIOTest() {
        BufferedImage image = ImageIO.read(this.file)
        assertNotNull(image)
    }

    @Test
    void loadImageOpenCVTest() {
        Mat image = OpenCVUtil.loadImage(this.file)
        assertNotNull(image)
    }

    @Test
    void matToBufferedImageTest() {
        BufferedImage image = OpenCVUtil.matToBufferedImage(OpenCVUtil.loadImage(this.file))
        assertNotNull(image)
    }

    @Test
    void bufferedImageToMatTest() {
        Mat image = OpenCVUtil.bufferedImageToMat(ImageIO.read(this.file))
        assertNotNull(image)
    }

    @Test
    void isTransparentTest() {
        assertTrue(OpenCVUtil.isTransparent(ImageIO.read(this.transparent), 1, 1), "Check transparency using ImageIO failed")
        assertTrue(OpenCVUtil.isTransparent(OpenCVUtil.matToBufferedImage(OpenCVUtil.loadImage(this.transparent)), 1, 1), "Check transparency using OpenCV failed")
    }

}
