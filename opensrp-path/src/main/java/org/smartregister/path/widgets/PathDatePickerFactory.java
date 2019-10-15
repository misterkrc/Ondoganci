package org.smartregister.path.widgets;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.widgets.DatePickerFactory;

import org.json.JSONObject;
import org.smartregister.path.watchers.LookUpTextWatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import util.PathConstants;

/**
 * Created by keyman on 11/04/2017.
 */
public class PathDatePickerFactory extends DatePickerFactory {

    @Override
    public void attachJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, MaterialEditText editText, TextView duration) {
        super.attachJson(stepName, context, formFragment, jsonObject, editText, duration);

        try {
            if (jsonObject.has(PathConstants.KEY.LOOK_UP) && jsonObject.get(PathConstants.KEY.LOOK_UP).toString().equalsIgnoreCase(Boolean.TRUE.toString())) {

                String entityId = jsonObject.getString(PathConstants.KEY.ENTITY_ID);

                Map<String, List<View>> lookupMap = formFragment.getLookUpMap();
                List<View> lookUpViews = new ArrayList<>();
                if (lookupMap.containsKey(entityId)) {
                    lookUpViews = lookupMap.get(entityId);
                }

                if (!lookUpViews.contains(editText)) {
                    lookUpViews.add(editText);
                }

                lookupMap.put(entityId, lookUpViews);

                editText.addTextChangedListener(new LookUpTextWatcher(formFragment, editText, entityId));
                editText.setTag(com.vijay.jsonwizard.R.id.after_look_up, false);
            }


        } catch (Exception e) {
            Log.e(getClass().getName(), e.toString(), e);
        }
    }
}
