package com.nowcent.volleyball;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import com.nowcent.volleyball.activity.SpiderActivity;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

public class VolleyballApplication extends Application {
    // user your appid the key.
    private static final String APP_ID = "2882303761518536386";
    // user your appid the key.
    private static final String APP_KEY = "5221853638386";

    // 此TAG在adb logcat中检索自己所需要的信息， 只需在命令行终端输入 adb logcat | grep
    // com.xiaomi.mipushdemo
    public static final String TAG = "com.xiaomi.mipushdemo";

    private static DemoHandler sHandler = null;
    private static SpiderActivity sSpiderActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();

        XGPushConfig.enableDebug(this,true);
        XGPushManager.registerPush(this, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                //token在设备卸载重装的时候有可能会变
                Log.d("TPush", "注册成功，设备token为：" + data);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
        //打开第三方推送
        XGPushConfig.enableOtherPush(getApplicationContext(), true);

        XGPushConfig.setMiPushAppId(getApplicationContext(), APP_ID);
        XGPushConfig.setMiPushAppKey(getApplicationContext(), APP_KEY);


        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }

//        LoggerInterface newLogger = new LoggerInterface() {
//
//            @Override
//            public void setTag(String tag) {
//                // ignore
//            }
//
//            @Override
//            public void log(String content, Throwable t) {
//                Log.d(TAG, content, t);
//            }
//
//            @Override
//            public void log(String content) {
//                Log.d(TAG, content);
//            }
//        };
//        Logger.setLogger(this, newLogger);
        if (sHandler == null) {
            sHandler = new DemoHandler(getApplicationContext());
        }
    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    public static void reInitPush(Context ctx) {
        MiPushClient.registerPush(ctx.getApplicationContext(), APP_ID, APP_KEY);
    }

    public static DemoHandler getHandler() {
        return sHandler;
    }

    public static void setMainActivity(SpiderActivity activity) {
        sSpiderActivity = activity;
    }

    public static class DemoHandler extends Handler {

        private Context context;

        public DemoHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(Message msg) {
            String s = (String) msg.obj;
            if (sSpiderActivity != null) {
//                sMainActivity.refreshLogInfo();
            }
            if (!TextUtils.isEmpty(s)) {
//                Toast.makeText(context, s, Toast.LENGTH_LONG).show();

            }
        }
    }

}
