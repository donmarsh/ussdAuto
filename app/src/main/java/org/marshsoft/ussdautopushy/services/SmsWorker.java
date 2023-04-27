package org.marshsoft.ussdautopushy.services;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.model.Sms;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class SmsWorker extends Worker {
    public SmsWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        Log.d("Sms Worker","Work starting");
        Date date = new Date();
        Instant inst = date.toInstant();
        LocalDate localDate = inst.atZone(ZoneId.systemDefault()).toLocalDate();
        Instant dayInst = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date startDate = Date.from(dayInst);
        LocalDateTime now = LocalDateTime.now();
        Date endDate = Date.from(now.with(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
        Log.d("Start Date", startDate.toString());
        Log.d("End Date", endDate.toString());
        Sms failedSms = AppDatabase.getInstance(this.getApplicationContext()).smsDao().getSingleSmsTodayByCallbackStatus("Failed",startDate,endDate);
        if(failedSms!=null){
            Log.d("Sms Worker","Sms Found");
            forwardSms(this.getApplicationContext(),failedSms);
            Data outputData = new Data.Builder().putString("Sms Found", "Sms Forwarded").build();
            return Result.success(outputData);

        }
        else{
            Log.d("Sms Worker","Sms Not Found");
            Data outputData = new Data.Builder().putString("No sms Found", "No job Found").build();
            return Result.success(outputData);

        }

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

        }, error -> {
            sms.setSmsCallbackStatus("Failed");
            updateSms(context,sms);
            Log.e("VOLLEY", error.toString());

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
