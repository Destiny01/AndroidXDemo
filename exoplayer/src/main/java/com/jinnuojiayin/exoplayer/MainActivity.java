package com.jinnuojiayin.exoplayer;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.exoplayer2.util.Util;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    TextView startTime;//开启时间00:00
    TextView endTime;//音频总时间 00:00
    TextView title;
    ImageView play;
    ImageView pause;
    private DefaultTimeBar mTimeBar;
    //00:41
    //private String baseUrl = "http://app.tianshengdiyi.com/upload_c/20171110/kaiyan_sound/v1510284684.mp3";
    // private String baseUrl = "http://app.tianshengdiyi.com/upload_a/20180927/kaiyan_member_sound/v1538026810FDRrY0.m4a";
    private String baseUrl = Environment.getExternalStorageDirectory().getAbsolutePath() + "/code.aac";
    private AudioControl audioControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_audio);
        startTime = findViewById(R.id.tv_start_time);
        endTime = findViewById(R.id.tv_end_time);
        title = findViewById(R.id.text);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        mTimeBar = findViewById(R.id.exo_progress);
        init();
    }

    private void init() {
        audioControl = new AudioControl(this);
        audioControl.setOnAudioControlListener(new AudioControl.AudioControlListener() {
            @Override
            public void setCurPositionTime(int position, long curPositionTime) {
                mTimeBar.setPosition(curPositionTime);
                Log.i(TAG, "当前时间curPositionTime" + curPositionTime);
            }

            @Override
            public void setDurationTime(int position, long durationTime) {
                mTimeBar.setDuration(durationTime);
                Log.i(TAG, "总时间durationTime" + durationTime);
            }

            @Override
            public void setBufferedPositionTime(int position, long bufferedPosition) {
                mTimeBar.setBufferedPosition(bufferedPosition);
            }

            @Override
            public void setCurTimeString(int position, String curTimeString) {
                startTime.setText(curTimeString);
                Log.i(TAG, "当前时间curTimeString" + curTimeString);
            }

            @Override
            public void isPlay(int position, boolean isPlay) {
                if (isPlay) {
                    if (play != null) {
                        play.setVisibility(View.INVISIBLE);
                    }
                    if (pause != null) {
                        pause.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (play != null) {
                        play.setVisibility(View.VISIBLE);
                    }
                    if (pause != null) {
                        pause.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void setDurationTimeString(int position, String durationTimeString) {
                endTime.setText(durationTimeString);
                Log.i(TAG, "总时间durationTimeString" + durationTimeString);
            }
        });

        mTimeBar.addListener(new TimeBar.OnScrubListener() {
            @Override
            public void onScrubStart(TimeBar timeBar, long position) {

            }

            @Override
            public void onScrubMove(TimeBar timeBar, long position) {
                startTime.setText(Util.getStringForTime(audioControl.getFormatBuilder(), audioControl.getFormatter(), position));
            }

            @Override
            public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                audioControl.seekToTimeBarPosition(position);
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play://播放
                audioControl.onPrepare(baseUrl);
                audioControl.onStart(0);
                break;
            case R.id.pause://暂停
                audioControl.onPause();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        audioControl.release();
    }
}
