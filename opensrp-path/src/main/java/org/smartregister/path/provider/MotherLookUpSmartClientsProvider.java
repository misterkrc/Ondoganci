package org.smartregister.path.provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.path.R;
import org.smartregister.util.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import util.PathConstants;

import static org.smartregister.util.Utils.fillValue;
import static org.smartregister.util.Utils.getName;
import static org.smartregister.util.Utils.getValue;

/**
 * Created by Ahmed on 13-Oct-15.
 */
public class MotherLookUpSmartClientsProvider {
    private final LayoutInflater inflater;
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");


    public MotherLookUpSmartClientsProvider(Context context) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void getView(CommonPersonObject commonPersonObject, List<CommonPersonObject> children, View convertView) {
        CommonPersonObjectClient pc = Utils.convert(commonPersonObject);

        List<CommonPersonObjectClient> childList = new ArrayList<>();
        for (CommonPersonObject childCommonPersonObject : children) {
            childList.add(Utils.convert(childCommonPersonObject));
        }

        String childName = name(pc);

        fillValue((TextView) convertView.findViewById(R.id.name), childName);

        String birthDateString = "Birthdate missing";
        DateTime birthDateTime = dob(pc);
        if (birthDateTime != null) {
            birthDateString = dateFormat.format(birthDateTime.toDate());
        }


        String childListString = "";
        if (!childList.isEmpty()) {
            if (childList.size() > 1) {
                //sort youngest to oldest
                sortList(childList);
            }

            for (int i = 0; i < childList.size(); i++) {
                String name = name(childList.get(i));
                if (i == (childList.size() - 1)) {
                    childListString += name;
                } else {
                    childListString += name + ", ";
                }
            }

        }

        fillValue((TextView) convertView.findViewById(R.id.details), birthDateString + " - " + childListString);
    }


    public View inflatelayoutForCursorAdapter() {
        return inflater().inflate(R.layout.mother_child_lookup_client, null);
    }

    private LayoutInflater inflater() {
        return inflater;
    }

    private void sortList(List<CommonPersonObjectClient> childList) {
        Collections.sort(childList, new Comparator<CommonPersonObjectClient>() {
            @Override
            public int compare(CommonPersonObjectClient lhs, CommonPersonObjectClient rhs) {
                DateTime lhsTime = dob(lhs);
                DateTime rhsTime = dob(rhs);

                if (lhsTime == null && rhsTime == null) {
                    return 0;
                } else if (lhsTime == null) {
                    return 1;
                } else if (rhsTime == null) {
                    return -1;
                }

                long diff = lhsTime.getMillis() - rhsTime.getMillis();
                if (diff > 0) {
                    return -1;
                } else if (diff < 0) {
                    return 1;
                }

                return 0;
            }
        });
    }

    private DateTime dob(CommonPersonObjectClient pc) {
        String dobString = getValue(pc.getColumnmaps(), PathConstants.EC_CHILD_TABLE.DOB, false);
        return util.Utils.dobStringToDateTime(dobString);
    }

    private String name(CommonPersonObjectClient pc) {
        String firstName = getValue(pc.getColumnmaps(), "first_name", true);
        String lastName = getValue(pc.getColumnmaps(), "last_name", true);
        return getName(firstName, lastName);
    }

}
