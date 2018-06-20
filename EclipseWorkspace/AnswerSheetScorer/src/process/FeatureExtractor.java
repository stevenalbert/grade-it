package process;

import java.util.ArrayList;
import java.util.List;

import org.ejml.simple.SimpleMatrix;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * A class used as a collection of feature extraction method used mainly for 2D
 * grayscale image
 * 
 * @author DATACLIM-PIKU2
 *
 */
public class FeatureExtractor {
    public static final int TWO_DIRECTION = 2;
    public static final int FOUR_DIRECTION = 4;
    public static final int SIX_DIRECTION = 6;
    public static final int EIGHT_DIRECTION = 8;
    public static final int TWELVE_DIRECTION = 12;
    public static final int SIXTEEN_DIRECTION = 16;

    public static final int TEXTURE_ORIGINAL = 0;
    public static final int TEXTURE_THINNING = 1;
    public static final int TEXTURE_CONTOUR = 2;

    public static final int DEFAULT_IMAGE_SIZE = 32;
    public static final int DEFAULT_TEXTURE = TEXTURE_ORIGINAL;
    public static final int DEFAULT_CELL = 8;
    public static final int DEFAULT_BLOCK = 2;
    public static final int DEFAULT_OVERLAP = 0;
    public static final int DEFAULT_TTL_DIRECTIONS = FOUR_DIRECTION;
    public static final boolean DEFAULT_IS_SIGNED = false;
    public static final boolean DEFAULT_IS_DECOMPOSED = true;
    public static final boolean DEFAULT_IS_NORM = true;

    private static final float E_NORM = 0.01f;

    private static final int X_NORM_SIZE = 11;
    // private static final int X_NORM_SIZE = 21;

    /**
     * Concatenate the given Mat object into a column vector of type double
     * 
     * @param mats
     *            the array of Mat object to be concatenated into a single array of
     *            type double
     * @return an array of type double which is a column vector resulting from
     *         concatenating the given array of Mats
     */
    public static double[] matsToDouble(Mat[] mats) {
        // prepare the variable
        double[] matVal = new double[mats.length * mats[0].cols()];

        // concatenate the hogs into 1 dimensional column vector
        // for each hogs
        for (int i = 0; i < mats.length; i++) {
            // get the value for each hog
            float[] eachMatVal = new float[mats[0].cols()];
            mats[i].get(0, 0, eachMatVal);

            // for each column in each region's hog, concatenate to the main array
            for (int ii = 0; ii < eachMatVal.length; ii++)
                matVal[i * mats[i].cols() + ii] = eachMatVal[ii];
        }
        return matVal;
    }

