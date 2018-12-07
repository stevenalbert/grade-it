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

        final int TOTAL_NUMBER = answerKey.getTotalNumber();
        final int TOTAL_ANSWER_SHEET = answerSheetList.size();

        StringBuilder summaryBuilder = new StringBuilder();
        List<EnumMap<Option, Integer>> totalOptionsOfNumber;
        List<int[]> verdictList = new ArrayList<>();
        List<Integer> trueAnswerTotalList = new ArrayList<>();

        // Create table header
        appendWithSeparator(summaryBuilder, "MCode");
        appendWithSeparator(summaryBuilder, "ExCode");

        for(int i = 1; i <= TOTAL_NUMBER; i++) {
            appendWithSeparator(summaryBuilder, String.valueOf(i));
        }
        appendWithSeparator(summaryBuilder, "Result");
        summaryBuilder.append(LINE_SEPARATOR);
        // End of table header

        // Add Answer key
        appendWithSeparator(summaryBuilder, answerKey.getMCodeString());
        appendWithSeparator(summaryBuilder, "000");
        String optionsString = AnswerKeyConverter.optionsToString(answerKey.getAnswerKeys());
        for(int i = 0; i < TOTAL_NUMBER; i++) {
            appendWithSeparator(summaryBuilder, String.valueOf(optionsString.charAt(i)));
        }
        appendWithSeparator(summaryBuilder, "KEY");
        summaryBuilder.append(LINE_SEPARATOR);
        // End of answer key

        // Initialize option statistic
        totalOptionsOfNumber = new ArrayList<>(TOTAL_NUMBER);
        Option[] allOptions = Option.values();
        for(int i = 0; i < TOTAL_NUMBER; i++) {
            totalOptionsOfNumber.add(new EnumMap<>(Option.class));
            for(Option option : allOptions) {
                totalOptionsOfNumber.get(i).put(option, 0);
            }
        }

        // Add Answer sheets
        for(AnswerSheet answerSheet : answerSheetList) {
            // Per answer sheet
            appendWithSeparator(summaryBuilder, answerSheet.getMCodeString());
            appendWithSeparator(summaryBuilder, answerSheet.getExCodeString());
            verdictList.add(answerSheet.getVerdicts());
            int trueAnswerTotal = 0;
            for (int number = 1; number <= TOTAL_NUMBER; number++) {
                String answerString = AnswerSheetConverter.answerToString(answerSheet.getAnswerOn(number));
                appendWithSeparator(summaryBuilder, answerString);
                trueAnswerTotal += answerSheet.isAnswerTrue(number) ? 1 : 0;
                for(int i=0; i<answerString.length(); i++) {
                    Option option = Option.getOption(answerString.charAt(i));
                    totalOptionsOfNumber.get(number - 1).put(option, totalOptionsOfNumber.get(number - 1).get(option) + 1);
                }
            }
            trueAnswerTotalList.add(trueAnswerTotal);
            appendWithSeparator(summaryBuilder, String.valueOf(trueAnswerTotal));
            summaryBuilder.append(LINE_SEPARATOR);
            // End of answer sheet
        }
        // End of answer sheets

        // Data for analysis
        double[] correlationValues = new double[TOTAL_NUMBER];

        int[][] verdictArray = new int[TOTAL_NUMBER][TOTAL_ANSWER_SHEET];
        int[] totalCorrectArray = new int[TOTAL_ANSWER_SHEET];

        for(int i = 0; i < answerSheetList.size(); i++) {
            for(int number = 1; number <= TOTAL_NUMBER; number++) {
                verdictArray[number - 1][i] = answerSheetList.get(i).isAnswerTrue(number) ? 1 : 0;
            }
            totalCorrectArray[i] = answerSheetList.get(i).getTotalCorrect();
        }


        // Calculate validity
        for(int i = 0; i < TOTAL_NUMBER; i++) {
            correlationValues[i] = correlation(verdictArray[i], totalCorrectArray);
        }
        // Add correlation
        appendWithSeparator(summaryBuilder, ""); // For MCode column
        appendWithSeparator(summaryBuilder, "Validity score"); // For ExCode column
        for(int i = 0; i < correlationValues.length; i++) {
            appendWithSeparator(summaryBuilder, new Double(correlationValues[i]).isNaN() ? "---" :
                    String.format(Locale.getDefault(), "%.2f", correlationValues[i]));
        }
        appendWithSeparator(summaryBuilder, ""); // For Result column
        summaryBuilder.append(LINE_SEPARATOR);
        // End of validity

        // Calculate reliability
        int[] score = new int[answerSheetList.size()];
        int[] correctAnswerOnNumber = new int[TOTAL_NUMBER];
        double[] p = new double[TOTAL_NUMBER];
        double[] q = new double[TOTAL_NUMBER];
        double[] pq = new double[TOTAL_NUMBER];
        double sumOfPQ = 0, KR20Value;

        for(int i = 0; i < score.length; i++) {
            score[i] = answerSheetList.get(i).getTotalCorrect();
        }
        double variance = variance(score);

        // Calculate correct answers
        for (int i = 0; i < TOTAL_NUMBER; i++) {
            correctAnswerOnNumber[i] = (int) sum(verdictArray[i]);
            p[i] = ((double) correctAnswerOnNumber[i]) / (double) TOTAL_ANSWER_SHEET;
            q[i] = 1 - p[i];
            pq[i] = p[i] * q[i];
            sumOfPQ += pq[i];
        }

        KR20Value = (((double) TOTAL_NUMBER) / (double) (TOTAL_NUMBER - 1)) * (1 - sumOfPQ / variance);
        // End of reliability

        // Calculate item discriminator
        // End of item discriminator

        // Calculate difficulty level
        double diffLevel;
        appendWithSeparator(summaryBuilder, "");
        appendWithSeparator(summaryBuilder, "Difficulty level");
        for(int i = 0; i < TOTAL_NUMBER; i++) {
            diffLevel = 1.0 - ((double) correctAnswerOnNumber[i]) / (double) TOTAL_ANSWER_SHEET;
            appendWithSeparator(summaryBuilder, String.format("%.2f", diffLevel));
        }
        summaryBuilder.append(LINE_SEPARATOR);
        // End of difficulty level

        // Output reliability
        summaryBuilder.append(LINE_SEPARATOR);
        summaryBuilder.append(LINE_SEPARATOR);
        appendWithSeparator(summaryBuilder, "");
        appendWithSeparator(summaryBuilder, "Reliability score");
        appendWithSeparator(summaryBuilder, String.format("%.2f", KR20Value));
