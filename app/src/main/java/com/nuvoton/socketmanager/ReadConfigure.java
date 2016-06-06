package com.nuvoton.socketmanager;

import android.content.Context;
import android.support.annotation.RequiresPermission;

import com.longevitysoft.android.util.*;
import com.longevitysoft.android.xml.plist.*;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by timsheu on 6/3/16.
 */
public class ReadConfigure {
    private static ReadConfigure readConfigure = new ReadConfigure();
//    private static String

    private ReadConfigure(){}
    public static ReadConfigure getInstance(Context context){
        try{
            FileInputStream fileInputStream = context.openFileInput("");
        }catch (IOException e){
            e.printStackTrace();
        }

        return readConfigure;
    }
}
