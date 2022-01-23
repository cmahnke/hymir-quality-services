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
package de.christianmahnke.lab.iiif.hymir

import de.christianmahnke.lab.images.opencv.OpenCVUtil
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.util.ResourceUtils

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.assertTrue

@TypeChecked
@Slf4j
class BackgroundImageQualityServiceTest {

    static Map<Integer, File> files = new HashMap<Integer, File>()

    @BeforeEach
    void setup() {
        files[0] = ResourceUtils.getFile("classpath:images/record_DE-MUS-062622_kenom_127703/vs.jpg")
    }

    @Test
    void backgroundServiceTest() {
        BackgroundImageQualityService biqs = new BackgroundImageQualityService()
        ImageReader reader = biqs.processStream("test", new FileInputStream(files.get(0)))
        def fileName = "output-background-iqs.png"
        BufferedImage result = reader.read(0)
        ImageIO.write(result, "png", new File(fileName))
        assertTrue(OpenCVUtil.isTransparent(result, 1, 1))
    }
}
