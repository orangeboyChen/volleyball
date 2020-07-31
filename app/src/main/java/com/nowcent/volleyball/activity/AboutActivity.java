package com.nowcent.volleyball.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.nowcent.volleyball.R;
import com.tencent.android.tpush.XGPushConfig;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.LongClick;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @AfterViews
    void init(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));

        setSupportActionBar(toolbar);
        toolbar.setTitle("关于");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_48px);
        toolbar.setNavigationOnClickListener((l) -> {
            onBackPressed();
        });

        String token = XGPushConfig.getToken(this);
        Log.e("about token: ", token);
        xgTokenEditText.setText(token);

        List<String> list = Arrays.asList("检查新版本", "隐私声明", "反馈与建议");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(arrayAdapter);
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
}