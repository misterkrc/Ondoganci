package org.smartregister.growthmonitoring.fragment;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.graphics.DashPathEffect;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.opensrp.api.constants.Gender;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.growthmonitoring.R;
import org.smartregister.growthmonitoring.domain.HCZScore;
import org.smartregister.growthmonitoring.domain.HeadCircumference;
import org.smartregister.growthmonitoring.listener.ViewMeasureListener;
import org.smartregister.growthmonitoring.util.ImageUtils;
import org.smartregister.util.DateUtil;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.DrishtiApplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

@SuppressLint("ValidFragment")
public class HCDialogFragment extends DialogFragment {
    private static final String TAG = HCDialogFragment.class.getName();
    private CommonPersonObjectClient personDetails;
    private List<HeadCircumference> headCircumferences;
    public static final String DIALOG_TAG = "HCDialogFragment";
    public static final String WRAPPER_TAG = "tag";
    private boolean isExpanded = false;
    private static final int GRAPH_MONTHS_TIMELINE = 12;
    private Calendar maxMeasureDate = null;
    private Calendar minMeasureDate = null;

    public static HCDialogFragment newInstance(CommonPersonObjectClient personDetails,
                                               List<HeadCircumference> headCircumferences) {

        HCDialogFragment vaccinationDialogFragment = new HCDialogFragment();
        vaccinationDialogFragment.setPersonDetails(personDetails);
        vaccinationDialogFragment.setHeadCircumferences(headCircumferences);

        return vaccinationDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    public void setHeadCircumferences(List<HeadCircumference> headCircumferences) {
        this.headCircumferences = headCircumferences;
        sortHeadCircumferences();
    }

    public void setPersonDetails(CommonPersonObjectClient personDetails) {
        this.personDetails = personDetails;
    }

    private void sortHeadCircumferences() {
        HashMap<Long, HeadCircumference> headCircumferenceHashMap = new HashMap<>();
        for (HeadCircumference curHC : headCircumferences) {
            if (curHC.getDate() != null) {
                Calendar curCalendar = Calendar.getInstance();
                curCalendar.setTime(curHC.getDate());
                standardiseCalendarDate(curCalendar);

                if (!headCircumferenceHashMap.containsKey(curCalendar.getTimeInMillis())) {
                    headCircumferenceHashMap.put(curCalendar.getTimeInMillis(), curHC);
                } else if (curHC.getUpdatedAt() > headCircumferenceHashMap.get(curCalendar.getTimeInMillis()).getUpdatedAt()) {
                    headCircumferenceHashMap.put(curCalendar.getTimeInMillis(), curHC);
                }
            }
        }

        List<Long> keys = new ArrayList<>(headCircumferenceHashMap.keySet());
        Collections.sort(keys, Collections.<Long>reverseOrder());

        List<HeadCircumference> result = new ArrayList<>();
        for (Long curKey : keys) {
            result.add(headCircumferenceHashMap.get(curKey));
        }

        this.headCircumferences = result;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        String firstName = Utils.getValue(personDetails.getColumnmaps(), "first_name", true);
        String lastName = Utils.getValue(personDetails.getColumnmaps(), "last_name", true);
        final ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.hc_growth_dialog_view, container, false);
        TextView nameView = dialogView.findViewById(R.id.child_name);
        nameView.setText(Utils.getName(firstName, lastName));

        String personId = Utils.getValue(personDetails.getColumnmaps(), "zeir_id", false);
        TextView numberView = dialogView.findViewById(R.id.child_zeir_id);
        if (StringUtils.isNotBlank(personId)) {
            numberView.setText(String.format("%s: %s", getString(R.string.label_zeir), personId));
        } else {
            numberView.setText("");
        }

        String genderString = Utils.getValue(personDetails, "gender", false);
        String baseEntityId = personDetails.entityId();
        ImageView profilePic = dialogView.findViewById(R.id.child_profilepic);
        profilePic.setTag(R.id.entity_id, baseEntityId);
        DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(baseEntityId,
                OpenSRPImageLoader.getStaticImageListener(
                        profilePic,
                        ImageUtils.profileImageResourceByGender(genderString),
                        ImageUtils.profileImageResourceByGender(genderString)));

        String formattedAge = "";
        String dobString = Utils.getValue(personDetails.getColumnmaps(), "dob", false);
        if (!TextUtils.isEmpty(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            Date dob = dateTime.toDate();
            long timeDiff = Calendar.getInstance().getTimeInMillis() - dob.getTime();

            if (timeDiff >= 0) {
                formattedAge = DateUtil.getDuration(timeDiff);
            }
        }

        TextView ageView = dialogView.findViewById(R.id.child_age);
        if (StringUtils.isNotBlank(formattedAge)) {
            ageView.setText(String.format("%s: %s", getString(R.string.age), formattedAge));
        } else {
            ageView.setText("");
        }

        TextView pmtctStatus = dialogView.findViewById(R.id.pmtct_status);
        String pmtctStatusString = Utils.getValue(personDetails.getColumnmaps(), "pmtct_status", true);
        if (!TextUtils.isEmpty(pmtctStatusString)) {
            pmtctStatus.setText(pmtctStatusString);
        } else {
            pmtctStatus.setText("");
        }

        Gender gender = Gender.UNKNOWN;
        if (genderString != null && genderString.equalsIgnoreCase("female")) {
            gender = Gender.FEMALE;
        } else if (genderString != null && genderString.equalsIgnoreCase("male")) {
            gender = Gender.MALE;
        }

        int genderStringRes = R.string.boys;
        if (gender == Gender.FEMALE) {
            genderStringRes = R.string.girls;
        }

        TextView headCircForAge = dialogView.findViewById(R.id.hc_for_age);
        headCircForAge.setText(String.format(getString(R.string.hc_for_age), getString(genderStringRes).toUpperCase()));

        Date dob = null;
        if (StringUtils.isNotBlank(dobString)) {
            DateTime dateTime = new DateTime(dobString);
            dob = dateTime.toDate();
            Calendar[] measureDates = getMinAndMaxMeasureDates(dob);
            minMeasureDate = measureDates[0];
            maxMeasureDate = measureDates[1];
        }

        Button done = dialogView.findViewById(R.id.done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HCDialogFragment.this.dismiss();
            }
        });

