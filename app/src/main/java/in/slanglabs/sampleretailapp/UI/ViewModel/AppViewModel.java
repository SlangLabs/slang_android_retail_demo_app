package in.slanglabs.sampleretailapp.UI.ViewModel;

import android.app.Application;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import ca.rmen.porterstemmer.PorterStemmer;
import in.slanglabs.sampleretailapp.App;
import in.slanglabs.sampleretailapp.Model.CartItemOffer;
import in.slanglabs.sampleretailapp.Model.FeedbackItem;
import in.slanglabs.sampleretailapp.Model.FilterOptions;
import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ItemOfferCart;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.OfferItemCart;
import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.Repository;
import in.slanglabs.sampleretailapp.SingleLiveEvent;
import in.slanglabs.sampleretailapp.Slang.SlangInterface;

import java.util.List;

public class AppViewModel extends AndroidViewModel {

    private final Repository mRepository;
    private String mCurrentSearchTerm;
    private SearchItem mSearchItem;

    private final MediatorLiveData<List<ItemOfferCart>> mSearchForNameMediator =
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

    private FilterOptions filterOptions;

    public AppViewModel(@NonNull Application application) {
        super(application);
        mRepository = ((App) application).getRepository();
        filterOptions = new FilterOptions();
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

    public void addItem(Item item) {
        mRepository.addItemToCart(item, 1);
    }

    public void addItem(Item item, int quantity) {
        mRepository.addItemToCart(item, quantity);
    }

    public void removeItem(Item item) {
        mRepository.removeItemFromCart(item);
    }

    //Functions/Methods related to orders.
    public void makeOrderRequest() {
        if (mOrderItems != null) {
            mOrdersMediator.removeSource(mOrderItems);
        }
        mOrderItems = mRepository.getOrderItems();
        mOrdersMediator.addSource(mOrderItems, mOrdersMediator::postValue);
    }

    public void makeOrderItemRequest(String orderId) {
        if (mOrderItem != null) {
            mOrderItemMediator.removeSource(mOrderItem);
        }
        mOrderItem = mRepository.getOrderItem(orderId);
        mOrderItemMediator.addSource(mOrderItem, mOrderItemMediator::postValue);
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

    //Functions/Methods related to search.
    public String getCurrentSearchTerm() {
        return mCurrentSearchTerm;
    }

    public void setSearchItem(SearchItem searchTerm) {
        this.mSearchItem = searchTerm;
    }

    public SearchItem getCurrentSearchItem() {
        return mSearchItem;
    }

    public void setCurrentSearchTerm(String searchTerm) {
        this.mCurrentSearchTerm = searchTerm;
    }

    public LiveData<List<ItemOfferCart>> getSearchForNameMediator() {
        return mSearchForNameMediator;
    }

    public void setSelectedSearchItem(int itemId) {
        mRepository.setSelectedSearchItem(itemId);
    }

    public void getSearchItem(String searchItem) {
        if (mSearchForName != null) {
            mSearchForNameMediator.removeSource(mSearchForName);
        }
        mSearchForName = mRepository.getItemsOfferCartForNameSync(searchItem,
                mRepository.getListType().getValue(), filterOptions);
        mSearchForNameMediator.addSource(mSearchForName, itemOfferCarts -> {
            if (mRepository.getIsDbCreated().getValue() != null && mRepository.getIsDbCreated().getValue()) {
                mSearchForNameMediator.postValue(itemOfferCarts);
            }
        });
    }

    public void getSearchItem(SearchItem searchInfo) {
        if (mSearchForName != null) {
            mSearchForNameMediator.removeSource(mSearchForName);
        }
        String name = "";
        if (searchInfo != null) {
            StringBuilder stringBuilder = new StringBuilder();
            if (!searchInfo.name.isEmpty()) {
                PorterStemmer porterStemmer = new PorterStemmer();
                String stem = porterStemmer.stemWord(searchInfo.name);
                stringBuilder.append(stem);
            }
            if (!searchInfo.brandName.isEmpty()) {
                stringBuilder.append(" ");
                stringBuilder.append(searchInfo.brandName);
            }
            if (!searchInfo.size.isEmpty()) {
                stringBuilder.append(" ");
                stringBuilder.append(searchInfo.size);
            }
            name = stringBuilder.toString();
        }
        mSearchForName = mRepository.getItemsOfferCartForNameSync(name,
                mRepository.getListType().getValue(), filterOptions);
        String finalName = name;
        mSearchForNameMediator.addSource(mSearchForName, itemOfferCarts -> {
            if (mRepository.getIsDbCreated().getValue() != null && mRepository.getIsDbCreated().getValue()) {
                if (itemOfferCarts.size() == 0) {
                    if (searchInfo != null) {
                        if (!searchInfo.size.equalsIgnoreCase("")) {
                            searchInfo.size = "";
                            getSearchItem(searchInfo);
                            return;
                        }
                        if (!searchInfo.brandName.equalsIgnoreCase("")) {
                            searchInfo.brandName = "";
                            getSearchItem(searchInfo);
                            return;
                        }
                    }
                }
                if(itemOfferCarts.size() == 0 && finalName.equalsIgnoreCase("")) {
                    return;
                }
                mSearchForNameMediator.postValue(itemOfferCarts);
            }
        });
    }

    public void sendAppFeedBack(FeedbackItem feedbackItem) {
        mRepository.sendFeedbackItem(feedbackItem);
    }

    public void showFeedBackFragment(FeedbackItem feedbackItem) {
        mRepository.setShowFeedBackFragment(feedbackItem);
    }

    public LiveData<List<String>> getItemBrands(@ListType String listType) {
        return mRepository.getItemBrands(listType);
    }

    public LiveData<List<String>> getItemSizes(@ListType String listType) {
        return mRepository.getItemSizes(listType);
    }

    public LiveData<List<String>> getItemColors(@ListType String listType) {
        return mRepository.getItemColors(listType);
    }

    public LiveData<List<String>> getItemCategories(@ListType String listType) {
        return mRepository.getItemCategories(listType);
    }

    public LiveData<List<String>> getItemGenders(@ListType String listType) {
        return mRepository.getItemGenders(listType);
    }

    public FilterOptions getFilterOptions() {
        return filterOptions;
    }

    public void setFilterOptions(FilterOptions filterOptions) {
        this.filterOptions = filterOptions;
    }

    //Functions/Methods related to list type.
    public void setListType(@ListType String listType) {
        setCurrentSearchTerm("");
        setSearchItem(null);
        getFilterOptions().clear();
        mRepository.switchCategory(listType);
        filterOptions.clear();
    }

    public LiveData<String> getListType() {
        return mRepository.getListType();
    }

    public LiveData<Boolean> getIsSlangInitialized() {
        return mRepository.getSlangInitiailzed();
    }

    public LiveData<Boolean> getIsSlangInvoked() {
        return mRepository.getIsSlangInvoked();
    }

    //Method related to activity navigation
    public SingleLiveEvent<Pair<Class, Bundle>> getActivityToStart() {
        return mRepository.getActivityToStart();
    }

    //Method to obtain the slang interface
    public SlangInterface getSlangInterface() {
        return mRepository.getSlangInterface();
    }

    //Misc
    public SingleLiveEvent<FeedbackItem> getFeedbackFragment() {
        return mRepository.getFeedbackFragment();
    }

    public void cancelOrderConfirmation(int index) {
        mRepository.cancelOrderConfirmation(index);
    }
}
