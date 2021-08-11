package in.slanglabs.sampleretailapp.UI.ViewModel;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import in.slanglabs.assistants.retail.OrderInfo;
import in.slanglabs.assistants.retail.SearchUserJourney;
import in.slanglabs.sampleretailapp.App;
import in.slanglabs.sampleretailapp.Model.CartItemOffer;
import in.slanglabs.sampleretailapp.Model.FeedbackItem;
import in.slanglabs.sampleretailapp.Model.FilterOptions;
import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ItemListUIModel;
import in.slanglabs.sampleretailapp.Model.ItemOfferCart;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.OfferItemCart;
import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.Repository;
import in.slanglabs.sampleretailapp.SingleLiveEvent;
import in.slanglabs.sampleretailapp.Slang.SlangInterface;

public class AppViewModel extends AndroidViewModel {

    private final Repository mRepository;
    private String mCurrentSearchTerm;
    private final SlangInterface mSlangInterface;
    private FilterOptions mFilterOptions;

    private boolean mIsSearchRequestComplete = false;
    private boolean mIsOrderRequestComplete = false;
    private boolean mIsOrderItemRequestComplete = false;

    private final MediatorLiveData<List<ItemListUIModel>> mSearchForNameMediator =
            new MediatorLiveData<>();
    private final MediatorLiveData<List<OrderItem>> mOrdersMediator =
            new MediatorLiveData<>();
    private final MediatorLiveData<OrderItem> mOrderItemMediator =
            new MediatorLiveData<>();

    private LiveData<List<ItemOfferCart>> mSearchForName =
            new MutableLiveData<>();
    private LiveData<List<OrderItem>> mOrderItems =
            new MutableLiveData<>();
    private LiveData<OrderItem> mOrderItem =
            new MutableLiveData<>();

    private final SingleLiveEvent<Boolean> showCancelConfirmation = new SingleLiveEvent<Boolean>();

