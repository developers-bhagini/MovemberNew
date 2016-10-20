package com.app.movember.ui;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.app.movember.R;
import com.app.movember.util.Constants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_towards_left);
        ActionBar actionBar = getSupportActionBar();
        if (null != actionBar) {
            actionBar.hide();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final Intent mainIntent = new Intent(MainActivity.this, ShareActivity.class);
                MainActivity.this.startActivity(mainIntent);
                if (!isFinishing()) {
                    MainActivity.this.finish();
                }
            }
        }, Constants.SPLASH_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_towards_left);
    }
    /*    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.slide_out_towards_left, R.anim.slide_in_from_right);
    }*/
}
