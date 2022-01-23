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
import de.digitalcollections.iiif.hymir.image.business.api.ImageQualityService
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import java.awt.image.BufferedImage

import static org.junit.jupiter.api.Assertions.assertTrue

@TypeChecked
@Slf4j
@SpringBootTest(classes = ImageQualityMockApplication.class)
@TestPropertySource(locations = ['classpath:application.yml', 'classpath:StackTest.properties'])
@ActiveProfiles("plugins,test")
class StackImageQualityServiceTest extends FoldImageQualityServiceTest {

    @Autowired
    private FoldImageQualityService fiqs

    @Autowired
    private BackgroundImageQualityService biqs

    @Override
    @Test
    @Tag('stack-iqs')
    void testProcessStream(TestInfo testInfo) {
        files.forEach (k, v) -> {
            log.info("Transforming ${v} using FoldImageQualityService with InputStream")
            List<ImageQualityService> services = new ArrayList<ImageQualityService>()
            services.addAll(Arrays.asList(fiqs, biqs))
            StackImageQualityService siqs = new StackImageQualityService("nofold,transparent-background", services)
            ImageReader reader = siqs.processStream(v.getAbsolutePath(), new FileInputStream(v))
            def fileName = "output-stack-iqs-${k}.png"
            BufferedImage result = reader.read(0)
            ImageIO.write(reader.read(0), "png", new File(fileName))
            assertTrue(OpenCVUtil.isTransparent(result, 1, 1))
        }
    }
}
