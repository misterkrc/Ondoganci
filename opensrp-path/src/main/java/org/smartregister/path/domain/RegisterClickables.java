package org.smartregister.path.domain;

import java.io.Serializable;

public class RegisterClickables implements Serializable {

    private boolean recordWeight;

    private boolean recordHC;

    private boolean recordAll;

    public void setRecordWeight(boolean recordWeight) {
        this.recordWeight = recordWeight;
    }

    public void setRecordHC(boolean recordHC) {
        this.recordHC = recordHC;
    }

    public boolean isRecordWeight() {
        return recordWeight;
    }

    public boolean isRecordHC() {
        return recordHC;
    }

    public void setRecordAll(boolean recordAll) {
        this.recordAll = recordAll;
    }

    public boolean isRecordAll() {
        return recordAll;
    }
}
