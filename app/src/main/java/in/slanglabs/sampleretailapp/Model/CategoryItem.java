package in.slanglabs.sampleretailapp.Model;

import androidx.annotation.ColorInt;

import java.util.Objects;

public class CategoryItem {
    public String categoryName;
    public @ColorInt
    int color;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryItem categoryItem = (CategoryItem) o;
        return categoryName.equals(categoryItem.categoryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), categoryName, color);
    }
}
