package in.slanglabs.sampleretailapp.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import in.slanglabs.sampleretailapp.Model.CartItem;
import in.slanglabs.sampleretailapp.Model.CartItemOffer;

@Dao
public interface CartDao {

//    @Transaction
//    @Query("SELECT * FROM cart ORDER BY date DESC")
//    LiveData<List<CartItemOffer>> getCartItems();

    @Transaction
    @Query("SELECT * FROM cart ORDER by cartId DESC")
    LiveData<List<CartItemOffer>> getCartItems();

    @Query("DELETE FROM cart")
    public void removeAllItems();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<CartItem> items);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CartItem cartItem);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(CartItem cartItem);

    @Delete
    void remove(CartItem cartItem);

    @Transaction
    @Query("SELECT * FROM cart where itemId=:id")
    LiveData<CartItem> getCartItemForId(int id);

    @Transaction
    @Query("SELECT * FROM cart where itemId=:id")
    CartItem getCartItemForIdSync(int id);
}
