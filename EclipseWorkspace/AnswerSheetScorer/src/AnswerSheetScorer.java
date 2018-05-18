
import java.util.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

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
	 * @param src - answer sheet photo image
	 * @return processable answer sheet image
	 */
	public static Mat convertAnswerSheet(Mat src, File file, String fileName) {
		// Check whether the argument is valid
		if(src == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}
		
		// Declare variables
		Mat result = new Mat(src.rows(), src.cols(), CvType.CV_8UC3);
		
		// Turn the photo image to grayscale image
		if(src.type() == CvType.CV_8UC3) {
			Imgproc.cvtColor(src, result, Imgproc.COLOR_RGB2GRAY);
		} else {
			result = src;
		}
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-1-gray.jpg").getAbsolutePath(), result);
		// Black and white
		for(int i=0; i<result.rows(); i++) {
			for(int j=0; j<result.cols(); j++) {
				if(result.get(i, j)[0] < 100) {
					result.put(i, j, 0);
				} else {
					result.put(i, j, 255);
				}
			}
		}
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-2-black-white.jpg").getAbsolutePath(), result);
/*		// Sharp
		Mat temp = new Mat(result.rows(), result.cols(), result.type());
		Imgproc.GaussianBlur(result, temp, new Size(0, 0), 10);
		Core.addWeighted(result, 1.5, temp, -0.5, 0, temp);
		result = temp.clone();
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "0-sharp.jpg").getAbsolutePath(), result);
*/		// Blur the grayscale image (reduce noise)
/*		Imgproc.GaussianBlur(result, result, new Size(5, 5), 0);
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "1-blur.jpg").getAbsolutePath(), result);
		// Erode
		int erosion_size = 1;
		Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2*erosion_size+1, 2*erosion_size+1));

		Imgproc.erode(result, result, erodeElement);
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "3-erode.jpg").getAbsolutePath(), result);
        // Dilate helps to remove potential holes between edge segments
		int dilation_size = 1;
		Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2*dilation_size+1, 2*dilation_size+1));
		
//		Imgproc.dilate(result, result, dilateElement);
//		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "2-dilate.jpg").getAbsolutePath(), result);
		// Adaptive Thresholding
		Imgproc.adaptiveThreshold(result, result, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 7);
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "4-thres.jpg").getAbsolutePath(), result);
*/		
		ArrayList<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		Mat res = new Mat(result.rows(), result.cols(), CvType.CV_8UC3);
		
		
		ArrayList<MatOfPoint> squares = new ArrayList<>();
		
        // Test contours
        MatOfPoint2f approx = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++)
        {
                // approximate contour with accuracy proportional
                // to the contour perimeter
                Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), 
                		approx, 
                		Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true)*0.06, 
                		true);

                // Note: absolute value of an area is used because
                // area may be positive or negative - in accordance with the
                // contour orientation
                if (approx.toArray().length == 4 &&
                        Math.abs(Imgproc.contourArea(approx)) > 1000 &&
                        Imgproc.isContourConvex(new MatOfPoint(approx.toArray())))
                {
                	double maxCosine = 0;

                    Point[] approxPoints = approx.toArray();
                    for (int j = 2; j < 5; j++)
                    {
                    	double cosine = Math.abs(angle(approxPoints[j%4], approxPoints[j-2], approxPoints[j-1]));
                    	maxCosine = Math.max(maxCosine, cosine);
                    }

                    if (maxCosine < 0.3)
                            squares.add(new MatOfPoint(approx.toArray()));
                }
        }

		
		double maxArea = -1, secondMaxArea = -1;
		int maxIdx = -1, secondMaxIdx = -1;
		for(int j=0; j<squares.size(); j++) {
			Rect rect = Imgproc.boundingRect(squares.get(j));
			double area = rect.area();
			if(area > maxArea) {
				secondMaxArea = maxArea;
				secondMaxIdx = maxIdx;
				maxIdx = j;
				maxArea = area;
			} else if(area > secondMaxArea) {
				secondMaxArea = area;
				secondMaxIdx = j;
			}
		}
		Imgproc.drawContours(res, squares, -1, new Scalar(0, 255, 0), 2);
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-3-contour.jpg").getAbsolutePath(), res);

		if(secondMaxIdx < 0 && maxIdx < 0) return null;
		System.out.println("Draw second max area square");
		int idx = (secondMaxIdx > -1 ? secondMaxIdx : (maxIdx > -1 ? maxIdx : -1));
		Imgproc.drawContours(src, squares, idx, new Scalar(0, 0, 255), 3);
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-4-square.jpg").getAbsolutePath(), src);

		// Give white color to the contour
		Imgproc.drawContours(result, squares, idx, new Scalar(255, 255, 255), 30);
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-5-square-prcs.jpg").getAbsolutePath(), result);
		
		/*
		//get the hierarchy
		int[] hierarchyVal = new int[hierarchy.channels() * (int)hierarchy.total()];
		hierarchy.get(0, 0, hierarchyVal);
		
		for(int i=0; i<contours.size(); i++) {
			if(hierarchyVal[i*hierarchy.channels() + 3] == -1) {
				Imgproc.drawContours(res, contours, i, new Scalar(0, 255, 0), 2);				
			} else {
				Imgproc.drawContours(res, contours, i, new Scalar(255, 0, 0), 2);
			}
		}*/
		
		// Find sorted 4 points : top left, top right, bottom right, bottom left
		Point[] points = squares.get(idx).toArray();
		Point topLeftPoint, topRightPoint, bottomRightPoint, bottomLeftPoint;
		// Find the most top and second most top
		int mostTopIdx = -1, secondMostTopIdx = -1;
		for(int i=0; i<points.length; i++) {
//			System.out.println("Point #" + i + ": " + points[i].x + "," + points[i].y);
		}
