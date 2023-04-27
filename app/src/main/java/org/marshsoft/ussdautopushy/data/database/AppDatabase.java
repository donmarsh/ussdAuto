package org.marshsoft.ussdautopushy.data.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import org.jetbrains.annotations.NotNull;
import org.marshsoft.ussdautopushy.data.converter.DateTimeConverter;
import org.marshsoft.ussdautopushy.data.dao.SmsCallbackDao;
import org.marshsoft.ussdautopushy.data.dao.SmsDao;
import org.marshsoft.ussdautopushy.data.dao.UssdTransactionDao;
import org.marshsoft.ussdautopushy.data.model.Sms;
import org.marshsoft.ussdautopushy.data.model.SmsCallback;
import org.marshsoft.ussdautopushy.data.model.UssdTransaction;

@Database(entities = {Sms.class, UssdTransaction.class, SmsCallback.class},exportSchema = false,version = 18)
@TypeConverters({DateTimeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "ussdDatabase";
    private static final String LOG_TAG = AppDatabase.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static AppDatabase sInstance;
    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "Creating new database instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME).addMigrations(MIGRATION_15_16,MIGRATION_16_17,MIGRATION_17_18).build();

            }
        }
        Log.d(LOG_TAG, "Getting the database instance");
        return sInstance;
    }
    static final Migration MIGRATION_15_16 = new Migration(15, 16) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Sms "
                    + " ADD COLUMN smsCallbackStatus TEXT");
            database.execSQL("UPDATE Sms SET smsCallbackStatus = status");
            database.execSQL("ALTER TABLE UssdTransaction "
                    + " ADD COLUMN ussdCallbackStatus TEXT");
            database.execSQL("UPDATE UssdTransaction SET ussdCallbackStatus = status");
        }
    };
    static final Migration MIGRATION_16_17 = new Migration(16, 17) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE Token");
        }
    };
    static final Migration MIGRATION_17_18 = new Migration(17, 18) {
        @Override
        public void migrate(@NotNull SupportSQLiteDatabase database) {

        }
    };
    public abstract SmsDao smsDao();
    public abstract SmsCallbackDao smsCallbackDao();
    public abstract UssdTransactionDao ussdTransactionDao();


}
