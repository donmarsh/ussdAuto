package org.marshsoft.ussdautopushy.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import org.marshsoft.ussdautopushy.R;
import org.marshsoft.ussdautopushy.data.RegisterForPushNotificationsAsync;

import java.util.Objects;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.model.PushyDeviceCredentials;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        getDeviceToken();
        String[] perms = {"android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS","android.permission.INTERNET","android.permission.CALL_PHONE","android.permission.RECEIVE_SMS","android.permission.READ_SMS"};
        if(!hasPermissions(getActivity(), perms)){
            requestPermissions();
        }
        Button confirmPermissions = root.findViewById(R.id.btnGetPermission);
        confirmPermissions.setOnClickListener(v -> requestPermissions());
        return root;
    }
    public void getDeviceToken(){
        if (!Pushy.isRegistered(requireActivity())) {
            new RegisterForPushNotificationsAsync(getActivity()).execute();
        }


    }
    private void requestPermissions(){
        int PERMISSION_ALL = 1;
        String[] perms = {"android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS","android.permission.INTERNET","android.permission.CALL_PHONE","android.permission.RECEIVE_SMS","android.permission.READ_SMS"};
        if (!hasPermissions(getActivity(), perms)) {
            ActivityCompat.requestPermissions(getActivity(), perms, PERMISSION_ALL);
        }
        else{
            Toast.makeText(getActivity(),"Permissions already granted",Toast.LENGTH_SHORT).show();
        }
    }
    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}