package com.umarfadil.passsamplesdk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.samsung.android.sdk.SsdkUnsupportedException;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingPassActivity extends AppCompatActivity implements MyPassCallback, View.OnClickListener,CompoundButton.OnCheckedChangeListener{
    MyPass myPass;
    Switch switchActive;
    Button btnIdWithUi, btnIdWithoutUi, btnRegister , btnIntended;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_pass);
        myPass = new MyPass(this);
        myPass.setCallback(this);
        try{
            myPass.initialize();
            initButton();
            if (myPass.getActive()){
                switchActive.setChecked(true);
                setEnableButton();
            }
        }catch (SsdkUnsupportedException e){
            showErrorDialog("SDK PASS",e.getMessage());
            return;
        }catch (UnsupportedOperationException e){
            showErrorDialog("OPERATION PASS",e.getMessage());
            return;
        }
    }
    void initButton(){
        switchActive = (Switch)findViewById(R.id.switch_active);
        switchActive.setOnCheckedChangeListener(this);
        btnIdWithUi = (Button)findViewById(R.id.btn_identify_with_ui);
        btnIdWithUi.setOnClickListener(this);
        btnIdWithoutUi = (Button)findViewById(R.id.btn_identify_without_ui);
        btnIdWithoutUi.setOnClickListener(this);
        btnRegister = (Button)findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(this);
        btnIntended = (Button)findViewById(R.id.btn_show_intended_finger);
        btnIntended.setOnClickListener(this);
    }
    void setEnableButton(){
        if (myPass.hasRegisteredFinger) {
            btnIdWithUi.setEnabled(true);
            btnIdWithoutUi.setEnabled(true);
            btnRegister.setEnabled(true);
            btnIntended.setEnabled(true);
        }
    }
    void setDisableButton(){
        btnIdWithUi.setEnabled(false);
        btnIdWithoutUi.setEnabled(false);
        btnRegister.setEnabled(false);
        btnIntended.setEnabled(false);
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
        int id = compoundButton.getId();
        switch (id){
            case R.id.switch_active:
                if(enabled){
                    if (myPass.hasRegisteredFinger){
                        myPass.setActive(true);
                        setEnableButton();
                        Log.d(TAG, "onCheckedChanged: switch already registered");
                    }else {
                        //Change to handler
                        myPass.handleRegisterFinger();
                    }
                }else{
                    myPass.setActive(false);
                    setDisableButton();
                }
                break;
            default:
                break;
        }
    }
    private String TAG = "Setting Pass";
    @Override
    public void onFinishedIdentify(int eventStatus) {
        Log.d(TAG, "onFinishedIdentify: "+eventStatus);
        Log.d(TAG, "onFinishedIdentify: "+MyPass.getEventStatusName(eventStatus));
    }
    @Override
    public void onFinishedRegister(boolean register) {
        if(!register) {
            switchActive.setChecked(false);
            myPass.setActive(false);
            setDisableButton();
        }else{
            myPass.setActive(true);
            setEnableButton();
        }
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id){
            case R.id.btn_identify_with_ui:
                //Change to handler
                myPass.handleIdentifyWithDialog(false);
                break;
            case R.id.btn_identify_without_ui:
                setDisableButton();
                //Change to handler
                myPass.handleIdentifyWithoutDialog();
                break;
            case R.id.btn_register:
                //Change to handler
                myPass.handleRegisterFinger();
                break;
            case R.id.btn_show_intended_finger:
                //Change to handler
                showDialogIndex();
                break;
        }
    }
    ArrayList selectedItems = new ArrayList();
    void showDialogIndex(){
        CharSequence[] fingerItems = myPass.getFingerprintName();
        Integer[] designatedIndex = myPass.getDesignatedIndex();
        boolean[] fingerChecked = getCheckedIndex(fingerItems,designatedIndex);
        selectedItems = new ArrayList<>(Arrays.asList(designatedIndex));
        AlertDialog dialogIndex = new AlertDialog.Builder(SettingPassActivity.this)
                .setTitle("Select designated finger.")
                .setMultiChoiceItems(fingerItems, fingerChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            selectedItems.add(indexSelected);
                        } else if (selectedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            selectedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on OK
                        //  You can write the code  to save the selected item here

                        Log.d(TAG, "onClick: "+ selectedItems.toString());
                        if (selectedItems.size() > 0) {
                            Integer[] selectedArray = (Integer[]) selectedItems.toArray(new Integer[selectedItems.size()]);
                            myPass.setDesignatedIndex(selectedArray);
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                    }
                }).create();
        dialogIndex.show();
    }
    boolean[] getCheckedIndex(CharSequence[] input, Integer[] designated){
        boolean[] checkedIndex = new boolean[input.length];
        Arrays.fill(checkedIndex,false);
        for (int i = 0  ; i < designated.length ; i++){
            if (designated[i] < input.length){
                checkedIndex[designated[i]] = true;
            }
        }
        return checkedIndex;
    }
    void showErrorDialog(String title,String message){
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        SettingPassActivity.this.finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}