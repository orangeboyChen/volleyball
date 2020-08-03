package com.nowcent.volleyball.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.VolleyballApplication;
import com.nowcent.volleyball.utils.NetworkUtils;
import com.xiaomi.mipush.sdk.MiPushClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import static com.nowcent.volleyball.utils.ActivityUtils.invalid;
import static com.nowcent.volleyball.utils.DataUtils.getSavedPassword;
import static com.nowcent.volleyball.utils.DataUtils.getSavedToken;
import static com.nowcent.volleyball.utils.DataUtils.saveToken;
import static com.nowcent.volleyball.utils.Utils.getTeamId;
import static com.nowcent.volleyball.utils.Utils.getTimeString;
import static com.nowcent.volleyball.utils.Utils.isNumber;
import static com.nowcent.volleyball.utils.Utils.save;

@EActivity(R.layout.activity_spider)
@OptionsMenu(R.menu.top_button)
public class SpiderActivity extends AppCompatActivity {

    private final int MIN_HOUR = 9;
    private final int MAX_HOUR = 22;
    private String password = null;
    private int teamId = -1;
    Thread fastModeThread;
    boolean fastModeThreadFlag = false;

    @ViewById(R.id.tokenEditText)
    EditText tokenEditText;

    @ViewById(R.id.startDateEditText)
    EditText startDateEditText;

    @ViewById(R.id.endDateEditText)
    EditText endDateEditText;

    @ViewById(R.id.submitButton)
    Button submitButton;


    @ViewById(R.id.statusEditText)
    EditText statusEditText;

    @ViewById(R.id.toolbar)
    Toolbar toolbar;

    @StringRes(R.string.app_name)
    String appName;



    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());


    @AfterViews
    void init(){
        mipushInit();
        hmspushInit();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));

//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        setSupportActionBar(toolbar);
        toolbar.inflateMenu(R.menu.top_button);
        toolbar.setTitle(appName);
        toolbar.setOnMenuItemClickListener((item) -> {
            switch(item.getItemId()){
                case R.id.aboutButton:
                    Intent intent = new Intent(this, AboutActivity_.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
            }
            return super.onOptionsItemSelected(item);
        });


        setRemoteData();
        String savedToken = getSavedToken(this);
        tokenEditText.setText(savedToken == null ? "" : savedToken);
        statusEditText.setVerticalScrollBarEnabled(true);
    }

    void mipushInit(){
        VolleyballApplication.setMainActivity(this);
        //设置别名，撤销别名（alias）
        MiPushClient.setAlias(SpiderActivity.this, "123456", null);
        //MiPushClient.unsetAlias(MainActivity.this, "demo1", null);
        //设置账号，撤销账号（account）
        MiPushClient.setUserAccount(SpiderActivity.this, "123456", null);
        //MiPushClient.unsetUserAccount(MainActivity.this, "user1", null);
        //设置标签，撤销标签（topic：话题、主题）
        MiPushClient.subscribe(SpiderActivity.this, "notification", null);
        //MiPushClient.unsubscribe(MainActivity.this, "IT", null);
        //设置接收时间（startHour, startMin, endHour, endMin）
        MiPushClient.setAcceptTime(SpiderActivity.this, 0, 0, 23, 59, null);
        //暂停和恢复推送 //MiPushClient.pausePush(MainActivity.this, null);
        //MiPushClient.resumePush(MainActivity.this, null);
    }

    void hmspushInit(){
        MyReceiver receiver = new MyReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction("com.huawei.codelabpush.ON_NEW_TOKEN");
        SpiderActivity.this.registerReceiver(receiver,filter);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_spider);
