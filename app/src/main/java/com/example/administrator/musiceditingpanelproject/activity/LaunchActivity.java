package com.example.administrator.musiceditingpanelproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.administrator.musiceditingpanelproject.R;
import com.example.administrator.musiceditingpanelproject.application.MusicEditingPanelApplication;

/**
 * Edited by Administrator on 2018/3/28.
 */

public class LaunchActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        TextView mTvLaunch = findViewById(R.id.tv_launch);
        mTvLaunch.setOnClickListener(this);
        mTvLaunch.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("!!!!!", "run: " + MusicEditingPanelApplication.getApplication().getResources().getDisplayMetrics().density);
            }
        },300);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_launch:
                startActivity(new Intent(LaunchActivity.this, EditMusicActivity.class));
        }
    }
}
