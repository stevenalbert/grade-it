package io.github.stevenalbert.gradeit.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AnswerSheetMetadata {

    public class Value {
        public String label;
        public int startVerticalIndex;
        public int endVerticalIndex;
        public int startHorizontalIndex;
        public int endHorizontalIndex;
        public int columnCount;
        public int rowCount;
        public char startColumnChar;
        public int startRowInteger;

        public Value(String label, int startVerticalIndex, int endVerticalIndex, int startHorizontalIndex,
                int endHorizontalIndex, int columnCount, int rowCount, int startRowInteger, char startColumnChar) {
            this.label = label;
            this.startVerticalIndex = startVerticalIndex;
            this.endVerticalIndex = endVerticalIndex;
            this.startHorizontalIndex = startHorizontalIndex;
            this.endHorizontalIndex = endHorizontalIndex;
            this.columnCount = columnCount;
            this.rowCount = rowCount;
            this.startRowInteger = startRowInteger;
            this.startColumnChar = startColumnChar;
        }
    }

    public class PaperDimension {
        public int width;
        public int height;
        public int squareWidth;
        public int squareHeight;
        public int squareAnswerBorder;

        public PaperDimension(int width, int height, int squareWidth, int squareHeight, int squareAnswerBorder,
                double scale) {
            this.width = (int) Math.round((double) width * scale);
            this.height = (int) Math.round((double) height * scale);
            this.squareWidth = (int) Math.round((double) squareWidth * scale);
            this.squareHeight = (int) Math.round((double) squareHeight * scale);
            this.squareAnswerBorder = (int) Math.round((double) squareAnswerBorder * scale);
        }
    }

    private static final String METADATA_EXTENSION = "asmf";

    private static final String DIMENSION_FORMAT_KEY = "Dim";

    private static final String DIMENSION_FORMAT = "\"" + DIMENSION_FORMAT_KEY
            + ", __WIDTH__, __HEIGHT__, __SQ_WIDTH__, __SQ_HEIGHT__, __SQ_ANS_BORDER__, __SCALE__\" (without quotes and fill __ATTR__ with number correspond to its attr)";

    private PaperDimension dimension;

    private ArrayList<Value> values;

    public AnswerSheetMetadata(InputStream metadataInputStream) {
        readMetadataFile(metadataInputStream);
    }

    private void readMetadataFile(InputStream metadataInputStream) {
        if (metadataInputStream == null)
            throw new IllegalArgumentException("Metadata cannot be null");

        values = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(metadataInputStream));
        String metadata = null;
        try {
            while ((metadata = reader.readLine()) != null) {
                if (isCommentOrEmpty(metadata))
                    continue;

                PaperDimension dim = processPaperDimension(metadata);
                if (dimension == null && dim != null)
                    dimension = dim;
                else if (dimension != null && dim != null)
                    throw new IllegalArgumentException("There is more than one lines which is started by \""
                            + DIMENSION_FORMAT_KEY + "\" (without quotes)");
                else if (dim == null) {
                    Value value = processMetadataLine(metadata);
                    if (value != null) {
                        values.add(value);
                    }
                }
            }

            if (dimension == null)
                throw new IllegalArgumentException("There is no dimension row. Expected once: " + DIMENSION_FORMAT);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                System.out.println("Metadata reader is not closed");
            }
        }
    }

    public Value getValue(int index) {
        return values.get(index);
    }

    public PaperDimension getDimension() {
        return dimension;
    }

    public int getValueLength() {
        return values.size();
    }

    private boolean isCommentOrEmpty(String metadata) {
        return metadata.startsWith("#") || metadata.trim().length() == 0;
    }

    private PaperDimension processPaperDimension(String metadata) {
        if (!metadata.startsWith("Dim"))
            return null;

        String[] data = metadata.split("\\s*,\\s*");
        int expectedTotalData = PaperDimension.class.getDeclaredFields().length
                - PaperDimension.class.getDeclaredMethods().length
                - PaperDimension.class.getDeclaredConstructors().length + 2; // Latest +2 is for scale attribute and
                                                                             // "Dim" string
        if (data.length != expectedTotalData) {
            throw new IllegalArgumentException("\"" + metadata + "\", format is not true.\nTotal data: " + data.length
                    + ", expected total data: " + expectedTotalData + "\nFormat:\n\t" + DIMENSION_FORMAT);
        }

        for (int i = 0; i < data.length; i++) {
            data[i].trim();
        }

        return new PaperDimension(Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3]),
                Integer.valueOf(data[4]), Integer.valueOf(data[5]), Double.valueOf(data[6]));
    }

    private Value processMetadataLine(String metadata) {
        String[] data = metadata.split(",\\s*");
        int expectedTotalData = Value.class.getDeclaredFields().length - Value.class.getDeclaredMethods().length
                - Value.class.getDeclaredConstructors().length;
        if (data.length != expectedTotalData) {
            throw new IllegalArgumentException("\"" + metadata + "\", format is not true.\nTotal data: " + data.length
                    + ", expected total data: " + expectedTotalData);
        }

        for (int i = 0; i < data.length; i++) {
            data[i].trim();
        }

        return new Value(data[0], Integer.valueOf(data[1]), Integer.valueOf(data[2]), Integer.valueOf(data[3]),
                Integer.valueOf(data[4]), Integer.valueOf(data[5]), Integer.valueOf(data[6]), Integer.valueOf(data[7]),
                Character.valueOf(data[8].charAt(0)));
    }
}
