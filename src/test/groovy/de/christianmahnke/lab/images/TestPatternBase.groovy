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
package de.christianmahnke.lab.images

import de.christianmahnke.lab.images.opencv.OpenCVUtil
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.apache.commons.io.FilenameUtils
import org.junit.jupiter.api.BeforeEach
import org.springframework.util.ResourceUtils

import javax.swing.*
import java.awt.*
import java.awt.image.BufferedImage
import java.util.List

@CompileStatic
@TypeChecked
class TestPatternBase {
    List<File> patterns
    File transparent

    final static String solidImage = "pattern.png"
    final static String transparentImage = "patternAlpha.png"

    protected Boolean checkTranparentArea(File file, BufferedImage result) {
        if (file.getAbsolutePath().contains(transparentImage)) {
            return OpenCVUtil.isTransparent(result, 295, 295)
        }
        return null
    }

    protected String getOutputFileName(File file, String marker) {
        return "output-${this.getClass().getSimpleName()}-${marker}-${FilenameUtils.getBaseName(file.getAbsolutePath())}.png"
    }

    @BeforeEach
    void setup() {
        this.patterns = Arrays.asList(ResourceUtils.getFile("classpath:images/${solidImage}"), ResourceUtils.getFile("classpath:images/${transparentImage}"))
        this.transparent = ResourceUtils.getFile("classpath:transparent.png")
    }

    static void display(BufferedImage image) {
        JFrame frame = new JFrame()
        frame.setTitle("stained_image")
        frame.setSize(image.getWidth(), image.getHeight())
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
        JLabel label = new JLabel()
        label.setIcon(new ImageIcon(image))
        frame.getContentPane().add(label, BorderLayout.CENTER)
        frame.setLocationRelativeTo(null)
        frame.pack()
        frame.setVisible(true)
        sleep(5000)
    }
}
