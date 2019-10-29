package org.smartregister.path.service.intent.path;

import android.content.Intent;

import org.smartregister.growthmonitoring.service.intent.ZScoreRefreshIntentService;
import org.smartregister.path.receiver.VaccinatorAlarmReceiver;

/**
 * Created by keyman on 4/16/2018.
 */

public class PathZScoreRefreshIntentService extends ZScoreRefreshIntentService {

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        VaccinatorAlarmReceiver.completeWakefulIntent(intent);
    }
}

