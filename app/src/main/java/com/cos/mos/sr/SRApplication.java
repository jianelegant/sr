package com.cos.mos.sr;

import android.app.Application;
import android.content.Context;

/**
 * Created by AdamLi on 2016/7/6.
 */
public class SRApplication extends Application{

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getAppCtx(){
        return mContext;
    }
}
