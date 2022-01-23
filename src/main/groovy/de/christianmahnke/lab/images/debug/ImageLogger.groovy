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

package de.christianmahnke.lab.images.debug

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.imgscalr.Scalr

import java.awt.image.BufferedImage

@TypeChecked
@CompileStatic
//@Warning('This class is completely untested')
class ImageLogger {
    BufferedImage img
    double scale
    List<String> darkToLight = ['#', 'A', '@', '%', '$', '+', '=', '*', ':', ',', '.', ' ']

    ImageLogger(BufferedImage img) {
        this.img = img
        this.scale = 1
    }

    ImageLogger(BufferedImage img, double scale) {
        this.img = Scalr.resize(img, Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, (img.getWidth() * scale).toInteger(), (img.getHeight() * scale).toInteger())
        this.scale = scale
    }

    String toString() {
        int colors = darkToLight.size()
        StringBuilder sb = new StringBuilder()
        for (int i = 0; i < this.img.height; i++) {
            for (int j = 0; j < this.img.width; j++) {
                def px = img.getRGB(j, i)
                int r = 0xff & (px >> 16)
                int g = 0xff & (px >> 8)
                int b = 0xff & px
                //def pxS = Ansi.ansi()

                int peak = Math.max(r, Math.max(g, b))
                int intensity = (int) ((colors * peak) / 255)
                if (intensity >= colors) {
                    intensity = colors - 1
                }
                def pxS = darkToLight[intensity]
                sb.append(pxS)
            }
            sb.append("\n")
        }
        sb.append("\n")
        return sb.toString()
    }

    static dump(BufferedImage img, double scale) {
        return new ImageLogger(img, scale).toString()
    }
}
