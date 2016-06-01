package com.nuvoton.skyeye;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.renderscript.Type;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragment {
    private static String platform = "";
    private String TAG = "timsheu";
    public static SettingFragment newInstance(String pf){
        platform = pf;
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    public SettingFragment(){
        Log.d(TAG, "SettingFragment: " + platform);
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (platform.equals("SkyEye")) {
            addPreferencesFromResource(R.xml.settings);
        } else if (platform.equals("DVR")) {
            addPreferencesFromResource(R.xml.settings_dvr);
        }

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }
}
