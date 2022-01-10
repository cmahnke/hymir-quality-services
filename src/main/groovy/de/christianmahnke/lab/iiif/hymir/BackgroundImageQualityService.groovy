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

import de.christianmahnke.lab.images.opencv.BackgroundRemover
import de.christianmahnke.lab.images.opencv.OpenCVUtil
import de.digitalcollections.iiif.hymir.image.business.api.ImageQualityService
import de.digitalcollections.iiif.model.image.ImageApiProfile
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.opencv.core.Mat
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import java.awt.image.BufferedImage

@Slf4j
@Service
@TypeChecked
@CompileStatic
class BackgroundImageQualityService implements ImageQualityService {
    String identifier

    @Value('${custom.image.quality.background.enabled:true}')
    private boolean enabled

    @Value('${custom.image.quality.background.name:transparent-background}')
    private String name;

    BackgroundImageQualityService() {

    }

    @Override
    public String name() {
        ""
    }

    @Override
    public ImageApiProfile.Quality getQuality() {
        return new ImageApiProfile.Quality(name);
    }

    @Override
    public boolean enabled() {
        return enabled;
    }

    @Override
    void setIdentifier(String identifier) {
        this.identifier = identifier
    }

    BufferedImage processImage(BufferedImage img) {
        log.info("Processing '${this.identifier}' with ${this.getClass().getSimpleName()} - Image Info: ${img.getWidth()}x${img.getHeight()}, channels ${img.getColorModel().getNumComponents()}")
        BackgroundRemover br = new BackgroundRemover(img)
        Mat result = br.process()
        return OpenCVUtil.matToBufferedImage(result)
    }

    @Override
    public boolean hasAlpha() {
        return true;
    }
}
