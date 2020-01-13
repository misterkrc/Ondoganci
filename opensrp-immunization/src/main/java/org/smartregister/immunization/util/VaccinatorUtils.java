/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smartregister.immunization.util;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.domain.Alert;
import org.smartregister.domain.AlertStatus;
import org.smartregister.domain.jsonmapping.Location;
import org.smartregister.domain.jsonmapping.util.LocationTree;
import org.smartregister.domain.jsonmapping.util.TreeNode;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.R;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.db.VaccineRepo.Vaccine;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceSchedule;
import org.smartregister.immunization.domain.ServiceTrigger;
import org.smartregister.immunization.domain.ServiceType;
import org.smartregister.immunization.domain.VaccineWrapper;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.fragment.UndoVaccinationDialogFragment;
import org.smartregister.immunization.fragment.VaccinationDialogFragment;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.IntegerUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.smartregister.immunization.R.id.vaccine;
import static org.smartregister.util.Utils.addToList;
import static org.smartregister.util.Utils.addToRow;
import static org.smartregister.util.Utils.convertDateFormat;
import static org.smartregister.util.Utils.getPreference;
import static org.smartregister.util.Utils.getValue;

/**
 * Class containing some static utility methods.
 */
public class VaccinatorUtils {
    private static final String TAG = "VaccinatorUtils";

    public static HashMap<String, String> providerDetails() {
        org.smartregister.Context context = ImmunizationLibrary.getInstance().context();
        org.smartregister.util.Log.logDebug("ANM DETAILS" + context.anmController().get());
        org.smartregister.util.Log.logDebug("USER DETAILS" + context.allSettings().fetchUserInformation());
        org.smartregister.util.Log.logDebug("TEAM DETAILS" + getPreference(context.applicationContext(), "team", "{}"));

        String locationJson = context.anmLocationController().get();
        LocationTree locationTree = AssetHandler.jsonStringToJava(locationJson, LocationTree.class);

        HashMap<String, String> map = new HashMap<>();

        if (locationTree != null) {
            Map<String, TreeNode<String, Location>> locationMap = locationTree.getLocationsHierarchy();
            Map<String, String> locations = new HashMap<>();
            addToList(locations, locationMap, "country");
            addToList(locations, locationMap, "province");
            addToList(locations, locationMap, "city");
            addToList(locations, locationMap, "town");
            addToList(locations, locationMap, "uc");
            addToList(locations, locationMap, "vaccination center");

            map.put("provider_uc", locations.get("uc"));
            map.put("provider_town", locations.get("town"));
            map.put("provider_city", locations.get("city"));
            map.put("provider_province", locations.get("province"));
            map.put("provider_location_id", locations.get("vaccination center"));
            map.put("provider_location_name", locations.get("vaccination center"));
            map.put("provider_id", context.anmService().fetchDetails().name());
        }

        try {
            JSONObject tm = new JSONObject(getPreference(context.applicationContext(), "team", "{}"));
            map.put("provider_name", tm.getJSONObject("person").getString("display"));
            map.put("provider_identifier", tm.getString("identifier"));
            map.put("provider_team", tm.getJSONObject("team").getString("teamName"));
        } catch (JSONException e) {
            Log.e(TAG, "", e);
        }

        return map;
    }

    public static ArrayList<HashMap<String, String>> getWasted(String startDate, String endDate, String type) {
        String sqlWasted = "select sum (total_wasted)as total_wasted from stock where `report` ='" + type + "' and `date` between '" + startDate + "' and '" + endDate + "'";
        return ImmunizationLibrary.getInstance().context().commonrepository("stock").rawQuery(sqlWasted);
    }

    public static int getWasted(String startDate, String endDate, String type, String... variables) {
        List<CommonPersonObject> cl = ImmunizationLibrary.getInstance().context().commonrepository("stock").customQueryForCompleteRow("SELECT * FROM stock WHERE `report` ='" + type + "' and `date` between '" + startDate + "' and '" + endDate + "'", null, "stock");
        int total = 0;
        for (CommonPersonObject c : cl) {
            for (String v : variables) {
                String val = getValue(c.getDetails(), v, "0", false);
                total += IntegerUtil.tryParse(val, 0);
            }
        }
        return total;
    }

