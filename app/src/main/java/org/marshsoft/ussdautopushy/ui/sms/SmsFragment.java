package org.marshsoft.ussdautopushy.ui.sms;

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

import org.marshsoft.ussdautopushy.Adapters.SmsDataAdapter;
import org.marshsoft.ussdautopushy.R;

public class SmsFragment extends Fragment {
    private SmsDataAdapter smsDataAdapter = new SmsDataAdapter();
    private SmsViewModel mViewModel;
    private RecyclerView smsRecyclerView;

    public static SmsFragment newInstance() {
        return new SmsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.sms_fragment, container, false);
        smsRecyclerView = view.findViewById(R.id.smsRecyclerView);
        smsRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        smsRecyclerView.setAdapter(smsDataAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(SmsViewModel.class);

        mViewModel.getAllSms().observe(getViewLifecycleOwner(), sms -> smsDataAdapter.setData(sms));
    }


}