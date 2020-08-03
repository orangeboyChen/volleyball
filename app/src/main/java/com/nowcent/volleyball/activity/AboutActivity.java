package com.nowcent.volleyball.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.utils.NetworkUtils;
import com.tencent.android.tpush.XGPushConfig;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nowcent.volleyball.utils.ActivityUtils.invalid;

@EActivity(R.layout.activity_about)
public class AboutActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_about);
//    }

    @ViewById(R.id.XGTokenEditText)
    EditText xgTokenEditText;

    @ViewById(R.id.aboutToolbar)
    Toolbar toolbar;

    @ViewById(R.id.menuListview)
    ListView listView;

    private List<String> list;
    private ArrayAdapter<String> arrayAdapter;
    private boolean isCheckingVersion = false;

    @AfterViews
    void init(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));

        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );


        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_48px);
        toolbar.setNavigationOnClickListener((l) -> {
            onBackPressed();
        });

        String token = XGPushConfig.getToken(this);
        Log.e("about token: ", token);
        xgTokenEditText.setText(token);

        list = Arrays.asList("检查新版本", "隐私声明", "反馈与建议");
        arrayAdapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            switch (position){
                case 0:
                    checkVersion(false);
                    break;
                case 1:
//                    Intent intent = new Intent(
//                            Intent.ACTION_VIEW,
//                            Uri.parse("http://volleyball.nowcent.cn/privacy.html")
//                    );
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);

                    Intent intent1 = new Intent(this, PrivacyActivity_.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent1);
                    break;
                case 2:
                    Intent intent2 = new Intent(this, FeedbackActivity_.class);
                    intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent2);
                    break;
            }
        });

        checkVersion(true);
    }

    @UiThread
    void setVersionItemText(String text){
        list.set(0, text);
        arrayAdapter.notifyDataSetChanged();
    }

    @Background
    void checkVersion(boolean isAutoCheck){
        if(isCheckingVersion){
            return;
        }
        isCheckingVersion = true;
        try {
            JSONObject remoteData = NetworkUtils.getRemoteData();
            runOnUiThread(() -> {
                isCheckingVersion = false;
                try {
                    NetworkUtils.checkVersion(getApplicationContext(), remoteData,
                            (newVersionHashMap) -> {
                        setVersionItemText("已是最新版本");
                        if(isAutoCheck){
                            return;
                        }
                                showDialog("已是最新版本", null, "好");
                            },
                            (newVersionHashMap) -> {
                                setVersionItemText("找到新版本");
                                if(isAutoCheck){
                                    return;
                                }
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
                                setVersionItemText("找到新版本");
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
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    invalid(this);
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
            invalid(this);
        }

    }

    @LongClick(R.id.XGTokenEditText)
    void copyXgToken(){
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setText(xgTokenEditText.getText().toString());
        showDialog("复制成功", null, "好");
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
    void showDialog(String title, String text, String positiveButtonText, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, listener)
                .setCancelable(false)
                .show();

    }



}