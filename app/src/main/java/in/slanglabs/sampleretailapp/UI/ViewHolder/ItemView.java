package in.slanglabs.sampleretailapp.UI.ViewHolder;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import in.slanglabs.sampleretailapp.Model.CartItem;
import in.slanglabs.sampleretailapp.Model.CartItemOffer;
import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ItemOfferCart;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.Offer;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;

import java.util.Locale;

public class ItemView extends RecyclerView.ViewHolder {

    private TextView itemName;
    private TextView imageName;
    private TextView quantities;
    private TextView addButton;
    private View controlButton;
    private TextView currentNumber;
    private TextView price;
    private TextView offerText;
    private TextView brandName;
    private ImageView imageView;

    public ItemView(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        itemName = itemView.findViewById(R.id.item_name);
        imageName = itemView.findViewById(R.id.image_item_name);
        brandName = itemView.findViewById(R.id.item_brand_name);
        quantities = itemView.findViewById(R.id.item_quantities);
        imageView = itemView.findViewById(R.id.imageView);
        controlButton = itemView.findViewById(R.id.control_buttons);
        addButton = itemView.findViewById(R.id.add_button);
        ImageButton addItem = itemView.findViewById(R.id.item_add);
        addItem.setOnClickListener(view -> itemClickListener.addItem(getAdapterPosition()));
        addButton.setOnClickListener(view -> itemClickListener.addItem(getAdapterPosition()));
        ImageButton removeItem = itemView.findViewById(R.id.item_remove);
        removeItem.setOnClickListener(view -> itemClickListener.removeItem(getAdapterPosition()));
        currentNumber = itemView.findViewById(R.id.item_current_number);
        price = itemView.findViewById(R.id.item_price);
        offerText = itemView.findViewById(R.id.item_offer_text);
        itemView.setOnClickListener(view -> itemClickListener.itemClicked(getAdapterPosition()));
    }

    public void setData(Item listItem, CartItem item, Offer offerItem) {
        addButton.setVisibility(View.VISIBLE);
        controlButton.setVisibility(View.GONE);
        quantities.setVisibility(View.GONE);
        itemName.setText(listItem.name);
        brandName.setText(listItem.brand);
        imageName.setText(String.format(Locale.ENGLISH, "%s", listItem.name));
        if(listItem.imageUrl != null && !listItem.imageUrl.isEmpty()) {
            Glide.with(itemView).load(listItem.imageUrl).into(imageView);
        }
        else {
            Glide.with(itemView).load(getImageUrl(listItem.name, listItem.type)).into(imageView);
        }

        if(!listItem.size.equalsIgnoreCase("")) {
            quantities.setText(String.format(Locale.ENGLISH, "%s", listItem.size));
            quantities.setVisibility(View.VISIBLE);
        }
        currentNumber.setText("0");
        String priceString = String.format(Locale.ENGLISH, "Rs %.1f",
                listItem.price);
        price.setText(priceString);
        if (item != null && offerItem != null) {
            if (item.quantity >= offerItem.minQuantity) {
                float discountedPrice;
                discountedPrice = (float) (listItem.price - (listItem.price * offerItem.percentageDiscount));
                priceString = String.format(Locale.ENGLISH, "Rs %.1f\t Rs %.1f",
                        listItem.price, discountedPrice);
                Spannable spannable = new SpannableString(priceString);
                spannable.setSpan(
                        new StrikethroughSpan(),
                        0, priceString.lastIndexOf("\t"),
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
        offerText.setText("");
        if (offerItem != null) {
            offerText.setText(String.format(Locale.ENGLISH, "Buy %d and get %d%% off",
                    offerItem.minQuantity,
                    (int) (offerItem.percentageDiscount * 100)));
        }
    }

    public void setData(CartItemOffer listItem) {
        addButton.setVisibility(View.GONE);
        controlButton.setVisibility(View.VISIBLE);
        itemName.setText(listItem.item.name);
        quantities.setVisibility(View.GONE);
        imageName.setText(String.format(Locale.ENGLISH, "%s", listItem.item.name));
        if(listItem.item.imageUrl != null && !listItem.item.imageUrl.isEmpty()) {
            Glide.with(itemView).load(listItem.item.imageUrl).into(imageView);
        }
        else {
            Glide.with(itemView).load(getImageUrl(listItem.item.name, listItem.item.type)).into(imageView);
        }

        if(!listItem.item.size.equalsIgnoreCase("")) {
            quantities.setText(String.format(Locale.ENGLISH, "%s", listItem.item.size));
            quantities.setVisibility(View.VISIBLE);
        }

        currentNumber.setText("0");
        currentNumber.setText(String.format(Locale.ENGLISH, "%d",
                listItem.cart.quantity));
        float totalPrice = (float) (listItem.cart.quantity * listItem.item.price);
        String priceString = String.format(Locale.ENGLISH, "Rs %.1f",
                totalPrice);
        price.setText(priceString);
        offerText.setText("");

        if (listItem.offer == null) {
            return;
        }
        offerText.setText(String.format(Locale.ENGLISH, "Buy %d and get %d%% off",
                listItem.offer.minQuantity,
                (int) (listItem.offer.percentageDiscount * 100)));
        if (listItem.cart.quantity >= listItem.offer.minQuantity) {
            float discountedPrice;
            discountedPrice = (float) (totalPrice - (totalPrice * listItem.offer.percentageDiscount));
            priceString = String.format(Locale.ENGLISH, "Rs %.1f\t Rs %.1f",
                    totalPrice, discountedPrice);
        }
        Spannable spannable = new SpannableString(priceString);
        if (listItem.cart.quantity >= listItem.offer.minQuantity) {
            spannable.setSpan(
                    new StrikethroughSpan(),
                    0, priceString.lastIndexOf("\t"),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        price.setText(spannable);
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

        if(name == null) {
            return "";
        }

        if (name.toLowerCase().contains("tomato -")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/40022638_3-fresho-tomato-local-organically-grown.jpg";
        } else if (name.toLowerCase().contains("onion -")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/40023472_3-fresho-onion-organically-grown.jpg";
        } else if (name.toLowerCase().contains("potato -")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/40023476_4-fresho-potato-organically-grown.jpg";
        } else if (name.toLowerCase().contains("maggi")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/266109_15-maggi-2-minute-instant-noodles-masala.jpg";
        } else if (name.toLowerCase().contains("aashirvaad atta")) {
            imageUrl = "https://www.bigbasket.com/media/uploads/p/s/126906_7-aashirvaad-atta-whole-wheat.jpg";
        }

        return imageUrl;
    }

}
