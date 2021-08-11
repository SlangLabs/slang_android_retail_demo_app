package in.slanglabs.sampleretailapp;

import android.app.Application;

import in.slanglabs.sampleretailapp.Slang.SlangInterface;
import in.slanglabs.sampleretailapp.db.AppDatabase;

public class App extends Application {

    private Repository mRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppExecutors mAppExecutors = new AppExecutors();
        SlangInterface mSlangInterface = new SlangInterface(this);
        mRepository = new Repository(
                AppDatabase.getInstance(this,
                        mAppExecutors), mAppExecutors, mSlangInterface);
    }

    public Repository getRepository() {
        return mRepository;
    }

}
