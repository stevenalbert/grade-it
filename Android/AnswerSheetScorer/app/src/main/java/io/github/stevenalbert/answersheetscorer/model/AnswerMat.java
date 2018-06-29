package io.github.stevenalbert.answersheetscorer.model;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;

import model.label.AnswerSheetLabel;

public class AnswerMat extends Mat implements Comparable<AnswerMat> {

    private AnswerSheetLabel label;

    public AnswerMat(Mat mat, AnswerSheetLabel label) {
        super(invertMat(mat), Range.all());
        this.label = label;
    }

    private static Mat invertMat(Mat mat) {
        Mat invertedMat = new Mat(mat.rows(), mat.cols(), mat.type());
        Core.bitwise_not(mat, invertedMat);
        return invertedMat;
    }

    public AnswerSheetLabel getLabel() {
        return label;
    }

    @Override
    public int compareTo(AnswerMat o) {
        return this.label.compareTo(o.getLabel());
    }
}
