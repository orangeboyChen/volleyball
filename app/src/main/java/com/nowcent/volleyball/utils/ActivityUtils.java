package com.nowcent.volleyball.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.nowcent.volleyball.activity.BanActivity_;

public class ActivityUtils {
    public static void invalid(Activity activity, String message){
        Intent intent = new Intent(activity, BanActivity_.class);
        intent.putExtra("message", message);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    public static void invalid(Activity activity){
        invalid(activity, null);
    }
}
