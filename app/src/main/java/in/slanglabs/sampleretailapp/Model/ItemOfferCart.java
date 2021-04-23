package in.slanglabs.sampleretailapp.Model;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ItemOfferCart {
    @Embedded
    public Item item;
    @Relation(
            parentColumn = "itemId",
            entityColumn = "itemId",
            entity = Offer.class
    )
    public Offer offer;
    @Relation(
            parentColumn = "itemId",
            entityColumn = "itemId"
    )
    public CartItem cart;
}
