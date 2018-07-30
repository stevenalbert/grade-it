package io.github.stevenalbert.gradeit.process;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * A class used as a collection of feature extraction method used mainly for 2D
 * grayscale image
 * 
 * @author DATACLIM-PIKU2
 *
 */
public class FeatureExtractor {
    private static final int X_NORM_SIZE = 11;

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
    public static double getFeatureX(Mat imgThreshold) {
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
        final int CENTER_SIZE = 5;
        Mat maskCenter = new Mat(CENTER_SIZE, CENTER_SIZE, CvType.CV_8SC1);
        byte[] maskCenterValue = new byte[] { 2, 1, 0, 1, 2, 1, 4, 3, 4, 1, 0, 3, 6, 3, 0, 1, 4, 3, 4, 1, 2, 1, 0, 1, 2 };
        maskCenter.put(0, 0, maskCenterValue);

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
        Mat imgTemp;
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

        Mat imgShifted = Mat.zeros(X_NORM_SIZE, X_NORM_SIZE, CvType.CV_8UC1);
        imgNorm.convertTo(imgNorm, CvType.CV_8UC1);
        Imgproc.warpAffine(imgNorm, imgShifted, transformMatrix, imgShifted.size());

        // =====================================================
        // masking to find the result of convolution with X-mask
        // =====================================================
        // create the mask, then dot the features
        // Mat mask = createMaskX11();
        Mat mask = createNewMaskX11();
        imgShifted.convertTo(imgShifted, CvType.CV_8SC1);
        double result = imgShifted.dot(mask);

        return result;
    }

    /**
     * create a 11x11 mask to detect "X" symbol as follows: </br>
     *  1,  1,  0, -9, -9, -9, -9, -9,  0,  1,  1 </br>
     *  1,  1,  1,  0, -9, -9, -9,  0,  1,  1,  1 </br>
     *  0,  1,  1,  1,  0, -9,  0,  1,  1,  1,  0 </br>
     * -9,  0,  1,  1,  1,  0,  1,  1,  1,  0, -9 </br>
     * -9, -9,  0,  1,  1,  1,  1,  1,  0, -9, -9 </br>
     * -9, -9, -9,  0,  1,  1,  1,  0, -9, -9, -9 </br>
     * -9, -9,  0,  1,  1,  1,  1,  1,  0, -9, -9 </br>
     * -9,  0,  1,  1,  1,  0,  1,  1,  1,  0, -9 </br>
     *  0,  1,  1,  1,  0, -9,  0,  1,  1,  1,  0 </br>
     *  1,  1,  1,  0, -9, -9, -9,  0,  1,  1,  1 </br>
     *  1,  1,  0, -9, -9, -9, -9, -9,  0,  1,  1 </br>
     *
     * @return a Mat object of size 11x11 containing the mask described above
     */
    private static Mat createMaskX11() {
        Mat mask = Mat.zeros(11, 11, CvType.CV_8SC1);
        byte[] maskVal = new byte[] {
                1, 1, 0, -9, -9, -9, -9, -9, 0, 1, 1,
                1, 1, 1, 0, -9, -9, -9, 0, 1, 1, 1,
                0, 1, 1, 1, 0, -9, 0, 1, 1, 1, 0,
                -9, 0, 1, 1, 1, 0, 1, 1, 1, 0, -9,
                -9, -9, 0, 1, 1, 1, 1, 1, 0, -9, -9,
                -9, -9, -9, 0, 1, 1, 1, 0, -9, -9, -9,
                -9, -9, 0, 1, 1, 1, 1, 1, 0, -9, -9,
                -9, 0, 1, 1, 1, 0, 1, 1, 1, 0, -9,
                0, 1, 1, 1, 0, -9, 0, 1, 1, 1, 0,
                1, 1, 1, 0, -9, -9, -9, 0, 1, 1, 1,
                1, 1, 0, -9, -9, -9, -9, -9, 0, 1, 1 };

        mask.put(0, 0, maskVal);

        return mask;
    }

    /**
     * create a 11x11 mask to detect "X" symbol as follows: </br>
     *   2,  1, -1, -7,-15,-15,-15, -7, -1,  1,  2, </br>
     *   1,  2,  1, -1, -7,-15, -7, -1,  1,  2,  1, </br>
     *  -1,  1,  2,  1, -1, -7, -1,  1,  2,  1, -1, </br>
     *  -7, -1,  1,  2,  1, -1,  1,  2,  1, -1, -7, </br>
     * -15, -7, -1,  1,  2,  1,  2,  1, -1, -7,-15, </br>
     * -15,-15, -7, -1,  1,  2,  1, -1, -7,-15,-15, </br>
     * -15, -7, -1,  1,  2,  1,  2,  1, -1, -7,-15, </br>
     *  -7, -1,  1,  2,  1, -1,  1,  2,  1, -1, -7, </br>
     *  -1,  1,  2,  1, -1, -7, -1,  1,  2,  1, -1, </br>
     *   1,  2,  1, -1, -7,-15, -7, -1,  1,  2,  1, </br>
     *   2,  1, -1, -7,-15,-15,-15, -7, -1,  1,  2, </br>
     *
     * @return a Mat object of size 11x11 containing the mask described above
     */
    private static Mat createNewMaskX11() {
        Mat mask = new Mat(11, 11, CvType.CV_8SC1);
        byte[] maskVal = new byte[] {
                2, 1, -1, -7, -15, -15, -15, -7, -1, 1, 2,
                1, 2, 1, -1, -7, -15, -7, -1, 1, 2, 1,
                -1, 1, 2, 1, -1, -7, -1, 1, 2, 1, -1,
                -7, -1, 1, 2, 1, -1, 1, 2, 1, -1, -7,
                -15, -7, -1, 1, 2, 1, 2, 1, -1, -7, -15,
                -15, -15, -7, -1, 1, 2, 1, -1, -7, -15, -15,
                -15, -7, -1, 1, 2, 1, 2, 1, -1, -7, -15,
                -7, -1, 1, 2, 1, -1, 1, 2, 1, -1, -7,
                -1, 1, 2, 1, -1, -7, -1, 1, 2, 1, -1,
                1, 2, 1, -1, -7, -15, -7, -1, 1, 2, 1,
                2, 1, -1, -7, -15, -15, -15, -7, -1, 1, 2
        };
        mask.put(0, 0, maskVal);
        return mask;
    }
}