//    }


    @UiThread
    void addStatusText(String text){
        String currentText = statusEditText.getText().toString();
        statusEditText.setText(currentText + "\n[" + getTimeString() + "] " + text);
        statusEditText.setMovementMethod(ScrollingMovementMethod.getInstance());
        statusEditText.setSelection(statusEditText.getText().length(), statusEditText.getText().length());
    }

    @Background
    void setRemoteData(){
        JSONObject result;
        try{
            result = JSON.parseObject(getIntent().getStringExtra("data"));
            if(result == null || result.isEmpty()){
                result = NetworkUtils.getRemoteData();
            }

            checkVersion(result);
            String message = result.getString("message");
            password = result.getString("password");

            //不允许使用
            if(password == null || password.isEmpty()){
                invalid(this);
            }

            //检查密钥
            String savedPassword = getSavedPassword(this);
            if(!password.equals(savedPassword)){
                Intent intent = new Intent(this, PasswordActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("password", password);
                startActivity(intent);
                finish();
            }

            if (!message.isEmpty()) {
                addStatusText("\n[公共信息]\n" + message);
            }

        } catch (PackageManager.NameNotFoundException | IOException e) {
            e.printStackTrace();
            invalid(this, "权限验证失败");
        }
    }

    @TextChange(R.id.startDateEditText)
    void changeDate(){
        String startDate = startDateEditText.getText().toString();
        if(!startDate.isEmpty() && isNumber(startDate)){
            endDateEditText.setText(String.valueOf(Integer.parseInt(startDate) + 1));
        }
    }




    @Background
    void spider(String token, Date startDate, Date endDate){
        Thread thread = new Thread(() -> {
            try {
                addStatusText("正在执行");
                if(teamId == -1){
                    teamId = getTeamId(token, startDate, endDate);
                    if(teamId == -1){
                        addStatusText("获取TeamID失败");
                        return;
                    }
                    else{
                        addStatusText("TeamID: " + teamId);
                    }
                }
                String result;
                if((result = save(token, startDate, endDate, teamId)) == null){
                    addStatusText("抢场成功");
                    executorService.shutdownNow();
                    executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());
                    showDialog("抢场成功", "请与" + startDate.getHours() + "时前到达场地", "好");
                }
                else{
                    addStatusText(JSON.parseObject(result).getString("msg"));
                }
                Thread.sleep(250);

            } catch (ParseException e) {
                e.printStackTrace();
                addStatusText("转化失败");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                addStatusText("获取JavaScript特定方法失败");
            } catch (ScriptException e) {
                e.printStackTrace();
                addStatusText("获取JavaScript失败");
            } catch (IOException e) {
                e.printStackTrace();
                addStatusText("读取JavaScript失败");
            } catch (JSONException e) {
                e.printStackTrace();
                addStatusText(e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        executorService.execute(thread);
    }


    @Click(R.id.submitButton)
    void onSubmitButtonClick() {
//        LogcatHelper.getInstance(this).start();
        if(!isValidInput()){
            return;
        }
        saveToken(this, tokenEditText.getText().toString());

        final String token = tokenEditText.getText().toString();
        final Date startDate;
        final Date endDate;
        final String pattern = "yyyy-MM-dd HH:mm:ss";
        final SimpleDateFormat dateFormat=new SimpleDateFormat(pattern);
        try {
//            startDate = DateFormat.getDateTimeInstance().parse("2013-01-01 " + startDateEditText.getText().toString() + ":00:00");
//            endDate = DateFormat.getDateTimeInstance().parse("2013-01-01 " + endDateEditText.getText().toString() + ":00:00");

            startDate = dateFormat.parse("2013-01-01 " + Integer.parseInt(startDateEditText.getText().toString()) + ":00:00");
            endDate = dateFormat.parse("2013-01-01 " + Integer.parseInt(endDateEditText.getText().toString()) + ":00:00");

        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        spider(token, startDate, endDate);
    }




    @LongClick(R.id.submitButton)
    void onSubmitButtonLongClick() {
        fastModeThreadFlag = true;
        fastModeThread = new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                if(!fastModeThreadFlag){
                    break;
                }
                onSubmitButtonClick();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        fastModeThread.start();
        showDialog("已打开快速模式", "自动点击按钮50次。如需退出，请长按状态显示框", "好");
    }

    @LongClick(R.id.statusEditText)
    void stopSpider(){
        showDialog("确定停止订场吗", null, "是", ((dialogInterface, i) -> {
            fastModeThreadFlag = false;
            executorService.shutdownNow();
            executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());
            showDialog("已停止订场", null, "好");

        }), "否", ((dialogInterface, i) -> {}));
    }

    @UiThread
    void showDialog(String title, String text, String positiveButtonText, DialogInterface.OnClickListener positiveListener,
                    String negativeButtonText, DialogInterface.OnClickListener negativeListener){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, positiveListener)
                .setNegativeButton(negativeButtonText, negativeListener)
                .setCancelable(false)
                .show();

    }

    @UiThread
    void showDialog(String title, String text, String positiveButtonText){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, (dialogInterface, i) -> {})
                .setCancelable(false)
                .show();

    }

    @UiThread
    void showDialog(String title, String text, String positiveButtonText, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, listener)
                .setCancelable(false)
                .show();

    }

    boolean isValidInput(){
//        //错误的密钥
//        if(!password.contentEquals(passwordEditText.getText().toString())){
//            showDialog("密钥错误", "请重新输入密钥", "好");
//            return false;
//        }

        //检查文本框信息是否为空
        if(tokenEditText.getText().toString().isEmpty() ||
                startDateEditText.getText().toString().isEmpty() ||
                endDateEditText.getText().toString().isEmpty()){
            showDialog("有未填写的信息", "请填写所有信息", "好");
            return false;
        }

        //检查文本框的信息是否正确（日期）
        if(!isNumber(startDateEditText.getText().toString()) || !isNumber(endDateEditText.getText().toString())){
            showDialog("日期填写错误", "请填写正确的日期", "好");
            return false;
        }

        int startHour = Integer.parseInt(startDateEditText.getText().toString());
        int endHour = Integer.parseInt(endDateEditText.getText().toString());

        if(startHour < MIN_HOUR ||
                startHour > MAX_HOUR ||
                endHour < MIN_HOUR ||
                endHour > MAX_HOUR ||
                endHour - startHour != 1){
            showDialog("日期填写错误", "请填写正确的日期", "好");
            return false;
        }
        return true;
    }




    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.huawei.codelabpush.ON_NEW_TOKEN".equals(intent.getAction())) {
//                String token = intent.getStringExtra("token");
//                Toast.makeText(context, token, Toast.LENGTH_LONG).show();
//                showDialog("token", token, "好");
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        onRestartBackground();
    }

    @Background
    void onRestartBackground(){
        try {
            JSONObject jsonObject = NetworkUtils.getRemoteData();
            checkVersion(jsonObject);
        }catch (IOException | PackageManager.NameNotFoundException e){
            e.printStackTrace();
            invalid(this, "权限验证失败");
        }
    }

    void checkVersion(JSONObject jsonObject) throws PackageManager.NameNotFoundException {
        NetworkUtils.checkVersion(getApplicationContext(), jsonObject,
                (newVersionHashMap) -> {},
                (newVersionHashMap) -> {
                    showDialog("找到新版本",
                            newVersionHashMap.get("newVersionName") + "\n" + newVersionHashMap.get("updateLog"),
                            "去下载",
                            ((dialogInterface, i) -> {
                                Intent intent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(newVersionHashMap.get("apkUrl"))
                                );
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }),
                            "下次再说",
                            ((dialogInterface, i) -> {
                            }));

                }, (newVersionHashMap) -> {
                    showDialog("你的版本过时了，请前往下载",
                            newVersionHashMap.get("newVersionName") + "\n" + newVersionHashMap.get("updateLog"),
                            "好",
                            ((dialogInterface, i) -> {
                                Intent intent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(newVersionHashMap.get("apkUrl"))
                                );
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            }));
                }, () -> invalid(this));

