package in.slanglabs.sampleretailapp.UI.Activities;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Locale;

import in.slanglabs.assistants.retail.SearchUserJourney;
import in.slanglabs.assistants.retail.SlangRetailAssistant;
import in.slanglabs.sampleretailapp.Model.CartItem;
import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.Offer;
import in.slanglabs.sampleretailapp.R;

public class ItemActivity extends BaseActivity {

    private TextView itemName;
    private TextView imageName;
    private TextView quantities;
    private Button addItem;
    private Button removeItem;
    private Button addButton;
    private View controlButton;
    private TextView currentNumber;
    private TextView price;
    private TextView offerText;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        itemName = findViewById(R.id.item_name);
        imageName = findViewById(R.id.image_item_name);
        quantities = findViewById(R.id.item_quantities);
        controlButton = findViewById(R.id.control_buttons);
        addButton = findViewById(R.id.add_button);
        addItem = findViewById(R.id.item_add);
        currentNumber = findViewById(R.id.item_current_number);
        price = findViewById(R.id.item_price);
        offerText = findViewById(R.id.item_offer_text);
        removeItem = findViewById(R.id.item_remove);
        imageView = findViewById(R.id.imageView);

        int itemId = getIntent().getIntExtra("itemId", 0);

        appViewModel.getItemForId(itemId).observe(this, itemOfferCart -> {
            handleItem(itemOfferCart.cart, itemOfferCart.offer, itemOfferCart.item);
            addItem.setOnClickListener(view -> {
                appViewModel.addItem(itemOfferCart.item);
            });
            addButton.setOnClickListener(view -> {
                appViewModel.addItem(itemOfferCart.item);
            });
            removeItem.setOnClickListener(view -> {
                appViewModel.removeItem(itemOfferCart.item);
            });

            if(itemOfferCart.item.imageUrl != null) {
                Glide.with(this).load(itemOfferCart.item.imageUrl).into(imageView);
            }
            else {
                Glide.with(this).load(getImageUrl(itemOfferCart.item.name, itemOfferCart.item.type))
                        .into(imageView);
            }
        });
    }

    private void handleItem(CartItem item, Offer offerItem, Item listItem) {
        addButton.setVisibility(View.VISIBLE);
        controlButton.setVisibility(View.GONE);
        itemName.setText(listItem.name);
        imageName.setText(String.format(Locale.ENGLISH, "%s %s", listItem.name, listItem.size));
        quantities.setText(String.format(Locale.ENGLISH, "%s", listItem.size));
        currentNumber.setText("0");
        String priceString = String.format(Locale.ENGLISH, "Rs %.1f",
                listItem.price);
        price.setText(priceString);
        if (item != null && offerItem != null) {
            if (item.quantity >= offerItem.minQuantity) {
                float discountedPrice;
                discountedPrice = (float) (listItem.price - (listItem.price * offerItem.percentageDiscount));
                priceString = String.format(Locale.ENGLISH, "Rs %.1f\nRs %.1f",
                        listItem.price, discountedPrice);
                Spannable spannable = new SpannableString(priceString);
                spannable.setSpan(
                        new StrikethroughSpan(),
                        0, priceString.lastIndexOf("\n"),
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                price.setText(spannable);
            }
        }
        if (item != null) {
            currentNumber.setText(String.format(Locale.ENGLISH, "%d",
                    item.quantity));
            addButton.setVisibility(View.GONE);
            controlButton.setVisibility(View.VISIBLE);
        }
        offerText.setVisibility(View.GONE);
        if (offerItem != null) {
            offerText.setVisibility(View.VISIBLE);
            offerText.setText(String.format(Locale.ENGLISH, "Buy %d and get %d%% off",
                    offerItem.minQuantity,
                    (int) (offerItem.percentageDiscount * 100)));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Show the slang trigger in this activity
        appViewModel.getSlangInterface().showTrigger(this);
    }

    private String getImageUrl(String name, @ListType String category) {
        String imageUrl = "";
        switch (category) {
            case ListType.GROCERY:
                imageUrl = "https://www.honestbee.tw/images/placeholder.jpg";
                break;
            case ListType.FASHION:
                break;
            case ListType.PHARMACY:
                imageUrl = "https://previews.123rf.com/images/happyicon/happyicon1805/happyicon180500042/100632499-vector-pills-icon.jpg";
                break;
        }

        if (name.toLowerCase().contains("tomato")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/40022638_3-fresho-tomato-local-organically-grown.jpg";
        } else if (name.toLowerCase().contains("onion")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/40023472_3-fresho-onion-organically-grown.jpg";
        } else if (name.toLowerCase().contains("potato")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/40023476_4-fresho-potato-organically-grown.jpg";
        } else if (name.toLowerCase().contains("maggi")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/266109_15-maggi-2-minute-instant-noodles-masala.jpg";
        } else if (name.toLowerCase().contains("aashirvaad atta")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/126906_7-aashirvaad-atta-whole-wheat.jpg";
        }

        return imageUrl;
    }
}