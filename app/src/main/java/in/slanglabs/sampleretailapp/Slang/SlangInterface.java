package in.slanglabs.sampleretailapp.Slang;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.util.Log;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import in.slanglabs.assistants.retail.AssistantConfiguration;
import in.slanglabs.assistants.retail.AssistantError;
import in.slanglabs.assistants.retail.AssistantSubDomain;
import in.slanglabs.assistants.retail.NavigationInfo;
import in.slanglabs.assistants.retail.NavigationUserJourney;
import in.slanglabs.assistants.retail.OrderInfo;
import in.slanglabs.assistants.retail.OrderManagementUserJourney;
import in.slanglabs.assistants.retail.RetailUserJourney;
import in.slanglabs.assistants.retail.SearchInfo;
import in.slanglabs.assistants.retail.SearchUserJourney;
import in.slanglabs.assistants.retail.SlangRetailAssistant;
import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.sampleretailapp.App;
import in.slanglabs.sampleretailapp.BuildConfig;
import in.slanglabs.sampleretailapp.Repository;

public class SlangInterface {
    private final String TAG = "SlangInterface";

    private SearchUserJourney mSearchJourney;
    private OrderManagementUserJourney mOrderManagementJourney;
    private NavigationUserJourney mNavigationUserJourney;

    private boolean mIsAssistantClosed = false;

    public SlangInterface(Application application) {
//        init(application, BuildConfig.ASSISTANT_ID, BuildConfig.API_KEY);

        SlangRetailAssistant.setAction(new SlangRetailAssistant.Action() {

            @Override
            public SearchUserJourney.AppState onSearch(
                    SearchInfo searchInfo,
                    SearchUserJourney searchJourney) {

                Repository repository = ((App) (application)).getRepository();

                //Have a copy of the user journey object to perform operations on it.
                mSearchJourney = searchJourney;

                new Handler().post(() -> {

                    //Get instance of the repository and perform the logic for this user journey.
                    repository.onSearch(searchInfo);
                });

                //Return waiting state in indicate that the async operation is being performed.
                return SearchUserJourney.AppState.WAITING;
            }

            @Override
            public OrderManagementUserJourney.AppState onOrderManagement(
                    OrderInfo orderInfo,
                    OrderManagementUserJourney orderManagementJourney) {

                Repository repository = ((App) (application)).getRepository();

                //Have a copy of the user journey object to perform operations on it.
                mOrderManagementJourney = orderManagementJourney;
                new Handler().post(() -> {

                    //Get instance of the repository and perform the logic for this user journey.
                    repository.onOrder(orderInfo);
                });

                if (orderInfo.getAction() == OrderInfo.Action.CANCEL ||
                        orderInfo.getAction() == OrderInfo.Action.VIEW) {
                    //Return waiting state in indicate that the async operation is being performed.
                    return OrderManagementUserJourney.AppState.WAITING;
                } else {
                    //Return unsupported state to indicate that the app only supports CANCEL and VIEW user journey.
                    return OrderManagementUserJourney.AppState.UNSUPPORTED;
                }
            }

            @Override
            public NavigationUserJourney.AppState onNavigation(
                    NavigationInfo navigationInfo,
                    NavigationUserJourney navigationUserJourney) {

                Repository repository = ((App) (application)).getRepository();

                //Have a copy of the user journey object to perform operations on it.
                mNavigationUserJourney = navigationUserJourney;

                String targetString = navigationInfo.getTarget().toLowerCase();
                new Handler().post(() -> {

                    //Get instance of the repository and perform the logic for this user journey.
                    repository.onNavigation(targetString);
                });

                //Return waiting state in indicate that the async operation is being performed.
                return NavigationUserJourney.AppState.WAITING;
            }

            @Override
            public void onAssistantError(AssistantError assistantError) {
                Log.e(TAG, "Error: " + assistantError.getDescription());
            }
        });
        SlangRetailAssistant.setLifecycleObserver(new SlangRetailAssistant.LifecycleObserver() {
            @Override
            public void onAssistantInitSuccess() {
            }

            @Override
            public void onAssistantInitFailure(String s) {
            }

            @Override
            public void onAssistantInvoked() {
                mIsAssistantClosed = false;
            }

            @Override
            public void onAssistantClosed(boolean b) {
                Repository repository = ((App) (application)).getRepository();
                repository.setSelectedSearchItem(-1);
                repository.setCurrentSearchItem(null);
                repository.setCurrentOrderItem(null);
                mIsAssistantClosed = true;
            }

            @Override
            public void onAssistantLocaleChanged(Locale locale) {
            }

            @Override
            public boolean onUnrecognisedUtterance(String s) {
                return false;
            }

            @Override
            public void onUtteranceDetected(String utterance) {
            }

            @Override
            public void onOnboardingSuccess() {
            }

            @Override
            public void onOnboardingFailure() {

            }

            @Override
            public void onMicPermissionDenied() {

            }
        });
    }

