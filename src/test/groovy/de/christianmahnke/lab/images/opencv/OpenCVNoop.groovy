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

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import nu.pattern.OpenCV
import org.opencv.core.Mat

import java.awt.image.BufferedImage

@TypeChecked
@CompileStatic
class OpenCVNoop extends AbstractImageManipulator implements AutoCloseable {
    protected Mat img = null

    static {
        OpenCV.loadShared()
    }

    OpenCVNoop(Mat mat) {
        this.img = mat
    }

    OpenCVNoop(BufferedImage img) {
        this.img = OpenCVUtil.bufferedImageToMat(img)
    }

    OpenCVNoop(InputStream is) {
        this(OpenCVUtil.loadImage(is))
    }

    BufferedImage processBufferedImage() {
        BufferedImage result = OpenCVUtil.matToBufferedImage(this.process())
        return result
    }

    Mat processMat() {
        return this.process()
    }

    protected Mat process() {
        return this.img
    }

    @Override
    void close() {
        if (this.img != null) {
            this.img.release()
        }
    }
}
