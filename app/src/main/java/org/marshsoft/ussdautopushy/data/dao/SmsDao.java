package org.marshsoft.ussdautopushy.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import org.marshsoft.ussdautopushy.data.model.Sms;

import java.util.Date;
import java.util.List;

@Dao
public interface SmsDao {
    @Query("SELECT * FROM Sms order by smsId desc")
    LiveData<List<Sms>> getAll();
    @Query("SELECT * FROM Sms WHERE status=:status order by smsId asc LIMIT 1")
    Sms getSingleSmsByStatus(String status);
    @Query("SELECT * FROM Sms WHERE smsCallbackStatus=:callbackStatus order by smsId asc LIMIT 1")
    Sms getSingleSmsByCallbackStatus(String callbackStatus);
    @Query("SELECT * FROM Sms WHERE smsCallbackStatus=:callbackStatus and smsTime BETWEEN :startDate AND :endDate order by smsId asc LIMIT 1")
    Sms getSingleSmsTodayByCallbackStatus(String callbackStatus, Date startDate, Date endDate);
    @Insert
    Long insert(Sms sms);
    @Update
    void update(Sms sms);
    @Delete
    void delete(Sms sms);
}
