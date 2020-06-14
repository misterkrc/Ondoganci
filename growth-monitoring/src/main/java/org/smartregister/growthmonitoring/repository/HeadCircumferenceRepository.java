package org.smartregister.growthmonitoring.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.apache.commons.lang3.StringUtils;
import org.opensrp.api.constants.Gender;
import org.smartregister.growthmonitoring.domain.HeadCircumference;
import org.smartregister.growthmonitoring.domain.HCZScore;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.Repository;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HeadCircumferenceRepository extends BaseRepository {
    private static final String TAG = HeadCircumferenceRepository.class.getCanonicalName();
    private static final String HEADCIRCUMFERENCE_SQL = "CREATE TABLE HEADCIRCUMFEENCES (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,base_entity_id VARCHAR NOT NULL,program_client_id VARCHAR NULL,kg REAL NOT NULL,date DATETIME NOT NULL,anmid VARCHAR NULL,location_id VARCHAR NULL,sync_status VARCHAR,updated_at INTEGER NULL)";
    public static final String HEADCIRCUMFERENCE_TABLE_NAME = "HEADCIRCUMFEENCES";
    public static final String ID_COLUMN = "_id";
    public static final String BASE_ENTITY_ID = "base_entity_id";
    public static final String EVENT_ID = "event_id";
    public static final String PROGRAM_CLIENT_ID = "program_client_id";// ID to be used to identify entity when base_entity_id is unavailable
    public static final String FORMSUBMISSION_ID = "formSubmissionId";
    public static final String OUT_OF_AREA = "out_of_area";

    public static final String INCH = "inch";
    public static final String DATE = "date";
    public static final String ANMID = "anmid";
    public static final String LOCATIONID = "location_id";
    public static final String SYNC_STATUS = "sync_status";
    public static final String UPDATED_AT_COLUMN = "updated_at";
    public static final String Z_SCORE = "z_score";
    public static final double DEFAULT_Z_SCORE = 999999d;
    public static final String CREATED_AT = "created_at";
    public static final String UPDATE_TABLE_ADD_TEAM_COL = "ALTER TABLE headcircumferences ADD COLUMN team VARCHAR;";
    public static final String UPDATE_TABLE_ADD_TEAM_ID_COL = "ALTER TABLE headcircumferences ADD COLUMN team_id VARCHAR;";
    public static final String UPDATE_TABLE_ADD_CHILD_LOCATION_ID_COL = "ALTER TABLE headcircumferences ADD COLUMN child_location_id VARCHAR;";

    public static final String[] HEADCIRCUMFERENCE_TABLE_COLUMNS = {
            ID_COLUMN, BASE_ENTITY_ID, PROGRAM_CLIENT_ID, INCH, DATE, ANMID, LOCATIONID, SYNC_STATUS,
            UPDATED_AT_COLUMN, EVENT_ID, FORMSUBMISSION_ID, Z_SCORE, OUT_OF_AREA, CREATED_AT};

    private static final String BASE_ENTITY_ID_INDEX = "CREATE INDEX " + HEADCIRCUMFERENCE_TABLE_NAME + "_" + BASE_ENTITY_ID + "_index ON " + HEADCIRCUMFERENCE_TABLE_NAME + "(" + BASE_ENTITY_ID + " COLLATE NOCASE);";
    private static final String SYNC_STATUS_INDEX = "CREATE INDEX " + HEADCIRCUMFERENCE_TABLE_NAME + "_" + SYNC_STATUS + "_index ON " + HEADCIRCUMFERENCE_TABLE_NAME + "(" + SYNC_STATUS + " COLLATE NOCASE);";
    private static final String UPDATED_AT_INDEX = "CREATE INDEX " + HEADCIRCUMFERENCE_TABLE_NAME + "_" + UPDATED_AT_COLUMN + "_index ON " + HEADCIRCUMFERENCE_TABLE_NAME + "(" + UPDATED_AT_COLUMN + ");";
    public static final String UPDATE_TABLE_ADD_EVENT_ID_COL = "ALTER TABLE " + HEADCIRCUMFERENCE_TABLE_NAME + " ADD COLUMN " + EVENT_ID + " VARCHAR;";
    public static final String EVENT_ID_INDEX = "CREATE INDEX " + HEADCIRCUMFERENCE_TABLE_NAME + "_" + EVENT_ID + "_index ON " + HEADCIRCUMFERENCE_TABLE_NAME + "(" + EVENT_ID + " COLLATE NOCASE);";
    public static final String UPDATE_TABLE_ADD_FORMSUBMISSION_ID_COL = "ALTER TABLE " + HEADCIRCUMFERENCE_TABLE_NAME + " ADD COLUMN " + FORMSUBMISSION_ID + " VARCHAR;";
    public static final String FORMSUBMISSION_INDEX = "CREATE INDEX " + HEADCIRCUMFERENCE_TABLE_NAME + "_" + FORMSUBMISSION_ID + "_index ON " + HEADCIRCUMFERENCE_TABLE_NAME + "(" + FORMSUBMISSION_ID + " COLLATE NOCASE);";
    public static final String UPDATE_TABLE_ADD_OUT_OF_AREA_COL = "ALTER TABLE " + HEADCIRCUMFERENCE_TABLE_NAME + " ADD COLUMN " + OUT_OF_AREA + " VARCHAR;";
    public static final String UPDATE_TABLE_ADD_OUT_OF_AREA_COL_INDEX = "CREATE INDEX " + HEADCIRCUMFERENCE_TABLE_NAME + "_" + OUT_OF_AREA + "_index ON " + HEADCIRCUMFERENCE_TABLE_NAME + "(" + OUT_OF_AREA + " COLLATE NOCASE);";

    public static final String ALTER_ADD_Z_SCORE_COLUMN = "ALTER TABLE " + HEADCIRCUMFERENCE_TABLE_NAME + " ADD COLUMN " + Z_SCORE + " REAL NOT NULL DEFAULT " + String.valueOf(DEFAULT_Z_SCORE);
    public static final String ALTER_ADD_CREATED_AT_COLUMN = "ALTER TABLE " + HEADCIRCUMFERENCE_TABLE_NAME + " ADD COLUMN " + CREATED_AT + " DATETIME NULL ";


    public HeadCircumferenceRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(HEADCIRCUMFERENCE_SQL);
        database.execSQL(BASE_ENTITY_ID_INDEX);
        database.execSQL(SYNC_STATUS_INDEX);
        database.execSQL(UPDATED_AT_INDEX);
    }

    /**
     * This method sets the headcircumference's z-score, before adding it to the database
     *
     * @param dateOfBirth
     * @param gender
     * @param headcircumference
     */
    public void add(Date dateOfBirth, Gender gender, HeadCircumference headcircumference) {
        headcircumference.setZScore(HCZScore.calculate(gender, dateOfBirth, headcircumference.getDate(), headcircumference.getInch()));
        add(headcircumference);
    }

    public void add(HeadCircumference headcircumference) {
        try {
            if (headcircumference == null) {
                return;
            }
            if (StringUtils.isBlank(headcircumference.getSyncStatus())) {
                headcircumference.setSyncStatus(TYPE_Unsynced);
            }
            if (StringUtils.isBlank(headcircumference.getFormSubmissionId())) {
                headcircumference.setFormSubmissionId(generateRandomUUIDString());
            }


            if (headcircumference.getUpdatedAt() == null) {
                headcircumference.setUpdatedAt(Calendar.getInstance().getTimeInMillis());
            }

            SQLiteDatabase database = getRepository().getWritableDatabase();
            if (headcircumference.getId() == null) {
                HeadCircumference sameHeadcircumference = findUnique(database, headcircumference);
                if (sameHeadcircumference != null) {
                    headcircumference.setUpdatedAt(sameHeadcircumference.getUpdatedAt());
                    headcircumference.setId(sameHeadcircumference.getId());
                    update(database, headcircumference);
                } else {
                    if (headcircumference.getCreatedAt() == null) {
                        headcircumference.setCreatedAt(new Date());
                    }
                    headcircumference.setId(database.insert(HEADCIRCUMFERENCE_TABLE_NAME, null, createValuesFor(headcircumference)));
                }
            } else {
                headcircumference.setSyncStatus(TYPE_Unsynced);
                update(database, headcircumference);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void update(SQLiteDatabase database, HeadCircumference headcircumference) {
        if (headcircumference == null || headcircumference.getId() == null) {
            return;
        }

        try {
            SQLiteDatabase db;
            if (database == null) {
                db = getRepository().getWritableDatabase();
            } else {
                db = database;
            }

            String idSelection = ID_COLUMN + " = ?";
            db.update(HEADCIRCUMFERENCE_TABLE_NAME, createValuesFor(headcircumference), idSelection, new String[]{headcircumference.getId().toString()});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public List<HeadCircumference> findUnSyncedBeforeTime(int hours) {
        List<HeadCircumference> headcircumferences = new ArrayList<>();
        Cursor cursor = null;
        try {

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR_OF_DAY, -hours);

            Long time = calendar.getTimeInMillis();

            cursor = getRepository().getReadableDatabase().query(HEADCIRCUMFERENCE_TABLE_NAME, HEADCIRCUMFERENCE_TABLE_COLUMNS, UPDATED_AT_COLUMN + " < ? " + COLLATE_NOCASE + " AND " + SYNC_STATUS + " = ? " + COLLATE_NOCASE, new String[]{time.toString(), TYPE_Unsynced}, null, null, null, null);
            headcircumferences = readAllHeadcircumferences(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headcircumferences;
    }

    public HeadCircumference findUnSyncedByEntityId(String entityId) {
        HeadCircumference headcircumference = null;
        Cursor cursor = null;
        try {

            cursor = getRepository().getReadableDatabase().query(HEADCIRCUMFERENCE_TABLE_NAME, HEADCIRCUMFERENCE_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE + " AND " + SYNC_STATUS + " = ? ", new String[]{entityId, TYPE_Unsynced}, null, null, UPDATED_AT_COLUMN + COLLATE_NOCASE + " DESC", null);
            List<HeadCircumference> headcircumferences = readAllHeadcircumferences(cursor);
            if (!headcircumferences.isEmpty()) {
                headcircumference = headcircumferences.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headcircumference;
    }

    public List<HeadCircumference> findByEntityId(String entityId) {
        List<HeadCircumference> headcircumferences = null;
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase().query(HEADCIRCUMFERENCE_TABLE_NAME, HEADCIRCUMFERENCE_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE, new String[]{entityId}, null, null, null, null);
            headcircumferences = readAllHeadcircumferences(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return headcircumferences;
    }

    public List<HeadCircumference> findWithNoZScore() {
        List<HeadCircumference> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase().query(HEADCIRCUMFERENCE_TABLE_NAME,
                    HEADCIRCUMFERENCE_TABLE_COLUMNS, Z_SCORE + " = " + DEFAULT_Z_SCORE, null, null, null, null, null);
            result = readAllHeadcircumferences(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return result;
    }

    public HeadCircumference find(Long caseId) {
        HeadCircumference headcircumference = null;
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase().query(HEADCIRCUMFERENCE_TABLE_NAME, HEADCIRCUMFERENCE_TABLE_COLUMNS, ID_COLUMN + " = ?", new String[]{caseId.toString()}, null, null, null, null);
            List<HeadCircumference> headcircumferences = readAllHeadcircumferences(cursor);
            if (!headcircumferences.isEmpty()) {
                headcircumference = headcircumferences.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headcircumference;
    }

    public List<HeadCircumference> findLast5(String entityid) {
        List<HeadCircumference> headcircumferenceList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getRepository().getReadableDatabase().query(HEADCIRCUMFERENCE_TABLE_NAME, HEADCIRCUMFERENCE_TABLE_COLUMNS, BASE_ENTITY_ID + " = ? " + COLLATE_NOCASE, new String[]{entityid}, null, null, UPDATED_AT_COLUMN + COLLATE_NOCASE + " DESC", null);
            headcircumferenceList = readAllHeadcircumferences(cursor);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headcircumferenceList;
    }

    public HeadCircumference findUnique(SQLiteDatabase db, HeadCircumference headcircumference) {

        if (headcircumference == null || (StringUtils.isBlank(headcircumference.getFormSubmissionId()) && StringUtils.isBlank(headcircumference.getEventId()))) {
            return null;
        }

        try {
            SQLiteDatabase database = db;
            if (database == null) {
                database = getRepository().getReadableDatabase();
            }

            String selection = null;
            String[] selectionArgs = null;
            if (StringUtils.isNotBlank(headcircumference.getFormSubmissionId()) && StringUtils.isNotBlank(headcircumference.getEventId())) {
                selection = FORMSUBMISSION_ID + " = ? " + COLLATE_NOCASE + " OR " + EVENT_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{headcircumference.getFormSubmissionId(), headcircumference.getEventId()};
            } else if (StringUtils.isNotBlank(headcircumference.getEventId())) {
                selection = EVENT_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{headcircumference.getEventId()};
            } else if (StringUtils.isNotBlank(headcircumference.getFormSubmissionId())) {
                selection = FORMSUBMISSION_ID + " = ? " + COLLATE_NOCASE;
                selectionArgs = new String[]{headcircumference.getFormSubmissionId()};
            }

            Cursor cursor = database.query(HEADCIRCUMFERENCE_TABLE_NAME, HEADCIRCUMFERENCE_TABLE_COLUMNS, selection, selectionArgs, null, null, ID_COLUMN + " DESC ", null);
            List<HeadCircumference> headcircumferenceList = readAllHeadcircumferences(cursor);
            if (!headcircumferenceList.isEmpty()) {
                return headcircumferenceList.get(0);
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return null;
    }

    public void delete(String id) {
        try {
            getRepository().getWritableDatabase().delete(HEADCIRCUMFERENCE_TABLE_NAME, ID_COLUMN + " = ? " + COLLATE_NOCASE + " AND " + SYNC_STATUS + " = ? ", new String[]{id, TYPE_Unsynced});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public void close(Long caseId) {
        try {
            ContentValues values = new ContentValues();
            values.put(SYNC_STATUS, TYPE_Synced);
            getRepository().getWritableDatabase().update(HEADCIRCUMFERENCE_TABLE_NAME, values, ID_COLUMN + " = ?", new String[]{caseId.toString()});
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    public static void migrateCreatedAt(SQLiteDatabase database) {
        try {
            String sql = "UPDATE " + HEADCIRCUMFERENCE_TABLE_NAME +
                    " SET " + CREATED_AT + " = " +
                    " ( SELECT " + EventClientRepository.event_column.dateCreated.name() +
                    "   FROM " + EventClientRepository.Table.event.name() +
                    "   WHERE " + EventClientRepository.event_column.eventId.name() + " = " + HEADCIRCUMFERENCE_TABLE_NAME + "." + EVENT_ID +
                    "   OR " + EventClientRepository.event_column.formSubmissionId.name() + " = " + HEADCIRCUMFERENCE_TABLE_NAME + "." + FORMSUBMISSION_ID +
                    " ) " +
                    " WHERE " + CREATED_AT + " is null ";
            database.execSQL(sql);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private List<HeadCircumference> readAllHeadcircumferences(Cursor cursor) {
        List<HeadCircumference> headcircumferences = new ArrayList<>();
        try {
            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    Double zScore = cursor.getDouble(cursor.getColumnIndex(Z_SCORE));
                    if (zScore != null && zScore.equals(new Double(DEFAULT_Z_SCORE))) {
                        zScore = null;
                    }

                    Date createdAt = null;
                    String dateCreatedString = cursor.getString(cursor.getColumnIndex(CREATED_AT));
                    if (StringUtils.isNotBlank(dateCreatedString)) {
                        try {
                            createdAt = EventClientRepository.dateFormat.parse(dateCreatedString);
                        } catch (ParseException e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }

                    headcircumferences.add(
                            new HeadCircumference(cursor.getLong(cursor.getColumnIndex(ID_COLUMN)),
                                    cursor.getString(cursor.getColumnIndex(BASE_ENTITY_ID)),
                                    cursor.getString(cursor.getColumnIndex(PROGRAM_CLIENT_ID)),
                                    cursor.getFloat(cursor.getColumnIndex(INCH)),
                                    new Date(cursor.getLong(cursor.getColumnIndex(DATE))),
                                    cursor.getString(cursor.getColumnIndex(ANMID)),
                                    cursor.getString(cursor.getColumnIndex(LOCATIONID)),
                                    cursor.getString(cursor.getColumnIndex(SYNC_STATUS)),
                                    cursor.getLong(cursor.getColumnIndex(UPDATED_AT_COLUMN)),
                                    cursor.getString(cursor.getColumnIndex(EVENT_ID)),
                                    cursor.getString(cursor.getColumnIndex(FORMSUBMISSION_ID)),
                                    zScore,
                                    cursor.getInt(cursor.getColumnIndex(OUT_OF_AREA)),
                                    createdAt

                            ));

                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return headcircumferences;

    }


    private ContentValues createValuesFor(HeadCircumference headcircumference) {
        ContentValues values = new ContentValues();
        values.put(ID_COLUMN, headcircumference.getId());
        values.put(BASE_ENTITY_ID, headcircumference.getBaseEntityId());
        values.put(PROGRAM_CLIENT_ID, headcircumference.getProgramClientId());
        values.put(INCH, headcircumference.getInch());
        values.put(DATE, headcircumference.getDate() != null ? headcircumference.getDate().getTime() : null);
        values.put(ANMID, headcircumference.getAnmId());
        values.put(LOCATIONID, headcircumference.getLocationId());
        values.put(SYNC_STATUS, headcircumference.getSyncStatus());
        values.put(UPDATED_AT_COLUMN, headcircumference.getUpdatedAt() != null ? headcircumference.getUpdatedAt() : null);
        values.put(EVENT_ID, headcircumference.getEventId() != null ? headcircumference.getEventId() : null);
        values.put(FORMSUBMISSION_ID, headcircumference.getFormSubmissionId() != null ? headcircumference.getFormSubmissionId() : null);
        values.put(OUT_OF_AREA, headcircumference.getOutOfCatchment() != null ? headcircumference.getOutOfCatchment() : null);
        values.put(Z_SCORE, headcircumference.getZScore() == null ? DEFAULT_Z_SCORE : headcircumference.getZScore());
        values.put(CREATED_AT, headcircumference.getCreatedAt() != null ? EventClientRepository.dateFormat.format(headcircumference.getCreatedAt()) : null);
        return values;
    }
}
