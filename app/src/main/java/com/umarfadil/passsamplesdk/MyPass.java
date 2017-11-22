package com.umarfadil.passsamplesdk;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;
import com.samsung.android.sdk.pass.SpassInvalidStateException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by umarfadil on 8/17/17.
 */

public class MyPass implements Handler.Callback{
    private SpassFingerprint mSpassFingerprint;
    private Spass mSpass;
    private boolean isFeatureEnabled;
    private String TAG = "Test Pass";
    private Context context;
    private MyPassCallback myPassCallback;
    private PassPreferences passPrefs;
    public boolean isFeatureEnabled_index = false;
    public boolean isFeatureEnabled_custom = false;
    public boolean isFeatureEnabled_uniqueId = false;
    public boolean isFeatureEnabled_backupPw = false;
    public boolean hasRegisteredFinger = false;
    private Handler mHandler;
    public MyPass(Context context){
        mSpass = new Spass();
        this.context = context;
        passPrefs = new PassPreferences(context);
        mHandler = new Handler(this);
    }
    public void setCallback(MyPassCallback myPassCallback){
        this.myPassCallback = myPassCallback;
    }
    public boolean initialize() throws SsdkUnsupportedException,UnsupportedOperationException{
        mSpass.initialize(context);
        isFeatureEnabled = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        if(isFeatureEnabled){
            mSpassFingerprint = new SpassFingerprint(context);
            checkAvalailableFeatures();
            checkRegistered();
        } else {
            return false;
        }
        return true;
    }
    private void checkAvalailableFeatures(){
        isFeatureEnabled_index = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_FINGER_INDEX);
        isFeatureEnabled_custom = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_CUSTOMIZED_DIALOG);
        isFeatureEnabled_uniqueId = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_UNIQUE_ID);
        isFeatureEnabled_backupPw = mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_AVAILABLE_PASSWORD);
    }
    //Menyimpan kondisi aktif security di dalam shared preferences
    public void setActive(boolean active){
        passPrefs.setActive(active);
    }
    public boolean getActive(){
        return passPrefs.getActive();
    }
    //Menyimpan data designated index di dalam shared preferences
    public void setDesignatedIndex(Integer[] input){
        passPrefs.setDesignated(input);
    }
    public Integer[] getDesignatedIndex(){
        return passPrefs.getDesignated();
    }
    public void identifyByIndex(){
        Integer[] designatedArray = getDesignatedIndex();
        //Karena intendedFingerPrint dimulai dari index ke 1 maka perlu di sesuaikan pada default index 0
        for(int i = 0 ; i < designatedArray.length ; i++){
            designatedArray[i] = designatedArray[i] + 1;
        }
        ArrayList<Integer> newDesignatedArray = new ArrayList<>(Arrays.asList(designatedArray));
        mSpassFingerprint.setIntendedFingerprintIndex(newDesignatedArray);
    }
    //Check apakah sudah ada data yang diregistrasi
    public void checkRegistered() throws UnsupportedOperationException{
        hasRegisteredFinger = mSpassFingerprint.hasRegisteredFinger();
    }
    //HANDLER REGISTER
    public void handleRegisterFinger(){
        mHandler.sendEmptyMessage(MSG_REGISTER);
    }
    //REGISTER FINGER 1003
    private void registerFinger(){
        mSpassFingerprint.registerFinger(context,mRegisterListener);
    }
    public CharSequence[] getFingerprintName() {
        SparseArray<String> fingerSparse= null;
        List<String> fingerList = new ArrayList<>();
        fingerSparse = mSpassFingerprint.getRegisteredFingerprintName();
        if (fingerSparse == null) {
            Log.d(TAG, "getFingerprintName: ZERO SIZE");
            return fingerList.toArray(new String[fingerList.size()]);
        } else {
            Log.d(TAG, "getFingerprintName: "+fingerSparse.size());
            for (int i = 0; i < fingerSparse.size(); i++) {
                int index = fingerSparse.keyAt(i);
                String name = fingerSparse.get(index);
                fingerList.add(name);
            }
            return fingerList.toArray(new String[fingerList.size()]);
        }
    }
    private boolean onReadyIdentify = false;
    private boolean needRetryIdentify = false;
    //HANDLER IDENTIFY FINGER
    public void handleIdentifyWithDialog(boolean designated){
        Message msg = new Message();
        msg.what = MSG_AUTH_DIALOG;
        Bundle bundle = new Bundle();
        bundle.putBoolean(MSG_BUNDLE,designated);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    //IDENTIFY FINGER 1001
    private void identifyFingerWithDialog(boolean designated){
        if (onReadyIdentify == false) {
            onReadyIdentify = true;
            try {
                if (mSpassFingerprint != null) {
//                    setIdentifyIndexDialog();

                    //Jika designated true maka request menyesuaikan dengan index yang diinginkan
                    if(designated) {
                        identifyByIndex();
                    }
                    mSpassFingerprint.startIdentifyWithDialog(context, mIdentifyListenerDialog, false);
                }
                //if (designatedFingersDialog != null) {
                //  log("Please identify finger to verify you with " + designatedFingersDialog.toString() + " finger");
                //} else {
                //  log("Please identify finger to verify you");
                //}
            } catch (IllegalStateException e) {
                onReadyIdentify = false;
                //resetIdentifyIndexDialog();
                //log("Exception: " + e);
            }
        } else {
            //log("The previous request is remained. Please finished or cancel first");
        }
    }
    //HANDLER IDENTIFY FINGER WITHOUT DIALOG
    public void handleIdentifyWithoutDialog(){
        mHandler.sendEmptyMessage(MSG_AUTH_WITHOUT_DIALOG);
    }
    //IDENTIFY FINGER WITHOUT DIALOG 1002
    private void identifyFingerWithoutDialog() {
        Log.d(TAG, "startIdentify: "+onReadyIdentify);
        if (onReadyIdentify == false) {
            try {
                onReadyIdentify = true;
                if (mSpassFingerprint != null) {
                    //setIdentifyIndex();
                    mSpassFingerprint.startIdentify(mIdentifyListener);
                }
               /*
               if (designatedFingers != null) {
                   log("Please identify finger to verify you with " + designatedFingers.toString() + " finger");
               } else {
                   log("Please identify finger to verify you");
               }
               */
            } catch (SpassInvalidStateException ise) {
                onReadyIdentify = false;
                //resetIdentifyIndex();
                if (ise.getType() == SpassInvalidStateException.STATUS_OPERATION_DENIED) {
                    //log("Exception: " + ise.getMessage());
                    Log.d(TAG, "startIdentify: "+ ise.getMessage());
                }
            } catch (IllegalStateException e) {
                onReadyIdentify = false;
                //resetIdentifyIndex();
                //log("Exception: " + e);
                Log.d(TAG, "startIdentify: "+e.getMessage());
            }
        } else {
            //log("The previous request is remained. Please finished or cancel first");
        }
    }
    private SpassFingerprint.RegisterListener mRegisterListener = new SpassFingerprint.RegisterListener() {
        @Override
        public void onFinished() {
            checkRegistered();
            myPassCallback.onFinishedRegister(hasRegisteredFinger);
        }
    };
    private SpassFingerprint.IdentifyListener mIdentifyListenerDialog = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            myPassCallback.onFinishedIdentify(eventStatus);
            //log("identify finished : reason =" + getEventStatusName(eventStatus));
            int FingerprintIndex = 0;
            boolean isFailedIdentify = false;
            onReadyIdentify = false;
            try {
                FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
                Log.d(TAG, "onFinished: "+FingerprintIndex);
            } catch (IllegalStateException ise) {
                //  log(ise.getMessage());
            }
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                //log("onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                //log("onFinished() : Password authentification Success");
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED
                    || eventStatus == SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE) {
                // log("onFinished() : User cancel this identify.");
            } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                //log("onFinished() : The time for identify is finished.");
            } else if (!mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT_AVAILABLE_PASSWORD)) {
                if (eventStatus == SpassFingerprint.STATUS_BUTTON_PRESSED) {
                    //  log("onFinished() : User pressed the own button");
                    //Toast.makeText(mContext, "Please connect own Backup Menu", Toast.LENGTH_SHORT).show();
                }
            } else {
                //log("onFinished() : Authentification Fail for identify");
                isFailedIdentify = true;
            }
            if (!isFailedIdentify) {
                //resetIdentifyIndexDialog();
            }
        }
        @Override
        public void onReady() {
            //log("identify state is ready");
        }
        @Override
        public void onStarted() {
            //log("User touched fingerprint sensor");
        }
        @Override
        public void onCompleted() {
            //log("the identify is completed");
        }
    };
    private SpassFingerprint.IdentifyListener mIdentifyListener = new SpassFingerprint.IdentifyListener() {
        @Override
        public void onFinished(int eventStatus) {
            //log("identify finished : reason =" + getEventStatusName(eventStatus));

            myPassCallback.onFinishedIdentify(eventStatus);
            int FingerprintIndex = 0;
            String FingerprintGuideText = null;
            try {
                FingerprintIndex = mSpassFingerprint.getIdentifiedFingerprintIndex();
            } catch (IllegalStateException ise) {
                //log(ise.getMessage());
            }
            if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS) {
                //log("onFinished() : Identify authentification Success with FingerprintIndex : " + FingerprintIndex);
            } else if (eventStatus == SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS) {
                //log("onFinished() : Password authentification Success");
            } else if (eventStatus == SpassFingerprint.STATUS_OPERATION_DENIED) {
                //log("onFinished() : Authentification is blocked because of fingerprint service internally.");
            } else if (eventStatus == SpassFingerprint.STATUS_USER_CANCELLED) {
                //log("onFinished() : User cancel this identify.");
            } else if (eventStatus == SpassFingerprint.STATUS_TIMEOUT_FAILED) {
                //log("onFinished() : The time for identify is finished.");
            } else if (eventStatus == SpassFingerprint.STATUS_QUALITY_FAILED) {
                //log("onFinished() : Authentification Fail for identify.");
                needRetryIdentify = true;
                FingerprintGuideText = mSpassFingerprint.getGuideForPoorQuality();
                Toast.makeText(context, FingerprintGuideText, Toast.LENGTH_SHORT).show();
            } else {
                //log("onFinished() : Authentification Fail for identify");
                needRetryIdentify = true;
            }
            if (!needRetryIdentify) {
                //resetIdentifyIndex();
            }
        }
        @Override
        public void onReady() {
            //log("identify state is ready");
        }
        @Override
        public void onStarted() {
            //log("User touched fingerprint sensor");
        }
        @Override
        public void onCompleted() {
            //log("the identify is completed");
            onReadyIdentify = false;
            Log.d(TAG, "onCompleted: onready "+onReadyIdentify);
            Log.d(TAG, "onCompleted: need "+needRetryIdentify);
            if (needRetryIdentify) {
                needRetryIdentify = false;
                mHandler.sendEmptyMessageDelayed(MSG_AUTH_WITHOUT_DIALOG, 100);
            }
        }
    };
    //HANDLER CANCEL IDENTIFY
    public void handleCancelIdentify(){
        mHandler.sendEmptyMessage(MSG_CANCEL);
    }
    //CANCEL FINGER 1004
    private void cancelIdentify() {
        if (onReadyIdentify == true) {
            try {
                if (mSpassFingerprint != null) {
                    mSpassFingerprint.cancelIdentify();
                }
                //log("cancelIdentify is called");
            } catch (IllegalStateException ise) {
                //log(ise.getMessage());
            }
            onReadyIdentify = false;
            needRetryIdentify = false;
        } else {
            //log("Please request Identify first");
        }
    }
    public static String getEventStatusName(int eventStatus) {
        switch (eventStatus) {
            case SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS:
                return "STATUS_AUTHENTIFICATION_SUCCESS";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS:
                return "STATUS_AUTHENTIFICATION_PASSWORD_SUCCESS";
            case SpassFingerprint.STATUS_TIMEOUT_FAILED:
                return "STATUS_TIMEOUT";
            case SpassFingerprint.STATUS_SENSOR_FAILED:
                return "STATUS_SENSOR_ERROR";
            case SpassFingerprint.STATUS_USER_CANCELLED:
                return "STATUS_USER_CANCELLED";
            case SpassFingerprint.STATUS_QUALITY_FAILED:
                return "STATUS_QUALITY_FAILED";
            case SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                return "STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE";
            case SpassFingerprint.STATUS_BUTTON_PRESSED:
                return "STATUS_BUTTON_PRESSED";
            case SpassFingerprint.STATUS_OPERATION_DENIED:
                return "STATUS_OPERATION_DENIED";
            case SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED:
            default:
                return "STATUS_AUTHENTIFICATION_FAILED";
        }
    }
    private static final int MSG_AUTH_DIALOG = 1000;
    private static final int MSG_AUTH_WITHOUT_DIALOG = 1002;
    private static final int MSG_REGISTER = 1003;
    private static final int MSG_CANCEL = 10004;
    private String MSG_BUNDLE = "msg_bundle";
    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_AUTH_DIALOG:
                boolean designated = message.getData().getBoolean(MSG_BUNDLE);
                identifyFingerWithDialog(designated);
                break;
            case MSG_AUTH_WITHOUT_DIALOG:
                identifyFingerWithoutDialog();
                break;
            case MSG_REGISTER:
                registerFinger();
                break;
            case MSG_CANCEL:
                cancelIdentify();
                break;
        }
        return true;
    }
}
interface MyPassCallback{
    void onFinishedIdentify(int eventStatus);
    void onFinishedRegister(boolean register);
}