    public static ArrayList<HashMap<String, String>> getUsed(String startDate, String endDate, String table, String... vaccines) {
        String q = "SELECT * FROM (";
        for (String v : vaccines) {
            q += " (select count(*) " + v + " from " + table + " where " + v + " between '" + startDate + "' and '" + endDate + "') " + v + " , ";
        }
        q = q.trim().substring(0, q.trim().lastIndexOf(","));
        q += " ) e ";

        Log.i("DD", q);
        return ImmunizationLibrary.getInstance().context().commonrepository(table).rawQuery(q);
    }

    public static int getTotalUsed(String startDate, String endDate, String table, String... vaccines) {
        int totalUsed = 0;

        for (HashMap<String, String> v : getUsed(startDate, endDate, table, vaccines)) {
            for (String k : v.keySet()) {
                totalUsed += Integer.parseInt(v.get(k) == null ? "0" : v.get(k));
            }
        }
        Log.i("", "TOTAL USED: " + totalUsed);

        return totalUsed;
    }

    public static void addStatusTag(Context context, TableLayout table, String tag, boolean hrLine) {
        TableRow tr = new TableRow(context);
        if (hrLine) {
            tr.setBackgroundColor(Color.LTGRAY);
            tr.setPadding(1, 1, 1, 1);
            table.addView(tr);
        }
        tr = addToRow(context, Html.fromHtml("<b>" + tag + "</b>"), new TableRow(context), true, 1);
        tr.setPadding(15, 5, 0, 0);
        table.addView(tr);
    }


