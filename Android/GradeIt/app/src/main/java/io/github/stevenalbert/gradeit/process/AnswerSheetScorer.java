package io.github.stevenalbert.gradeit.process;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.AnswerMat;
import io.github.stevenalbert.gradeit.model.AnswerSheet;
import io.github.stevenalbert.gradeit.model.AnswerSheetMetadata;
import io.github.stevenalbert.gradeit.model.AnswerSheetMetadata.PaperDimension;
import io.github.stevenalbert.gradeit.model.AnswerSheetMetadata.Value;
import io.github.stevenalbert.gradeit.model.Option;
import io.github.stevenalbert.gradeit.model.label.AnswerLabel;
import io.github.stevenalbert.gradeit.model.label.AnswerSheetLabel;
import io.github.stevenalbert.gradeit.model.label.ExCodeLabel;
import io.github.stevenalbert.gradeit.model.label.MCodeLabel;

public class AnswerSheetScorer {

    /**
     * Convert answer sheet photo image to processable answer sheet image
     * 
     * @param src
     *            - answer sheet photo image
     * @return processable answer sheet image
     */
    public static Mat convertAnswerSheet(Mat src, AnswerSheetMetadata metadata) {
        long ttlTime = -System.nanoTime();
        // Check whether the argument is valid
        if (src == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        // Declare variables
        int srcRows = src.rows(), srcCols = src.cols();
        Mat result = new Mat(srcRows, srcCols, CvType.CV_8UC1);

        // Turn the photo image to grayscale image
        if(src.channels() == 4) Imgproc.cvtColor(src, result, Imgproc.COLOR_RGBA2GRAY);
        else Imgproc.cvtColor(src, result, Imgproc.COLOR_RGB2GRAY);

		// Normalized Box Blur
		Imgproc.blur(result, result, new Size(3, 3));

        // Adaptive Gaussian Thresholding
        int blockSize = 171;
        int C = 6;

        long time = -System.nanoTime();
        Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,
                blockSize, C);
        time += System.nanoTime();
        System.out.println("convert - Adaptive: " + String.format("%.3f", time / 1e9) + "s");
        // Find all contour on mat
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        time = -System.nanoTime();
        Mat copyResultForContour = result.clone();
        Imgproc.findContours(copyResultForContour, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        copyResultForContour.release();
        time += System.nanoTime();
        System.out.println("convert - Find contours: " + String.format("%.3f", time / 1e9) + "s");

        ArrayList<MatOfPoint> squares = new ArrayList<>();

        // Test contours
        time = -System.nanoTime();
        MatOfPoint2f approx = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx,
                    0.02 * Math.min(metadata.getDimension().height, metadata.getDimension().width)
                    //Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.005
                    , true);

            // Note: absolute value of an area is used because
            // area may be positive or negative - in accordance with the
            // contour orientation
            double area = Math.abs(Imgproc.contourArea(approx));
            MatOfPoint approxArray = new MatOfPoint(approx.toArray());
            if (approx.toArray().length == 4 && area > srcCols * srcRows * 2 / 10
                    && area < srcCols * srcRows * 98 / 100
                    && Imgproc.isContourConvex(approxArray)) {
                squares.add(approxArray);
            }
        }
        time += System.nanoTime();
        System.out.println("convert - Filter contours: " + String.format("%.3f", time / 1e9) + "s");

        double maxArea = -1, secondMaxArea = -1;
        int maxIdx = -1, secondMaxIdx = -1;
        time = -System.nanoTime();
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
        time += System.nanoTime();
        System.out.println("convert - Find 2nd max area: " + String.format("%.3f", time / 1e9) + "s");

        if (secondMaxIdx < 0 && maxIdx < 0)
            return null;

        int idx = (secondMaxIdx > -1 ? secondMaxIdx : (maxIdx > -1 ? maxIdx : -1));

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

        assert topLeftPoint != null && topRightPoint != null && bottomLeftPoint != null && bottomRightPoint != null;

