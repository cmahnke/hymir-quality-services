package de.christianmahnke.lab.images.opencv


import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.opencv.core.Mat

import java.awt.image.BufferedImage

@TypeChecked
@CompileStatic
abstract class AbstractImageManipulator {

    abstract BufferedImage processBufferedImage();

    abstract Mat processMat();
}
