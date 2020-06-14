package org.smartregister.path.wrapper;

import org.smartregister.growthmonitoring.domain.HeadCircumference;

public class HCViewRecordUpdateWrapper extends BaseViewRecordUpdateWrapper {

    private HeadCircumference headCircumference;

    public HeadCircumference getHeadCircumference() {
        return headCircumference;
    }

    public void setHeadCircumference(HeadCircumference headCircumference) {
        this.headCircumference = headCircumference;
    }
}
