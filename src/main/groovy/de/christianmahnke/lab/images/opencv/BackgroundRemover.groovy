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

import de.christianmahnke.lab.images.opencv.OpenCVUtil as CV
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import nu.pattern.OpenCV

import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui

import java.awt.image.BufferedImage

@TypeChecked
@CompileStatic
class BackgroundRemover implements AutoCloseable {

    protected Point INITIAL = new Point(1d, 1d)
    protected Scalar FILL = new Scalar(255)
    protected int THRESHOLD = 15
    protected Mat img = null

    static {
        OpenCV.loadShared()
    }

    BackgroundRemover() {

    }

    BackgroundRemover(Mat mat) {
        this.img = mat
    }

    BackgroundRemover(BufferedImage img) {
        //Make sure we get BGR without alpha
        this.img = OpenCVUtil.bufferedImageToMat(img, true)
    }

    BackgroundRemover(String file) {
        this(OpenCVUtil.loadImage(file))
    }

    BufferedImage processImage() {
        BufferedImage result = OpenCVUtil.matToBufferedImage(this.process())
        return result
    }

    protected Mat process() {
        Mat alphaMask = generateAlphaMask(this.img)
        Mat result = OpenCVUtil.addAlphaBGR(this.img, alphaMask)
        this.img.release()
        alphaMask.release()
        return result
    }

    protected Mat generateAlphaMask(Mat inMat) {
        if (inMat.channels() > 3) {
            throw new IllegalStateException("Number of channels in input image must be 1 or 3")
        }
        // TODO: Check order of arguments
        Mat wrkMat = new Mat()
        int w = inMat.cols()
        int h = inMat.rows()

        wrkMat = CV.threshold(inMat, 255 - THRESHOLD, 255, CV.THRESH_BINARY_INV)
        Mat floodfill = CV.copy(wrkMat)

        Mat mask = Mat.zeros(h + 2, w + 2, CvType.CV_8U) //inMat.type()

        CV.floodFill(floodfill, mask, INITIAL, FILL)
        Mat floodfill_inv = CV.bitwise_not(floodfill)
        return OpenCVUtil.extractChannel(CV.bitwise_or(wrkMat, floodfill_inv), 0)
    }

    @Override
    void close() {
        if (this.img != null) {
            this.img.release()
        }
    }

}
