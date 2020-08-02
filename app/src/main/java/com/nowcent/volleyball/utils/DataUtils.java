package com.nowcent.volleyball.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

public class DataUtils {
    public static void saveToken(Context context, String string){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", string);
        editor.apply();
    }

    public static String getSavedToken(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }

    public static void savePassword(Context context, String string){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password", string);
        editor.apply();
    }

    public static String getSavedPassword(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString("password", null);
    }

    public static void save(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void save(Context context, String database, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(database, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void saveCurrent(Context context, String key, String value){
        String database = String.valueOf(getDailyStartTime(System.currentTimeMillis()));
        SharedPreferences sharedPreferences = context.getSharedPreferences(database, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String get(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static String getCurrent(Context context, String key){
        String database = String.valueOf(getDailyStartTime(System.currentTimeMillis()));
        SharedPreferences sharedPreferences = context.getSharedPreferences(database, MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }





    public static Long getDailyStartTime(Long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.setTimeInMillis(timeStamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