//		System.out.println("Size of mat: " + result.cols() + "," + result.rows());
		for(int i=0; i<points.length; i++) {
//			System.out.println("Current y: " + points[i].y);
/*			System.out.println("Current state: " + (mostTopIdx >= 0 ? points[mostTopIdx].y : -1) + 
					" | " + (secondMostTopIdx >= 0 ? points[secondMostTopIdx].y : -1));*/
			if(mostTopIdx >= 0 && points[i].y < points[mostTopIdx].y) {
				secondMostTopIdx = mostTopIdx;
				mostTopIdx = i;
			} else if(secondMostTopIdx >= 0 && points[i].y < points[secondMostTopIdx].y) {
				secondMostTopIdx = i;
			} else if(mostTopIdx < 0) {
				mostTopIdx = i;
			} else if(secondMostTopIdx < 0) {
				secondMostTopIdx = i;
			}
//			System.out.println("After checking: " + mostTopIdx + " | " + secondMostTopIdx);
		}
		if(points[mostTopIdx].x < points[secondMostTopIdx].x) {
			topLeftPoint = points[mostTopIdx];
			topRightPoint = points[secondMostTopIdx];
		} else {
			topLeftPoint = points[secondMostTopIdx];
			topRightPoint = points[mostTopIdx];			
		}
		// Find bottom left and bottom right
		int[] bottomIdx = new int[2];
		int countBottomIdx = 0;
		for(int i=0; i<points.length; i++) {
			if(i != mostTopIdx && i != secondMostTopIdx)
				bottomIdx[countBottomIdx++] = i;
		}
		if(points[bottomIdx[0]].x < points[bottomIdx[1]].x) {
			bottomLeftPoint = points[bottomIdx[0]];
			bottomRightPoint = points[bottomIdx[1]];
		} else {
			bottomLeftPoint = points[bottomIdx[1]];
			bottomRightPoint = points[bottomIdx[0]];			
		}
