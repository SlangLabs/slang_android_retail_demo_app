package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
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
import in.slanglabs.sampleretailapp.UI.Fragments.GroceryAndPharmaFilterDialogFragment;
import in.slanglabs.sampleretailapp.UI.Fragments.SearchDialogFragment;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;
import in.slanglabs.sampleretailapp.UI.ViewModel.SearchViewModel;

import static in.slanglabs.sampleretailapp.Model.OrderBy.HIGH_LOW_PRICE;
import static in.slanglabs.sampleretailapp.Model.OrderBy.LOW_HIGH_PRICE;
import static in.slanglabs.sampleretailapp.Model.OrderBy.NONE;
import static in.slanglabs.sampleretailapp.Model.OrderBy.RELEVANCE;

public class SearchListActivity extends MainActivity implements ItemClickListener {

    private ListAdapter mListAdapter;
    private SearchViewModel mAppViewModel;
    private TextView mOrderEmptyTextView;
    private View mLoadingItemsView;

    private boolean mListAlreadyShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_search_list, null, false);
        mLinearLayout.addView(contentView, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT));

        RecyclerView listItemView = contentView.findViewById(R.id.list_item_view);
        mOrderEmptyTextView = contentView.findViewById(R.id.order_empty_text_view);
        mLoadingItemsView = contentView.findViewById(R.id.loading_items_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Home");
        }

        mToolbar.setOnClickListener(view -> clearSearch());


        mLoadingItemsView.setVisibility(View.VISIBLE);

        mAppViewModel = new ViewModelProvider(this).get(
                SearchViewModel.class);
        mOrderEmptyTextView.setVisibility(View.GONE);

        FloatingActionButton fab = contentView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(SearchListActivity.this, CartActivity.class);
            startActivity(intent);
        });

        TextView cartItemCount = findViewById(R.id.cart_item_count);
        cartItemCount.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);

        mAppViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems.size() == 0) {
                cartItemCount.setVisibility(View.GONE);
                fab.setVisibility(View.GONE);
            } else {
                cartItemCount.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
            }
            cartItemCount.setText(String.format(Locale.ENGLISH, "%d", cartItems.size()));
        });

        mListAdapter = new ListAdapter(mAppViewModel, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listItemView.setLayoutManager(layoutManager);
        listItemView.setItemAnimator(null);
        listItemView.setAdapter(mListAdapter);

        mAppViewModel.getSearchForNameMediator().observe(this, itemOfferCarts -> {
            mLoadingItemsView.setVisibility(View.GONE);

                if(!mListAlreadyShown) {
                    final Context context = listItemView.getContext();
                    final LayoutAnimationController controller =
                            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_slide_right);
                    listItemView.setLayoutAnimation(controller);
                    mListAdapter.setList(itemOfferCarts);
                    listItemView.scheduleLayoutAnimation();
                    mListAlreadyShown = true;
                }
                else {
                    mListAdapter.setList(itemOfferCarts);
                }

            if (itemOfferCarts.size() == 0) {
                mOrderEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mOrderEmptyTextView.setVisibility(View.GONE);
            }
        });

        mFilterButton.setOnClickListener(view -> {
            if (mAppViewModel.getListType().getValue().equals(ListType.GROCERY) ||
                    mAppViewModel.getListType().getValue().equals(ListType.PHARMACY)) {
                GroceryAndPharmaFilterDialogFragment newFragment = GroceryAndPharmaFilterDialogFragment.newInstance(
                        mAppViewModel.getFilterOptions());
                newFragment.viewItemListener = filterOptions -> {
                    mAppViewModel.setFilterOptions(filterOptions);
                    mAppViewModel.getSearchItem(mAppViewModel.getCurrentSearchTerm());
                };
                newFragment.show(getSupportFragmentManager(), "FilterAndSortDialogFragment");
            }
        });

        mSortButton.setOnClickListener(view -> {
            final String[] ordering = new String[4];
            int selectedIndxForOrdering = 0;
            ordering[0] = "None";
            ordering[1] = "Relevance";
            ordering[2] = "Price: High to Low";
            ordering[3] = "Price: Low to High";
            switch (mAppViewModel.getFilterOptions().getOrderBy()) {
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
                        mAppViewModel.getFilterOptions().setOrderBy(RELEVANCE);
                        break;
                    case 2:
                        mAppViewModel.getFilterOptions().setOrderBy(HIGH_LOW_PRICE);
                        break;
                    case 3:
                        mAppViewModel.getFilterOptions().setOrderBy(LOW_HIGH_PRICE);
                        break;
                    default:
                        mAppViewModel.getFilterOptions().setOrderBy(NONE);
                }
            });
            mBuilder.setPositiveButton("OK", (dialog, which) -> {
                mAppViewModel.getSearchItem(mAppViewModel.getCurrentSearchTerm());
            });
            AlertDialog mDialog = mBuilder.create();
            mDialog.show();
        });

        mClearButton.setOnClickListener(view -> {
            clearSearch();
        });

        mSearchTextView.setText(mAppViewModel.getCurrentSearchTerm());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        SearchItem searchInfo = (SearchItem) intent.getSerializableExtra("search_term");
        mAppViewModel.setCurrentSearchTerm("");
        if (searchInfo != null) {
            mAppViewModel.setCurrentSearchTerm(searchInfo.brandName + " " + searchInfo.name);
            mClearButton.setVisibility(View.VISIBLE);
            mSearchTextView.setText(mAppViewModel.getCurrentSearchTerm());
            mOrderEmptyTextView.setVisibility(View.GONE);
            mListAlreadyShown = false;
            mListAdapter.clear();
            mAppViewModel.getSearchItem(searchInfo);
            mLoadingItemsView.setVisibility(View.VISIBLE);
        }
        else {
            clearSearch();
        }
    }

    @Override
    protected void showDialog() {
        SearchDialogFragment newFragment = SearchDialogFragment.newInstance(
                mAppViewModel.getCurrentSearchTerm());
        newFragment.mViewItemListener = item -> {
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
        mAppViewModel.setSelectedSearchItem(item);
        Intent intent = new Intent(this, ItemActivity.class);
        intent.putExtra("itemId", item.itemId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Show the slang trigger in this activity
        mAppViewModel.getSlangInterface().showTrigger(this);
    }

    @Override
    public void onBackPressed() {
        if (mAppViewModel.getCurrentSearchTerm() == null ||
                mAppViewModel.getCurrentSearchTerm().isEmpty()) {
            super.onBackPressed();
        }
        else {
            clearSearch();
        }
    }

    private void clearSearch() {
        mOrderEmptyTextView.setVisibility(View.GONE);
        mListAdapter.clear();
        mAppViewModel.setCurrentSearchTerm("");
        mAppViewModel.getFilterOptions().clear();
        mClearButton.setVisibility(View.GONE);
        mSearchTextView.setText(mAppViewModel.getCurrentSearchTerm());
        mAppViewModel.getSearchItem(mAppViewModel.getCurrentSearchTerm());
        mLoadingItemsView.setVisibility(View.VISIBLE);
        mListAlreadyShown = false;
    }
}