        final ImageButton scrollButton = dialogView.findViewById(R.id.scroll_button);
        scrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Prior implementation
                if (!isExpanded) {
                    isExpanded = true;
                    getHeight(dialogView.findViewById(R.id.hc_growth_chart), new ViewMeasureListener() {
                        @Override
                        public void onCompletedMeasuring(int height) {
                            dialogView.findViewById(R.id.ll_growthDialogView_headTableLayout).getLayoutParams().height =
                                    getResources().getDimensionPixelSize(R.dimen.weight_table_height) + height;
                        }
                    });
                    dialogView.findViewById(R.id.hc_growth_chart).setVisibility(View.GONE);
                    //Change the icon
                    scrollButton.setImageResource(R.drawable.ic_icon_expand);

                } else {
                    isExpanded = false;
                    dialogView.findViewById(R.id.hc_growth_chart).setVisibility(View.VISIBLE);
                    dialogView.findViewById(R.id.ll_growthDialogView_headTableLayout).getLayoutParams().height =
                            getResources().getDimensionPixelSize(R.dimen.weight_table_height);
                    //Revert the icon
                    scrollButton.setImageResource(R.drawable.ic_icon_collapse);
                }
            }
        });

        try {
            refreshGrowthChart(dialogView, gender, dob);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        try {
            refreshPreviousHeadsTable(dialogView, gender, dob);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return dialogView;
    }

    private void refreshPreviousHeadsTable(final ViewGroup dialogView, Gender gender, Date dob) {

        if (minMeasureDate == null || maxMeasureDate == null) {
            return;
        }

        TableLayout tableLayout = dialogView.findViewById(R.id.head_circ_table);
        for (HeadCircumference headCircumference : headCircumferences) {
            TableRow dividerRow = new TableRow(dialogView.getContext());
            View divider = new View(dialogView.getContext());
            TableRow.LayoutParams params = (TableRow.LayoutParams) divider.getLayoutParams();
            if (params == null) params = new TableRow.LayoutParams();
            params.width = TableRow.LayoutParams.MATCH_PARENT;
            params.height = getResources().getDimensionPixelSize(R.dimen.weight_table_divider_height);
            params.span = 3;
            divider.setLayoutParams(params);
            divider.setBackgroundColor(getResources().getColor(R.color.client_list_header_dark_grey));
            dividerRow.addView(divider);
            tableLayout.addView(dividerRow);

            TableRow curRow = new TableRow(dialogView.getContext());

            TextView ageTextView = new TextView(dialogView.getContext());
            ageTextView.setHeight(getResources().getDimensionPixelSize(R.dimen.table_contents_text_height));
            ageTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.weight_table_contents_text_size));
            ageTextView.setText(DateUtil.getDuration(headCircumference.getDate().getTime() - dob.getTime()));
            ageTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            ageTextView.setTextColor(getResources().getColor(R.color.client_list_grey));
            curRow.addView(ageTextView);

            TextView headTextView = new TextView(dialogView.getContext());
            headTextView.setHeight(getResources().getDimensionPixelSize(R.dimen.table_contents_text_height));
            headTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.weight_table_contents_text_size));
            headTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            headTextView.setText(
                    String.format("%s %s", String.valueOf(headCircumference.getInch()), getString(R.string.in)));
            headTextView.setTextColor(getResources().getColor(R.color.client_list_grey));
            curRow.addView(headTextView);

            TextView zScoreTextView = new TextView(dialogView.getContext());
            zScoreTextView.setHeight(getResources().getDimensionPixelSize(R.dimen.table_contents_text_height));
            zScoreTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimension(R.dimen.weight_table_contents_text_size));
            zScoreTextView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            if (headCircumference.getDate().compareTo(maxMeasureDate.getTime()) > 0) {
                zScoreTextView.setText("");
            } else {
                double zScore = HCZScore.calculate(gender, dob, headCircumference.getDate(), headCircumference.getInch());
                zScore = HCZScore.roundOff(zScore);
                zScoreTextView.setTextColor(getResources().getColor(HCZScore.getZScoreColor(zScore)));
                zScoreTextView.setText(String.valueOf(zScore));
            }
            curRow.addView(zScoreTextView);
            tableLayout.addView(curRow);
        }

        //Now set the expand button if items are too many
        final ScrollView headsTableScrollView = dialogView.findViewById(R.id.head_scroll_view);
        getHeight(headsTableScrollView, new ViewMeasureListener() {
            @Override
            public void onCompletedMeasuring(int height) {
                int childHeight = headsTableScrollView.getChildAt(0).getMeasuredHeight();
                ImageButton scrollButton = dialogView.findViewById(R.id.scroll_button);
                if (childHeight > height) {
                    scrollButton.setVisibility(View.VISIBLE);
                } else {
                    scrollButton.setVisibility(View.GONE);
                }
            }
        });

    }

    private void refreshGrowthChart(ViewGroup parent, Gender gender, Date dob) {
        if (minMeasureDate == null || maxMeasureDate == null) {
            return;
        }

        if (gender != Gender.UNKNOWN && dob != null && minMeasureDate != null) {
            LineChartView headChart = parent.findViewById(R.id.hc_growth_chart);
            double minAge = HCZScore.getAgeInMonths(dob, minMeasureDate.getTime());
            double maxAge = minAge + GRAPH_MONTHS_TIMELINE;
            List<Line> lines = new ArrayList<>();
            for (int z = -3; z <= 3; z++) {
                if (z != 1 && z != -1) {
                    Line curLine = getZScoreLine(gender, minAge, maxAge, z,
                            getActivity().getResources().getColor(HCZScore.getZScoreColor(z)));
                    if (z == -3) {
                        curLine.setPathEffect(new DashPathEffect(new float[]{10, 20}, 0));
                    }
                    lines.add(curLine);
                }
            }

            lines.add(getTodayLine(gender, dob, minAge, maxAge));
            lines.add(getPersonHeadLine(gender, dob));

            List<AxisValue> bottomAxisValues = new ArrayList<>();
            for (int i = (int) Math.round(Math.floor(minAge)); i <= (int) Math.round(Math.ceil(maxAge)); i++) {
                AxisValue curValue = new AxisValue((float) i);
                curValue.setLabel(String.valueOf(i));
                bottomAxisValues.add(curValue);
            }

            LineChartData data = new LineChartData();
            data.setLines(lines);

            Axis bottomAxis = new Axis(bottomAxisValues);
            bottomAxis.setHasLines(true);
            bottomAxis.setHasTiltedLabels(false);
            bottomAxis.setName(getString(R.string.months));
            data.setAxisXBottom(bottomAxis);

            Axis leftAxis = new Axis();
            leftAxis.setHasLines(true);
            leftAxis.setHasTiltedLabels(false);
            leftAxis.setAutoGenerated(true);
            leftAxis.setName(getString(R.string.in));
            data.setAxisYLeft(leftAxis);

            Axis topAxis = new Axis();
            topAxis.setHasTiltedLabels(false);
            topAxis.setAutoGenerated(false);
            data.setAxisXTop(topAxis);

            Axis rightAxis = new Axis();
            rightAxis.setHasTiltedLabels(false);
            rightAxis.setAutoGenerated(false);
            data.setAxisYRight(rightAxis);

            headChart.setLineChartData(data);
        }

    }

    private Line getTodayLine(Gender gender, Date dob, double minAge, double maxAge) {
        double personsAgeInMonthsToday = HCZScore.getAgeInMonths(dob, Calendar.getInstance().getTime());
        double maxY = getMaxY(dob, maxAge, gender);
        double minY = getMinY(dob, minAge, gender);

        if (personsAgeInMonthsToday > HCZScore.MAX_REPRESENTED_AGE) {
            personsAgeInMonthsToday = HCZScore.MAX_REPRESENTED_AGE;
        }

        List<PointValue> values = new ArrayList<>();
        values.add(new PointValue((float) personsAgeInMonthsToday, (float) minY));
        values.add(new PointValue((float) personsAgeInMonthsToday, (float) maxY));

        Line todayLine = new Line(values);
        todayLine.setColor(getResources().getColor(R.color.growth_today_color));
        todayLine.setHasPoints(false);
        todayLine.setHasLabels(false);
        todayLine.setStrokeWidth(4);

        return todayLine;
    }

    private double getMaxY(Date dob, double maxAge, Gender gender) {
        if (minMeasureDate == null || maxMeasureDate == null) {
            return 0d;
        }

        double maxY = HCZScore.reverse(gender, maxAge, 3d);

        for (HeadCircumference curHead : headCircumferences) {
            if (isHeadOkToDisplay(minMeasureDate, maxMeasureDate, curHead) && curHead.getInch() > maxY) {
                maxY = curHead.getInch();
            }
        }

        return maxY;
    }

    private double getMinY(Date dob, double minAge, Gender gender) {
        if (minMeasureDate == null || maxMeasureDate == null) {
            return 0d;
        }

        double minY = HCZScore.reverse(gender, minAge, -3d);

        for (HeadCircumference curHead : headCircumferences) {
            if (isHeadOkToDisplay(minMeasureDate, maxMeasureDate, curHead) && curHead.getInch() < minY) {
                minY = curHead.getInch();
            }
        }

        return minY;
    }

    private Line getPersonHeadLine(Gender gender, Date dob) {
        if (minMeasureDate == null || maxMeasureDate == null) {
            return null;
        }

        List<PointValue> values = new ArrayList<>();
        for (HeadCircumference curHead : headCircumferences) {
            if (isHeadOkToDisplay(minMeasureDate, maxMeasureDate, curHead)) {
                Calendar measureDate = Calendar.getInstance();
                measureDate.setTime(curHead.getDate());
                standardiseCalendarDate(measureDate);
                double x = HCZScore.getAgeInMonths(dob, measureDate.getTime());
                double y = curHead.getInch();
                values.add(new PointValue((float) x, (float) y));
            }
        }

        Line line = new Line(values);
        line.setColor(getResources().getColor(android.R.color.black));
        line.setStrokeWidth(4);
        line.setHasPoints(true);
        line.setHasLabels(false);
        return line;
    }

    private boolean isHeadOkToDisplay(Calendar minMeasureDate, Calendar maxMeasureDate,
                                        HeadCircumference headCircumference) {
        if (minMeasureDate != null && maxMeasureDate != null
                && minMeasureDate.getTimeInMillis() <= maxMeasureDate.getTimeInMillis()
                && headCircumference.getDate() != null) {
            Calendar measureDate = Calendar.getInstance();
            measureDate.setTime(headCircumference.getDate());
            standardiseCalendarDate(measureDate);

            return measureDate.getTimeInMillis() >= minMeasureDate.getTimeInMillis()
                    && measureDate.getTimeInMillis() <= maxMeasureDate.getTimeInMillis();
        }

        return false;
    }

    private Calendar[] getMinAndMaxMeasureDates(Date dob) {
        Calendar minGraphTime = null;
        Calendar maxGraphTime = null;
        if (dob != null) {
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.setTime(dob);
            standardiseCalendarDate(dobCalendar);

            minGraphTime = Calendar.getInstance();
            maxGraphTime = Calendar.getInstance();

            if (HCZScore.getAgeInMonths(dob, maxGraphTime.getTime()) > HCZScore.MAX_REPRESENTED_AGE) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dob);
                cal.add(Calendar.MONTH, (int) Math.round(HCZScore.MAX_REPRESENTED_AGE));
                maxGraphTime = cal;
                minGraphTime = (Calendar) maxGraphTime.clone();
            }

            minGraphTime.add(Calendar.MONTH, -GRAPH_MONTHS_TIMELINE);
            standardiseCalendarDate(minGraphTime);
            standardiseCalendarDate(maxGraphTime);

            if (minGraphTime.getTimeInMillis() < dobCalendar.getTimeInMillis()) {
                minGraphTime.setTime(dob);
                standardiseCalendarDate(minGraphTime);

                maxGraphTime = (Calendar) minGraphTime.clone();
                maxGraphTime.add(Calendar.MONTH, GRAPH_MONTHS_TIMELINE);
            }
        }

        return new Calendar[]{minGraphTime, maxGraphTime};
    }

    private Line getZScoreLine(Gender gender, double startAgeInMonths, double endAgeInMonths, double z, int color) {
        List<PointValue> values = new ArrayList<>();
        while (startAgeInMonths <= endAgeInMonths) {
            Double headCircumference = HCZScore.reverse(gender, startAgeInMonths, z);

            if (headCircumference != null) {
                values.add(new PointValue((float) startAgeInMonths, (float) headCircumference.doubleValue()));
            }

            startAgeInMonths++;
        }

        Line line = new Line(values);
        line.setColor(color);
        line.setHasPoints(false);
        line.setHasLabels(true);
        line.setStrokeWidth(2);
        return line;
    }

    @Override
    public void onStart() {
        super.onStart();

        // without a handler, the window sizes itself correctly
        // but the keyboard does not show up
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Window window = null;
                if (getDialog() != null) {
                    window = getDialog().getWindow();
                }

                if (window == null) {
                    return;
                }

                Point size = new Point();

                Display display = window.getWindowManager().getDefaultDisplay();
                display.getSize(size);

                int width = size.x;

                window.setLayout((int) (width * 0.9), FrameLayout.LayoutParams.WRAP_CONTENT);
                window.setGravity(Gravity.CENTER);
            }
        });
    }

    private static void standardiseCalendarDate(Calendar calendarDate) {
        calendarDate.set(Calendar.HOUR_OF_DAY, 0);
        calendarDate.set(Calendar.MINUTE, 0);
        calendarDate.set(Calendar.SECOND, 0);
        calendarDate.set(Calendar.MILLISECOND, 0);
    }

    private void getHeight (final View view, final ViewMeasureListener viewMeasureListener) {
        if (view == null) {
            if (viewMeasureListener != null) {
                viewMeasureListener.onCompletedMeasuring(0);
            }

            return;
        }

        int measuredHeight = view.getMeasuredHeight();

        if (measuredHeight > 0) {
            if (viewMeasureListener != null) {
                viewMeasureListener.onCompletedMeasuring(measuredHeight);
            }

            return;
        }

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (viewMeasureListener != null) {
                    viewMeasureListener.onCompletedMeasuring(view.getMeasuredHeight());
                }

                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

}