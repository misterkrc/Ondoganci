package org.smartregister.growthmonitoring.service.intent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.HeadCircumference;
import org.smartregister.growthmonitoring.repository.HeadCircumferenceRepository;
import org.smartregister.growthmonitoring.util.GMConstants;
import org.smartregister.growthmonitoring.util.JsonFormUtils;

import java.util.List;

public class HeadCIntentService extends IntentService {
    private static final String TAG = HeadCIntentService.class.getCanonicalName();
    public static final String EVENT_TYPE = "Growth Monitoring";
    public static final String EVENT_TYPE_OUT_OF_CATCHMENT = "Out of Area Service - Growth Monitoring";
    public static final String ENTITY_TYPE = "headCircumference";
    private HeadCircumferenceRepository headCircumferenceRepository;


    public HeadCIntentService() {
        super("HeadCService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            List<HeadCircumference> headCircumferences = headCircumferenceRepository.findUnSyncedBeforeTime(GMConstants.HC_SYNC_TIME);
            if (!headCircumferences.isEmpty()) {
                for (HeadCircumference headCircumference : headCircumferences) {

                    //Head Circumference
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(GMConstants.JsonForm.KEY, "Head_Circumference_Inches");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_ENTITY, "concept");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_ID, "5314AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_PARENT, "");
                    jsonObject.put(GMConstants.JsonForm.OPENMRS_DATA_TYPE, "decimal");
                    jsonObject.put(GMConstants.JsonForm.VALUE, headCircumference.getInch());

                    //Zscore
                    JSONObject zScoreObject = new JSONObject();
                    zScoreObject.put(GMConstants.JsonForm.KEY, "Z_Score_Head_Age");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_ENTITY, "concept");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_ID, "162584AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_ENTITY_PARENT, "");
                    zScoreObject.put(GMConstants.JsonForm.OPENMRS_DATA_TYPE, "calculation");
                    zScoreObject.put(GMConstants.JsonForm.VALUE, headCircumference.getZScore());

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    jsonArray.put(zScoreObject);

                    JsonFormUtils.createHCEvent(getApplicationContext(), headCircumference, EVENT_TYPE, ENTITY_TYPE, jsonArray);
                    if (headCircumference.getBaseEntityId() == null || headCircumference.getBaseEntityId().isEmpty()) {
                        JsonFormUtils.createHCEvent(getApplicationContext(), headCircumference, EVENT_TYPE_OUT_OF_CATCHMENT, ENTITY_TYPE, jsonArray);

                    }
                    headCircumferenceRepository.close(headCircumference.getId());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        headCircumferenceRepository = GrowthMonitoringLibrary.getInstance().headCircumferenceRepository();
        return super.onStartCommand(intent, flags, startId);
    }
}
