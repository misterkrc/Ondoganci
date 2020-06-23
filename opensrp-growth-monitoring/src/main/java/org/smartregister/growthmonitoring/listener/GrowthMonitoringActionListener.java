package org.smartregister.growthmonitoring.listener;

import org.smartregister.growthmonitoring.domain.HeightWrapper;
import org.smartregister.growthmonitoring.domain.WeightWrapper;

public interface GrowthMonitoringActionListener {

    void onGrowthRecorded(WeightWrapper weightWrapper, HeightWrapper heightWrapper);

}
