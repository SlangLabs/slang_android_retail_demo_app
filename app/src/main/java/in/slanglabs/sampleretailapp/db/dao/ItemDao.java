package in.slanglabs.sampleretailapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import in.slanglabs.sampleretailapp.Model.Item;
import in.slanglabs.sampleretailapp.Model.ItemOfferCart;
import in.slanglabs.sampleretailapp.Model.ItemsFts;
import in.slanglabs.sampleretailapp.Model.ListType;
import in.slanglabs.sampleretailapp.Model.Offer;
import in.slanglabs.sampleretailapp.Model.OrderItem;

@Dao
public interface ItemDao {

    @Transaction
    @Query("SELECT * FROM items")
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    LiveData<List<ItemOfferCart>> getItemsOffersCart();

    @Transaction
    @Query("SELECT * FROM items")
    LiveData<List<Item>> getItems();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(List<Item> items);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Item item);

    @RawQuery(observedEntities = {Item.class, Offer.class, OrderItem.class})
    LiveData<List<ItemOfferCart>> getItemsAndOffersBasedOnSearchFts(SupportSQLiteQuery query);

    @RawQuery(observedEntities = {Item.class, Offer.class, OrderItem.class})
    List<ItemOfferCart> getItemsAndOffersBasedOnSearchFtsSync(SupportSQLiteQuery query);

    @Transaction
    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT * FROM items where itemId = :id")
    LiveData<ItemOfferCart> getItemForId(int id);

    @Transaction
    @Query("SELECT DISTINCT brand FROM items where type = :type")
    LiveData<List<String>> getItemsBrands(@ListType String type);

    @Transaction
    @Query("SELECT DISTINCT size FROM items where type = :type")
    LiveData<List<String>> getItemsSizes(@ListType String type);

    @Transaction
    @Query("SELECT DISTINCT color FROM items where type = :type")
    LiveData<List<String>> getItemColors(@ListType String type);

    @Transaction
    @Query("SELECT DISTINCT category FROM items where type = :type")
    LiveData<List<String>> getItemCategories(@ListType String type);

    @Transaction
    @Query("SELECT DISTINCT gender FROM items where type = :type")
    LiveData<List<String>> getItemGenders(@ListType String type);

    @Query("SELECT * FROM items WHERE itemId = :id LIMIT 1")
    Item getItemId(int id);
}
