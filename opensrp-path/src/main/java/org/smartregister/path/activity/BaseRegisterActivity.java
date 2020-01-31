package org.smartregister.path.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.smartregister.Context;
import org.smartregister.domain.FetchStatus;
import org.smartregister.path.R;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.listener.CustomNavigationBarListener;
import org.smartregister.path.listener.NavigationItemListener;
import org.smartregister.path.receiver.SyncStatusBroadcastReceiver;
import org.smartregister.path.sync.ECSyncUpdater;
import org.smartregister.view.activity.DrishtiApplication;
import org.smartregister.view.activity.SecuredNativeSmartRegisterActivity;

import java.util.Calendar;

import util.ServiceTools;

/**
 * Base activity class for path regiters views
 * Created by keyman.
 */
public abstract class BaseRegisterActivity extends SecuredNativeSmartRegisterActivity
        implements NavigationView.OnNavigationItemSelectedListener, SyncStatusBroadcastReceiver.SyncStatusListener {

    public static final String IS_REMOTE_LOGIN = "is_remote_login";
    private Snackbar syncStatusSnackbar;
    private NavigationItemListener navigationItemListener = new NavigationItemListener(this);
    private CustomNavigationBarListener customNavigationBarListener = new CustomNavigationBarListener(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        BaseActivityToggle toggle = new BaseActivityToggle(this, drawer,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

            }
        };

        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null) {
            boolean isRemote = extras.getBoolean(IS_REMOTE_LOGIN);
            if (isRemote) {
                updateFromServer();
            }
        }
    }

    @Override
    public void onSyncStart() {
        refreshSyncStatusViews(null);
    }

    @Override
    public void onSyncInProgress(FetchStatus fetchStatus) {
        refreshSyncStatusViews(fetchStatus);
    }

    @Override
    public void onSyncComplete(FetchStatus fetchStatus) {
        refreshSyncStatusViews(fetchStatus);
    }

    public abstract void refreshList(FetchStatus fetchStatus);

    private void registerSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().addSyncStatusListener(this);
    }

    private void unregisterSyncStatusBroadcastReceiver() {
        SyncStatusBroadcastReceiver.getInstance().removeSyncStatusListener(this);
    }

    private void updateFromServer() {
        ServiceTools.startSyncService(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerSyncStatusBroadcastReceiver();
        initViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterSyncStatusBroadcastReceiver();
    }

    private void initViews() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        Button logoutButton = (Button) navigationView.findViewById(R.id.logout_b);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrishtiApplication application = (DrishtiApplication) getApplication();
                application.logoutCurrentUser();
                finish();
            }
        });

        ImageButton cancelButton = (ImageButton) navigationView.findViewById(R.id.cancel_b);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout) BaseRegisterActivity.this.findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });

        TextView initialsTV = (TextView) navigationView.findViewById(R.id.initials_tv);
        String preferredName = context().allSharedPreferences().getANMPreferredName(
                context().allSharedPreferences().fetchRegisteredANM());
        if (!TextUtils.isEmpty(preferredName)) {
            String[] initialsArray = preferredName.split(" ");
            String initials = "";
            if (initialsArray.length > 0) {
                initials = initialsArray[0].substring(0, 1);
                if (initialsArray.length > 1) {
                    initials = initials + initialsArray[1].substring(0, 1);
                }
            }

            initialsTV.setText(initials.toUpperCase());
        }

        TextView nameTV = (TextView) navigationView.findViewById(R.id.name_tv);
        nameTV.setText(preferredName);
        refreshSyncStatusViews(null);
        initializeCustomNavbarLIsteners();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return navigationItemListener.onNavigationItemSelected(item);
    }

    private void startSync() {
        ServiceTools.startSyncService(getApplicationContext());
    }

    /////////////////////////for custom navigation //////////////////////////////////////////////////////
    private void refreshSyncStatusViews(FetchStatus fetchStatus) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null && navigationView.getMenu() != null) {
            LinearLayout syncMenuItem = (LinearLayout) navigationView.findViewById(R.id.nav_sync);
            if (syncMenuItem != null) {
                if (SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
                    ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
                    if (syncStatusSnackbar != null) syncStatusSnackbar.dismiss();
                    syncStatusSnackbar = Snackbar.make(rootView, R.string.syncing,
                            Snackbar.LENGTH_LONG);
                    syncStatusSnackbar.show();
                    ((TextView) syncMenuItem.findViewById(R.id.nav_synctextview)).setText(R.string.syncing);
                } else {
                    if (fetchStatus != null) {
                        if (syncStatusSnackbar != null) syncStatusSnackbar.dismiss();
                        ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
                        if (fetchStatus.equals(FetchStatus.fetchedFailed)) {
                            syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_failed, Snackbar.LENGTH_INDEFINITE);
                            syncStatusSnackbar.setActionTextColor(getResources().getColor(R.color.snackbar_action_color));
                            syncStatusSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startSync();
                                }
                            });
                        } else if (fetchStatus.equals(FetchStatus.fetched)
                                || fetchStatus.equals(FetchStatus.nothingFetched)) {
                            syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_complete, Snackbar.LENGTH_LONG);
                        } else if (fetchStatus.equals(FetchStatus.noConnection)) {
                            syncStatusSnackbar = Snackbar.make(rootView, R.string.sync_failed_no_internet, Snackbar.LENGTH_LONG);
                        }
                        syncStatusSnackbar.show();
                    }

