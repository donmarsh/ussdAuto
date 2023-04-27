package org.marshsoft.ussdautopushy.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.marshsoft.ussdautopushy.MainActivity;
import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.model.SmsCallback;
import org.marshsoft.ussdautopushy.data.model.UssdTransaction;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PushReceiver extends BroadcastReceiver {
    private static final String TAG = "PushReceiverService";
    @Override
    public void onReceive(Context context, Intent intent) {
         Intent mainActivityIntent = new Intent(context, MainActivity.class);
         mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
         mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainActivityIntent);
        if (intent.getStringExtra("body") != null) {
            String token = intent.getStringExtra("body");
            if (verifyToken(token)) {
                DecodedJWT jwt = JWT.decode(token);
                Map<String, Claim> claims = jwt.getClaims();
                Log.d("Pushy",jwt.getSubject());
                if(jwt.getSubject().equals("SmsCallback Settings")){
                    String smsSettingsJson = Objects.requireNonNull(claims.get("smsCallbackSettings")).asString();
                    try {
                        JSONObject obj = new JSONObject(smsSettingsJson);
                        JSONArray settingsArray = obj.getJSONArray("senderSettings");
                        saveSettings(context,settingsArray);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
                else{
                    String transactionReference = Objects.requireNonNull(claims.get("transactionReference")).asString();
                    String ussdNumber = Objects.requireNonNull(claims.get("ussdNumber")).asString();
                    String callbackUrl = Objects.requireNonNull(claims.get("ussdCallbackUrl")).asString();

                    //save to Database
                    UssdTransaction ussdTransaction = new UssdTransaction();
                    ussdTransaction.setTransactionReference(transactionReference);
                    ussdTransaction.setUssdNumber(ussdNumber);
                    ussdTransaction.setStatus("Requested");
                    ussdTransaction.setUssdCallbackStatus("Requested");
                    ussdTransaction.setUssdCallbackUrl(callbackUrl);
                    ussdTransaction.setResponseType(false);
                    ussdTransaction.setTransactionTime(new Date());
                    long currentId = insertUssdTransaction(context, ussdTransaction);
                    Log.d("Insert ID", "" + currentId);
                }



            } else {
                Log.d("Pushy", "No payload found");
            }
        }
    }

    public void saveSettings(Context context,JSONArray settingsArray){
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Callable<Void> saveSettingsCallable = () ->{
          for(int i=0;i<settingsArray.length();i++){
              JSONObject currentObject = settingsArray.getJSONObject(i);
              String sender = currentObject.getString("smsSender");
              String callbackUrl = currentObject.getString("smsCallbackUrl");
              SmsCallback smsCallback = AppDatabase.getInstance(context.getApplicationContext()).smsCallbackDao().loadBySender(sender);
              if(smsCallback!=null){
                  smsCallback.setCallbackUrl(callbackUrl);
                  AppDatabase.getInstance(context.getApplicationContext()).smsCallbackDao().update(smsCallback);
              }
              else{
                  SmsCallback newCallback = new SmsCallback();
                  newCallback.setCallbackUrl(callbackUrl);
                  newCallback.setSmsSender(sender);
                  AppDatabase.getInstance(context.getApplicationContext()).smsCallbackDao().insert(newCallback);
              }
          }
          return null;
        };
        executorService.submit(saveSettingsCallable);
    }
    public long insertUssdTransaction(Context context, UssdTransaction ussdTransaction) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Callable<Long> insertCallable = () -> {
            UssdTransaction existingTransaction = AppDatabase.getInstance(context.getApplicationContext()).ussdTransactionDao().getTransactionByTransactionReference(ussdTransaction.getTransactionReference());
            if(existingTransaction==null){
                return AppDatabase.getInstance(context.getApplicationContext()).ussdTransactionDao().insert(ussdTransaction);
            }
            else{
                Log.d("Pushy","Transaction already exists");
                sendTransactionToCallback(context, existingTransaction);
                return existingTransaction.getTransactionId();
            }
        };
        Long rowId = 0L;

        Future<Long> future = executorService.submit(insertCallable);
        try {
            rowId = future.get();
            if(rowId!=0L){
                ussdTransaction.setTransactionId(rowId);
            }
        } catch (InterruptedException | ExecutionException e1) {
            e1.printStackTrace();
        }
        return rowId;
    }
    public void sendTransactionToCallback(Context context, UssdTransaction ussdTransaction){
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
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, callbackUrl, response -> {
            ussdTransaction.setUssdCallbackStatus("Completed");
            updateUssdTransaction(context,ussdTransaction);
        }, error -> {
            Log.d("Volley",error.toString());
            VolleyLog.d(TAG, "Error: " + error.getMessage());
            ussdTransaction.setUssdCallbackStatus("Failed");
            updateUssdTransaction(context,ussdTransaction);
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
                assert response != null;
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

    public boolean verifyToken(String token){

        try {
            Algorithm algorithm = Algorithm.HMAC256("abcd");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("test")
                    .build(); //Reusable verifier instance
            verifier.verify(token);
            return true;
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
            Log.d("Jwt Verification",exception.getMessage());
            return false;
        }
    }
}
