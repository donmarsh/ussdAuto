package org.marshsoft.ussdautopushy.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.preference.PreferenceManager;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.marshsoft.ussdautopushy.MainActivity;
import org.marshsoft.ussdautopushy.R;
import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.enums.Actions;
import org.marshsoft.ussdautopushy.data.model.Sms;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SmsProcessService extends Service {
    private static final String TAG = "SmsProcessService";
    private static final String POWER_TAG = "ussdAuto:processSmsServiceTag";
    private boolean isServiceStarted = false;
    private int threadDelay = 150;
    private boolean transactionInProgress = false;
    private PowerManager.WakeLock wakeLock = null;
    private ScheduledExecutorService scheduleExecutor;
    private ScheduledFuture<?> scheduleManager;
    Runnable checkAndProcessSms;
    public SmsProcessService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
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
        Log.d(TAG,"Process Sms Service started");
        Notification notification  = createNotification();
        startForeground(2,notification);
    }

    private void startService(){
        String delay = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("SmsDelay","150");
        threadDelay = Integer.parseInt(delay);
        if(!isServiceStarted){
            scheduleExecutor = Executors.newScheduledThreadPool(1);
            isServiceStarted = true;
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,POWER_TAG);
            wakeLock.acquire();
            checkAndProcessSms = () -> {
                Log.d("runnable","running");
                if(!transactionInProgress){
                    Log.d("runnable","Starting processing of sms");
                    Sms sms = AppDatabase.getInstance(getApplicationContext()).smsDao().getSingleSmsByCallbackStatus("Received");
                    if(sms!=null){
                        Log.d("Sms gotten",""+sms.getSmsId());
                        transactionInProgress = true;
                        forwardSms(getApplicationContext() ,sms);

                    }
                }

                String newDelay = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("SmsDelay","150");
                int latestDelay = Integer.parseInt(newDelay);
                if(latestDelay!=threadDelay){
                    threadDelay = latestDelay;
                    changeScheduleTime(threadDelay);
                }
                Log.d("Thread", ""+threadDelay);

            };

            Log.d("Thread", ""+threadDelay);
            scheduleManager = scheduleExecutor.scheduleWithFixedDelay(checkAndProcessSms,threadDelay,threadDelay , TimeUnit.MILLISECONDS);
        }

    }

    public void changeScheduleTime(int timeMilliSeconds){

        if (scheduleManager!= null)
        {
            scheduleManager.cancel(false);
        }
        scheduleManager = scheduleExecutor.scheduleAtFixedRate(checkAndProcessSms, timeMilliSeconds, timeMilliSeconds, TimeUnit.MILLISECONDS);
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
        String notificationChannelId = "SMS PROCESSOR SERVICE CHANNEL";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(notificationChannelId, "Sms Processor Service Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Sms Processor");
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
            long[] pattern = {100, 200, 300, 400};
            channel.setVibrationPattern(pattern);


            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        Notification.Builder builder = new Notification.Builder(this, notificationChannelId);
        return builder.setContentTitle("SmsProcessor Service")
                .setContentText("Sms Processor Running")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Sms Processor Service")
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

    public void updateSms(Context context, Sms sms){
        AsyncTask.execute(() -> {
            Log.d("SmsId",""+sms.getSmsId());
            AppDatabase.getInstance(context.getApplicationContext()).smsDao().update(sms);

        });
    }
    public void forwardSms(Context context, Sms sms ){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        String forwardUrl = sms.getSmsCallbackUrl();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        String jsonString = gson.toJson(sms);
        Log.d("GSON",jsonString);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, forwardUrl, response -> {
            sms.setSmsCallbackStatus("Completed");
            updateSms(context,sms);
            Log.i("VOLLEY", response);
            transactionInProgress = false;

        }, error -> {
            sms.setSmsCallbackStatus("Failed");
            updateSms(context,sms);
            Log.e("VOLLEY", error.toString());
            transactionInProgress = false;

        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() {
                return jsonString == null ? null : jsonString.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);

                }
                assert response != null;
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };


        requestQueue.add(stringRequest);

    }

}
