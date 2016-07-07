package com.cos.mos.sr.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.widget.Toast;

import com.cos.mos.sr.R;
import com.cos.mos.sr.SRApplication;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by AdamLi on 2016/7/5.
 */
public enum ScrRecorder {
    instance;

    public static final int REQUEST_CODE = 1989;
    private static final String DEFAULT_FOLDER = "CosScrRecorder";
    private static final String DEFAULT_PREFIX = "Scr_recoder_";
    private static final String DEFAULT_EXTENSION_NAME = ".mp4";
    private static final String DEFAULT_VD_NAME = ScrRecorder.class.getSimpleName() + "-Display";

    private static final int HANDLER_MSG_START_RECORDING = 1;
    private static final int HANDLER_MSG_STOP_RECORDING = 2;

    private static final String MIME_TYPE = "video/avc"; // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30; // 30 fps
    private static final int IFRAME_INTERVAL = 10; // 10 seconds between I-frames
    private static final int TIMEOUT_US = 10000;

    private WeakReference<Activity> mWeakAct;
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;

    private MediaCodec mEncoder;
    private Surface mSurface;
    private MediaMuxer mMediaMuxer;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();

    private volatile AtomicBoolean mIsQuit = new AtomicBoolean(false);
    private boolean mIsMuxerStarted = false;
    private int mVideoTrackIndex = -1;

    private int mWidth = 1280;
    private int mHeight = 720;
    private int mBitRate = 6000000;
    private int mDpi = 1;

    public void init(Activity activity){
        mWeakAct = new WeakReference<>(activity);
        mMediaProjectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;
    }

    public void requestRecord(){
        Activity activity = mWeakAct.get();
        if(activity != null){
            Intent intent = mMediaProjectionManager.createScreenCaptureIntent();
            activity.startActivityForResult(intent, REQUEST_CODE);
        }
    }

    public void start(final int resultCode, final Intent requestData){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, requestData);
                    if (null == mMediaProjection) {
                        return;
                    }
                    mHandler.sendEmptyMessage(HANDLER_MSG_START_RECORDING);
                    /**
                     * wait the toast dismiss
                     */
                    Thread.sleep(2700);
                    if (!prepareEncoder()) {
                        return;
                    }
                    startRecord();
                    mHandler.sendEmptyMessage(HANDLER_MSG_STOP_RECORDING);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    release();
                }
            }
        }).start();
    }

    private void startRecord() {
        mIsQuit.set(false);
        while (!mIsQuit.get()){
            int index = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_US);
            switch (index){
                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                    resetOutputFormat();
                    break;
                case MediaCodec.INFO_TRY_AGAIN_LATER:
                    break;
                default:
                    if(index > 0){
                        encodeToVideoTrack(index);
                    }
                    break;
            }
        }
    }

    private void resetOutputFormat() {
        if(mIsMuxerStarted){
            return;
        }
        MediaFormat mediaFormat = mEncoder.getOutputFormat();
        mVideoTrackIndex = mMediaMuxer.addTrack(mediaFormat);
        mMediaMuxer.start();
        mIsMuxerStarted = true;
    }

    private void encodeToVideoTrack(int index) {
        if (!mIsMuxerStarted){
            return;
        }
        ByteBuffer byteBuffer = mEncoder.getOutputBuffer(index);
        if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0){
            mBufferInfo.size = 0;
        }
        if (0 == mBufferInfo.size){
            byteBuffer = null;
        }
        if(byteBuffer != null){
            byteBuffer.position(mBufferInfo.offset);
            byteBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
            mMediaMuxer.writeSampleData(mVideoTrackIndex, byteBuffer, mBufferInfo);
        }
        mEncoder.releaseOutputBuffer(index, false);
    }

    private boolean prepareEncoder() {
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFRAME_INTERVAL);

        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
            mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mEncoder.createInputSurface();
            mEncoder.start();

            String fileName = generateFileName();
            mMediaMuxer = new MediaMuxer(fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(DEFAULT_VD_NAME, mWidth, mHeight, mDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mSurface, null, null);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setWidth(int width){
        mWidth = width;
    }

    public void setHeight(int height){
        mHeight = height;
    }

    public void setBitRate(int bitRate){
        mBitRate = bitRate;
    }

    public void setDpi(int dpi){
        mDpi = dpi;
    }

    public String generateFileName(){
        String folderName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + DEFAULT_FOLDER;
        File file = new File(folderName);
        if(!file.exists()){
            file.mkdirs();
        }
        return folderName + File.separator + DEFAULT_PREFIX + System.currentTimeMillis() + DEFAULT_EXTENSION_NAME;
    }

    public void quit(){
        mIsQuit.set(true);
    }

    public boolean isRecording(){
        return mIsMuxerStarted;
    }

    private void release() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
        if (mMediaProjection != null) {
            mMediaProjection.stop();
        }
        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
            mIsMuxerStarted = false;
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLER_MSG_START_RECORDING:
                    Toast.makeText(SRApplication.getAppCtx(), R.string.toast_start_recording, Toast.LENGTH_SHORT).show();
                    break;
                case HANDLER_MSG_STOP_RECORDING:
                    Toast.makeText(SRApplication.getAppCtx(), R.string.toast_stop_recording, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
}
