package org.smartregister.growthmonitoring;

import org.smartregister.Context;
import org.smartregister.growthmonitoring.repository.HCZScoreRepository;
import org.smartregister.growthmonitoring.repository.HeadCircumferenceRepository;
import org.smartregister.growthmonitoring.repository.WeightRepository;
import org.smartregister.growthmonitoring.repository.ZScoreRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;

public class GrowthMonitoringLibrary {

    private final Repository repository;
    private final Context context;

    private WeightRepository weightRepository;
    private HeadCircumferenceRepository headCircumferenceRepository;
    private ZScoreRepository zScoreRepository;
    private HCZScoreRepository hczScoreRepository;
    private EventClientRepository eventClientRepository;

    private static GrowthMonitoringLibrary instance;

    public static void init(Context context, Repository repository) {
        if (instance == null) {
            instance = new GrowthMonitoringLibrary(context, repository);
        }
    }

    public static GrowthMonitoringLibrary getInstance() {
        if (instance == null) {
            throw new IllegalStateException(" Instance does not exist!!! Call " + GrowthMonitoringLibrary.class.getName() + ".init method in the onCreate method of your Application class ");
        }
        return instance;
    }

    private GrowthMonitoringLibrary(Context context, Repository repository) {
        this.repository = repository;
        this.context = context;
    }

    public Repository getRepository() {
        return repository;
    }

    public WeightRepository weightRepository() {
        if (weightRepository == null) {
            weightRepository = new WeightRepository(getRepository());
        }
        return weightRepository;
    }

    public HeadCircumferenceRepository headCircumferenceRepository() {
        if (headCircumferenceRepository == null) {
            headCircumferenceRepository = new HeadCircumferenceRepository(getRepository());
        }
        return headCircumferenceRepository;
    }


    public ZScoreRepository zScoreRepository() {
        if (zScoreRepository == null) {
            zScoreRepository = new ZScoreRepository(getRepository());
        }

        return zScoreRepository;
    }

    public HCZScoreRepository hczScoreRepository() {
        if (hczScoreRepository == null) {
            hczScoreRepository = new HCZScoreRepository(getRepository());
        }

        return hczScoreRepository;
    }

    public EventClientRepository eventClientRepository() {
        if (eventClientRepository == null) {
            eventClientRepository = new EventClientRepository(getRepository());
        }

        return eventClientRepository;
    }

    public Context context() {
        return context;
    }

}
