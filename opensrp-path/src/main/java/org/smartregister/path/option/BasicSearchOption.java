package org.smartregister.path.option;

import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.path.R;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.view.contract.SmartRegisterClient;
import org.smartregister.view.dialog.FilterOption;

public class BasicSearchOption implements FilterOption {
    public enum Type {
        CHILD, WOMAN
    }

    private String filter;

    public BasicSearchOption(String filter) {
        this.filter = filter;
    }

    // FIXME path_conflict
    //@Override
    public void setFilter(String filter) {
        this.filter = filter;
    }

    // FIXME path_conflict
    //@Override
    public String getCriteria() {
        return filter;
    }

    @Override
    public boolean filter(SmartRegisterClient client) {
        CommonPersonObjectClient currentclient = (CommonPersonObjectClient) client;
        if (currentclient.getDetails().get("first_name") != null
                && currentclient.getDetails().get("first_name").toLowerCase().contains(filter.toLowerCase())) {
            return true;
        }
        if (currentclient.getDetails().get("zeir_id") != null
                && currentclient.getDetails().get("zeir_id").equalsIgnoreCase(filter)) {
            return true;
        }
        if (currentclient.getDetails().get("existing_zeir_id") != null
                && currentclient.getDetails().get("existing_zeir_id").equalsIgnoreCase(filter)) {
            return true;
        }
        if (currentclient.getDetails().get("epi_card_number") != null
                && currentclient.getDetails().get("epi_card_number").contains(filter)) {
            return true;
        }
        if (currentclient.getDetails().get("father_name") != null
                && currentclient.getDetails().get("father_name").contains(filter)) {
            return true;
        }
        if (currentclient.getDetails().get("mother_first_name") != null
                && currentclient.getDetails().get("mother_first_name").contains(filter)) {
            return true;
        }
        if (currentclient.getDetails().get("mother_last_name") != null
                && currentclient.getDetails().get("mother_last_name").contains(filter)) {
            return true;
        }
        if (currentclient.getDetails().get("husband_name") != null
                && currentclient.getDetails().get("husband_name").contains(filter)) {
            return true;
        }
        return currentclient.getDetails().get("contact_phone_number") != null
                && currentclient.getDetails().get("contact_phone_number").contains(filter);
    }

    @Override
    public String name() {
        return VaccinatorApplication.getInstance().context().applicationContext().getResources().getString(R.string.search_hint);
    }
}
