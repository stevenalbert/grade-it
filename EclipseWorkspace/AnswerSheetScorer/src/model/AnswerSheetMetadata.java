package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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

    private ArrayList<Value> values;

    public AnswerSheetMetadata(String filePath) {
        File metadataFile = new File(filePath);
        readMetadataFile(metadataFile);
    }

    public AnswerSheetMetadata(File metadataFile) {
        readMetadataFile(metadataFile);
    }

    private void readMetadataFile(File metadataFile) {
        if (!isMetadataFileValid(metadataFile))
            throw new IllegalArgumentException("Metadata file is invalid");

        values = new ArrayList<>();
        BufferedReader reader = null;
        String metadata;
        try {
            reader = new BufferedReader(new FileReader(metadataFile));
            while ((metadata = reader.readLine()) != null) {
                Value value = processMetadataLine(metadata);
                if (value != null) {
                    values.add(value);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Value getValue(int index) {
        return values.get(index);
    }

    public int getValueLength() {
        return values.size();
    }

    private Value processMetadataLine(String metadata) {
        if (metadata.startsWith("#") || metadata.trim().length() == 0) {
            return null;
        }

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

    private boolean isMetadataFileValid(File metadataFile) {
        return metadataFile != null && metadataFile.exists() && metadataFile.isFile()
                && metadataFile.getName().matches(".+\\.asmf");
    }
}
