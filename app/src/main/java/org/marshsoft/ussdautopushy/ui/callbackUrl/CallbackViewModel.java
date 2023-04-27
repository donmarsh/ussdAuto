package org.marshsoft.ussdautopushy.ui.callbackUrl;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.model.Sms;
import org.marshsoft.ussdautopushy.data.model.SmsCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallbackViewModel extends AndroidViewModel {
    private ExecutorService executorService;
    public CallbackViewModel(@NonNull Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
    }
    LiveData<List<SmsCallback>> getAllCallbacks(){
        return AppDatabase.getInstance(getApplication()).smsCallbackDao().getAll();
    }
}