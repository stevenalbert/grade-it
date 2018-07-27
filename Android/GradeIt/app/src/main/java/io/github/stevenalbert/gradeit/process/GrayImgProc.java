package io.github.stevenalbert.gradeit.process;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A class used as a collection of image processing algorithm specifically for
 * grayscale image. The algorithms are implemented using functions from OpenCv
 * 3.0 library
 * 
 * @author DATACLIM-PIKU2
 *
 */
public class GrayImgProc {
    /**
     * Comparator object to compare two MatOfPoint objects based on their
     */
    public static final Comparator<Rect> comparatorMatOfPoint = new Comparator<Rect>() {
        public int compare(Rect o1, Rect o2) {
            if (o1.y < o2.y)
                return -1;
            else if (o1.y > o2.y)
                return +1;
            else {
                return o1.x - o2.x;
            }
        }
    };

    /**
     * write the content of in to a file specified by fileName argument
     * 
     * @param in
     *            the Mat object to be written to a file
     * @param fullPathName
     *            the file's name, complete with its path and extension, where the
     *            values will be written
     */
    public static void matToTxt(Mat in, String fullPathName) {
        // getting the dimension of the array (which is the dimension of the image)
        int rows = in.rows();
        int cols = in.cols();

        // check whether the matrix' value is unsigned or not
        boolean isUnsigned = false;
        if (in.depth() == CvType.depth(CvType.CV_8U) || in.depth() == CvType.depth(CvType.CV_16U))
            isUnsigned = true;

        // create an array with corresponding data type with the data in the matrix
        if (in.depth() == CvType.depth(CvType.CV_8U) || in.depth() == CvType.depth(CvType.CV_8S)) {
            byte[] data = new byte[(int) in.total() * in.channels()];
            in.get(0, 0, data);
            // writing to text (separated by space)
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathName));
                for (int i = 0; i < rows; i++) {
                    for (int ii = 0; ii < cols; ii++) {
                        for (int iii = 0; iii < in.channels(); iii++) {
                            if (isUnsigned && data[i * cols + ii + iii] < 0)
                                writer.write((int) (256 + data[i * cols + ii + iii]) + " ");
                            else
                                writer.write(data[i * cols + ii + iii] + " ");
                        }
                        writer.write("\t");
                    }
                    writer.newLine();
                }
                writer.close();
                writer = null;
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            data = null;
        } else if (in.depth() == CvType.depth(CvType.CV_16U) || in.depth() == CvType.depth(CvType.CV_16S)) {
            short[] data = new short[(int) in.total() * in.channels()];
            in.get(0, 0, data);
            // writing to text (separated by space)
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathName));
                for (int i = 0; i < rows; i++) {
                    for (int ii = 0; ii < cols; ii++) {
                        for (int iii = 0; iii < in.channels(); iii++) {
                            if (isUnsigned && data[i * cols + ii + iii] < 0)
                                writer.write(65536 + data[i * cols + ii + iii] + " ");
                            else
                                writer.write(data[i * cols + ii + iii] + " ");
                        }
                        writer.write("\t");
                    }
                    writer.newLine();
                }
                writer.close();
                writer = null;
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            data = null;
        } else if (in.depth() == CvType.depth(CvType.CV_32S)) {
            int[] data = new int[(int) in.total() * in.channels()];
            in.get(0, 0, data);
            // writing to text (separated by space)
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathName));
                for (int i = 0; i < rows; i++) {
                    for (int ii = 0; ii < cols; ii++) {
                        for (int iii = 0; iii < in.channels(); iii++)
                            writer.write(data[i * cols + ii + iii] + " ");
                        writer.write("\t");
                    }
                    writer.newLine();
                }
                writer.close();
                writer = null;
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            data = null;
        } else if (in.depth() == CvType.depth(CvType.CV_32F)) {
            float[] data = new float[(int) in.total() * in.channels()];
            in.get(0, 0, data);
            // writing to text (separated by space)
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathName));
                for (int i = 0; i < rows; i++) {
                    for (int ii = 0; ii < cols; ii++) {
                        for (int iii = 0; iii < in.channels(); iii++)
                            writer.write(data[i * cols + ii + iii] + " ");
                        writer.write("\t");
                    }
                    writer.newLine();
                }
                writer.close();
                writer = null;
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            data = null;
        } else if (in.depth() == CvType.depth(CvType.CV_64F)) {
            double[] data = new double[(int) in.total() * in.channels()];
            in.get(0, 0, data);
            // writing to text (separated by space)
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fullPathName));
                for (int i = 0; i < rows; i++) {
                    for (int ii = 0; ii < cols; ii++) {
                        for (int iii = 0; iii < in.channels(); iii++)
                            writer.write(data[i * cols + ii + iii] + " ");
                        writer.write("\t");
                    }
                    writer.newLine();
                }
                writer.close();
                writer = null;
            } catch (IOException e) {
                System.err.println("Error: " + e.getMessage());
            }
            data = null;
        } else
            return;
    }

    /**
     * normalize the dimension of the given binary image to the given dimension
     * (using moment normalization) with new aspect ratio = cubic root of the
     * original aspect ratio. The given image must have black background with white
     * pixels indicating the object inside.
     * 
     * @param in
     *            the binary image whose size is to be normalized, with black as
     *            background and white as object
     * @param newRows
     *            the number of rows of the normalized binary image
     * @param newCols
     *            the number of columns of the normalized binary image
     * @return a new binary image which is the result of normalizing the given
     *         binary image to the given dimension
     */
    public static Mat normalizationF9(Mat in, int newRows, int newCols) {
        float r1, r2;
        int rowsOfInterest, colsOfInterest;
        int startingNewRowIndex, startingNewColIndex;

        // get the input values
        Mat tempIn = new Mat(in.rows(), in.cols(), in.type());
        in.copyTo(tempIn);

        // calculate the moment of the original image
        Moments moments = Imgproc.moments(tempIn, true);
        double centerXOri = moments.m10 / moments.m00;
        double centerYOri = moments.m01 / moments.m00;
        double miu20 = Math.sqrt(moments.mu20 / moments.m00);
        double miu02 = Math.sqrt(moments.mu02 / moments.m00);

        // expand/trim the dimension of the original image using moment (the start index
        // is inclusive while the end is exclusive)
        int startingOriRowIndex = (int) Math.round(centerYOri - 2 * miu02),
                startingOriColIndex = (int) Math.round(centerXOri - 2 * miu20);
        int endOriRowIndex = (int) Math.round(centerYOri + 2 * miu02),
                endOriColIndex = (int) Math.round(centerXOri + 2 * miu20);
        // expand
        int expandStartRow = (startingOriRowIndex < 0) ? -startingOriRowIndex : 0;
        int expandStartCol = (startingOriColIndex < 0) ? -startingOriColIndex : 0;
        int expandEndRow = (endOriRowIndex > in.rows()) ? (endOriRowIndex - in.rows()) : 0;
        int expandEndCol = (endOriColIndex > in.cols()) ? (endOriColIndex - in.cols()) : 0;
        tempIn = new Mat(in.rows() + expandStartRow + expandEndRow, in.cols() + expandStartCol + expandEndCol,
                CvType.CV_8UC1, new Scalar(0));
        Core.copyMakeBorder(in, tempIn, expandStartRow, expandEndRow, expandStartCol, expandEndCol,
                Core.BORDER_ISOLATED, new Scalar(0));
        in = tempIn;
        tempIn = null;

        // trim (be cautious when the leftmost / top index has been expanded, it will
        // change the rightmost / bottom index to be trimmed)
        expandStartRow = (startingOriRowIndex > 0) ? startingOriRowIndex : 0;
        expandStartCol = (startingOriColIndex > 0) ? startingOriColIndex : 0;
        // check whether expansions has occurred for the starting index
        if (startingOriRowIndex < 0)
            endOriRowIndex -= startingOriRowIndex;
        if (startingOriColIndex < 0)
            endOriColIndex -= startingOriColIndex;
        expandEndRow = (endOriRowIndex < in.rows()) ? endOriRowIndex : in.rows();
        expandEndCol = (endOriColIndex < in.cols()) ? endOriColIndex : in.cols();
        in = in.submat(expandStartRow, expandEndRow, expandStartCol, expandEndCol);

        // adjust the center x and y of the original image with the new dimension
        centerXOri = centerXOri - startingOriColIndex;
        centerYOri = centerYOri - startingOriRowIndex;

        // Calculate the new dimension for the output matrix
        if (in.rows() < in.cols()) {
            r1 = (float) in.rows() / in.cols();
            r2 = (float) Math.cbrt(r1);
            colsOfInterest = newCols;
            startingNewColIndex = 0;
            rowsOfInterest = (int) Math.round(r2 * colsOfInterest);
            // startingNewRowIndex = (int)Math.floor((newRows - rowsOfInterest)/2);
            startingNewRowIndex = (int) Math.round((float) (newRows - rowsOfInterest) / 2);
        } else {
            r1 = (float) in.cols() / in.rows();
            r2 = (float) Math.cbrt(r1);
            rowsOfInterest = newRows;
            startingNewRowIndex = 0;
            colsOfInterest = (int) Math.round(r2 * rowsOfInterest);
            // startingNewColIndex = (int)Math.floor((newCols - colsOfInterest)/2);
            startingNewColIndex = (int) Math.round((float) (newCols - colsOfInterest) / 2);
        }
        // calculate the center of the normalized image
        int centerXNorm = colsOfInterest / 2, centerYNorm = rowsOfInterest / 2;

        // Calculate the transformation
        float alpha, betha;
        alpha = (float) colsOfInterest / in.cols();
        betha = (float) rowsOfInterest / in.rows();

        // Normalize by discretization
        Mat outOfInterest = new Mat(rowsOfInterest, colsOfInterest, CvType.CV_8UC1, new Scalar(0));
        byte[] outVals = new byte[(int) outOfInterest.total()];
        int[] whitePixels = new int[(int) outOfInterest.total()];
        int[] totalPixels = new int[(int) outOfInterest.total()];
        Arrays.fill(whitePixels, 0);
        Arrays.fill(totalPixels, 0);

        // for all pixels
        byte[] inVals = new byte[(int) in.total()];
        in.get(0, 0, inVals);

        int currentNewRowIndex, currentNewColIndex, nextNewRowIndex, nextNewColIndex;
        // scan through the original image
        for (int i = 0; i < in.rows(); i++) {
            // indexing for the normalized image
            currentNewRowIndex = (int) Math.round((i - centerYOri) * betha + centerYNorm);
            if (currentNewRowIndex < 0)
                currentNewRowIndex = 0;
            else if (currentNewRowIndex >= outOfInterest.rows())
                currentNewRowIndex = outOfInterest.rows() - 1;

            nextNewRowIndex = (int) Math.round((i + 1 - centerYOri) * betha + centerYNorm);
            if (nextNewRowIndex < 0)
                nextNewRowIndex = 0;
            else if (nextNewRowIndex >= outOfInterest.rows())
                nextNewRowIndex = outOfInterest.rows() - 1;

            for (int ii = 0; ii < in.cols(); ii++) {
                // indexing for the normalized image
                currentNewColIndex = (int) Math.round((ii - centerXOri) * alpha + centerXNorm);
                if (currentNewColIndex < 0)
                    currentNewColIndex = 0;
                else if (currentNewColIndex >= outOfInterest.cols())
                    currentNewColIndex = outOfInterest.cols() - 1;

                nextNewColIndex = (int) Math.round((ii + 1 - centerXOri) * alpha + centerXNorm);
                if (nextNewColIndex < 0)
                    nextNewColIndex = 0;
                else if (nextNewColIndex >= outOfInterest.cols())
                    nextNewColIndex = outOfInterest.cols() - 1;

                // current color
                int currVal = (inVals[i * in.cols() + ii] == 0 ? 0 : 1);
                // for each pixel between the indices, update them with the value at (i,ii)
                for (int iii = currentNewRowIndex; iii <= nextNewRowIndex; iii++) {
                    for (int iiii = currentNewColIndex; iiii <= nextNewColIndex; iiii++) {
                        // add current color to output matrix section
                        whitePixels[iii * outOfInterest.cols() + iiii] += currVal;
                        totalPixels[iii * outOfInterest.cols() + iiii]++;
                    }
                } // end of for (updating normalized image)
            }
        } // end of for (scanning original image's)

        // calculate the color in outVals based on totalPixels
        for (int i = 0; i < outVals.length; i++) {
            if (whitePixels[i] + whitePixels[i] < totalPixels[i])
                outVals[i] = 0;
            else
                outVals[i] = -1;
        }

        // copy the region of interest to the center of the output matrix
        Mat out = new Mat(newRows, newCols, CvType.CV_8UC1, new Scalar(0));
        outOfInterest.put(0, 0, outVals);
        outOfInterest.copyTo(out.submat(startingNewRowIndex, startingNewRowIndex + outOfInterest.rows(),
                startingNewColIndex, startingNewColIndex + outOfInterest.cols()));
        return out;
    }

    /**
     * get the horizontal projection (the number of black pixels in each row) of the
     * given binary image
     * 
     * @param in
     *            the binary image with total number of rows < 32767
     * @return a 1D array (rows x 1) of type integer containing horizontal
     *         projection (for black pixels) of the given binary image
     */
    public static Mat horizontalProjection(Mat in) {
        // prepare the output
        Mat out = Mat.zeros(in.rows(), 1, CvType.CV_32SC1);
        Mat tempIn = new Mat(in.rows(), in.cols(), in.type());
        in.copyTo(tempIn);

        // dilate the black pixel before thresholding
        /*
         * Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new
         * Size(5,5)); Imgproc.erode(in, tempIn, element);
         */

        // convert the black pixel to 1 while the white pixel to 0
        Imgproc.threshold(in, tempIn, 150, 1, Imgproc.THRESH_BINARY_INV);

        // projection
        Core.reduce(tempIn, out, 1, Core.REDUCE_SUM, CvType.CV_32SC(1));

        tempIn = null;
        return out;
    }

    /**
     * get the vertical projection (the number of black pixels in each col) of the
     * given binary image
     * 
     * @param in
     *            the binary image with total number of columns < 32767
     * @return a 1D array (1 x cols) of type integer containing vertical projection
     *         (for black pixels) of the given binary image
     */
    public static Mat verticalProjection(Mat in) {
        // prepare the output
        Mat out = Mat.zeros(1, in.cols(), CvType.CV_32SC1);
        Mat tempIn = new Mat(in.rows(), in.cols(), in.type());
        in.copyTo(tempIn);

        // dilate the black pixel before thresholding
        /*
         * Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new
         * Size(5,5)); Imgproc.erode(in, tempIn, element);
         */

        // convert the black pixel to 1 while the white pixel to 0
        Imgproc.threshold(tempIn, tempIn, 150, 1, Imgproc.THRESH_BINARY_INV);

        // projection
        Core.reduce(tempIn, out, 0, Core.REDUCE_SUM, CvType.CV_32SC(1));

        tempIn = null;
        return out;
    }
}
