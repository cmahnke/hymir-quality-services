package de.christianmahnke.lab.images.opencv.imageio

import de.christianmahnke.lab.images.opencv.OpenCVUtil
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.imgcodecs.Imgcodecs

import javax.imageio.ImageReadParam
import javax.imageio.ImageReader
import javax.imageio.ImageTypeSpecifier
import javax.imageio.metadata.IIOMetadata
import javax.imageio.spi.ImageReaderSpi
import java.awt.image.BufferedImage

@CompileStatic
@TypeChecked
class OpenCVImageReader extends ImageReader {
    protected Mat img
    protected String identifier

    protected OpenCVImageReader(ImageReaderSpi originatingProvider) {
        super(originatingProvider)
    }

    protected OpenCVImageReader(String identifier, Mat img, ImageReaderSpi spi) {
        super(spi)
        this.identifier = identifier
        this.img = img
    }

    static OpenCVImageReader getInstance (String identifier, InputStream is) {
        ImageReaderSpi spi = new OpenCvImageReaderSpi()
        return new OpenCVImageReader(identifier, OpenCVUtil.loadImage(is, Imgcodecs.IMREAD_UNCHANGED), spi)
    }

    static OpenCVImageReader getInstance (String identifier, Mat img) {
        ImageReaderSpi spi = new OpenCvImageReaderSpi()
        return new OpenCVImageReader(identifier, img, spi)
    }

    static OpenCVImageReader getInstance (Mat img) {
        ImageReaderSpi spi = new OpenCvImageReaderSpi()
        return new OpenCVImageReader(null, img, spi)
    }

    @Override
    int getNumImages(boolean allowSearch) throws IOException {
        return 1
    }

    @Override
    int getWidth(int imageIndex) throws IOException {
        return img.cols()
    }

    @Override
    int getHeight(int imageIndex) throws IOException {
        return img.rows()
    }

    @Override
    Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented.")
    }

    @Override
    IIOMetadata getStreamMetadata() throws IOException {
        throw new UnsupportedOperationException("Not implemented.")
    }

    @Override
    IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        throw new UnsupportedOperationException("Not implemented.")
    }

    @Override
    BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (imageIndex != 0) {
            throw new IndexOutOfBoundsException()
        }
        if (this.img == null) {
            throw new IllegalStateException()
        }
        //processImageStarted(imageIndex)
        //throw new UnsupportedOperationException("Not implemented.")
        if (param != null && param.getSourceRegion() != null) {

            def x = param.getSourceRegion().x
            def y = param.getSourceRegion().y
            def w = param.getSourceRegion().width
            def h = param.getSourceRegion().height

            Rect region = new Rect(x, y, w, h)
            Mat mat = this.img.submat(region)
            BufferedImage bi = OpenCVUtil.matToBufferedImage(mat)
            return bi
        }
        //processImageComplete()
        return OpenCVUtil.matToBufferedImage(this.img)
    }

    static class OpenCvImageReaderSpi extends ImageReaderSpi {
        @Override
        boolean canDecodeInput(Object source) throws IOException {
            if (source instanceof Mat) {
                return true
            }
            return false
        }

        @Override
        ImageReader createReaderInstance(Object extension) throws IOException {
            return new OpenCVImageReader(this)
        }

        @Override
        String getDescription(Locale locale) {
            return "A ImageReader for OpenCV Mat objects"
        }
    }
}
