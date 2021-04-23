package in.slanglabs.sampleretailapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import ca.rmen.porterstemmer.PorterStemmer;
import in.slanglabs.assistants.retail.OrderInfo;
import in.slanglabs.assistants.retail.SearchInfo;
import in.slanglabs.sampleretailapp.Model.CartItem;
import in.slanglabs.sampleretailapp.Model.CartItemOffer;
import in.slanglabs.sampleretailapp.Model.FeedbackItem;
import in.slanglabs.sampleretailapp.Model.FilterOptions;
import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ItemOfferCart;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.Offer;
import in.slanglabs.sampleretailapp.Model.OfferItemCart;
import in.slanglabs.sampleretailapp.Model.OrderBy;
import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.Model.PriceRange;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.Slang.SlangInterface;
import in.slanglabs.sampleretailapp.UI.Activities.CartActivity;
import in.slanglabs.sampleretailapp.UI.Activities.CheckOutActivity;
import in.slanglabs.sampleretailapp.UI.Activities.OffersActivity;
import in.slanglabs.sampleretailapp.UI.Activities.OrderActivity;
import in.slanglabs.sampleretailapp.UI.Activities.OrderItemsActivity;
import in.slanglabs.sampleretailapp.UI.Activities.SearchListActivity;
import in.slanglabs.sampleretailapp.db.AppDatabase;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;

public class Repository {

    private String TAG = "Repository";

    private final MutableLiveData<String> listType =
            new MutableLiveData<>();

    private final SingleLiveEvent<Pair<Class, Bundle>> activityToStart =
            new SingleLiveEvent<>();

    private final SingleLiveEvent<FeedbackItem> showFeedBackFragment = new SingleLiveEvent<>();

    private final MutableLiveData<Boolean> slangInitialized =
            new MutableLiveData<>();

    private final MutableLiveData<Boolean> slangInvoked =
            new MutableLiveData<>();

    private final AppDatabase mDatabase;
    private final AppExecutors appExecutors;
    private final SlangInterface mSlangInterface;

    private Observer observer;

    private SharedPreferences mPrefs;

    private int selectedSearchItemId = -1;

    Repository(final AppDatabase database, final AppExecutors appExecutors,
               final SharedPreferences preferences,
               final SlangInterface slangInterface) {
        this.mDatabase = database;
        this.appExecutors = appExecutors;
        mSlangInterface = slangInterface;
        this.mPrefs = preferences;

        listType.setValue(ListType.GROCERY);

        Random rng = new Random();
        LiveData<List<Item>> items = database.itemDao().getItems();
        observer = (Observer<List<Item>>) itemAndOffers -> {
            if (itemAndOffers.size() > 1) {
                items.removeObserver(observer);
            } else {
                return;
            }
            List<Offer> offerItems = new ArrayList<>();
            List<Integer> generated = new ArrayList<>();
            int totalNumberOfOffers = (int) (0.3 * itemAndOffers.size());
            for (int i = 0; i < totalNumberOfOffers; i++) {
                while (true) {
                    Integer next = rng.nextInt(itemAndOffers.size());
                    if (!generated.contains(next)) {
                        generated.add(next);
                        break;
                    }
                }
            }
            int counter;
            for (counter = 0; counter < generated.size(); counter++) {
                int randomNumber = generated.get(counter);
                Offer offerItem = new Offer();
                offerItem.offerName = "Offer " + (counter + 1);
                offerItem.itemId = itemAndOffers.get(randomNumber).itemId;
                offerItem.minQuantity = 2 + new Random().nextInt(3);
                offerItem.percentageDiscount = randFloat(0.1f, 0.3f);
                offerItems.add(offerItem);
            }

            appExecutors.diskIO().execute(() -> {
                mDatabase.offerDao().removeAllOffers();
                mDatabase.offerDao().insert(offerItems);
            });
        };

        items.observeForever(observer);

    }

    public void switchCategory(@ListType String category) {
        listType.setValue(category);
        activityToStart.setValue(new Pair<>(SearchListActivity.class, null));
        mSlangInterface.clearSearchContext();
    }

    public void setSlangInitialized(boolean value) {
        slangInitialized.postValue(value);
    }

    public void setSlangInvoked(boolean value) {
        slangInvoked.postValue(value);
    }

