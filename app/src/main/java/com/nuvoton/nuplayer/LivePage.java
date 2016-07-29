package com.nuvoton.nuplayer;

import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import android.app.FragmentManager;
import android.view.Surface;
import android.view.View;

import java.util.ArrayList;

public class LivePage extends AppCompatActivity implements LiveFragment.OnHideBottomBarListener, SettingFragment.SettingFragmentInterface {
    // live view callbacks
    public void onHideBottomBar(boolean isHide){
        if (isHide){
            bottomNavigation.hideBottomNavigation(true);
        } else {
            bottomNavigation.restoreBottomNavigation(true);
        }
    }

    private int index=0;
    private String platform = "";
    private String cameraSerial = "0";
    private boolean clicked = false;
    private boolean isLandscape = false;
    private static final String TAG = "SkyEye", FRAGMENT_TAG = "CURRENT_FRAGMENT_INDEX";
    private AHBottomNavigation bottomNavigation;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private FragmentManager fragmentManager = getFragmentManager();
    private LiveFragment liveFragment = null;
    private FileFragment fileFragment = null;
    private SettingFragment settingFragment = null;
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);
        View decorView = getWindow().getDecorView();

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if (index != 2){
                bottomNavigation.hideBottomNavigation(true);
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                isLandscape = true;
            }
        } else {
            bottomNavigation.restoreBottomNavigation(true);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            isLandscape = false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        View decorView = getWindow().getDecorView();
        setContentView(R.layout.activity_live_page);
        platform = getIntent().getStringExtra("Platform");
        try{
            cameraSerial = getIntent().getStringExtra("CameraSerial");
        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "onCreate:" + platform + ", " + cameraSerial);
        initUI();
        switch (orientation){
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                if (index != 2){
                    bottomNavigation.hideBottomNavigation(true);
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
                    isLandscape = true;
                }
                break;
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                bottomNavigation.restoreBottomNavigation(true);
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                isLandscape = false;
                break;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        outState.putInt(FRAGMENT_TAG, index);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: " + String.valueOf(savedInstanceState.getInt(FRAGMENT_TAG)));
        changeFragment(savedInstanceState.getInt(FRAGMENT_TAG));
    }

    private void initUI(){
        final Bundle bundle = new Bundle();
        bundle.putString("Platform", platform);
        bundle.putString("CameraSerial", cameraSerial);
        if (liveFragment == null){
            liveFragment = LiveFragment.newInstance(bundle);
        }
        if (settingFragment == null){
            settingFragment = SettingFragment.newInstance(bundle);
            settingFragment.settingFragmentInterface = this;
        }
        if (fileFragment == null){
            fileFragment = FileFragment.newInstance(bundle);
        }
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem liveItem = new AHBottomNavigationItem("Live", R.drawable.livetab);
        AHBottomNavigationItem fileItem = new AHBottomNavigationItem("File", R.drawable.foldertab);
        final AHBottomNavigationItem settingItem = new AHBottomNavigationItem("Setting", R.drawable.geartab);

        bottomNavigationItems.add(liveItem);
        bottomNavigationItems.add(fileItem);
        bottomNavigationItems.add(settingItem);

        bottomNavigation.addItems(bottomNavigationItems);
        bottomNavigation.setAccentColor(Color.parseColor("#007DFF"));

        bottomNavigation.setNotification(0, 0);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, boolean wasSelected){
                if (index == position) return;
                FragmentTransaction trans = fragmentManager.beginTransaction();
                if (liveFragment.isAdded()) trans.hide(liveFragment);
                if (fileFragment.isAdded()) trans.hide(fileFragment);
                if (settingFragment.isAdded()) trans.hide(settingFragment);

                if (position == 0){
                    if (!liveFragment.isAdded()){
                        trans.add(R.id.fragment_content, liveFragment);
                    }else{
                        trans.show(liveFragment);
                    }
                }else if (position == 1){
                    if (!fileFragment.isAdded()){
                        trans.add(R.id.fragment_content, fileFragment);
                    }else{
                        trans.show(fileFragment);
                    }
                }else{
                    if (!settingFragment.isAdded()){
                        trans.add(R.id.fragment_content, settingFragment);
                    }else{
                        trans.show(settingFragment);
                    }
                }
                trans.commit();
                bottomNavigation.setNotification(0, position);
                index = position;
            }
        });
        FragmentTransaction trans = fragmentManager.beginTransaction();
        if (!liveFragment.isAdded()){
            trans.add(R.id.fragment_content, liveFragment).commit();
        }else{
            trans.show(liveFragment).commit();
        }
    }

    private void changeFragment(int savedIndex){
        Bundle bundle = new Bundle();
        bundle.putString("Platform", platform);
        bundle.putString("CameraSerial", cameraSerial);
        index = savedIndex;
        if (index == 0){
            FragmentTransaction trans = fragmentManager.beginTransaction();
            if (!liveFragment.isAdded()){
                trans.hide(fileFragment).hide(settingFragment).add(R.id.fragment_content, liveFragment).commit();
            }else{
                trans.hide(fileFragment).hide(settingFragment).show(liveFragment).commit();
            }
        }else if (index == 1){
            FragmentTransaction trans = fragmentManager.beginTransaction();
            if (!fileFragment.isAdded()){
                trans.hide(liveFragment).hide(settingFragment).add(R.id.fragment_content, fileFragment).commit();
            }else{
                trans.hide(liveFragment).hide(settingFragment).show(fileFragment).commit();
            }
        }else{
            FragmentTransaction trans = fragmentManager.beginTransaction();
            if (!settingFragment.isAdded()){
                trans.hide(fileFragment).hide(liveFragment).add(R.id.fragment_content, settingFragment).commit();
            }else{
                trans.hide(fileFragment).hide(liveFragment).show(settingFragment).commit();
            }
        }
        bottomNavigation.setNotification(0, index);
        bottomNavigation.setCurrentItem(index);
    }

    public void showBottomBar(boolean option){
        if (option == false){
            bottomNavigation.hideBottomNavigation(true);
        }else {
            bottomNavigation.restoreBottomNavigation(true);
        }
    }

    @Override
    public void restartStream() {
        liveFragment.restartStream();
    }

    @Override
    public void manualSendReport() {

    }
}
