package com.jinnuojiayin.recorddemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

/**
 *
 */
public class FirstActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button://整句跟读
                gotoActivity(TotalAudioActivity.class);
                break;
            case R.id.button2://分句跟读
                gotoActivity(SubAudioActivity.class);
                break;
        }
    }

    /**
     * @param cls
     */
    public void gotoActivity(Class<?> cls) {
        Intent intent = new Intent(this, cls);
        startActivity(intent);
    }
}
