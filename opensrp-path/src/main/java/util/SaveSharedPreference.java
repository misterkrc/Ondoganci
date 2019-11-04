package util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static util.PreferencesUtility.LOGGED_IN_PREF;
import static util.PreferencesUtility.PASS_WORD;
import static util.PreferencesUtility.PREFERRED_CONTACT;
import static util.PreferencesUtility.USER_NAME;

public class SaveSharedPreference {

    static SharedPreferences getPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Set the Login Status
     * @param context
     * @param loggedIn
     */
    public static void setLoggedIn(Context context, boolean loggedIn) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putBoolean(LOGGED_IN_PREF, loggedIn);
        editor.apply();
    }

    public static void setUsername(Context context, String username) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(USER_NAME, username);
        editor.apply();
    }

    public static void setPassword(Context context, String password) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(PASS_WORD, password);
        editor.apply();
    }

    public static void setPreferredContact(Context context, String preferredContact) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(PREFERRED_CONTACT, preferredContact);
        editor.apply();
    }

    /**
     * Get the Login Status
     * @param context
     * @return boolean: login status
     */
    public static boolean getLoggedStatus(Context context) {
        return getPreferences(context).getBoolean(LOGGED_IN_PREF, false);
    }

    public static String getUsername(Context context) {
        return getPreferences(context).getString(USER_NAME, "");
    }

    public static String getPassword(Context context) {
        return getPreferences(context).getString(PASS_WORD, "");
    }

    public static String getPreferredContact(Context context) {
        return getPreferences(context).getString(PREFERRED_CONTACT, "");
    }
}