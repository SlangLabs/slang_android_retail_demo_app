package in.slanglabs.sampleretailapp.UI.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderViewHolder extends RecyclerView.ViewHolder {

    private TextView orderTime;
    private TextView orderQuantities;
    private TextView orderPrice;
    private TextView orderStatus;

    public OrderViewHolder(@NonNull View itemView, ItemClickListener itemClickListener) {
        super(itemView);
        orderTime = itemView.findViewById(R.id.order_time);
        orderQuantities = itemView.findViewById(R.id.order_quantities);
        orderPrice = itemView.findViewById(R.id.order_price);
        orderStatus = itemView.findViewById(R.id.order_status);
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
        orderTime.setText(date);
        orderPrice.setText(String.format(Locale.ENGLISH,"Rs %d",orderItem.orderPrice));
        orderQuantities.setText(String.format(Locale.ENGLISH,"%d items",orderItem.numberOfItems));
        if(orderItem.active) {
            orderStatus.setBackgroundColor(itemView.getResources().getColor(R.color.colorAccent));
            orderStatus.setText("ACTIVE");
        }
        else {
            orderStatus.setBackgroundColor(itemView.getResources().getColor(R.color.colorPrimaryDark));
            orderStatus.setText("CANCELLED");
        }
    }


}
