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
import nu.pattern.OpenCV;
import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO: There might be some memory leaks in here since the used OpenCV Mat's aren't cleared
public class OpenCVUtil {
    // See https://docs.opencv.org/4.x/javadoc/constant-values.html
    public static final int THRESH_BINARY_INV = 1;

    public static final int COLOR_RGB2RGBA = 0;
    public static final int COLOR_BGR2BGRA = 0;
    public static final int COLOR_BGRA2BGR = 1;
    public static final int COLOR_BGR2RGBA = 2;
    public static final int COLOR_RGB2BGR = 4;
    public static final int COLOR_RGBA2BGRA = 5;
    public static final int COLOR_BGRA2RGBA = 5;

    public static final int INTER_CUBIC = 2;
    public static final int INTER_LANCZOS4 = 4;

    public static final int BORDER_REPLICATE = 1;
    public static final int BORDER_TRANSPARENT = 5;

    //public static final int COLOR_RGBA2ABGR = 1000;
    public static final int COLOR_ARGB2BGRA = 1000;
    public static final int COLOR_RGBA2BGR = 1001;
    public static final int COLOR_ARGB2BGR = 1002;

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

    public static Mat loadImage(String file, Integer mode) {
        if (mode == null) {
            mode = Imgcodecs.IMREAD_COLOR;
        }
        return Imgcodecs.imread(file, mode);
    }

    public static Mat loadImage(File file, Integer mode) {
        return loadImage(file.getAbsolutePath(), mode);
    }

    public static Mat loadImage(String file) {
        Mat src = loadImage(file, Imgcodecs.IMREAD_UNCHANGED);
        return src;
    }

    public static Mat loadImage(File file) {
        return loadImage(file.getAbsolutePath());
    }

    public static Mat loadImage(InputStream is, Integer mode) throws IOException {
        if (mode == null) {
            mode = Imgcodecs.IMREAD_UNCHANGED;
        }
        byte[] content = is.readAllBytes();
        return Imgcodecs.imdecode(new MatOfByte(content), mode);
    }

    public static Mat loadImage(InputStream is) throws IOException {
        return loadImage(is, Imgcodecs.IMREAD_UNCHANGED);
    }

    /*
    public static Mat loadImage(URL url) throws IOException {
        return bufferedImageToMat(ImageIO.read(url));
    }

    public static Mat loadImageC(String file) {
        return Imgcodecs.imread(file, Imgcodecs.IMREAD_COLOR);
    }

    public static Mat loadImageC(File file) {
        return Imgcodecs.imread(file.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
    }
*/
    public static boolean writeImage(String file, Mat mat) {
        return Imgcodecs.imwrite(file, mat);
    }

    // See https://riptutorial.com/opencv/example/21963/converting-an-mat-object-to-an-bufferedimage-object
    // With enhancements to handle alpha channels
    public static BufferedImage matToBufferedImage(Mat mat) {
        return matToBufferedImage(mat, null);
    }

