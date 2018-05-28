package model.label;

public abstract class AnswerSheetLabel implements Comparable<AnswerSheetLabel> {

    public static final boolean SORT_BY_COLUMN = true;
    public static final boolean SORT_BY_ROW = false;

    private String rowInfo;
    private String colInfo;
    private boolean sortByColInfo;

    public AnswerSheetLabel(String colInfo, String rowInfo, boolean sortByColInfo) {
        setColInfo(colInfo);
        setRowInfo(rowInfo);
        setSortByColInfo(sortByColInfo);
    }

    public AnswerSheetLabel(String colInfo, String rowInfo) {
        setColInfo(colInfo);
        setRowInfo(rowInfo);
        setSortByColInfo(false);
    }

    private void setSortByColInfo(boolean sortByColInfo) {
        this.sortByColInfo = sortByColInfo;
    }

    private void setRowInfo(String rowInfo) {
        this.rowInfo = rowInfo;
    }

    public void setColInfo(String colInfo) {
        this.colInfo = colInfo;
    }

    public final String getRowInfo() {
        return this.rowInfo;
    }

    public final String getColInfo() {
        return this.colInfo;
    }

    @Override
    public String toString() {
        return "{row:" + getRowInfo() + ",col:" + getColInfo() + "}";
    }

    @Override
    public int compareTo(AnswerSheetLabel o) {
        int classNameComparison = this.getClass().getSimpleName().compareTo(o.getClass().getSimpleName());
        int colComparison = this.getColInfo().compareTo(o.getColInfo());
        int rowComparison = this.getRowInfo().compareTo(o.getRowInfo());
        if (classNameComparison == 0) {
            if ((sortByColInfo ? colComparison : rowComparison) == 0) {
                if ((sortByColInfo ? rowComparison : colComparison) == 0) {
                    return 0;
                } else
                    return (sortByColInfo ? rowComparison : colComparison);
            } else
                return (sortByColInfo ? colComparison : rowComparison);
        } else
            return classNameComparison;
    }
}
