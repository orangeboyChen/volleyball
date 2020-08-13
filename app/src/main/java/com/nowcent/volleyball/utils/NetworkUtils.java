package com.nowcent.volleyball.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.BuildConfig;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.function.Consumer;

public class NetworkUtils {

    private static final String SECRET = "DYaLiwkFYrHZjcIY";

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

        if (minVersionCode <= 0) {
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


    public static JSONObject getRecentList() throws IOException {
        String signature = getSignature();
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://volleyball.nowcent.cn/api/get?signature=" + signature);
        CloseableHttpResponse execute;
        execute = closeableHttpClient.execute(httpGet);


        String result = EntityUtils.toString(execute.getEntity(), "UTF-8");

        if (result == null || result.isEmpty()) {
            throw new IOException();
        }

        return JSON.parseObject(result);
    }


    public static JSONObject broadCastSuccess(String token, String from, String to, String nickname) throws IOException {
        String signature = getSignature(token, from, to, nickname);

        assert signature != null;
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://volleyball.nowcent.cn/api/success?token=" + token +
                "&from=" + from + "&to=" + to + "&nickname=" + nickname + "&signature=" + signature);

        CloseableHttpResponse execute;
        execute = closeableHttpClient.execute(httpGet);



        String result = EntityUtils.toString(execute.getEntity(), "UTF-8");

        if (result == null || result.isEmpty()) {
            throw new IOException();
        }

        return JSON.parseObject(result);
    }


    public static JSONObject join(String fromToken, String toToken, String nickname) throws IOException {
        String signature = getSignature(fromToken, toToken, nickname);


        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://volleyball.nowcent.cn/api/join?fromToken=" + fromToken +
                "&toToken=" + toToken + "&nickname=" + nickname + "&signature=" + signature);
        CloseableHttpResponse execute;
        execute = closeableHttpClient.execute(httpGet);


        String result = EntityUtils.toString(execute.getEntity(), "UTF-8");

        if (result == null || result.isEmpty()) {
            throw new IOException();
        }

        return JSON.parseObject(result);
    }

    public static JSONObject sendFeedback(String feedback) throws IOException {
        String signature = getSignature(feedback);


        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://volleyball.nowcent.cn/api/feedback?" + "feedback=" + feedback +
                "&signature=" + signature);
        CloseableHttpResponse execute;
        execute = closeableHttpClient.execute(httpPost);


        String result = EntityUtils.toString(execute.getEntity(), "UTF-8");

        if (result == null || result.isEmpty()) {
            throw new IOException();
        }

        return JSON.parseObject(result);
    }


    private static String getSignature(String ... params){
        StringBuilder paramString = new StringBuilder();
        for (String param : params) {
            paramString.append(param);
        }
        paramString.append(SECRET);

        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(paramString.toString().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                int c = b & 0xff;
                String result = Integer.toHexString(c);
                if(result.length()<2){
                    sb.append(0);
                }
                sb.append(result);
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


}
