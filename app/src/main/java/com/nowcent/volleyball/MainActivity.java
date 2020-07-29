package com.nowcent.volleyball;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.EditText;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

import static com.nowcent.volleyball.Utils.getTeamId;
import static com.nowcent.volleyball.Utils.getTimeString;
import static com.nowcent.volleyball.Utils.isNumber;
import static com.nowcent.volleyball.Utils.save;

@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

//    //mipush
//    private static final String APP_ID = "2882303761518536386";
//    private static final String APP_KEY = "5221853638386";
//    public static final String TAG = "com.xiaomi.mipushdemo";
//    private static DemoHandler sHandler = null;
//    private static MainActivity sMainActivity = null;



    private final int MIN_HOUR = 9;
    private final int MAX_HOUR = 22;
    private String password = null;
    private int teamId = -1;

    @ViewById(R.id.tokenEditText)
    EditText tokenEditText;

    @ViewById(R.id.startDateEditText)
    EditText startDateEditText;

    @ViewById(R.id.endDateEditText)
    EditText endDateEditText;

    @ViewById(R.id.passwordEditText)
    EditText passwordEditText;

    @ViewById(R.id.submitButton)
    Button submitButton;

    @ViewById(R.id.statusEditText)
    EditText statusEditText;

    ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());

    @AfterViews
    void init(){
        getPassword();
        String savedToken = getSavedToken();
        tokenEditText.setText(savedToken == null ? "" : savedToken);

        String savedPassword = getSavedPassword();
        passwordEditText.setText(savedPassword == null ? "" : savedPassword);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
////        setContentView(R.layout.activity_main);
//    }






    @UiThread
    void addStatusText(String text){
        String currentText = statusEditText.getText().toString();
        statusEditText.setText(currentText + "\n[" + getTimeString() + "] " + text);
        statusEditText.setMovementMethod(ScrollingMovementMethod.getInstance());
        statusEditText.setSelection(statusEditText.getText().length(), statusEditText.getText().length());
    }

    @Background
    void getPassword(){
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://volleyball.nowcent.cn/password.html");
        CloseableHttpResponse execute;
        try{
            execute = closeableHttpClient.execute(httpGet);


            String result = EntityUtils.toString(execute.getEntity(), "UTF-8");

            if(result == null || result.isEmpty()){
                showDialog("获取密钥失败", "请检查网络连接", "好");
                return;
            }

            //解析密钥
            JSONObject jsonObject = JSON.parseObject(result);
            password = jsonObject.getString("password");

            //检查版本
            int newVersionCode = jsonObject.getIntValue("newVersion");
            PackageInfo packageInfo = getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(getPackageName(), 0);
            int localVersion = packageInfo.versionCode;

            if(newVersionCode > localVersion){
                showDialog("找到新版本", "请前往下载", "好");
            }

            String message = jsonObject.getString("message");
            if(!message.isEmpty()){
                addStatusText("\n[公共信息]\n" + message);
            }

        }catch (IOException | PackageManager.NameNotFoundException e){
            e.printStackTrace();
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
                    addStatusText("成功");
                    executorService.shutdownNow();
                    executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(512), new ThreadPoolExecutor.DiscardPolicy());
                }
                else{
                    addStatusText(result);
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
        if(!isValidInput()){
            return;
        }
        saveToken();
        savePassword();

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

    @UiThread
    void showDialog(String title, String text, String positiveButtonText){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, (dialogInterface, i) -> {})
                .show();

    }

    boolean isValidInput(){
        //错误的密钥
        if(!password.contentEquals(passwordEditText.getText().toString())){
            showDialog("密钥错误", "请重新输入密钥", "好");
            return false;
        }

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

    private void saveToken(){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", tokenEditText.getText().toString());
        editor.apply();
    }

    private String getSavedToken(){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString("token", null);
    }

    private void savePassword(){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("password", passwordEditText.getText().toString());
        editor.apply();
    }

    private String getSavedPassword(){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString("password", null);
    }







}