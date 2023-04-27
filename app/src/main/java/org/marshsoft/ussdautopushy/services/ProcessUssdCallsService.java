package org.marshsoft.ussdautopushy.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import org.marshsoft.ussdautopushy.MainActivity;
import org.marshsoft.ussdautopushy.R;
import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.enums.Actions;
import org.marshsoft.ussdautopushy.data.model.UssdTransaction;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ProcessUssdCallsService extends Service {
    private static final String TAG = "ProcessUssdCallsService";
    private static final String POWER_TAG = "ussdAuto:ussdServiceTag";
    private boolean isServiceStarted = false;
    private int threadDelay = 150;
    private boolean transactioninProgress = false;
    private PowerManager.WakeLock wakeLock = null;
    private ScheduledExecutorService scheduleExecutor;
    private ScheduledFuture<?> scheduleManager;
    Runnable checkandProcessTransaction;
    public ProcessUssdCallsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Some component wants to bind with the service");

       return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            String action = intent.getAction();
            if(action == null || action.equals(Actions.START.name())){
                startService();
            }
            else{
                stopService();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"Service started");
        Notification notification  = createNotification();
        startForeground(1,notification);
    }

    private void startService(){
        String delay = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("UssdDelay","150");
        threadDelay = Integer.parseInt(delay);
        if(!isServiceStarted){
            scheduleExecutor = Executors.newScheduledThreadPool(1);
            isServiceStarted = true;
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,POWER_TAG);
            wakeLock.acquire();
            ScheduledThreadPoolExecutor scheduledExecutor = (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1);
            checkandProcessTransaction = () -> {
                Log.d("runnable","running");
                if(!transactioninProgress){
                    Log.d("runnable","starting process of transaction");
                    UssdTransaction ussdTransaction = AppDatabase.getInstance(getApplicationContext()).ussdTransactionDao().getSingleTransactionbyCallbackStatus("Requested");

                    if(ussdTransaction!=null){
                        Log.d("Transaction gotten ",""+ussdTransaction.getTransactionId());
                        transactioninProgress = true;
                        makeUSSDcall(getApplicationContext() ,ussdTransaction);

                    }
                }

                String newDelay = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("UssdDelay","150");
                int latestDelay = Integer.parseInt(newDelay);
                if(latestDelay!=threadDelay){
                    threadDelay = latestDelay;
                    changeScheduleTime(threadDelay);
                }
                Log.d("Thread", ""+threadDelay);

            };

            Log.d("Thread", ""+threadDelay);
            scheduleManager = scheduleExecutor.scheduleWithFixedDelay(checkandProcessTransaction,threadDelay,threadDelay ,TimeUnit.MILLISECONDS);
        }

    }
    public void changeScheduleTime(int timeMilliSeconds){

        if (scheduleManager!= null)
        {
            scheduleManager.cancel(false);
        }
        scheduleManager = scheduleExecutor.scheduleAtFixedRate(checkandProcessTransaction, timeMilliSeconds, timeMilliSeconds, TimeUnit.MILLISECONDS);
    }
    private void stopService(){
        try{
            if(wakeLock.isHeld()){
                wakeLock.release();
            }
            scheduleExecutor.awaitTermination(5L,TimeUnit.SECONDS);
            stopForeground(true);
            stopSelf();
        }catch (Exception ex) {
            Log.d(TAG, "Service stopped without being started: "+ex.getMessage());
        }
        isServiceStarted = false;
    }
    private Notification createNotification(){
        String notificationChannelId = "USSD PROCESSOR SERVICE CHANNEL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(notificationChannelId, "Ussd Processor Service Channel", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Ussd Processor");
                channel.enableLights(true);
                channel.setLightColor(Color.BLUE);
                channel.enableVibration(true);
                channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                long[] pattern = {100, 200, 300, 400};
                channel.setVibrationPattern(pattern);


            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Notification.Builder builder = new Notification.Builder(this, notificationChannelId);
        return builder.setContentTitle("UssdProcessor Service")
                .setContentText("Ussd Processor Running")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Ussd Processor Service")
                .build();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent=  new Intent(getApplicationContext(),ProcessUssdCallsService.class);
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent  = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime()+1000,restartServicePendingIntent);

    }

    public void sendTransactiontoCallback(Context context, UssdTransaction ussdTransaction){
        Map<String, String > callbackInfo = new HashMap<>();
        String callbackUrl = ussdTransaction.getUssdCallbackUrl();
        callbackInfo.put("transactionId",Long.toString(ussdTransaction.getTransactionId()));
        callbackInfo.put("UssdFullResponse",ussdTransaction.getUssdResponse());
        callbackInfo.put("UssdStatus",""+ussdTransaction.getStatus());
        callbackInfo.put("transactionReference",""+ussdTransaction.getTransactionReference());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        callbackInfo.put("transactionTime",df.format(ussdTransaction.getTransactionTime()));
        JSONObject responseObject = new JSONObject(callbackInfo);
        String jsonString = responseObject.toString();
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, callbackUrl, response -> {
            ussdTransaction.setUssdCallbackStatus("Completed");
            updateUssdTransaction(context,ussdTransaction);
            transactioninProgress = false;
        }, error -> {
            Log.d("Volley","Error you didn't post");
            Log.d("Volley",error.toString());
            VolleyLog.d(TAG, "Error: " + error.getMessage());
            ussdTransaction.setUssdCallbackStatus("Failed");
            updateUssdTransaction(context,ussdTransaction);
            transactioninProgress = false;
            Log.e("VOLLEY", error.toString());
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return jsonString.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);

                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        queue.add(stringRequest);
    }
    public void updateUssdTransaction(Context context, UssdTransaction ussdTransaction){
        AsyncTask.execute(() -> {
            Log.d("UssdTransactionId",""+ussdTransaction.getTransactionId());
            AppDatabase.getInstance(context).ussdTransactionDao().update(ussdTransaction);

        });
    }
    public void makeUSSDcall(Context context, UssdTransaction ussdTransaction) {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Handler handler = new Handler(Looper.getMainLooper());
        Log.d("ussdCall","here");
        TelephonyManager.UssdResponseCallback responseCallback = new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                super.onReceiveUssdResponse(telephonyManager, request, response);
                ussdTransaction.setUssdResponse((String) response);
                ussdTransaction.setResponseType(true);
                ussdTransaction.setStatus("Processed");
                AsyncTask.execute(() -> {
                    Log.d("UssdTransactionId",""+ussdTransaction.getTransactionId());
                    AppDatabase.getInstance(context).ussdTransactionDao().update(ussdTransaction);
                    sendTransactiontoCallback(context,ussdTransaction);

                });
                Log.d("ussd Success", response.toString());

            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                ussdTransaction.setUssdResponse("" + failureCode);
                ussdTransaction.setStatus("Rejected");
                ussdTransaction.setResponseType(false);
                AsyncTask.execute(() -> {
                    Log.d("UssdTransactionId",""+ussdTransaction.getTransactionId());
                    AppDatabase.getInstance(context).ussdTransactionDao().update(ussdTransaction);
                    sendTransactiontoCallback(context,ussdTransaction);

                });
                Log.d("ussd Failure", "" + failureCode);


            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        telephonyManager.sendUssdRequest(ussdTransaction.getUssdNumber(), responseCallback, handler);

    }
}