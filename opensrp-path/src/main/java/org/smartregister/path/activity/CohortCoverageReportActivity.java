package org.smartregister.path.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.smartregister.immunization.db.VaccineRepo;
import org.smartregister.path.R;
import org.smartregister.path.application.VaccinatorApplication;
import org.smartregister.path.domain.Cohort;
import org.smartregister.path.domain.CohortIndicator;
import org.smartregister.path.domain.CoverageHolder;
import org.smartregister.path.domain.NamedObject;
import org.smartregister.path.receiver.CoverageDropoutBroadcastReceiver;
import org.smartregister.path.repository.CohortIndicatorRepository;
import org.smartregister.path.repository.CohortPatientRepository;
import org.smartregister.path.repository.CohortRepository;
import org.smartregister.path.toolbar.LocationSwitcherToolbar;
import org.smartregister.path.view.CustomHeightSpinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by keyman on 21/12/17.
 */
public class CohortCoverageReportActivity extends BaseReportActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle("");

        LocationSwitcherToolbar toolbar = (LocationSwitcherToolbar) getToolbar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CohortCoverageReportActivity.this, CoverageReportsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });

        ((TextView) toolbar.findViewById(R.id.title)).setText(getString(R.string.cohort_coverage_report));

        updateListViewHeader(R.layout.coverage_report_header);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_cohort_coverage_reports;
    }

    @Override
    protected int getDrawerLayoutId() {
        return R.id.drawer_layout;
    }

    @Override
    protected int getToolbarId() {
        return LocationSwitcherToolbar.TOOLBAR_ID;
    }

    @Override
    protected Class onBackActivity() {
        return null;
    }

    private void updateCohortSize() {
        Long size = getHolder().getSize();
        if (size == null) {
            size = 0L;
        }

        TextView textView = (TextView) findViewById(R.id.cohort_size_value);
        textView.setText(String.format(getString(R.string.cso_population_value), size));
    }

    private void updateSpinnerSize(List list) {
        if (list != null && list.size() > 12) {
            TypedValue typedValue = new TypedValue();
            CohortCoverageReportActivity.this.getTheme().resolveAttribute(android.R.attr.textAppearanceLarge, typedValue, true);

            int[] attribute = new int[]{R.attr.dropdownListPreferredItemHeight};
            TypedArray array = CohortCoverageReportActivity.this.obtainStyledAttributes(typedValue.resourceId, attribute);
            int heightOfDropoutItem = array.getDimensionPixelSize(0, -1);
            array.recycle();

            CustomHeightSpinner customHeightSpinner = (CustomHeightSpinner) findViewById(R.id.report_spinner);
            customHeightSpinner.updateHeight(heightOfDropoutItem, 12);
        }
    }

    @Override
    protected String getActionType() {
        return CoverageDropoutBroadcastReceiver.TYPE_GENERATE_COHORT_INDICATORS;
    }

    @Override
    protected int getParentNav() {
        return R.id.coverage_reports;
    }

    ////////////////////////////////////////////////////////////////
    // Reporting Methods
    ////////////////////////////////////////////////////////////////

    @Override
    protected <T> View generateView(final View view, final VaccineRepo.Vaccine vaccine, final List<T> indicators) {
        long value = 0;

        CohortIndicator cohortIndicator = retrieveCohortIndicator(indicators, vaccine);
        if (cohortIndicator != null) {
            value = cohortIndicator.getValue();
        }

        boolean finalized = isFinalized(vaccine, getHolder().getDate());

        TextView vaccinatedTextView = (TextView) view.findViewById(R.id.vaccinated);
        vaccinatedTextView.setText(String.valueOf(value));

        int percentage = 0;
        if (value > 0 && getHolder().getSize() != null && getHolder().getSize() > 0) {
            percentage = (int) (value * 100.0 / getHolder().getSize() + 0.5);
        }

        TextView coverageTextView = (TextView) view.findViewById(R.id.coverage);
        coverageTextView.setText(String.format(getString(R.string.coverage_percentage),
                percentage));

        vaccinatedTextView.setTextColor(getResources().getColor(R.color.black));
        coverageTextView.setTextColor(getResources().getColor(R.color.black));

        if (finalized) {
            vaccinatedTextView.setTextColor(getResources().getColor(R.color.bluetext));
            coverageTextView.setTextColor(getResources().getColor(R.color.bluetext));
        }
        return view;
    }

    @Override
    protected Map<String, NamedObject<?>> generateReportBackground() {

        CohortRepository cohortRepository = VaccinatorApplication.getInstance().cohortRepository();
        CohortPatientRepository cohortPatientRepository = VaccinatorApplication.getInstance().cohortPatientRepository();
        CohortIndicatorRepository cohortIndicatorRepository = VaccinatorApplication.getInstance().cohortIndicatorRepository();

        if (cohortRepository == null || cohortPatientRepository == null || cohortIndicatorRepository == null) {
            return null;
        }

        List<Cohort> cohorts = cohortRepository.fetchAll();
        if (cohorts.isEmpty()) {
            return null;
        }

        // Populate the default cohort
        Cohort cohort = cohorts.get(0);

        long cohortSize = cohortPatientRepository.countCohort(cohort.getId());
        CoverageHolder coverageHolder = new CoverageHolder(cohort.getId(), cohort.getMonthAsDate(), cohortSize);

        List<CohortIndicator> indicators = cohortIndicatorRepository.findByCohort(cohort.getId());

        Map<String, NamedObject<?>> map = new HashMap<>();
        NamedObject<List<Cohort>> cohortsNamedObject = new NamedObject<>(Cohort.class.getName(), cohorts);
        map.put(cohortsNamedObject.name, cohortsNamedObject);

        NamedObject<CoverageHolder> cohortHolderNamedObject = new NamedObject<>(CoverageHolder.class.getName(), coverageHolder);
        map.put(cohortHolderNamedObject.name, cohortHolderNamedObject);

        NamedObject<List<CohortIndicator>> indicatorMapNamedObject = new NamedObject<>(CohortIndicator.class.getName(), indicators);
        map.put(indicatorMapNamedObject.name, indicatorMapNamedObject);

        return map;
    }

    @Override
    protected void generateReportUI(Map<String, NamedObject<?>> map, boolean userAction) {
        List<Cohort> cohorts = new ArrayList<>();
        List<CohortIndicator> indicatorList = new ArrayList<>();

        if (map.containsKey(Cohort.class.getName())) {
            NamedObject<?> namedObject = map.get(Cohort.class.getName());
            if (namedObject != null) {
                cohorts = (List<Cohort>) namedObject.object;
            }
        }

        if (map.containsKey(CoverageHolder.class.getName())) {
            NamedObject<?> namedObject = map.get(CoverageHolder.class.getName());
            if (namedObject != null) {
                setHolder((CoverageHolder) namedObject.object);
            }
        }

        if (map.containsKey(CohortIndicator.class.getName())) {
            NamedObject<?> namedObject = map.get(CohortIndicator.class.getName());
            if (namedObject != null) {
                indicatorList = (List<CohortIndicator>) namedObject.object;
            }
        }

        updateReportDates(cohorts, new SimpleDateFormat("MMM yyyy"), null, true);
        updateSpinnerSize(cohorts);
        updateCohortSize();
        updateReportList(indicatorList);
    }

    @Override
    protected Pair<List, Long> updateReportBackground(Long id) {

        CohortIndicatorRepository cohortIndicatorRepository = VaccinatorApplication.getInstance().cohortIndicatorRepository();
        CohortPatientRepository cohortPatientRepository = VaccinatorApplication.getInstance().cohortPatientRepository();

        if (cohortIndicatorRepository == null || cohortPatientRepository == null) {
            return null;
        }

        List indicators = cohortIndicatorRepository.findByCohort(id);
        long cohortSize = cohortPatientRepository.countCohort(id);

        return Pair.create(indicators, cohortSize);
    }

    @Override
    protected void updateReportUI(Pair<List, Long> pair, boolean userAction) {
        setHolderSize(pair.second);
        updateCohortSize();
        updateReportList(pair.first);
    }
}
