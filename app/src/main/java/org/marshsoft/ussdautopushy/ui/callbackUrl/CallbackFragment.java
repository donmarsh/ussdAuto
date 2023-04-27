package org.marshsoft.ussdautopushy.ui.callbackUrl;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.marshsoft.ussdautopushy.Adapters.SmsCallBackDataAdapter;
import org.marshsoft.ussdautopushy.Adapters.SmsDataAdapter;
import org.marshsoft.ussdautopushy.R;
import org.marshsoft.ussdautopushy.ui.sms.SmsViewModel;

public class CallbackFragment extends Fragment {
    private SmsCallBackDataAdapter smsCallBackDataAdapter = new SmsCallBackDataAdapter();
    private RecyclerView callbackRecyclerView;
    private CallbackViewModel mViewModel;

    public static CallbackFragment newInstance() {
        return new CallbackFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.callback_fragment, container, false);
        callbackRecyclerView = view.findViewById(R.id.rvSmsCallbacks);
        callbackRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        callbackRecyclerView.setAdapter(smsCallBackDataAdapter);
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(CallbackViewModel.class);
        mViewModel.getAllCallbacks().observe(getViewLifecycleOwner(), callbacks -> smsCallBackDataAdapter.setData(callbacks));
    }

}