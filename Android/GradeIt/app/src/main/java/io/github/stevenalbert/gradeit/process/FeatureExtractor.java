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
     * create a 11x11 mask to detect "X" symbol as follows: </br>
     * 1,1,0,-3,-8,-15,-8,-3,0,1,1</br>
     * 1,1,1,0,-3,-8,-3,0,1,1,1</br>
     * 0,1,1,1,0,-3,0,1,1,1,0</br>
     * -3,0,1,1,1,0,1,1,1,0,-3</br>
     * -8,-3,0,1,1,1,1,1,0,-3,-8</br>
     * -15,-8,-3,0,1,1,1,0,-3,-8,-15</br>
     * -8,-3,0,1,1,1,1,1,0,-3,-8</br>
     * -3,0,1,1,1,0,1,1,1,0,-3</br>
     * 0,1,1,1,0,-3,0,1,1,1,0</br>
     * 1,1,1,0,-3,-8,-3,0,1,1,1</br>
     * 1,1,0,-3,-8,-15,-8,-3,0,1,1</br>
     * 
     * @return a Mat object of size 11x11 containing the mask described above
     */
    private static Mat createNewMaskX11() {
        Mat mask = new Mat(11, 11, CvType.CV_8SC1);
        byte[] maskVal = new byte[] { 1, 1, -1, -5, -15, -15, -15, -5, -1, 1, 1, 1, 2, 1, -1, -5, -15, -5, -1, 1, 2, 1,
                -1, 1, 2, 1, -1, -5, -1, 1, 2, 1, -1, -5, -1, 1, 2, 1, -1, 1, 2, 1, -1, -5, -15, -5, -1, 1, 2, 1, 2, 1,
                -1, -5, -15, -15, -15, -5, -1, 1, 2, 1, -1, -5, -15, -15, -15, -5, -1, 1, 2, 1, 2, 1, -1, -5, -15, -5,
                -1, 1, 2, 1, -1, 1, 2, 1, -1, -5, -1, 1, 2, 1, -1, -5, -1, 1, 2, 1, -1, 1, 2, 1, -1, -5, -15, -5, -1, 1,
                2, 1, 1, 1, -1, -5, -15, -15, -15, -5, -1, 1, 1 };

        mask.put(0, 0, maskVal);
        return mask;
    }
}