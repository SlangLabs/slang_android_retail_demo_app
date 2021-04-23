package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.sampleretailapp.BuildConfig;
import in.slanglabs.sampleretailapp.Model.FeedbackItem;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.SearchItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.Fragments.SearchDialogFragment;

public class MainActivity extends BaseActivity {

    protected LinearLayout ll;
    protected TextView editText;
    protected View searchLayout;
    protected ImageButton filterButton;
    protected ImageButton sortButton;
    protected ImageButton clearButton;
    protected ImageButton feedbackButton;
    private ImageView voiceButton;
    protected Toolbar toolbar;

    private DrawerLayout dl;
    private ActionBarDrawerToggle t;
    private NavigationView nv;

    private static String TAG = "BaseActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editText = findViewById(R.id.search_text);
        voiceButton = findViewById(R.id.voice_image);
        searchLayout = findViewById(R.id.search_layout);
        feedbackButton = findViewById(R.id.feedback_button);
        filterButton = toolbar.findViewById(R.id.filter_button);
        filterButton.setVisibility(View.GONE);
        sortButton = toolbar.findViewById(R.id.sort_button);
        sortButton.setVisibility(View.GONE);
        clearButton = findViewById(R.id.clear_image);
        clearButton.setVisibility(View.GONE);


        dl = findViewById(R.id.activity_main);
        ll = findViewById(R.id.layout_main);
        t = new ActionBarDrawerToggle(this, dl, R.string.Open, R.string.Close);

        dl.addDrawerListener(t);
        t.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        nv = findViewById(R.id.nv);
        TextView textView = nv.findViewById(R.id.app_version);
        textView.setText(String.format(Locale.ENGLISH,
                "Version : %s(%d)",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE));
        nv.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent;
            switch (id) {
                case R.id.grocery_home:
                    dl.closeDrawer(GravityCompat.START);
                    appViewModel.setListType(ListType.GROCERY);
                    break;
                case R.id.pharmacy_home:
                    dl.closeDrawer(GravityCompat.START);
                    appViewModel.setListType(ListType.PHARMACY);
                    break;
                case R.id.fashion_home:
                    dl.closeDrawer(GravityCompat.START);
                    appViewModel.setListType(ListType.FASHION);
                    break;
                case R.id.my_orders:
                    dl.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, OrderActivity.class);
                    startActivity(intent);
                    break;
                case R.id.my_offers:
                    dl.closeDrawer(GravityCompat.START);
                    intent = new Intent(MainActivity.this, OffersActivity.class);
                    startActivity(intent);
                    break;
                default:
                    return true;
            }

            return true;

        });

        feedbackButton.setOnClickListener(view -> {
            FeedbackItem feedbackItem = new FeedbackItem();
            appViewModel.showFeedBackFragment(feedbackItem);
        });

        voiceButton.setOnClickListener(view -> appViewModel.getSlangInterface().startConversation(
                MainActivity.this));
        voiceButton.setEnabled(false);
        voiceButton.setAlpha(0.5f);

        appViewModel.getIsSlangInitialized().observe(this, aBoolean -> {
            if (aBoolean != null && aBoolean) {
                voiceButton.setEnabled(true);
                voiceButton.setAlpha(1.0f);
            } else {
                voiceButton.setEnabled(false);
                voiceButton.setAlpha(0.5f);
            }
        });

        appViewModel.getIsSlangInvoked().observe(this, aBoolean -> {
            if ((appViewModel.getIsSlangInitialized().getValue() == null) ||
                    !appViewModel.getIsSlangInitialized().getValue()) {
                return;
            }
            if (aBoolean != null && aBoolean) {
                voiceButton.setEnabled(false);
                voiceButton.setAlpha(0.5f);
            } else {
                voiceButton.setEnabled(true);
                voiceButton.setAlpha(1.0f);
            }
        });

        editText.setOnClickListener(view -> showDialog());
    }

    @Override
    public void onBackPressed() {
        if (dl.isDrawerOpen(GravityCompat.START)) {
            dl.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            dl.openDrawer(Gravity.LEFT);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showDialog() {
        SearchDialogFragment newFragment = SearchDialogFragment.newInstance("");
        newFragment.viewItemListener = item -> {
            appViewModel.getSlangInterface().clearSearchContext();
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