    //Cart Related Methods
    public void addItemToCart(Item item, int quantity) {
        appExecutors.diskIO().execute(() -> {
            mDatabase.runInTransaction(() -> {
                int currentNumber = quantity;
                CartItem cartItem = mDatabase.cartDao().getCartItemForIdSync(item.itemId);
                if (cartItem != null) {
                    int number = cartItem.quantity;
                    number = number + currentNumber;
                    cartItem.quantity = number;
                    mDatabase.cartDao().update(cartItem);
                } else {
                    CartItem cartItemFinal = new CartItem();
                    cartItemFinal.itemId = item.itemId;
                    cartItemFinal.quantity = currentNumber;
                    mDatabase.cartDao().insert(cartItemFinal);
                }

            });
        });
        mSlangInterface.notifyAddToCartSuccess();
    }

    public void removeItemFromCart(Item item) {
        appExecutors.diskIO().execute(() -> {
            mDatabase.runInTransaction(() -> {
                CartItem cartItem = mDatabase.cartDao().getCartItemForIdSync(item.itemId);
                if (cartItem == null) {
                    return;
                }
                int number = cartItem.quantity;
                number = number - 1;
                if (number == 0) {
                    mDatabase.cartDao().remove(cartItem);
                    return;
                }
                cartItem.quantity = number;
                mDatabase.cartDao().update(cartItem);
            });
        });
    }

    public void clearCart() {
        appExecutors.diskIO().execute(() -> {
            mDatabase.cartDao().removeAllItems();
        });
    }

    //Order Related Methods
    public LiveData<OrderItem> getOrderItem(String orderItemId) {
        return mDatabase.orderDao().loadOrder(orderItemId);
    }

    public void addOrderItem(OrderItem item) {
        appExecutors.diskIO().execute(() -> {
            mDatabase.orderDao().insert(item);
        });
    }

    public void removeOrderItem(OrderItem item) {
        appExecutors.diskIO().execute(() -> {
            mDatabase.orderDao().update(false, item.orderId);
        });
    }

    //Slang callback handlers
    public void onSearch(SearchInfo searchInfo) {

        //Get search category
        String category = searchInfo.getItem().getCategory();

        //Switch category if category was found in the search info.
        if (category != null && !category.isEmpty()) {
            switch (category) {
                case "pharmacy":
                    listType.setValue(ListType.PHARMACY);
                    break;
                case "grocery":
                    listType.setValue(ListType.GROCERY);
                    break;
                case "fashion":
                    listType.setValue(ListType.FASHION);
            }
        }

        //Move to the search activity with the search term found.
        Bundle bundle = new Bundle();
        SearchItem searchItem = new SearchItem();
        searchItem.name = searchInfo.getItem().getDescription();
        if (searchInfo.getItem().getSize() != null) {
            String unit = searchInfo.getItem().getSize().getUnit().toString();
            if (unit.equals(in.slanglabs.assistants.retail.Item.Size.Unit.GRAM.toString())) {
                unit = "g";
            }
            searchItem.size = searchInfo.getItem().getSize().getAmount() + " " + unit;
        }
        if (searchInfo.getItem().getQuantity() != null) {
            searchItem.quantity = searchInfo.getItem().getQuantity().getAmount();
        }
        searchItem.isAddToCart = searchInfo.isAddToCart();

        if (selectedSearchItemId != -1) {
            int itemId = selectedSearchItemId;
            if (searchItem.quantity > 0) {
                appExecutors.diskIO().execute(() -> {
                    Item item = mDatabase.itemDao().getItemId(itemId);
                    addItemToCart(item, searchItem.quantity);
                });
                mSlangInterface.notifyAddToCartSuccess();
            }
            return;
        }

        bundle.putSerializable("search_term", searchItem);
        bundle.putBoolean("is_voice_search", true);
        activityToStart.setValue(new Pair<>(SearchListActivity.class, bundle));
    }

    public void onOrder(OrderInfo orderInfo) {

        //Get order item index
        int index = orderInfo.getIndex();
        if (index == 0) {

            //Move to the orders list activity to show all the orders.
            Bundle bundle = new Bundle();
            if (orderInfo.getAction().equals(OrderInfo.Action.VIEW)) {
                bundle.putBoolean("is_voice_view_order", true);
            } else {
                bundle.putBoolean("is_voice_cancel_order", true);
            }
            activityToStart.setValue(new Pair<>(OrderActivity.class, bundle));
            return;
        }

        if (orderInfo.getAction().equals(OrderInfo.Action.VIEW)) {

            //If the index is not 0, try to show the order at the index.
            showOrder(index);
        } else if (orderInfo.getAction() == OrderInfo.Action.CANCEL) {

            switch (orderInfo.getCancelConfirmationStatus()) {
                case UNKNOWN:

                    //If cancel confirmation status is UNKNOWN, show cancelConfirmation prompt.
                    cancelOrderConfirmation(index);
                    break;
                case CONFIRMED:

                    //If cancel confirmation status is CONFIRMED, cancel the order and dismiss the confirmation prompt
                    acceptOrderCancel(index);
                    break;
                case DENIED:

                    //If cancel confirmation status is DENIED, dismiss the confirmation prompt.
                    denyOrderCancel(index);
            }
        }
    }

