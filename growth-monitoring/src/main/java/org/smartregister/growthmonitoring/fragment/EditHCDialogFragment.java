package org.smartregister.growthmonitoring.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Selection;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.R;
import org.smartregister.growthmonitoring.domain.HCWrapper;
import org.smartregister.growthmonitoring.listener.HCActionListener;
import org.smartregister.growthmonitoring.repository.HeadCircumferenceRepository;
import org.smartregister.growthmonitoring.util.ImageUtils;
import org.smartregister.util.DatePickerUtils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("ValidFragment")
public class EditHCDialogFragment extends DialogFragment {
    private final Context context;
    private final HCWrapper tag;
    private HCActionListener listener;
    public static final String DIALOG_TAG = "EditHCDialogFragment";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private DateTime currentHCDate;
    private Float currentHC;

    private Date dateOfBirth;

    private EditHCDialogFragment(Context context, Date dateOfBirth, HCWrapper tag) {
        this.context = context;
        this.dateOfBirth = dateOfBirth;
        if (tag == null) {
            this.tag = new HCWrapper();
        } else {
            this.tag = tag;
        }
    }

    public static EditHCDialogFragment newInstance(Context context, Date dateOfBirth, HCWrapper tag) {
        return new EditHCDialogFragment(context, dateOfBirth, tag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.edit_hc_dialog_view, container, false);

        final EditText editHC = dialogView.findViewById(R.id.edit_head);
        if (tag.getHeadCircumference() != null) {
            editHC.setText(tag.getHeadCircumference().toString());
            editHC.setSelection(editHC.getText().length());
            currentHC = tag.getHeadCircumference();
        }

        if (tag.getUpdatedHCDate() != null) {
            currentHCDate = tag.getUpdatedHCDate();
        }

        final DatePicker earlierDatePicker = dialogView.findViewById(R.id.earlier_date_picker);
        earlierDatePicker.setMaxDate(Calendar.getInstance().getTimeInMillis());
        if (dateOfBirth != null) {
            earlierDatePicker.setMinDate(dateOfBirth.getTime());
        }

        TextView nameView = dialogView.findViewById(R.id.child_name);
        nameView.setText(tag.getPatientName());

        TextView numberView = dialogView.findViewById(R.id.child_zeir_id);
        if (StringUtils.isNotBlank(tag.getPatientNumber())) {
            numberView.setText(String.format("%s: %s", getString(R.string.label_zeir), tag.getPatientNumber()));
        } else {
            numberView.setText("");
        }

        TextView ageView = dialogView.findViewById(R.id.child_age);
        if (StringUtils.isNotBlank(tag.getPatientAge())) {
            ageView.setText(String.format("%s: %s", getString(R.string.age), tag.getPatientAge()));
        } else {
            ageView.setText("");
        }

        TextView pmtctStatusView = dialogView.findViewById(R.id.pmtct_status);
        pmtctStatusView.setText(tag.getPmtctStatus());

        if (tag.getId() != null) {
            ImageView mImageView = dialogView.findViewById(R.id.child_profilepic);

            if (tag.getId() != null) {//image already in local storage most likely ):
                //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                mImageView.setTag(R.id.entity_id, tag.getId());
                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(tag.getId(), OpenSRPImageLoader.getStaticImageListener(mImageView, ImageUtils.profileImageResourceByGender(tag.getGender()), ImageUtils.profileImageResourceByGender(tag.getGender())));
            }
        }


        final Button set = dialogView.findViewById(R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String headCircString = editHC.getText().toString();
                if (StringUtils.isBlank(headCircString) || Float.valueOf(headCircString) <= 0f) {
                    return;
                }

                dismiss();

                boolean hcChanged = false;
                boolean dateChanged = false;

                if (earlierDatePicker.getVisibility() == View.VISIBLE) {
                    int day = earlierDatePicker.getDayOfMonth();
                    int month = earlierDatePicker.getMonth();
                    int year = earlierDatePicker.getYear();

                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day);

                    if (!org.apache.commons.lang3.time.DateUtils.isSameDay(calendar.getTime(), currentHCDate.toDate())) {
                        tag.setUpdatedHCDate(new DateTime(calendar.getTime()), false);
                        dateChanged = true;
                    }
                }


                Float headCircumference = Float.valueOf(headCircString);
                if (!headCircumference.equals(currentHC)) {
                    tag.setHeadCircumference(headCircumference);
                    hcChanged = true;
                }

                if (hcChanged || dateChanged) {
                    listener.onHCTaken(tag);
                }

            }
        });

        final Button hcDelete = dialogView.findViewById(R.id.head_delete);
        hcDelete.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                HeadCircumferenceRepository headCircumferenceRepository = GrowthMonitoringLibrary.getInstance().headCircumferenceRepository();
                headCircumferenceRepository.delete(String.valueOf(tag.getDbKey()));
                listener.onHCTaken(null);

            }
        });
        if (tag.getUpdatedHCDate() != null) {
            ((TextView) dialogView.findViewById(R.id.service_date)).setText("Date measured: " + tag.getUpdatedHCDate().dayOfMonth().get() + "-" + tag.getUpdatedHCDate().monthOfYear().get() + "-" + tag.getUpdatedHCDate().year().get() + "");
        } else {
            dialogView.findViewById(R.id.service_date).setVisibility(View.GONE);
            hcDelete.setVisibility(View.GONE);
        }

        final Button hcTakenEarlier = dialogView.findViewById(R.id.hc_taken_earlier);
        hcTakenEarlier.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                hcTakenEarlier.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                earlierDatePicker.setVisibility(View.VISIBLE);
                earlierDatePicker.requestFocus();
                set.setVisibility(View.VISIBLE);

                DatePickerUtils.themeDatePicker(earlierDatePicker, new char[]{'d', 'm', 'y'});

                earlierDatePicker.updateDate(currentHCDate.year().get(), currentHCDate.monthOfYear().get() - 1, currentHCDate.dayOfMonth().get());
            }
        });

        Button cancel = dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        return dialogView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the WeightActionListener so we can send events to the host
            listener = (HCActionListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement WeightActionListener");
        }
    }

    private void formatEditHCView(EditText editHC, String userInput) {
        StringBuilder stringBuilder = new StringBuilder(userInput);

        while (stringBuilder.length() > 2 && stringBuilder.charAt(0) == '0') {
            stringBuilder.deleteCharAt(0);
        }
        while (stringBuilder.length() < 2) {
            stringBuilder.insert(0, '0');
        }
        stringBuilder.insert(stringBuilder.length() - 1, '.');

        editHC.setText(stringBuilder.toString());
        // keeps the cursor always to the right
        Selection.setSelection(editHC.getText(), stringBuilder.toString().length());
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

                window.setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

            }
        });

    }
}
