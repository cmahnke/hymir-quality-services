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

import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.springframework.util.ResourceUtils

import javax.imageio.ImageIO
import javax.imageio.ImageReader

@TypeChecked
@Slf4j
class FoldImageQualityServiceTest {
    static Map<Integer, File> files = new HashMap<Integer, File>()
    static int count = 7

    @BeforeEach
    void setup() {
        (1..count).each {
            StringBuilder file = new StringBuilder("images/DE-611-HS-3461927/").append(String.format("%08d", it)).append(".jpg")
            log.info("Added " + file.toString() + " as nr ${it} to list")
            files[it] = ResourceUtils.getFile("classpath:" + file.toString())
        }
    }

    @Test
    @Tag('fold-iqs')
    void testProcessStream(TestInfo testInfo) {
        files.forEach (k, v) -> {
            log.info("Transforming ${v} using FoldImageQualityService with InputStream")
            FoldImageQualityService fiqs = new FoldImageQualityService()
            ImageReader reader = fiqs.processStream(v.getAbsolutePath(), new FileInputStream(v))
            def fileName = "output-fold-iqs-${k}.png"
            ImageIO.write(reader.read(0), "png", new File(fileName))
        }
    }
}
