package org.smartregister.path.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.commonregistry.CommonFtsObject;
import org.smartregister.growthmonitoring.GrowthMonitoringLibrary;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.ZScoreRepository;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.immunization.domain.VaccineSchedule;
import org.smartregister.immunization.domain.jsonmapping.Vaccine;
import org.smartregister.immunization.domain.jsonmapping.VaccineGroup;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.repository.RecurringServiceTypeRepository;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.util.VaccinateActionUtils;
import org.smartregister.immunization.util.VaccinatorUtils;
import org.smartregister.path.BuildConfig;
import org.smartregister.path.R;
import org.smartregister.path.activity.LoginActivity;
import org.smartregister.path.receiver.CoverageDropoutBroadcastReceiver;
import org.smartregister.path.receiver.Hia2ServiceBroadcastReceiver;
import org.smartregister.path.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.path.receiver.VaccinatorAlarmReceiver;
import org.smartregister.path.repository.CohortIndicatorRepository;
import org.smartregister.path.repository.CohortPatientRepository;
import org.smartregister.path.repository.CohortRepository;
import org.smartregister.path.repository.CumulativeIndicatorRepository;
import org.smartregister.path.repository.CumulativePatientRepository;
import org.smartregister.path.repository.CumulativeRepository;
import org.smartregister.path.repository.DailyTalliesRepository;
import org.smartregister.path.repository.HIA2IndicatorsRepository;
import org.smartregister.path.repository.MonthlyTalliesRepository;
import org.smartregister.path.repository.PathRepository;
import org.smartregister.path.repository.PathStockHelperRepository;
import org.smartregister.path.repository.UniqueIdRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Hia2ReportRepository;
import org.smartregister.repository.Repository;
import org.smartregister.stock.StockLibrary;
import org.smartregister.stock.repository.StockRepository;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.receiver.TimeChangedBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.fabric.sdk.android.Fabric;
import util.PathConstants;

import static org.smartregister.util.Log.logError;
import static org.smartregister.util.Log.logInfo;

/**
 * Created by koros on 2/3/16.
 */
