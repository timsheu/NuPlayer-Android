package com.nuvoton.nuplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nuvoton.socketmanager.ReadConfigure;

public class StartPage extends AppCompatActivity {
    static final String TAG = "StartPage";
    private ReadConfigure configure;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);

        Button skyeyeButton = (Button)findViewById(R.id.button_skyEye);
        skyeyeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openLivePage(0);
            }
        });

        Button dvrButton = (Button)findViewById(R.id.button_DVR);
        dvrButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openLivePage(1);
            }
        });

        Button showTutorialButton = (Button) findViewById(R.id.showTutorial);
        showTutorialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTutorial();
            }
        });
        configure = ReadConfigure.getInstance(this, false);
        boolean isTutorial = configure.isTutorial();
        Log.d(TAG, "onCreate: " + String.valueOf(isTutorial));
        if (isTutorial){
            showTutorial();
        }
    }

    private void openLivePage(int pageIndex){
        Intent intent = new Intent();
        if (pageIndex == 0){ // nuplayer live page
            intent.putExtra("Platform", "SkyEye");
            intent.setClass(this, SelectCamera.class);
        }else if (pageIndex == 1){
            intent.putExtra("Platform", "DVR");
            intent.putExtra("CameraSerial", "0");
            intent.setClass(this, LivePage.class);
        }
        startActivity(intent);
    }

    private void showTutorial(){
        configure.setTutorial(1, false);
        Intent intent = new Intent();
        intent.setClass(this, TutorialActivity.class);
        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    private boolean exit = false;
    @Override
    public void onBackPressed() {
        if (exit){
            super.onBackPressed();
            return;
        }else {
            Toast.makeText(this, "Press Back again to Exit !", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3000);

        }
    }


}
