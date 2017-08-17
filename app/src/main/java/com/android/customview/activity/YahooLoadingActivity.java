package com.android.customview.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.android.customview.R;
import com.android.customview.widget.YahooLoadingView;

/**
 * Created by Ben.li on 2017/8/17.
 */

public class YahooLoadingActivity extends AppCompatActivity implements Runnable {

    private YahooLoadingView loadingView;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_yahoo_loading);

        handler = new Handler();
        loadingView = (YahooLoadingView) findViewById(R.id.loadingView);

        handler.postDelayed(this, 3000);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(this);
        super.onDestroy();
    }

    public void onClick(View view) {
        loadingView.startLoading();
        handler.postDelayed(this, 3000);
    }

    @Override
    public void run() {
        loadingView.loadingComplete();
    }
}
