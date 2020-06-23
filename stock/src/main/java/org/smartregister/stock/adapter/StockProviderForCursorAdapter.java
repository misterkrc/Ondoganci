package org.smartregister.stock.adapter;

import android.view.LayoutInflater;
import android.view.View;

import org.smartregister.stock.domain.Stock;
import org.smartregister.view.contract.SmartRegisterClients;
import org.smartregister.view.dialog.FilterOption;
import org.smartregister.view.dialog.ServiceModeOption;
import org.smartregister.view.dialog.SortOption;
import org.smartregister.view.viewholder.OnClickFormLauncher;

/**
 * Created by raihan on 3/9/16.
 */
public interface StockProviderForCursorAdapter {
    void getView(Stock stock, View view);

    SmartRegisterClients updateClients(FilterOption villageFilter, ServiceModeOption serviceModeOption,
                                       FilterOption searchFilter, SortOption sortOption);

    void onServiceModeSelected(ServiceModeOption serviceModeOption);

    OnClickFormLauncher newFormLauncher(String formName, String entityId, String metaData);

    LayoutInflater inflater();

    View inflatelayoutForCursorAdapter();
}
