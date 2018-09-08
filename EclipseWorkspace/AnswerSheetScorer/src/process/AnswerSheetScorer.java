package process;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
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
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import model.AnswerKey;
import model.AnswerMat;
import model.AnswerSheet;
import model.AnswerSheetMetadata;
import model.AnswerSheetMetadata.PaperDimension;
import model.AnswerSheetMetadata.Value;
import model.Option;
import model.label.AnswerLabel;
import model.label.AnswerSheetLabel;
import model.label.ExCodeLabel;
import model.label.MCodeLabel;

public class AnswerSheetScorer {

    private static int count = 0;
    private static File currentFolder;
    /**
     * Convert answer sheet photo image to processable answer sheet image
     * 
     * @param src
     *            - answer sheet photo image
     * @return processable answer sheet image
     */
    public static Mat convertAnswerSheet(Mat src, AnswerSheetMetadata metadata, File file, String fileName,
            boolean debugOutput) {
        // Check whether the argument is valid
        if (src == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        }

        // Declare variables
        Mat result = new Mat(src.rows(), src.cols(), CvType.CV_8UC1);
        int counter = 1;
        // Turn the photo image to grayscale image
        Imgproc.cvtColor(src, result, Imgproc.COLOR_RGB2GRAY);
        // Imgcodecs.imwrite(new File(file, (counter++) +
        // "-gray.jpg").getAbsolutePath(), result);
        // Black and white
        // Binary Thresholding
        // Imgproc.threshold(result, result, 120, 255, Imgproc.THRESH_BINARY);
        Imgproc.blur(result, result, new Size(3, 3));
        // Imgproc.GaussianBlur(result, result, new Size(3, 3), 0);
        // Adaptive Gaussian Thresholding
        int blockSize = 171;// (int) (Math.max(PROCESSED_SQUARE_HEIGHT, PROCESSED_SQUARE_WIDTH) *
                            // PROCESSED_RATIO * 4) + 1;
        int C = 6;// (int) (Math.max(PROCESSED_SQUARE_HEIGHT, PROCESSED_SQUARE_WIDTH) *
                  // PROCESSED_RATIO) / 4;
        Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,
                blockSize, C);
        // Adaptive Mean Thresholding
        // Imgproc.adaptiveThreshold(result, result, 255,
        // Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 69, 25);

        // DEBUG
        if(debugOutput) {
            Imgcodecs.imwrite(new File(file, (counter++) + "-thres.jpg").getAbsolutePath(), result);
        }

        // Find all contour on mat
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if (debugOutput) {
            Mat drawSquareMat = new Mat(result, Range.all());
            Imgproc.cvtColor(drawSquareMat, drawSquareMat, Imgproc.COLOR_GRAY2RGB);
            Imgproc.drawContours(drawSquareMat, contours, -1, new Scalar(0, 0, 255), 10);
            Imgcodecs.imwrite(new File(file, (counter++) + "-contours.jpg").getAbsolutePath(), drawSquareMat);
        }

        ArrayList<MatOfPoint> squares = new ArrayList<>();

