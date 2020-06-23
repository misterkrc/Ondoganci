package org.smartregister.growthmonitoring.util;

import org.smartregister.growthmonitoring.BuildConfig;

/**
 * Created by keyman on 26/07/2017.
 */
public class GMConstants {

    public static final String ZSCORE_MALE_URL = "http://www.who.int/childgrowth/standards/wfa_boys_0_5_zscores.txt";
    public static final String ZSCORE_FEMALE_URL = "http://www.who.int/childgrowth/standards/wfa_girls_0_5_zscores.txt";
    public static final String HCZSCORE_MALE_URL = "https://www.who.int/childgrowth/standards/second_set/tab_hcfa_boys_z_0_5.txt";
    public static final String HCZSCORE_FEMALE_URL = "https://www.who.int/childgrowth/standards/second_set/tab_hcfa_girls_z_0_5.txt";

    public static final String CHILD_TABLE_NAME = "ec_child";
    public static final String MOTHER_TABLE_NAME = "ec_mother";

    public static final int WEIGHT_SYNC_TIME = BuildConfig.WEIGHT_SYNC_TIME;
    public static final int HC_SYNC_TIME = BuildConfig.WEIGHT_SYNC_TIME;

    public static final class JsonForm {
        public static final String OPENMRS_ENTITY = "openmrs_entity";
        public static final String OPENMRS_ENTITY_ID = "openmrs_entity_id";
        public static final String OPENMRS_ENTITY_PARENT = "openmrs_entity_parent";
        public static final String OPENMRS_DATA_TYPE = "openmrs_data_type";
        public static final String VALUE = "value";
        public static final String KEY = "key";
    }

}
