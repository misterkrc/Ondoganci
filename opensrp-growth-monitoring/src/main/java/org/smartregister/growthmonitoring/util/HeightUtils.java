package org.smartregister.growthmonitoring.util;

import org.smartregister.growthmonitoring.domain.Height;
import org.smartregister.util.DateUtil;

public class HeightUtils {

    public static boolean lessThanThreeMonths(Height height) {
        ////////////////////////check 3 months///////////////////////////////
        return height == null || height.getCreatedAt() == null ||
                !DateUtil.checkIfDateThreeMonthsOlder(height.getCreatedAt());
        ///////////////////////////////////////////////////////////////////////
    }
}
