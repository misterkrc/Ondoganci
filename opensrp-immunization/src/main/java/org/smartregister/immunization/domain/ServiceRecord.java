package org.smartregister.immunization.domain;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by keyman on 3/1/17.
 */
public class ServiceRecord {
    private static final String ZEIR_ID = "ZEIR_ID";
    private Long id;
    private String baseEntityId;
    private String programClientId;
    private Long recurringServiceId;
    private String value;
    private Date date;
    private String anmId;
    private String locationId;
    private String syncStatus;
    private String eventId;
    private String formSubmissionId;
    private Long updatedAt;
    private Date createdAt;

    protected String type;
    protected String name;
    private String team;
    private String teamId;
    private String childLocationId;

    public ServiceRecord() {
    }

    public ServiceRecord(Long id, String baseEntityId, Long recurringServiceId, String value,
                         Date date, String anmId, String locationId, String syncStatus, String
                                 eventId, String formSubmissionId, Long updatedAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.recurringServiceId = recurringServiceId;
        this.value = value;
        this.date = date;
        this.anmId = anmId;
        this.locationId = locationId;
        this.syncStatus = syncStatus;
        this.eventId = eventId;
        this.formSubmissionId = formSubmissionId;
        this.updatedAt = updatedAt;
    }

    public ServiceRecord(Long id, String baseEntityId, String programClientId, Long
            recurringServiceId, String value, Date date, String anmId, String locationId, String
                                 syncStatus, String eventId, String formSubmissionId, Long updatedAt, Date createdAt) {
        this.id = id;
        this.baseEntityId = baseEntityId;
        this.programClientId = programClientId;
        this.recurringServiceId = recurringServiceId;
        this.value = value;
        this.date = date;
        this.anmId = anmId;
        this.locationId = locationId;
        this.syncStatus = syncStatus;
        this.eventId = eventId;
        this.formSubmissionId = formSubmissionId;
        this.updatedAt = updatedAt;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBaseEntityId() {
        return baseEntityId;
    }

    public void setBaseEntityId(String baseEntityId) {
        this.baseEntityId = baseEntityId;
    }

    public Long getRecurringServiceId() {
        return recurringServiceId;
    }

    public void setRecurringServiceId(Long recurringServiceId) {
        this.recurringServiceId = recurringServiceId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAnmId() {
        return anmId;
    }

    public void setAnmId(String anmId) {
        this.anmId = anmId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(String syncStatus) {
        this.syncStatus = syncStatus;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getFormSubmissionId() {
        return formSubmissionId;
    }

    public void setFormSubmissionId(String formSubmissionId) {
        this.formSubmissionId = formSubmissionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProgramClientId() {
        return programClientId;
    }

    public void setProgramClientId(String programClientId) {
        this.programClientId = programClientId;
    }

    public HashMap<String, String> getIdentifiers() {
        HashMap<String, String> identifiers = new HashMap<>();
        identifiers.put(ZEIR_ID, programClientId);
        return identifiers;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getChildLocationId() {
        return childLocationId;
    }

    public void setChildLocationId(String childLocationId) {
        this.childLocationId = childLocationId;
    }
}
