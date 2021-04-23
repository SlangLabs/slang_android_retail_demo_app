package in.slanglabs.sampleretailapp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import in.slanglabs.sampleretailapp.Slang.SlangInterface;
import in.slanglabs.sampleretailapp.db.AppDatabase;

public class App extends Application {

    private Repository mRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppExecutors mAppExecutors = new AppExecutors();
        SlangInterface mSlangInterface = new SlangInterface(this);
        mSlangInterface.init(
                "AssistantID",
                "APIKey"
        );
        SharedPreferences mPrefs = this.getSharedPreferences("slang_sample_app",
                        Context.MODE_PRIVATE);
        mRepository = new Repository(
                AppDatabase.getInstance(this,
                        mAppExecutors), mAppExecutors, mPrefs, mSlangInterface);
    }

    public Repository getRepository() {
        return mRepository;
    }

}
