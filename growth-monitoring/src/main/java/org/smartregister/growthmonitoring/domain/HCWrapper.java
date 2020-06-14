package org.smartregister.growthmonitoring.domain;

import org.joda.time.DateTime;
import org.smartregister.domain.Photo;

import java.io.Serializable;

public class HCWrapper implements Serializable {
    private String id;
    private Long dbKey;
    private String gender;
    private Photo photo;
    private String patientName;
    private String patientNumber;
    private String patientAge;
    private String pmtctStatus;
    private Float headCircumference;
    private DateTime updatedHCDate;
    private boolean today;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getDbKey() {
        return dbKey;
    }

    public void setDbKey(Long dbKey) {
        this.dbKey = dbKey;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientNumber() {
        return patientNumber;
    }

    public void setPatientNumber(String patientNumber) {
        this.patientNumber = patientNumber;
    }

    public DateTime getUpdatedHCDate() {
        return updatedHCDate;
    }

    public void setUpdatedHCDate(DateTime updatedHCDate, boolean today) {
        this.today = today;
        this.updatedHCDate = updatedHCDate;
    }

    public boolean isToday() {
        return today;
    }

    public String getUpdatedHCDateAsString() {
        return updatedHCDate != null ? updatedHCDate.toString("yyyy-MM-dd") : "";
    }

    public void setPhoto(Photo photo) {
        this.photo = photo;
    }

    public Photo getPhoto() {
        return photo;
    }

    public void setHeadCircumference(Float headCircumference) {
        this.headCircumference = headCircumference;
    }

    public Float getHeadCircumference() {
        return headCircumference;
    }

    public void setPatientAge(String patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientAge() {
        return patientAge;
    }

    public void setPmtctStatus(String pmtctStatus) {
        this.pmtctStatus = pmtctStatus;
    }

    public String getPmtctStatus() {
        return pmtctStatus;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}
