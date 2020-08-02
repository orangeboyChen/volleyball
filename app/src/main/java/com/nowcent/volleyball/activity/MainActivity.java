package com.nowcent.volleyball.activity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.activity.fragment.MyFragment;
import com.nowcent.volleyball.activity.fragment.SpiderFragment;
import com.nowcent.volleyball.utils.FragmentSwitchTool;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.nowcent.volleyball.utils.LocationUtils.getPosition;


@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {


    long lastTime = 0;

    BottomNavigationView navView = null;

    NavController navController = null;

    @AfterViews
    void init(){

//        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );





        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(navView, navController);

        Intent intent = getIntent();
        String params = intent.getStringExtra("my");
        if("1".equals(params)){
            navController.navigate(R.id.myFragment);
            navView.setSelectedItemId(R.id.myFragment);
        }


        navView.setOnNavigationItemSelectedListener(item -> {
            if(System.currentTimeMillis() - lastTime < 150){
                return false;
            }

            lastTime = System.currentTimeMillis();
            if (item.isChecked()) {
                return true;
            }

            if(status){
                showDialog("抢场时不允许切换界面", null, "好");
                return false;
            }


            boolean popBackStack = navController.popBackStack(item.getItemId(), false);
            if(popBackStack){
                return true;
            }
            else{
                return NavigationUI.onNavDestinationSelected(item, navController);
            }
        });



    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((keyCode == KeyEvent.KEYCODE_BACK) ||
                (keyCode == KeyEvent.KEYCODE_HOME))
                && event.getRepeatCount() == 0) {
            finish();
        }
        return false;

    }

    private static boolean status = false;
    public static void setStatus(boolean flag){
        status = flag;
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//    }

    @UiThread
    void showDialog(String title, String text, String positiveButtonText){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, (dialogInterface, i) -> {})
                .setCancelable(false)
                .show();

    }

    public NavController getNavController() {
        return navController;
    }

    public BottomNavigationView getNavView() {
        return navView;
    }
}