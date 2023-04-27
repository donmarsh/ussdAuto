package org.marshsoft.ussdautopushy.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.marshsoft.ussdautopushy.MainActivity;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){

            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(mainActivityIntent);

        }
    }
}
