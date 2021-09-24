package in.slanglabs.sampleretailapp;

import android.app.Application;

import java.util.HashSet;
import java.util.Locale;

import in.slanglabs.assistants.retail.AssistantConfiguration;
import in.slanglabs.assistants.retail.AssistantSubDomain;
import in.slanglabs.assistants.retail.NavigationUserJourney;
import in.slanglabs.assistants.retail.OrderManagementUserJourney;
import in.slanglabs.assistants.retail.SearchUserJourney;
import in.slanglabs.assistants.retail.SlangRetailAssistant;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.sampleretailapp.Slang.SlangInterface;
import in.slanglabs.sampleretailapp.db.AppDatabase;

public class App extends Application {

    private Repository mRepository;

    @Override
    public void onCreate() {
        super.onCreate();
        AppExecutors mAppExecutors = new AppExecutors();

        HashSet<Locale> requestedLocales = new HashSet<>();
        requestedLocales.add(SlangLocale.LOCALE_ENGLISH_IN);
        requestedLocales.add(SlangLocale.LOCALE_HINDI_IN);
        AssistantConfiguration configuration = new AssistantConfiguration.Builder()
                .setRequestedLocales(requestedLocales)
                .setAssistantId("<AssistantId>")
                .setAPIKey("<APIKey>")
                .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                .setEnvironment(SlangRetailAssistant.Environment.STAGING)
                .build();

        SlangRetailAssistant.initialize(this, configuration);
        SlangRetailAssistant.setAppDefaultSubDomain(AssistantSubDomain.GROCERY);
        SearchUserJourney.disablePreserveContext();
        NavigationUserJourney.disablePreserveContext();
        OrderManagementUserJourney.disablePreserveContext();

        SlangInterface mSlangInterface = new SlangInterface(this);
        mRepository = new Repository(
                AppDatabase.getInstance(this,
                        mAppExecutors), mAppExecutors, mSlangInterface);
    }

    public Repository getRepository() {
        return mRepository;
    }

}
