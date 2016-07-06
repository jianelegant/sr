package com.cos.mos.sr.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;

import com.cos.mos.sr.RequestActivity;
import com.cos.mos.sr.util.L;
import com.cos.mos.sr.R;
import com.cos.mos.sr.util.ShakeDetector;

/**
 * Created by AdamLi on 2016/7/6.
 */
public class SRService extends Service{

    private static final int NOTIFICATION_ID = 1989;

    private ShakeDetector mShakeDetector;

    @Override
    public void onCreate() {
        super.onCreate();
        L.d("LifeCycle");

        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {
                L.d("onShake:"+count);
                if(ScrRecorder.instance.isRecording()){
                    ScrRecorder.instance.quit();
                } else {
                    Intent intent = new Intent(SRService.this, RequestActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            }
        });
        mShakeDetector.register();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d("LifeCycle");
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.notification_content))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(NOTIFICATION_ID, notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        L.d("LifeCycle");
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.d("LifeCycle");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.d("LifeCycle");
        mShakeDetector.unRegister();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        L.d("LifeCycle");
    }
}
