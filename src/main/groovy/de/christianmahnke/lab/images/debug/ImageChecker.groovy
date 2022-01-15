package de.christianmahnke.lab.images.debug

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

@CompileStatic
@TypeChecked
@Slf4j
class ImageChecker {
    BufferedImage imageA
    BufferedImage imageB
    Integer skip = 0

    ImageChecker(File imageA, File imageB, int skip = 30) {
        this.imageA = ImageIO.read(imageA)
        this.imageB = ImageIO.read(imageB)
        this.skip = skip
    }

    Boolean compare() {
        if (imageA.width != imageB.width || imageA.height != imageB.height) {
            log.debug("Image sizes don't match")
            return false
        }

        for (int y = 0; y < imageA.height-skip; y += skip) {
            for (int x = 0; x < imageA.width-skip; x += skip) {
                if (imageA.getRGB(x, y) != imageB.getRGB(x, y)) {
                    return false
                }
            }
        }
        return true
    }

}