        int perspectiveWidth, perspectiveHeight;
        perspectiveHeight = metadata.getDimension().height;
        perspectiveWidth = metadata.getDimension().width;

        time = -System.nanoTime();
        Mat perspective = new Mat(perspectiveHeight, perspectiveWidth, src.type());
        MatOfPoint2f srcPoints = new MatOfPoint2f(topLeftPoint, topRightPoint, bottomRightPoint, bottomLeftPoint);
        MatOfPoint2f dst = new MatOfPoint2f(new Point(0, 0), new Point(perspectiveWidth - 1, 0),
                new Point(perspectiveWidth - 1, perspectiveHeight - 1), new Point(0, perspectiveHeight - 1));

        Mat transform = Imgproc.getPerspectiveTransform(srcPoints, dst);
        Imgproc.warpPerspective(result, perspective, transform, new Size(perspectiveWidth, perspectiveHeight));
        time += System.nanoTime();
        System.out.println("convert - Perspective transform: " + String.format("%.3f", time / 1e9) + "s");

        Imgproc.adaptiveThreshold(perspective, perspective, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, blockSize, C);

        ttlTime += System.nanoTime();
        System.out.println("convert - Total time: " + String.format("%.3f", ttlTime / 1e9) + "s");

        return perspective;
    }

    /**
     * Process converted answer sheet image to get identity and all the answers
     * 
     * @param src
     *            - answer sheet photo image
     * @return answerMats
     */
    public static ArrayList<AnswerMat> processAnswerSheet(Mat src, Mat outputDraw, AnswerSheetMetadata metadata) throws Exception {
        // Check whether the argument is valid
        if (src == null)
            throw new Exception("Paper not found");

        // Initialize for drawing
        if(outputDraw.rows() != src.rows() || outputDraw.cols() != src.cols() || outputDraw.channels() != 3) {
            throw new IllegalArgumentException("Argument outputDraw must have the same rows and cols as src with 3 channels");
        }
        Imgproc.cvtColor(src, outputDraw, Imgproc.COLOR_GRAY2BGR);

        // Find all contours
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat copyForContour = src.clone();
        Imgproc.findContours(copyForContour, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        copyForContour.release();

        // Find the fit squares
        ArrayList<MatOfPoint> squares = new ArrayList<>();
        MatOfPoint2f approx = new MatOfPoint2f();
        int area = (int) (metadata.getDimension().squareHeight * metadata.getDimension().squareWidth);
        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            MatOfPoint2f point2f = new MatOfPoint2f(contours.get(i).toArray());
            Imgproc.approxPolyDP(point2f, approx,
                    Imgproc.arcLength(point2f, true) * 0.08, true);

            MatOfPoint approxArray = new MatOfPoint(approx.toArray());
            if (approx.toArray().length == 4 && Math.abs(Imgproc.contourArea(approx)) > area
                    && Imgproc.isContourConvex(approxArray)) {
                squares.add(approxArray);
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
            }
        }

        for(Rect rect : blackSquaresRect) {
            drawContour(outputDraw, rect, new Scalar(255, 0, 0), 2);
        }

        if (blackSquaresRect.size() == 0) {
            throw new Exception("Black squares found is 0.");
        }

        // Collect all squares for input
        final int ALIGN_TOLERANCE = 10;
        int mostLeftBlackSquareIndex = 0;
        int mostBottomBlackSquareIndex = 0;
        ArrayList<Rect> verticalSquares = new ArrayList<>();
        ArrayList<Rect> horizontalSquares = new ArrayList<>();

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

        Collections.sort(verticalSquares, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return Integer.compare(o1.y, o2.y);
            }
        });
        Collections.sort(horizontalSquares, new Comparator<Rect>() {
            @Override
            public int compare(Rect o1, Rect o2) {
                return Integer.compare(o1.x, o2.x);
            }
        });

        // Average of all black squares width and height
        int averageWidth = 0, averageHeight = 0;

        for (int i = 0; i < verticalSquares.size(); i++) {
            averageHeight += verticalSquares.get(i).height;
        }
        averageHeight = averageHeight / verticalSquares.size() + (averageHeight % verticalSquares.size() * 2 >= verticalSquares.size() ? 1 : 0);
