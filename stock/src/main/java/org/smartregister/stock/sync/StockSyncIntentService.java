package org.smartregister.stock.sync;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Response;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.service.ActionService;
import org.smartregister.service.HTTPAgent;
import org.smartregister.stock.R;
import org.smartregister.stock.StockLibrary;
import org.smartregister.stock.domain.Stock;
import org.smartregister.stock.repository.StockRepository;
import org.smartregister.stock.util.NetworkUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

/**
 * Created by samuelgithengi on 2/16/18.
 */

public class StockSyncIntentService extends IntentService {
    private static final String STOCK_Add_PATH = "/rest/stockresource/add/";
    private static final String STOCK_SYNC_PATH = "rest/stockresource/sync/";

    private Context context;
    private HTTPAgent httpAgent;
    private ActionService actionService;


    public StockSyncIntentService() {
        super("StockSyncIntentService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getBaseContext();
        httpAgent = StockLibrary.getInstance().getContext().getHttpAgent();
        actionService = StockLibrary.getInstance().getContext().actionService();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent workIntent) {

        if (StockLibrary.getInstance().getContext().IsUserLoggedOut()) {
            logInfo("Not updating from server as user is not logged in.");
            return;
        }

        if (NetworkUtils.isNetworkAvailable(context)) {
            // push
            pushStockToServer();

            // pull
            pullStockFromServer();
            actionService.fetchNewActions();

        }
    }


    private void pullStockFromServer() {
        final String LAST_STOCK_SYNC = "last_stock_sync";
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
        String anmId = allSharedPreferences.fetchRegisteredANM();
        String baseUrl = StockLibrary.getInstance().getContext().configuration().dristhiBaseURL();
        if (baseUrl.endsWith(context.getString(R.string.url_separator))) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(R.string.url_separator)));
        }

        while (true) {
            long timestamp = preferences.getLong(LAST_STOCK_SYNC, 0);
            String timeStampString = String.valueOf(timestamp);
            String uri = MessageFormat.format("{0}/{1}?providerid={2}&serverVersion={3}",
                    baseUrl,
                    STOCK_SYNC_PATH,
                    anmId,
                    timeStampString
            );
            Response<String> response = httpAgent.fetch(uri);
            if (response.isFailure()) {
                logError("Stock pull failed.");
                return;
            }
            String jsonPayload = response.payload();
            ArrayList<Stock> Stock_arrayList = getStockFromPayload(jsonPayload);
            Long highestTimestamp = getHighestTimestampFromStockPayLoad(jsonPayload);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(LAST_STOCK_SYNC, highestTimestamp);
            editor.commit();
            if (Stock_arrayList.isEmpty()) {
                return;
            } else {
                StockRepository stockRepository = StockLibrary.getInstance().getStockRepository();
                for (int j = 0; j < Stock_arrayList.size(); j++) {
                    Stock fromServer = Stock_arrayList.get(j);
                    List<Stock> existingStock = stockRepository.findUniqueStock(fromServer.getStockTypeId(), fromServer.getTransactionType(), fromServer.getProviderid(),
                            String.valueOf(fromServer.getValue()), String.valueOf(fromServer.getDateCreated()), fromServer.getToFrom());
                    if (!existingStock.isEmpty()) {
                        for (Stock stock : existingStock) {
                            fromServer.setId(stock.getId());
                        }
                    }
                    stockRepository.add(fromServer);
                }

            }
        }
    }

    private Long getHighestTimestampFromStockPayLoad(String jsonPayload) {
        Long toreturn = 0l;
        try {
            JSONObject stockContainer = new JSONObject(jsonPayload);
            if (stockContainer.has(context.getString(R.string.stocks_key))) {
                JSONArray stockArray = stockContainer.getJSONArray(context.getString(R.string.stocks_key));
                for (int i = 0; i < stockArray.length(); i++) {

                    JSONObject stockObject = stockArray.getJSONObject(i);
                    if (stockObject.getLong(context.getString(R.string.server_version_key)) > toreturn) {
                        toreturn = stockObject.getLong(context.getString(R.string.server_version_key));
                    }

                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        return toreturn;
    }

    private ArrayList<Stock> getStockFromPayload(String jsonPayload) {
        ArrayList<Stock> Stock_arrayList = new ArrayList<>();
        try {
            JSONObject stockcontainer = new JSONObject(jsonPayload);
            if (stockcontainer.has(context.getString(R.string.stocks_key))) {
                JSONArray stockArray = stockcontainer.getJSONArray(context.getString(R.string.stocks_key));
                for (int i = 0; i < stockArray.length(); i++) {
                    JSONObject stockObject = stockArray.getJSONObject(i);
                    Stock stock = new Stock(null,
                            stockObject.getString(context.getString(R.string.transaction_type_key)),
                            stockObject.getString(context.getString(R.string.providerid_key)),
                            stockObject.getInt(context.getString(R.string.value_key)),
                            stockObject.getLong(context.getString(R.string.date_created_key)),
                            stockObject.getString(context.getString(R.string.to_from_key)),
                            BaseRepository.TYPE_Synced,
                            stockObject.getLong(context.getString(R.string.date_updated_key)),
                            stockObject.getString(context.getString(R.string.stock_type_id_key)));
                    Stock_arrayList.add(stock);
                }
            }
        } catch (Exception e) {
            Log.e(getClass().getCanonicalName(), e.getMessage());
        }
        return Stock_arrayList;
    }

    private void pushStockToServer() {
        boolean keepSyncing = true;
        int limit = 50;

        try {

            while (keepSyncing) {
                StockRepository stockRepository = StockLibrary.getInstance().getStockRepository();
                ArrayList<Stock> stocks = (ArrayList<Stock>) stockRepository.findUnSyncedWithLimit(limit);
                JSONArray stocksarray = createJsonArrayFromStockArray(stocks);
                if (stocks.isEmpty()) {
                    return;
                }

                String baseUrl = StockLibrary.getInstance().getContext().configuration().dristhiBaseURL();
                if (baseUrl.endsWith(context.getString(R.string.url_separator))) {
                    baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(context.getString(R.string.url_separator)));
                }
                // create request body
                JSONObject request = new JSONObject();
                request.put(context.getString(R.string.stocks_key), stocksarray);

                String jsonPayload = request.toString();
                Response<String> response = httpAgent.post(
                        MessageFormat.format("{0}/{1}",
                                baseUrl,
                                STOCK_Add_PATH),
                        jsonPayload);
                if (response.isFailure()) {
                    Log.e(getClass().getName(), "Stocks sync failed.");
                    return;
                }
                stockRepository.markEventsAsSynced(stocks);
                Log.i(getClass().getName(), "Stocks synced successfully.");
            }
        } catch (JSONException e) {
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    private JSONArray createJsonArrayFromStockArray(ArrayList<Stock> stocks) {
        JSONArray array = new JSONArray();
        for (int i = 0; i < stocks.size(); i++) {
            JSONObject stock = new JSONObject();
            try {
                stock.put("identifier", stocks.get(i).getId());
                stock.put(context.getString(R.string.stock_type_id_key), stocks.get(i).getStockTypeId());
                stock.put(context.getString(R.string.transaction_type_key), stocks.get(i).getTransactionType());
                stock.put(context.getString(R.string.providerid_key), stocks.get(i).getProviderid());
                stock.put(context.getString(R.string.date_created_key), stocks.get(i).getDateCreated());
                stock.put(context.getString(R.string.value_key), stocks.get(i).getValue());
                stock.put(context.getString(R.string.to_from_key), stocks.get(i).getToFrom());
                stock.put(context.getString(R.string.date_updated_key), stocks.get(i).getUpdatedAt());
                array.put(stock);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return array;
    }

}