//        appendWithSeparator(summaryBuilder, reliabilityResult(KR20Value));
        summaryBuilder.append(LINE_SEPARATOR);
        // End of output reliability


        // Add options statistics
        summaryBuilder.append(LINE_SEPARATOR);
        for(Option option : allOptions) {
            appendWithSeparator(summaryBuilder, ""); // For MCode column
            appendWithSeparator(summaryBuilder, "Total " + option.getOption().toString()); // For ExCode column
            for(int i = 0; i < totalOptionsOfNumber.size(); i++) {
                appendWithSeparator(summaryBuilder, String.valueOf(totalOptionsOfNumber.get(i).get(option)));
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

    private static double correlation(int[] x, int[] y) {
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

    private static double variance(int[] x) {
        double  sum = 0,
                sumOfSquare = 0,
                mean;

        for (int v : x) {
            sum += v;
            sumOfSquare += v * v;
        }

        mean = sum / (double) x.length;

        return (sumOfSquare / (double) x.length - mean * mean);
    }

    private static double sum(int[] x) {
        double sum = 0;
        for (int v : x) {
            sum += v;
        }
        return sum;
    }

    private static String reliabilityResult(double reliabilityScore) {
        if(reliabilityScore >= 0.9) return "Excellent";
        else if(reliabilityScore >= 0.8) return "Good";
        else if(reliabilityScore >= 0.7) return "Acceptable";
        else if(reliabilityScore >= 0.6) return "Questionable";
        else if(reliabilityScore >= 0.5) return "Poor";
        else return "Unacceptable";
    }
}
