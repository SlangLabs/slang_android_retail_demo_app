package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

import in.slanglabs.sampleretailapp.BuildConfig;
import in.slanglabs.sampleretailapp.Model.FeedbackItem;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.Fragments.SearchDialogFragment;

public class MainActivity extends BaseActivity {

    protected LinearLayout mLinearLayout;
    protected View mSearchLayoutView;
    protected TextView mSearchTextView;
    protected ImageButton mFilterButton;
    protected ImageButton mSortButton;
    protected ImageButton mClearButton;
    protected ImageButton mFeedbackButton;
    protected Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mSearchTextView = findViewById(R.id.search_text);
        mSearchLayoutView = findViewById(R.id.search_layout);
        mFeedbackButton = findViewById(R.id.feedback_button);
        mFilterButton = mToolbar.findViewById(R.id.filter_button);
        mFilterButton.setVisibility(View.GONE);
        mSortButton = mToolbar.findViewById(R.id.sort_button);
        mSortButton.setVisibility(View.GONE);
        mClearButton = findViewById(R.id.clear_image);
        mClearButton.setVisibility(View.GONE);


        mDrawerLayout = findViewById(R.id.activity_main);
        mLinearLayout = findViewById(R.id.layout_main);
        ActionBarDrawerToggle mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.Open, R.string.Close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        NavigationView mNavigationView = findViewById(R.id.nv);
        TextView textView = mNavigationView.findViewById(R.id.app_version);
        textView.setText(String.format(Locale.ENGLISH,
                "Version : %s(%d)",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE));
        mNavigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            switch (id) {
                case R.id.home:
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    mAppViewModel.setListType(ListType.GROCERY);
                    break;
                case R.id.my_orders:
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, OrderActivity.class);
                    startActivity(intent);
                    break;
                case R.id.my_offers:
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, OffersActivity.class);
                    startActivity(intent);
                    break;
                default:
                    return true;
            }

            return true;

        });

        mFeedbackButton.setOnClickListener(view -> {
            FeedbackItem feedbackItem = new FeedbackItem();
        });


        mSearchTextView.setOnClickListener(view -> showDialog());
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(Gravity.LEFT);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showDialog() {
        SearchDialogFragment newFragment = SearchDialogFragment.newInstance("");
        newFragment.mViewItemListener = item -> {
            Intent intent = new Intent(MainActivity.this,
                    SearchListActivity.class);
            SearchItem searchItem = new SearchItem();
            searchItem.name = item;
            intent.putExtra("search_term", searchItem);
            startActivity(intent);
        };
        newFragment.show(getSupportFragmentManager(), "autoCompleteFragment");
    }

}
