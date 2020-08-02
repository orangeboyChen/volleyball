package com.nowcent.volleyball.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alibaba.fastjson.JSON;
import com.nowcent.volleyball.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_privacy)
public class PrivacyActivity extends AppCompatActivity {

    @ViewById(R.id.privacyToolbar)
    Toolbar toolbar;

    @ViewById(R.id.privacyWebView)
    WebView webView;

    @AfterViews
    void init(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
//        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        Resources resources = getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        int height = resources.getDimensionPixelSize(resourceId);

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) toolbar.getLayoutParams();
        layoutParams.setMargins(0, height, 0, 0);

        ConstraintLayout.LayoutParams layoutParam2 = (ConstraintLayout.LayoutParams) webView.getLayoutParams();

        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        toolbar.measure(w, h);
        int toolbarHeight = toolbar.getMeasuredHeight();

        layoutParam2.setMargins(0, height + toolbarHeight, 0, 0);


        Log.e("height", String.valueOf(toolbarHeight));



        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_48px);
        toolbar.setNavigationOnClickListener((l) -> {
            onBackPressed();
        });


        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://volleyball.nowcent.cn/privacy.html");
        webView.getSettings().setDefaultTextEncodingName("UTF-8");

    }
}