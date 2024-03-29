package com.nowcent.volleyball.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.activity.MainActivity;
import com.nowcent.volleyball.pojo.ResultMessage;
import com.nowcent.volleyball.utils.DataUtils;


import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;
import javax.xml.transform.Result;

import lombok.SneakyThrows;

import static com.nowcent.volleyball.utils.Utils.getTeamId;
import static com.nowcent.volleyball.utils.Utils.save;

public class SpiderService extends IntentService {

    private String message = "";
    private boolean isRunning = true;
    private int taskTotal = 0;
    private int taskCurrent = 0;
    private int teamId = -1;

    List<ResultMessage> resultMessages = new ArrayList<>();

    /**
     * 任务线程池
     */
    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());


    public String getMessage() {
        return message;
    }



    public class SpiderBinder extends Binder{

        public void setMessage(String string){
            SpiderService.this.message = string;
        }

        public ResultMessage getLastResultMessages(){
            if(resultMessages.isEmpty()){
                return null;
            }
            else{
                return resultMessages.get(resultMessages.size() - 1);
            }
        }

        public void addTask(String token, Date startDate, Date endDate){
            taskTotal ++;
            onDataCallback.onCountDataChange(taskCurrent, taskTotal);
            if(executorService.isShutdown()){
                executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());
            }
            Thread thread = new Thread(() -> {
                try {
                    MainActivity.setStatus(true);
                    if(teamId == -1){
                        teamId = getTeamId(token, startDate, endDate);
                        if (teamId == -1) {
                            executorService.shutdownNow();

                            addResultMessage(new Date(), "获取TeamID失败");

                            taskCurrent = taskTotal;
                            onDataCallback.onCountDataChange(taskTotal, taskTotal);
                            onDataCallback.onGetTeamIdError();

                            taskTotal = 0;
                            taskCurrent = 0;
                            MainActivity.setStatus(false);
                            Thread.sleep(250);
                            return;
                        } else {
                            onDataCallback.onGetTeamIdSuccess(teamId);
                        }
                    }


                    boolean isSuccess = false;

                    //获取验证码信息
                    JSONArray captchaArray = DataUtils.getCaptchaArray(getApplicationContext());
                    if(captchaArray.size() == 0){
                        executorService.shutdownNow();

                        onDataCallback.onLackOfCaptcha();

                        taskCurrent = taskTotal;
                        onDataCallback.onCountDataChange(taskTotal, taskTotal);
                        onDataCallback.onLackOfCaptcha();

                        taskTotal = 0;
                        taskCurrent = 0;
                        MainActivity.setStatus(false);
                        Thread.sleep(250);

                        return;
                    }



                    long latestExpireTime = 0;

                    if(captchaArray.size() > 0){
                        latestExpireTime = ((JSONObject)captchaArray.get(0)).getLong("time");
                    }

                    if(captchaArray.size() > 1){
                        latestExpireTime = ((JSONObject)captchaArray.get(captchaArray.size() - 1)).getLong("time");
                    }
                    onDataCallback.onCaptchaInfoChange(captchaArray.size() - 1, ((JSONObject)captchaArray.get(0)).getLong("time"), latestExpireTime);
                    String captchaToken = ((JSONObject)captchaArray.get(0)).getString("nc_token");
                    String sig = ((JSONObject)captchaArray.get(0)).getString("sig");
                    String sessionId = ((JSONObject)captchaArray.get(0)).getString("csessionid");

                    captchaArray.remove(0);
                    DataUtils.saveCaptchaArray(getApplicationContext(), captchaArray);

                    String result = save(token, startDate, endDate, teamId, sessionId, sig, captchaToken);
                    if (result == null) {
//                        executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());
//                            showDialog("抢场成功", "请与" + startDate.getHours() + "时前到达场地", "好");
                        onDataCallback.success(startDate, endDate);
                        taskCurrent = taskTotal = 0;
                        MainActivity.setStatus(false);

                        addResultMessage(new Date(), "成功预定" + startDate.getHours() + "时至" + endDate.getHours() + "时的场地");
                        isSuccess = true;
                        executorService.shutdownNow();
                    } else {
                        String msg = JSON.parseObject(result).getString("msg");
                        onDataCallback.onTaskFail(msg);
                        addResultMessage(new Date(), msg);

//                            addStatusText(JSON.parseObject(result).getString("msg"));
                    }

                    taskCurrent++;
                    onDataCallback.onCountDataChange(taskCurrent, taskTotal);

                    if (taskCurrent >= taskTotal) {
                        taskCurrent = 0;
                        taskTotal = 0;

                        MainActivity.setStatus(false);
                        if(isSuccess){
                            onDataCallback.success(startDate, endDate);
                        }
                        else{
                            onDataCallback.fail();
                        }
                    }
                    Thread.sleep(250);

                } catch (ParseException | InterruptedException | IOException | ScriptException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
            });
            executorService.execute(thread);
        }

        public void addTask(String token, Date startDate, Date endDate, int count){
            for (int i = 0; i < count; i++) {
                addTask(token, startDate, endDate);
            }
        }


        public void shutdown(){
            executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());
        }


        public SpiderService getService(){
            return SpiderService.this;
        }
    }

    private SpiderBinder spiderBinder = new SpiderBinder();
    private OnDataCallback onDataCallback = null;

    private void addResultMessage(Date date, String message){
        ResultMessage resultMessage = new ResultMessage(date, message);
        onDataCallback.onRecentResult(resultMessage);
        resultMessages.add(resultMessage);
    }


    public void setOnDataCallback(OnDataCallback onDataCallback) {
        this.onDataCallback = onDataCallback;
    }

    public interface OnDataCallback{
        void onGetTeamIdError();
        void onGetTeamIdSuccess(int teamId);
        void onTaskSuccess(Date startDate, Date endDate);
        void onTaskFail(String message);
        void onRecentResult(ResultMessage recentResult);

        void onLackOfCaptcha();
        void onCaptchaInfoChange(int total, long earliestExpireTime, long latestExpireTime);

        void onCountDataChange(int current, int total);
        void success(Date startDate, Date endDate);
        void fail();
    }


    public SpiderService() {
        super("SpiderService");
    }


//    @SneakyThrows
    @Override
    protected void onHandleIntent(Intent intent) {

    }

    public SpiderBinder getSpiderBinder() {
        return spiderBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread() {
            @SneakyThrows
            @Override
            public void run() {
//                int i = 1;
                while (isRunning) {
                    Thread.sleep(2000);
//                    if(onDataCallback != null){
//                        onDataCallback.onDataChange(message + i);
//                    }
//                    i++;
//                    try {
//                        sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            }
        }.start();
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return spiderBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }


}
