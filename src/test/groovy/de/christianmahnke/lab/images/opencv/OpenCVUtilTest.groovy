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

import de.christianmahnke.lab.images.debug.ImageChecker
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.apache.commons.io.FilenameUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.opencv.core.Mat
import org.springframework.util.ResourceUtils

import javax.imageio.ImageIO
import javax.swing.*
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

import static org.junit.jupiter.api.Assertions.*

@TypeChecked
@Slf4j
class OpenCVUtilTest {

    List<File> patterns
    File transparent

    protected String getOutputFileName(File file, String marker) {
        return "output-${marker}-${FilenameUtils.getBaseName(file.getAbsolutePath())}.png"
    }


    @BeforeEach
    void setup() {
        this.patterns = Arrays.asList(ResourceUtils.getFile("classpath:images/pattern.png"), ResourceUtils.getFile("classpath:images/patternAlpha.png"))
        this.transparent = ResourceUtils.getFile("classpath:transparent.png")
    }

    @Test
    void loadImageImageIOTest(TestInfo testInfo) {
        for (pattern in this.patterns) {
            BufferedImage image = ImageIO.read(pattern)
            assertNotNull(image)
        }
    }

    @Test
    void loadImageOpenCVTest(TestInfo testInfo) {
        for (pattern in this.patterns) {
            Mat image = OpenCVUtil.loadImage(pattern)
            assertNotNull(image)
            def fileName = getOutputFileName(pattern, "load-as-mat")
            OpenCVUtil.writeImage(fileName, image)
            assertTrue(new ImageChecker(pattern, new File(fileName)).compare())
        }
    }

    @Test
    void matToBufferedImageTest(TestInfo testInfo) {
        for (pattern in this.patterns) {
            Mat m = OpenCVUtil.loadImage(pattern)
            BufferedImage image = OpenCVUtil.matToBufferedImage(m)
            assertNotNull(image)
            def fileName = getOutputFileName(pattern, "mat-to-bi")
            ImageIO.write(image, "png", new File(fileName))
            assertTrue(new ImageChecker(pattern, new File(fileName)).compare())
        }
    }

    @Test
    void bufferedImageToMatTest(TestInfo testInfo) {
        for (pattern in this.patterns) {
            BufferedImage bi = ImageIO.read(pattern)
            Mat image = OpenCVUtil.bufferedImageToMat(bi)
            assertNotNull(image)
            def fileName = getOutputFileName(pattern, "bi-to-mat")
            OpenCVUtil.writeImage(fileName, image)
            assertTrue(new ImageChecker(pattern, new File(fileName)).compare())
        }
    }

    @Test
    void isTransparentTest(TestInfo testInfo) {
        assertTrue(OpenCVUtil.isTransparent(ImageIO.read(this.transparent), 1, 1), "Check transparency using ImageIO failed")
        assertTrue(OpenCVUtil.isTransparent(OpenCVUtil.matToBufferedImage(OpenCVUtil.loadImage(this.transparent)), 1, 1), "Check transparency using OpenCV failed")
    }

    @Test
    void transparentToMatRGB() {
        def fileName = "output-should-be-white.png"
        Mat image = OpenCVUtil.bufferedImageToMat(ImageIO.read(this.transparent), true)
        OpenCVUtil.writeImage(fileName, image)
        assertFalse(OpenCVUtil.isTransparent(ImageIO.read(new File(fileName)), 1, 1))
    }

    @Test
    void loadBIThreeChannelsWriteMat() {
        for (pattern in this.patterns) {
            BufferedImage bi = ImageIO.read(pattern)
            Mat image = OpenCVUtil.bufferedImageToMat(bi, true)
            def fileName = getOutputFileName(pattern, "read-mat-three-channels-mat")
            OpenCVUtil.writeImage(fileName, image)
            if (bi.getColorModel().hasAlpha()) {
                assertFalse(new ImageChecker(pattern, new File(fileName)).compare(), "${pattern.getAbsolutePath()} faied")
            } else {
                assertTrue(new ImageChecker(pattern, new File(fileName)).compare(), "${pattern.getAbsolutePath()} faied")
            }
        }
    }

    @Test
    void loadBIThreeChannelsWriteBI() {
        for (pattern in this.patterns) {
            BufferedImage bi = ImageIO.read(pattern)
            Mat image = OpenCVUtil.bufferedImageToMat(bi, true)
            def fileName = getOutputFileName(pattern, "read-bi-three-channels-bi")
            BufferedImage writeBi = OpenCVUtil.matToBufferedImage(image)
            ImageIO.write(writeBi, "png", new File(fileName))
            if (bi.getColorModel().hasAlpha()) {
                assertFalse(new ImageChecker(pattern, new File(fileName)).compare())
            } else {
                assertTrue(new ImageChecker(pattern, new File(fileName)).compare())
            }
        }
    }

    @Test
    void loadMatThreeChannelsWriteBI() {
        for (pattern in this.patterns) {
            Mat image = OpenCVUtil.loadImage(pattern)
            BufferedImage bi = OpenCVUtil.matToBufferedImage(image, true)
            def fileName = getOutputFileName(pattern, "read-mat-three-channels-bi")
            ImageIO.write(bi, "png", new File(fileName))
            if (image.channels() > 3) {
                assertFalse(new ImageChecker(pattern, new File(fileName)).compare())
            } else {
                assertTrue(new ImageChecker(pattern, new File(fileName)).compare())
            }
        }
    }

    public static void display(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.setTitle("stained_image");
        frame.setSize(image.getWidth(), image.getHeight());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel label = new JLabel();
        label.setIcon(new ImageIcon(image));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
        sleep(5000)
    }
}
