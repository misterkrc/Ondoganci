package org.smartregister.path.domain;

/**
 * Created by keyman on 16/11/2016.
 */
public class EditWrapper {
    private String currentValue;
    private String newValue;
    private String field;

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
