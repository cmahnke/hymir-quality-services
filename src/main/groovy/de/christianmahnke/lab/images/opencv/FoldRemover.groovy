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

import com.google.common.base.Enums
import de.christianmahnke.lab.images.opencv.OpenCVUtil as CV
import de.christianmahnke.lab.images.opencv.OpenCVUtil.RGBA
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import nu.pattern.OpenCV
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Size

import java.awt.*
import java.awt.image.BufferedImage
import java.util.List
import java.util.regex.Pattern

import static java.lang.Math.*

@TypeChecked
@CompileStatic
@Slf4j
class FoldRemover implements AutoCloseable {
    // Tweaks (These are just te defaults for every operation)
    static double minLineLengthDiv = 1.2
    static double maxLineGapDiv = 4
    static double minAngle = -88
    static double maxAngle = -92
    static double xWheight = 2
    static double xWindow = 0.2

    protected Mat img = null
    protected Side side
    protected Page page
    // TODO: Make the patten configurable
    static List<Pattern> identifierPatterns = [~/.*.[^\d](?<identifier>\d{5,8}).*?$/]
    static {
        OpenCV.loadShared()
    }

    FoldRemover(Mat mat, String sideHint = "None") {
        this.img = mat
        if (Enums.getIfPresent(FoldRemover.Side.class, sideHint.toUpperCase()).isPresent()) {
            this.side = Side.valueOf(sideHint.toUpperCase())
        } else {
            this.side = Side.NONE
        }
    }

    FoldRemover(BufferedImage img, String sideHint) {
        this(OpenCVUtil.bufferedImageToMat(img), sideHint)
    }

    FoldRemover(String file, String sideHint) {
        this(OpenCVUtil.loadImage(file), sideHint)
    }

    FoldRemover(URL url, String sideHint) {
        this(OpenCVUtil.loadImage(url), sideHint)
    }

    BufferedImage processImage(BufferedImage img, String side) {
        this.img = OpenCVUtil.bufferedImageToMat(img)
        this.side = Side.valueOf(side.toUpperCase())
        try {
            return OpenCVUtil.matToBufferedImage(this.process())
        } catch (RecognitionException | org.opencv.core.CvException e) {
            log.warn("""Got exception ${e.getMessage()}, returning input image""")
            img.setRGB(1, 1, Color.RED.getRGB())
            return img
        }
    }

    Mat process() {
        this.page = new Page(this.img, this.side)
        return this.page.rotate()
    }

    protected Page getPage() {
        if (this.page == null) {
            this.page = new Page(this.img, this.side)
        }
        return this.page
    }

    static Point toPoint(Tuple2<Integer, Integer> p) {
        return new Point(p[0] as double, p[1] as double)
    }

    @CompileStatic
    enum Side {
        NONE, RECTO, VERSO
    }

    @CompileStatic
    interface DebugDrawable {
        void debugDraw(Mat img);
    }

    @CompileStatic
    class Line implements DebugDrawable {
        int x1
        int y1
        int x2
        int y2
        double distance
        double angleDeg
        double angleRad
        int width = 1
        CV.RGBA color = new CV.RGBA(0, 0, 0, 0)
        protected Size size
        double averageX
        double score

        Line(List<Double> line) {
            this(line.get(0).toInteger(), line.get(1).toInteger(), line.get(2).toInteger(), line.get(3).toInteger())
        }

        Line(int x1, int y1, int x2, int y2) {
            this(x1, y1, x2, y2, null)
        }

        Line(int x1, int y1, int x2, int y2, Size size) {
            this.x1 = x1
            this.y1 = y1
            this.x2 = x2
            this.y2 = y2

            this.size = size

            def points = toPointTuple2(this)

            this.distance = calcDistance(points)
            this.angleDeg = calcAngleDeg(points)
            this.angleRad = calcAngleRad(points)

            averageX = this.averageVertical(points)
        }

