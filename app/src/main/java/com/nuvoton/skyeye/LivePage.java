package com.nuvoton.skyeye;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import android.app.FragmentManager;
import android.view.Surface;
import android.view.View;
import android.widget.VideoView;

import java.util.ArrayList;

public class LivePage extends AppCompatActivity implements LiveFragment.OnHideBottomBarListener {
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
    private boolean clicked = false;
    private boolean isLandscape = false;
    private static final String TAG = "SkyEye", FRAGMENT_TAG = "CURRENT_FRAGMENT_INDEX";
    private AHBottomNavigation bottomNavigation;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private FragmentManager fragmentManager = getFragmentManager();
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
        Log.d(TAG, "onCreate:" + platform);
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
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_navigation);
        AHBottomNavigationItem liveItem = new AHBottomNavigationItem("Live", R.drawable.livetab);
        AHBottomNavigationItem fileItem = new AHBottomNavigationItem("File", R.drawable.foldertab);
        AHBottomNavigationItem settingItem = new AHBottomNavigationItem("Setting", R.drawable.geartab);

        bottomNavigationItems.add(liveItem);
        bottomNavigationItems.add(fileItem);
        bottomNavigationItems.add(settingItem);

        bottomNavigation.addItems(bottomNavigationItems);
        bottomNavigation.setAccentColor(Color.parseColor("#007DFF"));

        bottomNavigation.setNotification(0, 0);
        LiveFragment fragment = new LiveFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_content, fragment)
                .commit();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        bottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position, boolean wasSelected){
                index = position;
                if (position == 0){
                    LiveFragment fragment = new LiveFragment();
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_content, fragment)
                            .commit();
                }else if (position == 1){
                    FileFragment fragment = FileFragment.newInstance(position);
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_content, fragment)
                            .commit();
                }else{
                    SettingFragment fragment = SettingFragment.newInstance(platform);
                    fragmentManager.beginTransaction()
                            .replace(R.id.fragment_content, fragment)
                            .commit();
                }
                bottomNavigation.setNotification(0, position);
            }
        });

    }

    private void changeFragment(int savedIndex){
        index = savedIndex;
        if (index == 0){
            LiveFragment fragment = new LiveFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, fragment)
                    .commit();
        }else if (index == 1){
            FileFragment fragment = FileFragment.newInstance(index);
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, fragment)
                    .commit();
        }else{
            SettingFragment fragment = new SettingFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_content, fragment)
                    .commit();
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
}
