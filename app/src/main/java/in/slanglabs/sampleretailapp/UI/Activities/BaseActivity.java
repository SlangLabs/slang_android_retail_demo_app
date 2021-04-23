package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import in.slanglabs.sampleretailapp.Model.FeedbackItem;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.Slang.SlangInterface;
import in.slanglabs.sampleretailapp.UI.Fragments.FeedbackDialogFragment;
import in.slanglabs.sampleretailapp.UI.Fragments.SearchDialogFragment;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

public class BaseActivity extends AppCompatActivity {

    AppViewModel appViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appViewModel = new ViewModelProvider(this).get(
                AppViewModel.class);

        appViewModel.getActivityToStart().observe(this, classBundlePair -> {
            if (classBundlePair.first == null) {
                //If first param is null, then the target view is the back page hence we just need to finish the
                //current activity and notify a success.

                BaseActivity.this.finish();

                //Notify SlangRetailAssistant that the navigation user journey was successful.
                appViewModel.getSlangInterface().notifyNavigationUserJourneySuccess();
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

        appViewModel.getFeedbackFragment().observe(this, this::showFeedbackFragment);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
            if (intent.getBooleanExtra("is_voice_navigation", false)) {

            //Notify SlangRetailAssistant that the navigation user journey was successful.
            appViewModel.getSlangInterface().notifyNavigationUserJourneySuccess();
        }
    }

    private void showFeedbackFragment(FeedbackItem feedbackItem) {
        FeedbackDialogFragment newFragment = FeedbackDialogFragment.newInstance(feedbackItem);
        if(feedbackItem.isPositiveFeedback != null) {
            newFragment.setCancelable(false);
        }
        else {
            newFragment.setCancelable(true);
        }
        newFragment.show(getSupportFragmentManager(), "FeedbackDialogFragment");
    }

}
