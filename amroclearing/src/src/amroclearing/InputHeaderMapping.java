package amroclearing;


public class InputHeaderMapping {
    private String fieldName;
    private int startIndex=-1;
    private int endIndex=-1;
    private int fieldLength=0;


    public enum Headers {
        REF, FIELDNAME, LENGTH, STARTINDEX, ENDINDEX
    }


    public InputHeaderMapping(String fieldName, Integer  startIndex, Integer endIndex, Integer fieldLength){
        this.fieldName= fieldName;
        this.startIndex= startIndex;
        this.endIndex= endIndex;
        this.fieldLength= fieldLength;

    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public int getFieldLength() {
        return fieldLength;
    }

    public void setFieldLength(int fieldLength) {
        this.fieldLength = fieldLength;
    }

}
