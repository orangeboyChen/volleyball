package com.nowcent.volleyball.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.BuildConfig;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

public class NetworkUtils {

    public static JSONObject getRemoteData() throws IOException {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://volleyball.nowcent.cn/password.html");
        CloseableHttpResponse execute;
        execute = closeableHttpClient.execute(httpGet);


        String result = EntityUtils.toString(execute.getEntity(), "UTF-8");

        if (result == null || result.isEmpty()) {
            throw new IOException();
        }

        return JSON.parseObject(result);
    }


    /**
     * 检查新版本
     * @param context Activity
     * @param jsonObject 数据
     * @param newVersion 当前为新版本
     * @param lowerThanOldVersion 旧版本但不是废弃版本
     * @param lowerThanMinVersion 废弃版本
     * @param invalid 非法
     * @throws PackageManager.NameNotFoundException 包找不到错误
     */
    public static void checkVersion(Context context, JSONObject jsonObject,
                                    Consumer<HashMap<String, String>> newVersion,
                                    Consumer<HashMap<String, String>> lowerThanOldVersion,
                                    Consumer<HashMap<String, String>> lowerThanMinVersion,
                                    Runnable invalid) throws PackageManager.NameNotFoundException {
        int newVersionCode = jsonObject.getIntValue("newVersion");
        int minVersionCode = jsonObject.getIntValue("minVersion");
        String apkUrl = jsonObject.getString("apkUrl");
        String newVersionName = jsonObject.getString("newVersionName");
        String updateLog = jsonObject.getString("updateLog");
        PackageInfo packageInfo = context.getApplicationContext()
                .getPackageManager()
                .getPackageInfo(context.getPackageName(), 0);
        int localVersion = packageInfo.versionCode;

        if (BuildConfig.DEBUG && minVersionCode <= 0) {
            invalid.run();
            return;
        }

        HashMap<String, String> newVersionHashMap = new HashMap<>(3);
        newVersionHashMap.put("newVersionName", newVersionName);
        newVersionHashMap.put("apkUrl", apkUrl);
        newVersionHashMap.put("updateLog", updateLog);

        if (newVersionCode > localVersion) {
            if (minVersionCode > localVersion) {
                lowerThanMinVersion.accept(newVersionHashMap);
            } else {
                lowerThanOldVersion.accept(newVersionHashMap);
            }
        }
        else{
            newVersion.accept(newVersionHashMap);
        }

    }
}
