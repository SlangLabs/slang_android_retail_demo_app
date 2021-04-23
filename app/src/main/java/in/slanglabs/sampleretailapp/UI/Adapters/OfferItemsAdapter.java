package in.slanglabs.sampleretailapp.UI.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampleretailapp.Model.OfferItemCart;
import in.slanglabs.sampleretailapp.R;
import in.slanglabs.sampleretailapp.UI.ItemClickListener;
import in.slanglabs.sampleretailapp.UI.ViewHolder.ItemView;
import in.slanglabs.sampleretailapp.UI.ViewHolder.OfferViewHolder;
import in.slanglabs.sampleretailapp.UI.ViewModel.AppViewModel;

import java.util.ArrayList;
import java.util.List;

public class OfferItemsAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemClickListener {

    public static enum Type {
        HORIZONTAL_LIST,
        VERTICAL_LIST
    }

    private List<OfferItemCart> list = new ArrayList<>();
    private OfferItemsAdapter.Type type;

    private AppViewModel appViewModel;
    private ItemClickListener itemClickListener;
    private Integer maxSize;

    public OfferItemsAdapter(OfferItemsAdapter.Type type, AppViewModel appViewModel, ItemClickListener itemClickListener, Integer maxSize) {
        this.appViewModel = appViewModel;
        this.itemClickListener = itemClickListener;
        this.type = type;
        this.maxSize = maxSize;
    }

    public void setList(List<OfferItemCart> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (type == Type.VERTICAL_LIST) {
            View retailItem = LayoutInflater
                    .from(parent.getContext()).inflate(
                            R.layout.retail_item,
                            parent, false);
            return new ItemView(retailItem, this);
        } else {
            View retailItem = LayoutInflater
                    .from(parent.getContext()).inflate(
                            R.layout.offer_item,
                            parent, false);
            return new OfferViewHolder(retailItem, itemClickListener);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (type == Type.VERTICAL_LIST) {
            ItemView viewHolder = (ItemView) holder;
            viewHolder.setData(list.get(position).item, list.get(position).cart, list.get(position).offer);
        } else {
            OfferViewHolder viewHolder = (OfferViewHolder) holder;
            viewHolder.setData(list.get(position).offer);
        }
    }

    @Override
    public int getItemCount() {
        if (maxSize != null) {
            return Math.min(maxSize, list.size());
        }
        return list.size();
    }

    @Override
    public void itemClicked(int position) {
        this.itemClickListener.itemClicked(list.get(position).item);
    }

    @Override
    public void addItem(int position) {
        appViewModel.addItem(list.get(position).item);
    }

    @Override
    public void removeItem(int position) {
        appViewModel.removeItem(list.get(position).item);
    }

}