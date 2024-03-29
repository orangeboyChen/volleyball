package com.nowcent.volleyball.activity.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.activity.AboutActivity_;
import com.nowcent.volleyball.activity.GetTokenActivity;
import com.nowcent.volleyball.activity.GetTokenActivity_;
import com.nowcent.volleyball.activity.PasswordActivity_;
import com.nowcent.volleyball.utils.ActivityUtils;
import com.nowcent.volleyball.utils.DataUtils;
import com.nowcent.volleyball.utils.NetworkUtils;
import com.nowcent.volleyball.utils.Utils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;

import static com.nowcent.volleyball.utils.ActivityUtils.invalid;
import static com.nowcent.volleyball.utils.DataUtils.getSavedPassword;
import static com.nowcent.volleyball.utils.DataUtils.saveToken;
import static com.nowcent.volleyball.utils.NetworkUtils.checkVersion;

@EFragment(R.layout.fragment_my)
public class MyFragment extends Fragment {
    public MyFragment() {
        // Required empty public constructor
    }



    @AfterViews
    void init(){

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) nestedScrollView.getLayoutParams();
        layoutParams.height = getContext().getResources().getDisplayMetrics().heightPixels - (int)(( 320 ) * getContext().getResources().getDisplayMetrics().density);

        String token = DataUtils.getSavedToken(getContext());
        String nickname = DataUtils.get(getContext(), "nickname");

        myTokenEditText.setText(token == null ? "" : token);
        myNicknameEditText.setText(nickname == null ? "" : nickname);

        myTokenEditText.setMovementMethod(ScrollingMovementMethod.getInstance());

        String message = DataUtils.get(getContext(), "message");
        if(message != null && !message.isEmpty()){
            textView.setText(message);
        }
        else{
            textView.setText("无");
        }

        getRemoteData();
    }

    @Background
    void getRemoteData(){
        try {
            JSONObject remoteData = NetworkUtils.getRemoteData();
            checkVersion(getContext(), remoteData,
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
                                    getActivity().finish();

                                }));
                    }, () -> invalid(getActivity()));
            String message = remoteData.getString("message");
            String password = remoteData.getString("password");

            //不允许使用
            if(password == null || password.isEmpty()){
                invalid(getActivity());
            }

            //检查密钥
            String savedPassword = getSavedPassword(getContext());
            if(!password.equals(savedPassword)){
                Intent intent = new Intent(getActivity(), PasswordActivity_.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("password", password);
                startActivity(intent);
                getActivity().finish();
            }


            getActivity().runOnUiThread(() -> {
                DataUtils.save(getContext(), "message", message);
                textView.setText(message == null || message.isEmpty() ? "" : message);
            });








        } catch (IOException | PackageManager.NameNotFoundException e) {
            invalid(getActivity());
        }

    }

    @ViewById(R.id.myMessageTextView)
    TextView textView;

    @ViewById(R.id.myNicknameEditText)
    EditText myNicknameEditText;

    @ViewById(R.id.myTokenEditText)
    EditText myTokenEditText;

    @ViewById(R.id.myAboutButton)
    Button myAboutButton;

    @ViewById(R.id.myScrollView)
    NestedScrollView nestedScrollView;

    @ViewById(R.id.myGetTokenButton)
    Button myGetTokenButton;

    @TextChange(R.id.myTokenEditText)
    void onTokenFocusChange(){
        DataUtils.saveToken(getContext(), myTokenEditText.getText().toString());
    }

    @TextChange(R.id.myNicknameEditText)
    void onMyNicknameEditTextFocusChange(){
        DataUtils.save(getContext(), "nickname", myNicknameEditText.getText().toString());
    }

    @Click(R.id.myAboutButton)
    void onMyAboutButtonClick(){
        Intent intent = new Intent(getContext(), AboutActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Click(R.id.myGetTokenButton)
    void onMyGetTokenButtonClick(){
        Intent intent = new Intent(getContext(), GetTokenActivity_.class);
        startActivityForResult(intent, 667);
    }


//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
////        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//
////        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
//
//
////        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
////        getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
////        getActivity().getWindow().getDecorView().setSystemUiVisibility(
////                View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
////                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
////                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
////        );
//
////        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
////        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
////        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
////        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
////        getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
////        getActivity().getWindow().getDecorView().setSystemUiVisibility(
////                View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
////                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
////                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
////        );
////        getActivity().getWindow().setStatusBarColor(this.getResources().getColor(R.color.ic_launcher_background));
////        getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//
//
//
//
//        super.onCreate(savedInstanceState);
//    }

    //    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//

    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );


        if(rootView == null){
            return inflater.inflate(R.layout.fragment_my, container, false);
        }
        else{
            ViewGroup viewGroup = (ViewGroup) rootView.getParent();
            if(viewGroup != null){
                viewGroup.removeView(rootView);
            }
//            if(viewGroup != null){
//                rootView = null;
//                rootView = inflater.inflate(R.layout.fragment_spider, container, false);
//            }
        }
        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 666 && requestCode == 667){
            String token = data.getStringExtra("token");
            if(token != null && !token.isEmpty()){
                myTokenEditText.setText(token);
                showDialog("获取Token成功", null, "好");
                saveToken(getContext(), token);
            }
            else{
                showDialog("获取Token失败", null, "好");
            }
        }
    }


    @UiThread
    void showDialog(String title, String text, String positiveButtonText){
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, (dialogInterface, i) -> {})
                .setCancelable(false)
                .show();

    }


    @UiThread
    void showDialog(String title, String text, String positiveButtonText, DialogInterface.OnClickListener positiveListener,
                    String negativeButtonText, DialogInterface.OnClickListener negativeListener){
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, positiveListener)
                .setNegativeButton(negativeButtonText, negativeListener)
                .setCancelable(false)
                .show();

    }


    @UiThread
    void showDialog(String title, String text, String positiveButtonText, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, listener)
                .setCancelable(false)
                .show();

    }

}