        // Test contours
        MatOfPoint2f approx = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx,
                    0.02 * Math.min(metadata.getDimension().height, metadata.getDimension().width)
                    /*
                     * Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.005
                     */, true);

            // Note: absolute value of an area is used because
            // area may be positive or negative - in accordance with the
            // contour orientation
            double area = Math.abs(Imgproc.contourArea(approx));
            if (approx.toArray().length == 4 && area > src.cols() * src.rows() * 2 / 10
                    && area < src.cols() * src.rows() * 98 / 100
                    && Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))) {
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

        if (secondMaxIdx < 0 && maxIdx < 0)
            return null;
        System.out.println("Draw second max area square");
        int idx = (secondMaxIdx > -1 ? secondMaxIdx : (maxIdx > -1 ? maxIdx : -1));

        if (debugOutput) {
            Mat drawSquareMat = new Mat(result, Range.all());
            Imgproc.cvtColor(drawSquareMat, drawSquareMat, Imgproc.COLOR_GRAY2RGB);
            Imgproc.drawContours(drawSquareMat, squares, idx, new Scalar(0, 0, 255), 10);
            Imgcodecs.imwrite(new File(file, (counter++) + "-bw-getsquare.jpg").getAbsolutePath(), drawSquareMat);
        }

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
        System.out.println("Start perspective transform");
        Mat perspective = new Mat(perspectiveHeight, perspectiveWidth, src.type());
        MatOfPoint2f srcPoints = new MatOfPoint2f(topLeftPoint, topRightPoint, bottomRightPoint, bottomLeftPoint);
        MatOfPoint2f dst = new MatOfPoint2f(new Point(0, 0), new Point(perspective.cols() - 1, 0),
                new Point(perspective.cols() - 1, perspective.rows() - 1), new Point(0, perspective.rows() - 1));

        Mat transform = Imgproc.getPerspectiveTransform(srcPoints, dst);
        Imgproc.warpPerspective(result, perspective, transform, new Size(perspective.cols(), perspective.rows()));
        Imgproc.adaptiveThreshold(perspective, perspective, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY, blockSize, C);

        if (debugOutput)
            Imgcodecs.imwrite(new File(file, (counter++) + "-transform.jpg").getAbsolutePath(), perspective);

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
            String fileName, boolean debugOutput) {
        // Check whether the argument is valid
        if (src == null)
            throw new IllegalArgumentException("Argument src cannot be null");

        int counter = 1;
        
        // Find all contours
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src.clone(), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        if (debugOutput) {
            Mat drawSquareMat = src.clone();
            Imgproc.cvtColor(drawSquareMat, drawSquareMat, Imgproc.COLOR_GRAY2RGB);
            Imgproc.drawContours(drawSquareMat, contours, -1, new Scalar(0, 0, 255), 4);
            Imgcodecs.imwrite(new File(file, "2-" + (counter++) + "-contours.jpg").getAbsolutePath(), drawSquareMat);
        }

        // Find the fit squares
        ArrayList<MatOfPoint> squares = new ArrayList<>();
        MatOfPoint2f approx = new MatOfPoint2f();
        int area = (int) (metadata.getDimension().squareHeight * metadata.getDimension().squareWidth);
        for (int i = 0; i < contours.size(); i++) {
            // approximate contour with accuracy proportional
            // to the contour perimeter
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approx,
                    Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true) * 0.08, true);

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
                if (debugOutput) {
                    MatOfPoint matOfPoint = new MatOfPoint(new Point(rect.x, rect.y),
                            new Point(rect.x + rect.width, rect.y),
                            new Point(rect.x + rect.width, rect.y + rect.height),
                            new Point(rect.x, rect.y + rect.height));
                    Imgproc.drawContours(res, Arrays.asList(matOfPoint), 0, new Scalar(0, 255, 0), 5);
                }
            }
        }

        // DEBUG OUTPUT
        if (debugOutput) {
            Imgcodecs.imwrite(new File(file, "2-" + (counter++) + "-key-squares.jpg").getAbsolutePath(), res);
        }

        if (blackSquaresRect.size() == 0) {
            if (debugOutput) {
                Imgproc.drawContours(res, contours, -1, new Scalar(0, 255, 255), 3);
                Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-fail-contour.jpg")
                        .getAbsolutePath(), res);
            }
            throw new RuntimeException("Black squares found is 0 from " + contours.size() + " contours.");
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

        // RESET MAT DEBUG
        if (debugOutput) {
            for (int i = 0; i < src.rows(); i++) {
                for (int j = 0; j < src.cols(); j++) {
                    double data = src.get(i, j)[0];
                    res.put(i, j, data, data, data);
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
            Imgcodecs.imwrite(new File(file, "2-" + (counter++) + "-v-h-key.jpg").getAbsolutePath(), res);
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
        averageHeight = averageHeight / verticalSquares.size() + (averageHeight % verticalSquares.size() * 2 >= verticalSquares.size() ? 1 : 0);
//        averageHeight = averageHeight * 96 / 100;

        for (int i = 0; i < horizontalSquares.size(); i++) {
            averageWidth += horizontalSquares.get(i).width;
        }
        averageWidth = averageWidth / horizontalSquares.size() + (averageWidth % horizontalSquares.size() * 2 >= horizontalSquares.size() ? 1 : 0);
//        averageWidth = averageWidth * 96 / 100;

        // Create debug folder
        File folder = new File(file, "Content");
        currentFolder = folder;
        if (debugOutput)
            folder.mkdirs();

        ArrayList<AnswerMat> answerMats = new ArrayList<>();
        // Get all rectangles using meta data
        for (int i = 0; i < metadata.getValueLength(); i++) {
            answerMats.addAll(findAllRect(src, res, verticalSquares, horizontalSquares, metadata.getValue(i),
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

        // Write to file for debug
        if (debugOutput) {
            for (AnswerMat answerMat : answerMats) {
                StringBuilder builder = new StringBuilder();
                builder.append(answerMat.getLabel().toString());
                Mat outputAnswerMat = new Mat(answerMat.rows(), answerMat.cols(), answerMat.type());
                Core.bitwise_not(answerMat, outputAnswerMat);
                Imgcodecs.imwrite(new File(folder, answerMat.getLabel().toString() + "-content.jpg").getAbsolutePath(),
                        outputAnswerMat);
            }
            Imgcodecs.imwrite(new File(file, "overall-content.jpg").getAbsolutePath(), res);
        }
        // End draw
        Collections.sort(answerMats);
        return answerMats;
    }

    public static AnswerSheet recognizeAnswerSheet(ArrayList<AnswerMat> answerMats, File dir) {
        final int TOTAL_ANSWER = 40;
        AnswerSheet answerSheet = new AnswerSheet(TOTAL_ANSWER);
        StringBuilder mCode = new StringBuilder("000");
        StringBuilder exCode = new StringBuilder("000");

        for (AnswerMat answerMat : answerMats) {
            int value = (int) FeatureExtractor.getFeatureX(answerMat, true, true, dir.getAbsolutePath() + "/" + answerMat.getLabel().toString());
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

    private static ArrayList<AnswerMat> findAllRect(Mat src, Mat drawOn, ArrayList<Rect> vertical,
            ArrayList<Rect> horizontal, Value metadataValue, int width, int height, PaperDimension paperDimension) {
        return findAllRect(src, drawOn, vertical.get(metadataValue.startVerticalIndex),
                vertical.get(metadataValue.endVerticalIndex), horizontal.get(metadataValue.startHorizontalIndex),
                horizontal.get(metadataValue.endHorizontalIndex), metadataValue.rowCount, metadataValue.columnCount,
                width, height, metadataValue.label, metadataValue.startRowInteger, metadataValue.startColumnChar,
                paperDimension);
    }

    private static ArrayList<AnswerMat> findAllRect(Mat src, Mat drawOn, Rect firstVerticalRect,
            Rect secondVerticalRect, Rect firstHorizontalRect, Rect secondHorizontalRect, int numberOfRows,
            int numberOfCols, int averageWidth, int averageHeight, String firstname, int firstExt, int secondExt,
            PaperDimension paperDimension) {
        final int RADIUS = 3;
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
                AnswerSheetLabel label = null;
                if (firstname.equals("ExCode")) {
                    label = new ExCodeLabel(secondExt + j - '1', firstExt + i);
                } else if (firstname.equals("MCode")) {
                    label = new MCodeLabel(secondExt + j - '1', firstExt + i);
                } else {
                    label = new AnswerLabel(firstExt + i, Option.getOption((char) (secondExt + j)));
                }
                
//                System.out.print(label.toString());
                Mat mat = findSquareFromCenter(src, drawOn, (int) Math.round(startCenterX),
                        (int) Math.round(startCenterY), averageWidth, averageHeight, paperDimension,
                        firstname + "-" + String.valueOf(firstExt + i) + "-" + (char) (secondExt + j));

                answerMats.add(new AnswerMat(mat, label));
            }
        }

        return answerMats;
    }

    private static Mat findSquareFromCenter(Mat src, Mat drawOn, int x, int y, int width, int height,
            PaperDimension dimension, String filename) {
        Point currentCenter = new Point(x, y);

        /*
         * Option 2 (concern in accuracy: the size of processed rectangle ->
         * ERROR_RATE_SCALE)
         */
        // Get the smallest possible rectangle to determine the questioned rectangle
        final double ERROR_RATE_SCALE = 1.5;
        int overscaleWidth = (int) Math.round(width * ERROR_RATE_SCALE);
        int overscaleHeight = (int) Math.round(height * ERROR_RATE_SCALE);
        Rect currentRect = new Rect(
                new Point(currentCenter.x - overscaleWidth / 2, currentCenter.y - overscaleHeight / 2),
                new Size(overscaleWidth, overscaleHeight));
        Mat square = src.submat(currentRect);
        Mat sqDrawOn = drawOn.submat(currentRect);

        if(count == 0) {
            Imgcodecs.imwrite(new File(currentFolder, "../interest-square.jpg").getAbsolutePath(), sqDrawOn);
            count++;
        }
        
        // Find the most fit rectangle
        int border = dimension.squareAnswerBorder;
        Point rectStartPoint = findMostFitRect(square, width, height, border);
        currentRect = new Rect(rectStartPoint, new Size(width, height));
//        drawContour(sqDrawOn, currentRect, new Scalar(0, 0, 255), 3);
        /*
         * currentRect = scaleRectOnCenter(currentRect, 1 - 2.5 * (double)
         * dimension.squareAnswerBorder / (double) Math.min(width, height));
         */
        if(count == 1) {
            Imgcodecs.imwrite(new File(currentFolder, "../interest-square.jpg").getAbsolutePath(), sqDrawOn);
            count++;
        }

        int contentPadding = border;
        currentRect = new Rect((int) (rectStartPoint.x + contentPadding), (int) (rectStartPoint.y + contentPadding),
                width - 2 * contentPadding, height - 2 * contentPadding);
        drawContour(sqDrawOn, currentRect, new Scalar(0, 255, 0), 3);
        // Imgcodecs.imwrite(new File(directory, filename + ".jpg").getAbsolutePath(),
        // sqDrawOn);

        return square.submat(currentRect).clone();
    }

    private static Point findMostFitRect(Mat mat, int width, int height, int insideBorderThickness) {
        int matRows = mat.rows(), matCols = mat.cols();
        int maxCol = matCols - width + 1, maxRow = matRows - height + 1;
        if (maxCol <= 0 || maxRow <= 0)
            return new Point(0, 0);
        int x, y, maxBlackPixel, xStart, yStart, outsideRectBlackPixel, insideRectBlackPixel, currBlackPixel;
        int[][] blackPixel = new int[matRows + 1][matCols + 1];
        for (y = 0; y < matRows; y++) {
            blackPixel[y][0] = 0;
        }
        for (x = 0; x < matCols; x++) {
            blackPixel[0][x] = 0;
        }
        /*
         * Mat sum = new Mat(mat.rows(), mat.cols(), CvType.CV_32SC1);
         * Imgproc.integral(mat, sum);
         */
        // Integral image (OpenCV)
        for (y = 1; y <= matRows; y++) {
            for (x = 1; x <= matCols; x++) {
                blackPixel[y][x] = blackPixel[y - 1][x] + blackPixel[y][x - 1] - blackPixel[y - 1][x - 1];
                if (mat.get(y - 1, x - 1)[0] == 0) { // if the color is black
                    blackPixel[y][x]++;
                }
            }
        }
        
        int centerRow = matRows / 2;
        int centerCol = matCols / 2;
        int sqDistToCenter = matRows * matRows + matCols * matCols, sqDist;
        double intensity, maxIntensity = 0;
        double area = width * height - (width - 2 * insideBorderThickness) * (height - 2 * insideBorderThickness);

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
                intensity = ((double) currBlackPixel) / area;
//                intensity = new Double(Math.round(intensity * 100.0) / 100.0);
                if (maxIntensity < intensity) {
                    maxIntensity = intensity;
//                    sqDistToCenter = (((x - 1 + width) / 2) - centerCol) * (((x - 1 + width) / 2) - centerCol) 
//                            + (((y - 1 + height) / 2) - centerRow) * (((y - 1 + height) / 2) - centerRow);
                    xStart = x;
                    yStart = y;
//                } else if(maxIntensity == intensity) {
//                    sqDist = (((x - 1 + width) / 2) - centerCol) * (((x - 1 + width) / 2) - centerCol) 
//                            + (((y - 1 + height) / 2) - centerRow) * (((y - 1 + height) / 2) - centerRow);
//                    if(sqDistToCenter > sqDist) {
//                        sqDistToCenter = sqDist;
//                        xStart = x;
//                        yStart = y;
//                    }
                }
            }
        }

//        System.out.println(" " + maxIntensity + " " + sqDistToCenter + " " + centerRow + " " + centerCol + " " + ((xStart - 1 + width) / 2) + " " + ((yStart - 1 + height) / 2));
        return new Point(xStart - 1, yStart - 1);
    }

    private static void drawContour(Mat mat, Rect rect, Scalar color, int thickness) {
        MatOfPoint matOfPoint = new MatOfPoint(new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height), new Point(rect.x, rect.y + rect.height));
        Imgproc.drawContours(mat, Arrays.asList(matOfPoint), 0, color, thickness);
    }

    /*
     * private static double angle(Point pt1, Point pt2, Point pt0) { double dx1 =
     * pt1.x - pt0.x; double dy1 = pt1.y - pt0.y; double dx2 = pt2.x - pt0.x; double
     * dy2 = pt2.y - pt0.y; return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 +
     * dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10); }
     */
    private static Point getCenter(Rect rect) {
        if (rect == null)
            return null;
        Point center = new Point();
        center.x = rect.x + rect.width / 2;
        center.y = rect.y + rect.height / 2;

        return center;
    }
}
