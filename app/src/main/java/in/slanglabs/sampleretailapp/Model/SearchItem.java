package in.slanglabs.sampleretailapp.Model;

import java.io.Serializable;
import java.util.Objects;

public class SearchItem implements Serializable {
    public String name = "";
    public String brandName = "";
    public String productName = "";
    public String size = "";
    public int quantity = 0;
    public boolean isAddToCart = false;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchItem that = (SearchItem) o;
        return quantity == that.quantity && name.equalsIgnoreCase(that.name)
                && size.equalsIgnoreCase(that.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, brandName, size, quantity, isAddToCart);
    }
}