public class VaccinatorApplication extends DrishtiApplication
        implements TimeChangedBroadcastReceiver.OnTimeChangedListener {

    private static final String TAG = "VaccinatorApplication";
    private static CommonFtsObject commonFtsObject;
    private UniqueIdRepository uniqueIdRepository;
    private DailyTalliesRepository dailyTalliesRepository;
    private MonthlyTalliesRepository monthlyTalliesRepository;
    private HIA2IndicatorsRepository hIA2IndicatorsRepository;
    private EventClientRepository eventClientRepository;
    private StockRepository stockRepository;
    private CohortRepository cohortRepository;
    private CohortIndicatorRepository cohortIndicatorRepository;
    private CohortPatientRepository cohortPatientRepository;
    private CumulativeRepository cumulativeRepository;
    private CumulativeIndicatorRepository cumulativeIndicatorRepository;
    private CumulativePatientRepository cumulativePatientRepository;
    private Hia2ReportRepository hia2ReportRepository;
    private boolean lastModified;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;

        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        //Initialize Modules
        CoreLibrary.init(context());
        GrowthMonitoringLibrary.init(context(), getRepository(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);
        ImmunizationLibrary.init(context(), getRepository(), createCommonFtsObject(), BuildConfig.VERSION_CODE, BuildConfig.DATABASE_VERSION);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        Hia2ServiceBroadcastReceiver.init(this);
        SyncStatusBroadcastReceiver.init(this);
        CoverageDropoutBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.init(this);
        TimeChangedBroadcastReceiver.getInstance().addOnTimeChangedListener(this);

        applyUserLanguagePreference();
        cleanUpSyncState();
        initOfflineSchedules();
        setCrashlyticsUser(context);

        //Initialize stock lib and pass stock helper repository for external db functions
        StockLibrary.init(context, getRepository(), new PathStockHelperRepository(getRepository()));
    }

    public static synchronized VaccinatorApplication getInstance() {
        return (VaccinatorApplication) mInstance;
    }

    @Override
    public void logoutCurrentUser() {

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    protected void cleanUpSyncState() {
        context.allSharedPreferences().saveIsSyncInProgress(false);
    }


    @Override
    public void onTerminate() {
        logInfo("Application is terminating. Stopping Bidan Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        SyncStatusBroadcastReceiver.destroy(this);
        TimeChangedBroadcastReceiver.destroy(this);
        super.onTerminate();
    }

    protected void applyUserLanguagePreference() {
        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = context.allSharedPreferences().fetchLanguagePreference();
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            locale = new Locale(lang);
            updateConfiguration(config);
        }
    }

    private void updateConfiguration(Configuration config) {
        config.locale = locale;
        Locale.setDefault(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private static String[] getFtsSearchFields(String tableName) {
        if (tableName.equals(PathConstants.CHILD_TABLE_NAME)) {
            return new String[]{"zeir_id", "epi_card_number", "first_name", "last_name"};
        }
        return null;
    }

    private static String[] getFtsSortFields(String tableName) {


        if (tableName.equals(PathConstants.CHILD_TABLE_NAME)) {
            ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");
            List<String> names = new ArrayList<>();
            names.add("first_name");
            names.add("dob");
            names.add("zeir_id");
            names.add("last_interacted_with");
            names.add("inactive");
            names.add("lost_to_follow_up");
            names.add(PathConstants.EC_CHILD_TABLE.DOD);

            for (VaccineRepo.Vaccine vaccine : vaccines) {
                names.add("alerts." + VaccinateActionUtils.addHyphen(vaccine.display()));
            }

            return names.toArray(new String[names.size()]);
        }
        return null;
    }

    private static String[] getFtsTables() {
        return new String[]{PathConstants.CHILD_TABLE_NAME};
    }

    private static Map<String, Pair<String, Boolean>> getAlertScheduleMap() {
        ArrayList<VaccineRepo.Vaccine> vaccines = VaccineRepo.getVaccines("child");
        Map<String, Pair<String, Boolean>> map = new HashMap<>();
        for (VaccineRepo.Vaccine vaccine : vaccines) {
            map.put(vaccine.display(), Pair.create(PathConstants.CHILD_TABLE_NAME, false));
        }
        return map;
    }

    public static CommonFtsObject createCommonFtsObject() {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            }
        }
        commonFtsObject.updateAlertScheduleMap(getAlertScheduleMap());
        return commonFtsObject;
    }

    /**
     * This method sets the Crashlytics user to whichever username was used to log in last. It only
     * does so if the app is not built for debugging
     *
     * @param context The user's context
     */
    public static void setCrashlyticsUser(Context context) {
        if (!BuildConfig.DEBUG
                && context != null && context.userService() != null
                && context.userService().getAllSharedPreferences() != null) {
            Crashlytics.setUserName(context.userService().getAllSharedPreferences().fetchRegisteredANM());
        }
    }

    private void grantPhotoDirectoryAccess() {
        Uri uri = FileProvider.getUriForFile(this,
                "com.vijay.jsonwizard.fileprovider",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        grantUriPermission("com.vijay.jsonwizard", uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    @Override
    public Repository getRepository() {
        try {
            if (repository == null) {
                repository = new PathRepository(getInstance().getApplicationContext(), context());
                uniqueIdRepository();
                dailyTalliesRepository();
                monthlyTalliesRepository();
                hIA2IndicatorsRepository();
                eventClientRepository();
                stockRepository();
            }
        } catch (UnsatisfiedLinkError e) {
            logError("Error on getRepository: " + e);

        }
        return repository;
    }


    public WeightRepository weightRepository() {
        return GrowthMonitoringLibrary.getInstance().weightRepository();
    }

    public Context context() {
        return context;
    }

    public VaccineRepository vaccineRepository() {
        return ImmunizationLibrary.getInstance().vaccineRepository();
    }

    public ZScoreRepository zScoreRepository() {
        return GrowthMonitoringLibrary.getInstance().zScoreRepository();
    }

    public UniqueIdRepository uniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository((PathRepository) getRepository());
        }
        return uniqueIdRepository;
    }

    public DailyTalliesRepository dailyTalliesRepository() {
        if (dailyTalliesRepository == null) {
            dailyTalliesRepository = new DailyTalliesRepository((PathRepository) getRepository());
        }
        return dailyTalliesRepository;
    }

    public MonthlyTalliesRepository monthlyTalliesRepository() {
        if (monthlyTalliesRepository == null) {
            monthlyTalliesRepository = new MonthlyTalliesRepository((PathRepository) getRepository());
        }

        return monthlyTalliesRepository;
    }

    public HIA2IndicatorsRepository hIA2IndicatorsRepository() {
        if (hIA2IndicatorsRepository == null) {
            hIA2IndicatorsRepository = new HIA2IndicatorsRepository((PathRepository) getRepository());
        }
        return hIA2IndicatorsRepository;
    }

    public RecurringServiceTypeRepository recurringServiceTypeRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceTypeRepository();
    }

    public RecurringServiceRecordRepository recurringServiceRecordRepository() {
        return ImmunizationLibrary.getInstance().recurringServiceRecordRepository();
    }

    public EventClientRepository eventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository(getRepository());
        }
        return eventClientRepository;
    }

    public Hia2ReportRepository hia2ReportRepository() {
        if (hia2ReportRepository == null) {
            hia2ReportRepository = new Hia2ReportRepository(getRepository());
        }
        return hia2ReportRepository;
    }

    public StockRepository stockRepository() {
        if (stockRepository == null) {
            stockRepository = new StockRepository(getRepository());
        }
        return stockRepository;
    }

    public CohortRepository cohortRepository() {
        if (cohortRepository == null) {
            cohortRepository = new CohortRepository((PathRepository) getRepository());
        }
        return cohortRepository;
    }

    public CohortIndicatorRepository cohortIndicatorRepository() {
        if (cohortIndicatorRepository == null) {
            cohortIndicatorRepository = new CohortIndicatorRepository((PathRepository) getRepository());
        }
        return cohortIndicatorRepository;
    }

    public CohortPatientRepository cohortPatientRepository() {
        if (cohortPatientRepository == null) {
            cohortPatientRepository = new CohortPatientRepository((PathRepository) getRepository());
        }
        return cohortPatientRepository;
    }

    public CumulativeRepository cumulativeRepository() {
        if (cumulativeRepository == null) {
            cumulativeRepository = new CumulativeRepository((PathRepository) getRepository());
        }
        return cumulativeRepository;
    }

    public CumulativeIndicatorRepository cumulativeIndicatorRepository() {
        if (cumulativeIndicatorRepository == null) {
            cumulativeIndicatorRepository = new CumulativeIndicatorRepository((PathRepository) getRepository());
        }
        return cumulativeIndicatorRepository;
    }

    public CumulativePatientRepository cumulativePatientRepository() {
        if (cumulativePatientRepository == null) {
            cumulativePatientRepository = new CumulativePatientRepository((PathRepository) getRepository());
        }
        return cumulativePatientRepository;
    }

    public boolean isLastModified() {
        return lastModified;
    }

    public void setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
    }

    public static CommonFtsObject getCommonFtsObject() {
        return commonFtsObject;
    }

    @Override
    public void onTimeChanged() {
        Toast.makeText(this, R.string.device_time_changed, Toast.LENGTH_LONG).show();
        context.userService().forceRemoteLogin();
        logoutCurrentUser();
    }

    @Override
    public void onTimeZoneChanged() {
        Toast.makeText(this, R.string.device_timezone_changed, Toast.LENGTH_LONG).show();
        context.userService().forceRemoteLogin();
        logoutCurrentUser();
    }

    private void initOfflineSchedules() {
        try {
            List<VaccineGroup> childVaccines = VaccinatorUtils.getSupportedVaccines(this);
            List<Vaccine> specialVaccines = VaccinatorUtils.getSpecialVaccines(this);
            VaccineSchedule.init(childVaccines, specialVaccines, "child");
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static void setAlarms(android.content.Context context) {
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.WEIGHT_SYNC_PROCESSING_MINUTES, PathConstants.ServiceType.WEIGHT_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.VACCINE_SYNC_PROCESSING_MINUTES, PathConstants.ServiceType.VACCINE_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.RECURRING_SERVICES_SYNC_PROCESSING_MINUTES, PathConstants.ServiceType.RECURRING_SERVICES_SYNC_PROCESSING);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.DAILY_TALLIES_GENERATION_MINUTES, PathConstants.ServiceType.DAILY_TALLIES_GENERATION);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.COVERAGE_DROPOUT_GENERATION_MINUTES, PathConstants.ServiceType.COVERAGE_DROPOUT_GENERATION);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.IMAGE_UPLOAD_MINUTES, PathConstants.ServiceType.IMAGE_UPLOAD);
        VaccinatorAlarmReceiver.setAlarm(context, BuildConfig.PULL_UNIQUE_IDS_MINUTES, PathConstants.ServiceType.PULL_UNIQUE_IDS);
    }


}
