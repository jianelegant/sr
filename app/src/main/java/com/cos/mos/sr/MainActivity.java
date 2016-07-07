package com.cos.mos.sr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cos.mos.sr.service.SRService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS_NEED_REQUEST = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQUEST_CODE = 1989;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.id_btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(MainActivity.this, SRService.class));
            }
        });

        checkPermissions();
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