    public static void addVaccineDetail(final Context context, TableLayout table, final VaccineWrapper vaccineWrapper) {
        TableRow tr = (TableRow) ((Activity) context).getLayoutInflater().inflate(R.layout.vaccinate_row_view, null);
        tr.setGravity(Gravity.CENTER_VERTICAL);
        tr.setTag(vaccineWrapper.getId());

        RelativeLayout relativeLayout = (RelativeLayout) tr.findViewById(R.id.vacc_status_layout);
        if (vaccineWrapper.isCompact()) {
            relativeLayout.setPadding(dpToPx(context, 0f), dpToPx(context, 7.5f), dpToPx(context, 0f), dpToPx(context, 7.5f));
        } else {
            relativeLayout.setPadding(dpToPx(context, 0f), dpToPx(context, 10f), dpToPx(context, 0f), dpToPx(context, 10f));
        }


        TextView label = (TextView) tr.findViewById(vaccine);
        label.setText(vaccineWrapper.getVaccine().display());

        String vaccineDate = "";
        String color = "#ffffff";
        if (vaccineWrapper.getStatus().equalsIgnoreCase("due")) {
            if (vaccineWrapper.getAlert() != null) {
                color = getColorValue(context, vaccineWrapper.getAlert().status());
                vaccineDate = "due: " + convertDateFormat(vaccineWrapper.getVaccineDateAsString(), true) + "";
                addVaccinationDialogHook(context, tr, vaccineWrapper, color, vaccineDate);
            } else if (StringUtils.isNotBlank(vaccineWrapper.getVaccineDateAsString())) {
                color = getColorValue(context, AlertStatus.inProcess);
                vaccineDate = "due: " + convertDateFormat(vaccineWrapper.getVaccineDateAsString(), true) + "";
                addVaccinationDialogHook(context, tr, vaccineWrapper, color, vaccineDate);
            }
        } else if (vaccineWrapper.getStatus().equalsIgnoreCase("done")) {
            color = "#31B404";
            vaccineDate = convertDateFormat(vaccineWrapper.getVaccineDateAsString(), true);
        } else if (vaccineWrapper.getStatus().equalsIgnoreCase("expired")) {
            color = getColorValue(context, AlertStatus.inProcess);
            vaccineDate = "exp: " + convertDateFormat(vaccineWrapper.getVaccineDateAsString(), true) + "";
            addVaccinationDialogHook(context, tr, vaccineWrapper, color, vaccineDate);
        }

        LinearLayout l = new LinearLayout(context);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setVerticalGravity(Gravity.CENTER_VERTICAL);
        l.setGravity(Gravity.CENTER_VERTICAL);

        Button s = (Button) tr.findViewById(R.id.status);
        s.setBackgroundColor(StringUtils.isBlank(color) ? Color.WHITE : Color.parseColor(color));

        TextView v = (TextView) tr.findViewById(R.id.date);
        v.setText(vaccineDate);

        Button u = (Button) tr.findViewById(R.id.undo);
        FrameLayout.LayoutParams ulp = (FrameLayout.LayoutParams) u.getLayoutParams();
        if (vaccineWrapper.isCompact()) {
            ulp.width = dpToPx(context, 70f);
            ulp.height = dpToPx(context, 35f);
            u.setLayoutParams(ulp);
        } else {
            ulp.width = dpToPx(context, 65f);
            ulp.height = dpToPx(context, 40f);
            u.setLayoutParams(ulp);
        }

        u.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = ((Activity) context).getFragmentManager().beginTransaction();
                Fragment prev = ((Activity) context).getFragmentManager().findFragmentByTag(UndoVaccinationDialogFragment.DIALOG_TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                UndoVaccinationDialogFragment undoVaccinationDialogFragment = UndoVaccinationDialogFragment.newInstance(vaccineWrapper);
                undoVaccinationDialogFragment.show(ft, UndoVaccinationDialogFragment.DIALOG_TAG);
            }
        });

        if (table.getChildCount() > 0 && StringUtils.isNotBlank(vaccineWrapper.getId()) && StringUtils.isNotBlank(vaccineWrapper.getPreviousVaccineId()) && !vaccineWrapper.getId().equals(vaccineWrapper.getId())) {
            View view = new View(context);
            view.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, dpToPx(context, 10f)));

            table.addView(view);
        }

        table.addView(tr);
    }

    private static void addVaccinationDialogHook(final Context context, TableRow tr, final VaccineWrapper vaccineWrapper, String color, String formattedDate) {

        if (VaccinateActionUtils.addDialogHookCustomFilter(vaccineWrapper)) {
            vaccineWrapper.setColor(color);
            vaccineWrapper.setFormattedVaccineDate(formattedDate);

            tr.setOnClickListener(new TableRow.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentTransaction ft = ((Activity) context).getFragmentManager().beginTransaction();
                    Fragment prev = ((Activity) context).getFragmentManager().findFragmentByTag(VaccinationDialogFragment.DIALOG_TAG);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    ArrayList<VaccineWrapper> list = new ArrayList<VaccineWrapper>();
                    list.add(vaccineWrapper);
                    VaccinationDialogFragment vaccinationDialogFragment = VaccinationDialogFragment.newInstance(null, null, list);
                    vaccinationDialogFragment.show(ft, VaccinationDialogFragment.DIALOG_TAG);

                }
            });
        }
    }


    private static DateTime getReceivedDate(Map<String, String> received, Vaccine v) {
        if (received.get(v.name()) != null) {
            return new DateTime(received.get(v.name()));
        } else if (received.get(v.name() + "_retro") != null) {
            return new DateTime(received.get(v.name() + "_retro"));
        }
        return null;
    }

    public static List<Map<String, Object>> generateScheduleList(String category, DateTime milestoneDate, Map<String, Date> received, List<Alert> alerts) {
        return generateScheduleList(category, milestoneDate, received, alerts, false);
    }

    /**
     * To use generateSchedule() method just use this method and set showExpired to true
     *
     * @param category
     * @param milestoneDate
     * @param received
     * @param alerts
     * @param showExpired
     * @return
     */
    public static List<Map<String, Object>> generateScheduleList(String category, DateTime milestoneDate, Map<String, Date> received, List<Alert> alerts, boolean showExpired) {
        List<Map<String, Object>> schedule = new ArrayList<>();
        boolean m1Given = false;
        boolean m2Given = false;
        boolean oGiven = false;
        try {
            ArrayList<Vaccine> vl = VaccineRepo.getVaccines(category);
            for (Vaccine v : vl) {
                Map<String, Object> m = new HashMap<>();
                Date recDate = received.get(v.display().toLowerCase());
                if (recDate != null) {
                    m = createVaccineMap("done", null, new DateTime(recDate), v);
                    // Check for vaccines where either can be given - mealses/mr opv0/opv4
                    if (VaccineRepo.Vaccine.measles1.equals(v) || VaccineRepo.Vaccine.mr1.equals(v)) {
                        m1Given = true;
                    } else if (VaccineRepo.Vaccine.measles2.equals(v) || VaccineRepo.Vaccine.mr2.equals(v)) {
                        m2Given = true;
                    } else if (VaccineRepo.Vaccine.opv0.equals(v) || VaccineRepo.Vaccine.opv4.equals(v)) {
                        oGiven = true;
                    }
                } else if (showExpired && milestoneDate != null && v.expiryDays() > 0 && milestoneDate.plusDays(v.expiryDays()).isBefore(DateTime.now())) {
                    m = createVaccineMap("expired", null, milestoneDate.plusDays(v.expiryDays()), v);
                } else if (alerts.size() > 0) {
                    for (Alert a : alerts) {
                        if (a.scheduleName().replaceAll(" ", "").equalsIgnoreCase(v.name())
                                || a.visitCode().replaceAll(" ", "").equalsIgnoreCase(v.name())) {
                            m = createVaccineMap("due", a, new DateTime(a.startDate()), v);
                        }
                    }
                }

                if (m.isEmpty()) {
                    if (v.prerequisite() != null) {
                        Date prereq = received.get(v.prerequisite().display().toLowerCase());
                        if (prereq != null) {
                            DateTime prereqDateTime = new DateTime(prereq);
                            prereqDateTime = prereqDateTime.plusDays(v.prerequisiteGapDays());
                            m = createVaccineMap("due", null, prereqDateTime, v);
                        } else {
                            m = createVaccineMap("due", null, null, v);
                        }
                    } else if (milestoneDate != null) {
                        m = createVaccineMap("due", null, milestoneDate.plusDays(v.milestoneGapDays()), v);
                    } else {
                        m = createVaccineMap("na", null, null, v);
                    }
                }

                schedule.add(m);
            }

            // Check for vaccines where either can be given - mealses/mr opv0/opv4
            List<Map<String, Object>> toRemove = retrieveNotDoneSchedule(schedule, m1Given, m2Given, oGiven);
            if (toRemove != null && !toRemove.isEmpty()) {
                schedule.removeAll(toRemove);
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return schedule;
    }

    private static List<Map<String, Object>> retrieveNotDoneSchedule(List<Map<String, Object>> schedule, boolean m1Given, boolean m2Given, boolean oGiven) {
        if (schedule == null || schedule.isEmpty() || (!m1Given && !m2Given && !oGiven)) {
            return new ArrayList<>();
        }

        final String STATUS = "status";
        final String VACCINE = "vaccine";
        final String DONE = "done";
        List<Map<String, Object>> toRemove = new ArrayList<>();

        for (Map<String, Object> m : schedule) {
            if (m == null ||
                    m.get(STATUS) == null ||
                    m.get(VACCINE) == null ||
                    !(m.get(VACCINE) instanceof Vaccine) ||
                    DONE.equalsIgnoreCase(m.get(STATUS).toString())) {
                continue;
            }

            Vaccine v = (Vaccine) m.get(VACCINE);

            if (m1Given && (VaccineRepo.Vaccine.measles1.equals(v) || VaccineRepo.Vaccine.mr1.equals(v))) {
                toRemove.add(m);
            } else if (m2Given && (VaccineRepo.Vaccine.measles2.equals(v) || VaccineRepo.Vaccine.mr2.equals(v))) {
                toRemove.add(m);
            } else if (oGiven && (VaccineRepo.Vaccine.opv0.equals(v) || VaccineRepo.Vaccine.opv4.equals(v))) {
                toRemove.add(m);
            }
        }

        return toRemove;
    }

    public static List<Map<String, Object>> generateScheduleList(List<ServiceType> serviceTypes, DateTime milestoneDate, Map<String, Date> received, List<Alert> alerts) {
        List<Map<String, Object>> schedule = new ArrayList();
        try {
            for (ServiceType s : serviceTypes) {
                final String VIT_A_IFC_2 = "Vit A IFC 2";
                if (VIT_A_IFC_2.equals(s.getName())) {
                    int months = Months.monthsBetween(milestoneDate, DateTime.now()).getMonths();
                    if (months >= 6) {
                        continue;
                    }

                }
                Map<String, Object> m = new HashMap<>();
                Date recDate = received.get(s.getName());
                if (recDate != null) {
                    m = createServiceMap("done", null, new DateTime(recDate), s);
                } /*else if (milestoneDate != null && StringUtils.isNotBlank(s.getExpiryOffset()) && ServiceSchedule.addOffsetToDateTime(milestoneDate, s.getExpiryOffset()).isBefore(DateTime.now())) {
                m = createServiceMap("expired", null, ServiceSchedule.addOffsetToDateTime(milestoneDate, s.getExpiryOffset()), s);
            }*/ else if (alerts.size() > 0) {
                    for (Alert a : alerts) {
                        if (a.scheduleName().equalsIgnoreCase(s.getName())
                                || a.visitCode().equalsIgnoreCase(s.getName())) {
                            m = createServiceMap("due", a, new DateTime(a.startDate()), s);
                        }
                    }
                }

                if (m.isEmpty()) {
                    DateTime dueDateTime = getServiceDueDate(s, milestoneDate, received);
                    m = createServiceMap("due", null, dueDateTime, s);
                }

                schedule.add(m);
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return schedule;
    }

    public static DateTime getServiceDueDate(ServiceType serviceType, DateTime milestoneDate, List<ServiceRecord> serviceRecordList) {
        return getServiceDueDate(serviceType, milestoneDate, receivedServices(serviceRecordList));
    }

    public static DateTime getServiceDueDate(ServiceType serviceType, DateTime milestoneDate, Map<String, Date> received) {
        try {
            if (serviceType == null || milestoneDate == null || received == null) {
                return null;
            }
            boolean hasPrerequisite = false;
            Date prereq = null;
            if (StringUtils.isNotBlank(serviceType.getPrerequisite())) {
                String prerequisite = serviceType.getPrerequisite();
                if (!prerequisite.equalsIgnoreCase(ServiceTrigger.Reference.DOB.name())) {
                    String[] preArray = prerequisite.split("\\|");
                    if (preArray.length >= 2) {
                        if (preArray[0].equalsIgnoreCase(ServiceTrigger.Reference.PREREQUISITE.name())) {
                            String preService = preArray[1];
                            prereq = received.get(preService);
                            if (prereq != null) {
                                hasPrerequisite = true;
                            }
                        } else if (preArray[0].equalsIgnoreCase(ServiceTrigger.Reference.MULTIPLE.name())) {
                            String condition = preArray[1];
                            if (condition.equalsIgnoreCase("or") && preArray.length == 3) {
                                String arrayString = preArray[2];
                                String[] preqs = convertToArray(arrayString);
                                if (preqs != null) {
                                    for (String preService : preqs) {
                                        if (preService.equalsIgnoreCase(ServiceTrigger.Reference.DOB.name())) {
                                            continue;
                                        }
                                        prereq = received.get(preService);
                                        if (prereq != null) {
                                            hasPrerequisite = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (hasPrerequisite && StringUtils.isNotBlank(serviceType.getPreOffset())) {
                DateTime prereqDateTime = new DateTime(prereq);
                return ServiceSchedule.addOffsetToDateTime(prereqDateTime, serviceType.getPreOffset());
            } else if (StringUtils.isNotBlank(serviceType.getMilestoneOffset())) {
                String[] milestones = convertToArray(serviceType.getMilestoneOffset());
                if (milestones != null) {
                    List<String> milestoneList = Arrays.asList(milestones);
                    return ServiceSchedule.addOffsetToDateTime(milestoneDate, milestoneList);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }

    public static DateTime getServiceExpiryDate(ServiceType serviceType, DateTime milestoneDate) {
        if (serviceType == null || milestoneDate == null) {
            return null;
        }
        return ServiceSchedule.addOffsetToDateTime(milestoneDate, serviceType.getExpiryOffset());
    }

    private static Map<String, Object> createVaccineMap(String status, Alert a, DateTime
            date, Vaccine v) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", status);
        m.put("alert", a);
        m.put("date", date);
        m.put("vaccine", v);

        return m;
    }

    private static Map<String, Object> createServiceMap(String status, Alert a, DateTime
            date, ServiceType s) {
        Map<String, Object> m = new HashMap<>();
        m.put("status", status);
        m.put("alert", a);
        m.put("date", date);
        m.put("service", s);

        return m;
    }

    public static Map<String, Object> nextVaccineDue
            (List<Map<String, Object>> schedule, Date lastVisit) {
        Map<String, Object> v = null;
        try {
            for (Map<String, Object> m : schedule) {
                if (m != null && m.get("status") != null && m.get("status").toString().equalsIgnoreCase("due")) {
                    if (m.get("vaccine") != null && (((Vaccine) m.get("vaccine")).equals(Vaccine.bcg2) || ((Vaccine) m.get("vaccine")).equals(Vaccine.ipv))) {
                        // bcg2 is a special alert and should not be considered as the next vaccine
                        continue;
                    }

                    if (v == null) {
                        v = m;
                    } else if (m.get("date") != null && v.get("date") != null
                            && ((DateTime) m.get("date")).isBefore((DateTime) v.get("date"))
                            && (lastVisit == null
                            || lastVisit.before(((DateTime) m.get("date")).toDate()))) {
                        v = m;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return v;
    }

    public static Map<String, Object> nextVaccineDue
            (List<Map<String, Object>> schedule, List<Vaccine> vaccineList) {
        Map<String, Object> v = null;
        try {
            for (Map<String, Object> m : schedule) {
                if (m != null && m.get("status") != null && m.get("status").toString().equalsIgnoreCase("due")) {
                    if (m.get("vaccine") != null && (((Vaccine) m.get("vaccine")).equals(Vaccine.bcg2) || ((Vaccine) m.get("vaccine")).equals(Vaccine.ipv))) {
                        // bcg2 is a special alert and should not be considered as the next vaccine
                        continue;
                    }

                    if (v == null && m.get("vaccine") != null && vaccineList.contains((Vaccine) m.get("vaccine"))) {
                        v = m;
                    } else if ((v.get("alert") == null && m.get("alert") != null) && (m.get("vaccine") != null && vaccineList.contains((Vaccine) m.get("vaccine")))) {
                        v = m;
                    } else if (v.get("alert") != null && m.get("alert") != null && m.get("vaccine") != null && vaccineList.contains((Vaccine) m.get("vaccine"))) {
                        Alert vAlert = (Alert) v.get("alert");
                        Alert mAlert = (Alert) m.get("alert");
                        if ((!vAlert.status().equals(AlertStatus.urgent) && (vAlert.status().equals(AlertStatus.upcoming)) && (mAlert.status().equals(AlertStatus.normal) || mAlert.status().equals(AlertStatus.urgent)))) {
                            v = m;

                        } else if (vAlert.status().equals(AlertStatus.normal) && mAlert.status().equals(AlertStatus.urgent)) {
                            v = m;

                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return v;
    }

    public static Map<String, Object> nextServiceDue
            (List<Map<String, Object>> schedule, Date lastVisit) {
        Map<String, Object> v = null;
        try {
            for (Map<String, Object> m : schedule) {
                if (m != null && m.get("status") != null && m.get("status").toString().equalsIgnoreCase("due")) {

                    if (v == null) {
                        v = m;
                    } else if (m.get("date") != null && v.get("date") != null
                            && ((DateTime) m.get("date")).isBefore((DateTime) v.get("date"))
                            && (lastVisit == null
                            || lastVisit.before(((DateTime) m.get("date")).toDate()))) {
                        v = m;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return v;
    }

    public static Map<String, Object> nextServiceDue
            (List<Map<String, Object>> schedule, List<ServiceType> serviceTypeList) {
        Map<String, Object> v = null;
        try {
            for (Map<String, Object> m : schedule) {
                if (m != null && m.get("status") != null && m.get("status").toString().equalsIgnoreCase("due")) {

                    if (v == null && m.get("service") != null && serviceTypeList.contains((ServiceType) m.get("service"))) {
                        v = m;

                    } else if (v.get("alert") == null && m.get("alert") != null && m.get("service") != null && serviceTypeList.contains((ServiceType) m.get("service"))) {
                        v = m;

                    } else if (v.get("alert") != null && m.get("alert") != null && m.get("service") != null && serviceTypeList.contains((ServiceType) m.get("service"))) {
                        Alert vAlert = (Alert) v.get("alert");
                        Alert mAlert = (Alert) m.get("alert");
                        if (!vAlert.status().equals(AlertStatus.urgent)) {
                            if (vAlert.status().equals(AlertStatus.upcoming) && (mAlert.status().equals(AlertStatus.normal) || mAlert.status().equals(AlertStatus.urgent))) {
                                v = m;
                            } else if (vAlert.status().equals(AlertStatus.normal) && mAlert.status().equals(AlertStatus.urgent)) {
                                v = m;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return v;
    }

    public static Map<String, Object> nextServiceDue
            (List<Map<String, Object>> schedule, ServiceRecord lastServiceRecord) {
        if (lastServiceRecord == null || StringUtils.isBlank(lastServiceRecord.getType()) || StringUtils.isBlank(lastServiceRecord.getName())) {
            return null;
        }

        if (!lastServiceRecord.getSyncStatus().equalsIgnoreCase(RecurringServiceRecordRepository.TYPE_Unsynced)) {
            return null;
        }
        Map<String, Object> v = null;
        for (Map<String, Object> m : schedule) {
            if (m != null && m.get("service") != null) {
                ServiceType mServiceType = (ServiceType) m.get("service");
                if (mServiceType.getName().equalsIgnoreCase(lastServiceRecord.getName()) && mServiceType.getType().equalsIgnoreCase(lastServiceRecord.getType())) {
                    v = m;
                }
            }
        }
        return v;
    }

    public static int dpToPx(Context context, float dpValue) {
        Resources r = context.getResources();
        float val = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics());
        return new Float(val).intValue();
    }

    /**
     * Returns a list of VaccineGroup containing a list of supported vaccines
     *
     * @param context Current valid context to be used
     * @return list of VaccineGroup with the supported vaccines
     */
    public static List<VaccineGroup> getSupportedVaccines(@Nullable Context context) {
        Class<List<VaccineGroup>> clazz = (Class) List.class;
        Type listType = new TypeToken<List<VaccineGroup>>() {
        }.getType();
        return ImmunizationLibrary.getInstance().assetJsonToJava("vaccines.json", clazz, listType);
    }

    /**
     * Returns a list of VaccineGroup containing a list of supported woman vaccines
     *
     * @param context Current valid context to be used
     * @return list of VaccineGroup with the supported vaccines
     */
    public static List<VaccineGroup> getSupportedWomanVaccines(@Nullable Context context) {
        Class<List<VaccineGroup>> clazz = (Class) List.class;
        Type listType = new TypeToken<List<VaccineGroup>>() {
        }.getType();
        return ImmunizationLibrary.getInstance().assetJsonToJava("mother_vaccines.json", clazz, listType);
    }

    public static List<org.smartregister.immunization.domain.jsonmapping.Vaccine> getSpecialVaccines(@Nullable Context context) {
        Class<List<org.smartregister.immunization.domain.jsonmapping.Vaccine>> clazz = (Class) List.class;
        Type listType = new TypeToken<List<org.smartregister.immunization.domain.jsonmapping.Vaccine>>() {
        }.getType();
        return ImmunizationLibrary.getInstance().assetJsonToJava("special_vaccines.json", clazz, listType);
    }

    /**
     * Returns a JSON String containing a list of supported services
     *
     * @param context Current valid context to be used
     * @return JSON String with the supported vaccines or NULL if unable to obtain the list
     */
    public static String getSupportedRecurringServices(Context context) {
        String supportedServicesString = org.smartregister.util.Utils.readAssetContents(context, "recurring_service_types.json");
        return supportedServicesString;
    }

    public static int getVaccineCalculation(Context context, String vaccineName)
            throws JSONException {
        List<VaccineGroup> supportedVaccines = getSupportedVaccines(context);
        for (VaccineGroup vaccineGroup : supportedVaccines) {
            for (org.smartregister.immunization.domain.jsonmapping.Vaccine vaccine : vaccineGroup.vaccines) {
                if (vaccine.name.equalsIgnoreCase(vaccineName))
                    return vaccine.openmrs_calculate.calculation;
            }
        }
        return -1;
    }

    public static Map<String, Date> receivedVaccines(List<org.smartregister.immunization.domain.Vaccine> vaccines) {
        Map<String, Date> map = new LinkedHashMap<>();
        if (vaccines != null) {
            for (org.smartregister.immunization.domain.Vaccine vaccine : vaccines) {
                if (vaccine.getDate() != null) {
                    map.put(vaccine.getName(), vaccine.getDate());
                }
            }
        }
        return map;

    }

    public static Map<String, Date> receivedServices(List<org.smartregister.immunization.domain.ServiceRecord> serviceRecordList) {
        Map<String, Date> map = new LinkedHashMap<>();
        if (serviceRecordList != null) {
            for (org.smartregister.immunization.domain.ServiceRecord serviceRecord : serviceRecordList) {
                if (serviceRecord.getDate() != null) {
                    map.put(serviceRecord.getName(), serviceRecord.getDate());
                }
            }
        }
        return map;

    }

    /**
     * This method retrieves the human readable name corresponding to the vaccine name from {@code Vaccine.getName()}
     *
     * @param vaccineDbName
     * @return
     */
    public static String getVaccineDisplayName(Context context, final String vaccineDbName) {
        List<VaccineGroup> availableVaccines = getSupportedVaccines(context);

        for (VaccineGroup vaccineGroup : availableVaccines) {
            for (org.smartregister.immunization.domain.jsonmapping.Vaccine vaccine : vaccineGroup.vaccines) {
                if (vaccine.name.equalsIgnoreCase(vaccineDbName))
                    return vaccine.name;
            }
        }

        return vaccineDbName;
    }

    /**
     * Converts string [a,b,c] to string array
     *
     * @param s_
     * @return
     */
    private static String[] convertToArray(String s_) {
        try {
            String s = s_;
            if (StringUtils.isBlank(s)) {
                return null;
            }

            if (s.contains("[")) {
                s = s.replace("[", "");
            }

            if (s.contains("]")) {
                s = s.replace("]", "");
            }

            if (StringUtils.isBlank(s)) {
                return null;
            } else if (s.contains(",")) {
                return StringUtils.stripAll(s.split(","));
            } else if (StringUtils.isNotBlank(s)) {
                return new String[]{s};
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }


    public static String getColorValue(Context cxt, AlertStatus alertStatus) {
        if (alertStatus.equals(AlertStatus.upcoming)) {
            return "#" + Integer.toHexString(cxt.getResources().getColor(org.smartregister.immunization.R.color.alert_upcoming)).substring(2);
        }
        if (alertStatus.equals(AlertStatus.normal)) {
            return "#" + Integer.toHexString(cxt.getResources().getColor(org.smartregister.immunization.R.color.alert_normal)).substring(2);
        }
        if (alertStatus.equals(AlertStatus.urgent)) {
            return "#" + Integer.toHexString(cxt.getResources().getColor(org.smartregister.immunization.R.color.alert_urgent)).substring(2);
        } else {
            return "#" + Integer.toHexString(cxt.getResources().getColor(org.smartregister.immunization.R.color.alert_na)).substring(2);
        }
    }
}
