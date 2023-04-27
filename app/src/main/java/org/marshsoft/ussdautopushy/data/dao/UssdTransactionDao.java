package org.marshsoft.ussdautopushy.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.marshsoft.ussdautopushy.data.model.UssdTransaction;

import java.util.List;
@Dao
public interface UssdTransactionDao {
    @Query("SELECT * FROM UssdTransaction order by transactionId desc")
    LiveData<List<UssdTransaction>> getAll();
    @Query ("SELECT * FROM UssdTransaction WHERE transactionId = :id LIMIT 1")
    UssdTransaction getTransactionByTransactionId(long id);

    @Query("SELECT * FROM UssdTransaction WHERE transactionReference = :transactionReference LIMIT 1")
    UssdTransaction getTransactionByTransactionReference(String transactionReference);
    @Query("SELECT * FROM UssdTransaction WHERE status=:status order by transactionId asc LIMIT 1")
    UssdTransaction getSingleTransactionbyStatus(String status);
    @Query("SELECT * FROM UssdTransaction WHERE ussdCallbackStatus=:callbackStatus order by transactionId asc LIMIT 1")
    UssdTransaction getSingleTransactionbyCallbackStatus(String callbackStatus);
    @Delete
    void delete(UssdTransaction ussdTransaction);
    @Insert
    Long insert(UssdTransaction ussdTransaction);
    @Update
    void update(UssdTransaction ussdTransaction);
}
