package com.nowcent.volleyball.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.nowcent.volleyball.R;
import com.nowcent.volleyball.utils.ActivityUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import static com.nowcent.volleyball.utils.DataUtils.savePassword;

@EActivity(R.layout.activity_password)
public class PasswordActivity extends AppCompatActivity {

    @ViewById(R.id.passwordToolbar)
    Toolbar toolbar;

    @ViewById(R.id.passwordSubmitButton)
    Button button;

    @ViewById(R.id.passwordInputEditText)
    EditText editText;

    String password;
    String data;

    @AfterViews
    void init(){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));

        Intent intent = getIntent();
        password = intent.getStringExtra("password");
        data = intent.getStringExtra("data");

        if(password == null){
            ActivityUtils.invalid(this);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Click(R.id.passwordSubmitButton)
    void onPasswordSubmitButtonClick(){
        if(password.contentEquals(editText.getText())){
            savePassword(this, password);
            toMainActivity();
        }
        else{
            ActivityUtils.invalid(this, "密钥不正确");
        }
    }

    void toMainActivity(){
        Intent intent = new Intent(this, MainActivity_.class);
        intent.putExtra("password", password);
        intent.putExtra("data", data);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}