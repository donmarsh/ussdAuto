package org.marshsoft.ussdautopushy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.marshsoft.ussdautopushy.data.RegisterForPushNotificationsAsync;
import org.marshsoft.ussdautopushy.data.enums.Actions;
import org.marshsoft.ussdautopushy.services.ProcessUssdCallsService;
import org.marshsoft.ussdautopushy.services.SmsProcessService;
import org.marshsoft.ussdautopushy.services.SmsWorker;

import java.util.concurrent.TimeUnit;

import me.pushy.sdk.Pushy;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,R.id.navigation_ussd,R.id.navigation_sms,R.id.navigation_callbacks, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        FirebaseApp.getInstance();
        NavigationUI.setupWithNavController(navView, navController);
        Pushy.toggleFCM(true, getApplicationContext());
        getDeviceToken();
        Pushy.listen(this);
        startUssdProcessingService();
        startSmsForwardService();
        disableBatteryOptimizations();
        final WorkManager mWorkManager = WorkManager.getInstance(getApplicationContext());
        final Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        final PeriodicWorkRequest mRetryFailedSmsRequest = new PeriodicWorkRequest.Builder(SmsWorker.class, 15, TimeUnit.MINUTES).setConstraints(constraints).build();
        mWorkManager.enqueueUniquePeriodicWork("failedSms", ExistingPeriodicWorkPolicy.REPLACE,mRetryFailedSmsRequest);

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startUssdProcessingService();
        startSmsForwardService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startUssdProcessingService();
        startSmsForwardService();
    }
    public void disableBatteryOptimizations(){
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // Check if app isn't already whitelisted from battery optimizations
        if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            // Get app name as string
            String appName = getPackageManager().getApplicationLabel(getApplicationInfo()).toString();

            // Instruct user to whitelist app from battery optimizations
            new AlertDialog.Builder(this)
                    .setTitle("Disable battery optimizations")
                    .setMessage("If you'd like to receive notifications in the background, please click OK and select \"All apps\" -> " + appName + " -> Don't optimize.")
                    .setPositiveButton("OK", (dialogInterface, i) -> {
                        // Display the battery optimization settings screen
                        startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                    })
                    .setNegativeButton("Cancel", null).show();
        }
    }
    public void getDeviceToken(){
        if (!Pushy.isRegistered(this)) {
            new RegisterForPushNotificationsAsync(this).execute();
        }


    }
    private void startSmsForwardService(){
        Intent intent = new Intent(this, SmsProcessService.class);
        intent.setAction(Actions.START.name());
        Log.d(TAG,"Starting the service in >=26 Mode");
        startForegroundService(intent);

    }
    private void startUssdProcessingService() {
        Intent intent = new Intent(this, ProcessUssdCallsService.class);
        intent.setAction(Actions.START.name());
        Log.d(TAG,"Starting the service in >=26 Mode");
        startForegroundService(intent);
    }
}
