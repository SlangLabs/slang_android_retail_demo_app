package in.slanglabs.sampleretailapp.UI.ViewHolder;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StrikethroughSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import in.slanglabs.sampleretailapp.Model.CartItem;
import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.Offer;
import in.slanglabs.sampleretailapp.R;

import java.util.Locale;

public class OrderCartViewHolder extends RecyclerView.ViewHolder {

    private TextView itemName;
    private TextView quantities;
    private TextView currentNumber;
    private TextView price;
    private TextView brandName;
    private ImageView imageView;

    public OrderCartViewHolder(@NonNull View itemView) {
        super(itemView);
        itemName = itemView.findViewById(R.id.item_name);
        brandName = itemView.findViewById(R.id.item_brand_name);
        quantities = itemView.findViewById(R.id.item_quantities);
        currentNumber = itemView.findViewById(R.id.item_total_quantity);
        price = itemView.findViewById(R.id.item_price);
        imageView = itemView.findViewById(R.id.imageView);
    }

    public void setData(Item item, CartItem cartItem, Offer offerItem) {
        itemName.setText(item.name);
        brandName.setText(item.brand);
        quantities.setText(String.format(Locale.ENGLISH, "%s", item.size));
        currentNumber.setText("0");
        currentNumber.setText(String.format(Locale.ENGLISH, "Quantity : %d", cartItem.quantity));
        if(item.imageUrl != null) {
            Glide.with(itemView).load(item.imageUrl).into(imageView);
        }
        else {
            Glide.with(itemView).load(getImageUrl(item.name, item.type)).into(imageView);
        }

        int totalPrice = (int) (cartItem.quantity * item.price);
        String priceString = String.format(Locale.ENGLISH, "Rs %d",
                totalPrice);
        price.setText(priceString);

        if (offerItem == null) {
            return;
        }
        if (cartItem.quantity >= offerItem.minQuantity) {
            int discountedPrice;
            discountedPrice = (int) (totalPrice - (totalPrice * offerItem.percentageDiscount));
            priceString = String.format(Locale.ENGLISH, "Rs %d\nRs %d",
                    totalPrice, discountedPrice);
        }
        Spannable spannable = new SpannableString(priceString);
        if (cartItem.quantity >= offerItem.minQuantity) {
            spannable.setSpan(
                    new StrikethroughSpan(),
                    0, priceString.lastIndexOf("\n"),
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
