package in.slanglabs.sampleretailapp.UI.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import in.slanglabs.sampleretailapp.Model.CartItemOffer;
import in.slanglabs.sampleretailapp.Model.Offer;
import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.Adapters.OrderCartAdapter;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

public class OrderItemsActivity extends BaseActivity {

    private TextView totalCost;
    private TextView totalSave;
    private RecyclerView listItemView;
    private Button removeButton;
    private View orderControlSection;
    private AlertDialog alertDialog;
    private boolean showCancelAlert = false;
    private boolean mIsVoiceCancel;
    private int currentItemIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_items);
        removeButton = findViewById(R.id.remove_button);
        listItemView = findViewById(R.id.list_item_view);
        orderControlSection = findViewById(R.id.order_control_section);
        totalCost = findViewById(R.id.total_cost);
        totalSave = findViewById(R.id.total_save);
        appViewModel = new ViewModelProvider(this).get(
                AppViewModel.class);

        appViewModel.getActivityToStart().observe(this, classBundlePair -> {
            if (classBundlePair.first == null) {
                OrderItemsActivity.this.finish();
                appViewModel.getSlangInterface().notifyNavigationUserJourneySuccess();
                return;
            }
            Intent intent = new Intent(OrderItemsActivity.this,
                    classBundlePair.first);
            if (classBundlePair.second != null) {
                intent.putExtras(classBundlePair.second);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        handleIntent(getIntent());
        appViewModel.getOrderItem()
                .observe(this, this::updateOrderItem);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        appViewModel.makeOrderItemRequest(intent.getStringExtra("orderItemId"));
        boolean mIsVoiceView = intent.getBooleanExtra("is_voice_view_order", false);
        if (mIsVoiceView) {

            //Notify SlangRetailAssistant that the order view journey is successful.
            appViewModel.getSlangInterface().notifyOrderManagementSuccess();
            return;
        }
        mIsVoiceCancel = intent.getBooleanExtra("is_voice_order_cancel", false);
        if (!mIsVoiceCancel) {
            return;
        }
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        showCancelAlert = intent.getBooleanExtra("showConfirmScreen", false);
        currentItemIndex = intent.getIntExtra("orderItemIndex", 0);
    }

    private void updateOrderItem(OrderItem orderItem) {
        totalCost.setText("");
        totalSave.setVisibility(View.GONE);
        int sum = 0;
        int sumWithoutDiscount = 0;
        for (CartItemOffer cartItem : orderItem.orderItems) {
            float price = cartItem.cart.quantity * cartItem.item.price;
            sumWithoutDiscount += price;
            Offer offerItem = cartItem.offer;
            if (offerItem != null) {
                if (cartItem.cart.quantity >= offerItem.minQuantity) {
                    price = price - (offerItem.percentageDiscount * price);
                }
            }
            sum += price;
        }
        totalCost.setText(String.format(Locale.ENGLISH, "Total: Rs %d",
                sum));
        int saved = sumWithoutDiscount - sum;
        if (saved > 0) {
            totalSave.setVisibility(View.VISIBLE);
            totalSave.setText(String.format(Locale.ENGLISH, "Saved: Rs %d", saved
            ));
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Order #" + orderItem.orderId.substring(0, 10));
        }
        OrderCartAdapter listAdapter = new OrderCartAdapter(appViewModel);
        listAdapter.setList(orderItem.orderItems);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listItemView.setLayoutManager(layoutManager);
        listItemView.setItemAnimator(null);
        listItemView.setAdapter(listAdapter);
        removeButton.setOnClickListener(view -> {
            showRemoveAction(orderItem);
        });
        if (orderItem.active) {
            orderControlSection.setBackgroundColor(Color.BLACK);
            removeButton.setEnabled(true);
            removeButton.setText("CANCEL ORDER");
        } else {
            orderControlSection.setBackgroundColor(Color.BLACK);
            removeButton.setEnabled(false);
            removeButton.setText("ORDER CANCELLED");
        }

        if (alertDialog != null) {
            alertDialog.dismiss();
        }

        if(mIsVoiceCancel) {
            if (showCancelAlert && orderItem.active) {
                showRemoveAction(orderItem);

                //Notify SlangRetailAssistant to prompt for order cancel confirmation
                appViewModel.getSlangInterface().notifyOrderManagementCancelConfirmation(currentItemIndex);
            } else if (!orderItem.active) {

                //Notify SlangRetailAssistant that order cancel user journey has resulted in confirmation success.
                appViewModel.getSlangInterface().notifyOrderManagementCancelConfirmationSuccess();
            } else {

                //Notify SlangRetailAssistant that order cancel user journey has resulted in confirmation denied.
                appViewModel.getSlangInterface().notifyOrderManagementCancelConfirmationDenied();
            }
            showCancelAlert = false;
        }

    }

    private void showRemoveAction(OrderItem orderItem) {
        alertDialog = new AlertDialog.Builder(OrderItemsActivity.this).create();
        alertDialog.setTitle("Cancel Order");
        alertDialog.setCancelable(false);
        alertDialog.setMessage("Are you sure, you want to cancel this order ?");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "YES",
                (dialog, which) -> {
                    appViewModel.removeOrderItem(orderItem);
                    if(mIsVoiceCancel) {
                        appViewModel.getSlangInterface().notifyOrderManagementCancelConfirmationSuccess();
                        mIsVoiceCancel = false;
                    }
                    dialog.dismiss();
                });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO",
                (dialog, which) -> {
                    if(mIsVoiceCancel) {
                        appViewModel.getSlangInterface().notifyOrderManagementCancelConfirmationDenied();
                        mIsVoiceCancel = false;
                    }
                    dialog.dismiss();
                });
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //Clear the context when we move out the current view.
        appViewModel.getSlangInterface().clearOrderManagementContext();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Show the slang trigger in this activity
        appViewModel.getSlangInterface().showTrigger(this);
    }


}