package com.nowcent.volleyball.pushService;

import android.content.Intent;
import android.util.Log;

import com.huawei.hms.push.HmsMessageService;

import org.androidannotations.annotations.UiThread;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpGetHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class HmspushMessageService extends HmsMessageService {
    private static final String TAG = "PushDemoLog";
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.i(TAG, "receive token:" + token);
        sendTokenToDisplay(token);
    }

    private void sendTokenToDisplay(String token) {
        new Thread(() -> {
            CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("http://volleyball.nowcent.cn/api/hms?token=" + token);

            try{
                CloseableHttpResponse execute = closeableHttpClient.execute(httpGet);
            }catch (Exception e){
                e.printStackTrace();
            }

        }).start();

        Intent intent = new Intent("com.huawei.push.codelab.ON_NEW_TOKEN");
        intent.putExtra("token", token);
        sendBroadcast(intent);

    }
}
