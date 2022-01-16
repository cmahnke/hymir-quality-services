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

@Grab(group = 'org.openpnp', module = 'opencv', version = '4.5.1-2')

import nu.pattern.OpenCV
import org.opencv.core.*
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc

def fileName = "src/test/resources/images/pattern.png"
def fileNameAlpha = "src/test/resources/images/patternAlpha.png"
def fieldHeight = 100
def fieldWidth = 100


OpenCV.loadShared();

def colors = Arrays.asList(new Scalar(127, 127, 127), new Scalar(0, 0, 255), new Scalar(0, 255, 0), new Scalar(255, 0, 0), new Scalar(255, 255, 0), new Scalar(255, 0, 255), new Scalar(0, 255, 255), new Scalar(255, 255, 255), new Scalar(0, 0, 0))

//Check if output directories exist
if (!new File(fileName).getParentFile().exists()) {
    new File(fileName).getParentFile().mkdirs()
}

List<Mat> fields = []

for (color in colors) {
    Mat test = new Mat(fieldHeight, fieldWidth, CvType.CV_8UC3)
    test.setTo(color)
    fields.add(test)
}

List<Mat> rows = []
for (i = 0; i < Math.sqrt(fields.size()); i++) {
    Mat dst = new Mat()
    List<Mat> src = fields[Math.sqrt(fields.size()) * i..Math.sqrt(fields.size()) * (i + 1) - 1]
    Core.hconcat(src, dst)
    rows.add(dst)
}

Mat testpattern = new Mat()
Core.vconcat(rows, testpattern)
log.info("Resulting file will be " + new File(fileName).getAbsolutePath())

Imgcodecs.imwrite(fileName, testpattern)

Mat alphaPattern = new Mat(testpattern.height(), testpattern.width(), CvType.CV_8UC4)
Imgproc.cvtColor(testpattern, alphaPattern, Imgproc.COLOR_BGR2BGRA);

Mat mask = new Mat(testpattern.height(), testpattern.width(), CvType.CV_8U)
mask.setTo(new Scalar(255))

def overlap = 2
//def alphaPatches = 4

for (i = 0; i < Math.sqrt(fields.size()) - 1; i++) {
    def x = (i * fieldWidth + fieldWidth - (fieldWidth / overlap) / 2).toInteger()
    for (j = 0; j < Math.sqrt(fields.size()) - 1; j++) {
        def y = (j * fieldWidth + fieldWidth - (fieldHeight / overlap) / 2).toInteger()
        Mat part = mask.submat(new Rect(x, y, fieldWidth / overlap, fieldHeight / overlap))
        part.setTo(new Scalar(127))
    }
}

List<Mat> bgr = new ArrayList<Mat>();
Core.split(alphaPattern, bgr);
Core.extractChannel(mask, mask, 0)
bgr.remove(bgr.size() - 1)
bgr.add(mask);
Core.merge(bgr, testpattern);

//HighGui.imshow("test", mask)
//HighGui.waitKey()

log.info("Resulting file will be " + new File(fileNameAlpha).getAbsolutePath())
Imgcodecs.imwrite(fileNameAlpha, testpattern)