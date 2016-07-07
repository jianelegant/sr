package com.cos.mos.sr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cos.mos.sr.service.SRService;
import com.cos.mos.sr.util.Util;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS_NEED_REQUEST = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQUEST_CODE = 1989;

    Button mStartServiceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStartServiceBtn = (Button) findViewById(R.id.id_btn_start);

        mStartServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SRService.class);
                if (Util.isServiceRunning(SRService.class)){
                    stopService(intent);
                    mStartServiceBtn.setText(R.string.btn_start_service);
                    mStartServiceBtn.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
                } else {
                    startService(intent);
                    mStartServiceBtn.setText(R.string.btn_stop_service);
                    mStartServiceBtn.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
                }
            }
        });

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetUIStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void resetUIStatus(){
        if (Util.isServiceRunning(SRService.class)){
            mStartServiceBtn.setText(R.string.btn_stop_service);
            mStartServiceBtn.getBackground().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY);
        } else {
            mStartServiceBtn.setText(R.string.btn_start_service);
            mStartServiceBtn.getBackground().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.MULTIPLY);
        }
    }

    private void checkPermissions() {
        List<String> pers = new ArrayList<>();
        for(String per : PERMISSIONS_NEED_REQUEST){
            if(ActivityCompat.checkSelfPermission(MainActivity.this, per) == PackageManager.PERMISSION_DENIED){
                pers.add(per);
            }
        }
        int size = pers.size();
        if(size > 0){
            ActivityCompat.requestPermissions(MainActivity.this, pers.toArray(new String[size]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(PERMISSION_REQUEST_CODE == requestCode){
            int length = grantResults.length;
            for(int i = 0 ; i < length;i++){
                if(PackageManager.PERMISSION_DENIED == grantResults[i]){
                    // TODO permission denied.
                }
            }
        }
    }
}
