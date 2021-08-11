package in.slanglabs.sampleretailapp.UI.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampleretailapp.Model.OrderItem;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;
import in.slanglabs.sampleretailapp.UI.ViewHolder.OrderViewHolder;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ItemClickListener mItemClickListener;

    public OrderAdapter(ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    private List<OrderItem> mList = new ArrayList<>();

    public void setList(List<OrderItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View orderItem = LayoutInflater
                .from(parent.getContext()).inflate(
                        R.layout.order_item,
                        parent, false);
        return new OrderViewHolder(orderItem,mItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OrderViewHolder viewHolder = (OrderViewHolder) holder;
        viewHolder.setData(mList.get(position));
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
