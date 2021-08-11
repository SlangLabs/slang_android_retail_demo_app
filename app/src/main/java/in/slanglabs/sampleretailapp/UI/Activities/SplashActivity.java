package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import in.slanglabs.sampleretailapp.R;

public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(() -> {
            Intent mainIntent = new Intent(SplashActivity.this, SearchListActivity.class);
            SplashActivity.this.startActivity(mainIntent);
            SplashActivity.this.finish();
        }, 3000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAppViewModel.getSlangInterface().hideTrigger(this);
    }
}