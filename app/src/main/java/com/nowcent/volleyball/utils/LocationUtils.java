package com.nowcent.volleyball.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.List;

public class LocationUtils {
    public static void getPosition(Context context, AMapLocationListener listener){
        AMapLocationClient client = new AMapLocationClient(context);
        client.setLocationListener(listener);
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setOnceLocation(true);
        client.setLocationOption(option);
        client.stopLocation();
        client.startLocation();


    }



}
