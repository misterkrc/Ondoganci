package org.smartregister.path.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.path.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by keyman on 12/22/17.
 */

public class SpinnerAdapter extends ArrayAdapter<Date> {

    private Context context;
    private SimpleDateFormat simpleDateFormat;
    private String firstSuffix;


    public SpinnerAdapter(Context context, int resource, List<Date> objects, SimpleDateFormat simpleDateFormat) {
        super(context, resource, objects);
        this.context = context;
        this.simpleDateFormat = simpleDateFormat;
    }

    public void setFirstSuffix(String firstSuffix) {
        this.firstSuffix = firstSuffix;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_spinner, parent, false);
        } else {
            view = convertView;
        }

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            Date date = getItem(position);

            String dateString = simpleDateFormat.format(date);
            if (position == 0 && StringUtils.isNoneBlank(firstSuffix)) {
                dateString = dateString + " " + firstSuffix;
            }
            textView.setText(dateString);
            textView.setTag(date);
        }
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_spinner_drop_down, parent, false);
        } else {
            view = convertView;
        }

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            Date date = getItem(position);

            String dateString = simpleDateFormat.format(date);
            if (position == 0 && StringUtils.isNoneBlank(firstSuffix)) {
                dateString = dateString + " " + firstSuffix;
            }
            textView.setText(dateString);
            textView.setTag(date);
        }
        return view;
    }
}
