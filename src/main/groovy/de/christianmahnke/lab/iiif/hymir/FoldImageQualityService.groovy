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
import de.christianmahnke.lab.images.opencv.FoldRemover
import de.digitalcollections.iiif.model.image.ImageApiProfile
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.opencv.core.Mat
import org.springframework.beans.factory.annotation.Value

import java.awt.image.BufferedImage

import org.springframework.stereotype.Service

@Service
@TypeChecked
@CompileStatic
class FoldImageQualityService implements ImageQualityService {
    String identifier
    String side

    @Value('${custom.image.quality.fold.enabled:true}')
    private boolean enabled

    @Value('${custom.image.quality.fold.name:nofold}')
    private String name;

    FoldImageQualityService() {

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
        return enabled
    }

    void setIdentifier(String identifier) {
        this.identifier = identifier
        this.side = FoldRemover.guessSide(identifier)
    }

    BufferedImage processImage(BufferedImage img) {
        Mat cvImage = OpenCVUtil.bufferedImageToMat(img)
        if (this.side == null) {
            this.side = "NONE"
        }
        FoldRemover fr = new FoldRemover(img, this.side)
        Mat result = fr.process()
        return OpenCVUtil.matToBufferedImage(result)
    }
}
