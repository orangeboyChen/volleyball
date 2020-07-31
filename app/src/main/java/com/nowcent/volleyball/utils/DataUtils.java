package com.nowcent.volleyball.utils;

import android.content.Context;
import android.content.SharedPreferences;

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
}
