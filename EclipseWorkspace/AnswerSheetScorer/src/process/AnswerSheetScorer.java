package process;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import model.AnswerMat;
import model.AnswerSheetMetadata;
import model.AnswerSheetMetadata.Value;
import model.Option;
import model.label.AnswerLabel;
import model.label.AnswerSheetLabel;
import model.label.ExCodeLabel;
import model.label.MCodeLabel;

public class AnswerSheetScorer {

    private static final int PROCESSED_WIDTH = 457;
    private static final int PROCESSED_HEIGHT = 577;
    private static final int PROCESSED_SQUARE_WIDTH = 16;
    private static final int PROCESSED_SQUARE_HEIGHT = 16;
    private static final int PROCESSED_ANSWER_SQUARE_BORDER = 2;
    private static final double PROCESSED_RATIO = 2.5;

    /**
     * Convert answer sheet photo image to processable answer sheet image
     * 
     * @param src
     *            - answer sheet photo image
     * @return processable answer sheet image
     */
    public static Mat convertAnswerSheet(Mat src, File file, String fileName) {
        // Check whether the argument is valid
        if (src == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        // Declare variables
        Mat result = new Mat(src.rows(), src.cols(), CvType.CV_8UC1);

        // Turn the photo image to grayscale image
        Imgproc.cvtColor(src, result, Imgproc.COLOR_RGB2GRAY);
        Imgcodecs.imwrite(
                new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-1-gray.jpg").getAbsolutePath(),
                result);
        // Black and white
        Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 69, 25);
        Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-2-black-white.jpg")
                .getAbsolutePath(), result);

        // Find all contour on mat
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        Mat res = new Mat(result.rows(), result.cols(), CvType.CV_8UC3);

        ArrayList<MatOfPoint> squares = new ArrayList<>();