        @Override
        String toString() {
            return """${this.class.getSimpleName()} -> x1: ${this.x1}, y1: ${this.y1}, x2: ${this.x2}, y2: ${this.y2}, avarage x:${this.averageX}, distance: ${this.distance}, angle: ${this.angleDeg}, color: ${this.color}, width: ${this.width}""".toString()
        }

        Tuple2<Point, Point> getLine() {
            return new Tuple2<Point, Point>(new Point(this.x1, this.y1), new Point(this.x2, this.y2))
        }

        static Double averageVertical(Tuple2<Point, Point> line) {
            def (x1, y1, x2, y2) = [line.getV1().x, line.getV1().y, line.getV2().x, line.getV2().y]
            return (x1 + x2) / 2
        }

        static Double calcDistance(Tuple2<Point, Point> line) {
            def (x1, y1, x2, y2) = [line.getV1().x, line.getV1().y, line.getV2().x, line.getV2().y]
            return sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2))
        }


        static Double calcAngleDeg(Tuple2<Point, Point> line) {
            def (x1, y1, x2, y2) = [line.getV1().x, line.getV1().y, line.getV2().x, line.getV2().y]
            return atan2(y2 - y1, x2 - x1) * 180.0 / Math.PI
        }

        static Double calcAngleRad(Tuple2<Point, Point> line) {
            def (x1, y1, x2, y2) = [line.getV1().x, line.getV1().y, line.getV2().x, line.getV2().y]
            return atan2(y1 - y2, x1 - x2)
        }

        static Tuple2<Point, Point> toPointTuple2(Line line) {
            return new Tuple2<Point, Point>(new Point(line.x1, line.y1), new Point(line.x2, line.y2))
        }

        static double calcPointDinstance(Point p1, Point p2) {
            def (double x1, double y1) = [p1.x, p1.y]
            def (double x2, double y2) = [p2.x, p2.y]
            return sqrt(pow(x2 - x1, 2) + pow(y2 - y1, 2))
        }

        static Point calculatePoint(Point point, double angleRad, double distance) {
            //return new Point(point.x - distance * cos(angleRad), point.y - distance * sin(angleRad))
            return new Point((point.x - distance * cos(angleRad)).toInteger(), (point.y - distance * sin(angleRad)).toInteger())
        }

        Point p1() {
            return new Point(this.x1, this.y1)
        }

        Point p2() {
            return new Point(this.x2, this.y2)
        }

        Point bottom() {
            if (this.x1 >= this.x2 && this.y1 >= this.y2) {
                return new Point(this.x2, this.y2)
            } else {
                return new Point(this.x1, this.y1)
            }
        }

        Point top() {
            if (this.x1 <= this.x2 && this.y1 <= this.y2) {
                return new Point(this.x1, this.y1)
            } else {
                return new Point(this.x2, this.y2)
            }
        }

        @Override
        void debugDraw(Mat inMat) {
            log.debug(this.toString())
            CV.line(inMat, top(), bottom(), this.color, this.width)
        }

    }

    @CompileStatic
    class RecognitionException extends Exception {
        RecognitionException(String errorMessage) {
            super(errorMessage)
        }
    }

    @CompileStatic
    class Cut extends Line {
        protected Line line
        protected int h
        protected int w
        protected Side side

        Cut(Line line, Size size) {
            super(line.x1, line.y1, line.x2, line.y2)
            this.size = size
            this.h = (int) this.size.height
            this.w = (int) this.size.width

            this.color = new CV.RGBA(255, 0, 0, 255)
            this.width = 3

            def distanceTop = calcPointDinstance(new Point(this.x2, 0), this.p2())
            def distanceBottom = calcPointDinstance(new Point(this.x1, this.h), new Point(this.x1, this.y1))
            def pTop = calculatePoint(new Point(this.x2, this.y2), this.angleRad, distanceTop)
            def pBottom = calculatePoint(new Point(this.x1, this.y1), this.angleRad, -(distanceBottom))
            this.x1 = pBottom.x.toInteger()
            this.y1 = pBottom.y.toInteger()
            this.x2 = pTop.x.toInteger()
            this.y2 = pTop.y.toInteger()

            def points = new Tuple2<Point, Point>(new Point(this.x1, this.y1), new Point(this.x2, this.y2))
            this.distance = calcDistance(points)

            if (this.x1 < this.w / 2) {
                this.side = Side.VERSO
            } else {
                this.side = Side.RECTO
            }
        }
    }

    @CompileStatic
    class Page implements DebugDrawable {
        Mat img
        Side side
        int w
        int h
        Point center
        List<Line> lines
        Cut cut
        Tuple<Point> box
        List<Point> rotatedBox
        boolean fitBox = true
        // Debug
        int boxWidth = 2
        int rotatedBoxWidth = 4
        CV.RGBA boxColor = new CV.RGBA(0, 127, 127, 127)
        CV.RGBA rotatedBoxColor = new CV.RGBA(0, 255, 255, 255)

        Page(Mat img, Side side) {
            this.img = img
            this.side = side
            this.w = img.cols()
            this.h = img.rows()
            this.center = new Point((this.w / 2) as Double, (this.h / 2) as Double)
        }

        void addAll(List<List<Double>> lines) {
            this.lines = []
            lines.each {
                this.lines.add(new Line(it))
            }
        }

        def findLines(double minLineLength = this.h / FoldRemover.minLineLengthDiv, double maxLineGap = this.h / FoldRemover.maxLineGapDiv) {
            Mat wrkMat

            if (this.img.channels() < 3) {
                wrkMat = OpenCVUtil.cvtColor(this.img, CV.COLOR_RGB2BGR)
            } else {
                wrkMat = CV.copy(this.img)
            }
            // Edge detection
            log.trace("""Finding Lines with at least ${minLineLength} px, maximum allowed gap is ${maxLineGap}""")
            wrkMat = CV.Canny(wrkMat, 30, 120, 3)
            def lines = CV.HoughLinesP(wrkMat, 5, (Math.PI / 180) as Double, 50, minLineLength, maxLineGap)
            this.addAll(lines)
        }

        static List<Line> filterDeg(List<Line> lines, double min = FoldRemover.minAngle, double max = FoldRemover.maxAngle) {
            //lines = lines.sort({ Line line -> line.distance })
            lines = lines.findAll { Line line -> min >= line.angleDeg && line.angleDeg >= max }
            return lines
        }

        static List<Line> filterXPos(List<Line> lines, int min, int max) {
            return lines.findAll { Line line -> min <= line.averageX && line.averageX <= max }
        }

        def calcCutScore (Line line, double xWheight = FoldRemover.xWheight)  {
            double x
            if (this.side == Side.VERSO) {
                x = line.width
            } else {
                x = this.w - line.averageX
            }
            line.score = (x * xWheight) * line.distance
            log.trace("""Calculate score of ${line.score} (x weighted by ${xWheight}, avarage x at ${line.averageX}) for ${line}""")
        }

        def findCut(double xWheight = FoldRemover.xWheight, double xWindow = FoldRemover.xWindow) {
            if (this.lines == null) {
                this.findLines()
            }
            if (this.lines.size() == 0) {
                throw new RecognitionException("No lines found!")
            }
            log.trace("""Num of lines before filtering is ${this.lines.size()}""")
            this.lines = filterDeg(this.lines)
            log.trace("""Num of lines after filtering is ${this.lines.size()}""")
            def foldLines
            if (this.side == Side.VERSO) {
                //Fold right
                foldLines = filterXPos(this.lines, this.w - (this.w * xWindow) as Integer, this.w)
                //foldLines = this.lines.findAll { Line line -> line.x1 > this.w / 2 }
            } else {
                //Fold left
                foldLines = filterXPos(this.lines, 0, (this.w * xWindow) as Integer)
                //foldLines = this.lines.findAll { Line line -> line.x1 < this.w / 2 }
            }
            if (FoldRemover.xWheight > 0) {
                foldLines.each {Line line -> calcCutScore(line, xWheight)}
                foldLines.sort({ Line line -> line.score })
            } else {
                foldLines.sort({ Line line -> line.distance })
            }
            log.trace("""Candidates for ${this.side} fold lines ${foldLines.size()}""")
            if (foldLines.size() < 1) {
                throw new RecognitionException("Cant find a possible cut line")
            }
            this.cut = new Cut(foldLines.get(0), this.img.size())
            log.trace("""Picked ${this.cut} as cut line""")
        }

        def calculateBox() {
            if (this.lines == null) {
                this.findCut()
            }
            double w = this.w
            if (this.cut.bottom().x < this.cut.top().x) {
                w = w - this.cut.top().x
            } else {
                w = w - this.cut.bottom().x
            }
            if (this.side == Side.VERSO) {
                w = -(this.w - w)
            }
            double angle = this.cut.angleRad + toRadians(90)

            if (this.fitBox == false) {
                def pTop = Line.calculatePoint(new Point(this.cut.top().x, this.cut.top().y), angle, w)
                def pBottom = Line.calculatePoint(new Point(this.cut.bottom().x, this.cut.bottom().y), angle, w)
                this.box = new Tuple(new Point(this.cut.top().x, this.cut.top().y), pTop, pBottom, new Point(this.cut.bottom().x, this.cut.bottom().y))
            } else {

                if (this.cut.angleDeg > -90) {
                    // Tilted to the right, lower initial point need to be adjusted to fit the page
                    Point pTop = Line.calculatePoint(new Point(this.cut.top().x, this.cut.top().y), angle, w)
                    Point pBottom = Line.calculatePoint((pTop), this.cut.angleRad, -(this.h - pTop.y))
                    Point npBottom = Line.calculatePoint((pBottom), angle, -(w))
                    this.box = new Tuple(new Point(this.cut.top().x, this.cut.top().y), pTop, pBottom, npBottom)
                } else {
                    // Tilted to the left, upper initial point need to be adjusted to fit the page
                    Point pBottom = Line.calculatePoint(new Point(this.cut.bottom().x, this.cut.bottom().y), angle, w)
                    Point pTop = Line.calculatePoint((pBottom), this.cut.angleRad, -(this.h - pBottom.y))
                    Point npTop = Line.calculatePoint((pTop), angle, -(w))
                    this.box = new Tuple(npTop, pTop, pBottom, new Point(this.cut.bottom().x, this.cut.bottom().y))
                }
            }
            this.rotatedBox = new ArrayList<Point>(this.box)
            this.box.eachWithIndex { coord, i ->
                Mat matrix = CV.getRotationMatrix2D(this.center, this.cut.angleDeg + 90, null)
                double cos = abs(matrix.get(0, 0)[0])
                double sin = abs(matrix.get(0, 1)[0])
                List<Double> vector = [coord.x, coord.y, 1]
                Mat mul = new Mat()
                def calculated = matMul(matrix, vector)
                //this.rotatedBox.set(i, new Point(calculated[0], calculated[1]))
                this.rotatedBox.set(i, new Point(calculated[0].toInteger(), calculated[1].toInteger()))
            }
        }

        protected double[] matMul(Mat mat, List<Double> vector) {
            int rows = mat.height()
            int columns = mat.width()

            double[] result = new double[rows]

            for (int row = 0; row < rows; row++) {
                double sum = 0
                for (int column = 0; column < columns; column++) {
                    sum += mat.get(row, column)[0] * vector.get(column)
                }
                result[row] = sum
            }
            return result
        }

        Mat rotate() {
            if (this.side == Side.NONE) {
                log.trace("""Side is set to ${this.side}, returning unaltered image.""")
                return this.img
            }
            if (this.rotatedBox == null) {
                this.calculateBox()
            }
            Mat matrix = CV.getRotationMatrix2D(this.center, this.cut.angleDeg + 90, null)
            def (Point p1, Point p2) = [this.rotatedBox.get(0), this.rotatedBox.get(2)]
            log.debug("extracting at ${p1.toString()} -> ${p2.toString()} (of ${this.w}x${this.h})")
            Mat rotated = CV.warpAffine(this.img, matrix, this.img.size(), CV.INTER_CUBIC, CV.BORDER_REPLICATE)

            Rect crop = new Rect(p1.x, p1.y, (p2.x - p1.x + 1), (p2.y - p1.y + 1))
            return rotated.submat(crop)
        }

        boolean debugColorize() {
            if (this.side == Side.NONE) {
                return false
            }
            if (this.lines == null) {
                findLines()
            }
            List<Line> coloredLines = []
            int color

            if (this.lines.size() > 0) {
                color = 255
                def leftLines = this.lines.findAll { Line line -> line.x1 < this.w / 2 }
                leftLines.sort({ Line line -> line.distance })
                for (line in leftLines) {
                    line.color = new CV.RGBA(0, 0, color, color)
                    color = color - 20
                    if (color < 63) {
                        color = 63
                    }
                    coloredLines.add(line)
                }

                color = 255
                def rightLines = this.lines.findAll { Line line -> line.x1 > this.w / 2 }
                rightLines.sort({ Line line -> line.distance })
                for (line in rightLines) {
                    line.color = new CV.RGBA(0, color, 0, color)
                    color = color - 20
                    if (color < 63) {
                        color = 63
                    }
                    coloredLines.add(line)
                }
                this.lines = coloredLines
            }
            return true
        }

        protected static drawBox(Mat inMat, List<Point> box, RGBA color, int width) {
            for (int i = 0; i < box.size() - 1; i++) {
                def p1 = new Point(box.get(i).x, box.get(i).y)
                def p2 = new Point(box.get(i + 1).x, box.get(i + 1).y)
                log.debug("Drawing line ${i} from ${p1} to ${p2}")
                CV.line(inMat, p1, p2, color, width)
            }
            log.debug("""Drawing line from ${box.get(3).x},${box.get(3).y} to ${box.get(0).x}, ${box.get(0).y}""")
            CV.line(inMat, new Point(box.get(3).x, box.get(3).y), new Point(box.get(0).x, box.get(0).y), color, width)
        }

        @Override
        void debugDraw(Mat inMat) {
            if (this.box == null) {
                calculateBox()
            }
            if (inMat.channels() < 4) {
                CV.cvtColor(inMat, inMat, CV.COLOR_BGR2BGRA)
            }

            drawBox(inMat, this.box, this.boxColor, this.boxWidth)
            drawBox(inMat, this.rotatedBox, this.rotatedBoxColor, this.rotatedBoxWidth)
            /*
            for (int i = 0; i < this.box.size() - 1; i++) {
                def p1 = new Point(this.box.get(i).x, this.box.get(i).y)
                def p2 = new Point(this.box.get(i + 1).x, this.box.get(i + 1).y)
                log.debug("Drawing line ${i} from ${p1} to ${p2}")
                CV.line(inMat, p1, p2, this.color, this.width)
            }
            log.debug("""Drawing line from ${this.box.get(3).x},${this.box.get(3).y} to ${this.box.get(0).x}, ${this.box.get(0).y}""")
            CV.line(inMat, new Point(this.box.get(3).x, this.box.get(3).y), new Point(this.box.get(0).x, this.box.get(0).y), this.color, this.width)
            */

            for (Line l : this.lines) {
                l.debugDraw(inMat)
            }

            this.cut.width = 2
            this.cut.color = new OpenCVUtil.RGBA(255, 0, 0, 127)
            this.cut.debugDraw(inMat)
        }
    }

    static String guessSide(String identifier) {
        String number = null
        for (pattern in this.identifierPatterns) {
            def matcher = identifier =~ pattern
            if (matcher.matches()) {
                number = matcher.group("identifier")
                break
            }
        }
        if (number == null) {
            return Side.NONE.name()
        }

        int page = Integer.parseInt(number)

        if (page == 1) {
            return Side.NONE.name()
        } else if (page % 2 == 0) {
            //Fold left
            return Side.VERSO.name()
        } else {
            //Fold right
            return Side.RECTO.name()
        }

    }

    @Override
    void close() {
        if (this.img != null) {
            this.img.release()
        }
    }
}
