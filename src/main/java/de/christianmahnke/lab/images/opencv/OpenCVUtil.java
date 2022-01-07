/**
 * IIIF Image Services
 * Copyright (C) 2022  Christian Mahnke
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.christianmahnke.lab.images.opencv;

import com.google.common.primitives.Doubles;
import groovy.lang.Tuple;
import nu.pattern.OpenCV;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import us.ihmc.ihmcPerception.OpenCVTools;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenCVUtil {
    // See https://docs.opencv.org/4.x/javadoc/constant-values.html
    public static final int THRESH_BINARY_INV = 1;
    public static final int COLOR_RGB2RGBA = 0;
    public static final int COLOR_RGB2BGR = 4;
    public static final int COLOR_BGR2RGBA = 2;
    public static final int COLOR_BGRA2RGBA = 5;
    public static final int COLOR_BGR2BGRA = 0;
    public static final int INTER_CUBIC = 2;
    public static final int INTER_LANCZOS4 = 4;
    public static final int BORDER_REPLICATE = 1;
    public static final int BORDER_TRANSPARENT = 5;
    public static final int COLOR_RGBA2BGRA = 5;
    public static final int COLOR_RGBA2ABGR = 1000;


    private static final OpenCVUtil instance;

    static {
        OpenCV.loadShared();
        instance = new OpenCVUtil();
    }

    private OpenCVUtil() {

    }

    public OpenCVUtil getIntance() {
        return instance;
    }

    public static Mat loadImage(String file) {
        return Imgcodecs.imread(file, Imgcodecs.IMREAD_UNCHANGED);
    }

    public static Mat loadImage(File file) {
        return Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
    }

    public static Mat loadImage(URL url) throws IOException {
        return bufferedImageToMat(ImageIO.read(url));
    }

    public static Mat loadImageC(String file) {
        return Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);
    }

    public static Mat loadImageC(File file) {
        return Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
    }

    // See https:\/\/riptutorial.com\/opencv\/example\/21963\/converting-an-mat-object-to-an-bufferedimage-object
    // With enhancements to handle alpha channels
    public static BufferedImage matToBufferedImage(Mat mat) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1 && mat.channels() < 4) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else if (mat.channels() > 3) {
            type = BufferedImage.TYPE_4BYTE_ABGR;
        }
        if (mat.channels() > 3) {
            mat = COLOR_RGBA2ABGR(mat);
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        return image;
    }

    protected static void cvtColor(Mat src, Mat dst, int mode) {
        if (mode == COLOR_RGBA2ABGR) {
            COLOR_RGBA2ABGR(src, dst);
        } else {
            Imgproc.cvtColor(src, dst, mode);
        }
    }

    static Mat cvtColor(Mat inMat, int mode) {
        Mat wrkMat = new Mat();
        cvtColor(inMat, wrkMat, mode);
        return wrkMat;
    }

    protected static Mat COLOR_RGBA2ABGR(Mat inMat) {
        if (inMat.channels() != 4) {
            return null;
        }
        Mat wrkMat = new Mat();

        COLOR_RGBA2ABGR(inMat, wrkMat);
        return wrkMat;
    }

    protected static void COLOR_RGBA2ABGR(Mat src, Mat dst) {
        List<Mat> rgba = new ArrayList<Mat>();
        Core.split(src, rgba);
        List<Mat> abgr = new ArrayList<Mat>();
        abgr.addAll(Arrays.asList(rgba.get(3), rgba.get(0), rgba.get(1), rgba.get(2)));
        Core.merge(abgr, dst);
    }

    static Mat extractChannel(Mat inMat, int channel) {
        Mat wrkMat = new Mat();
        Core.extractChannel(inMat, wrkMat, channel);
        return wrkMat;
    }

    static Mat addAlpha(Mat inMat, Mat alpha) {
        if (alpha.type() != CvType.CV_8UC1) {
            throw new IllegalStateException("Wrong channel count for alpha - should be one");
        }

        Mat wrkMat = new Mat();
        List<Mat> rgba = new ArrayList<Mat>();
        Core.split(inMat, rgba);
        if (inMat.type() == CvType.CV_8UC4) {
            rgba.remove(rgba.size() - 1);
        }
        rgba.add(alpha);
        Core.merge(rgba, wrkMat);
        return wrkMat;
    }

    static BufferedImage matToBufferedImageWithAlpha(Mat inMat, Mat alpha) {
        if (alpha.type() != CvType.CV_8UC1) {
            throw new IllegalStateException("Wrong channel count for alpha - should be one");
        }
        if (inMat.channels() > 3) {
            throw new IllegalStateException("Wrong channel count for input Matrix - should be three");
        }
        Mat wrkMat = new Mat();
        List<Mat> rgb = new ArrayList<Mat>();
        Core.split(inMat, rgb);
        List<Mat> abgr = new ArrayList<Mat>();
        abgr.addAll(Arrays.asList(alpha, rgb.get(0), rgb.get(1), rgb.get(2)));
        Core.merge(abgr, wrkMat);
        // Convert
        int bufferSize = wrkMat.channels() * wrkMat.cols() * wrkMat.rows();
        byte[] b = new byte[bufferSize];
        wrkMat.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(wrkMat.cols(), wrkMat.rows(), BufferedImage.TYPE_4BYTE_ABGR);
        return image;
    }

    // See OpenCVTools.convertBufferedImageToMat (See https://github.com/ihmcrobotics/ihmc-open-robotics-software/blob/09287cf6c061f60f73dd699aad356eedaa0830aa/ihmc-perception/src/main/java/us/ihmc/ihmcPerception/OpenCVTools.java)
    public static Mat bufferedImageToMat(BufferedImage img) {
        return OpenCVTools.convertBufferedImageToMat(img);
    }

    public static void saveImage(Mat img, String file) {
        Imgcodecs.imwrite(file, img);
    }

    //https://stackoverflow.com/questions/8978228/java-bufferedimage-how-to-know-if-a-pixel-is-transparent
    public static boolean isTransparent(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x, y);
        return (pixel >> 24) == 0x00;
    }

    static Mat threshold(Mat inMat, double thresholdValue, double maxValue, int type) {
        Mat wrkMat = new Mat();
        Imgproc.threshold(inMat, wrkMat, thresholdValue, maxValue, type);
        return wrkMat;
    }

    static Mat bitwise_not(Mat src) {
        Mat wrkMat = new Mat();
        Core.bitwise_not(src, wrkMat);
        return wrkMat;
    }

    static void floodFill(Mat inMat, Mat mask, Point point, Scalar val) {
        Imgproc.floodFill(inMat, mask, point, val);
    }

    static Mat copy(Mat inMat) {
        Mat wrkMat = new Mat();
        inMat.copyTo(wrkMat);
        return wrkMat;
    }

    static Mat warpAffine(Mat inMat, Mat matrix, Size size, int flags, int borderMode) {
        Mat wrkMat = new Mat();
        Imgproc.warpAffine(inMat, wrkMat, matrix, size, flags, borderMode);
        return wrkMat;
    }

    static Mat getRotationMatrix2D(Point center, double angle, Double scale) {
        if (scale == null) {
            scale = 1.0;
        }
        return Imgproc.getRotationMatrix2D(center, angle, scale);
    }

    static Mat bitwise_or(Mat inMat1, Mat inMat2) {
        Mat wrkMat = new Mat();
        Core.bitwise_or(inMat1, inMat2, wrkMat);
        return wrkMat;
    }

    static Mat Canny(Mat inMat, double thres1, double thres2, int aperture) {
        Mat wrkMat = new Mat();
        Imgproc.Canny(inMat, wrkMat, thres1, thres2, aperture, false);
        return wrkMat;
    }

    static List<List<Double>> HoughLinesP(Mat img, Double rho, Double theta, Integer threshold, Double minLineLength, Double maxLineGap) {
        Mat linesP = new Mat();
        Imgproc.HoughLinesP(img, linesP, rho, theta, threshold, minLineLength, maxLineGap);
        ArrayList<List<Double>> lines = new ArrayList<List<Double>>();
        for (int i = 0; i < linesP.rows(); i++) {
            List<Double> dots = Doubles.asList(linesP.get(i, 0));
            lines.add(dots);
        }
        return lines;
    }

    static void line(Mat inMat, Point p1, Point p2, RGBA color, int width) {
        Imgproc.line(inMat, p1, p2, color.toScalar(), width);
    }

    static class RGBA {
        Tuple<Integer> color;

        RGBA(Tuple<Integer> color) {
            this.color = color;
        }

        RGBA(Integer r, Integer g, Integer b, Integer a) {
            this.color = new Tuple<Integer>(r, g, b, a);
        }

        RGBA(int r, int g, int b, int a) {
            this.color = new Tuple<Integer>(r, g, b, a);
        }

        Scalar toScalar() {
            return new Scalar(this.color.get(0), this.color.get(1), this.color.get(2), this.color.get(3));
        }

        @Override
        public String toString() {
            return "(R: " + this.color.get(0) + ", G:" + this.color.get(1) + ", B:" + this.color.get(2) + ", A:" + this.color.get(3) + ")";
        }
    }

}
