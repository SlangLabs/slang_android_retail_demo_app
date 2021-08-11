package in.slanglabs.sampleretailapp.UI.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampleretailapp.BuildConfig;
import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    private TextView mOrderTime;
    private TextView mOrderQuantities;
    private TextView mOrderPrice;
    private TextView mOrderStatus;

    public OrderViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        mOrderTime = itemView.findViewById(R.id.order_time);
        mOrderQuantities = itemView.findViewById(R.id.order_quantities);
        mOrderPrice = itemView.findViewById(R.id.order_price);
        mOrderStatus = itemView.findViewById(R.id.order_status);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemClickListener.itemClicked(getAdapterPosition());
            }
        });
    }

    public void setData(OrderItem orderItem) {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy hh:mm aa");
        String date = format.format(orderItem.orderTime);
        mOrderTime.setText(date);
        mOrderPrice.setText(String.format(Locale.ENGLISH,"%s %d", BuildConfig.CURRENCY_TYPE, orderItem.orderPrice));
        mOrderQuantities.setText(String.format(Locale.ENGLISH,"%d items",orderItem.numberOfItems));
        if(orderItem.active) {
            mOrderStatus.setBackgroundColor(itemView.getResources().getColor(R.color.colorAccent));
            mOrderStatus.setText("ACTIVE");
        }
        else {
            mOrderStatus.setBackgroundColor(itemView.getResources().getColor(R.color.colorPrimaryDark));
            mOrderStatus.setText("CANCELLED");
        }
    }


}
