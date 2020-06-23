package org.smartregister.immunization.util;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import org.smartregister.domain.Alert;
import org.smartregister.immunization.ImmunizationLibrary;
import org.smartregister.immunization.domain.ServiceRecord;
import org.smartregister.immunization.domain.ServiceWrapper;
import org.smartregister.immunization.repository.RecurringServiceRecordRepository;
import org.smartregister.immunization.view.ServiceGroup;
import org.smartregister.immunization.view.ServiceRowGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keyman on 23/05/2017.
 */
public class RecurringServiceUtils {


    public static void updateServiceGroupViews(View view, final ArrayList<ServiceWrapper> wrappers, List<ServiceRecord> serviceRecordList, List<Alert> alertList) {
        updateServiceGroupViews(view, wrappers, serviceRecordList, alertList, false);
    }

    public static void updateServiceGroupViews(View view, final ArrayList<ServiceWrapper> wrappers, final List<ServiceRecord> serviceRecordList, final List<Alert> alertList, final boolean undo) {
        if (view == null || wrappers == null || wrappers.isEmpty()) {
            return;
        }

        if (view instanceof ServiceGroup) {
            final ServiceGroup serviceGroup = (ServiceGroup) view;
            serviceGroup.setModalOpen(false);

            if (Looper.myLooper() == Looper.getMainLooper()) {
                if (undo) {
                    serviceGroup.setServiceRecordList(serviceRecordList);
                    serviceGroup.setAlertList(alertList);
                    serviceGroup.updateAllWrapperStatus();
                }
                serviceGroup.updateViews();

            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (undo) {
                            serviceGroup.setServiceRecordList(serviceRecordList);
                            serviceGroup.setAlertList(alertList);
                            serviceGroup.updateAllWrapperStatus();
                        }
                        serviceGroup.updateViews();
                    }
                });
            }
        } else if (view instanceof ServiceRowGroup) {
            final ServiceRowGroup serviceRowGroup = (ServiceRowGroup) view;
            serviceRowGroup.setModalOpen(false);

            if (Looper.myLooper() == Looper.getMainLooper()) {
                if (undo) {
                    serviceRowGroup.setServiceRecordList(serviceRecordList);
                    serviceRowGroup.updateWrapperStatus(wrappers.get(0));
                }
                serviceRowGroup.updateViews(wrappers);

            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (undo) {
                            serviceRowGroup.setServiceRecordList(serviceRecordList);
                            serviceRowGroup.updateWrapperStatus(wrappers.get(0));
                        }
                        serviceRowGroup.updateViews(wrappers);
                    }
                });
            }
        }
    }


    public static ServiceGroup getLastOpenedServiceView(List<ServiceGroup> serviceGroups) {
        if (serviceGroups == null) {
            return null;
        }

        for (ServiceGroup serviceGroup : serviceGroups) {
            if (serviceGroup.isModalOpen()) {
                return serviceGroup;
            }
        }

        return null;
    }

    public static void saveService(ServiceWrapper tag, String baseEntityId, String providerId, String locationId) {
        if (tag.getUpdatedVaccineDate() == null) {
            return;
        }

        RecurringServiceRecordRepository recurringServiceRecordRepository = ImmunizationLibrary.getInstance().recurringServiceRecordRepository();

        ServiceRecord serviceRecord = new ServiceRecord();
        if (tag.getDbKey() != null) {
            serviceRecord = recurringServiceRecordRepository.find(tag.getDbKey());
            serviceRecord.setDate(tag.getUpdatedVaccineDate().toDate());
        } else {
            serviceRecord.setDate(tag.getUpdatedVaccineDate().toDate());

            serviceRecord.setBaseEntityId(baseEntityId);
            serviceRecord.setRecurringServiceId(tag.getTypeId());
            serviceRecord.setDate(tag.getUpdatedVaccineDate().toDate());
            serviceRecord.setAnmId(providerId);
            serviceRecord.setValue(tag.getValue());
            serviceRecord.setLocationId(locationId);

            serviceRecord.setTeam("testTeam");
            serviceRecord.setTeamId("testTeamId");
            serviceRecord.setChildLocationId("testChildLocationId");
        }

        recurringServiceRecordRepository.add(serviceRecord);
        tag.setDbKey(serviceRecord.getId());
    }

}
