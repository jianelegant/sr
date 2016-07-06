package com.cos.mos.sr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.cos.mos.sr.service.ScrRecorder;

public class RequestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager .LayoutParams.FLAG_FULLSCREEN);

        ScrRecorder.instance.init(this);
        ScrRecorder.instance.requestRecord();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ScrRecorder.instance.start(resultCode, data);
        finish();
    }
}
