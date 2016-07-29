package com.nuvoton.nuplayer;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfiguration;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;

/**
 * Created by timsheu on 7/15/16.
 */


@ReportsCrashes(
        mailTo = "cchsu20@nuvoton.com",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.CUSTOM_DATA,
                ReportField.STACK_TRACE,
                ReportField.LOGCAT
        }
//        mode = ReportingInteractionMode.TOAST
)

public class CrashReport extends Application {
    private static final String TAG = "CrashReport";
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        int toastTextID = R.string.crash_toast_text;
        if (ACRA.isInitialised()){
            Toast.makeText(this, toastTextID, Toast.LENGTH_LONG).show();
        }
        ACRA.init(this);
    }
}
