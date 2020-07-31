package com.nowcent.volleyball.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.utils.NetworkUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;

import java.io.IOException;

import static com.nowcent.volleyball.utils.ActivityUtils.invalid;

@EActivity(R.layout.activity_splash)
public class SplashActivity extends AppCompatActivity {

    @AfterViews
    void init(){
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.ic_launcher_background));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        checkInitData();
    }

    String password = null;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
//
////        setContentView(R.layout.activity_splash);
//
//    }

    @Background
    void checkInitData() {
        JSONObject result;
        try {
            result = NetworkUtils.getRemoteData();
//            checkVersion(result);
            String message = result.getString("message");
            password = result.getString("password");

            if (password == null || password.isEmpty()) {
                invalid(this);
            }

            if(canIShowPasswordActivity()){
                toPasswordActivity(password, result);
            }
            else{
                Intent intent = new Intent(SplashActivity.this, MainActivity_.class);
                intent.putExtra("data", result.toJSONString());
                startActivity(intent);
                finish();
            }

        } catch (IOException e) {
            e.printStackTrace();
            invalid(this, "权限验证失败");
        }
    }

    boolean canIShowPasswordActivity(){
        String savedPassword = getSavedPassword();
        return !password.equals(savedPassword);
    }

    void toPasswordActivity(String password, JSONObject data){
        Intent intent = new Intent(SplashActivity.this, PasswordActivity_.class);
        intent.putExtra("data", data.toJSONString());
        intent.putExtra("password", password);
        startActivity(intent);
        finish();
    }

    private String getSavedPassword(){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        return sharedPreferences.getString("password", null);
    }

}