    public void showTrigger(Activity activity) {
        //This method is to notify SlangRetailAssistant to show the trigger UI on this activity.
        SlangRetailAssistant.getUI().showTrigger(activity);
    }

    public void hideTrigger(Activity activity) {
        //This method is to notify SlangRetailAssistant to hide the trigger UI on this activity.
        SlangRetailAssistant.getUI().hideTrigger(activity);
    }

    public void startConversation(Activity activity) {
        //This method is to notify SlangRetailAssistant to start a new conversation.
        SlangRetailAssistant.startConversation(RetailUserJourney.SEARCH, activity, true);
    }

    //This method is to notify SlangRetailAssistant that the search async operation was successful.
    public void notifySearchSuccess() {
        if (mSearchJourney == null || mIsAssistantClosed) return;
        try {

            //Call the appropriate app state condition on the user journey object.
            mSearchJourney.setSuccess();

            //Notify the current appropriate app state for search.
            mSearchJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        mSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the search async operation has resulted in search item not specified.
    public void notifySearchItemNotSpecified() {
        if (mSearchJourney == null || mIsAssistantClosed) return;
        try {

            //Call the appropriate app state condition on the user journey object.
            mSearchJourney.setItemNotSpecified();

            //Notify the current appropriate app state for search.
            mSearchJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        mSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the search async operation has resulted in search item not found.
    public void notifySearchItemNotFound() {
        if (mSearchJourney == null || mIsAssistantClosed) return;
        try {

            //Call the appropriate app state condition on the user journey object.
            mSearchJourney.setItemNotFound();

            //Notify the current appropriate app state for search.
            mSearchJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        mSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation was successful.
    public void notifyAddToCartSuccess() {
        if (mSearchJourney == null || mIsAssistantClosed) return;
        try {

            //Optionally, trigger a new search user journey post reporting the app state.
            mSearchJourney.setRetailJourneyToContinue(RetailUserJourney.SEARCH, true);

            //Call the appropriate app state condition on the user journey object.
            mSearchJourney.setSuccess();

            //Notify the current appropriate app state for search.
            mSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        mSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation has resulted in item not found.
    public void notifyAddToCartItemNotFound() {
        if (mSearchJourney == null || mIsAssistantClosed) return;
        try {

            //Call the appropriate app state condition on the user journey object.
            mSearchJourney.setItemNotFound();

            //Notify the current appropriate app state for search.
            mSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        mSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation was successful.
    public void notifyAddToCartNeedDisambiguation() {
        if (mSearchJourney == null || mIsAssistantClosed) return;
        try {

            //Call the appropriate app state condition on the user journey object.
            mSearchJourney.setNeedDisambiguation();

            //Notify the current appropriate app state for search.
            mSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation was successful.
    public void notifyAddToCartNeedQuantity() {
        if (mSearchJourney == null || mIsAssistantClosed) return;
        try {

            //Call the appropriate app state condition on the user journey object.
            mSearchJourney.setNeedItemQuantity();

            //Notify the current appropriate app state for search.
            mSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order async operation was successful.
    public void notifyOrderManagementSuccess() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setSuccess();
        try {
            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation was unsuccessful.
    public void notifyOrderManagementFailure() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setFailure();

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation resulted in an empty list.
    public void notifyOrderManagementEmpty() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setOrdersEmpty();

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation resulted in an empty list.
    public void notifyOrderManagementCancelEmpty() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setOrdersEmpty();

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation resulted in invalid order item.
    public void notifyOrderNotFound(int orderIndex) {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setOrderNotFound(orderIndex);

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation needs to disambiguate the index position.
    public void notifyOrderManagementIndexRequiredView() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setOrderIndexRequired();

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order async operation needs to disambiguate the index position.
    public void notifyOrderManagementIndexRequiredCancel() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setOrderIndexRequired();

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in success.
    public void notifyOrderManagementCancelConfirmationSuccess() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setUserConfirmedCancel();

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation requested a cancel confirmation prompt.
    public void notifyOrderManagementCancelConfirmation(Integer index) {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        OrderManagementUserJourney.getContext().setOrderIndex(index);

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setConfirmationRequired();
        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in invalid order item.
    public void notifyOrderManagementCancelOrderNotFound(int index) {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setOrderNotFound(index);

        try {

            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in failure.
    public void notifyOrderManagementCancelFailure() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setFailure();
        try {
            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in failure.
    public void notifyOrderManagementCancelSuccess() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setSuccess();
        try {
            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order index required async operation resulted in denied.
    public void notifyOrderManagementCancelConfirmationDenied() {
        if (mOrderManagementJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the user journey object.
        mOrderManagementJourney.setUserDeniedCancel();
        try {
            //Notify the current appropriate app state for order management.
            mOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the navigation async operation resulted in success.
    public void notifyNavigationUserJourneySuccess() {
        if (mNavigationUserJourney == null) return;

        //Call the appropriate app state condition on the navigation journey object.
        mNavigationUserJourney.setNavigationSuccess();
        try {

            //Notify the current appropriate app state for navigation.
            mNavigationUserJourney.notifyAppState(NavigationUserJourney.AppState.NAVIGATION);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //This method is to notify SlangRetailAssistant that the navigation async operation resulted in failure.
    public void notifyNavigationUserJourneyFailure() {
        if (mNavigationUserJourney == null || mIsAssistantClosed) return;

        //Call the appropriate app state condition on the navigation journey object.
        mNavigationUserJourney.setNavigationFailure();
        try {

            //Notify the current appropriate app state for navigation.
            mNavigationUserJourney.notifyAppState(NavigationUserJourney.AppState.NAVIGATION);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isSlangActive() {
        return !mIsAssistantClosed;
    }

    public void trackAppEvent(String eventName, Map<String, String> eventMap) {
        SlangBuddy.trackAppEvent(eventName, eventMap);
    }

    private void init(Application application, String assistantId, String apiKey) {
        HashSet<Locale> requestedLocales = new HashSet<>();
        requestedLocales.add(SlangLocale.LOCALE_ENGLISH_IN);
        requestedLocales.add(SlangLocale.LOCALE_HINDI_IN);
        requestedLocales.add(SlangLocale.LOCALE_KANNADA_IN);
        requestedLocales.add(SlangLocale.LOCALE_ENGLISH_US);

        boolean disableOnBoardingLocaleSelection = false;
        Locale defaultLocale = SlangLocale.LOCALE_ENGLISH_IN;

        if(BuildConfig.FLAVOR.equalsIgnoreCase("bel")) {
            defaultLocale = SlangLocale.LOCALE_ENGLISH_US;
            disableOnBoardingLocaleSelection = true;
        }

        AssistantConfiguration configuration = new AssistantConfiguration.Builder()
                .setRequestedLocales(requestedLocales)
                .setAssistantId(assistantId)
                .disableOnboardingLocaleSelection(disableOnBoardingLocaleSelection)
                .setAPIKey(apiKey)
                .setDefaultLocale(defaultLocale)
                .setEnvironment(SlangRetailAssistant.Environment.STAGING)
                .build();

        SlangRetailAssistant.initialize(application, configuration);
        SlangRetailAssistant.setAppDefaultSubDomain(AssistantSubDomain.GROCERY);
        SearchUserJourney.disablePreserveContext();
        NavigationUserJourney.disablePreserveContext();
        OrderManagementUserJourney.disablePreserveContext();
    }

}
