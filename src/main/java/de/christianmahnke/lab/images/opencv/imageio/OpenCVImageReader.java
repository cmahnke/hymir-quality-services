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
package de.christianmahnke.lab.images.opencv.imageio;

import de.christianmahnke.lab.images.opencv.OpenCVUtil;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Locale;

public class OpenCVImageReader extends ImageReader {
    protected Mat img;
    protected String identifier;

    protected OpenCVImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    protected OpenCVImageReader(String identifier, Mat img, ImageReaderSpi spi) {
        super(spi);
        this.identifier = identifier;
        this.img = img;
    }

    static OpenCVImageReader getInstance(String identifier, InputStream is) {
        ImageReaderSpi spi = new OpenCvImageReaderSpi();
        Mat image = null;
        try {
            image = OpenCVUtil.loadImage(is, Imgcodecs.IMREAD_UNCHANGED);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
        return new OpenCVImageReader(identifier, image, spi);
    }

    static OpenCVImageReader getInstance(String identifier, Mat img) {
        ImageReaderSpi spi = new OpenCvImageReaderSpi();
        return new OpenCVImageReader(identifier, img, spi);
    }

    public static OpenCVImageReader getInstance(Mat img) {
        ImageReaderSpi spi = new OpenCvImageReaderSpi();
        return new OpenCVImageReader(null, img, spi);
    }

    public static InputStream getInputStreamFromImageReader(ImageReader reader) throws IOException {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        ImageIO.write(reader.read(0), "PNG", content);
        return new ByteArrayInputStream(content.toByteArray());
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        return img.cols();
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        return img.rows();
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException();
        }
        if (this.img == null) {
            throw new IllegalStateException();
        }
        //processImageStarted(imageIndex)
        //throw new UnsupportedOperationException("Not implemented.")
        if (param != null && param.getSourceRegion() != null) {

            int x = param.getSourceRegion().x;
            int y = param.getSourceRegion().y;
            int w = param.getSourceRegion().width;
            int h = param.getSourceRegion().height;

            Rect region = new Rect(x, y, w, h);
            Mat mat = this.img.submat(region);
            BufferedImage bi = OpenCVUtil.matToBufferedImage(mat);
            return bi;
        }
        //processImageComplete()
        return OpenCVUtil.matToBufferedImage(this.img);
    }

    static class OpenCvImageReaderSpi extends ImageReaderSpi {
        @Override
        public boolean canDecodeInput(Object source) throws IOException {
            return source instanceof Mat;
        }

        @Override
        public ImageReader createReaderInstance(Object extension) throws IOException {
            return new OpenCVImageReader(this);
        }

        @Override
        public String getDescription(Locale locale) {
            return "A ImageReader for OpenCV Mat objects";
        }
    }
}
