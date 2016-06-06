package com.nuvoton.socketmanager;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.RequiresPermission;
import android.util.Log;

import com.longevitysoft.android.util.*;
import com.longevitysoft.android.xml.plist.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;

/**
 * Created by timsheu on 6/3/16.
 */
public class ReadConfigure {
    private static Context contextLocal;
    private static final String TAG = "ReadConfigure";
    private static ReadConfigure readConfigure = new ReadConfigure();

    private static String settingConfig = "SettingsPropertyList.plist";
    private static String infoConfig = "InfoPropertyList.plist";
    private static String fileNameConfig = "FileNamePropertyList.plist";
    private static String wifiConfig = "QRCodePropertyList.plist";

    private static String settingLocalConfig = "SettingsPropertyListLocal.plist";
    private static String infoLocalConfig = "InfoPropertyListLocal.plist";
    private static String fileNameLocalConfig = "FileNamePropertyListLocal.plist";
    private static String wifiLocalConfig = "QRCodePropertyListLocal.plist";

    private ReadConfigure(){
        Log.d(TAG, "ReadConfigure: create");
    }
    public static ReadConfigure getInstance(Context context){
        Log.d(TAG, "getInstance: ");
        contextLocal = context;
        readConfigure.ExamineLocalFile();

        return readConfigure;
    }

    public void ExamineLocalFile() {
        Log.d(TAG, "ExamineLocalFile: " );
        try{
            InputStream inputStream = contextLocal.getAssets().open(settingConfig);
            File destFile = contextLocal.getFileStreamPath(settingLocalConfig);
            if (destFile.exists() == false){
                copyFile(inputStream, destFile);
            }
            FileInputStream fo = new FileInputStream(destFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(fo));
            String line;
            while((line = br.readLine()) != null){
                Log.d(TAG, "copyFile: " + line);
            }
            br.close();
            fo.close();
            Log.d(TAG, "ExamineLocalFile: dest" + destFile.getPath());
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void copyFile(InputStream inputStream, File dest) throws IOException{
        FileOutputStream outputStream = new FileOutputStream(dest);
        try {
            byte[] bytes = new byte[1024];
            int read;
            while((read = inputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, read);
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (inputStream != null){
                try{
                    inputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if (outputStream != null){
                try {
                    outputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }

}
