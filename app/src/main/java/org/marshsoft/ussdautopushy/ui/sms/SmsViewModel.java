package org.marshsoft.ussdautopushy.ui.sms;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.model.Sms;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SmsViewModel extends AndroidViewModel {


    private ExecutorService executorService;
    public SmsViewModel(@NonNull Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
    }
    LiveData<List<Sms>> getAllSms(){
        return AppDatabase.getInstance(getApplication()).smsDao().getAll();
    }

}