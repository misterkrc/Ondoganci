package org.smartregister.growthmonitoring.util;

import org.smartregister.growthmonitoring.domain.HeadCircumference;
import org.smartregister.util.DateUtil;

public class HeadCircumferenceUtils {

    public static boolean lessThanThreeMonths(HeadCircumference headCircumference) {
        ////////////////////////check 3 months///////////////////////////////
        return headCircumference == null || headCircumference.getCreatedAt() == null || !DateUtil.checkIfDateThreeMonthsOlder(headCircumference.getCreatedAt());
        ///////////////////////////////////////////////////////////////////////
    }
}
