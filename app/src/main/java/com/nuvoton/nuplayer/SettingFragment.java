package com.nuvoton.nuplayer;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.preference.Preference.OnPreferenceChangeListener;

import com.nuvoton.socketmanager.ReadConfigure;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{
    private String key; 
    private static String platform, cameraSerial, preferenceName;
    private String TAG = "SettingFragment";
    private ArrayList<Preference> settingArrayList;
    public static SettingFragment newInstance(Bundle bundle){
        platform = bundle.getString("Platform");
        cameraSerial = bundle.getString("CameraSerial");
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    public SettingFragment(){
        Log.d(TAG, "SettingFragment: " + platform);
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        preferenceName = "Setup Camera " + String.valueOf(cameraSerial);

//        getActivity().getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        getPreferenceManager().setSharedPreferencesName(preferenceName);
//        getPreferenceManager().getSharedPreferences();
        Log.d(TAG, "onCreate: " + preferenceName + " pref name: " + getPreferenceManager().getSharedPreferencesName());
        // Inflate the layout for this fragment

        if (platform.equals("SkyEye")) {
            addPreferencesFromResource(R.xml.settings);
        } else if (platform.equals("DVR")) {
            addPreferencesFromResource(R.xml.settings_dvr);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "onSharedPreferenceChanged: " + key);
        determineSettings(key, sharedPreferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        getActivity().getSharedPreferences(preferenceName, Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(this);
//        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        getActivity().getSharedPreferences(preferenceName, Context.MODE_PRIVATE).unregisterOnSharedPreferenceChangeListener(this);
//        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void determineSettings(String key, SharedPreferences sharedPreference){
        switch (key){
            case "Resolution":
//                if (valueOfKey.equals("")){
//
//                }
                break;
            case "Adaptive":
                break;
            case "Fixed Bit Rate":
                break;
            case "Fixed Quality":
                break;
            case "FPS":
                break;
            case "Device Mic":
                break;
            case "Transmission":
                break;
            case "Reboot":
                break;
            case "Reset Data":
                break;
            case "Wi-Fi QR Code":
                break;
        }
    }

}
