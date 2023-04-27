package org.marshsoft.ussdautopushy.ui.ussd;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.model.UssdTransaction;

import java.util.List;

public class UssdViewModel extends AndroidViewModel {

    public UssdViewModel(@NonNull Application application) {
        super(application);

    }
    LiveData<List<UssdTransaction>> getAllUssdTransactions(){
        return AppDatabase.getInstance(getApplication()).ussdTransactionDao().getAll();
    }
}