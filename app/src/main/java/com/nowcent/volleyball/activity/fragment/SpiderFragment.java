package com.nowcent.volleyball.activity.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.nowcent.volleyball.R;
import com.nowcent.volleyball.activity.MainActivity;
import com.nowcent.volleyball.activity.PasswordActivity_;
import com.nowcent.volleyball.activity.ScheduleActivity;
//import com.nowcent.volleyball.activity.ScheduleActivity_;
import com.nowcent.volleyball.activity.ScheduleActivity_;
import com.nowcent.volleyball.activity.adapter.CardAdapter;
import com.nowcent.volleyball.pojo.BookPojo;
import com.nowcent.volleyball.pojo.ResultMessage;
import com.nowcent.volleyball.service.SpiderService;
import com.nowcent.volleyball.utils.ActivityUtils;
import com.nowcent.volleyball.utils.DataUtils;
import com.nowcent.volleyball.utils.NetworkUtils;
import com.nowcent.volleyball.utils.Utils;
import com.tencent.android.tpush.XGPushConfig;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.nowcent.volleyball.utils.ActivityUtils.invalid;
import static com.nowcent.volleyball.utils.DataUtils.get;
import static com.nowcent.volleyball.utils.DataUtils.getCurrent;
import static com.nowcent.volleyball.utils.DataUtils.getSavedPassword;
import static com.nowcent.volleyball.utils.DataUtils.getSavedToken;
import static com.nowcent.volleyball.utils.DataUtils.save;
import static com.nowcent.volleyball.utils.DataUtils.saveCurrent;
import static com.nowcent.volleyball.utils.DataUtils.saveToken;
import static com.nowcent.volleyball.utils.LocationUtils.getPosition;
import static com.nowcent.volleyball.utils.NetworkUtils.checkVersion;
import static com.nowcent.volleyball.utils.Utils.getTimeString;
import static com.nowcent.volleyball.utils.Utils.isNumber;

@EFragment(value = R.layout.fragment_spider)
public class SpiderFragment extends Fragment {

    private int MIN_HOUR = 9;
    private int MAX_HOUR = 22;

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             @Nullable ViewGroup container,
//                             @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_spider, container, false);
//    }

    @ViewById(R.id.nestedScrollView)
    NestedScrollView nestedSCrollView;

    @ViewById(R.id.spiderPositionAndWeatherTextView)
    TextView spiderPositionAndWeatherTextView;

    @ViewById(R.id.spiderWeatherDetailTextView)
    TextView spiderWeatherDetailTextView;

    @ViewById(R.id.spiderFromEditText)
    EditText spiderFromEditText;

    @ViewById(R.id.spiderToEditText)
    EditText spiderToEditText;

    @ViewById(R.id.spiderSingleSubmitButton)
    Button spiderSubmitButton;

    @ViewById(R.id.spiderPlanButton)
    Button spiderPlanButton;

    @ViewById(R.id.spiderDoButton)
    Button spiderDoButton;

    @ViewById(R.id.spiderStatusTextView)
    TextView spiderStatusTextView;

    @ViewById(R.id.spiderTipTextView)
    TextView spiderTipTextView;

    @ViewById(R.id.spiderStatusInfoTextView)
    TextView spiderStatusInfoTextView;

    @ViewById(R.id.spiderInfoTextView)
    TextView spiderInfoTextView;

    @ViewById(R.id.spiderProgressBar)
    ProgressBar progressBar;

    @ViewById(R.id.cardView2)
    CardView singleCareView;

    @ViewById(R.id.recyclerView)
    RecyclerView recyclerView;

    @ViewById(R.id.spiderRecentTipTextView)
    TextView spiderRecentTipTextView;

    @ViewById(R.id.spiderRecentTitleTextView)
    TextView spiderRecentTitleTextView;

    CardAdapter adapter;

    private boolean isFirstUpdateRecentData = true;



    SpiderService spiderService = null;
    SpiderService.SpiderBinder binder = null;
    List<BookPojo> bookList = null;

