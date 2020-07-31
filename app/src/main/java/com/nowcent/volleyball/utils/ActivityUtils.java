package com.nowcent.volleyball.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.nowcent.volleyball.activity.BanActivity_;

public class ActivityUtils {
    public static void invalid(Activity activity, String message){
        Intent intent = new Intent(activity, BanActivity_.class);
        intent.putExtra("message", message);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void invalid(Activity activity){
        Intent intent = new Intent(activity, BanActivity_.class);
        activity.startActivity(intent);
        activity.finish();
    }
}
