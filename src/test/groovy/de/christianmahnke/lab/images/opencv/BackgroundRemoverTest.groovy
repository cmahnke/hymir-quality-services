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
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import org.springframework.util.ResourceUtils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

@TypeChecked
@Slf4j
class BackgroundRemoverTest {

    static Map<Integer, File> files = new HashMap<Integer, File>()

    @BeforeEach
    void setup() {
        files[0] = ResourceUtils.getFile("classpath:images/record_DE-MUS-062622_kenom_127703/vs.jpg")
        files[1] = ResourceUtils.getFile("classpath:images/DE-611-HS-3461927/00000001.jpg")
    }

    @Test
    void loadImage() {
        BufferedImage image = ImageIO.read(files[0])
        assertNotNull(image)
    }

    @Test
    @Tag("mat")
    void transformMat(TestInfo testInfo) {
        files.forEach (i, file) -> {
            BufferedImage image = ImageIO.read(file)
            Mat cvImage = OpenCVUtil.bufferedImageToMat(image)
            BackgroundRemover br = new BackgroundRemover(cvImage)
            Mat result = br.process()
            def fileName = "output-" + String.join("-", testInfo.getTags()) + "-${i}.png"
            log.info("Writing ${fileName}")
            OpenCVUtil.saveImage(result, fileName)
        }
    }

    @Test
    @Tag("bufferedImage")
    void transformBufferedImage(TestInfo testInfo) {
        files.forEach (i, file) -> {
            BufferedImage image = ImageIO.read(file)
            BackgroundRemover br = new BackgroundRemover(image)
            BufferedImage result = br.processBufferedImage()
            def fileName = "output-" + String.join("-", testInfo.getTags()) + "-${i}.png"

            ImageIO.write(result, "png", new File(fileName))
            assertTrue(OpenCVUtil.isTransparent(result, 1, 1))
        }
    }

    @Test
    @Tag("bufferedImageRGB")
    void transformBufferedImageBGR(TestInfo testInfo) {
        files.forEach (i, file) -> {
            BufferedImage image = ImageIO.read(file)
            BackgroundRemover br = new BackgroundRemover(image)
            BufferedImage result = br.processBufferedImage()
            def fileName = "output-" + String.join("-", testInfo.getTags()) + "-${i}.png"
            ImageIO.write(result, "png", new File(fileName))
            assertTrue(OpenCVUtil.isTransparent(result, 1, 1))
        }
    }

    @Test
    @Tag("withAlpha")
    void withAlpha(TestInfo testInfo) {
        files.forEach (i, file) -> {
            Mat inMat = OpenCVUtil.loadImage(file)
            Mat alphaMat = OpenCVUtil.cvtColor(inMat, Imgproc.COLOR_RGB2RGBA)
            BufferedImage biA = OpenCVUtil.matToBufferedImage(alphaMat, true)
            BackgroundRemover br1 = new BackgroundRemover(biA)
            BufferedImage result1 = br1.processBufferedImage()
            BackgroundRemover br2 = new BackgroundRemover(biA)
            BufferedImage result2 = br2.processBufferedImage()
            def fileName = "output-" + String.join("-", testInfo.getTags()) + "-${i}.png"
            ImageIO.write(result2, "png", new File(fileName))
        }
    }

}