    private ServiceConnection spiderConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            binder = (SpiderService.SpiderBinder)iBinder;
            spiderService = binder.getService();

            spiderService.setOnDataCallback(new SpiderService.OnDataCallback() {
                @Override
                public void onGetTeamIdError() {
                    changeTaskInfo("失败", "获取TeamID失败！请检查Token是否正确，或者您未通过实名认证。请关注微信公众号“罗湖文体通”，打开相应的页面进行实名认证！", Color.parseColor("#D50000"));
                    progressBar.setProgressDrawable(getContext().getResources().getDrawable(R.drawable.progressbar_red));
                    progressBar.setProgress(100);
                }

                @Override
                public void onGetTeamIdSuccess(int teamId) {

                }

                public void onTask(){
                    changeTaskInfo("抢场中", null, Color.parseColor("#0091EA"));

                    Log.e("onTask", "onTask");
                    progressBar.setProgressDrawable(getContext().getResources().getDrawable(R.drawable.progressbar_orange));
                }

                @Override
                public void onTaskSuccess(Date startDate, Date endDate) {
                    onTask();
                }

                @Override
                public void onTaskFail(String message) {

                    Log.e("onTask", "onfail");

                    onTask();

                }

                @Override
                public void onRecentResult(ResultMessage recentResult) {
                    Log.e("onTask", "recent");
                    save(getContext(), "recentMessage", JSON.toJSONString(recentResult));
                    getActivity().runOnUiThread(() -> {
                        spiderStatusInfoTextView.setText(getTimeString(recentResult.getTime()) + " " + recentResult.getMessage());
                    });
                }

                @Override
                public void onCountDataChange(int current, int total) {
                    progressBar.setProgress((int)(current / (float)total * 100));
                }

                @Override
                public void success(Date startDate, Date endDate) {
                    Log.e("onTask", "success");
                    saveCurrent(getContext(), "cardTitle", "成功");
                    String token = XGPushConfig.getToken(getContext());
                    String nickname = DataUtils.get(getContext(), "nickname");
                    try {
                        JSONObject jsonObject = NetworkUtils.broadCastSuccess(token, String.valueOf(startDate.getHours()), String.valueOf(endDate.getHours()), nickname);
                        Log.e("sujs", jsonObject.toJSONString());
                    } catch (IOException e) {
                        e.printStackTrace();
                        ActivityUtils.invalid(getActivity());
                    }
                    getSuccess();
                }

                @Override
                public void fail() {
                    Log.e("onTask", "fail");
                    String current = getCurrent(getContext(), "cardTitle");
                    if("成功".equals(current)){
                        changeTaskInfo("本次失败但曾成功", null, Color.parseColor("#0091EA"));
                        return;
                    }

                    saveCurrent(getContext(), "cardTitle", "失败");
                    getFail();
                }


            });

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private void getSuccess(){
        changeTaskInfo("成功", null, Color.parseColor("#00C853"));
        progressBar.setProgressDrawable(getContext().getResources().getDrawable(R.drawable.progressbar_green));
        progressBar.setProgress(100);
    }

    private void getFail(){
        changeTaskInfo("失败", null, Color.parseColor("#D50000"));
        progressBar.setProgressDrawable(getContext().getResources().getDrawable(R.drawable.progressbar_red));
        progressBar.setProgress(100);
    }

    @UiThread
    void toast(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();

    }

    @UiThread
    void changeTaskInfo(String title, String tip, int color){
        spiderStatusTextView.setTextColor(color);
        spiderStatusTextView.setText(title);

        spiderTipTextView.setText(tip);
        spiderTipTextView.setVisibility(tip == null ? View.GONE : View.VISIBLE);
    }




    @AfterViews
    void init(){
        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) nestedSCrollView.getLayoutParams();
        layoutParams.height = getContext().getResources().getDisplayMetrics().heightPixels - (int)(( 160 ) * getContext().getResources().getDisplayMetrics().density);

