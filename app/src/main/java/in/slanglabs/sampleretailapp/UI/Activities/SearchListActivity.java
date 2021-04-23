package in.slanglabs.sampleretailapp.UI.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Locale;

import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.Adapters.ListAdapter;
import in.slanglabs.sampleretailapp.UI.Fragments.FashionFilterDialogFragment;
import in.slanglabs.sampleretailapp.UI.Fragments.GroceryAndPharmaFilterDialogFragment;
import in.slanglabs.sampleretailapp.UI.Fragments.SearchDialogFragment;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

import static in.slanglabs.sampleretailapp.Model.OrderBy.HIGH_LOW_PRICE;
import static in.slanglabs.sampleretailapp.Model.OrderBy.LOW_HIGH_PRICE;
import static in.slanglabs.sampleretailapp.Model.OrderBy.NONE;
import static in.slanglabs.sampleretailapp.Model.OrderBy.RELEVANCE;

public class SearchListActivity extends MainActivity implements ItemClickListener {

    private ListAdapter listAdapter;
    private AppViewModel appViewModel;
    private TextView orderEmptyTextView;
    private boolean isVoiceSearch;
    private ProgressDialog dialog;
    private SearchItem searchInfo;
    private SearchItem searchItemCopy;
    private boolean isVoiceSearchCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_search_list, null, false);
        ll.addView(contentView, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT));

        RecyclerView listItemView = contentView.findViewById(R.id.list_item_view);
        orderEmptyTextView = contentView.findViewById(R.id.order_empty_text_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Search");
        }

        toolbar.setOnClickListener(view -> clearSearch());

        dialog = ProgressDialog.show(SearchListActivity.this, "",
                "Loading. Please wait...", true);

        dialog.show();

        appViewModel = new ViewModelProvider(this).get(
                AppViewModel.class);
        orderEmptyTextView.setVisibility(View.GONE);
        FloatingActionButton fab = contentView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(SearchListActivity.this, CartActivity.class);
            startActivity(intent);
        });
        TextView cartItemCount = findViewById(R.id.cart_item_count);
        cartItemCount.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        appViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems.size() == 0) {
                cartItemCount.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
            } else {
                cartItemCount.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
            }
            cartItemCount.setText(String.format(Locale.ENGLISH, "%d", cartItems.size()));
        });
        listAdapter = new ListAdapter(appViewModel, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listItemView.setLayoutManager(layoutManager);
        listItemView.setItemAnimator(null);
        listItemView.setAdapter(listAdapter);

        appViewModel.getSearchForNameMediator().observe(this, itemOfferCarts -> {
            dialog.dismiss();
            listAdapter.setList(itemOfferCarts);
            if (itemOfferCarts.size() == 0) {
                orderEmptyTextView.setVisibility(View.VISIBLE);
                if (searchInfo != null && (
                        appViewModel.getListType().getValue().equals(ListType.FASHION) ||
                                searchInfo.isAddToCart ||
                                !searchInfo.size.equalsIgnoreCase(""))) {
                    if (isVoiceSearch) {

                        //Notify SlangRetailAssistant that search user journey has resulted in add to cart item not found.
                        appViewModel.getSlangInterface().notifyAddToCartItemNotFound();
                    }
                }
                if (isVoiceSearch) {

                    //Notify SlangRetailAssistant that search user journey has resulted in search item not found.
                    appViewModel.getSlangInterface().notifySearchItemNotFound();
                }
                isVoiceSearch = false;
            } else {
                orderEmptyTextView.setVisibility(View.GONE);
                if (searchInfo != null && (
                        appViewModel.getListType().getValue().equals(ListType.FASHION) ||
                                searchInfo.isAddToCart ||
                                !searchInfo.size.equalsIgnoreCase(""))) {
                    if (itemOfferCarts.size() > 1) {
                        if (isVoiceSearch) {

                            //Notify SlangRetailAssistant that the search view journey add to cart needs to disambiguate the search item.
                            appViewModel.getSlangInterface().notifyAddToCartNeedDisambiguation();
                        }
                    } else if (searchInfo.quantity == 0) {
                        if (isVoiceSearch) {

                            //Notify SlangRetailAssistant that the search view journey add to cart requires quantity to be specified.
                            appViewModel.getSlangInterface().notifyAddToCartNeedQuantity();
                        }
                    } else {
                        appViewModel.addItem(itemOfferCarts.get(0).item, searchInfo.quantity);
                        if (isVoiceSearch) {

                            // Notify SlangRetailAssistant that search user journey has resulted in add to cart successful.
                            appViewModel.getSlangInterface().notifyAddToCartSuccess();
                        }
                    }
                    searchInfo = null;
                    isVoiceSearch = false;
                    return;
                }

                if (isVoiceSearch) {

                    // Notify SlangRetailAssistant that search user journey has resulted in search successful.
                    appViewModel.getSlangInterface().notifySearchSuccess();
                    isVoiceSearch = false;
                }
            }
        });

        handleIntent(getIntent());

        filterButton.setOnClickListener(view -> {
            if (appViewModel.getListType().getValue().equals(ListType.GROCERY) ||
                    appViewModel.getListType().getValue().equals(ListType.PHARMACY)) {
                GroceryAndPharmaFilterDialogFragment newFragment = GroceryAndPharmaFilterDialogFragment.newInstance(
                        appViewModel.getFilterOptions());
                newFragment.viewItemListener = filterOptions -> {
                    appViewModel.setFilterOptions(filterOptions);
                    appViewModel.getSearchItem(appViewModel.getCurrentSearchTerm());
                };
                newFragment.show(getSupportFragmentManager(), "FilterAndSortDialogFragment");
                return;
            }
            FashionFilterDialogFragment newFragment = FashionFilterDialogFragment.newInstance(
                    appViewModel.getFilterOptions());
            newFragment.viewItemListener = filterOptions -> {
                appViewModel.setFilterOptions(filterOptions);
                appViewModel.getSearchItem(appViewModel.getCurrentSearchTerm());
            };
            newFragment.show(getSupportFragmentManager(), "FilterAndSortDialogFragment");
        });

        sortButton.setOnClickListener(view -> {
            final String[] ordering = new String[4];
            int selectedIndxForOrdering = 0;
            ordering[0] = "None";
            ordering[1] = "Relevance";
            ordering[2] = "Price: High to Low";
            ordering[3] = "Price: Low to High";
            switch (appViewModel.getFilterOptions().getOrderBy()) {
                case HIGH_LOW_PRICE:
                    selectedIndxForOrdering = 2;
                    break;
                case LOW_HIGH_PRICE:
                    selectedIndxForOrdering = 3;
                    break;
                case RELEVANCE:
                    selectedIndxForOrdering = 1;
                    break;
                default:
                    selectedIndxForOrdering = 0;
            }
            final AlertDialog.Builder mBuilder = new AlertDialog.Builder(
                    SearchListActivity.this);
            mBuilder.setTitle("Select preferred sort:");
            mBuilder.setSingleChoiceItems(ordering, selectedIndxForOrdering, (dialogInterface, i) -> {
                switch (i) {
                    case 1:
                        appViewModel.getFilterOptions().setOrderBy(RELEVANCE);
                        break;
                    case 2:
                        appViewModel.getFilterOptions().setOrderBy(HIGH_LOW_PRICE);
                        break;
                    case 3:
                        appViewModel.getFilterOptions().setOrderBy(LOW_HIGH_PRICE);
                        break;
                    default:
                        appViewModel.getFilterOptions().setOrderBy(NONE);
                }
            });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                appViewModel.getSearchItem(appViewModel.getCurrentSearchTerm());
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        clearButton.setOnClickListener(view -> {
            clearSearch();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if(intent.getExtras() != null) {
            clearSearch();
        }
        String listTitle = "Grocery Search";
        if (appViewModel.getListType().getValue() != null) {
            @ListType String listType = appViewModel.getListType().getValue();
            switch (listType) {
                case ListType.GROCERY:
                    filterButton.setVisibility(View.VISIBLE);
                    sortButton.setVisibility(View.VISIBLE);
                    break;
                case ListType.PHARMACY:
                    listTitle = "Pharmacy Search";
                    filterButton.setVisibility(View.VISIBLE);
                    sortButton.setVisibility(View.VISIBLE);
                    break;
                case ListType.FASHION:
                    listTitle = "Fashion Search";
                    filterButton.setVisibility(View.VISIBLE);
                    sortButton.setVisibility(View.VISIBLE);
                    break;
            }
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(listTitle);
        }
        isVoiceSearch = intent.getBooleanExtra("is_voice_search", false);
        isVoiceSearchCopy = isVoiceSearch;
        searchInfo = (SearchItem) intent.getSerializableExtra("search_term");
        searchItemCopy = searchInfo;
        appViewModel.setCurrentSearchTerm("");
        if (searchInfo != null) {
            appViewModel.setSearchItem(searchInfo);
        }
        if (appViewModel.getCurrentSearchItem() != null) {
            appViewModel.setCurrentSearchTerm(appViewModel.getCurrentSearchItem().brandName + " " + appViewModel.getCurrentSearchItem().name + " " + appViewModel.getCurrentSearchItem().size);
            clearButton.setVisibility(View.VISIBLE);
        } else {
            clearButton.setVisibility(View.GONE);
        }
        editText.setText(appViewModel.getCurrentSearchTerm());
        appViewModel.getSearchItem(appViewModel.getCurrentSearchItem());
        dialog.show();
    }

    @Override
    protected void showDialog() {
        SearchDialogFragment newFragment = SearchDialogFragment.newInstance(
                appViewModel.getCurrentSearchTerm());
        newFragment.viewItemListener = item -> {
            Intent intent = new Intent(SearchListActivity.this,
                    SearchListActivity.class);
            SearchItem searchItem = new SearchItem();
            searchItem.name = item;
            intent.putExtra("search_term", searchItem);
            startActivity(intent);
        };
        newFragment.show(getSupportFragmentManager(), "autoCompleteFragment");
    }

    @Override
    public void itemClicked(Item item) {
        if(searchItemCopy != null) {
            if (isVoiceSearchCopy) {
                if (searchItemCopy.quantity == 0) {
                    appViewModel.setSelectedSearchItem(item.id);

                    //Notify SlangRetailAssistant that the search view journey add to cart requires quantity to be specified.
                    appViewModel.getSlangInterface().notifyAddToCartNeedQuantity();
                }
                else {

                    //Add item to the cart which will internally report that the item is added to the cart.
                    appViewModel.addItem(item, searchItemCopy.quantity);
                }
            }
            searchItemCopy = null;
            isVoiceSearchCopy = false;
        }
        Intent intent = new Intent(this, ItemActivity.class);
        intent.putExtra("itemId", item.itemId);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Clear the context when we move out the current view.
        appViewModel.getSlangInterface().clearSearchContext();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Show the slang trigger in this activity
        appViewModel.getSlangInterface().showTrigger(this);
    }

    @Override
    public void onBackPressed() {
        if (appViewModel.getCurrentSearchTerm() == null ||
                appViewModel.getCurrentSearchTerm().isEmpty()) {
            super.onBackPressed();
        }
        else {
            clearSearch();
        }
    }

    private void clearSearch() {
        appViewModel.setCurrentSearchTerm("");
        appViewModel.setSearchItem(null);
        appViewModel.getFilterOptions().clear();
        clearButton.setVisibility(View.GONE);
        editText.setText(appViewModel.getCurrentSearchTerm());
        appViewModel.getSearchItem(appViewModel.getCurrentSearchTerm());
        appViewModel.getSlangInterface().clearSearchContext();
        dialog.show();
    }
}
