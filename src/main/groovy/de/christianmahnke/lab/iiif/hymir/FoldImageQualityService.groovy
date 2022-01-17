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

import de.christianmahnke.lab.images.opencv.FoldRemover
import de.christianmahnke.lab.images.opencv.OpenCVUtil
import de.christianmahnke.lab.images.opencv.imageio.OpenCVImageReader
import de.digitalcollections.iiif.hymir.image.business.api.ImageQualityService
import de.digitalcollections.iiif.model.image.ImageApiProfile
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import org.opencv.core.Mat
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import javax.imageio.ImageReader
import java.awt.image.BufferedImage

@Slf4j
@Service
@TypeChecked
@CompileStatic
class FoldImageQualityService implements ImageQualityService.Source {

    @Value('${custom.image.quality.fold.enabled:true}')
    private boolean enabled

    @Value('${custom.image.quality.fold.name:nofold}')
    private String name;

    FoldImageQualityService() {

    }

    @Override
    public String name() {
        "Fold line remover"
    }

    @Override
    public ImageApiProfile.Quality getQuality() {
        return new ImageApiProfile.Quality(name)
    }

    @Override
    public boolean enabled() {
        return enabled
    }

    BufferedImage processImage(String identifier, BufferedImage img) {
        String side = null
        Mat cvImage = OpenCVUtil.bufferedImageToMat(img)
        if (identifier != null) {
            side = FoldRemover.guessSide(identifier)
        }
        if (side == null) {
            side = "NONE"
        }
        FoldRemover fr = new FoldRemover(img, side)
        fr.setKeepSize(true);
        log.info("Processing '${identifier}' with ${this.getClass().getSimpleName()} - Image Info: ${img.getWidth()}x${img.getHeight()}, channels ${img.getColorModel().getNumComponents()}")
        return fr.processBufferedImage()
    }

    @Override
    public boolean hasAlpha() {
        return false;
    }

    @Override
    ImageReader processStream(String identifier, InputStream inputStream) {
        String side = null
        if (identifier != null) {
            side = FoldRemover.guessSide(identifier)
        }
        if (side == null) {
            side = "NONE"
        }
        FoldRemover fr = new FoldRemover(inputStream, side)
        fr.setKeepSize(true);
        Mat img = fr.processMat()
        return OpenCVImageReader.getInstance(img)
    }
}
