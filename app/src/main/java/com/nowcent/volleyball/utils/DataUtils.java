package com.nowcent.volleyball.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;


import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

public class DataUtils {

    final static String DATABASE_SCHEME = "data";

    public static void saveToken(Context context, String string){
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATABASE_SCHEME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", string);
        editor.apply();
    }

    public static String getSavedToken(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATABASE_SCHEME, MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }

    public static void savePassword(Context context, String string){
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATABASE_SCHEME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password", string);
        editor.apply();
    }

    public static String getSavedPassword(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATABASE_SCHEME, MODE_PRIVATE);
        return sharedPreferences.getString("password", null);
    }

    public static void save(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATABASE_SCHEME, MODE_PRIVATE);
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
        SharedPreferences sharedPreferences = context.getSharedPreferences(DATABASE_SCHEME, MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static String getCurrent(Context context, String key){
        String database = String.valueOf(getDailyStartTime(System.currentTimeMillis()));
        SharedPreferences sharedPreferences = context.getSharedPreferences(database, MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

    public static void saveCaptcha(Context context, JSONObject result){
        JSONArray newCaptchaArray = updateCaptcha(context, false);

        result.put("time", System.currentTimeMillis());
        newCaptchaArray.add(result);

        save(context, "captcha", newCaptchaArray.toJSONString());
    }


    public static void saveCaptchaArray(Context context, JSONArray result){
        save(context, "captcha", result.toJSONString());
    }

    public static JSONArray updateCaptcha(Context context, boolean save){
        String raw = get(context, "captcha");
        if(raw == null){
            JSONArray captchaArray = new JSONArray();
            save(context, "captcha", captchaArray.toJSONString());
            return captchaArray;
        }

        JSONArray captchaArray = JSON.parseArray(raw);
        JSONArray newCaptchaArray = new JSONArray();
        if (captchaArray != null && captchaArray.size() > 0){
            for (int i = 0; i < captchaArray.size(); i++) {
                long time = ((JSONObject)captchaArray.get(i)).getLong("time");
                if (System.currentTimeMillis() - time <= 3 * 60 * 1000){
                    newCaptchaArray.add(captchaArray.get(i));
                }
            }
        }

        if(save){
            save(context, "captcha", newCaptchaArray.toJSONString());
        }

        return newCaptchaArray;
    }

    public static Map<String, Long> getCaptchaInfo(Context context){
        JSONArray captchaArray = updateCaptcha(context, true);
        long earliestExpireTime = 0;
        long latestExpireTime = 0;

        if(captchaArray.size() > 0){
            earliestExpireTime = ((JSONObject)captchaArray.get(0)).getLong("time");
            latestExpireTime = earliestExpireTime;
        }

        if(captchaArray.size() > 1){
            latestExpireTime = ((JSONObject)captchaArray.get(captchaArray.size() - 1)).getLong("time");
        }

        Map<String, Long> returnMap = new HashMap<>();
        returnMap.put("earliestExpireTime", earliestExpireTime);
        returnMap.put("latestExpireTime", latestExpireTime);
        returnMap.put("size", (long) captchaArray.size());

        return returnMap;
    }

    public static JSONArray getCaptchaArray(Context context){
        return updateCaptcha(context, true);
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