//        //检查版本
//        int newVersionCode = jsonObject.getIntValue("newVersion");
//        int minVersionCode = jsonObject.getIntValue("minVersion");
//        String apkUrl = jsonObject.getString("apkUrl");
//        String newVersionName = jsonObject.getString("newVersionName");
//        String updateLog = jsonObject.getString("updateLog");
//        PackageInfo packageInfo = getApplicationContext()
//                .getPackageManager()
//                .getPackageInfo(getPackageName(), 0);
//        int localVersion = packageInfo.versionCode;
//
//        if (BuildConfig.DEBUG && minVersionCode <= 0) {
//            invalid();
//            return;
//        }
//
//        if (newVersionCode > localVersion) {
//            if (minVersionCode > localVersion) {
//                showDialog("你的版本过时了，请前往下载",
//                        newVersionName + "\n" + updateLog,
//                        "好",
//                        ((dialogInterface, i) -> {
//                            Intent intent = new Intent(
//                                    Intent.ACTION_VIEW,
//                                    Uri.parse(apkUrl)
//                            );
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//                            finish();
//
//                        }));
//            } else {
//                showDialog("找到新版本",
//                        newVersionName + "\n" + updateLog,
//                        "去下载",
//                        ((dialogInterface, i) -> {
//                            Intent intent = new Intent(
//                                    Intent.ACTION_VIEW,
//                                    Uri.parse(apkUrl)
//                            );
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            startActivity(intent);
//
//                        }),
//                        "下次再说",
//                        ((dialogInterface, i) -> {
//                        }));
//            }
//        }
    }

//    void invalid(){
////        runOnUiThread(() -> {
////            Toast.makeText(this, "本应用已被关闭，请稍后再试！", Toast.LENGTH_LONG).show();
////        });
////        finish();
//        Intent intent = new Intent(this, BanActivity_.class);
//        startActivity(intent);
//        finish();
//    }
//
//    void invalid(String message){
////        runOnUiThread(() -> {
////            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
////        });
////        finish();
//    }
}