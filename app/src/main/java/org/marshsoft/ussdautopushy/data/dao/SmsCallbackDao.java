package org.marshsoft.ussdautopushy.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import org.marshsoft.ussdautopushy.data.model.SmsCallback;

import java.util.List;
@Dao
public interface SmsCallbackDao {
    @Query("SELECT * FROM SmsCallback order by smsCallbackId asc")
    LiveData<List<SmsCallback>> getAll();
    @Query("SELECT * FROM SmsCallback WHERE smsSender = :smsSender")
    SmsCallback loadBySender(String smsSender);
    @Query("SELECT smsSender FROM SmsCallback")
    List<String> getAllSenders();
    @Insert
    Long insert(SmsCallback smsCallback);
    @Update
    void update(SmsCallback smsCallback);
    @Delete
    void delete(SmsCallback smsCallback);
}
