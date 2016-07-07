package com.cos.mos.sr.util;

import android.app.ActivityManager;
import android.content.Context;

import com.cos.mos.sr.SRApplication;

/**
 * Created by AdamLi on 2016/7/7.
 */
public class Util {

    public static boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) SRApplication.getAppCtx().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
