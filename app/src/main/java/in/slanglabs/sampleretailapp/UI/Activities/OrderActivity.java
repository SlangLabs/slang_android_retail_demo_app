package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.Adapters.OrderAdapter;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;

public class OrderActivity extends MainActivity {

    private OrderAdapter mListAdapter;
    private List<OrderItem> mOrderItems = new ArrayList<>();
    private TextView orderEmptyTextView;
    private boolean mIsVoiceViewOrder;
    private boolean mIsVoiceCancelOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView = inflater.inflate(R.layout.activity_order, null, false);
        ll.addView(contentView, new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Orders");
        }

        searchLayout.setVisibility(View.GONE);

        orderEmptyTextView = contentView.findViewById(R.id.order_empty_text_view);
        orderEmptyTextView.setVisibility(View.GONE);

        RecyclerView listItemView = contentView.findViewById(R.id.list_item_view);

        handleIntent(getIntent());

        appViewModel.getOrderItems().observe(this,
                orderItems -> {
                    this.mOrderItems = orderItems;
                    mListAdapter.setList(orderItems);
                    if (orderItems.size() == 0) {
                        orderEmptyTextView.setVisibility(View.VISIBLE);
                        if (mIsVoiceViewOrder) {

                            //Notify SlangRetailAssistant that order view journey resulted in empty orders.
                            appViewModel.getSlangInterface().notifyOrderManagementEmpty();
                            mIsVoiceViewOrder = false;
                        } else if (mIsVoiceCancelOrder) {

                            //Notify SlangRetailAssistant that order cancel journey resulted in empty orders.
                            appViewModel.getSlangInterface().notifyOrderManagementCancelEmpty();

                            mIsVoiceCancelOrder = false;
                        }
                    } else {
                        orderEmptyTextView.setVisibility(View.GONE);
                        if (mIsVoiceViewOrder) {

                            //Notify SlangRetailAssistant that order view journey resulted in success.
                            appViewModel.getSlangInterface().notifyOrderManagementSuccess();
                        } else if (mIsVoiceCancelOrder) {

                            if (orderItems.size() == 1) {
                                appViewModel.cancelOrderConfirmation(1);
                            } else {
                                //Notify SlangRetailAssistant that the order cancel journey needs to disambiguate the order index.
                                appViewModel.getSlangInterface().notifyOrderManagementIndexRequiredCancel();
                            }

                        }
                    }
                });

        FloatingActionButton fab = contentView.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(OrderActivity.this, CartActivity.class);
            startActivity(intent);
        });
        TextView cartItemCount = findViewById(R.id.cart_item_count);
        cartItemCount.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
        mListAdapter = new OrderAdapter(new ItemClickListener() {
            @Override
            public void itemClicked(int position) {
                if (mIsVoiceCancelOrder) {
                    appViewModel.cancelOrderConfirmation(position + 1);
                    mIsVoiceCancelOrder = false;
                    return;
                }
                handleItemSelected(position);
            }
        });
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listItemView.setLayoutManager(layoutManager);
        listItemView.setItemAnimator(null);
        listItemView.setAdapter(mListAdapter);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        mIsVoiceViewOrder = intent.getBooleanExtra("is_voice_view_order", false);
        mIsVoiceCancelOrder = intent.getBooleanExtra("is_voice_cancel_order", false);
        appViewModel.makeOrderRequest();
    }

    private boolean handleItemSelected(int position) {
        if (position >= mOrderItems.size()) {
            return false;
        }

        if (position == -1) position = mOrderItems.size();

        OrderItem orderItem = mOrderItems.get(position);
        Intent intent = new Intent(OrderActivity.this, OrderItemsActivity.class);
        intent.putExtra("orderItemId", orderItem.orderId);
        if (mIsVoiceViewOrder) {
            intent.putExtra("is_voice_view_order", true);
            mIsVoiceViewOrder = false;
        }
        startActivity(intent);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Clear the context when we move out the current view.
        appViewModel.getSlangInterface().clearOrderManagementContext();
        mIsVoiceViewOrder = false;
        mIsVoiceCancelOrder = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Show the slang trigger in this activity
        appViewModel.getSlangInterface().showTrigger(this);
    }
}