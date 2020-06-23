package org.smartregister.path.service.intent.path;

import android.content.Intent;

import org.smartregister.growthmonitoring.service.intent.HeadCIntentService;
import org.smartregister.path.receiver.VaccinatorAlarmReceiver;

public class PathHeadCIntentService extends HeadCIntentService {

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        VaccinatorAlarmReceiver.completeWakefulIntent(intent);
    }
}
