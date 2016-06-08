package com.nuvoton.skyeye;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartPage extends AppCompatActivity {

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

    }

    private void openLivePage(int pageIndex){
        Intent intent = new Intent();
        if (pageIndex == 0){ // skyeye live page
            intent.putExtra("Platform", "SkyEye");
            intent.setClass(this, SelectCamera.class);
        }else if (pageIndex == 1){
            intent.putExtra("Platform", "DVR");
            intent.putExtra("CameraSerial", "0");
            intent.setClass(this, LivePage.class);
        }
        startActivity(intent);
    }
}
