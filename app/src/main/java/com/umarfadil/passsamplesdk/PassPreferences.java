package com.umarfadil.passsamplesdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by umarfadil on 8/17/17.
 */

public class PassPreferences {
    private String PREFS_NAME = "PassPrefs";
    private String KEY_ACTIVE = "PassActive";
    private String KEY_DESIGNATED = "PassDesignated";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private String TAG = "PREFERENCES TAG";
    public PassPreferences(Context context){
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }
    public void setActive(boolean active){
        editor.putBoolean(KEY_ACTIVE, active);
        editor.apply();
    }
    public Boolean getActive(){
        return preferences.getBoolean(KEY_ACTIVE, false);
    }
    public void setDesignated(Integer[] input){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            str.append(input[i]).append(",");
        }
        editor.putString(KEY_DESIGNATED, str.toString());
        editor.apply();
    }
    public Integer[] getDesignated(){
        String savedString = preferences.getString(KEY_DESIGNATED, "");
        Log.d(TAG, "getDesignated: saved string "+savedString);
        if (savedString == "") return new Integer[0];
        String[] designatedStringArr= savedString.split(",");
        Integer[] designatedIntArr = new Integer[designatedStringArr.length];
        Log.d(TAG, "getDesignated: "+designatedStringArr.length);
        for (int i = 0 ; i < designatedStringArr.length ; i++){
            designatedIntArr[i] = Integer.parseInt(designatedStringArr[i]);
        }
        return designatedIntArr;
    }
}