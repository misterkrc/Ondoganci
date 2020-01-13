package org.smartregister.path.domain;

import java.io.Serializable;

/**
 * Created by keyman on 24/02/2017.
 */
public class RegisterClickables implements Serializable {

    private boolean recordWeight;
    private boolean recordHeight;

    private boolean recordAll;

    public void setRecordWeight(boolean recordWeight) {
        this.recordWeight = recordWeight;
    }

    public boolean isRecordWeight() {
        return recordWeight;
    }
    public void setRecordHeight(boolean recordHeight) {
        this.recordHeight = recordHeight;
    }

    public boolean isRecordHeight() {
        return recordHeight;
    }

    public void setRecordAll(boolean recordAll) {
        this.recordAll = recordAll;
    }

    public boolean isRecordAll() {
        return recordAll;
    }
}
