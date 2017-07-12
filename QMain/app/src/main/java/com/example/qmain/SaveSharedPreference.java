package com.example.qmain;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.preference.PreferenceManager;
import java.util.ArrayList;

/**
 * Saves data about state of app use (signed in user, project selected)
 */
class SaveSharedPreference
{
    private static final String PREF_USER_NAME= "username";
    private static final String PROJECT = "project";
    private static final String HASH = "hash";
    private static final String CITY = "city";
    private static final String ORG = "organization";

    /**
     * Returns shared preferences
     *
     * @param ctx context of current activity
     * @return shared preferences
     */
    private static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    /**
     * Sets username in shared preferences to specified username
     *
     * @param ctx context of current activity
     * @param userName string username
     */
    static void setUserName(Context ctx, String userName)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    /**
     * Returns username of current logged in user
     *
     * @param ctx context of current activity
     * @return string username
     */
    static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    /**
     * Sets selected project metadata to specified project metadata
     *
     * @param ctx context of current activity
     * @param pr project name
     * @param hash project hash code
     * @param city project city
     * @param org project organization
     */
    static void setProjectInfo(Context ctx, String pr, String hash, String city, String org){
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PROJECT, pr);
        editor.putString(HASH, hash);
        editor.putString(CITY, city);
        editor.putString(ORG, org);
        editor.commit();
    }

    /**
     * Returns metadata of currently selected project
     *
     * @param ctx context of current activity
     * @return ArrayList of project metadata
     */
    static ArrayList<String> getProjectInfo(Context ctx){
        String pr = getSharedPreferences(ctx).getString(PROJECT, "");
        String hash = getSharedPreferences(ctx).getString(HASH, "");
        String city = getSharedPreferences(ctx).getString(CITY, "");
        String org = getSharedPreferences(ctx).getString(ORG, "");
        ArrayList<String> pi = new ArrayList<String>();
        if(pr.equals("") || hash.equals("") || city.equals("") || org.equals("")){
            return pi;
        }
        pi.add(pr);
        pi.add(hash);
        pi.add(city);
        pi.add(org);
        return pi;
    }
}