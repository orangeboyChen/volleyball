package com.nowcent.volleyball.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.nowcent.volleyball.R;
import com.nowcent.volleyball.activity.fragment.MyFragment_;
import com.nowcent.volleyball.pojo.ResultMessage;
import com.nowcent.volleyball.service.SpiderService;
import com.nowcent.volleyball.utils.DataUtils;
import com.nowcent.volleyball.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

import static com.nowcent.volleyball.utils.Utils.getTimeString;
import static com.nowcent.volleyball.utils.Utils.isNumber;
import static com.nowcent.volleyball.utils.Utils.isValidInput;

@EActivity(R.layout.activity_schedule)
public class ScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
    }

    private ServiceConnection spiderConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (SpiderService.SpiderBinder)iBinder;
            spiderService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    SpiderService spiderService = null;
    SpiderService.SpiderBinder binder = null;

    @ViewById(R.id.scheduleSubmitButton)
    Button button;

    @ViewById(R.id.scheduleTaskStartTimeEditText)
    EditText scheduleTaskStartTimeEditText;

    @ViewById(R.id.scheduleFromEditText)
    EditText scheduleFromEditText;

    @ViewById(R.id.scheduleToEditText)
    EditText scheduleToEditText;

    @ViewById(R.id.scheduleIsRemoteSwitch)
    Switch scheduleIsRemoteSwitch;

    @ViewById(R.id.scheduleTimesEditText)
    EditText scheduleTimesEditText;

    @ViewById(R.id.scheduleToolbar)
    Toolbar toolbar;

    @AfterViews
    void init(){
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
//
//        getWindow().setNavigationBarColor(Color.TRANSPARENT);
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_48px);
        toolbar.setNavigationOnClickListener((l) -> {
            onBackPressed();
        });

        spiderService = new SpiderService();
        Intent intent = new Intent(this, SpiderService.class);
        startService(intent);
//        binder = spiderService.getSpiderBinder();
        bindService(intent, spiderConn, BIND_AUTO_CREATE);


        scheduleTaskStartTimeEditText.setText("现在");
        scheduleTaskStartTimeEditText.setEnabled(false);

        scheduleIsRemoteSwitch.setChecked(false);
        scheduleIsRemoteSwitch.setEnabled(false);

    }

    @Click(R.id.scheduleSubmitButton)
    void submit(){
        boolean status = isValidInput(scheduleFromEditText, scheduleToEditText, () -> {
            showDialog("时间填写错误", "请填写正确的时间", "好");
        });
        if(!status){
            return;
        }

        if(scheduleTimesEditText.getText().toString().isEmpty()){
            showDialog("存在空信息", "请填写所有信息", "好");
            return;
        }

        if(!isNumber(scheduleTimesEditText.getText().toString())){
            showDialog("次数信息填写错误", "请填写所有信息", "好");
            return;
        }

        int times = Integer.parseInt(scheduleTimesEditText.getText().toString());

        final String token = DataUtils.getSavedToken(this);
        if(token == null || token.isEmpty()){
            showDialog("Token为空", "请前往个人页填写Token", "好", (dialogInterface, i) -> {
                Intent intent = new Intent(this, MyFragment_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            return;
        }

        final String nickname = DataUtils.get(this, "nickname");
        if(nickname == null ||nickname.isEmpty()){
            showDialog("昵称为空", "请前往个人页填写昵称", "好", (dialogInterface, i) -> {
                Intent intent = new Intent(this, MyFragment_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
            return;
        }
        Utils.conveyToCorrectDate(scheduleFromEditText, scheduleToEditText, (startDate, endDate) -> {
            binder.addTask(token, startDate, endDate, times);
            finish();
        });
    }

    @TextChange(R.id.scheduleFromEditText)
    void onScheduleFromEditTextChange(){
        String startDate = scheduleFromEditText.getText().toString();
        if(!startDate.isEmpty() && isNumber(startDate)){
            scheduleToEditText.setText(String.valueOf(Integer.parseInt(startDate) + 1));
        }
    }

    @Override
    protected void onDestroy() {
        if(spiderService != null){
            if(spiderService.isRestricted()){
                unbindService(spiderConn);
            }
        }
        super.onDestroy();
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

    @Override
    protected void onStart() {
        super.onStart();
        spiderService = new SpiderService();
        Intent intent = new Intent(this, SpiderService.class);
        startService(intent);
//        binder = spiderService.getSpiderBinder();
        bindService(intent, spiderConn, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if(spiderService != null){
            unbindService(spiderConn);
        }
        super.onStop();
    }
}