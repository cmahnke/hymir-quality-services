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
import org.springframework.util.ResourceUtils

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.assertTrue

@TypeChecked
@Slf4j
class FoldRemoverTest {
    Map<Integer, File> files = new HashMap<Integer, File>()
    int count = 7

    protected transformSingleMat (Integer k, File v, String suffix = "") {
        BufferedImage image = ImageIO.read(v)
        Mat cvImage = OpenCVUtil.bufferedImageToMat(image)
        FoldRemover fr = new FoldRemover(cvImage, FoldRemover.guessSide(v.toString()))
        Mat result = fr.process()
        def fileName = "output-${suffix}-" +  k.toString() + ".png"
        OpenCVUtil.saveImage(result, fileName)
    }

    @BeforeEach
    void setup() {
        (1..count).each {
            StringBuilder file = new StringBuilder("images/DE-611-HS-3461927/").append(String.format("%08d", it)).append(".jpg")
            log.info("Added " + file.toString() + " as nr ${it} to list")
            files[it] = ResourceUtils.getFile("classpath:" + file.toString())
        }
    }

    @Test
    void testGuesser() {
        files.forEach (k, v) -> {
            log.info("""File ${v.toString()}  guessed ${FoldRemover.guessSide(v.toString())}""")
        }
    }

    @Test
    void testGuessedImage() {
        File v = files.get(1)
        BufferedImage image = ImageIO.read(v)
        Mat cvImage = OpenCVUtil.bufferedImageToMat(image)
        FoldRemover fr = new FoldRemover(cvImage, FoldRemover.guessSide(v.toString()))
        Mat result = fr.process()
    }

    @Test
    void testTransformMat(TestInfo testInfo) {
        files.forEach (k, v) -> {
            log.info("Transforming ${v} using Mat")
            transformSingleMat(k, v, String.join("-", testInfo.getTags()))
        }
    }

    @Test
    @Tag('keep-size')
    void testTransformSize(TestInfo testInfo) {
        files.forEach (k, v) -> {
            log.info("Transforming ${v} using Mat and checking if sizes match")
            Mat image = OpenCVUtil.loadImage(v)
            FoldRemover fr = new FoldRemover(image, FoldRemover.guessSide(v.toString()))
            fr.setKeepSize(true)
            String side = FoldRemover.guessSide(v.toString())
            Mat result = fr.process()
            def fileName = "output-" + String.join("-", testInfo.getTags()) + "-${side}-${k}.png"
            assertTrue(image.size().equals(result.size()))
            OpenCVUtil.writeImage(fileName, result)
        }
    }

    @Test
    @Tag('prototype-image')
    void testTransformPrototype(TestInfo testInfo) {
        def i = 7
        log.info("Processing nr ${i} - ${files.get(i)}")
        transformSingleMat(i, files.get(i), String.join("-", testInfo.getTags()))
    }

    @Test
    @Tag('bufferedImage')
    void testTransformBufferedImage(TestInfo testInfo) {
        files.forEach (k, v) -> {
            BufferedImage image = ImageIO.read(v)
            Mat cvImage = OpenCVUtil.bufferedImageToMat(image)
            String side = FoldRemover.guessSide(v.toString())
            FoldRemover fr = new FoldRemover(cvImage, side)
            BufferedImage result = fr.processImage(image, side)
            def fileName = "output-" + String.join("-", testInfo.getTags()) + "-${side}-" + k.toString() + ".png"
            ImageIO.write(result, "png", new File(fileName))
        }
    }
    @Test
    @Tag('debug-draw')
    void testDebug(TestInfo testInfo) {
        boolean failed = false
        files.forEach (k, v) -> {
            Mat cvImage = OpenCVUtil.loadImage(v)
            def side = FoldRemover.guessSide(v.toString())
            FoldRemover fr = new FoldRemover(cvImage, side)
            log.info("Processing ${v.toString()} (guessed ${side})")

            FoldRemover.Page p = fr.getPage()
            p.findLines()
            if (p.debugColorize()) {
                Mat debugMat = OpenCVUtil.loadImage(v)
                p.calculateRotatedBox()
                p.debugDraw(debugMat)
                def fileName = "output-" + String.join("-", testInfo.getTags()) + "-${side}-"+  k.toString()  + ".png"
                log.debug("Writing image ${fileName}")
                OpenCVUtil.saveImage(debugMat, fileName)
                try {
                    fr.process()
                } catch (Exception e) {
                    log.error("Processing failed", e)
                }
            }
        }
    }
}
