package com.smox.smoxuser.utils.preference;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import  com.smox.smoxuser.BuildConfig;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class LocalSharedPreference {
    private static SharedPreferences sharedPreferences;
    private static LocalSharedPreference instance;

    private static final String IS_ADDRESS_FOR_EDIT = "isAddressForEdit";


    private LocalSharedPreference() {
    }

    public static LocalSharedPreference getInstance(Context context) {
        if (instance == null) {
            instance = new LocalSharedPreference();
            sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        }
        return instance;
    }



    public boolean getIsForEdit() {
        return sharedPreferences.getBoolean(IS_ADDRESS_FOR_EDIT, false);
    }

    public void saveIsForEdit(boolean isForEdit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_ADDRESS_FOR_EDIT, isForEdit);
        editor.commit();
    }


}