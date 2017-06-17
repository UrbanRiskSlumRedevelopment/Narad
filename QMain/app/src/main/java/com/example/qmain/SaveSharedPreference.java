package com.example.qmain;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.Context;
import android.preference.PreferenceManager;
import java.util.ArrayList;

public class SaveSharedPreference
{
    static final String PREF_USER_NAME= "username";
    static final String PROJECT = "project";
    static final String HASH = "hash";
    static final String CITY = "city";
    static final String ORG = "organization";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setUserName(Context ctx, String userName)
    {
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, userName);
        editor.commit();
    }

    public static String getUserName(Context ctx)
    {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    public static void setProjectInfo(Context ctx, String pr, String hash, String city, String org){
        Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PROJECT, pr);
        editor.putString(HASH, hash);
        editor.putString(CITY, city);
        editor.putString(ORG, org);
        editor.commit();
    }

    public static ArrayList<String> getProjectInfo(Context ctx){
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