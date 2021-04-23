package in.slanglabs.sampleretailapp.UI.ViewHolder;

import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampleretailapp.Model.CategoryItem;
import in.slanglabs.sampleretailapp.R;

public class CategoryViewHolder extends RecyclerView.ViewHolder {

    private ImageView categoryImage;
    private TextView categoryName;

    public CategoryViewHolder(@NonNull View itemView) {
        super(itemView);
        categoryImage = itemView.findViewById(R.id.category_image);
        categoryName = itemView.findViewById(R.id.category_name);
    }

    public void setData(CategoryItem categoryItem) {
        categoryImage.getBackground().setColorFilter(categoryItem.color, PorterDuff.Mode.SRC_OVER);
        categoryName.setText(categoryItem.categoryName);
    }

}
