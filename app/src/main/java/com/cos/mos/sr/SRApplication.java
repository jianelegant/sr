package com.cos.mos.sr;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.flurry.android.FlurryAgent;

/**
 * Created by AdamLi on 2016/7/6.
 */
public class SRApplication extends Application{

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        initFlurry();
    }

    private void initFlurry() {
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .withLogLevel(Log.INFO)
                .withContinueSessionMillis(5000L)
                .withCaptureUncaughtExceptions(true)
                .withPulseEnabled(true)
                .build(this, "ZH7ZXGSKX7CJCJ35NJ4G");
    }

    public static Context getAppCtx(){
        return mContext;
    }
}
