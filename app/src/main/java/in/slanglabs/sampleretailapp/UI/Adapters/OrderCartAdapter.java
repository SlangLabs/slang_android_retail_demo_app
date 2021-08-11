package in.slanglabs.sampleretailapp.UI.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampleretailapp.Model.CartItemOffer;
import in.slanglabs.sampleretailapp.Model.ItemOfferCart;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ViewHolder.OrderCartViewHolder;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class OrderCartAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CartItemOffer> mList = new ArrayList<>();

    private AppViewModel mAppViewModel;

    public OrderCartAdapter(AppViewModel appViewModel) {
        this.mAppViewModel = appViewModel;
    }

    public void setList(List<CartItemOffer> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View orderCartItem = LayoutInflater
                .from(parent.getContext()).inflate(
                        R.layout.order_cart_item,
                        parent, false);
        return new OrderCartViewHolder(orderCartItem);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        OrderCartViewHolder viewHolder = (OrderCartViewHolder) holder;
        viewHolder.setData(mList.get(position).item,mList.get(position).cart, mList.get(position).offer);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
