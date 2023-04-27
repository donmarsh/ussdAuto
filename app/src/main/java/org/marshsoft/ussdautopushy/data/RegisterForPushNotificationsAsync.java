package org.marshsoft.ussdautopushy.data;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import androidx.preference.PreferenceManager;

import org.marshsoft.ussdautopushy.R;

import java.net.URL;

import me.pushy.sdk.Pushy;

public class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Object> {
    Activity mActivity;

    public RegisterForPushNotificationsAsync(Activity activity) {
        this.mActivity = activity;
    }

    protected Object doInBackground(Void... params) {
        try {
            // Register the device for notifications
            String deviceToken = Pushy.register(mActivity.getApplicationContext());

            // Registration succeeded, log token to logcat
            Log.d("Pushy", "Pushy device token: " + deviceToken);
            PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext()).edit().putString("token",deviceToken).apply();

            // Provide token to onPostExecute()
            return deviceToken;
        }
        catch (Exception exc) {
            // Registration failed, provide exception to onPostExecute()
            return exc;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        String message;

        // Registration failed?
        if (result instanceof Exception) {
            // Log to console
            Log.e("PushyException", result.toString());

            // Display error in alert
            message = ((Exception) result).getMessage();
        }
        else {
            message = "Pushy device token: " + result.toString() + "\n\n(copy from logcat)";
            PreferenceManager.getDefaultSharedPreferences(mActivity.getApplicationContext()).edit().putString("token",result.toString()).apply();

        }

        // Registration succeeded, display an alert with the device token
        new android.app.AlertDialog.Builder(this.mActivity)
                .setTitle("Pushy")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}