package com.nuvoton.socketmanager;

import com.longevitysoft.android.util.*;
import com.longevitysoft.android.xml.plist.*;
/**
 * Created by timsheu on 6/3/16.
 */
public class ReadConfigure {
    private static ReadConfigure readConfigure = new ReadConfigure();

    private ReadConfigure(){}
    public static ReadConfigure getInstance(){
        return readConfigure;
    }

}
