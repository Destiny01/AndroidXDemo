package com.jinnuojiayin.recorddemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jinnuojiayin.recorddemo.util.AudioRecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isRecord = false;
    private AudioRecorder audioRecorder;

    private TextView timeTips;
    private RelativeLayout micLayout;
    private ImageView mic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioRecorder = new AudioRecorder();
        initView();
        initEvent();
    }


    public void initView() {
        timeTips = (TextView) findViewById(R.id.tv_video_time);
        micLayout = (RelativeLayout) findViewById(R.id.mic_img_relative);
        mic = (ImageView) findViewById(R.id.mic_img);
        timeTips.setVisibility(View.INVISIBLE);
    }

    public void initEvent() {
        micLayout.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        requestPermission();
    }

    /**
     *
     */
    private void record() {
        isRecord = !isRecord;
        if (isRecord) {
            mic.setSelected(true);
            Toast.makeText(this, "start", Toast.LENGTH_SHORT).show();
            timeTips.setVisibility(View.VISIBLE);
            audioRecorder.startAudioRecording();
        } else {
            mic.setSelected(false);
            Toast.makeText(this, "stop", Toast.LENGTH_SHORT).show();
            timeTips.setVisibility(View.INVISIBLE);
            audioRecorder.stopAudioRecording();
        }
    }

    /**
     * 获取权限使用的 RequestCode
     */
    private static final int PERMISSIONS_REQUEST_CODE = 1002;

    /**
     * 检查支付宝 SDK 所需的权限，并在必要的时候动态获取。
     * 在 targetSDK = 23 以上，READ_PHONE_STATE 和 WRITE_EXTERNAL_STORAGE 权限需要应用在运行时获取。
     * 如果接入支付宝 SDK 的应用 targetSdk 在 23 以下，可以省略这个步骤。
     */
    private void requestPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.RECORD_AUDIO
                    }, PERMISSIONS_REQUEST_CODE);

        } else {
            showToast(this, getString(R.string.permission_already_granted));
            record();
        }
    }

    /**
     * 权限获取回调
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {

                // 用户取消了权限弹窗
                if (grantResults.length == 0) {
                    showToast(this, getString(R.string.permission_rejected));
                    return;
                }

                // 用户拒绝了某些权限
                for (int x : grantResults) {
                    if (x == PackageManager.PERMISSION_DENIED) {
                        showToast(this, getString(R.string.permission_rejected));
                        return;
                    }
                }

                // 所需的权限均正常获取
                showToast(this, getString(R.string.permission_granted));
                record();
            }
        }
    }

    private static void showToast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }
}