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
import org.smartregister.growthmonitoring.R;
import org.smartregister.growthmonitoring.domain.HCWrapper;
import org.smartregister.growthmonitoring.domain.WeightWrapper;
import org.smartregister.growthmonitoring.listener.HCActionListener;
import org.smartregister.growthmonitoring.listener.WeightActionListener;
import org.smartregister.growthmonitoring.util.ImageUtils;
import org.smartregister.util.DatePickerUtils;
import org.smartregister.util.OpenSRPImageLoader;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

@SuppressLint("ValidFragment")
public class RecordHCDialogFragment extends DialogFragment {
    private HCWrapper tag;
    private HCActionListener listener;
    private Date dateOfBirth;

    public static final String WRAPPER_TAG = "tag";
    public static final String DATE_OF_BIRTH_TAG = "dob";

    public static RecordHCDialogFragment newInstance(
            Date dateOfBirth, HCWrapper tag) {

        HCWrapper tagToSend;
        if (tag == null) {
            tagToSend = new HCWrapper();
        } else {
            tagToSend = tag;
        }

        RecordHCDialogFragment recordHCDialogFragment = new RecordHCDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable(DATE_OF_BIRTH_TAG, dateOfBirth);
        args.putSerializable(WRAPPER_TAG, tagToSend);
        recordHCDialogFragment.setArguments(args);

        return recordHCDialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        Serializable serializable = bundle.getSerializable(WRAPPER_TAG);
        if (serializable != null && serializable instanceof WeightWrapper) {
            tag = (HCWrapper) serializable;
        }

        if (tag == null) {
            return null;
        }

        Serializable dateSerializable = bundle.getSerializable(DATE_OF_BIRTH_TAG);
        if (dateSerializable != null && dateSerializable instanceof Date) {
            dateOfBirth = (Date) dateSerializable;
        }

        ViewGroup dialogView = (ViewGroup) inflater.inflate(R.layout.record_weight_dialog_view, container, false);

        final EditText editHC = dialogView.findViewById(R.id.edit_head);

        if (tag.getHeadCircumference() != null) {
            editHC.setText(tag.getHeadCircumference().toString());
            editHC.setSelection(editHC.getText().length());
        }
        //formatEditWeightView(editWeight, "");

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

            if (tag.getId() != null) {//image already in local storage most likey ):
                //set profile image by passing the client id.If the image doesn't exist in the image repository then download and save locally
                mImageView.setTag(R.id.entity_id, tag.getId());
                DrishtiApplication.getCachedImageLoaderInstance().getImageByClientId(tag.getId(),
                        OpenSRPImageLoader.getStaticImageListener(mImageView,
                                ImageUtils.profileImageResourceByGender(tag.getGender()),
                                ImageUtils.profileImageResourceByGender(tag.getGender())));
            }
        }

        final Button set = dialogView.findViewById(R.id.set);
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hcString = editHC.getText().toString();

                if (StringUtils.isBlank(hcString) || Float.valueOf(hcString) <= 0f) {
                    return;
                }

                dismiss();

                int day = earlierDatePicker.getDayOfMonth();
                int month = earlierDatePicker.getMonth();
                int year = earlierDatePicker.getYear();

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                tag.setUpdatedHCDate(new DateTime(calendar.getTime()), false);

                Float headCircumference = Float.valueOf(hcString);
                tag.setHeadCircumference(headCircumference);

                listener.onHCTaken(tag);

            }
        });

        final Button weightTakenToday = dialogView.findViewById(R.id.weight_taken_today);
        weightTakenToday.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hcString = editHC.getText().toString();
                if (StringUtils.isBlank(hcString) || Float.valueOf(hcString) <= 0f) {
                    return;
                }

                dismiss();

                Calendar calendar = Calendar.getInstance();
                tag.setUpdatedHCDate(new DateTime(calendar.getTime()), true);

                Float headCirc = Float.valueOf(hcString);
                tag.setHeadCircumference(headCirc);

                listener.onHCTaken(tag);

            }
        });

        final Button weightTakenEarlier = dialogView.findViewById(R.id.weight_taken_earlier);
        weightTakenEarlier.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                weightTakenEarlier.setVisibility(View.GONE);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                earlierDatePicker.setVisibility(View.VISIBLE);
                earlierDatePicker.requestFocus();
                set.setVisibility(View.VISIBLE);

                DatePickerUtils.themeDatePicker(earlierDatePicker, new char[]{'d', 'm', 'y'});
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
                    + " must implement HCActionListener");
        }
    }

    private void formatEditWeightView(EditText editHC, String userInput) {
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
        // without a handler, the window size itself correctly
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