    public AppViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((App) application).getRepository();
        mFilterOptions = new FilterOptions();
        mSlangInterface = mRepository.getSlangInterface();
    }

    //Functions/Methods related to items.
    public LiveData<List<ItemOfferCart>> getItems() {
        return mRepository.getItems();
    }

    public LiveData<ItemOfferCart> getItemForId(int id) {
        return mRepository.getItemForId(id);
    }

    //Functions/Methods related to offers.
    public LiveData<List<OfferItemCart>> getOfferItems() {
        return mRepository.getOfferItems();
    }

    //Functions/Methods related to cart.
    public LiveData<List<CartItemOffer>> getCartItems() {
        return mRepository.getCartItems();
    }

    public void clearCart() {
        mRepository.clearCart();
    }

    public void addItem(Item item, boolean uiAction) {
        mRepository.addItemToCart(item, 1, uiAction);
    }

    public void addItem(Item item, int quantity) {
        mRepository.addItemToCart(item, quantity, false);
    }

    public void removeItem(Item item) {
        mRepository.removeItemFromCart(item);
    }

    //Functions/Methods related to orders.
    public void makeOrderRequest() {
        mIsOrderRequestComplete = false;
        if (mOrderItems != null) {
            mOrdersMediator.removeSource(mOrderItems);
        }
        mOrderItems = mRepository.getOrderItems();
        mOrdersMediator.addSource(mOrderItems, new Observer<List<OrderItem>>() {
            @Override
            public void onChanged(List<OrderItem> orderItems) {
                OrderInfo orderInfo = mRepository.getCurrentOrderItem();
                if(orderInfo != null && !mIsOrderRequestComplete)  {
                    if (orderItems.size() == 0) {
                        if(orderInfo.getAction() == OrderInfo.Action.VIEW) {
                            mSlangInterface.notifyOrderManagementEmpty();
                        }
                        else {
                            mSlangInterface.notifyOrderManagementCancelEmpty();
                        }
                    } else {
                        if(orderInfo.getAction() == OrderInfo.Action.VIEW) {
                            mSlangInterface.notifyOrderManagementSuccess();
                        }
                        else {
                            mSlangInterface.notifyOrderManagementIndexRequiredCancel();
                        }
                    }
                }
                mOrdersMediator.postValue(orderItems);
                mIsOrderRequestComplete = true;
            }
        });
    }

    public void makeOrderItemRequest(String orderId) {
        mIsOrderItemRequestComplete = false;
        if (mOrderItem != null) {
            mOrderItemMediator.removeSource(mOrderItem);
        }
        mOrderItem = mRepository.getOrderItem(orderId);
        mOrderItemMediator.addSource(mOrderItem, new Observer<OrderItem>() {
            @Override
            public void onChanged(OrderItem orderItem) {
                OrderInfo orderInfo = mRepository.getCurrentOrderItem();
                if(orderInfo != null && !mIsOrderItemRequestComplete)  {
                        if(orderInfo.getAction() == OrderInfo.Action.VIEW) {
                            mSlangInterface.notifyOrderManagementSuccess();
                        }
                        else {
                            if(orderInfo.getCancelConfirmationStatus()
                                    == OrderInfo.CancelConfirmationStatus.UNKNOWN) {
                                if(orderItem.active) {
                                    showCancelConfirmation.postValue(true);
                                }
                                else {
                                    mSlangInterface.notifyOrderManagementCancelSuccess();
                                }
                            }
                            else if (orderInfo.getCancelConfirmationStatus()
                                    == OrderInfo.CancelConfirmationStatus.CONFIRMED) {
                                showCancelConfirmation.postValue(false);
                            }
                            else {
                                showCancelConfirmation.postValue(false);
                            }
                        }
                    }
                mOrderItemMediator.postValue(orderItem);
                mIsOrderItemRequestComplete = true;
            }
        });
    }

    public LiveData<List<OrderItem>> getOrderItems() {
        return mOrdersMediator;
    }

    public LiveData<OrderItem> getOrderItem() {
        return mOrderItemMediator;
    }

    public void addOrderItem(OrderItem orderItem) {
        mRepository.addOrderItem(orderItem);
    }

    public void removeOrderItem(OrderItem orderItem) {
        mRepository.removeOrderItem(orderItem);
    }

    public void holdOrderItem(OrderItem orderItem) {
        mSlangInterface.notifyOrderManagementCancelConfirmationDenied();
    }

    public void cancelOrderConfirmation(int index) {
        if(!mSlangInterface.isSlangActive()) {
            return;
        }
        mRepository.cancelOrderConfirmation(index, true);
    }

    public SingleLiveEvent<Boolean> getShowCancelOrderConfirmation() {
        return showCancelConfirmation;
    }

    //Functions/Methods related to search.
    public String getCurrentSearchTerm() {
        return mCurrentSearchTerm;
    }

    public void setCurrentSearchTerm(String searchTerm) {
        this.mCurrentSearchTerm = searchTerm;
    }

    public LiveData<List<ItemListUIModel>> getSearchForNameMediator() {
        return mSearchForNameMediator;
    }

    public void setSelectedSearchItem(Item item) {
        if (mRepository.getCurrentSearchItem() == null) {
            return;
        }
        if (mSlangInterface.isSlangActive()) {
            mRepository.setSelectedSearchItem(item.itemId);
            if (mRepository.getCurrentSearchItem().quantity == 0) {

                //Notify SlangRetailAssistant that the search view journey add to cart requires quantity to be specified.
                mSlangInterface.notifyAddToCartNeedQuantity();
            } else {

                //Add item to the cart which will internally report that the item is added to the cart.
                addItem(item, mRepository.getCurrentSearchItem().quantity);
            }
        }
    }

    public void getSearchItem(String searchItem) {
        mRepository.setCurrentSearchItem(null);
        if (mSearchForName != null) {
            mSearchForNameMediator.removeSource(mSearchForName);
        }
        mSearchForName = mRepository.getItemsOfferCartForNameSync(searchItem,
                mRepository.getmListType().getValue(), mFilterOptions);
        mSearchForNameMediator.addSource(mSearchForName, itemOfferCarts -> {
            ArrayList<ItemListUIModel> itemListUIModels = new ArrayList<>();
            for(ItemOfferCart itemOfferCart:itemOfferCarts) {
                ItemListUIModel itemListUIModel = new ItemListUIModel();
                itemListUIModel.itemOfferCart = itemOfferCart;
                itemListUIModels.add(itemListUIModel);
            }
            mSearchForNameMediator.postValue(itemListUIModels);
        });
    }

    public void getSearchItem(SearchItem searchInfo) {
        mIsSearchRequestComplete = false;
        mRepository.setCurrentSearchItem(null);
        if (searchInfo == null) {
            return;
        }

        mRepository.setCurrentSearchItem(searchInfo);
        if (mSearchForName != null) {
            mSearchForNameMediator.removeSource(mSearchForName);
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (!searchInfo.name.isEmpty()) {
            stringBuilder.append(searchInfo.name);
        }
        if (!searchInfo.brandName.isEmpty()) {
            stringBuilder.append(" ");
            stringBuilder.append(searchInfo.brandName);
        }

        String name = stringBuilder.toString();

        if(!searchInfo.size.isEmpty()) {
            int currentSize = Integer.parseInt(
                    searchInfo.size.replaceAll("[^0-9]", "")
            );
            if (currentSize % 5 == 0) {
                mFilterOptions.setSizes(new ArrayList<>(Arrays.asList("5kg")));
            } else if (currentSize % 2 == 0) {
                mFilterOptions.setSizes(new ArrayList<>(Arrays.asList("2kg")));
            } else {
                mFilterOptions.setSizes(new ArrayList<>(Arrays.asList("1kg")));
            }
        }

        mSearchForName = mRepository.getItemsOfferCartForNameSync(name,
                mRepository.getmListType().getValue(), mFilterOptions);
        mFilterOptions.clear();
        mSearchForNameMediator.addSource(mSearchForName, itemOfferCarts -> {
            boolean shouldHightlightItem = false;
            if (itemOfferCarts.size() == 0) {
                if (!searchInfo.name.equalsIgnoreCase(searchInfo.productName) &&
                        !searchInfo.productName.equalsIgnoreCase("")) {
                    searchInfo.name = searchInfo.productName;
                    getSearchItem(searchInfo);
                    return;
                }
                if (!searchInfo.brandName.equalsIgnoreCase("")) {
                    searchInfo.brandName = "";
                    getSearchItem(searchInfo);
                    return;
                }

                if (!mIsSearchRequestComplete) {
                    if (searchInfo.isAddToCart ||
                            !searchInfo.size.equalsIgnoreCase("")) {

                        //Notify SlangRetailAssistant that search user journey has resulted in add to cart item not found.
                        mSlangInterface.notifyAddToCartItemNotFound();
                    } else {
                        //Notify SlangRetailAssistant that search user journey has resulted in add to cart item not found.
                        mSlangInterface.notifySearchItemNotFound();
                    }
                }

            } else if (!mIsSearchRequestComplete) {
                if (searchInfo.isAddToCart ||
                        !searchInfo.size.equalsIgnoreCase("")) {
                    if(itemOfferCarts.size() == 1 || itemOfferCarts.get(0).item.confidence > 90) {
                        if (!searchInfo.size.isEmpty()) {
                            int size = Integer.parseInt(
                                    itemOfferCarts.get(0).item.size.replaceAll("[^0-9]", ""));
                            int currentSize = Integer.parseInt(
                                    searchInfo.size.replaceAll("[^0-9]", "")
                            );
                            int numbers = currentSize / size;

                            int quantity = searchInfo.quantity == 0 ? 1 : searchInfo.quantity;
                            if(itemOfferCarts.size() > 1) {
                                new Handler().postDelayed(() -> addItem(itemOfferCarts.get(0).item, quantity * numbers),700);
                                shouldHightlightItem = true;
                            }
                            else {
                                addItem(itemOfferCarts.get(0).item, quantity * numbers);
                            }
                        }
                        else {
                            mSlangInterface.notifyAddToCartNeedQuantity();
                        }
                    }
                    else if(itemOfferCarts.size() == 3) {
                        if (searchInfo.size.isEmpty()) {
                            mSlangInterface.notifyAddToCartNeedQuantity();
                        }
                    }
                    else if (itemOfferCarts.size() > 1) {
                        //Notify SlangRetailAssistant that the search view journey add to cart needs to disambiguate the search item.
                        mSlangInterface.notifyAddToCartNeedDisambiguation();
                    } else {
                        if (searchInfo.quantity != 0) {
                            addItem(itemOfferCarts.get(0).item, searchInfo.quantity);
                        } else if (!searchInfo.size.isEmpty()) {
                            addItem(itemOfferCarts.get(0).item, 1);
                        } else {
                            mSlangInterface.notifyAddToCartNeedQuantity();
                        }
                    }
                } else {
                    mSlangInterface.notifySearchSuccess();
                }
            }

            ArrayList<ItemListUIModel> itemListUIModels = new ArrayList<>();
            for(ItemOfferCart itemOfferCart:itemOfferCarts) {
                ItemListUIModel itemListUIModel = new ItemListUIModel();
                itemListUIModel.itemOfferCart = itemOfferCart;
                itemListUIModels.add(itemListUIModel);
            }
            if(shouldHightlightItem && itemListUIModels.size() > 0) {
                itemListUIModels.get(0).shouldHightLight = true;
            }

            mSearchForNameMediator.postValue(itemListUIModels);
            mIsSearchRequestComplete = true;
        });
    }

    //Functions/Methods related to list type.
    public void setListType(@ListType String listType) {
        mRepository.switchCategory(listType);
        mFilterOptions.clear();
    }

    public LiveData<String> getListType() {
        return mRepository.getmListType();
    }

    //Method related to activity navigation
    public SingleLiveEvent<Pair<Class, Bundle>> getActivityToStart() {
        return mRepository.getmActivityToStart();
    }

    //Method related to triggering a new slang session
    public SingleLiveEvent<Boolean> startSlangSession() {
        return mRepository.getStartSlangSession();
    }

    //Method to obtain the slang interface
    public SlangInterface getSlangInterface() {
        return mRepository.getSlangInterface();
    }

    //Method related to other misc operations
    public void sendAppFeedBack(FeedbackItem feedbackItem) {
        mRepository.sendFeedbackItem(feedbackItem);
    }

    public LiveData<List<String>> getItemBrands(@ListType String listType) {
        return mRepository.getItemBrands(listType);
    }

    public LiveData<List<String>> getItemSizes(@ListType String listType) {
        return mRepository.getItemSizes(listType);
    }

    public FilterOptions getFilterOptions() {
        return mFilterOptions;
    }

    public void setFilterOptions(FilterOptions mFilterOptions) {
        this.mFilterOptions = mFilterOptions;
    }

}
