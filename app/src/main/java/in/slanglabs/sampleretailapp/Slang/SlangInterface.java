package in.slanglabs.sampleretailapp.Slang;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import in.slanglabs.assistants.base.AssistantUIPosition;
import in.slanglabs.assistants.base.BaseUserJourney;
import in.slanglabs.assistants.retail.AssistantConfiguration;
import in.slanglabs.assistants.retail.AssistantError;
import in.slanglabs.assistants.retail.AssistantSubDomain;
import in.slanglabs.assistants.retail.AssistantUI;
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
import in.slanglabs.platform.prompt.SlangMessage;
import in.slanglabs.sampleretailapp.App;
import in.slanglabs.sampleretailapp.BuildConfig;
import in.slanglabs.sampleretailapp.Model.FeedbackItem;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.Repository;

public class SlangInterface {
    private final String TAG = "SlangInterface";
    private SearchUserJourney sSearchJourney;
    private OrderManagementUserJourney sOrderManagementJourney;
    private NavigationUserJourney sNavigationUserJourney;
    private final Application application;
    private boolean assistantClosed = false;

    private String utterances = "";

    public void init(String assitantId, String apiKey) {
        HashSet<Locale> requestedLocales = new HashSet<>();
        requestedLocales.add(SlangLocale.LOCALE_ENGLISH_IN);
        requestedLocales.add(SlangLocale.LOCALE_HINDI_IN);
        requestedLocales.add(SlangLocale.LOCALE_KANNADA_IN);

        AssistantConfiguration configuration = new AssistantConfiguration.Builder()
                .setRequestedLocales(requestedLocales)
                .setAssistantId(assitantId)
                .setAPIKey(apiKey)
                .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                .setEnvironment(SlangRetailAssistant.Environment.STAGING)
                .build();

        SlangRetailAssistant.initialize(application, configuration);
    }