/*		
		System.out.println("Top left point: " + topLeftPoint.x + "," + topLeftPoint.y);
		System.out.println("Top right point: " + topRightPoint.x + "," + topRightPoint.y);
		System.out.println("Bottom right point: " + bottomRightPoint.x + "," + bottomRightPoint.y);
		System.out.println("Bottom left point: " + bottomLeftPoint.x + "," + bottomLeftPoint.y);
*/		
		
		int perspectiveWidth, perspectiveHeight;
		perspectiveHeight = (int) Math.round(PROCESSED_HEIGHT * PROCESSED_RATIO);
		perspectiveWidth = (int) Math.round(PROCESSED_WIDTH * PROCESSED_RATIO);
		System.out.println("Start perspective transform");
		Mat perspective = new Mat(perspectiveHeight, perspectiveWidth, src.type());
		MatOfPoint2f srcPoints = new MatOfPoint2f(
				topLeftPoint, topRightPoint,
				bottomRightPoint, bottomLeftPoint);
		MatOfPoint2f dst = new MatOfPoint2f(
				new Point(0, 0),
				new Point(perspective.cols()-1, 0),
				new Point(perspective.cols()-1, perspective.rows()-1),
				new Point(0, perspective.rows()-1)
		);
		
		Mat transform = Imgproc.getPerspectiveTransform(srcPoints, dst);
		Imgproc.warpPerspective(result, perspective, transform, new Size(perspective.cols(), perspective.rows()));
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-6-perspective.jpg").getAbsolutePath(), perspective);


		return perspective;
	}
	
	/** 
	 * Process converted answer sheet image to get identity and all the answers
	 * @param src - answer sheet photo image
	 * @return 
	 */
	public static void processAnswerSheet(Mat src, File file, String fileName) {
		// Check whether the argument is valid
		if(src == null) {
			throw new IllegalArgumentException("Argument cannot be null");
		}
		
		// Declare variables
		Mat result = new Mat(src.rows(), src.cols(), CvType.CV_8UC3);
		
		// Turn the photo image to grayscale image
		if(src.type() == CvType.CV_8UC3) {
			Imgproc.cvtColor(src, result, Imgproc.COLOR_RGB2GRAY);
		} else {
			result = src;
		}
		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-0-bw.jpg").getAbsolutePath(), result);

		ArrayList<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(result, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		
		ArrayList<MatOfPoint> squares = new ArrayList<>();
		
        // Test contours
        MatOfPoint2f approx = new MatOfPoint2f();
        int area = (int) (PROCESSED_SQUARE_HEIGHT * PROCESSED_SQUARE_WIDTH * PROCESSED_RATIO * PROCESSED_RATIO);
        for (int i = 0; i < contours.size(); i++)
        {
                // approximate contour with accuracy proportional
                // to the contour perimeter
                Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), 
                		approx, 
                		Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true)*0.06, 
                		true);

                // Note: absolute value of an area is used because
                // area may be positive or negative - in accordance with the
                // contour orientation
                /*if (approx.toArray().length == 4 &&
                        Math.abs(Imgproc.contourArea(approx)) > area &&
                        Imgproc.isContourConvex(new MatOfPoint(approx.toArray())))
                {
                	double maxCosine = 0;

                    Point[] approxPoints = approx.toArray();
                    for (int j = 2; j < 5; j++)
                    {
                    	double cosine = Math.abs(angle(approxPoints[j%4], approxPoints[j-2], approxPoints[j-1]));
                    	maxCosine = Math.max(maxCosine, cosine);
                    }

                    if (maxCosine < 0.3)
                            squares.add(new MatOfPoint(approx.toArray()));
                }*/
                if(Math.abs(Imgproc.contourArea(approx)) > area && 
                	Imgproc.isContourConvex(new MatOfPoint(approx.toArray()))) {
                	squares.add(new MatOfPoint(approx.toArray()));
                }
        }
        
		
		Mat res = new Mat(result.rows(), result.cols(), CvType.CV_8UC3);
		for(int i=0; i<result.rows(); i++) {
			for(int j=0; j<result.cols(); j++) {
				double data = result.get(i, j)[0];
				res.put(i, j, data, data, data);
			}
		}
        ArrayList<MatOfPoint> blackSquares = new ArrayList<>();
        ArrayList<Rect> blackSquaresRect = new ArrayList<>();

        for(int i=0; i<squares.size(); i++) {
			// Calculate non zero color
        	Rect rect = Imgproc.boundingRect(squares.get(i));
        	Mat subMat = result.submat(rect);
        	int nonZero = Core.countNonZero(subMat);
        	int total = rect.height * rect.width;
        	double percentage = 1.0 - ((double)nonZero)/((double)total);
        	if(percentage > 0.8) {
        		blackSquares.add(squares.get(i));
        		blackSquaresRect.add(rect);
            	MatOfPoint matOfPoint = new MatOfPoint(new Point(rect.x, rect.y), 
						new Point(rect.x+rect.width, rect.y), 
						new Point(rect.x+rect.width, rect.y+rect.height),
						new Point(rect.x, rect.y+rect.height));
            	Imgproc.drawContours(res, Arrays.asList(matOfPoint), 0, new Scalar(0, 255, 0), 3);
        	}
        }

		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-1-contour.jpg").getAbsolutePath(), res);
		
		if(blackSquaresRect.size() == 0) {
			return;
		}
		
		// Collect all squares for input
		final int ALIGN_TOLERANCE = 10;
		int mostLeftBlackSquareIndex = 0;
		int mostBottomBlackSquareIndex = 0;
		ArrayList<Rect> verticalSquares = new ArrayList<>();
		ArrayList<Rect> horizontalSquares = new ArrayList<>();

		// RESET MAT
		for(int i=0; i<result.rows(); i++) {
			for(int j=0; j<result.cols(); j++) {
				double data = result.get(i, j)[0];
				res.put(i, j, data, data, data);
			}
		}
		
		// Find anchor
		for(int i=1; i<blackSquaresRect.size(); i++) {
			if(getCenter(blackSquaresRect.get(mostLeftBlackSquareIndex)).x > getCenter(blackSquaresRect.get(i)).x) {
				mostLeftBlackSquareIndex = i;
			}
			if(getCenter(blackSquaresRect.get(mostLeftBlackSquareIndex)).y < getCenter(blackSquaresRect.get(i)).y) {
				mostBottomBlackSquareIndex = i;
			}
		}

		// Find squares on vertical most left
		for(int i=0; i<blackSquaresRect.size(); i++) {
			if(Math.abs(getCenter(blackSquaresRect.get(mostLeftBlackSquareIndex)).x - getCenter(blackSquaresRect.get(i)).x) <= ALIGN_TOLERANCE) {
				verticalSquares.add(blackSquaresRect.get(i));
			}
		}
		// Find squares on horizontal most bottom
		for(int i=0; i<blackSquaresRect.size(); i++) {
			if(Math.abs(getCenter(blackSquaresRect.get(mostBottomBlackSquareIndex)).y - getCenter(blackSquaresRect.get(i)).y) <= ALIGN_TOLERANCE) {
				horizontalSquares.add(blackSquaresRect.get(i));
			}
		}
		
		// Draw contour of horizontal and vertical squares
		final int RADIUS = 4;
		for(int i=0; i<verticalSquares.size(); i++) {
			Rect rect = verticalSquares.get(i);
			drawContour(res, rect, new Scalar(0, 0, 255), 3);
        	
        	Point centerOfRect = getCenter(rect);
        	for(int x=(int)centerOfRect.x - RADIUS; x<=(int)centerOfRect.x + RADIUS; x++) {
        		for(int y=(int)centerOfRect.y - RADIUS; y<=(int)centerOfRect.y + RADIUS; y++) {
        			res.put(y, x, 0, 0, 255);
        		}
        	}
		}
		for(int i=0; i<horizontalSquares.size(); i++) {
			Rect rect = horizontalSquares.get(i);
			drawContour(res, rect, new Scalar(255, 255, 0), 3);

        	Point centerOfRect = getCenter(rect);
        	for(int x=(int)centerOfRect.x - RADIUS; x<=(int)centerOfRect.x + RADIUS; x++) {
        		for(int y=(int)centerOfRect.y - RADIUS; y<=(int)centerOfRect.y + RADIUS; y++) {
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
		
		for(int i=0; i<verticalSquares.size(); i++) {
			averageHeight += verticalSquares.get(i).height;
		}
		averageHeight /= verticalSquares.size();
		
		for(int i=0; i<horizontalSquares.size(); i++) {
			averageWidth += horizontalSquares.get(i).width;
		}
		averageWidth /= horizontalSquares.size();

		// ANSWER SHEET TYPE P-40
		final int FIRST_SEC_NUM_COLUMNS = 3;
		final int SECOND_SEC_NUM_COLUMNS = 5;
		final int THIRD_SEC_NUM_COLUMNS = 5;
		final int FIRST_SEC_NUM_ROWS = 10;
		final int SECOND_SEC_NUM_ROWS = 10;

		// Create debug folder
		File folder = new File(file, fileName.substring(0, fileName.lastIndexOf('.')));
		folder.mkdirs();
		
		// First top set of columns
		Point centerStartVerticalPoint = getCenter(verticalSquares.get(0));
		Point centerEndVerticalPoint = getCenter(verticalSquares.get(1));
		Point centerStartHorizontalPoint = getCenter(horizontalSquares.get(0));
		Point centerEndHorizontalPoint = getCenter(horizontalSquares.get(1));
		double vDiff = (centerEndVerticalPoint.y - centerStartVerticalPoint.y) / (double)(FIRST_SEC_NUM_ROWS - 1);
		double hDiff = (centerEndHorizontalPoint.x - centerStartHorizontalPoint.x) / (double)(FIRST_SEC_NUM_COLUMNS - 1);
		double startCenterY, startCenterX;
		startCenterY = centerStartVerticalPoint.y;
		for(int i=0; i<FIRST_SEC_NUM_ROWS; i++, startCenterY += vDiff) {
			startCenterX = centerStartHorizontalPoint.x;
			for(int j=0; j<FIRST_SEC_NUM_COLUMNS; j++, startCenterX += hDiff) {
	        	for(int x=(int)startCenterX - RADIUS; x<=(int)startCenterX + RADIUS; x++) {
	        		for(int y=(int)startCenterY - RADIUS; y<=(int)startCenterY + RADIUS; y++) {
	        			res.put(y, x, 0, 255, 0);
	        		}
	        	}
				drawSquareFromCenter(result, res, (int)Math.round(startCenterX), (int)Math.round(startCenterY), averageWidth, averageHeight, folder, "ExCode-" + String.valueOf(0 + i) + "-" + String.valueOf(1 + j));
			}
		}

		// Second top set of columns
		centerStartVerticalPoint = getCenter(verticalSquares.get(0));
		centerEndVerticalPoint = getCenter(verticalSquares.get(1));
		centerStartHorizontalPoint = getCenter(horizontalSquares.get(2));
		centerEndHorizontalPoint = getCenter(horizontalSquares.get(3));
		vDiff = (centerEndVerticalPoint.y - centerStartVerticalPoint.y) / (double)(FIRST_SEC_NUM_ROWS - 1);
		hDiff = (centerEndHorizontalPoint.x - centerStartHorizontalPoint.x) / (double)(SECOND_SEC_NUM_COLUMNS - 1);
		startCenterY = centerStartVerticalPoint.y;
		for(int i=0; i<FIRST_SEC_NUM_ROWS; i++, startCenterY += vDiff) {
			startCenterX = centerStartHorizontalPoint.x;
			for(int j=0; j<SECOND_SEC_NUM_COLUMNS; j++, startCenterX += hDiff) {
	        	for(int x=(int)startCenterX - RADIUS; x<=(int)startCenterX + RADIUS; x++) {
	        		for(int y=(int)startCenterY - RADIUS; y<=(int)startCenterY + RADIUS; y++) {
	        			res.put(y, x, 0, 255, 0);
	        		}
	        	}
				drawSquareFromCenter(result, res, (int)Math.round(startCenterX), (int)Math.round(startCenterY), averageWidth, averageHeight, folder, "Ans#" + String.valueOf(1 + i) + "-" + (char)('A' + j));
			}
		}

		// Third top set of columns
		centerStartVerticalPoint = getCenter(verticalSquares.get(0));
		centerEndVerticalPoint = getCenter(verticalSquares.get(1));
		centerStartHorizontalPoint = getCenter(horizontalSquares.get(4));
		centerEndHorizontalPoint = getCenter(horizontalSquares.get(5));
		vDiff = (centerEndVerticalPoint.y - centerStartVerticalPoint.y) / (double)(FIRST_SEC_NUM_ROWS - 1);
		hDiff = (centerEndHorizontalPoint.x - centerStartHorizontalPoint.x) / (double)(THIRD_SEC_NUM_COLUMNS - 1);
		startCenterY = centerStartVerticalPoint.y;
		for(int i=0; i<FIRST_SEC_NUM_ROWS; i++, startCenterY += vDiff) {
			startCenterX = centerStartHorizontalPoint.x;
			for(int j=0; j<THIRD_SEC_NUM_COLUMNS; j++, startCenterX += hDiff) {
	        	for(int x=(int)startCenterX - RADIUS; x<=(int)startCenterX + RADIUS; x++) {
	        		for(int y=(int)startCenterY - RADIUS; y<=(int)startCenterY + RADIUS; y++) {
	        			res.put(y, x, 0, 255, 0);
	        		}
	        	}
				drawSquareFromCenter(result, res, (int)Math.round(startCenterX), (int)Math.round(startCenterY), averageWidth, averageHeight, folder, "Ans#" + String.valueOf(11 + i) + "-" + (char)('A' + j));
			}
		}

		// First top set of columns
		centerStartVerticalPoint = getCenter(verticalSquares.get(2));
		centerEndVerticalPoint = getCenter(verticalSquares.get(3));
		centerStartHorizontalPoint = getCenter(horizontalSquares.get(0));
		centerEndHorizontalPoint = getCenter(horizontalSquares.get(1));
		vDiff = (centerEndVerticalPoint.y - centerStartVerticalPoint.y) / (double)(SECOND_SEC_NUM_ROWS - 1);
		hDiff = (centerEndHorizontalPoint.x - centerStartHorizontalPoint.x) / (double)(FIRST_SEC_NUM_COLUMNS - 1);
		startCenterY = centerStartVerticalPoint.y;
		for(int i=0; i<SECOND_SEC_NUM_ROWS; i++, startCenterY += vDiff) {
			startCenterX = centerStartHorizontalPoint.x;
			for(int j=0; j<FIRST_SEC_NUM_COLUMNS; j++, startCenterX += hDiff) {
	        	for(int x=(int)startCenterX - RADIUS; x<=(int)startCenterX + RADIUS; x++) {
	        		for(int y=(int)startCenterY - RADIUS; y<=(int)startCenterY + RADIUS; y++) {
	        			res.put(y, x, 0, 255, 0);
	        		}
	        	}
				drawSquareFromCenter(result, res, (int)Math.round(startCenterX), (int)Math.round(startCenterY), averageWidth, averageHeight, folder, "MCode-" + String.valueOf(0 + i) + "-" + String.valueOf(1 + j));
			}
		}

		// Second top set of columns
		centerStartVerticalPoint = getCenter(verticalSquares.get(2));
		centerEndVerticalPoint = getCenter(verticalSquares.get(3));
		centerStartHorizontalPoint = getCenter(horizontalSquares.get(2));
		centerEndHorizontalPoint = getCenter(horizontalSquares.get(3));
		vDiff = (centerEndVerticalPoint.y - centerStartVerticalPoint.y) / (double)(SECOND_SEC_NUM_ROWS - 1);
		hDiff = (centerEndHorizontalPoint.x - centerStartHorizontalPoint.x) / (double)(SECOND_SEC_NUM_COLUMNS - 1);
		startCenterY = centerStartVerticalPoint.y;
		for(int i=0; i<SECOND_SEC_NUM_ROWS; i++, startCenterY += vDiff) {
			startCenterX = centerStartHorizontalPoint.x;
			for(int j=0; j<SECOND_SEC_NUM_COLUMNS; j++, startCenterX += hDiff) {
	        	for(int x=(int)startCenterX - RADIUS; x<=(int)startCenterX + RADIUS; x++) {
	        		for(int y=(int)startCenterY - RADIUS; y<=(int)startCenterY + RADIUS; y++) {
	        			res.put(y, x, 0, 255, 0);
	        		}
	        	}
				drawSquareFromCenter(result, res, (int)Math.round(startCenterX), (int)Math.round(startCenterY), averageWidth, averageHeight, folder, "Ans#" + String.valueOf(21 + i) + "-" + (char)('A' + j));
			}
		}

		// Third bottom set of columns
		centerStartVerticalPoint = getCenter(verticalSquares.get(2));
		centerEndVerticalPoint = getCenter(verticalSquares.get(3));
		centerStartHorizontalPoint = getCenter(horizontalSquares.get(4));
		centerEndHorizontalPoint = getCenter(horizontalSquares.get(5));
		vDiff = (centerEndVerticalPoint.y - centerStartVerticalPoint.y) / (double)(SECOND_SEC_NUM_ROWS - 1);
		hDiff = (centerEndHorizontalPoint.x - centerStartHorizontalPoint.x) / (double)(THIRD_SEC_NUM_COLUMNS - 1);
		startCenterY = centerStartVerticalPoint.y;
		for(int i=0; i<SECOND_SEC_NUM_ROWS; i++, startCenterY += vDiff) {
			startCenterX = centerStartHorizontalPoint.x;
			for(int j=0; j<THIRD_SEC_NUM_COLUMNS; j++, startCenterX += hDiff) {
	        	for(int x=(int)startCenterX - RADIUS; x<=(int)startCenterX + RADIUS; x++) {
	        		for(int y=(int)startCenterY - RADIUS; y<=(int)startCenterY + RADIUS; y++) {
	        			res.put(y, x, 0, 255, 0);
	        		}
	        	}
				drawSquareFromCenter(result, res, (int)Math.round(startCenterX), (int)Math.round(startCenterY), averageWidth, averageHeight, folder, "Ans#" + String.valueOf(31 + i) + "-" + (char)('A' + j));
			}
		}

		Imgcodecs.imwrite(new File(file, fileName.substring(0, fileName.lastIndexOf('.')) + "-2-class-square.jpg").getAbsolutePath(), res);
		// End draw
	}
	
	public static void findSquares(Mat image, ArrayList<MatOfPoint> squares)
	{
	    // blur will enhance edge detection
	    Mat blurredImage = image.clone();
	    Imgproc.medianBlur(image, blurredImage, 9);

	    Mat gray0 = new Mat(blurredImage.size(), CvType.CV_8U);
	    Mat gray = new Mat();
	    ArrayList<MatOfPoint> contours = new ArrayList<>();

	    // find squares in every color plane of the image
	    for (int c = 0; c < 3; c++)
	    {
	        List<Mat> src, dst;
	        src = new ArrayList<Mat>();
	        src.add(blurredImage);
	        dst = new ArrayList<Mat>();
	        dst.add(gray0);
	        MatOfInt ch = new MatOfInt(c, 0);
	        Core.mixChannels(src, dst, ch);

	        // try several threshold levels
	        final int threshold_level = 2;
	        for (int l = 0; l < threshold_level; l++)
	        {
	            // Use Canny instead of zero threshold level!
	            // Canny helps to catch squares with gradient shading
	            if (l == 0)
	            {
	                Imgproc.Canny(gray0, gray, 10, 20, 3, false); // 

	                // Dilate helps to remove potential holes between edge segments
	                Imgproc.dilate(gray, gray, new Mat(), new Point(-1,-1), 1);
	            }
	            else
	            {
	            	Core.compare(gray0, new Scalar((l+1.0) * 255 / threshold_level), gray, Core.CMP_GE);
	            }

	            // Find contours and store them in a list
	            Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

	            // Test contours
	            MatOfPoint2f approx = new MatOfPoint2f();
	            for (int i = 0; i < contours.size(); i++)
	            {
	                    // approximate contour with accuracy proportional
	                    // to the contour perimeter
	                    Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), 
	                    		approx, 
	                    		Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true)*0.02, 
	                    		true);

	                    // Note: absolute value of an area is used because
	                    // area may be positive or negative - in accordance with the
	                    // contour orientation
	                    if (approx.toArray().length == 4 &&
	                            Math.abs(Imgproc.contourArea(approx)) > 1000 &&
	                            Imgproc.isContourConvex(new MatOfPoint(approx.toArray())))
	                    {
	                    	double maxCosine = 0;

                            Point[] approxPoints = approx.toArray();
                            for (int j = 2; j < 5; j++)
                            {
                            	double cosine = Math.abs(angle(approxPoints[j%4], approxPoints[j-2], approxPoints[j-1]));
                            	maxCosine = Math.max(maxCosine, cosine);
                            }

                            if (maxCosine < 0.3)
                                    squares.add(new MatOfPoint(approx.toArray()));
	                    }
	            }
	        }
	    }
	}
	
	private static double angle(Point pt1, Point pt2, Point pt0)
	{
	    double dx1 = pt1.x - pt0.x;
	    double dy1 = pt1.y - pt0.y;
	    double dx2 = pt2.x - pt0.x;
	    double dy2 = pt2.y - pt0.y;
	    return (dx1 * dx2 + dy1 * dy2) / 
	    		Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);
	}
	
	private static Point getCenter(Rect rect) {
		if(rect == null) return null;
		Point center = new Point();
		center.x = rect.x + rect.width / 2;
		center.y = rect.y + rect.height / 2;
		
		return center;
	}
	
	private static Point findFirstBlackPixel(Mat mat, Point pStart, int xIter, int yIter) {
		Point p = new Point(pStart.x, pStart.y);
		while(p.x < mat.width() && p.x >= 0 && p.y < mat.height() && p.y >= 0 && mat.get((int)p.y, (int)p.x)[0] == 255) {
			p.x += xIter;
			p.y += yIter;
		}
		return p;
	}
	
	private static void drawContour(Mat mat, Rect rect, Scalar color, int thickness) {
    	MatOfPoint matOfPoint = new MatOfPoint(new Point(rect.x, rect.y), 
				new Point(rect.x+rect.width, rect.y), 
				new Point(rect.x+rect.width, rect.y+rect.height),
				new Point(rect.x, rect.y+rect.height));
    	Imgproc.drawContours(mat, Arrays.asList(matOfPoint), 0, color, thickness);
	}
	
	private static void drawSquareFromCenter(Mat src, Mat drawOn, int x, int y, int predictedWidth, int predictedHeight, File directory, String filename) {
    	Point currentCenter = new Point(x, y);
    	/*
    	 * Option 1 (likely not used)
    	 */
    	Point top, bottom, left, right;
    	top = findFirstBlackPixel(src, currentCenter, 0, -1);
    	bottom = findFirstBlackPixel(src, currentCenter, 0, 1);
    	left = findFirstBlackPixel(src, currentCenter, -1, 0);
    	right = findFirstBlackPixel(src, currentCenter, 1, 0);
    	Rect currentRect = new Rect(new Point(left.x, top.y), 
    								new Size(right.x - left.x, bottom.y - top.y));
    	drawContour(drawOn, currentRect, new Scalar(0, 0, 255), 2);
    	int shrinkPixelX = currentRect.width / 10;
    	int shrinkPixelY = currentRect.height / 10;
    	currentRect.x += shrinkPixelX; currentRect.y += shrinkPixelY;
    	currentRect.width -= 2 * shrinkPixelX; currentRect.height -= 2 * shrinkPixelY;
    	drawContour(drawOn, currentRect, new Scalar(0, 255, 0), 2);
    	
    	/*
    	 * Option 2 (concern accuracy)
    	 */
    	int width, height;
    	width = predictedWidth * 11 / 5;
    	height = predictedHeight * 11 / 5;
    	currentRect = new Rect(new Point(currentCenter.x - width / 2, currentCenter.y - height / 2),
    								new Size(width, height));
    	Mat square = (new Mat(drawOn, currentRect)).clone();
    	Imgcodecs.imwrite(new File(directory, filename + ".jpg").getAbsolutePath(), square);
    	
    	// TRY FLOODFILL
    	//floodFill(square);
    	//Imgcodecs.imwrite(new File(directory, filename + "-1.jpg").getAbsolutePath(), square);
    	// TRY WITH MOST FIT TEMPLATE
    	Point rectStartPoint = findMostFitRect(square, predictedWidth, predictedHeight, (int) Math.round(PROCESSED_ANSWER_SQUARE_BORDER * PROCESSED_RATIO));
    	//System.out.println(filename + ": " + rectStartPoint);
    	currentRect = new Rect(rectStartPoint, new Size(predictedWidth, predictedHeight));
    	drawContour(square, currentRect, new Scalar(255, 0, 255), 2);
    	Imgcodecs.imwrite(new File(directory, filename + "-2.jpg").getAbsolutePath(), square);
//    	drawContour(drawOn, currentRect, new Scalar(255, 0, 255), 2);    	
	}
	
	private static Point findMostFitRect(Mat mat, int width, int height, int insideBorderThickness) {
		int maxCol = mat.cols() - width + 1, maxRow = mat.rows() - height + 1;
		if(maxCol <= 0 || maxRow <= 0) return new Point(0, 0);
		int x, y, maxBlackPixel, xStart, yStart, outsideRectBlackPixel, insideRectBlackPixel, currBlackPixel;
		int[][] blackPixel = new int[mat.rows() + 1][mat.cols() + 1];
		for(y=0; y<mat.rows(); y++) {
			blackPixel[y][0] = 0;
		}
		for(x=0; x<mat.cols(); x++) {
			blackPixel[0][x] = 0;
		}
		// Integral image (OpenCV)
		for(y=1; y<=mat.rows(); y++) {
			for(x=1; x<=mat.cols(); x++) {
				blackPixel[y][x] = blackPixel[y-1][x] + blackPixel[y][x-1] - blackPixel[y-1][x-1];
				if(mat.get(y-1, x-1)[0] == 0) { // if the color is black
					blackPixel[y][x]++;
				}
			}
		}
		
		xStart = yStart = -1;
		maxBlackPixel = -1;
		for(y=1; y<=maxRow; y++) {
			for(x=1; x<=maxCol; x++) {
				outsideRectBlackPixel = blackPixel[y + height - 1][x + width - 1] - 
										blackPixel[y + height - 1][x - 1] - 
										blackPixel[y - 1][x + width - 1] + 
										blackPixel[y - 1][x - 1];
				
				insideRectBlackPixel =  blackPixel[y + height - insideBorderThickness - 1][x + width - insideBorderThickness - 1] - 
										blackPixel[y + height - insideBorderThickness - 1][x + insideBorderThickness - 1] - 
										blackPixel[y + insideBorderThickness - 1][x + width - insideBorderThickness - 1] + 
										blackPixel[y + insideBorderThickness - 1][x + insideBorderThickness - 1];
				
				currBlackPixel = outsideRectBlackPixel - insideRectBlackPixel;
				if(maxBlackPixel < currBlackPixel) {
					maxBlackPixel = currBlackPixel;
					xStart = x;
					yStart = y;
				}
			}
		}
		
		return new Point(xStart - 1, yStart - 1);
	}
	
	private static final byte WHITE = 1;
	private static final byte BLACK = 0;
	private static final byte OUTER_BLACK = 2;

	private static void floodFill(Mat mat) {
		byte[][] img = new byte[mat.height()][mat.width()];
		for(int i=0; i<img.length; i++) Arrays.fill(img[i], WHITE);
		
		for(int i=0; i<mat.width(); i++) {
			for(int j=0; j<mat.height(); j++) {
				if(mat.get(j, i)[0] == BLACK && mat.get(j, i)[1] == BLACK && mat.get(j, i)[2] == BLACK) {
					img[j][i] = BLACK;
				}
			}
		}
		
		final int THICKNESS = 3;
		for(int i=0; i<mat.width(); i++) {
			for(int j=0; j<THICKNESS; j++) {
				fillOuterBlack(img, i+j, 0);
				fillOuterBlack(img, i-j, img.length - 1);				
			}
		}

		for(int i=0; i<mat.height(); i++) {
			for(int j=0; j<THICKNESS; j++) {
				fillOuterBlack(img, 0, i+j);
				fillOuterBlack(img, img[0].length - 1, i-j);
			}
		}

		for(int i=0; i<mat.width(); i++) {
			for(int j=0; j<mat.height(); j++) {
				if(img[j][i] == OUTER_BLACK) {
					fillOuterBlackFromWhite(img, i-1, j);
					fillOuterBlackFromWhite(img, i, j-1);
					fillOuterBlackFromWhite(img, i+1, j);
					fillOuterBlackFromWhite(img, i, j+1);
				}
			}
		}
		
    	for(int i=0; i<img.length; i++) {
    		for(int j=0; j<img[i].length; j++) {
    			if(img[i][j] == BLACK || img[i][j] == OUTER_BLACK) 
    				mat.put(i, j, 0, 0, 0);
    		}
    	}
	}
	
	private static void fillOuterBlack(byte[][] img, int x, int y) {
		LinkedList<Point> pPoints = new LinkedList<>();
		Point curr;
		pPoints.add(new Point(x, y));
		
		int height = img.length;
		int width = img[0].length;
		
		while(!pPoints.isEmpty()) {
			curr = pPoints.pollFirst();
			x = (int) curr.x;
			y = (int) curr.y;
			if(x < 0 || y < 0 || x >= width || y >= height) continue;
			if(img[y][x] == BLACK) {
				img[y][x] = OUTER_BLACK;
				pPoints.add(new Point(x-1, y));
				pPoints.add(new Point(x, y-1));
				pPoints.add(new Point(x+1, y));
				pPoints.add(new Point(x, y+1));
			}
		}
	}

	private static void fillOuterBlackFromWhite(byte[][] img, int x, int y) {
		LinkedList<Point> pPoints = new LinkedList<>();
		Point curr;
		pPoints.add(new Point(x, y));
		
		int height = img.length;
		int width = img[0].length;
		
		while(!pPoints.isEmpty()) {
			curr = pPoints.pollFirst();
			x = (int) curr.x;
			y = (int) curr.y;
			if(x < 0 || y < 0 || x >= width || y >= height) continue;
			if(img[y][x] == WHITE) {
				img[y][x] = OUTER_BLACK;
				pPoints.add(new Point(x-1, y));
				pPoints.add(new Point(x, y-1));
				pPoints.add(new Point(x+1, y));
				pPoints.add(new Point(x, y+1));
			}
		}
	}
}
