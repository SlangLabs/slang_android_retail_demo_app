package in.slanglabs.sampleretailapp.UI.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;

import in.slanglabs.sampleretailapp.Slang.SlangInterface;

public class SearchViewModel extends AppViewModel {

    public SearchViewModel(@NonNull Application application) {
        super(application);
        getSearchItem("");
    }

}
