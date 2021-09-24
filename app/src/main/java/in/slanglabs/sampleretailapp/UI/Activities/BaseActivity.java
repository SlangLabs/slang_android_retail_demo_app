package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import in.slanglabs.assistants.base.BaseUI;
import in.slanglabs.assistants.retail.AssistantError;
import in.slanglabs.assistants.retail.NavigationInfo;
import in.slanglabs.assistants.retail.NavigationUserJourney;
import in.slanglabs.assistants.retail.OrderInfo;
import in.slanglabs.assistants.retail.OrderManagementUserJourney;
import in.slanglabs.assistants.retail.SearchInfo;
import in.slanglabs.assistants.retail.SearchUserJourney;
import in.slanglabs.assistants.retail.SlangRetailAssistant;
import in.slanglabs.sampleretailapp.App;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.Repository;
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

        SlangRetailAssistant.setAction(new SlangRetailAssistant.Action() {
            @Override
            public SearchUserJourney.AppState onSearch(SearchInfo searchInfo, SearchUserJourney searchUserJourney) {

                Intent intent = new Intent(BaseActivity.this,
                        SearchListActivity.class);
                SearchItem searchItem = new SearchItem();
                searchItem.name = searchInfo.getItem().getDescription();
                intent.putExtra("search_term", searchItem);
                startActivity(intent);

                searchUserJourney.setSuccess();
                return SearchUserJourney.AppState.SEARCH_RESULTS;
            }

            @Override
            public OrderManagementUserJourney.AppState onOrderManagement(
                    OrderInfo orderInfo,
                    OrderManagementUserJourney orderManagementJourney) {

                Repository repository = ((App)
                        (BaseActivity.this.getApplication())).getRepository();

                if (orderInfo.getAction() == OrderInfo.Action.VIEW) {

                    //If orderInfo is 0 then it means we need to show all the orders.
                    if (orderInfo.getIndex() == 0) {
                        Intent intent = new Intent(BaseActivity.this, OrderActivity.class);
                        BaseActivity.this.startActivity(intent);
                        //If total orders are 0 set app state as orders not found
                        if (repository.getTotalOrders() == 0) {
                            orderManagementJourney.setOrderNotFound(0);
                        } else {
                            orderManagementJourney.setSuccess();
                        }
                    } else {
                        //If total orders are 0 set app state as orders not found
                        if (repository.getTotalOrders() == 0) {
                            orderManagementJourney.setOrderNotFound(0);
                        } else {
                            String orderId = repository.getOrderIdForIndex(orderInfo.getIndex());
                            if (orderId != null) {
                                Intent intent = new Intent(
                                        BaseActivity.this, OrderItemsActivity.class);
                                intent.putExtra("orderItemId", orderId);
                                BaseActivity.this.startActivity(intent);
                                //If the respective order is available, then we set success.
                                orderManagementJourney.setSuccess();
                            } else {
                                //If the respective order is not available, then we set not available.
                                orderManagementJourney.setOrderNotFound(orderInfo.getIndex());
                            }
                        }
                    }
                    return OrderManagementUserJourney.AppState.VIEW_ORDER;
                }

                return OrderManagementUserJourney.AppState.UNSUPPORTED;
            }

            @Override
            public NavigationUserJourney.AppState onNavigation(
                    NavigationInfo navigationInfo,
                    NavigationUserJourney navigationUserJourney) {

                switch (navigationInfo.getTarget()) {
                    case "back":
                        BaseActivity.this.onBackPressed();
                        navigationUserJourney.setNavigationSuccess();
                        break;
                    case "home":
                        BaseActivity.this.startActivity(
                                new Intent(BaseActivity.this,
                                        SearchListActivity.class)
                        );
                        navigationUserJourney.setNavigationSuccess();
                        break;
                    case "cart":
                        BaseActivity.this.startActivity(
                                new Intent(BaseActivity.this,
                                        CartActivity.class)
                        );
                        navigationUserJourney.setNavigationSuccess();
                        break;
                    case "order":
                        BaseActivity.this.startActivity(
                                new Intent(BaseActivity.this,
                                        OrderActivity.class)
                        );
                        navigationUserJourney.setNavigationSuccess();
                        break;
                    case "checkout":
                        BaseActivity.this.startActivity(
                                new Intent(BaseActivity.this,
                                        CheckOutActivity.class)
                        );
                        navigationUserJourney.setNavigationSuccess();
                        break;
                    case "offers":
                        BaseActivity.this.startActivity(
                                new Intent(BaseActivity.this,
                                        OffersActivity.class)
                        );
                        navigationUserJourney.setNavigationSuccess();
                        break;
                    default:
                        navigationUserJourney.setNavigationFailure();
                }

                return NavigationUserJourney.AppState.NAVIGATION;
            }

            @Override
            public void onAssistantError(AssistantError assistantError) {

            }
        });
    }

}
