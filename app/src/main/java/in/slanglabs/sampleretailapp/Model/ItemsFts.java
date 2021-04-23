package in.slanglabs.sampleretailapp.Model;

import androidx.room.Entity;
import androidx.room.Fts4;

@Fts4(contentEntity = Item.class)
@Entity(tableName = "itemsFts")
public class ItemsFts {
    public String name;
    public String brand;
    public String synonms;
    public String size;
    public String gender;
    public String category;
    public String color;
}
