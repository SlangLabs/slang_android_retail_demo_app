package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import in.slanglabs.assistants.base.BaseUI;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

public class BaseActivity extends AppCompatActivity {

    AppViewModel mAppViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAppViewModel = new ViewModelProvider(this).get(
                AppViewModel.class);

        mAppViewModel.getActivityToStart().observe(this, classBundlePair -> {
            if (classBundlePair.first == null) {
                //If first param is null, then the target view is the back page hence we just need to finish the
                //current activity and notify a success.

                BaseActivity.this.finish();

                //Notify SlangRetailAssistant that the navigation user journey was successful.
                mAppViewModel.getSlangInterface().notifyNavigationUserJourneySuccess();
                return;
            }
            Intent intent = new Intent(BaseActivity.this,
                    classBundlePair.first);
            if (classBundlePair.second != null) {
                intent.putExtras(classBundlePair.second);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        mAppViewModel.startSlangSession().observe(this, aBoolean -> {
            if(aBoolean != null) {
                mAppViewModel.getSlangInterface().startConversation(BaseActivity.this);
            }
        });

    }

}