    public void onNavigation(String targetString) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("is_voice_navigation", true);

        //Get the target view string and make sure to navigate to the right view.
        switch (targetString) {
            case "back":

                //Set null for the targetActivity to indicate that we just to finish the current one.
                activityToStart.setValue(new Pair<>(null, null));
                break;
            case "home":
                activityToStart.setValue(new Pair<>(SearchListActivity.class, bundle));
                break;
            case "pharmacy":
                listType.setValue(ListType.PHARMACY);
                activityToStart.setValue(new Pair<>(SearchListActivity.class, bundle));
                break;
            case "grocery":
                listType.setValue(ListType.GROCERY);
                activityToStart.setValue(new Pair<>(SearchListActivity.class, bundle));
                break;
            case "fashion":
                listType.setValue(ListType.FASHION);
                activityToStart.setValue(new Pair<>(SearchListActivity.class, bundle));
                break;
            case "cart":
                activityToStart.setValue(new Pair<>(CartActivity.class, bundle));
                break;
            case "order":
                activityToStart.setValue(new Pair<>(OrderActivity.class, bundle));
                break;
            case "checkout":
                activityToStart.setValue(new Pair<>(CheckOutActivity.class, bundle));
                break;
            case "offers":
                activityToStart.setValue(new Pair<>(OffersActivity.class, bundle));
                break;
            default:
                mSlangInterface.notifyNavigationUserJourneyFailure();
        }
    }

    public void showOrder(int orderIndex) {
        appExecutors.diskIO().execute(() -> {
            //Note, its advisable to perform all SlangRetailAssistant actions on the main thread.
            List<OrderItem> orderItems = mDatabase.orderDao().loadAllOrdersSync();
            if (orderItems.size() < 1) {

                appExecutors.mainThread().execute(mSlangInterface::notifyOrderManagementEmpty);
                return;
            } else if (orderIndex > orderItems.size()) {

                //Notify SlangRetailAssistant the order item cannot be found.
                appExecutors.mainThread().execute(()
                        -> mSlangInterface.notifyOrderNotFound(orderIndex));
                return;
            }
            if (orderIndex == -1) {

                //If the index is -1, it means that we need to show the latest/last order
                //Obtain the latest order and move to that view.
                String orderId = orderItems.get(0).orderId;
                appExecutors.mainThread().execute(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("orderItemId", orderId);
                    bundle.putBoolean("is_voice_view_order", true);
                    activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
                });
                return;
            }

            //If the index is not -1, it means that we need move to the item at that specific index.
            //Obtain the item at that index and move to that view.
            //Note: SlangRetailAssistant will provide the index starting from 1 instead of 0 hence, we need to subtract 1.
            String orderId = orderItems.get(orderIndex - 1).orderId;
            appExecutors.mainThread().execute(() -> {
                Bundle bundle = new Bundle();
                bundle.putString("orderItemId", orderId);
                bundle.putBoolean("is_voice_view_order", true);
                activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
            });
        });
    }

    public void cancelOrderConfirmation(int orderIndex) {
        appExecutors.diskIO().execute(() -> {
            //Note, its advisable to perform all SlangRetailAssistant actions on the main thread.
            List<OrderItem> orderItems = mDatabase.orderDao().loadAllOrdersSync();
            if (orderItems.size() < 1) {

                //Notify SlangRetailAssistant the order items are empty.
                appExecutors.mainThread().execute(mSlangInterface::notifyOrderManagementEmpty);
                return;
            } else if (orderIndex > orderItems.size()) {

                //Notify SlangRetailAssistant the order item cannot be found.
                appExecutors.mainThread().execute(()
                        -> mSlangInterface.notifyOrderManagementCancelOrderNotFound(orderIndex));
                return;
            }
            if (orderIndex == -1) {

                //If the index is -1, it means that we need to show the latest/last order
                //Obtain the latest order and move to that view.
                String orderId = orderItems.get(0).orderId;
                appExecutors.mainThread().execute(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("orderItemId", orderId);
                    bundle.putBoolean("is_voice_order_cancel", true);
                    //Note : Since, this is a cancel confirmation request, we need to add this param to show the confirmation alert on the order item page.
                    bundle.putBoolean("showConfirmScreen", true);
                    activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
                });
                return;
            }

            //If the index is not -1, it means that we need move to the item at that specific index.
            //Obtain the item at that index and move to that view.
            //Note: SlangRetailAssistant will provide the index starting from 1 instead of 0 hence, we need to subtract 1.
            String orderId = orderItems.get(orderIndex - 1).orderId;
            appExecutors.mainThread().execute(() -> {
                Bundle bundle = new Bundle();
                bundle.putString("orderItemId", orderId);
                bundle.putInt("orderItemIndex", orderIndex);
                bundle.putBoolean("is_voice_order_cancel", true);
                //Note : Since, this is a cancel confirmation request, we need to add this param to show the confirmation alert on the order item page.
                bundle.putBoolean("showConfirmScreen", true);
                activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
            });
        });
    }

    public void acceptOrderCancel(int orderIndex) {
        appExecutors.diskIO().execute(() -> {
            //Note, its advisable to perform all SlangRetailAssistant actions on the main thread.

            List<OrderItem> orderItems = mDatabase.orderDao().loadAllOrdersSync();
            if (orderItems.size() < 1) {

                //Notify SlangRetailAssistant the order items are empty.
                appExecutors.mainThread().execute(mSlangInterface::notifyOrderManagementEmpty);
                return;
            } else if (orderIndex > orderItems.size()) {

                //Notify SlangRetailAssistant the order item cannot be found.
                appExecutors.mainThread().execute(()
                        -> mSlangInterface.notifyOrderManagementCancelOrderNotFound(orderIndex));
                return;
            }
            if (orderIndex == -1) {

                //If the index is -1, it means that we need to show the latest/last order
                //Obtain the latest order and move to that view.
                appExecutors.mainThread().execute(() -> {
                    removeOrderItem(orderItems.get(0));
                    Bundle bundle = new Bundle();
                    bundle.putString("orderItemId", orderItems.get(0).orderId);
                    bundle.putBoolean("is_voice_order_cancel", true);
                    //Note : We need to add this param to remove the confirmation alert on the order item page.
                    bundle.putBoolean("showConfirmScreen", false);
                    activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
                });
                return;
            }
            appExecutors.mainThread().execute(() -> {
                removeOrderItem(orderItems.get(orderIndex - 1));
                Bundle bundle = new Bundle();
                bundle.putString("orderItemId", orderItems.get(orderIndex - 1).orderId);
                bundle.putBoolean("is_voice_order_cancel", true);
                //Note : We need to add this param to remove the confirmation alert on the order item page.
                bundle.putBoolean("showConfirmScreen", false);
                activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
            });
        });
    }

    public void denyOrderCancel(int orderIndex) {
        appExecutors.diskIO().execute(() -> {

            //Note, its advisable to perform all SlangRetailAssistant actions on the main thread.
            List<OrderItem> orderItems = mDatabase.orderDao().loadAllOrdersSync();
            if (orderItems.size() < 1) {

                //Notify SlangRetailAssistant the order items are empty.
                appExecutors.mainThread().execute(mSlangInterface::notifyOrderManagementEmpty);
                return;
            } else if (orderIndex > orderItems.size()) {

                //Notify SlangRetailAssistant the order item cannot be found.
                appExecutors.mainThread().execute(()
                        -> mSlangInterface.notifyOrderManagementCancelOrderNotFound(orderIndex));
                return;
            }
            if (orderIndex == -1) {
                appExecutors.mainThread().execute(() -> {
                    Bundle bundle = new Bundle();
                    bundle.putString("orderItemId", orderItems.get(0).orderId);
                    bundle.putBoolean("is_voice_order_cancel", true);
                    //Note : We need to add this param to remove the confirmation alert on the order item page.
                    bundle.putBoolean("showConfirmScreen", false);
                    activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
                });
                return;
            }
            appExecutors.mainThread().execute(() -> {
                Bundle bundle = new Bundle();
                bundle.putString("orderItemId", orderItems.get(orderIndex - 1).orderId);
                bundle.putBoolean("is_voice_order_cancel", true);
                //Note : We need to add this param to remove the confirmation alert on the order item page.
                bundle.putBoolean("showConfirmScreen", false);
                activityToStart.setValue(new Pair<>(OrderItemsActivity.class, bundle));
            });
        });
    }

    public void sendFeedbackItem(FeedbackItem feedbackItem) {
        Log.d(TAG, "feedback " + feedbackItem + " sent");
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<FeedbackItem> jsonAdapter = moshi.adapter(FeedbackItem.class);
        String json = jsonAdapter.toJson(feedbackItem);
        Type type = Types.newParameterizedType(Map.class, String.class, String.class);
        JsonAdapter<Map<String, String>> adapter = moshi.adapter(type);
        try {
            Map<String, String> map = adapter.fromJson(json);
            Map<String, String> journeyDetails = feedbackItem.journeyDetails;
            for (String key : journeyDetails.keySet()) {
                map.put("journeyDetails." + key, journeyDetails.get(key));
            }
            if (feedbackItem.isPositiveFeedback) {
                mSlangInterface.trackAppEvent("feedback_positive",map);
            } else {
                mSlangInterface.trackAppEvent("feedback_negative",map);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Getters
    public MutableLiveData<String> getListType() {
        return listType;
    }

    public SlangInterface getSlangInterface() {
        return mSlangInterface;
    }

    public SingleLiveEvent<Pair<Class, Bundle>> getActivityToStart() {
        return activityToStart;
    }

    public SingleLiveEvent<FeedbackItem> getFeedbackFragment() {
        return showFeedBackFragment;
    }

    public LiveData<List<ItemOfferCart>> getItems() {
        return mDatabase.itemDao().getItemsOffersCart();
    }

    public LiveData<ItemOfferCart> getItemForId(int id) {
        return mDatabase.itemDao().getItemForId(id);
    }

    public LiveData<List<OfferItemCart>> getOfferItems() {
        return mDatabase.offerDao().getOffersAndItems();
    }

    public LiveData<List<CartItemOffer>> getCartItems() {
        return mDatabase.cartDao().getCartItems();
    }

    public LiveData<List<OrderItem>> getOrderItems() {
        return mDatabase.orderDao().loadAllOrders();
    }

    public LiveData<List<ItemOfferCart>> getItemsOfferCartForNameSync(
            String name,
            @ListType String listType,
            FilterOptions filterOptions) {

        PorterStemmer porterStemmer = new PorterStemmer();
        String stem = porterStemmer.stemWord(name);

        MediatorLiveData<List<ItemOfferCart>> itemOfferCartListLiveData = new MediatorLiveData<>();

        StringBuilder stringBuilder = new StringBuilder();
        List<Object> args = new ArrayList();
        stringBuilder.append("SELECT * FROM items JOIN itemsFts ON items.name == itemsFts.name WHERE type =?");
        args.add(listType);
        if (!stem.equals("")) {
            stringBuilder.append(" AND itemsFts MATCH ?");
            args.add(fixQuery(stem));
        }
        if (!filterOptions.getBrands().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.brand IN (");
            appendPlaceholders(stringBuilder, filterOptions.getBrands().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getBrands());
        }
        if (!filterOptions.getSizes().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.size IN (");
            appendPlaceholders(stringBuilder, filterOptions.getSizes().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getSizes());
        }
        if (!filterOptions.getColors().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.color IN (");
            appendPlaceholders(stringBuilder, filterOptions.getColors().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getColors());
        }
        if (!filterOptions.getGenders().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.gender IN (");
            appendPlaceholders(stringBuilder, filterOptions.getGenders().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getGenders());
        }
        if (!filterOptions.getCategories().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.category IN (");
            appendPlaceholders(stringBuilder, filterOptions.getCategories().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getCategories());
        }
        if (!filterOptions.getPriceRanges().isEmpty()) {
            stringBuilder.append(" AND (");
            for (int i = 0; i < filterOptions.getPriceRanges().size(); i++) {
                PriceRange priceRange = filterOptions.getPriceRanges().get(i);
                if (i != 0) {
                    stringBuilder.append(" OR");
                }
                stringBuilder.append(" (items.price >= ?");
                args.add(priceRange.getStartPrice());
                if (priceRange.getStopPrice() != -1) {
                    stringBuilder.append("AND items.price <= ?)");
                    args.add(priceRange.getStopPrice());
                } else {
                    stringBuilder.append(")");
                }
            }
            stringBuilder.append(")");
        }
        if (!filterOptions.getColors().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.color IN (");
            appendPlaceholders(stringBuilder, filterOptions.getColors().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getColors());
        }
        if (!filterOptions.getUnits().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.unit IN (");
            appendPlaceholders(stringBuilder, filterOptions.getUnits().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getUnits());
        }
        if (!filterOptions.getVariants().isEmpty()) {
            stringBuilder.append(" AND");
            stringBuilder.append(" items.variant IN (");
            appendPlaceholders(stringBuilder, filterOptions.getVariants().size());
            stringBuilder.append(")");
            args.addAll(filterOptions.getVariants());
        }
        if (filterOptions.getOrderBy() == OrderBy.RELEVANCE) {
            stringBuilder.append(" GROUP BY items.name");
            stringBuilder.append(" ORDER BY items.name ASC");
        } else if (filterOptions.getOrderBy() == OrderBy.HIGH_LOW_PRICE) {
            stringBuilder.append(" GROUP BY items.name");
            stringBuilder.append(" ORDER BY items.price DESC");
        } else if (filterOptions.getOrderBy() == OrderBy.LOW_HIGH_PRICE) {
            stringBuilder.append(" GROUP BY items.name");
            stringBuilder.append(" ORDER BY items.price ASC");
        } else if (filterOptions.getOrderBy() == OrderBy.NONE) {
            stringBuilder.append(" GROUP BY items.itemId");
            stringBuilder.append(" ORDER BY items.itemId ASC");
        }
        stringBuilder.append(" LIMIT 300");
        stringBuilder.append(";");

        itemOfferCartListLiveData.addSource(
                mDatabase.itemDao().getItemsAndOffersBasedOnSearchFts(
                        new SimpleSQLiteQuery(stringBuilder.toString(), args.toArray())),
                itemOfferCarts -> {
                    appExecutors.diskIO().execute(() -> {
                        List<String> itemNames = new ArrayList<>();
                        HashMap<String, ItemOfferCart> itemNamesObjectMap = new HashMap<>();
                        List<ItemOfferCart> itemOfferCartObjects = new ArrayList<>();
                        for (ItemOfferCart itemOfferCart : itemOfferCarts) {
                            itemNames.add(itemOfferCart.item.name);
                            itemNamesObjectMap.put(itemOfferCart.item.name, itemOfferCart);
                        }
                        List<ExtractedResult> results = FuzzySearch.extractSorted(stem, itemNames);
                        for (ExtractedResult result : results) {
                            itemOfferCartObjects.add(itemNamesObjectMap.get(result.getString()));
                        }
                        itemOfferCartListLiveData.postValue(itemOfferCartObjects);
                    });
                });

        return itemOfferCartListLiveData;
    }

    public LiveData<Boolean> getIsDbCreated() {
        return mDatabase.getDatabaseCreated();
    }

    public LiveData<Boolean> getSlangInitiailzed() {
        return slangInitialized;
    }

    public LiveData<Boolean> getIsSlangInvoked() {
        return slangInvoked;
    }

    public LiveData<List<String>> getItemBrands(@ListType String type) {
        return mDatabase.itemDao().getItemsBrands(type);
    }

    public LiveData<List<String>> getItemSizes(@ListType String type) {
        return mDatabase.itemDao().getItemsSizes(type);
    }

    public LiveData<List<String>> getItemColors(@ListType String type) {
        return mDatabase.itemDao().getItemColors(type);
    }

    public LiveData<List<String>> getItemCategories(@ListType String type) {
        return mDatabase.itemDao().getItemCategories(type);
    }

    public LiveData<List<String>> getItemGenders(@ListType String type) {
        return mDatabase.itemDao().getItemGenders(type);
    }

    public void setShowFeedBackFragment(FeedbackItem feedbackItem) {
        showFeedBackFragment.postValue(feedbackItem);
    }

    public void setSelectedSearchItem(int itemId) {
        selectedSearchItemId = itemId;
    }

    //Helpers
    public static float randFloat(float min, float max) {
        Random rand = new Random();
        return rand.nextFloat() * (max - min) + min;
    }

    private static String fixQuery(String query) {
        String[] splited = query.split("\\s+");
        StringBuilder finalString = new StringBuilder();
        for (String substring : splited) {
            finalString.append(substring);
            try {
                int num = Integer.parseInt(substring);
                finalString.append(" ");
            } catch (NumberFormatException e) {
                finalString.append("*");
            }
        }
        return finalString.toString();
    }

    public static void appendPlaceholders(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            builder.append("?");
            if (i < count - 1) {
                builder.append(",");
            }
        }
    }

}
