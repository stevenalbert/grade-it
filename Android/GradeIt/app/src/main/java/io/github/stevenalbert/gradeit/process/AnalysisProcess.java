package io.github.stevenalbert.gradeit.process;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import io.github.stevenalbert.gradeit.model.AnswerKey;
import io.github.stevenalbert.gradeit.model.AnswerKeyConverter;
import io.github.stevenalbert.gradeit.model.AnswerSheet;
import io.github.stevenalbert.gradeit.model.AnswerSheetConverter;
import io.github.stevenalbert.gradeit.model.Option;

/**
 * Created by Steven Albert on 7/10/2018.
 */
public class AnalysisProcess {

    private static final String COLUMN_SEPARATOR = ",";
    private static final String LINE_SEPARATOR = "\n";

    public static String getAnswersSummary(List<AnswerSheet> answerSheetList, AnswerKey answerKey) throws Exception {
        // Check existance of answer sheets and answer key
        if(answerKey == null)
            throw new Exception("Answer key must be exist");
        if(answerSheetList == null || answerSheetList.size() == 0)
            throw new Exception("Must have at least 1 answer sheets");

        // Check whether the MCode is the same from all of answer sheets and answer key
        int expectedMCode = answerKey.getMCode();
        for(AnswerSheet answerSheet : answerSheetList) {
            if(answerSheet.getMCode() != expectedMCode) {
                throw new Exception("Multiple MCode detected, abort process");
            }
        }

        StringBuilder summaryBuilder = new StringBuilder();
        EnumMap<Option, Integer>[] totalOptionsOfNumber;
        List<int[]> verdictList = new ArrayList<>();
        List<Integer> trueAnswerTotalList = new ArrayList<>();

        // Create table header
        appendWithSeparator(summaryBuilder, "MCode");
        appendWithSeparator(summaryBuilder, "ExCode");
        int totalNumber = answerKey.getAnswerKeys().length;

        for(int i = 1; i <= totalNumber; i++) {
            appendWithSeparator(summaryBuilder, String.valueOf(i));
        }
        appendWithSeparator(summaryBuilder, "Result");
        summaryBuilder.append(LINE_SEPARATOR);
        // End of table header

        // Add Answer key
        appendWithSeparator(summaryBuilder, answerKey.getMCodeString());
        appendWithSeparator(summaryBuilder, "000");
        String optionsString = AnswerKeyConverter.optionsToString(answerKey.getAnswerKeys());
        for(int i = 0; i < optionsString.length(); i++) {
            appendWithSeparator(summaryBuilder, String.valueOf(optionsString.charAt(i)));
        }
        appendWithSeparator(summaryBuilder, "KEY");
        summaryBuilder.append(LINE_SEPARATOR);
        // End of answer key

        // Initialize option statistic
        totalOptionsOfNumber = new EnumMap[totalNumber];
        Option[] allOptions = Option.values();
        for(int i = 0; i < totalOptionsOfNumber.length; i++) {
            totalOptionsOfNumber[i] = new EnumMap<>(Option.class);
            for(Option option : allOptions) {
                totalOptionsOfNumber[i].put(option, 0);
            }
        }

        // Add Answer sheets
        for(AnswerSheet answerSheet : answerSheetList) {
            // Per answer sheet
            appendWithSeparator(summaryBuilder, answerSheet.getMCodeString());
            appendWithSeparator(summaryBuilder, answerSheet.getExCodeString());
            verdictList.add(answerSheet.getVerdicts());
            int trueAnswerTotal = 0;
            for (int number = 1; number <= totalNumber; number++) {
                String answerString = AnswerSheetConverter.answerToString(answerSheet.getAnswerOn(number));
                appendWithSeparator(summaryBuilder, answerString);
                trueAnswerTotal += answerSheet.isAnswerTrue(number) ? 1 : 0;
                if(answerString.length() > 0) {
                    Option option = Option.getOption(answerString.charAt(0));
                    totalOptionsOfNumber[number - 1].put(option, totalOptionsOfNumber[number - 1].get(option) + 1);
                }
            }
            trueAnswerTotalList.add(trueAnswerTotal);
            appendWithSeparator(summaryBuilder, String.valueOf(trueAnswerTotal));
            summaryBuilder.append(LINE_SEPARATOR);
            // End of answer sheet
        }
        // End of answer sheets

        // Add correlation
        appendWithSeparator(summaryBuilder, ""); // For MCode column
        appendWithSeparator(summaryBuilder, ""); // For ExCode column
        double[] corrValue = getAnswerCorrelations(answerSheetList);
        for(int i = 0; i < corrValue.length; i++) {
            appendWithSeparator(summaryBuilder, String.format(Locale.getDefault(), "%.2f", corrValue[i]));
        }
        appendWithSeparator(summaryBuilder, ""); // For Result column
        summaryBuilder.append(LINE_SEPARATOR);
        summaryBuilder.append(LINE_SEPARATOR);
        summaryBuilder.append(LINE_SEPARATOR);
        // End of correlation

        // Add options statistics
        for(Option option : allOptions) {
            appendWithSeparator(summaryBuilder, ""); // For MCode column
            appendWithSeparator(summaryBuilder, option.getOption().toString()); // For ExCode column
            for(int i = 0; i < totalOptionsOfNumber.length; i++) {
                appendWithSeparator(summaryBuilder, String.valueOf(totalOptionsOfNumber[i].get(option)));
            }
            appendWithSeparator(summaryBuilder, ""); // For Result column
            summaryBuilder.append(LINE_SEPARATOR);
        }

        return summaryBuilder.toString();
    }

    private static void appendWithSeparator(StringBuilder appendedTo, String appendedString) {
        appendedTo.append(appendedString);
        appendedTo.append(COLUMN_SEPARATOR);
    }

    private static double[] getAnswerCorrelations(List<AnswerSheet> answerSheets) {
        int totalNumber = answerSheets.get(0).getTotalAnswer();
        int totalAnswerSheet = answerSheets.size();
        double[] correlationValues = new double[totalNumber];

        int[][] verdictArray = new int[totalNumber][totalAnswerSheet];
        int[] totalCorrectArray = new int[totalAnswerSheet];

        for(int i = 0; i < answerSheets.size(); i++) {
            for(int number = 1; number <= totalNumber; number++) {
                verdictArray[number - 1][i] = answerSheets.get(i).isAnswerTrue(number) ? 1 : 0;
            }
            totalCorrectArray[i] = answerSheets.get(i).getTotalCorrect();
        }

/*
        TODO: Insert correlation formula here
*/
        // Calculate correlation value
        for(int i = 0; i < totalNumber; i++) {
            correlationValues[i] = getCorrelation(verdictArray[i], totalCorrectArray);
        }
        // Finish calculation

        return correlationValues;
    }

    private static double getCorrelation(int[] x, int[] y) {
        if(x.length != y.length)
            throw new IllegalArgumentException("x and y must have the same length");

        double  sumOfXY = 0,
                sumOfX = 0,
                sumOfY = 0,
                sumOfSquareX = 0,
                sumOfSquareY = 0,
                n = x.length;

        for(int i = 0; i < n; i++) {
            sumOfXY += x[i] * y[i];
            sumOfX += x[i];
            sumOfY += y[i];
            sumOfSquareX += x[i] * x[i];
            sumOfSquareY += y[i] * y[i];
        }

        return (n * sumOfXY - sumOfX * sumOfY) /
                Math.sqrt((n * sumOfSquareX - sumOfX * sumOfX) * (n * sumOfSquareY - sumOfY * sumOfY));
    }
}