        // Test contours
        MatOfPoint2f approx = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx,
                    Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.06, true);

            // Note: absolute value of an area is used because
            // area may be positive or negative - in accordance with the
            // contour orientation
            double area = Math.abs(Imgproc.contourArea(approx));
            if (approx.toArray().length == 4 && area > 1000 && area < src.cols() * src.rows() * 98 / 100
                    && Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))) {
                double maxCosine = 0;

                Point[] approxPoints = approx.toArray();
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(angle(approxPoints[j % 4], approxPoints[j - 2], approxPoints[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }

                if (maxCosine < 0.3)
                    squares.add(new MatOfPoint(approx.toArray()));
            }
        }

        double maxArea = -1, secondMaxArea = -1;
        int maxIdx = -1, secondMaxIdx = -1;
        for (int j = 0; j < squares.size(); j++) {
            Rect rect = Imgproc.boundingRect(squares.get(j));
            double area = rect.area();
            if (area > maxArea) {
                secondMaxArea = maxArea;
                secondMaxIdx = maxIdx;
                maxIdx = j;
                maxArea = area;
            } else if (area > secondMaxArea) {
                secondMaxArea = area;
                secondMaxIdx = j;
            }
        }
        Imgproc.drawContours(res, squares, -1, new Scalar(0, 255, 0), 2);
        Imgcodecs.imwrite(
                new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-3-contour.jpg").getAbsolutePath(),
                res);

        if (secondMaxIdx < 0 && maxIdx < 0)
            return null;
        System.out.println("Draw second max area square");
        int idx = (secondMaxIdx > -1 ? secondMaxIdx : (maxIdx > -1 ? maxIdx : -1));
        Imgproc.drawContours(src, squares, idx, new Scalar(0, 0, 255), 3);
        Imgcodecs.imwrite(
                new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-4-square.jpg").getAbsolutePath(),
                src);

        // Give white color to the contour
        Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-5-square-prcs.jpg")
                .getAbsolutePath(), result);

        // Find sorted 4 points : top left, top right, bottom right, bottom left
        Point[] points = squares.get(idx).toArray();
        Point topLeftPoint, topRightPoint, bottomRightPoint, bottomLeftPoint;
        // Find the most top and second most top
        int mostTopIdx = -1, secondMostTopIdx = -1;
        for (int i = 0; i < points.length; i++) {
            if (mostTopIdx >= 0 && points[i].y < points[mostTopIdx].y) {
                secondMostTopIdx = mostTopIdx;
                mostTopIdx = i;
            } else if (secondMostTopIdx >= 0 && points[i].y < points[secondMostTopIdx].y) {
                secondMostTopIdx = i;
            } else if (mostTopIdx < 0) {
                mostTopIdx = i;
            } else if (secondMostTopIdx < 0) {
                secondMostTopIdx = i;
            }
        }
        if (points[mostTopIdx].x < points[secondMostTopIdx].x) {
            topLeftPoint = points[mostTopIdx];
            topRightPoint = points[secondMostTopIdx];
        } else {
            topLeftPoint = points[secondMostTopIdx];
            topRightPoint = points[mostTopIdx];
        }
        // Find bottom left and bottom right
        int[] bottomIdx = new int[2];
        int countBottomIdx = 0;
        for (int i = 0; i < points.length; i++) {
            if (i != mostTopIdx && i != secondMostTopIdx)
                bottomIdx[countBottomIdx++] = i;
        }
        if (points[bottomIdx[0]].x < points[bottomIdx[1]].x) {
            bottomLeftPoint = points[bottomIdx[0]];
            bottomRightPoint = points[bottomIdx[1]];
        } else {
            bottomLeftPoint = points[bottomIdx[1]];
            bottomRightPoint = points[bottomIdx[0]];
        }

        int perspectiveWidth, perspectiveHeight;
        perspectiveHeight = (int) Math.round(PROCESSED_HEIGHT * PROCESSED_RATIO);
        perspectiveWidth = (int) Math.round(PROCESSED_WIDTH * PROCESSED_RATIO);
        System.out.println("Start perspective transform");
        Mat perspective = new Mat(perspectiveHeight, perspectiveWidth, src.type());
        MatOfPoint2f srcPoints = new MatOfPoint2f(topLeftPoint, topRightPoint, bottomRightPoint, bottomLeftPoint);
        MatOfPoint2f dst = new MatOfPoint2f(new Point(0, 0), new Point(perspective.cols() - 1, 0),
                new Point(perspective.cols() - 1, perspective.rows() - 1), new Point(0, perspective.rows() - 1));

        Mat transform = Imgproc.getPerspectiveTransform(srcPoints, dst);
        Imgproc.warpPerspective(result, perspective, transform, new Size(perspective.cols(), perspective.rows()));
        Imgproc.adaptiveThreshold(perspective, perspective, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
                55, 7);
        Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-6-perspective.jpg")
                .getAbsolutePath(), perspective);

        return perspective;
    }

    /**
     * Process converted answer sheet image to get identity and all the answers
     * 
     * @param src
     *            - answer sheet photo image
     * @return
     */
    public static ArrayList<AnswerMat> processAnswerSheet(Mat src, AnswerSheetMetadata metadata, File file,
            String fileName) {
        // Check whether the argument is valid
        if (src == null)
            throw new IllegalArgumentException("Argument src cannot be null");
        // Check whether the argument is binary image
        for (int row = 0; row < src.rows(); row++) {
            for (int col = 0; col < src.cols(); col++) {
                double[] colors = src.get(row, col);
                if ((int) colors[0] > 0 && (int) colors[0] < 255)
                    throw new IllegalArgumentException("Found color value " + (int) colors[0]
                            + ". Argument src must be binary image (0 = black, 255 = white)");
                for (int channel = 1; channel < src.channels(); channel++)
                    if (colors[channel] != colors[channel - 1])
                        throw new IllegalArgumentException(
                                "Argument src must be binary image (0 = black, 255 = white)");
            }
        }

        // Turn the photo image to grayscale image (DEBUG SUPPORT)
        Imgcodecs.imwrite(
                new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-0-bw.jpg").getAbsolutePath(), src);

        // Find all contours
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Find the fit squares
        ArrayList<MatOfPoint> squares = new ArrayList<>();
        MatOfPoint2f approx = new MatOfPoint2f();
        int area = (int) (PROCESSED_SQUARE_HEIGHT * PROCESSED_SQUARE_WIDTH * PROCESSED_RATIO * PROCESSED_RATIO);
        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx,
                    Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.06, true);

            if (approx.toArray().length == 4 && Math.abs(Imgproc.contourArea(approx)) > area
                    && Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))) {
                squares.add(new MatOfPoint(approx.toArray()));
            }
        }

        // Fill output DEBUG Mat with corresponding colors
        Mat res = new Mat(src.rows(), src.cols(), CvType.CV_8UC3);
        for (int i = 0; i < src.rows(); i++) {
            for (int j = 0; j < src.cols(); j++) {
                double data = src.get(i, j)[0];
                res.put(i, j, data, data, data);
            }
        }

        // Filter the black squares only
        ArrayList<Rect> blackSquaresRect = new ArrayList<>();
        for (int i = 0; i < squares.size(); i++) {
            // Calculate non zero color
            Rect rect = Imgproc.boundingRect(squares.get(i));
            int nonZero = Core.countNonZero(src.submat(rect));
            int total = rect.height * rect.width;
            double percentage = 1.0 - ((double) nonZero) / ((double) total);
            if (percentage > 0.8) {
                blackSquaresRect.add(rect);
                // DEBUG OUTPUT
                MatOfPoint matOfPoint = new MatOfPoint(new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Point(rect.x, rect.y + rect.height));
                Imgproc.drawContours(res, Arrays.asList(matOfPoint), 0, new Scalar(0, 255, 0), 3);
            }
        }

        // DEBUG OUTPUT
        Imgcodecs.imwrite(
                new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-1-contour.jpg").getAbsolutePath(),
                res);

        if (blackSquaresRect.size() == 0) {
            throw new RuntimeException("Black squares found is 0");
        }

        // Collect all squares for input
        final int ALIGN_TOLERANCE = 10;
        int mostLeftBlackSquareIndex = 0;
        int mostBottomBlackSquareIndex = 0;
        ArrayList<Rect> verticalSquares = new ArrayList<>();
        ArrayList<Rect> horizontalSquares = new ArrayList<>();

        // RESET MAT DEBUG
        for (int i = 0; i < src.rows(); i++) {
            for (int j = 0; j < src.cols(); j++) {
                double data = src.get(i, j)[0];
                res.put(i, j, data, data, data);
            }
        }

        // Find anchor
        for (int i = 1; i < blackSquaresRect.size(); i++) {
            if (getCenter(blackSquaresRect.get(mostLeftBlackSquareIndex)).x > getCenter(blackSquaresRect.get(i)).x) {
                mostLeftBlackSquareIndex = i;
            }
            if (getCenter(blackSquaresRect.get(mostLeftBlackSquareIndex)).y < getCenter(blackSquaresRect.get(i)).y) {
                mostBottomBlackSquareIndex = i;
            }
        }

        // Find squares on vertical most left
        for (int i = 0; i < blackSquaresRect.size(); i++) {
            if (Math.abs(getCenter(blackSquaresRect.get(mostLeftBlackSquareIndex)).x
                    - getCenter(blackSquaresRect.get(i)).x) <= ALIGN_TOLERANCE) {
                verticalSquares.add(blackSquaresRect.get(i));
            }
        }
        // Find squares on horizontal most bottom
        for (int i = 0; i < blackSquaresRect.size(); i++) {
            if (Math.abs(getCenter(blackSquaresRect.get(mostBottomBlackSquareIndex)).y
                    - getCenter(blackSquaresRect.get(i)).y) <= ALIGN_TOLERANCE) {
                horizontalSquares.add(blackSquaresRect.get(i));
            }
        }

        // Draw contour of horizontal and vertical squares
        final int RADIUS = 4;
        for (int i = 0; i < verticalSquares.size(); i++) {
            Rect rect = verticalSquares.get(i);
            drawContour(res, rect, new Scalar(0, 0, 255), 3);

            Point centerOfRect = getCenter(rect);
            for (int x = (int) centerOfRect.x - RADIUS; x <= (int) centerOfRect.x + RADIUS; x++) {
                for (int y = (int) centerOfRect.y - RADIUS; y <= (int) centerOfRect.y + RADIUS; y++) {
                    res.put(y, x, 0, 0, 255);
                }
            }
        }
        for (int i = 0; i < horizontalSquares.size(); i++) {
            Rect rect = horizontalSquares.get(i);
            drawContour(res, rect, new Scalar(255, 255, 0), 3);

            Point centerOfRect = getCenter(rect);
            for (int x = (int) centerOfRect.x - RADIUS; x <= (int) centerOfRect.x + RADIUS; x++) {
                for (int y = (int) centerOfRect.y - RADIUS; y <= (int) centerOfRect.y + RADIUS; y++) {
                    res.put(y, x, 255, 255, 0);
                }
            }
        }

        Collections.sort(verticalSquares, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return Integer.valueOf(o1.y).compareTo(o2.y);
            }
        });
        Collections.sort(horizontalSquares, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return Integer.valueOf(o1.x).compareTo(o2.x);
            }
        });

        // Average of all black squares width and height
        int averageWidth = 0, averageHeight = 0;

        for (int i = 0; i < verticalSquares.size(); i++) {
            averageHeight += verticalSquares.get(i).height;
        }
        averageHeight /= verticalSquares.size();

        for (int i = 0; i < horizontalSquares.size(); i++) {
            averageWidth += horizontalSquares.get(i).width;
        }
        averageWidth /= horizontalSquares.size();

        /*
         * // ANSWER SHEET TYPE P-40 int[] blackSquareVerticalIndices = new int[] { 0,
         * 1, 0, 1, 0, 1, 2, 3, 2, 3, 2, 3 }; int[] blackSquareHorizontalIndices = new
         * int[] { 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5 }; int[] segmentNumberColumns =
         * new int[] { 3, 5, 5, 3, 5, 5 }; int[] segmentNumberRows = new int[] { 10, 10,
         * 10, 10, 10, 10 }; // char[] groupIn = new char[] {'V', 'H', 'H', 'V', 'H',
         * 'H'}; // For grouping // by horizontal line or vertical line String[] label =
         * new String[] { "ExCode", "Answer", "Answer", "MCode", "Answer", "Answer" };
         * int[] startFrom = new int[] { 0, 1, 11, 0, 21, 31 }; int[] ext = new int[] {
         * '1', 'A', 'A', '1', 'A', 'A' };
         */

        // Create debug folder
        File folder = new File(file, fileName.substring(0, fileName.lastIndexOf('.')));
        folder.mkdirs();
        File contentFolder = new File(folder, "Content");
        contentFolder.mkdirs();

        ArrayList<AnswerMat> answerMats = new ArrayList<>();
        // Get all rectangles using metadata
        for (int i = 0; i < metadata.getValueLength(); i++) {
            answerMats.addAll(findAllRect(src, res, verticalSquares, horizontalSquares, metadata.getValue(i),
                    averageWidth, averageHeight, folder));
        }

        Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-2-class-square.jpg")
                .getAbsolutePath(), res);
        // End draw
        return answerMats;
    }

    private static ArrayList<AnswerMat> findAllRect(Mat src, Mat drawOn, ArrayList<Rect> vertical,
            ArrayList<Rect> horizontal, Value metadataValue, int averageWidth, int averageHeight, File folder) {
        return findAllRect(src, drawOn, vertical.get(metadataValue.startVerticalIndex),
                vertical.get(metadataValue.endVerticalIndex), horizontal.get(metadataValue.startHorizontalIndex),
                horizontal.get(metadataValue.endHorizontalIndex), metadataValue.rowCount, metadataValue.columnCount,
                averageWidth, averageHeight, folder, metadataValue.label, metadataValue.startRowInteger,
                metadataValue.startColumnChar);
    }

    private static ArrayList<AnswerMat> findAllRect(Mat src, Mat drawOn, Rect firstVerticalRect,
            Rect secondVerticalRect, Rect firstHorizontalRect, Rect secondHorizontalRect, int numberOfRows,
            int numberOfCols, int averageWidth, int averageHeight, File folder, String firstname, int firstExt,
            int secondExt) {
        final int RADIUS = 4;
        ArrayList<AnswerMat> answerMats = new ArrayList<>();
        Point centerStartVerticalPoint = getCenter(firstVerticalRect);
        Point centerEndVerticalPoint = getCenter(secondVerticalRect);
        Point centerStartHorizontalPoint = getCenter(firstHorizontalRect);
        Point centerEndHorizontalPoint = getCenter(secondHorizontalRect);
        double vDiff = (centerEndVerticalPoint.y - centerStartVerticalPoint.y) / (double) (numberOfRows - 1);
        double hDiff = (centerEndHorizontalPoint.x - centerStartHorizontalPoint.x) / (double) (numberOfCols - 1);
        double startCenterY = centerStartVerticalPoint.y;
        for (int i = 0; i < numberOfRows; i++, startCenterY += vDiff) {
            double startCenterX = centerStartHorizontalPoint.x;
            for (int j = 0; j < numberOfCols; j++, startCenterX += hDiff) {
                for (int x = (int) startCenterX - RADIUS; x <= (int) startCenterX + RADIUS; x++) {
                    for (int y = (int) startCenterY - RADIUS; y <= (int) startCenterY + RADIUS; y++) {
                        drawOn.put(y, x, 0, 255, 0);
                    }
                }
                Mat mat = findSquareFromCenter(src, drawOn, (int) Math.round(startCenterX),
                        (int) Math.round(startCenterY), averageWidth, averageHeight, folder,
                        firstname + "-" + String.valueOf(firstExt + i) + "-" + (char) (secondExt + j));

                AnswerSheetLabel label;
                if (firstname.equals("ExCode")) {
                    label = new ExCodeLabel(secondExt + j - '1', firstExt + i);
                } else if (firstname.equals("MCode")) {
                    label = new MCodeLabel(secondExt + j - '1', firstExt + i);
                } else {
                    label = new AnswerLabel(firstExt + i, Option.getOption((char) (secondExt + j)));
                }
                answerMats.add(new AnswerMat(mat, label));
                Imgcodecs.imwrite(new File(new File(folder, "Content"),
                        firstname + "-" + String.valueOf(firstExt + i) + "-" + (char) (secondExt + j) + ".jpg")
                                .getAbsolutePath(),
                        mat);
            }
        }

        return answerMats;
    }

    private static Mat findSquareFromCenter(Mat src, Mat drawOn, int x, int y, int predictedWidth, int predictedHeight,
            File directory, String filename) {
        Point currentCenter = new Point(x, y);

        /*
         * Option 2 (concern in accuracy: the size of processed rectangle ->
         * ERROR_RATE_SCALE)
         */
        // Get the smallest possible rectangle to determine the questioned rectangle
        int width, height;
        final double ERROR_RATE_SCALE = 1.5;
        width = (int) Math.round(predictedWidth * ERROR_RATE_SCALE);
        height = (int) Math.round(predictedHeight * ERROR_RATE_SCALE);
        Rect currentRect = new Rect(new Point(currentCenter.x - width / 2, currentCenter.y - height / 2),
                new Size(width, height));
        Mat square = src.submat(currentRect);
        Mat sqDrawOn = drawOn.submat(currentRect);

        // Find the most fit rectangle
        Point rectStartPoint = findMostFitRect(square, predictedWidth, predictedHeight,
                (int) Math.round(PROCESSED_ANSWER_SQUARE_BORDER * PROCESSED_RATIO));
        currentRect = new Rect(rectStartPoint, new Size(predictedWidth, predictedHeight));
        currentRect = scaleRectOnCenter(currentRect, 1 - 2.5 * (double) PROCESSED_ANSWER_SQUARE_BORDER
                / (double) Math.min(PROCESSED_SQUARE_HEIGHT, PROCESSED_SQUARE_WIDTH));
        drawContour(sqDrawOn, currentRect, new Scalar(255, 0, 255), 2);
        Imgcodecs.imwrite(new File(directory, filename + ".jpg").getAbsolutePath(), sqDrawOn);

        return square.submat(currentRect).clone();
    }

    private static Rect scaleRectOnCenter(Rect rect, double scale) {
        int newWidth = (int) Math.round(rect.width * scale);
        int newHeight = (int) Math.round(rect.height * scale);
        int newX = rect.x + (int) (rect.width * (1 - scale) / 2);
        int newY = rect.y + (int) (rect.height * (1 - scale) / 2);
        return new Rect(newX, newY, newWidth, newHeight);
    }

    private static Point findMostFitRect(Mat mat, int width, int height, int insideBorderThickness) {
        int maxCol = mat.cols() - width + 1, maxRow = mat.rows() - height + 1;
        if (maxCol <= 0 || maxRow <= 0)
            return new Point(0, 0);
        int x, y, maxBlackPixel, xStart, yStart, outsideRectBlackPixel, insideRectBlackPixel, currBlackPixel;
        int[][] blackPixel = new int[mat.rows() + 1][mat.cols() + 1];
        for (y = 0; y < mat.rows(); y++) {
            blackPixel[y][0] = 0;
        }
        for (x = 0; x < mat.cols(); x++) {
            blackPixel[0][x] = 0;
        }
        /*
         * Mat sum = new Mat(mat.rows(), mat.cols(), CvType.CV_32SC1);
         * Imgproc.integral(mat, sum);
         */
        // Integral image (OpenCV)
        for (y = 1; y <= mat.rows(); y++) {
            for (x = 1; x <= mat.cols(); x++) {
                blackPixel[y][x] = blackPixel[y - 1][x] + blackPixel[y][x - 1] - blackPixel[y - 1][x - 1];
                if (mat.get(y - 1, x - 1)[0] == 0) { // if the color is black
                    blackPixel[y][x]++;
                }
            }
        }

        xStart = yStart = -1;
        maxBlackPixel = -1;
        for (y = 1; y <= maxRow; y++) {
            for (x = 1; x <= maxCol; x++) {
                outsideRectBlackPixel = blackPixel[y + height - 1][x + width - 1] - blackPixel[y + height - 1][x - 1]
                        - blackPixel[y - 1][x + width - 1] + blackPixel[y - 1][x - 1];

                insideRectBlackPixel = blackPixel[y + height - insideBorderThickness - 1][x + width
                        - insideBorderThickness - 1]
                        - blackPixel[y + height - insideBorderThickness - 1][x + insideBorderThickness - 1]
                        - blackPixel[y + insideBorderThickness - 1][x + width - insideBorderThickness - 1]
                        + blackPixel[y + insideBorderThickness - 1][x + insideBorderThickness - 1];

                currBlackPixel = outsideRectBlackPixel - insideRectBlackPixel;
                if (maxBlackPixel < currBlackPixel) {
                    maxBlackPixel = currBlackPixel;
                    xStart = x;
                    yStart = y;
                }
            }
        }

        return new Point(xStart - 1, yStart - 1);
    }

    private static void drawContour(Mat mat, Rect rect, Scalar color, int thickness) {
        MatOfPoint matOfPoint = new MatOfPoint(new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height), new Point(rect.x, rect.y + rect.height));
        Imgproc.drawContours(mat, Arrays.asList(matOfPoint), 0, color, thickness);
    }

    private static double angle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
    }

    private static Point getCenter(Rect rect) {
        if (rect == null)
            return null;
        Point center = new Point();
        center.x = rect.x + rect.width / 2;
        center.y = rect.y + rect.height / 2;

        return center;
    }
}
