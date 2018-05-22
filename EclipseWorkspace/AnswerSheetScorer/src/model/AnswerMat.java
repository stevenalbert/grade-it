package model;

import org.opencv.core.Mat;
import org.opencv.core.Range;

import model.label.AnswerSheetLabel;

public class AnswerMat extends Mat {

    private AnswerSheetLabel label;

    public AnswerMat(Mat mat, AnswerSheetLabel label) {
        super(mat, Range.all());
    }

    public AnswerSheetLabel getLabel() {
        return label;
    }
}
