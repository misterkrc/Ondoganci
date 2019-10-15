package org.smartregister.path.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.twotoasters.sectioncursoradapter.SectionCursorAdapter;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.commonregistry.CommonPersonObject;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.path.R;
import org.smartregister.path.provider.AdvancedSearchClientsProvider;

/**
 * Created by keyman on 4/5/17.
 */
public class AdvancedSearchPaginatedCursorAdapter extends SectionCursorAdapter {
    private final AdvancedSearchClientsProvider listItemProvider;
    private final CommonRepository commonRepository;

    public AdvancedSearchPaginatedCursorAdapter(Context context, Cursor c, AdvancedSearchClientsProvider listItemProvider, CommonRepository commonRepository) {
        super(context, c);
        this.listItemProvider = listItemProvider;
        this.commonRepository = commonRepository;
    }

    @Override
    public View newItemView(Context context, Cursor cursor, ViewGroup parent) {
        return listItemProvider.inflatelayoutForCursorAdapter();
    }

    @Override
    public void bindItemView(View view, Context context, Cursor cursor) {
        CommonPersonObject personinlist = commonRepository.readAllcommonforCursorAdapter(cursor);
        CommonPersonObjectClient pClient = new CommonPersonObjectClient(personinlist.getCaseId(), personinlist.getDetails(), personinlist.getDetails().get("FWHOHFNAME"));
        pClient.setColumnmaps(personinlist.getColumnmaps());
        listItemProvider.getView(cursor, pClient, view);
    }

    @Override
    protected Object getSectionFromCursor(Cursor cursor) {

        if (cursor != null && cursor.getCount() > 0) {
            String inactive = "";
            int index = cursor.getColumnIndex("inactive");
            if (index != -1) {
                inactive = cursor.getString(index);
            }

            if (StringUtils.isNotBlank(inactive) && inactive.equals(Boolean.TRUE.toString())) {
                return "INACTIVE OR LOST TO FOLLOW-UP";
            }

            String lostToFollowUp = "";
            index = cursor.getColumnIndex("lost_to_follow_up");
            if (index != -1) {
                lostToFollowUp = cursor.getString(index);
            }

            if (StringUtils.isNotBlank(lostToFollowUp) && lostToFollowUp.equals(Boolean.TRUE.toString())) {
                return "INACTIVE OR LOST TO FOLLOW-UP";
            }
        }
        return "ACTIVE";
    }

    @Override
    protected View newSectionView(Context context, Object item, ViewGroup parent) {
        return getLayoutInflater().inflate(R.layout.advanced_search_section, parent, false);
    }

    @Override
    protected void bindSectionView(View convertView, Context context, int position, Object item) {
        ((TextView) convertView).setText((String) item);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        return super.swapCursor(newCursor);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }
}
