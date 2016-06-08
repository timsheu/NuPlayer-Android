package com.nuvoton.skyeye;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class SelectCamera extends AppCompatActivity {
    private String platform = "";
    private String cameraSerial = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_camera);
        platform = getIntent().getStringExtra("Platform");

        AdapterView.OnItemClickListener itemClickListener =
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        cameraSerial = String.valueOf(position + 1);
                        Intent intent = new Intent(SelectCamera.this, LivePage.class);
                        intent.putExtra("Platform", platform);
                        intent.putExtra("CameraSerial", cameraSerial);
                        startActivity(intent);

                    }
                };
        ListView listView = (ListView) findViewById(R.id.list_select_camera);
        listView.setOnItemClickListener(itemClickListener);
    }
}