    /**
     * get gradient features from the given binary image with Sobel operators. The
     * details are as follow: <br>
     * 1. normalized the image using moment normalization <br>
     * 2. calculate the gradient (magnitude and direction) of each pixel <br>
     * 3. partition the binary image into non-overlapping regions with the specified
     * regionWidth and regionHeight <br>
     * 4. calculate HOG (Histogram of Gradients) for each region by projecting /
     * decomposing the gradient of each pixel into the 2 closest standard directions
     * 
     * @param img
     *            the image where gradient features are calculated from
     * @param texture
     *            the texture of the character on which features are to be extracted
     *            (use the available constants for extracting features from original
     *            black-white image, its contour, or its thinned version)
     * @param normalizedWidth
     *            the width the image will be normalized to
     * @param normalizedHeight
     *            the height the image will be normalized to
     * @param cellWidthInPixel
     *            the width of the cell in pixels (must divide the image without
     *            remains)
     * @param cellHeightInPixel
     *            the height of the cell in pixels (must divide the image without
     *            remains)
     * @param blockWidthInCell
     *            the width of the block in cells
     * @param blockHeightInCell
     *            the height of the block in cells
     * @param cellOverlapped
     *            number of overlapped cells between adjacent blocks (for both row
     *            and column)
     * @param ttlDirections
     *            the total number of standard directions for the HOG
     * @param isDirectionSigned
     *            true if only vectors in the first and second quadrant are to be
     *            considered, false if vector from all quadrants are to be
     *            considered
     * @param isDecomposed
     *            true if the gradient vectors are to be decomposed into standard
     *            directions, false if the gradient vectors are grouped into range
     * @param fileName
     * @return an array of Mat which contains the HOG (whose size equals
     *         ttlDirections) for each region in the image (sorted from top to
     *         bottom, and left to right). Each HOG is a column vector (with 1 row
     *         and ttlDirections columns).
     */
    public static Mat[] getGradientFeatures(Mat img, int texture, int normalizedWidth, int normalizedHeight,
            int cellWidthInPixel, int cellHeightInPixel, int blockWidthInCell, int blockHeightInCell,
            int cellOverlapped, int ttlDirections, boolean isDirectionSigned, boolean isDecomposed, boolean isL2Normed,
            String fileName) {
        // CHECKING THE DIMENSION
        // ======================
        // check overlapping cells and total cells in each block
        if (cellOverlapped >= blockWidthInCell || cellOverlapped >= blockHeightInCell)
            return null;
        if (blockWidthInCell <= 0)
            blockWidthInCell = 1;
        if (blockHeightInCell <= 0)
            blockHeightInCell = 1;
        int ttlCellsHor = normalizedWidth / cellWidthInPixel;
        int ttlCellsVer = normalizedHeight / cellHeightInPixel;

        // check consistency among overlapping cells, cells per block, and total cells
        // available
        int startCellRow, startCellCol; // the first cell's index of a block (inclusive)
        int endCellRow, endCellCol; // the last cell's index of a block (exclusive)
        int intervalRowInCell, intervalColInCell; // the interval between two adjacent cells (in cells)
        int ttlBlocksRow, ttlBlocksCol; // total number of blocks in each row and column respectively
        startCellRow = startCellCol = 0;
        endCellRow = startCellRow + blockHeightInCell;
        endCellCol = startCellCol + blockWidthInCell;
        intervalRowInCell = blockHeightInCell - cellOverlapped;
        intervalColInCell = blockWidthInCell - cellOverlapped;
        // checking the rows
        ttlBlocksRow = ttlBlocksCol = 1;
        while (endCellRow != ttlCellsVer) {
            ttlBlocksRow++;
            startCellRow += intervalRowInCell;
            endCellRow = startCellRow + blockHeightInCell;
            if (endCellRow > ttlCellsVer)
                return null;
        }
        // checking the cols
        while (endCellCol != ttlCellsHor) {
            ttlBlocksCol++;
            startCellCol += intervalColInCell;
            endCellCol = startCellCol + blockWidthInCell;
            if (endCellCol > ttlCellsHor)
                return null;
        }
        // PREPROCESS THE IMAGE
        // ====================
        // threshold the image
        Mat binaryImg = new Mat(img.rows(), img.cols(), img.type());
        Imgproc.threshold(img, binaryImg, 0, 255, Imgproc.THRESH_OTSU);

        // normalized the image
        Mat normalized = GrayImgProc.normalizationF9(binaryImg, normalizedHeight, normalizedWidth);

        // determine the texture of the character
        if (texture == TEXTURE_CONTOUR) {
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(normalized, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            normalized = Mat.zeros(normalized.rows(), normalized.cols(), normalized.type());
            Imgproc.drawContours(normalized, contours, -1, new Scalar(255));
        } else if (texture == TEXTURE_THINNING)
            normalized = GrayImgProc.thinningZhangWang(normalized);

        // get the gradient for each pixel with Sobel operator
        Mat[] gradient = getSobelGradients(normalized, 3);

        // PROCESSING HOGS FOR CELLS
        // =========================
        // partitioned the feature into cells
        Mat[] magnitudeInCells = GrayImgProc.partitionIntoRegions(gradient[0], cellWidthInPixel, cellHeightInPixel);
        Mat[] directionInCells = GrayImgProc.partitionIntoRegions(gradient[1], cellWidthInPixel, cellHeightInPixel);

        // create the hog by grouping the gradient features according to the direction
        Mat[] hogCells = calculateHogCells(magnitudeInCells, directionInCells, ttlDirections, isDirectionSigned,
                isDecomposed);

        // PROCESSING HOGS FOR BLOCKS
        // ==========================
        Mat[] hogBlocks = new Mat[ttlBlocksRow * ttlBlocksCol];
        // for each row
        for (int i = 0; i < ttlBlocksRow; i++) {
            // for each col
            for (int ii = 0; ii < ttlBlocksCol; ii++) {
                hogBlocks[i * ttlBlocksCol + ii] = new Mat(1, ttlDirections * blockHeightInCell * blockWidthInCell,
                        CvType.CV_32F);
                float[] cellVal = new float[ttlDirections];
                // System.out.println(i + "\t" + intervalRowInCell + "\t" + ii + "\t" +
                // intervalColInCell);
                // System.out.println(i + "\t" + ii + "\t" );
                // iterate through all cells correspond to this block
                for (int j = 0; j < blockHeightInCell; j++) {
                    for (int jj = 0; jj < blockWidthInCell; jj++) {
                        /// System.out.print((i*intervalRowInCell+j) + "\t" + (ii*intervalColInCell+jj)
                        /// + "\t\t");
                        // concatenate hogs from all corresponding cells
                        // hogCells[(i+j)*ttlCellsHor + (ii+jj)].get(0, 0, cellVal);
                        hogCells[(i * intervalRowInCell + j) * ttlCellsHor + (ii * intervalColInCell + jj)].get(0, 0,
                                cellVal);
                        hogBlocks[i * ttlBlocksCol + ii].put(0,
                                j * blockWidthInCell * ttlDirections + jj * ttlDirections,
                                // (j*blockWidthInCell + jj)*ttlDirections ,
                                cellVal);
                        // System.out.println((j*blockWidthInCell*ttlDirections + jj*ttlDirections));
                        // hogBlocks[i*ttlBlocksCol + ii].put(0, (j*blockWidthInCell + jj)*ttlDirections
                        // , cellVal);
                        /*
                         * for(int k=0; k<cellVal.length; k++) System.out.print(cellVal[k] + "\t");
                         * System.out.println();
                         */
                    }
                    // System.out.println();
                }
                // System.out.println();
                // normalize the feature vector (gradient feature) of the current block
                if (isL2Normed)
                    hogBlocks[i * ttlBlocksCol + ii] = l2Norm(hogBlocks[i * ttlBlocksCol + ii]);
            }
        }
        double[] temp = matsToDouble(hogBlocks);
        Mat allHog = new Mat(1, temp.length, CvType.CV_64F);
        allHog.put(0, 0, temp);
        GrayImgProc.matToTxt(allHog, "./res/hogs.txt");
        return hogBlocks;
    }

    /**
     * Calculate histogram of gradients in all cells given as the parameters
     * 
     * @param magnitudeInCells
     *            an array of Mat object, each representing a cell of an image,
     *            consisting of gradient's magnitude
     * @param directionInCells
     *            an array of Mat object, each representing a cell of an image,
     *            consisting of gradient's direction (angle)
     * @param ttlDirections
     *            the total number of standard directions for the HOG
     * @param isDirectionSigned
     *            true if only vectors in the first and second quadrant are to be
     *            considered, false if vector from all quadrants are to be
     *            considered
     * @param isDecomposed
     *            true if the gradient vectors are to be decomposed into standard
     *            directions, false if the gradient vectors are grouped into range
     * @return an array of Mat object, each containing the HoG for each cell
     */
    private static Mat[] calculateHogCells(Mat[] magnitudeInCells, Mat[] directionInCells, int ttlDirections,
            boolean isDirectionSigned, boolean isDecomposed) {
        // prepare the variable to be returned
        Mat[] hogCells = new Mat[magnitudeInCells.length];

        // calculate the interval for each direction
        float angleInterval;
        if (isDirectionSigned)
            angleInterval = (float) (2 * Math.PI) / ttlDirections;
        else
            angleInterval = (float) (Math.PI) / ttlDirections;

        // variables needed to decompose the gradient vector
        SimpleMatrix A, b; // needed in decomposition method
        A = new SimpleMatrix(2, 2); // storing the left hand side
        b = new SimpleMatrix(2, 1); // storing the right hand side

        // for all cells
        for (int i = 0; i < magnitudeInCells.length; i++) {
            // get the values from Mat
            float[] magnitudeVal = new float[magnitudeInCells[i].rows() * magnitudeInCells[i].cols()];
            float[] directionVal = new float[directionInCells[i].rows() * directionInCells[i].cols()];
            ;
            magnitudeInCells[i].get(0, 0, magnitudeVal);
            directionInCells[i].get(0, 0, directionVal);

            // prepare the hog for this cell (in float)
            hogCells[i] = new Mat(1, ttlDirections, CvType.CV_32FC1);
            float[] hogVal = new float[ttlDirections];

            // for each row
            double currentMagnitude;
            double currentAngle;
            for (int ii = 0; ii < magnitudeInCells[i].rows(); ii++) {
                // for each column
                for (int iii = 0; iii < magnitudeInCells[i].cols(); iii++) {
                    // process the current pixel (skip if the magnitude is 0)
                    currentMagnitude = magnitudeVal[ii * magnitudeInCells[i].cols() + iii];
                    currentAngle = directionVal[ii * directionInCells[i].cols() + iii];

                    // skip if current magnitude will not contribute anything
                    if (currentMagnitude == 0)
                        continue;

                    // if not signed, convert the angle to its positive counterpart
                    if (!isDirectionSigned)
                        if (currentAngle < 0)
                            currentAngle += Math.PI;

                    // check the lower bound used to determine the bin in both algorithms
                    int firstBin;
                    float division = (float) (currentAngle / angleInterval);
                    if (Math.abs(division - Math.ceil(division)) < 0.0001)
                        firstBin = (int) Math.ceil(division) % ttlDirections;
                    else
                        firstBin = (int) Math.floor(division) % ttlDirections;

                    // if signed, currentAngle can be negative, convert the lowerBound to match the
                    // correct bin
                    if (isDirectionSigned)
                        if (firstBin < 0)
                            firstBin += ttlDirections;

                    // System.out.println(currentAngle + "\t" + Math.cos(currentAngle) + "\t" +
                    // Math.sin(currentAngle) + "\t" + currentMagnitude + "\t" + firstBin + "\t");
                    // System.out.print(currentAngle + "\t" + Math.cos(currentAngle) + "\t" +
                    // Math.sin(currentAngle) + "\t" + currentMagnitude + "\t" +
                    // (currentAngle/angleInterval) + "\t");
                    // System.out.print(currentAngle + "\t" + Math.cos(currentAngle) + "\t" +
                    // Math.sin(currentAngle) + "\t" + currentMagnitude + "\t" + firstBin + "\t");
                    // assign the gradient to its bin either based on decomposition or a range of
                    // directions
                    if (!isDecomposed) {
                        hogVal[firstBin] += currentMagnitude;
                    } else {
                        // calculate the second bin for decomposition
                        int secondBin = (firstBin + 1) % ttlDirections;

                        // Solve a linear equation for decomposing the gradient vector
                        A.set(0, 0, Math.cos(firstBin * angleInterval));
                        A.set(1, 0, Math.sin(firstBin * angleInterval));
                        A.set(0, 1, Math.cos(secondBin * angleInterval));
                        A.set(1, 1, Math.sin(secondBin * angleInterval));
                        b.set(0, 0, Math.cos(currentAngle));
                        b.set(1, 0, Math.sin(currentAngle));
                        SimpleMatrix c = A.solve(b);
                        /*
                         * float firstDecomposition = (float)(c.get(0, 0) * currentMagnitude); float
                         * secondDecomposition = (float)(c.get(1, 0) * currentMagnitude);
                         */
                        float firstDecomposition = Math.abs((float) (c.get(0, 0) * currentMagnitude));
                        float secondDecomposition = Math.abs((float) (c.get(1, 0) * currentMagnitude));
                        // System.out.println(secondBin + "\t" + firstDecomposition + "\t" +
                        // secondDecomposition);
                        hogVal[firstBin] += firstDecomposition;
                        hogVal[secondBin] += secondDecomposition;
                    }
                }
            }
            // System.out.println();
            // put the value back to Mat
            hogCells[i].put(0, 0, hogVal);

            /*
             * for(int ii=0; ii<hogVal.length ;ii++) System.out.print(hogVal[ii] + "\t");
             * System.out.println();
             */
        }
        return hogCells;
    }

    /**
     * get the gradient (magnitude and direction in rad) of each pixel in the given
     * image
     * 
     * @param in
     *            the image from where the gradient will be extracted
     * @param kSize
     *            the kernel size used to calculate the derivative on x and y
     *            direction
     * @return an array of matrix and size 2, where the first index is for the
     *         magnitude, and the second index for the direction (in rad)
     */
    private static Mat[] getSobelGradients(Mat in, int kSize) {
        if (in.channels() != 1)
            return null;

        // apply the Sobel operator
        Mat[] sobel = getSobelFirstDerivativesXY(in, kSize);
        Mat[] grad = new Mat[2];
        grad[0] = new Mat(in.rows(), in.cols(), CvType.CV_32FC1);
        grad[1] = new Mat(in.rows(), in.cols(), CvType.CV_32FC1);

        // get the derivative on x and y direction
        short[] dx = new short[(int) in.total() * in.channels()];
        short[] dy = new short[(int) in.total() * in.channels()];
        sobel[0].get(0, 0, dx);
        sobel[1].get(0, 0, dy);

        // finding the magnitude and angle from gradients (result of Sobel operator)
        float[] tempMagnitude = new float[(int) (in.total() * in.channels())];
        float[] tempAngle = new float[(int) (in.total() * in.channels())];
        grad[0].get(0, 0, tempMagnitude);
        grad[1].get(0, 0, tempAngle);

        // for each pixel, calculate the magnitude and angle of gradient
        for (int i = 0; i < in.rows(); i++) {
            for (int ii = 0; ii < in.cols(); ii++) {
                tempMagnitude[i * in.cols() + ii] = (float) Math.sqrt(dx[i * in.cols() + ii] * dx[i * in.cols() + ii]
                        + dy[i * in.cols() + ii] * dy[i * in.cols() + ii]);
                tempAngle[i * in.cols() + ii] = (float) Math.atan2(dy[i * in.cols() + ii], dx[i * in.cols() + ii]);
            }
        }
        grad[0].put(0, 0, tempMagnitude);
        grad[1].put(0, 0, tempAngle);
        return grad;
    }

    /**
     * apply Sobel mask to the given grayscale image with the given kernel size
     * 
     * @param in
     *            the grayscale image on which Sobel operation is applied
     * @param kSize
     *            the size of the Sobel mask / kernel
     * @return an array of type Mat and size 2 (of type CV_16SC1) containing the
     *         gradient in x and y direction (in index 0 and 1 respectively)
     */
    private static Mat[] getSobelFirstDerivativesXY(Mat in, int kSize) {
        // check if the given image has more than one channels (usually in non-grayscale
        // image)
        if (in.channels() != 1)
            return null;

        // create the gradient for x and y with the given depth
        int depth = CvType.CV_16SC1;
        Mat sobelX = new Mat(in.rows(), in.cols(), depth);
        Mat sobelY = new Mat(in.rows(), in.cols(), depth);

        // Sobel operator
        Imgproc.Sobel(in, sobelX, depth, 1, 0, kSize, 1, 0);
        Imgproc.Sobel(in, sobelY, depth, 0, 1, kSize, 1, 0);

        // combined into single Mat to be returned
        Mat[] out = new Mat[2];
        out[0] = sobelX;
        out[1] = sobelY;

        sobelX = null;
        sobelY = null;
        return out;
    }

    /**
     * get a number indicating whether the given image contains an X or not. The
     * calculation is done using convolution with a mask which is made so that it
     * can detect an X.
     * 
     * @param imgThreshold
     *            the image to be checked whether it contains an X or not
     * @return a number indicating whether the given image contains an X or not. The
     *         more positive it is, the higher the chance
     */
    public static double getFeatureX(Mat imgThreshold, String temp) {
        // =============
        // preprocessing
        // =============
        // filter the image
        // Imgproc.medianBlur(imgThreshold, imgThreshold, 3);

        // convert to binary
        // Imgproc.threshold(imgThreshold, imgThreshold, 150, 255, imgThreshold.type());

        // =======================================================
        // crop only the character without excess white background
        // =======================================================
        // top and bottom pixels
        Mat projHor = GrayImgProc.horizontalProjection(imgThreshold);
        int[] projHorVal = new int[(int) projHor.total()];
        projHor.get(0, 0, projHorVal);
        int idxTop = 0, idxBottom = projHorVal.length - 1;
        while (projHorVal[idxTop] == 0)
            idxTop++;
        while (projHorVal[idxBottom] == 0)
            idxBottom--;

        // left and right pixels
        Mat projVer = GrayImgProc.verticalProjection(imgThreshold);
        int[] projVerVal = new int[(int) projVer.total()];
        projVer.get(0, 0, projVerVal);
        int idxLeft = 0, idxRight = projVerVal.length - 1;
        while (projVerVal[idxLeft] == 0)
            idxLeft++;
        while (projVerVal[idxRight] == 0)
            idxRight--;

        // crop the character, then convert the image so that black indicates
        // background, while white indicates object
        Mat imgCropped = imgThreshold.submat(idxTop, idxBottom + 1, idxLeft, idxRight + 1);

        // ================================
        // normalize using F9 Normalization
        // ================================
        Mat imgNorm = GrayImgProc.normalizationF9(imgCropped, X_NORM_SIZE, X_NORM_SIZE);

        // threshold the image so that 255 becomes 1
        Imgproc.threshold(imgNorm, imgNorm, 127, 1, Imgproc.THRESH_BINARY);
        imgNorm.convertTo(imgNorm, CvType.CV_8SC1);

        // ==============================
        // for masking to find the center
        // ==============================
        // create the mask
        Mat maskCenter = Mat.ones(5, 5, CvType.CV_8SC1);
        maskCenter.put(2, 2, 5);

        // index for masking (adjustment by +1 and -1 to get the center)
        int idxStartCenterRow = (imgNorm.rows() + 1) / 2 - 1 - maskCenter.rows() / 2;
        int idxEndCenterRow = (imgNorm.rows() + 1) / 2 - 1 + maskCenter.rows() / 2;
        int idxStartCenterCol = (imgNorm.cols() + 1) / 2 - 1 - maskCenter.cols() / 2;
        int idxEndCenterCol = (imgNorm.cols() + 1) / 2 - 1 + maskCenter.cols() / 2;
        int idxCenterRow = 0;
        int idxCenterCol = 0;
        double dotMax = 0, dotTemp = 0;
        int distXTemp = maskCenter.cols(), distYTemp = maskCenter.rows();

        // searching the center of the character
        Mat imgTemp = new Mat();
        for (int ii = idxStartCenterRow; ii <= idxEndCenterRow; ii++) {
            for (int iii = idxStartCenterCol; iii <= idxEndCenterCol; iii++) {
                imgTemp = imgNorm.submat(ii - maskCenter.rows() / 2, ii + maskCenter.rows() / 2 + 1,
                        iii - maskCenter.cols() / 2, iii + maskCenter.cols() / 2 + 1);
                dotTemp = imgTemp.dot(maskCenter);
                if (dotMax < dotTemp) {
                    dotMax = dotTemp;
                    idxCenterRow = ii;
                    idxCenterCol = iii;
                    distXTemp = Math.abs(idxCenterCol - imgNorm.cols() / 2);
                    distYTemp = Math.abs(idxCenterRow - imgNorm.rows() / 2);
                } else if (dotMax == dotTemp) {
                    int distX = Math.abs((iii - imgNorm.cols() / 2));
                    int distY = Math.abs((ii - imgNorm.rows() / 2));
                    if (distX + distY < distXTemp + distYTemp) {
                        dotMax = dotTemp;
                        idxCenterRow = ii;
                        idxCenterCol = iii;
                        distXTemp = Math.abs(idxCenterCol - imgNorm.cols() / 2);
                        distYTemp = Math.abs(idxCenterRow - imgNorm.rows() / 2);
                    }
                }
            }
        }

        // shift the image according to the new found center
        Mat transformMatrix = new Mat(2, 3, CvType.CV_32FC1);
        transformMatrix.put(0, 0, 1);
        transformMatrix.put(0, 1, 0);
        transformMatrix.put(0, 2, imgNorm.cols() / 2 - idxCenterCol);
        transformMatrix.put(1, 0, 0);
        transformMatrix.put(1, 1, 1);
        transformMatrix.put(1, 2, imgNorm.rows() / 2 - idxCenterRow);
        // Mat imgShifted = Mat.zeros(X_NORM_SIZE, X_NORM_SIZE, CvType.CV_8SC1);
        Mat imgShifted = Mat.zeros(X_NORM_SIZE, X_NORM_SIZE, CvType.CV_8UC1);
        imgNorm.convertTo(imgNorm, CvType.CV_8UC1);
        Imgproc.warpAffine(imgNorm, imgShifted, transformMatrix, imgShifted.size());

        // =====================================================
        // masking to find the result of convolution with X-mask
        // =====================================================
        // create the mask, then dot the features
        Mat mask = createMaskX11();
        // Mat mask = createMaskX21();
        imgShifted.convertTo(imgShifted, CvType.CV_8SC1);
        double result = imgShifted.dot(mask);

        // ============================================================================================
        // drawing the image

        if (true) {
            Core.bitwise_not(imgCropped, imgCropped);
            Imgcodecs.imwrite(temp + "-1crop.jpg", imgCropped);
            imgNorm.convertTo(imgNorm, CvType.CV_8UC1);
            Imgproc.threshold(imgNorm, imgNorm, 0, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_not(imgNorm, imgNorm);
            Imgcodecs.imwrite(temp + "-2norm.jpg", imgNorm);
            Core.bitwise_not(imgNorm, imgNorm);
            GrayImgProc.matToTxt(imgNorm, temp + "-2norm.txt");
            imgShifted.convertTo(imgShifted, CvType.CV_8UC1);
            Imgproc.threshold(imgShifted, imgShifted, 0, 255, Imgproc.THRESH_BINARY);
            Core.bitwise_not(imgShifted, imgShifted);
            Imgcodecs.imwrite(temp + "-4shifted.jpg", imgShifted);
            Core.bitwise_not(imgShifted, imgShifted);
            GrayImgProc.matToTxt(imgShifted, temp + "-4shifted.txt");
        }

        return result;
    }

    /**
     * create a 11x11 mask to detect "X" symbol as follows: </br>
     * 1 : 10 10 10 00 -90 -90 -90 00 10 10 10 </br>
     * 2 : 10 10 10 10 00 -90 00 10 10 10 10 </br>
     * 3 : 10 10 10 10 10 00 10 10 10 10 10 </br>
     * 4 : 00 10 10 10 10 10 10 10 10 10 00 </br>
     * 5 :-90 00 10 10 10 10 10 10 10 00 -90 </br>
     * 6 :-90 -90 00 10 10 10 10 10 00 -90 -90 </br>
     * 7 :-90 00 10 10 10 10 10 10 10 00 -90 </br>
     * 8 : 00 10 10 10 10 10 10 10 10 10 00 </br>
     * 9 : 10 10 10 10 10 00 10 10 10 10 10 </br>
     * 10 : 10 10 10 10 00 -90 00 10 10 10 10 </br>
     * 11 : 10 10 10 00 -90 -90 -90 00 10 10 10
     * 
     * @return a Mat object of size 11x11 containing the mask described above
     */
    private static Mat createMaskX11() {
        Mat mask = Mat.zeros(11, 11, CvType.CV_8SC1);

        // the first row
        mask.put(0, 0, 10);
        mask.put(0, 1, 10);
        mask.put(0, 2, 10);
        mask.put(0, 3, 0);
        mask.put(0, 4, -90);
        mask.put(0, 5, -90);
        mask.put(0, 6, -90);
        mask.put(0, 7, 0);
        mask.put(0, 8, 10);
        mask.put(0, 9, 10);
        mask.put(0, 10, 10);

        // the second row
        mask.put(1, 0, 10);
        mask.put(1, 1, 10);
        mask.put(1, 2, 10);
        mask.put(1, 3, 10);
        mask.put(1, 4, 0);
        mask.put(1, 5, -90);
        mask.put(1, 6, 0);
        mask.put(1, 7, 10);
        mask.put(1, 8, 10);
        mask.put(1, 9, 10);
        mask.put(1, 10, 10);

        // the third row
        mask.put(2, 0, 10);
        mask.put(2, 1, 10);
        mask.put(2, 2, 10);
        mask.put(2, 3, 10);
        mask.put(2, 4, 10);
        mask.put(2, 5, 0);
        mask.put(2, 6, 10);
        mask.put(2, 7, 10);
        mask.put(2, 8, 10);
        mask.put(2, 9, 10);
        mask.put(2, 10, 10);

        // the fourth row
        mask.put(3, 0, 0);
        mask.put(3, 1, 10);
        mask.put(3, 2, 10);
        mask.put(3, 3, 10);
        mask.put(3, 4, 10);
        mask.put(3, 5, 10);
        mask.put(3, 6, 10);
        mask.put(3, 7, 10);
        mask.put(3, 8, 10);
        mask.put(3, 9, 10);
        mask.put(3, 10, 0);

        // the fifth row
        mask.put(4, 0, -90);
        mask.put(4, 1, 0);
        mask.put(4, 2, 10);
        mask.put(4, 3, 10);
        mask.put(4, 4, 10);
        mask.put(4, 5, 10);
        mask.put(4, 6, 10);
        mask.put(4, 7, 10);
        mask.put(4, 8, 10);
        mask.put(4, 9, 0);
        mask.put(4, 10, -90);

        // the sixth row
        mask.put(5, 0, -90);
        mask.put(5, 1, -90);
        mask.put(5, 2, 0);
        mask.put(5, 3, 10);
        mask.put(5, 4, 10);
        mask.put(5, 5, 10);
        mask.put(5, 6, 10);
        mask.put(5, 7, 10);
        mask.put(5, 8, 0);
        mask.put(5, 9, -90);
        mask.put(5, 10, -90);

        // the seventh row
        mask.put(6, 0, -90);
        mask.put(6, 1, 0);
        mask.put(6, 2, 10);
        mask.put(6, 3, 10);
        mask.put(6, 4, 10);
        mask.put(6, 5, 10);
        mask.put(6, 6, 10);
        mask.put(6, 7, 10);
        mask.put(6, 8, 10);
        mask.put(6, 9, 0);
        mask.put(6, 10, -90);

        // the eight row
        mask.put(7, 0, 0);
        mask.put(7, 1, 10);
        mask.put(7, 2, 10);
        mask.put(7, 3, 10);
        mask.put(7, 4, 10);
        mask.put(7, 5, 10);
        mask.put(7, 6, 10);
        mask.put(7, 7, 10);
        mask.put(7, 8, 10);
        mask.put(7, 9, 10);
        mask.put(7, 10, 0);

        // the ninth row
        mask.put(8, 0, 10);
        mask.put(8, 1, 10);
        mask.put(8, 2, 10);
        mask.put(8, 3, 10);
        mask.put(8, 4, 10);
        mask.put(8, 5, 0);
        mask.put(8, 6, 10);
        mask.put(8, 7, 10);
        mask.put(8, 8, 10);
        mask.put(8, 9, 10);
        mask.put(8, 10, 10);

        // the tenth row
        mask.put(9, 0, 10);
        mask.put(9, 1, 10);
        mask.put(9, 2, 10);
        mask.put(9, 3, 10);
        mask.put(9, 4, 0);
        mask.put(9, 5, -90);
        mask.put(9, 6, 0);
        mask.put(9, 7, 10);
        mask.put(9, 8, 10);
        mask.put(9, 9, 10);
        mask.put(9, 10, 10);

        // the eleventh row
        mask.put(10, 0, 10);
        mask.put(10, 1, 10);
        mask.put(10, 2, 10);
        mask.put(10, 3, 0);
        mask.put(10, 4, -90);
        mask.put(10, 5, -90);
        mask.put(10, 6, -90);
        mask.put(10, 7, 0);
        mask.put(10, 8, 10);
        mask.put(10, 9, 10);
        mask.put(10, 10, 10);

        return mask;
    }

    /**
     * create a 21x21 mask to detect "X" symbol as follows: </br>
     * 10 10 10 10 10 00 00 -90 -90 -90 -90 -90 -90 -90 00 00 10 10 10 10 10</br>
     * 10 10 10 10 10 10 00 00 -90 -90 -90 -90 -90 00 00 10 10 10 10 10 10</br>
     * 10 10 10 10 10 10 10 00 00 -90 -90 -90 00 00 10 10 10 10 10 10 10</br>
     * 10 10 10 10 10 10 10 10 00 00 -90 00 00 10 10 10 10 10 10 10 10</br>
     * 10 10 10 10 10 10 10 10 10 00 00 00 10 10 10 10 10 10 10 10 10</br>
     * 00 10 10 10 10 10 10 10 10 10 00 10 10 10 10 10 10 10 10 10 00</br>
     * 00 00 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 00 00</br>
     * -90 00 00 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 00 00 -90</br>
     * -90 -90 00 00 10 10 10 10 10 10 10 10 10 10 10 10 10 00 00 -90 -90</br>
     * -90 -90 -90 00 00 10 10 10 10 10 10 10 10 10 10 10 00 00 -90 -90 -90</br>
     * -90 -90 -90 -90 00 00 10 10 10 10 10 10 10 10 10 00 00 -90 -90 -90 -90</br>
     * -90 -90 -90 00 00 10 10 10 10 10 10 10 10 10 10 10 00 00 -90 -90 -90</br>
     * -90 -90 00 00 10 10 10 10 10 10 10 10 10 10 10 10 10 00 00 -90 -90</br>
     * -90 00 00 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 00 00 -90</br>
     * 00 00 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 10 00 00</br>
     * 00 10 10 10 10 10 10 10 10 10 00 10 10 10 10 10 10 10 10 10 00</br>
     * 10 10 10 10 10 10 10 10 10 00 00 00 10 10 10 10 10 10 10 10 10</br>
     * 10 10 10 10 10 10 10 10 00 00 -90 00 00 10 10 10 10 10 10 10 10</br>
     * 10 10 10 10 10 10 10 00 00 -90 -90 -90 00 00 10 10 10 10 10 10 10</br>
     * 10 10 10 10 10 10 00 00 -90 -90 -90 -90 -90 00 00 10 10 10 10 10 10</br>
     * 10 10 10 10 10 00 00 -90 -90 -90 -90 -90 -90 -90 00 00 10 10 10 10 10</br>
     * 
     * @return a Mat object of size 11x11 containing the mask described above
     */
    private static Mat createMaskX21() {
        Mat mask = new Mat(21, 21, CvType.CV_8SC1);
        byte[] maskVal = new byte[] { 10, 10, 10, 10, 10, 00, 00, -90, -90, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10, 00, 00, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 00, 00, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 00, 00, -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00,
                00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 00, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00,
                -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00, -90, -90, -90, 00, 00,
                10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10, 00, 00, -90, -90, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 00, 00, -90, -90, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 00, 00, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00,
                00, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00, -90, 00,
                00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00, 00, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00,
                00, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00, -90, 00, 00, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 00, 00, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 00, 00, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 00, 00, -90, -90, -90, -90, -90, -90, -90, 00, 00, 10, 10, 10, 10, 10 };

        mask.put(0, 0, maskVal);
        return mask;
    }

    /**
     * get the density of black pixels (total number of black pixels) of the binary
     * image
     * 
     * @param binaryImg
     *            the binary image
     * @return the density of black pixels of the given binary image
     */
    public static int getBlackDensity(Mat binaryImg) {
        return binaryImg.rows() * binaryImg.cols() - Core.countNonZero(binaryImg);
    }

    /**
     * get the density of white pixels (total number of white pixels) of the binary
     * image
     * 
     * @param binaryImg
     *            the binary image
     * @return the density of white pixels of the given binary image
     */
    public static int getWhiteDensity(Mat binaryImg) {
        return Core.countNonZero(binaryImg);
    }

    /**
     * Extract features from the given grayscale image in the form of 1 dimensional
     * vector. The features are the grayscale values, normalized to -1..1, of the
     * given image which is first normalized into normRows and normCols (without
     * keeping the aspect ratio), then place it in the middle of the image of size
     * outRows and outCols. In the extracted features, black pixels are treated as 1
     * while white pixels are treated as -1
     * 
     * @param grayscaleImg
     *            the grayscale image from which the features are to be extracted
     * @param normRows
     *            the number of rows into which the image will be normalized
     * @param normCols
     *            the number of columns into which the image will be normalized
     * @param outRows
     *            the number of rows of the final output image
     * @param outCols
     *            the number of columns of the final output image
     * @return a 1D array of type integer consisting the grayscale values of the
     *         images (column-wise)
     */
    public static double[][] getPixelsValues(Mat grayscaleImg, int normRows, int normCols, int outRows, int outCols) {
        // crop the image
        Mat cropImg = GrayImgProc.trimBorders(grayscaleImg);

        // normalize the image
        Mat normImg = new Mat(normRows, normCols, cropImg.type());
        // Imgproc.resize(cropImg, normImg, normImg.size(), 0, 0, Imgproc.INTER_AREA);
        normImg = GrayImgProc.normalizationF9(cropImg, normRows, normCols);

        // prepare adding the border
        Mat outImg = new Mat(outRows, outCols, grayscaleImg.type());
        int topBorder, bottomBorder;
        if ((outRows - normRows) % 2 == 0)
            topBorder = bottomBorder = (outRows - normRows) / 2;
        else {
            topBorder = (outRows - normRows) / 2;
            bottomBorder = topBorder + 1;
        }
        int leftBorder, rightBorder;
        if ((outCols - normCols) % 2 == 0)
            leftBorder = rightBorder = (outCols - normCols) / 2;
        else {
            leftBorder = (outCols - normCols) / 2;
            rightBorder = leftBorder + 1;
        }
        Core.copyMakeBorder(normImg, outImg, topBorder, bottomBorder, leftBorder, rightBorder, Core.BORDER_CONSTANT,
                new Scalar(255));

        // get the features in 1D array
        int[] tempFeatures = new int[outRows * outCols];
        outImg.convertTo(outImg, CvType.CV_32SC1);
        outImg.get(0, 0, tempFeatures);
        double[][] features = new double[outRows * outCols][1];
        // for each columns
        for (int i = 0; i < outImg.cols(); i++)
            // for each rows
            for (int ii = 0; ii < outImg.rows(); ii++)
                // mean = ((1 - mean/255) - 0.5f)*2;
                features[i * outImg.rows() + ii][0] = ((1 - tempFeatures[ii * outImg.cols() + i] / 255d) - 0.5d) * 2;

        // Imgcodecs.imwrite("./res/temp.png", outImg);
        // GrayImgProc.matToTxt(outImg, "./res/temp.txt");

        tempFeatures = null;
        normImg = outImg = null;
        return features;
    }

    /**
     * Apply L2-norm to the given vector
     * 
     * @param featureVector
     *            the vector of features to be normalized using L2-norm
     * @return a vector which is the result from applying L2-norm to the given
     *         vector
     */
    public static Mat l2Norm(Mat featureVector) {
        // calculating the denumerator
        float norm = (float) featureVector.dot(featureVector) + E_NORM * E_NORM;
        norm = (float) Math.sqrt(norm);

        // normalize
        Mat normVector = new Mat(featureVector.rows(), featureVector.cols(), featureVector.type());
        Core.multiply(featureVector, new Scalar(1 / norm), normVector);
        return normVector;
    }

    public static String getTextureName(int textureType) {
        switch (textureType) {
            case TEXTURE_CONTOUR:
                return "contour";
            case TEXTURE_ORIGINAL:
                return "original";
            case TEXTURE_THINNING:
                return "thinning";
            default:
                return "undefined";
        }
    }
}

/*
 * //get only the contours List<MatOfPoint> contours = new
 * ArrayList<MatOfPoint>(); Mat hierarchy = new Mat();
 * Imgproc.findContours(normalized, contours, hierarchy, Imgproc.RETR_LIST,
 * Imgproc.CHAIN_APPROX_SIMPLE); Collections.sort(contours,
 * FormProc.COMPARATOR_CONTOUR_AREA); int imgArea = normalized.cols() *
 * normalized.rows(); if(Math.abs(Imgproc.contourArea(contours.get(0)) -
 * imgArea)/imgArea <= 0.2) contours.remove(0); Mat thinned =
 * Mat.ones(normalized.rows(), normalized.cols(), normalized.type());
 * Imgproc.drawContours(thinned, contours, -1, new Scalar(0));
 * Imgproc.threshold(thinned, thinned, 0, 255, Imgproc.THRESH_BINARY);
 * Imgproc.rectangle(thinned, new Point(0,0), new Point(thinned.cols()-1,
 * thinned.rows()-1), new Scalar(255));
 */