        singleCareView.setVisibility(View.GONE);
        spiderTipTextView.setVisibility(View.GONE);

        initRecentCardList();
        getRecentData();
        initCard();
        checkRemoteData();

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setFocusableInTouchMode(false);

//        Intent activityIntent = getActivity().getIntent();
//        JSONObject jsonObject = JSON.parseObject(activityIntent.getStringExtra("data"));
//        spiderInfoTextView.setText(jsonObject.getString("message"));


//        Log.e("heights", String.valueOf(layoutParams.height));

        String weatherAndPosition = get(getContext(), "spiderPositionAndWeatherTextView");
        String weatherInfo = get(getContext(), "spiderWeatherDetailTextView");

        spiderPositionAndWeatherTextView.setText(weatherAndPosition == null ? "正在定位..." : weatherAndPosition);
        spiderWeatherDetailTextView.setText(weatherInfo == null ? "" : weatherInfo);


        spiderService = new SpiderService();
        Intent intent = new Intent(getContext(), SpiderService.class);
        getContext().startService(intent);
        boolean b = getContext().bindService(intent, spiderConn, BIND_AUTO_CREATE);



        getPosition(getContext(), (amapLocation) -> {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    Log.e("posissss", amapLocation.getAddress());

                    //检索参数为城市和天气类型，实况天气为WEATHER_TYPE_LIVE、天气预报为WEATHER_TYPE_FORECAST
                    WeatherSearchQuery mquery = new WeatherSearchQuery(amapLocation.getCity(), WeatherSearchQuery.WEATHER_TYPE_LIVE);
//                    WeatherSearchQuery mquery = new WeatherSearchQuery(amapLocation.getCity(), WeatherSearchQuery.WEATHER_TYPE_FORECAST);
                    WeatherSearch mweathersearch = new WeatherSearch(getContext());
                    mweathersearch.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
                        @Override
                        public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
                            if(i == 1000){
                                Log.e("ssss", JSON.toJSONString(localWeatherLiveResult));
                                LocalWeatherLive live = localWeatherLiveResult.getLiveResult();

                                //展示天气
                                spiderPositionAndWeatherTextView.setText(live.getCity() + "，" + live.getWeather());
                                spiderWeatherDetailTextView.setText("温度：" + live.getTemperature() + "°C  湿度：" + live.getHumidity() + "%");

                                //存进暂存区
                                save(getContext(), "spiderPositionAndWeatherTextView", spiderPositionAndWeatherTextView.getText().toString());
                                save(getContext(), "spiderWeatherDetailTextView", spiderWeatherDetailTextView.getText().toString());
                            }
                        }

                        @Override
                        public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {
                            Log.e("tttt", JSON.toJSONString(localWeatherForecastResult));
                        }
                    });
                    mweathersearch.setQuery(mquery);
                    mweathersearch.searchWeatherAsyn();
                }
            }

        });

    }


    private View rootView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        int flag =  getActivity().getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getActivity().getWindow().getDecorView().setSystemUiVisibility(flag);


        if(rootView == null){
            rootView = inflater.inflate(R.layout.fragment_spider, container, false);
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


//        return super.onCreateView(inflater, container, savedInstanceState);
    }