//        averageHeight = averageHeight * 96 / 100;

        for (int i = 0; i < horizontalSquares.size(); i++) {
            averageWidth += horizontalSquares.get(i).width;
        }
        averageWidth = averageWidth / horizontalSquares.size() + (averageWidth % horizontalSquares.size() * 2 >= horizontalSquares.size() ? 1 : 0);
//        averageWidth = averageWidth * 96 / 100;

        ArrayList<AnswerMat> answerMats = new ArrayList<>();
        // Get all rectangles using meta data
        for (int i = 0; i < metadata.getValueLength(); i++) {
            answerMats.addAll(findAllRect(src, outputDraw, verticalSquares, horizontalSquares, metadata.getValue(i),
                    averageWidth, averageHeight, metadata.getDimension()));
        }

        // Filter Noise AnswerMat
        for (AnswerMat answerMat : answerMats) {
            Scalar blackScalar = new Scalar(0);
//            drawContour(answerMat, new Rect(0, 0, answerMat.cols(), answerMat.rows()), blackScalar, 2);
            ArrayList<MatOfPoint> answerMatContours = new ArrayList<>(), filledContours = new ArrayList<>();
            Mat copyAnswerMat = answerMat.clone();
            Imgproc.findContours(copyAnswerMat, answerMatContours, new Mat(), Imgproc.RETR_LIST,
                    Imgproc.CHAIN_APPROX_SIMPLE);
            copyAnswerMat.release();
            for (MatOfPoint contour : answerMatContours) {
                Rect contourRect = Imgproc.boundingRect(contour);
                if (contourRect.area() < area / 25) {
                    filledContours.add(contour);
                }
            }
            Imgproc.drawContours(answerMat, filledContours, -1, blackScalar, Core.FILLED);
            answerMatContours.clear();
            filledContours.clear();
        }

        // End draw
        Collections.sort(answerMats);
        return answerMats;
    }

    public static AnswerSheet recognizeAnswerSheet(ArrayList<AnswerMat> answerMats) {
        final int TOTAL_ANSWER = 40;
        AnswerSheet answerSheet = new AnswerSheet(TOTAL_ANSWER);
        StringBuilder mCode = new StringBuilder("000");
        StringBuilder exCode = new StringBuilder("000");

        for (AnswerMat answerMat : answerMats) {
            int value = (int) FeatureExtractor.getFeatureX(answerMat);
            double zVal = BigDecimal.valueOf((double) value).round(new MathContext(3)).doubleValue();
            final double zThreshold = 15.0;
            boolean isX = (zVal >= zThreshold);

            AnswerSheetLabel label = answerMat.getLabel();
            if (label instanceof AnswerLabel) {
                AnswerLabel answerLabel = (AnswerLabel) label;
                // Set true if the X is recognized, otherwise false.
                answerSheet.setAnswerOn(answerLabel.getNumber(), answerLabel.getOption(), isX);
            } else if (label instanceof ExCodeLabel) {
                ExCodeLabel exCodeLabel = (ExCodeLabel) label;
                // Change the value if it recognized as X
                if (isX)
                    exCode.setCharAt(exCodeLabel.getColumnNumber(), (char) (exCodeLabel.getValue() + '0'));
            } else if (label instanceof MCodeLabel) {
                MCodeLabel mCodeLabel = (MCodeLabel) label;
                // Change the value if it recognized as X
                if (isX)
                    mCode.setCharAt(mCodeLabel.getColumnNumber(), (char) (mCodeLabel.getValue() + '0'));
            }
        }

        // Set ExCode and MCode
        answerSheet.setExCode(exCode.toString());
        answerSheet.setMCode(mCode.toString());

        return answerSheet;
    }

    public static void scoreAnswerSheet(AnswerSheet answerSheet, AnswerKey answerKey) {
        answerSheet.scoreAnswerSheet(answerKey);
    }

    private static ArrayList<AnswerMat> findAllRect(Mat src, Mat outputDraw, ArrayList<Rect> vertical,
            ArrayList<Rect> horizontal, Value metadataValue, int width, int height, PaperDimension paperDimension) {
        return findAllRect(src, outputDraw, vertical.get(metadataValue.startVerticalIndex),
                vertical.get(metadataValue.endVerticalIndex), horizontal.get(metadataValue.startHorizontalIndex),
                horizontal.get(metadataValue.endHorizontalIndex), metadataValue.rowCount, metadataValue.columnCount,
                width, height, metadataValue.label, metadataValue.startRowInteger, metadataValue.startColumnChar,
                paperDimension);
    }

    private static ArrayList<AnswerMat> findAllRect(Mat src, Mat outputDraw, Rect firstVerticalRect,
            Rect secondVerticalRect, Rect firstHorizontalRect, Rect secondHorizontalRect, int numberOfRows,
            int numberOfCols, int averageWidth, int averageHeight, String firstname, int firstExt, int secondExt,
            PaperDimension paperDimension) {
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
                Mat mat = findSquareFromCenter(src, outputDraw, (int) Math.round(startCenterX),
                        (int) Math.round(startCenterY), averageWidth, averageHeight, paperDimension);

                AnswerSheetLabel label;
                if (firstname.equals("ExCode")) {
                    label = new ExCodeLabel(secondExt + j - '1', firstExt + i);
                } else if (firstname.equals("MCode")) {
                    label = new MCodeLabel(secondExt + j - '1', firstExt + i);
                } else {
                    label = new AnswerLabel(firstExt + i, Option.getOption((char) (secondExt + j)));
                }
                answerMats.add(new AnswerMat(mat, label));
            }
        }

        return answerMats;
    }

    private static Mat findSquareFromCenter(Mat src, Mat outputDraw, int x, int y, int width, int height,
            PaperDimension dimension) {
        Point currentCenter = new Point(x, y);

        // Get the smallest possible rectangle to determine the questioned rectangle
        final double ERROR_RATE_SCALE = 1.5;
        int overscaleWidth = (int) Math.round(width * ERROR_RATE_SCALE);
        int overscaleHeight = (int) Math.round(height * ERROR_RATE_SCALE);
        Rect currentRect = new Rect(
                new Point(currentCenter.x - overscaleWidth / 2, currentCenter.y - overscaleHeight / 2),
                new Size(overscaleWidth, overscaleHeight));
        Mat square = src.submat(currentRect);
        Mat sqOutputDraw = outputDraw.submat(currentRect);

        // Find the most fit rectangle
        Point rectStartPoint = findMostFitRect(square, width, height, dimension.squareAnswerBorder);

        int contentPadding = dimension.squareAnswerBorder;
        currentRect = new Rect((int) (rectStartPoint.x + contentPadding), (int) (rectStartPoint.y + contentPadding),
                width - 2 * contentPadding, height - 2 * contentPadding);

        drawContour(sqOutputDraw, currentRect, new Scalar(0, 255, 0), 2);

        return square.submat(currentRect).clone();
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

        // Integral image
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

    private static Point getCenter(Rect rect) {
        if (rect == null)
            return null;
        Point center = new Point();
        center.x = rect.x + rect.width / 2;
        center.y = rect.y + rect.height / 2;

        return center;
    }

    private static void drawContour(Mat mat, Rect rect, Scalar color, int thickness) {
        MatOfPoint matOfPoint = new MatOfPoint(new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height), new Point(rect.x, rect.y + rect.height));
        Imgproc.drawContours(mat, Arrays.asList(matOfPoint), 0, color, thickness);
    }
}