    public SlangInterface(Application application) {
        this.application = application;
        SlangRetailAssistant.setAction(new SlangRetailAssistant.Action() {

            @Override
            public SearchUserJourney.AppState onSearch(
                    SearchInfo searchInfo,
                    SearchUserJourney searchJourney) {

                Repository repository = ((App) (application)).getRepository();

                //Clear other user journey contexts when the user journey has changed.
                OrderManagementUserJourney.getContext().clear();

                //Have a copy of the user journey object to perform operations on it.
                sSearchJourney = searchJourney;

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

                //Clear other user journey contexts when the user journey has changed.
                SearchUserJourney.getContext().clear();

                //Have a copy of the user journey object to perform operations on it.
                sOrderManagementJourney = orderManagementJourney;
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

                //Return waiting state in indicate that the async operation is being performed.
                SearchUserJourney.getContext().clear();
                OrderManagementUserJourney.getContext().clear();

                //Have a copy of the user journey object to perform operations on it.
                sNavigationUserJourney = navigationUserJourney;

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
                Repository repository = ((App) (application)).getRepository();
                repository.setSlangInitialized(true);
            }

            @Override
            public void onAssistantInitFailure(String s) {
                Log.d(TAG, "Initialization failure : " + s);
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(application.getApplicationContext(),
                        "Initialization failure : "+s,
                        Toast.LENGTH_LONG).show());
            }

            @Override
            public void onAssistantInvoked() {
                assistantClosed = false;
                Repository repository = ((App) (application)).getRepository();
                repository.setSlangInvoked(true);
            }

            @Override
            public void onAssistantClosed(boolean b) {
                assistantClosed = true;
                Repository repository = ((App) (application)).getRepository();
                repository.setSlangInvoked(false);
                repository.setSelectedSearchItem(-1);
                SearchUserJourney.getContext().clear();
                OrderManagementUserJourney.getContext().clear();
                NavigationUserJourney.getContext().clear();
                utterances = "";
            }

            @Override
            public void onAssistantLocaleChanged(Locale locale) {
                Repository repository = ((App) (application)).getRepository();
                repository.setSlangInitialized(true);
            }

            @Override
            public void onUnrecognisedUtterance(String s) {
                Repository repository = ((App) (application)).getRepository();
                repository.setSlangInitialized(true);
            }

            @Override
            public void onUtteranceDetected(String utterance) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (SlangInterface.this.utterances.equalsIgnoreCase("")) {
                        utterances = utterance;
                    } else {
                        utterances += "::" + utterance;
                    }
                });
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
        //This method is to notify SlangRetailAssistant to start a new user journey on top of this activity.
        SlangRetailAssistant.startConversation(RetailUserJourney.SEARCH, activity, true);
        Map<String, String> eventMetaData = new HashMap<>();
        eventMetaData.put("isAppTriggerClick", "true");
        trackAppEvent("startConversation", eventMetaData);
    }

    //This method is to notify SlangRetailAssistant that the search async operation was successful.
    public void notifySearchSuccess() {
        if (sSearchJourney == null || assistantClosed) return;
        try {

            SearchUserJourney.getContext().clear();

            //Call the appropriate app state condition on the user journey object.
            sSearchJourney.setSuccess();

            //Notify the current appropriate app state for search.
            sSearchJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        sSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the search async operation has resulted in search item not found.
    public void notifySearchItemNotFound() {
        if (sSearchJourney == null || assistantClosed) return;
        try {

            sSearchJourney.clearRetailJourneyToContinue();
            SearchUserJourney.getContext().clear();

            //Call the appropriate app state condition on the user journey object.
            sSearchJourney.setItemNotFound();

            //Notify the current appropriate app state for search.
            sSearchJourney.notifyAppState(SearchUserJourney.AppState.SEARCH_RESULTS);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        sSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation was successful.
    public void notifyAddToCartSuccess() {
        if (sSearchJourney == null || assistantClosed) return;
        try {

            SearchUserJourney.getContext().clear();

            //Call the appropriate app state condition on the user journey object.
            sSearchJourney.setSuccess();

            //Notify the current appropriate app state for search.
            sSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        sSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation has resulted in item not found.
    public void notifyAddToCartItemNotFound() {
        if (sSearchJourney == null || assistantClosed) return;
        try {

            sSearchJourney.clearRetailJourneyToContinue();
            SearchUserJourney.getContext().clear();

            //Call the appropriate app state condition on the user journey object.
            sSearchJourney.setItemNotFound();

            //Notify the current appropriate app state for search.
            sSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);
            SearchUserJourney.getContext().clear();

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
        sSearchJourney = null;
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation was successful.
    public void notifyAddToCartNeedDisambiguation() {
        if (sSearchJourney == null || assistantClosed) return;
        try {

            sSearchJourney.clearRetailJourneyToContinue();
            SearchUserJourney.getContext().clear();

            //Call the appropriate app state condition on the user journey object.
            sSearchJourney.setNeedDisambiguation();

            //Notify the current appropriate app state for search.
            sSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the add to cart async operation was successful.
    public void notifyAddToCartNeedQuantity() {
        if (sSearchJourney == null || assistantClosed) return;
        try {

            sSearchJourney.clearRetailJourneyToContinue();
            SearchUserJourney.getContext().clear();

            //Call the appropriate app state condition on the user journey object.
            sSearchJourney.setNeedItemQuantity();

            //Notify the current appropriate app state for search.
            sSearchJourney.notifyAppState(SearchUserJourney.AppState.ADD_TO_CART);

        } catch (Exception e) {
            Log.e(TAG, "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order async operation was successful.
    public void notifyOrderManagementSuccess() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setSuccess();
        try {
            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation was unsuccessful.
    public void notifyOrderManagementFailure() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setFailure();

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation resulted in an empty list.
    public void notifyOrderManagementEmpty() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setOrdersEmpty();
        OrderManagementUserJourney.getContext().clear();

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation resulted in an empty list.
    public void notifyOrderManagementCancelEmpty() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setOrdersEmpty();
        OrderManagementUserJourney.getContext().clear();

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation resulted in invalid order item.
    public void notifyOrderNotFound(int orderIndex) {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setOrderNotFound(orderIndex);

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order async operation needs to disambiguate the index position.
    public void notifyOrderManagementIndexRequiredView() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setOrderIndexRequired();

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.VIEW_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order async operation needs to disambiguate the index position.
    public void notifyOrderManagementIndexRequiredCancel() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setOrderIndexRequired();

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in success.
    public void notifyOrderManagementCancelConfirmationSuccess() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setUserConfirmedCancel();

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation requested a cancel confirmation prompt.
    public void notifyOrderManagementCancelConfirmation(Integer index) {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        OrderManagementUserJourney.getContext().setOrderIndex(index);

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setConfirmationRequired();
        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }
    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in invalid order item.
    public void notifyOrderManagementCancelOrderNotFound(int index) {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setOrderNotFound(index);

        try {

            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in failure.
    public void notifyOrderManagementCancelFailure() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setFailure();
        try {
            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order cancel async operation resulted in failure.
    public void notifyOrderManagementCancelSuccess() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setSuccess();
        try {
            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the order index required async operation resulted in denied.
    public void notifyOrderManagementCancelConfirmationDenied() {
        if (sOrderManagementJourney == null || assistantClosed) return;

        sOrderManagementJourney.clearRetailJourneyToContinue();
        OrderManagementUserJourney.getContext().clear();

        //Call the appropriate app state condition on the user journey object.
        sOrderManagementJourney.setUserDeniedCancel();
        try {
            //Notify the current appropriate app state for order management.
            sOrderManagementJourney
                    .notifyAppState(OrderManagementUserJourney.AppState.CANCEL_ORDER);
        } catch (Exception e) {
            Log.e("OrderActivity", "" + e.getLocalizedMessage());
        }

    }

    //This method is to notify SlangRetailAssistant that the navigation async operation resulted in success.
    public void notifyNavigationUserJourneySuccess() {
        if (sNavigationUserJourney == null) return;

        NavigationUserJourney.getContext().clear();

        //Call the appropriate app state condition on the navigation journey object.
        sNavigationUserJourney.setNavigationSuccess();
        try {

            //Notify the current appropriate app state for navigation.
            sNavigationUserJourney.notifyAppState(NavigationUserJourney.AppState.NAVIGATION);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //This method is to notify SlangRetailAssistant that the navigation async operation resulted in failure.
    public void notifyNavigationUserJourneyFailure() {
        if (sNavigationUserJourney == null || assistantClosed) return;

        sNavigationUserJourney.clearRetailJourneyToContinue();
        NavigationUserJourney.getContext().clear();

        //Call the appropriate app state condition on the navigation journey object.
        sNavigationUserJourney.setNavigationFailure();
        try {

            //Notify the current appropriate app state for navigation.
            sNavigationUserJourney.notifyAppState(NavigationUserJourney.AppState.NAVIGATION);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //This method is to notify SlangRetailAssistant to clear the current search context.
    //Calling this method will clear the existing context and will start a fresh context from the next search journey
    public void clearSearchContext() {
        SearchUserJourney.getContext().clear();
    }

    //This method is to notify SlangRetailAssistant to clear the current order management context.
    //Calling this method will clear the existing context and will start a fresh context from the next order management journey
    public void clearOrderManagementContext() {
        OrderManagementUserJourney.getContext().clear();
    }


    public void trackAppEvent(String eventName, Map<String, String> eventMap) {
        SlangBuddy.trackAppEvent(eventName, eventMap);
    }

}
