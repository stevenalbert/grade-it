package process;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

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
        };
    };

    /**
     * partition the given image into overlapping regions (half the width and
     * height) with the given width and height
     * 
     * @param in
     *            the image to be partitioned into overlapping regions
     * @param regionWidth
     *            the width of each region (must be an even number)
     * @param regionHeight
     *            the height of each region (must be an even number)
     * @return an array of image which is the result of the partition process,
     *         sorted from the top left region to the bottom right region by rows
     */
    public static Mat[] partitionIntoOverlapRegions(Mat in, int regionWidth, int regionHeight) {
        // if not even
        if (regionWidth % 2 != 0 || regionHeight % 2 != 0)
            return null;

        // if cannot partitioned evenly with the given region's size
        if (in.cols() % (regionWidth / 2) != 0 || in.rows() % (regionHeight / 2) != 0)
            return null;

        // determine the total regions
        int ttlRegionsCol = (in.cols() / (regionWidth / 2)) - 1;
        int ttlRegionsRow = (in.rows() / (regionHeight / 2)) - 1;
        Mat[] regions = new Mat[ttlRegionsCol * ttlRegionsRow];

        // crop the regions
        for (int i = 0; i < ttlRegionsRow; i++) {
            for (int ii = 0; ii < ttlRegionsCol; ii++) {
                regions[i * ttlRegionsCol + ii] = new Mat(regionHeight, regionWidth, in.type());
                // in.submat(new Rect(ii*regionWidth/2, i*regionHeight/2, regionWidth,
                // regionHeight)).copyTo(regions[i*ttlRegionsCol + ii]);
                in.submat(i * regionHeight / 2, i * regionHeight / 2 + regionHeight, ii * regionWidth / 2,
                        ii * regionWidth / 2 + regionWidth).copyTo(regions[i * ttlRegionsCol + ii]);
            }
        }

        return regions;
    }

    /**
     * partition the given image into non-overlapping regions with the given width
     * and height
     * 
     * @param in
     *            the image to be partitioned into non-overlapping regions
     * @param regionWidth
     *            the width of each region (must divide the image without remains)
     * @param regionHeight
     *            the height of each region (must divide the image without remains)
     * @return an array of image which is the result of the partition process,
     *         sorted from the top left region to the bottom right region by rows
     */
    public static Mat[] partitionIntoRegions(Mat in, int regionWidth, int regionHeight) {
        // if cannot partitioned evenly with the given region's size
        if (in.cols() % regionWidth != 0 || in.rows() % regionHeight != 0)
            return null;

        // determine the total regions
        int ttlRegionsCol = (in.cols() / regionWidth);
        int ttlRegionsRow = (in.rows() / regionHeight);
        Mat[] regions = new Mat[ttlRegionsCol * ttlRegionsRow];

        // crop the regions
        for (int i = 0; i < ttlRegionsRow; i++) {
            for (int ii = 0; ii < ttlRegionsCol; ii++) {
                regions[i * ttlRegionsCol + ii] = new Mat(regionHeight, regionWidth, in.type());
                in.submat(i * regionHeight, (i + 1) * regionHeight, ii * regionWidth, (ii + 1) * regionWidth)
                        .copyTo(regions[i * ttlRegionsCol + ii]);
            }
        }

        return regions;
    }

    /**
     * convert image stored in Mat type with 8 bit depth from OpenCV to
     * BufferedImage
     * 
     * @param in
     *            the Mat object containing the image to be converted (must have 8
     *            bit depth)
     * @return the image in BufferedImage type if successful (valid number of
     *         channels: 1 / 3), null otherwise
     */
    public static BufferedImage mat2BufferedImage(Mat in) {
        if (in.depth() != CvType.depth(CvType.CV_8S) && in.depth() != CvType.depth(CvType.CV_8U))
            return null;

        int cols = in.cols();
        int rows = in.rows();
        int type;

        byte[] data = new byte[rows * cols * (int) in.elemSize()];
        in.get(0, 0, data);

        switch (in.channels()) {
            case 1:
                type = BufferedImage.TYPE_BYTE_GRAY;
                break;
            case 3:
                type = BufferedImage.TYPE_3BYTE_BGR;
                break;
            default:
                return null;
        }

        BufferedImage out = new BufferedImage(cols, rows, type);
        out.getRaster().setDataElements(0, 0, cols, rows, data);
        return out;
    }

    /**
     * convert image stored in BufferedImage to Mat with 8 bit depth(from OpenCV)
     * 
     * @param in
     *            the BufferedImage object containing the image to be converted
     * @return a Mat object containing the image (with 8 bit depth)
     */
    public static Mat bufferedImage2Mat(BufferedImage in) {
        Mat out;
        int cols = in.getWidth(), rows = in.getHeight();
        byte[] data;
        int r, g, b;

        // RGB Image
        if (in.getType() == BufferedImage.TYPE_INT_RGB) {
            out = new Mat(rows, cols, CvType.CV_8UC3);
            data = new byte[rows * cols * (int) out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, cols, rows, null, 0, cols);
            for (int i = 0; i < dataBuff.length; i++) {
                data[i * 3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
                data[i * 3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                data[i * 3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
            }
        }
        // Grayscale Image
        else {
            out = new Mat(rows, cols, CvType.CV_8UC1);
            data = new byte[cols * rows * (int) out.elemSize()];
            int[] dataBuff = in.getRGB(0, 0, cols, rows, null, 0, cols);
            for (int i = 0; i < dataBuff.length; i++) {
                r = (byte) ((dataBuff[i] >> 16) & 0xFF);
                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
                b = (byte) ((dataBuff[i] >> 0) & 0xFF);
                data[i] = (byte) ((0.21 * r) + (0.71 * g) + (0.07 * b)); // luminosity
            }
        }
        out.put(0, 0, data);
        return out;
    }

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
     * apply a thresholding algorithm by Sauvola to the given image in Mat format
     * 
     * @param in
     *            a Mat object containing pixel values of a grayscale image
     * @param winSize
     *            the size of the window (local neighborhood used in Sauvola
     *            algorithm
     * @return a Mat object containing pixel values of the given image in binary (0
     *         / 255)
     */
    public static Mat thresholdSauvola(Mat in, int winSize) {
        if (in.channels() != 1)
            return null;

        // find the integral image (sum and sum of square)
        Mat integral = new Mat(in.rows(), in.cols(), CvType.CV_32SC1);
        Mat integral2 = new Mat(in.rows(), in.cols(), CvType.CV_64FC1);
        Imgproc.integral2(in, integral, integral2);
        int[] tempIntegral = new int[(int) (integral.total() * integral.channels())];
        double[] tempIntegral2 = new double[(int) (integral2.total() * integral2.channels())];
        integral.get(0, 0, tempIntegral);
        integral2.get(0, 0, tempIntegral2);

        // make sure the window size is an odd number
        if (winSize % 2 == 0)
            winSize -= 1;

        // prepare the indexing for windowing
        int dist = (winSize + 1) / 2;
        int idxTop, idxBottom, idxLeft, idxRight;
        int sumLeftTop, sumRightTop, sumLeftBottom, sumRightBottom;
        double sumLeftTop2, sumRightTop2, sumLeftBottom2, sumRightBottom2;
        sumLeftTop = sumRightTop = sumLeftBottom = sumRightBottom = 0;
        sumLeftTop2 = sumRightTop2 = sumLeftBottom2 = sumRightBottom2 = 0;

        // calculate the threshold of each pixel with sauvola's formula
        // Mat thresholds = new Mat(in.rows(), in.cols(), CvType.CV_32SC1);
        Mat thresholds = new Mat(in.rows(), in.cols(), CvType.CV_8UC1);
        // int[] tempThresholds = new int[(int) (in.total()*in.channels())];
        byte[] tempThresholds = new byte[(int) (in.total() * in.channels())];
        thresholds.get(0, 0, tempThresholds);
        for (int i = 0; i < in.rows(); i++) {
            for (int ii = 0; ii < in.cols(); ii++) {
                // determining the index
                idxTop = i - dist;
                idxLeft = ii - dist;
                idxBottom = i + (dist - 1);
                idxRight = ii + (dist - 1);
                if (idxBottom >= in.rows())
                    idxBottom = in.rows() - 1;
                if (idxRight >= in.cols())
                    idxRight = in.cols() - 1;

                // determining the sum and sum of square
                sumRightBottom = tempIntegral[(idxBottom + 1) * integral.cols() + idxRight + 1];
                sumRightBottom2 = tempIntegral2[(idxBottom + 1) * integral2.cols() + idxRight + 1];
                sumLeftTop = 0;
                sumLeftTop2 = 0;
                sumLeftBottom = 0;
                sumLeftBottom2 = 0;
                sumRightTop = 0;
                sumRightTop2 = 0;

                if (idxLeft >= 0) {
                    sumLeftBottom = tempIntegral[(idxBottom + 1) * integral.cols() + idxLeft + 1];
                    sumLeftBottom2 = tempIntegral2[(idxBottom + 1) * integral2.cols() + idxLeft + 1];
                    if (idxTop >= 0) {
                        sumLeftTop = tempIntegral[(idxTop + 1) * integral.cols() + idxLeft + 1];
                        sumLeftTop2 = tempIntegral2[(idxTop + 1) * integral2.cols() + idxLeft + 1];
                    }
                }
                if (idxTop >= 0) {
                    sumRightTop = tempIntegral[(idxTop + 1) * integral.cols() + idxRight + 1];
                    sumRightTop2 = tempIntegral2[(idxTop + 1) * integral2.cols() + idxRight + 1];
                }
                if (idxLeft < 0)
                    idxLeft = -1;
                if (idxTop < 0)
                    idxTop = -1;

                // calculate the mean
                int n = (idxBottom - idxTop) * (idxRight - idxLeft);
                float mean = (float) (sumRightBottom - sumLeftBottom - sumRightTop + sumLeftTop) / n;
                // calculate the standard deviation
                float std = (float) Math.sqrt(((sumRightBottom2 - sumLeftBottom2 - sumRightTop2 + sumLeftTop2)
                        - (double) (sumRightBottom - sumLeftBottom - sumRightTop + sumLeftTop)
                                * (sumRightBottom - sumLeftBottom - sumRightTop + sumLeftTop) / n)
                        / n);

                // calculate the threshold
                float threshold = mean * (1 + 0.5f * (std / 128 - 1));
                tempThresholds[i * in.cols() + ii] = (byte) Math.round(threshold);
            }
        }
        thresholds.put(0, 0, tempThresholds);

        Mat out = new Mat(in.rows(), in.cols(), CvType.CV_8UC1);
        Core.compare(in, thresholds, out, Core.CMP_GE);

        thresholds = null;
        integral = null;
        integral2 = null;
        tempThresholds = null;
        tempIntegral = null;
        tempIntegral2 = null;

        return out;
    }

    /**
     * normalize the dimension of the given binary image to the given dimension
     * (using linear normalization) with new aspect ratio = square root of the
     * original aspect ratio
     * 
     * @param in
     *            the binary image whose size is to be normalized
     * @param newRows
     *            the number of rows of the normalized binary image
     * @param newCols
     *            the number of columns of the normalized binary image
     * @return a new binary image which is the result of normalizing the given
     *         binary image to the given dimension
     */
    public static Mat normalizationF2(Mat in, int newRows, int newCols) {
        Mat out = new Mat(newRows, newCols, CvType.CV_8UC1, new Scalar(255));
        float r1, r2;
        int rowsOfInterest, colsOfInterest;
        int startingNewRowIndex, startingNewColIndex;

        // Calculate the new dimension for the output matrix
        if (in.rows() < in.cols()) {
            r1 = (float) in.rows() / in.cols();
            r2 = (float) Math.sqrt(r1);
            colsOfInterest = newCols;
            startingNewColIndex = 0;
            rowsOfInterest = (int) Math.round(r2 * colsOfInterest);
            startingNewRowIndex = (int) Math.floor((newRows - rowsOfInterest) / 2);
        } else {
            r1 = (float) in.cols() / in.rows();
            r2 = (float) Math.sqrt(r1);
            rowsOfInterest = newRows;
            startingNewRowIndex = 0;
            colsOfInterest = (int) Math.round(r2 * rowsOfInterest);
            startingNewColIndex = (int) Math.floor((newCols - colsOfInterest) / 2);
        }

        // Calculate the transformation
        float alpha, betha;
        alpha = (float) colsOfInterest / in.cols();
        betha = (float) rowsOfInterest / in.rows();

        // Normalize by discretization
        byte[] inVals = new byte[(int) in.total()];
        in.get(0, 0, inVals);
        Mat outOfInterest = new Mat(rowsOfInterest, colsOfInterest, CvType.CV_8UC1, new Scalar(255));
        byte[] outVals = new byte[(int) outOfInterest.total()];
        outOfInterest.get(0, 0, outVals);

        // for all pixels
        int currentNewRowIndex, currentNewColIndex, nextNewRowIndex, nextNewColIndex;
        // scan through the original image
        for (int i = 0; i < in.rows(); i++) {
            // indexing for the normalized image
            currentNewRowIndex = Math.round(i * betha);
            currentNewRowIndex = (currentNewRowIndex >= outOfInterest.rows()) ? outOfInterest.rows() - 1
                    : currentNewRowIndex;
            nextNewRowIndex = Math.round((i + 1) * betha);
            nextNewRowIndex = (nextNewRowIndex >= outOfInterest.rows()) ? outOfInterest.rows() - 1 : nextNewRowIndex;

            for (int ii = 0; ii < in.cols(); ii++) {
                // indexing for the normalized image
                currentNewColIndex = Math.round(ii * alpha);
                currentNewColIndex = (currentNewColIndex >= outOfInterest.cols()) ? outOfInterest.cols() - 1
                        : currentNewColIndex;

                nextNewColIndex = Math.round((ii + 1) * alpha);
                nextNewColIndex = (nextNewColIndex >= outOfInterest.cols()) ? outOfInterest.cols() - 1
                        : nextNewColIndex;

                // for each pixel between the indices, update them with the value at (i,ii)
                for (int iii = currentNewRowIndex; iii <= nextNewRowIndex; iii++) {
                    for (int iiii = currentNewColIndex; iiii <= nextNewColIndex; iiii++) {
                        outVals[iii * outOfInterest.cols() + iiii] = inVals[i * in.cols() + ii];
                    }
                } // end of for (updating normalized image)
            }
        } // end of for (scanning original image's)

        // copy the region of interest to the center of the output matrix
        outOfInterest.put(0, 0, outVals);
        outOfInterest.copyTo(out.submat(startingNewRowIndex, startingNewRowIndex + outOfInterest.rows(),
                startingNewColIndex, startingNewColIndex + outOfInterest.cols()));
        return out;
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
     * thinning algorithm by Zhang & Wang that would make the foreground on the
     * given image matrix one pixel width
     * 
     * @param in
     *            the matrix containing the image's pixel which will be thinned. The
     *            black pixels are 0, while the white ones are 255
     * @return a matrix containing the image's pixel where the foreground pixels
     *         will be one pixel width
     */
    public static Mat thinningZhangWang(Mat in) {
        if (in.channels() > 1)
            return null;

        // expand the input matrix with a constant border of 0 value
        Mat out = new Mat(in.rows() + 2, in.cols() + 2, CvType.CV_8UC1);
        Core.copyMakeBorder(in, out, 1, 1, 1, 1, Core.BORDER_CONSTANT, new Scalar(255));
        Core.bitwise_not(out, out);
        // black pixel is one, while white pixel is 0

        boolean isEnd = false;
        boolean isStable = false;

        // create the values needed to determine whether a given weight number is going
        // to be erased (thinned)
        int[] pta2tRef = { 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 0, 4, 1, 0, 1, 0, 1, 3, 0, 4, 1, 0, 1, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 0, 4, 1, 0, 1, 0, 1, 3, 0, 4, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 1, 1, 0, 2, 1, 0, 1, 0, 1, 1, 0, 2, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1,
                1, 0, 2, 1, 0, 1, 0, 1, 1, 0, 2, 1, 0, 1, 0 };

        // window 1: window 2:
        // 701 345
        // 6 2 2 6
        // 543 107

        // create windows containing the power of 2 from 0-7 (for calculating Weight
        // Number)
        Mat[] windows = new Mat[2];
        float[] powerOfTwo = new float[9];
        windows[0] = new Mat(3, 3, CvType.CV_32FC1);
        windows[0].get(0, 0, powerOfTwo);
        powerOfTwo[0] = 128f / 255;
        powerOfTwo[1] = 1f / 255;
        powerOfTwo[2] = 2f / 255;
        powerOfTwo[3] = 64f / 255;
        powerOfTwo[4] = 0;
        powerOfTwo[5] = 4f / 255;
        powerOfTwo[6] = 32f / 255;
        powerOfTwo[7] = 16f / 255;
        powerOfTwo[8] = 8f / 255;
        windows[0].put(0, 0, powerOfTwo);
        matToTxt(windows[0], "./Example-I test.txt");
        windows[1] = new Mat(3, 3, CvType.CV_32FC1);
        windows[1].get(0, 0, powerOfTwo);
        powerOfTwo[8] = 128f / 255;
        powerOfTwo[7] = 1f / 255;
        powerOfTwo[6] = 2f / 255;
        powerOfTwo[5] = 64f / 255;
        powerOfTwo[4] = 0;
        powerOfTwo[3] = 4f / 255;
        powerOfTwo[2] = 32f / 255;
        powerOfTwo[1] = 16f / 255;
        powerOfTwo[0] = 8f / 255;
        windows[1].put(0, 0, powerOfTwo);

        // thinning process
        Mat weightNumbers = new Mat(out.rows(), out.cols(), CvType.CV_16UC1);
        byte[] outVal = new byte[(int) out.total()];
        out.get(0, 0, outVal);

        while (!isEnd) {
            isEnd = true;
            for (int k = 0; k < 2; k++) // for each window
            {
                // generate matrix of Weight Number
                Imgproc.filter2D(out, weightNumbers, CvType.CV_16UC1, windows[k], new Point(-1, -1), 0,
                        Core.BORDER_CONSTANT);
                short[] weightNumbersVal = new short[(int) (weightNumbers.total())];
                weightNumbers.get(0, 0, weightNumbersVal);

                // for each pixel (try parallel)
                for (int i = 1; i < in.rows() - 1; i++) {
                    for (int ii = 1; ii < in.cols() - 1; ii++) {
                        // if it is already 0
                        if (outVal[i * out.cols() + ii] == 0)
                            continue;

                        // check the value
                        isStable = false;
                        switch (pta2tRef[weightNumbersVal[i * weightNumbers.cols() + ii]]) {
                            case 1:
                                isStable = true;
                                break;
                            case 2:
                                if (k == 0) {
                                    if (pta2tRef[weightNumbersVal[(i - 1) * weightNumbers.cols() + ii]] == 0)
                                        isStable = true;
                                } else if (k == 1) {
                                    if (pta2tRef[weightNumbersVal[(i + 1) * weightNumbers.cols() + ii]] == 0)
                                        isStable = true;
                                }
                                break;
                            case 3:
                                if (k == 0) {
                                    if (pta2tRef[weightNumbersVal[(i) * weightNumbers.cols() + (ii - 1)]] == 0)
                                        isStable = true;
                                } else if (k == 1) {
                                    if (pta2tRef[weightNumbersVal[(i) * weightNumbers.cols() + (ii + 1)]] == 0)
                                        isStable = true;
                                }
                                break;
                            case 4:
                                if (k == 0) {
                                    if (pta2tRef[weightNumbersVal[(i) * weightNumbers.cols() + (ii - 1)]] == 0
                                            && pta2tRef[weightNumbersVal[(i - 1) * weightNumbers.cols() + ii]] == 0)
                                        isStable = true;
                                } else if (k == 1) {
                                    if (pta2tRef[weightNumbersVal[(i) * weightNumbers.cols() + (ii + 1)]] == 0
                                            && pta2tRef[weightNumbersVal[(i + 1) * weightNumbers.cols() + ii]] == 0)
                                        isStable = true;
                                }
                                break;
                        }

                        // check whether the given pixel is to be thinned or not
                        if (isStable) {
                            outVal[i * out.cols() + ii] = 0;
                            isEnd = false;
                        }
                    } // end of for cols
                } // end of for rows

                // update the out matrix, the one to be thin
                out.put(0, 0, outVal);
            } // end of for windows
        } // end of while
        Core.bitwise_not(out, out);
        return out.submat(1, out.rows() - 1, 1, out.cols() - 1);
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

    /**
     * get the inside rectangle from the given RotatedRect object. The inside
     * rectangle is the rectangle formed by taking the innermost x (for innermost
     * left and right coordinate) and y (for innermost top and bottom coordinate) as
     * corner points.
     * 
     * @param rotatedRect
     *            the RotatedRect object whose inside rectangle is to be found
     * @return the rectangle formed by taking the innermost x and y for all four
     *         directions
     */
    public static Rect findInsideRect(RotatedRect rotatedRect) {
        double xLeft, xRight, yTop, yBottom;

        // get the points on the rotated rectangle
        Point[] tempPoints = new Point[4];
        rotatedRect.points(tempPoints);

        // for all points
        xLeft = rotatedRect.center.x - rotatedRect.size.width;
        xRight = rotatedRect.center.x + rotatedRect.size.width;
        yTop = rotatedRect.center.y - rotatedRect.size.height;
        yBottom = rotatedRect.center.y + rotatedRect.size.height;

        // the center of the rotated rectangle to divide between left and right, also
        // between top and bottom
        for (int i = 0; i < 4; i++) {
            // if current x is located on the RIGHT side of the center
            if (tempPoints[i].x > rotatedRect.center.x) {
                if (xRight > tempPoints[i].x)
                    xRight = tempPoints[i].x;
            }
            // if current x is located on the LEFT side of the center
            else if (tempPoints[i].x < rotatedRect.center.x) {
                if (xLeft < tempPoints[i].x)
                    xLeft = tempPoints[i].x;
            } else
                return null;

            // if current y is located BELOW the center
            if (tempPoints[i].y > rotatedRect.center.y) {
                if (yBottom > tempPoints[i].y)
                    yBottom = tempPoints[i].y;
            }
            // if current y is located ABOVE the center
            else if (tempPoints[i].y < rotatedRect.center.y) {
                if (yTop < tempPoints[i].y)
                    yTop = tempPoints[i].y;
            } else
                return null;
        }

        return new Rect(new Point(xLeft + 2, yTop + 2), new Point(xRight - 2, yBottom - 2));
    }

    /**
     * Get a point which is the same as the given one, but with different origin
     * (use the given origin instead of (0,0))
     * 
     * @param point
     *            the point whose origin to be changed
     * @param origin
     *            the new origin point used to calculate the coordinate of the given
     *            point
     * @return a new point with the same coordinate with the given one, but
     *         calculated from different origin
     */
    public static Point changeOrigin(Point point, Point origin) {
        return new Point(point.x - origin.x, point.y - origin.y);
    }

    /**
     * 
     * @param grayscaleImg
     * @return
     */
    public static Mat trimBorders(Mat grayscaleImg) {
        // blur the image for filtering
        Mat blurredImg = new Mat(grayscaleImg.rows(), grayscaleImg.cols(), grayscaleImg.type());
        Imgproc.medianBlur(grayscaleImg, blurredImg, 3);

        // threshold the image
        Mat thresholdImg = new Mat(grayscaleImg.rows(), grayscaleImg.cols(), grayscaleImg.type());
        Imgproc.threshold(blurredImg, thresholdImg, 0, 255, Imgproc.THRESH_OTSU);
        // Imgcodecs.imwrite("./res/temp1.png", thresholdImg);

        // get the vertical and horizontal projection
        Mat verProj = verticalProjection(thresholdImg);
        int[] verProjVal = new int[(int) verProj.total()];
        verProj.get(0, 0, verProjVal);
        int leftBorder, rightBorder;
        leftBorder = rightBorder = -1;
        for (int i = 0; i < verProjVal.length; i++) {
            if (verProjVal[i] != 0 && leftBorder == -1)
                leftBorder = i;
            if (verProjVal[verProjVal.length - 1 - i] != 0 && rightBorder == -1)
                rightBorder = verProjVal.length - 1 - i;
            if (leftBorder != -1 && rightBorder != -1)
                break;
        }

        Mat horProj = horizontalProjection(thresholdImg);
        int[] horProjVal = new int[(int) horProj.total()];
        horProj.get(0, 0, horProjVal);
        int topBorder, bottomBorder;
        topBorder = bottomBorder = -1;
        for (int i = 0; i < horProjVal.length; i++) {
            if (horProjVal[i] != 0 && topBorder == -1)
                topBorder = i;
            if (horProjVal[horProjVal.length - 1 - i] != 0 && bottomBorder == -1)
                bottomBorder = horProjVal.length - 1 - i;
            if (topBorder != -1 && bottomBorder != -1)
                break;
        }

        // crop the image
        Mat out = grayscaleImg.submat(topBorder, bottomBorder + 1, leftBorder, rightBorder + 1);
        blurredImg = thresholdImg = verProj = horProj = null;
        return out;
    }
}
