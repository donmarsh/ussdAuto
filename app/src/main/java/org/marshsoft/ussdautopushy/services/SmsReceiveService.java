package org.marshsoft.ussdautopushy.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import org.marshsoft.ussdautopushy.MainActivity;
import org.marshsoft.ussdautopushy.data.database.AppDatabase;
import org.marshsoft.ussdautopushy.data.enums.Actions;
import org.marshsoft.ussdautopushy.data.model.Sms;
import org.marshsoft.ussdautopushy.data.model.SmsCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsReceiveService extends BroadcastReceiver {
    static List<String> senderWhiteList = new ArrayList<>();
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent mainActivityIntent = new Intent(context, MainActivity.class);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(mainActivityIntent);
        Bundle data  = intent.getExtras();
        Object[] pdus = (Object[]) data.get("pdus");
        Log.d("sms",""+pdus.length);
        if(intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")){
            AsyncTask.execute(() -> {
                senderWhiteList = AppDatabase.getInstance(context.getApplicationContext()).smsCallbackDao().getAllSenders();
                Log.d("whitelist",""+senderWhiteList.size());
                Map<String, Sms> msg = retrieveMessages(context, intent);
                //handle received sms
                for (Map.Entry<String, Sms> SmsEntry : msg.entrySet()) {
                    Sms smsMessage = SmsEntry.getValue();
                    Log.d("SMS", smsMessage.getMessageContent());
                    SmsCallback smsCallback = AppDatabase.getInstance(context.getApplicationContext()).smsCallbackDao().loadBySender(smsMessage.getSender());
                    smsMessage.setSmsCallbackUrl(smsCallback.getCallbackUrl());
                    Log.d("whitelist",smsCallback.getCallbackUrl());
                    AppDatabase.getInstance(context.getApplicationContext()).smsDao().insert(smsMessage);
                }
            });

        }
    }

    private static Map<String, Sms> retrieveMessages(Context context, Intent intent) {
        Map<String, Sms> msg = null;
        SmsMessage[] msgs;
        Bundle bundle = intent.getExtras();

        if (bundle != null && bundle.containsKey("pdus")) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                int nbrOfpdus = pdus.length;
                msg = new HashMap<>(nbrOfpdus);
                msgs = new SmsMessage[nbrOfpdus];
                String format = bundle.getString("format");
                for (int i = 0; i < nbrOfpdus; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i],format);
                    String originatingAddress = msgs[i].getOriginatingAddress();
                    Log.d("senderWhite",""+senderWhiteList.size());
                    if (senderWhiteList.contains(originatingAddress)) {
                        startSmsProcessService(context);
                        if (!msg.containsKey(originatingAddress)) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(msgs[i].getTimestampMillis());
                            Date smsTime = calendar.getTime();
                            String sender = msgs[i].getDisplayOriginatingAddress();
                            Sms newSms = new Sms();
                            newSms.setMessageContent( msgs[i].getMessageBody());
                            newSms.setSender(sender);
                            newSms.setStatus("Received");
                            newSms.setSmsCallbackStatus("Received");
                            newSms.setSmsTime(smsTime);
                            msg.put(msgs[i].getOriginatingAddress(),newSms);

                        } else {

                            Sms previousSms = msg.get(originatingAddress);
                            String previousParts = msg.get(originatingAddress).getMessageContent();
                            String msgString = previousParts + msgs[i].getMessageBody();
                            previousSms.setMessageContent(msgString);
                            msg.put(originatingAddress, previousSms);
                        }
                    }


                }
            }
        }

        return msg;
    }
    private static void startSmsProcessService(Context context){
        Intent intent = new Intent(context, SmsProcessService.class);
        intent.setAction(Actions.START.name());
        Log.d("SmsReceiver","Starting the service in >=26 Mode");
        context.startForegroundService(intent);

    }

}