//    @Click(R.id.spiderImage)
//    void click(){
//        binder.setMessage("12233");
//        Log.e("binderM", binder.getService().getMessage());
//    }

    private void fadeIn(View view){
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void fadeIn(View view, Runnable runnable){
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        runnable.run();
                    }
                });
    }

    private void fadeOut(View view){
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(0f)
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });
    }

    private void fadeOut(View view, Runnable runnable){
        view.setAlpha(1f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(0f)
                .setDuration(100)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        runnable.run();
                    }
                });
    }

    @Click(R.id.spiderDoButton)
    void showSingleCareView(){
        if(singleCareView.getVisibility() == View.VISIBLE){
            fadeOut(singleCareView);
            if(bookList != null && bookList.size() > 0) {
                fadeOut(spiderRecentTitleTextView, () -> {
                    fadeIn(spiderRecentTitleTextView);
                });
            }

            fadeOut(recyclerView, () -> {
                fadeIn(recyclerView);
            });
        }
        else{
            if(bookList != null && bookList.size() > 0) {
                fadeOut(spiderRecentTitleTextView, () -> {
                    fadeIn(spiderRecentTitleTextView);
                });
            }
            fadeOut(recyclerView, () -> {
                fadeIn(recyclerView);
                fadeIn(singleCareView);
            });


        }
    }

    @Click(R.id.spiderSingleSubmitButton)
    void onSingleSubmitClick(){
        if(!Utils.isValidInput(spiderFromEditText, spiderToEditText, () -> {
            showDialog("时间填写错误", "请填写正确的时间", "好");
        })){
            return;
        }
//        saveToken(this, tokenEditText.getText().toString());

//        final String token = tokenEditText.getText().toString();

        final String token = DataUtils.getSavedToken(getContext());
        if(token == null || token.isEmpty()){
            showDialog("Token为空", "请前往个人页填写Token", "好", (dialogInterface, i) -> {
                ((MainActivity)getActivity()).getNavController().navigate(R.id.myFragment);
                ((MainActivity)getActivity()).getNavView().setSelectedItemId(R.id.myFragment);
            });
            return;
        }

        final String nickname = DataUtils.get(getContext(), "nickname");
        if(nickname == null || nickname.isEmpty()){
            showDialog("昵称为空", "请前往个人页填写昵称", "好", (dialogInterface, i) -> {
                ((MainActivity)getActivity()).getNavController().navigate(R.id.myFragment);
                ((MainActivity)getActivity()).getNavView().setSelectedItemId(R.id.myFragment);
            });
            return;
        }

        Utils.conveyToCorrectDate(spiderFromEditText, spiderToEditText, (startDate, endDate) -> {
            binder.addTask(token, startDate, endDate);
        });
//        spider(token, startDate, endDate);

    }

    @Click(R.id.spiderPlanButton)
    void onPlanButtonClick(){
        Intent intent = new Intent(getContext(), ScheduleActivity_.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @TextChange(R.id.spiderFromEditText)
    void onFromEditTextChange(){
        String startDate = spiderFromEditText.getText().toString();
        if(!startDate.isEmpty() && isNumber(startDate)){
            spiderToEditText.setText(String.valueOf(Integer.parseInt(startDate) + 1));
        }
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
    void showDialog(String title, String text, String positiveButtonText){
        new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(positiveButtonText, (dialogInterface, i) -> {})
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


    private boolean isValidInput(){
        //检查文本框信息是否为空
//        if(tokenEditText.getText().toString().isEmpty() ||
//                startDateEditText.getText().toString().isEmpty() ||
//                endDateEditText.getText().toString().isEmpty()){
//            showDialog("有未填写的信息", "请填写所有信息", "好");
//            return false;
//        }

        //检查文本框的信息是否正确（时间）
        if(!isNumber(spiderFromEditText.getText().toString()) || !isNumber(spiderToEditText.getText().toString())){
            showDialog("时间填写错误", "请填写正确的时间", "好");
            return false;
        }

        int startHour = Integer.parseInt(spiderFromEditText.getText().toString());
        int endHour = Integer.parseInt(spiderToEditText.getText().toString());

        if(startHour < MIN_HOUR ||
                startHour > MAX_HOUR ||
                endHour < MIN_HOUR ||
                endHour > MAX_HOUR ||
                endHour - startHour != 1){
            showDialog("时间填写错误", "请填写正确的时间", "好");
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        getActivity().getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE|
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        int flag =  getActivity().getWindow().getDecorView().getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        getActivity().getWindow().getDecorView().setSystemUiVisibility(flag);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        spiderService = new SpiderService();
        Intent intent = new Intent(getContext(), SpiderService.class);
        getContext().startService(intent);
//        binder = spiderService.getSpiderBinder();
        getContext().bindService(intent, spiderConn, BIND_AUTO_CREATE);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onPause() {
//        if(spiderService != null){
//            getContext().unbindService(spiderConn);
//        }
        super.onPause();
    }

    @Override
    public void onStop() {
        getContext().unbindService(spiderConn);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        if(spiderService != null){
            if(spiderService.isRestricted()){
                getContext().unbindService(spiderConn);
            }
        }
        super.onDestroy();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initCard(){
        String cardTitle = getCurrent(getContext(), "cardTitle");
        String recentMessage = get(getContext(), "recentMessage");

        if(cardTitle == null){
            spiderStatusTextView.setText("今天未订场");
        }
        else{
            spiderStatusTextView.setText(cardTitle);
            if ("成功".equals(cardTitle)) {
                getSuccess();
            } else {
                getFail();
            }
        }

        if(recentMessage == null){
            spiderStatusInfoTextView.setText("无");
        }
        else{
            ResultMessage recentResult = JSON.parseObject(recentMessage, ResultMessage.class);
            spiderStatusInfoTextView.setText(getTimeString(recentResult.getTime()) + " " + recentResult.getMessage());
        }
    }


    @Background
    void getRecentData(){
        try {
            JSONObject jsonObject = NetworkUtils.getRecentList();
            if(jsonObject.getIntValue("code") == 200){
                List<BookPojo> list = jsonObject.getJSONArray("data").toJavaList(BookPojo.class);
                if(list == null || list.size() == 0){
                    getActivity().runOnUiThread(() -> {
                        spiderRecentTitleTextView.setVisibility(View.GONE);
                    });
                }
                else{
                    getActivity().runOnUiThread(() -> {
                        spiderRecentTitleTextView.setVisibility(View.VISIBLE);
                    });

                    getActivity().runOnUiThread(() -> {
                        if(isFirstUpdateRecentData){
                            bookList.clear();
                            bookList.addAll(list);
                        }
                        adapter.notifyDataSetChanged();
                    });
                }
            }
            else{
                showDialog("获取订场信息失败", jsonObject.getString("message") == null || jsonObject.getString("message").isEmpty() ? null : "错误信息：" + jsonObject.getString("message"), "好");
                spiderRecentTipTextView.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ActivityUtils.invalid(getActivity());
        }
    }


    private void initRecentCardList(){
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        bookList = new ArrayList<>();
        adapter = new CardAdapter(getContext(), bookList, this::join);
        recyclerView.setAdapter(adapter);

    }

    @Background
    void join(int i){
        String nickname = get(getContext(), "nickname");
        if(nickname == null || nickname.isEmpty()){
            showDialog("昵称为空", "请至个人页面输入昵称后再进行约球", "好", (dialogInterface, j) -> {
                ((MainActivity)getActivity()).getNavController().navigate(R.id.myFragment);
                ((MainActivity)getActivity()).getNavView().setSelectedItemId(R.id.myFragment);
            }, "下次再说", (dialogInterface, j) -> {});
            return;
        }

        BookPojo bookPojo = bookList.get(i);

        String fromToken = XGPushConfig.getToken(getContext());
        String toToken = bookPojo.getToken();


        try {
            JSONObject result = NetworkUtils.join(fromToken, toToken, nickname);
            if(result.getIntValue("code") == 200){
                showDialog("约球成功", "对方已经收到了你的约球请求。", "好");
            }
            else{
                String message = result.getString("message");
                showDialog("约球失败",
                        message == null || message.isEmpty() ? null : "错误信息：" + message, "好");
            }
        } catch (IOException e) {
            e.printStackTrace();
            ActivityUtils.invalid(getActivity());
        }

    }

    @Background
    void checkRemoteData(){
        try {
            JSONObject result = NetworkUtils.getRemoteData();

            if(result == null || result.isEmpty()){
                result = NetworkUtils.getRemoteData();
            }

            checkVersion(getContext(), result,
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
            String message = result.getString("message");
            String password = result.getString("password");

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


        } catch (IOException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            ActivityUtils.invalid(getActivity());
        }
    }
}