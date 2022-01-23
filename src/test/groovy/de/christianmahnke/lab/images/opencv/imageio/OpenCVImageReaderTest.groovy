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
package de.christianmahnke.lab.images.opencv.imageio

import de.christianmahnke.lab.images.TestPatternBase
import de.christianmahnke.lab.images.opencv.OpenCVNoop
import de.christianmahnke.lab.images.opencv.OpenCVUtil
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.opencv.core.Mat

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue


@TypeChecked
@Slf4j
class OpenCVImageReaderTest extends TestPatternBase {

    @Test
    void loadMatAsImageReaderTest(TestInfo testInfo) {
        for (pattern in this.patterns) {
            Mat m = OpenCVUtil.loadImage(pattern)
            ImageReader matReader = OpenCVImageReader.getInstance(m)
            assertNotNull(matReader)
            def fileName = getOutputFileName(pattern, "load-mat-via-reader")
            BufferedImage result = matReader.read(0)
            ImageIO.write(matReader.read(0), "png", new File(fileName))
            assertTrue(checkTranparentArea(pattern, result) == null || checkTranparentArea(pattern, result))
        }
    }

    @Test
    void loadNoopMatAsImageReaderTest(TestInfo testInfo) {
        for (pattern in this.patterns) {
            BufferedImage bi = ImageIO.read(pattern)
            OpenCVNoop ocn = new OpenCVNoop(bi)
            Mat m = ocn.processMat()
            ImageReader matReader = OpenCVImageReader.getInstance(m)
            assertNotNull(matReader)
            BufferedImage result = matReader.read(0)
            def fileName = getOutputFileName(pattern, "load-bi-noop-reader")
            ImageIO.write(result, "png", new File(fileName))
            assertTrue(checkTranparentArea(pattern, result) == null || checkTranparentArea(pattern, result))
        }
    }

}
