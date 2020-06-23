package org.smartregister.growthmonitoring.util;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.domain.Weight;
import org.smartregister.growthmonitoring.domain.HeadCircumference;
import org.smartregister.repository.EventClientRepository;

import java.util.Date;

public class JsonFormUtils extends org.smartregister.util.JsonFormUtils {
    public static void createWeightEvent(Context context, Weight weight, String eventType, String entityType, JSONArray fields) {
        try {
            EventClientRepository db = GrowthMonitoringLibrary.getInstance().eventClientRepository();

            Event event = (Event) new Event()
                    .withBaseEntityId(weight.getBaseEntityId())
                    .withIdentifiers(weight.getIdentifiers())
                    .withEventDate(weight.getDate())
                    .withEventType(eventType)
                    .withLocationId(weight.getLocationId())
                    .withProviderId(weight.getAnmId())
                    .withEntityType(entityType)
                    .withFormSubmissionId(weight.getFormSubmissionId() == null ? generateRandomUUIDString() : weight.getFormSubmissionId())
                    .withDateCreated(new Date());


            if (fields != null && fields.length() != 0)
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject jsonObject = getJSONObject(fields, i);
                    String value = getString(jsonObject, VALUE);
                    if (StringUtils.isNotBlank(value)) {
                        addObservation(event, jsonObject);
                    }
                }


            if (event != null) {

                JSONObject eventJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(event));

                //check if an event already exists and update instead
                if (weight.getEventId() != null) {
                    JSONObject existingEvent = db.getEventsByEventId(weight.getEventId());
                    eventJson = merge(existingEvent, eventJson);
                }

                //merge if event exists
                db.addEvent(event.getBaseEntityId(), eventJson);

            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    public static void createHCEvent(Context context, HeadCircumference headCircumference, String eventType, String entityType, JSONArray fields) {
        try {
            EventClientRepository db = GrowthMonitoringLibrary.getInstance().eventClientRepository();

            Event event = (Event) new Event()
                    .withBaseEntityId(headCircumference.getBaseEntityId())
                    .withIdentifiers(headCircumference.getIdentifiers())
                    .withEventDate(headCircumference.getDate())
                    .withEventType(eventType)
                    .withLocationId(headCircumference.getLocationId())
                    .withProviderId(headCircumference.getAnmId())
                    .withEntityType(entityType)
                    .withFormSubmissionId(headCircumference.getFormSubmissionId() == null ? generateRandomUUIDString() : headCircumference.getFormSubmissionId())
                    .withDateCreated(new Date());


            if (fields != null && fields.length() != 0)
                for (int i = 0; i < fields.length(); i++) {
                    JSONObject jsonObject = getJSONObject(fields, i);
                    String value = getString(jsonObject, VALUE);
                    if (StringUtils.isNotBlank(value)) {
                        addObservation(event, jsonObject);
                    }
                }


            if (event != null) {

                JSONObject eventJson = new JSONObject(org.smartregister.util.JsonFormUtils.gson.toJson(event));

                //check if an event already exists and update instead
                if (headCircumference.getEventId() != null) {
                    JSONObject existingEvent = db.getEventsByEventId(headCircumference.getEventId());
                    eventJson = merge(existingEvent, eventJson);
                }

                //merge if event exists
                db.addEvent(event.getBaseEntityId(), eventJson);

            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

}


