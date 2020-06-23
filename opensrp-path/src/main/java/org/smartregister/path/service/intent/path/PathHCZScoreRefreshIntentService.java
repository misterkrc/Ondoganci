package org.smartregister.path.service.intent.path;

import android.content.Intent;

import org.smartregister.growthmonitoring.service.intent.HCZScoreRefreshIntentService;
import org.smartregister.path.receiver.VaccinatorAlarmReceiver;

public class PathHCZScoreRefreshIntentService extends HCZScoreRefreshIntentService {

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        VaccinatorAlarmReceiver.completeWakefulIntent(intent);
    }
}