//<<<<<<< HEAD
//                    if (!TextUtils.isEmpty(lastSync)) {
//                        lastSync = " " + String.format(getString(R.string.last_sync), lastSync);
//                    }
//                    ((TextView)syncMenuItem.findViewById(R.id.nav_synctextview)).setText(String.format(getString(R.string.sync_), lastSync));
//=======
                    updateLastSyncText();
                }
            }
        }
    }

    private void initializeCustomNavbarLIsteners() {
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        LinearLayout syncMenuItem = (LinearLayout) drawer.findViewById(R.id.nav_sync);
        syncMenuItem.setOnClickListener(customNavigationBarListener);

        LinearLayout addchild = (LinearLayout) drawer.findViewById(R.id.nav_register);
        addchild.setOnClickListener(customNavigationBarListener);

        LinearLayout outofcatchment = (LinearLayout) drawer.findViewById(R.id.nav_record_vaccination_out_catchment);
        outofcatchment.setOnClickListener(customNavigationBarListener);

        LinearLayout stockregister = (LinearLayout) drawer.findViewById(R.id.stock_control);
        stockregister.setOnClickListener(customNavigationBarListener);

        LinearLayout hia2 = (LinearLayout) drawer.findViewById(R.id.hia2_reports);
        hia2.setOnClickListener(customNavigationBarListener);

        LinearLayout childregister = (LinearLayout) drawer.findViewById(R.id.child_register);
        childregister.setOnClickListener(customNavigationBarListener);

        LinearLayout coverage = (LinearLayout) drawer.findViewById(R.id.coverage_reports);
        coverage.setOnClickListener(customNavigationBarListener);

        LinearLayout dropout = (LinearLayout) drawer.findViewById(R.id.dropout_reports);
        dropout.setOnClickListener(customNavigationBarListener);

    }

    private void updateLastSyncText() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null && navigationView.getMenu() != null) {
            TextView syncMenuItem = ((TextView) navigationView.findViewById(R.id.nav_synctextview));
            if (syncMenuItem != null) {
                String lastSync = getLastSyncTime();

                if (!TextUtils.isEmpty(lastSync)) {
                    lastSync = " " + String.format(getString(R.string.last_sync), lastSync);
                }
                syncMenuItem.setText(String.format(getString(R.string.sync_), lastSync));
            }
        }
    }

    private String getLastSyncTime() {
        String lastSync = "";
        long milliseconds = ECSyncUpdater.getInstance(this).getLastCheckTimeStamp();
        if (milliseconds > 0) {
            DateTime lastSyncTime = new DateTime(milliseconds);
            DateTime now = new DateTime(Calendar.getInstance());
            Minutes minutes = Minutes.minutesBetween(lastSyncTime, now);
            if (minutes.getMinutes() < 1) {
                Seconds seconds = Seconds.secondsBetween(lastSyncTime, now);
                lastSync = seconds.getSeconds() + "s";
            } else if (minutes.getMinutes() >= 1 && minutes.getMinutes() < 60) {
                lastSync = minutes.getMinutes() + "m";
            } else if (minutes.getMinutes() >= 60 && minutes.getMinutes() < 1440) {
                Hours hours = Hours.hoursBetween(lastSyncTime, now);
                lastSync = hours.getHours() + "h";
            } else {
                Days days = Days.daysBetween(lastSyncTime, now);
                lastSync = days.getDays() + "d";
            }
        }
        return lastSync;
    }

    @Override
    protected Context context() {
        return VaccinatorApplication.getInstance().context();
    }

    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////
    private class BaseActivityToggle extends ActionBarDrawerToggle {

        private BaseActivityToggle(Activity activity, DrawerLayout drawerLayout, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        /*public BaseActivityToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, @StringRes int openDrawerContentDescRes, @StringRes int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }*/

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            if (!SyncStatusBroadcastReceiver.getInstance().isSyncing()) {
                updateLastSyncText();
            }
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
        }
    }

}

