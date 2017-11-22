package com.umarfadil.passsamplesdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.SpassFingerprint;

public class MainActivity extends AppCompatActivity implements MyPassCallback,View.OnClickListener {
    MyPass myPass;
    Button btnSetting,btnSecret;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myPass = new MyPass(this);
        myPass.setCallback(this);
        try {
            myPass.initialize();
        }catch (SsdkUnsupportedException e){
            showErrorDialog("SDK PASS",e.getMessage());
            return;
        }catch (UnsupportedOperationException e){
            showErrorDialog("OPERATION PASS",e.getMessage());
            return;
        }
        btnSecret = (Button)findViewById(R.id.btn_secret);
        btnSetting = (Button)findViewById(R.id.btn_setting);
        btnSecret.setOnClickListener(this);
        btnSetting.setOnClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.btn_secret:
                if (myPass.getActive()){
                    myPass.handleIdentifyWithDialog(true);
                }else{
                    showErrorDialog("Fingerprint Activated","You need to activate the fingerprint security..");
                }
                break;
            case R.id.btn_setting:
                Intent setting = new Intent(MainActivity.this,SettingPassActivity.class);
                MainActivity.this.startActivity(setting);
                break;
        }
    }
    void showErrorDialog(String title,String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
    @Override
    public void onFinishedIdentify(int eventStatus) {
        if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
            Intent secret = new Intent(MainActivity.this, SecretActivity.class);
            this.startActivity(secret);
        }
    }
    @Override
    public void onFinishedRegister(boolean register) {
    }
}