package org.smartregister.growthmonitoring.repository;

import android.database.Cursor;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;

import org.opensrp.api.constants.Gender;
import org.smartregister.growthmonitoring.domain.HCZScore;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores child z-scores obtained from:
 * - https://www.who.int/childgrowth/standards/second_set/tab_hcfa_boys_z_0_5.txt
 * - https://www.who.int/childgrowth/standards/second_set/tab_hcfa_girls_z_0_5.txt
 * <p/>
 * Created by Chukwu Christian
 */

public class HCZScoreRepository extends BaseRepository {
    private static final String TAG = HCZScoreRepository.class.getName();
    public static final String TABLE_NAME = "hcz_scores";
    public static final String COLUMN_SEX = "sex";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_L = "l";
    public static final String COLUMN_M = "m";
    public static final String COLUMN_S = "s";
    public static final String COLUMN_SD = "sd";
    public static final String COLUMN_SD3NEG = "sd3neg";
    public static final String COLUMN_SD2NEG = "sd2neg";
    public static final String COLUMN_SD1NEG = "sd1neg";
    public static final String COLUMN_SD0 = "sd0";
    public static final String COLUMN_SD1 = "sd1";
    public static final String COLUMN_SD2 = "sd2";
    public static final String COLUMN_SD3 = "sd3";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE " + TABLE_NAME +
            " (_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_SEX + " VARCHAR NOT NULL, " +
            COLUMN_MONTH + " INTEGER NOT NULL, " +
            COLUMN_L + " REAL NOT NULL, " +
            COLUMN_M + " REAL NOT NULL, " +
            COLUMN_S + " REAL NOT NULL, " +
            COLUMN_SD + " REAL NOT NULL, " +
            COLUMN_SD3NEG + " REAL NOT NULL, " +
            COLUMN_SD2NEG + " REAL NOT NULL, " +
            COLUMN_SD1NEG + " REAL NOT NULL, " +
            COLUMN_SD0 + " REAL NOT NULL, " +
            COLUMN_SD1 + " REAL NOT NULL, " +
            COLUMN_SD2 + " REAL NOT NULL, " +
            COLUMN_SD3 + " REAL NOT NULL, " +
            "UNIQUE(" + COLUMN_SEX + ", " + COLUMN_MONTH + ") ON CONFLICT REPLACE)";

    private static final String CREATE_INDEX_SEX_QUERY = "CREATE INDEX " + COLUMN_SEX + "_index2 ON " + TABLE_NAME + "(" + COLUMN_SEX + " COLLATE NOCASE);";
    private static final String CREATE_INDEX_MONTH_QUERY = "CREATE INDEX " + COLUMN_MONTH + "_index2 ON " + TABLE_NAME + "(" + COLUMN_MONTH + " COLLATE NOCASE);";

    public HCZScoreRepository(Repository repository) {
        super(repository);
    }

    public static void createTable(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_QUERY);
        database.execSQL(CREATE_INDEX_SEX_QUERY);
        database.execSQL(CREATE_INDEX_MONTH_QUERY);
    }

    /**
     * @param query
     * @return
     */
    public boolean runRawQuery(String query) {
        try {
            getRepository().getWritableDatabase().execSQL(query);
            return true;
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return false;
    }

    public List<HCZScore> findByGender(Gender gender) {
        List<HCZScore> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase database = getRepository().getReadableDatabase();
            cursor = database.query(TABLE_NAME,
                    null,
                    COLUMN_SEX + " = ? " + COLLATE_NOCASE,
                    new String[]{gender.name()}, null, null, null, null);

            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    result.add(new HCZScore(gender,
                            cursor.getInt(cursor.getColumnIndex(COLUMN_MONTH)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_L)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_M)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_S)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD3NEG)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD2NEG)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD1NEG)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD0)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD1)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD2)),
                            cursor.getDouble(cursor.getColumnIndex(COLUMN_SD3))));
                    cursor.moveToNext();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } finally {
            if (cursor != null) cursor.close();
        }

        return result;
    }
}