    public static BufferedImage matToBufferedImage(Mat mat, Boolean removeAlpha) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (mat.channels() > 1 && mat.channels() < 4) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        } else if (mat.channels() > 3) {
            type = BufferedImage.TYPE_4BYTE_ABGR;
            Mat wrkMat;
            if (removeAlpha != null && removeAlpha != false) {
                wrkMat = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC3);
                Imgproc.cvtColor(mat, wrkMat, Imgproc.COLOR_BGRA2BGR, 3);
                mat = wrkMat;
                type = BufferedImage.TYPE_3BYTE_BGR;
            } else {
                wrkMat = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC4);
                //Converts from BGRA to ABGR
                int[] fromTo = {0, 1, 1, 2, 2, 3, 3, 0};
                Core.mixChannels(Arrays.asList(mat), Arrays.asList(wrkMat), new MatOfInt(fromTo));
                mat = wrkMat;
            }
        }
        int bufferSize = mat.channels() * mat.cols() * mat.rows();
        byte[] b = new byte[bufferSize];
        mat.get(0, 0, b); // get all the pixels
        BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);

        return image;
    }

    protected static BufferedImage bufferedImageToBGR(BufferedImage img) {
        if (BufferedImage.TYPE_3BYTE_BGR == img.getType()) {
            return img;
        } else {
            BufferedImage bgr = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = bgr.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(img, 0, 0, null);
            } finally {
                g.dispose();
            }
            return bgr;
        }

    }

    static Mat removeAlpha(Mat img) {
        Mat wrkMat = new Mat(img.rows(), img.cols(), CvType.CV_8UC3);
        Imgproc.cvtColor(img, wrkMat, Imgproc.COLOR_BGRA2BGR, 3);
        return wrkMat;
    }

    public static void cvtColor(Mat src, Mat dst, int mode) {
        cvtColor(src, dst, mode);
    }

    protected static void cvtColor(Mat src, Mat dst, int mode, Integer dstCn) {
        if (dstCn == null) {
            dstCn = 0;
        }

        if (mode == COLOR_ARGB2BGRA) {
            COLOR_ARGB2BGRA(src, dst);
        } else if (mode == COLOR_ARGB2BGR) {
            COLOR_ARGB2BGRA(src, dst);
        } else if (mode == COLOR_RGBA2BGR) {
            Imgproc.cvtColor(src, dst, COLOR_RGBA2BGRA, 3);
        } else {
            Imgproc.cvtColor(src, dst, mode, dstCn);
        }
    }

    static Mat addTransparencyBRG(Mat inMat) {
        if (inMat.channels() == 3) {
            Mat wrkMat = new Mat(inMat.cols(), inMat.rows(), CvType.CV_8UC4);
            cvtColor(inMat, wrkMat, COLOR_BGR2BGRA);
            return wrkMat;
        }
        return null;
    }

    static Mat cvtColor(Mat inMat, int mode) {
        Mat wrkMat = new Mat();
        cvtColor(inMat, wrkMat, mode, null);
        return wrkMat;
    }

    /*
    protected static void COLOR_RGBA2ABGR(Mat src, Mat dst) {
        int[] fromTo = {0, 3, 1, 2, 2, 1, 3, 0};
        Core.mixChannels(Arrays.asList(src), Arrays.asList(dst), new MatOfInt(fromTo));
    }
    */

    protected static void COLOR_ARGB2BGRA(Mat src, Mat dst) {
        int[] fromTo = {0, 3, 1, 2, 2, 1, 3, 0};
        Core.mixChannels(Arrays.asList(src), Arrays.asList(dst), new MatOfInt(fromTo));
    }

    protected static void COLOR_BGRA2BGR(Mat src, Mat dst) {
        cvtColor(src, dst, COLOR_BGRA2BGR, 3);
    }

    static Mat extractChannel(Mat inMat, int channel) {
        Mat wrkMat = new Mat();
        Core.extractChannel(inMat, wrkMat, channel);
        return wrkMat;
    }

    /**
     * Add an one channel Mat as a alpha channel to a BGR Mat
     *
     * @param bgr if given as MAt with four channels the last will be dropped
     * @param a   the Mat containing the alpha channel
     * @return
     */
    static Mat addAlphaBGR(Mat bgr, Mat a) {
        if (a.type() != CvType.CV_8UC1) {
            throw new IllegalStateException("Wrong channel count for alpha - should be one");
        }

        Mat wrkMat = new Mat();
        List<Mat> bgra = new ArrayList<Mat>();
        Core.split(bgr, bgra);

        if (bgr.type() == CvType.CV_8UC4) {
            bgra.remove(bgra.size() - 1);
        }
        bgra.add(a);
        Core.merge(bgra, wrkMat);
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

    //Inspired by https://stackoverflow.com/questions/33403526/how-to-match-the-color-models-of-bufferedimage-and-mat/33419984
    public static Mat bufferedImageToMat(BufferedImage img, Boolean removeAlpha) {
        int curCVtype = CvType.CV_8UC4; //Default type
        boolean swapAlpha = false;

        switch (img.getType()) {
            case BufferedImage.TYPE_3BYTE_BGR:
                curCVtype = CvType.CV_8UC3;
                break;
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_BYTE_BINARY:
                curCVtype = CvType.CV_8UC1;
                break;
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_INT_RGB:
                curCVtype = CvType.CV_32SC3;
                break;
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
                curCVtype = CvType.CV_32SC4;
                swapAlpha = true;
                break;
            case BufferedImage.TYPE_USHORT_GRAY:
                curCVtype = CvType.CV_16UC1;
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
                swapAlpha = true;
                curCVtype = CvType.CV_8UC4;
                break;
            default:
                throw new IllegalStateException("Unsupported BufferedImage type");
        }

        Mat mat = new Mat(img.getHeight(), img.getWidth(), curCVtype);
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, pixels);

        if (swapAlpha) {
            Mat swappedMat = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC4);
            //Maps to ARGB
            int[] fromTo = {0, 3, 1, 0, 2, 1, 3, 2};

            Core.mixChannels(Arrays.asList(mat), Arrays.asList(swappedMat), new MatOfInt(fromTo));
            if (removeAlpha != null && removeAlpha) {
                mat = cvtColor(swappedMat, Imgproc.COLOR_BGRA2BGR);
            } else {
                mat = swappedMat;
            }

        } else if (img.getType() == BufferedImage.TYPE_INT_RGB) {
            mat = cvtColor(mat, Imgproc.COLOR_RGB2BGR);
        }
        return mat;
    }

    public static Mat bufferedImageToMat(BufferedImage img) {
        return bufferedImageToMat(img, null);
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

    static void line(Mat inMat, Point p1, Point p2, Scalar color, int width) {
        Imgproc.line(inMat, p1, p2, color, width);
    }

    static Mat crop(Mat src, int x, int y, int width, int height) {
        return src.submat(new Rect(x, y, width, height));
    }

    static Mat crop(Mat src, Point p1, int width, int height) {
        return src.submat(new Rect((int) p1.x, (int) p1.y, width, height));
    }

    static Mat crop(Mat src, Point p1, Point p2) {
        return src.submat(new Rect((int) p1.x, (int) p1.y, (int) (p1.x - p2.x), (int) (p1.y - p2.y)));
    }
}
