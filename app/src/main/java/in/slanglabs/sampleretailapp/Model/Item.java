package in.slanglabs.sampleretailapp.Model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class Item {

    @PrimaryKey(autoGenerate = true)
    public int itemId;
    @NonNull
    public @ListType String type;
    @NonNull
    public int id;
    public String name;
    public String synonyms;
    public String brand;
    public Float price;
    public String size;
    public String imageUrl;
    public String gender;
    public String category;
    public String color;
    public int confidence;
}
