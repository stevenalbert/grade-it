package model.label;

public abstract class AnswerSheetLabel {

    private String rowInfo;
    private String colInfo;

    public AnswerSheetLabel(String colInfo, String rowInfo) {
        setColInfo(colInfo);
        setRowInfo(rowInfo);
    }

    private void setRowInfo(String rowInfo) {
        this.rowInfo = rowInfo;
    }

    public void setColInfo(String colInfo) {
        this.colInfo = colInfo;
    }

    public String getRowInfo() {
        return this.rowInfo;
    }

    public String getColInfo() {
        return this.colInfo;
    }

    @Override
    public String toString() {
        return "{row:" + getRowInfo() + ",col:" + getColInfo() + "}";
    }
}
