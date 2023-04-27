package org.marshsoft.ussdautopushy.ui.ussd;

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

import org.marshsoft.ussdautopushy.Adapters.UssdDataAdapter;
import org.marshsoft.ussdautopushy.R;

public class UssdFragment extends Fragment {
    private UssdDataAdapter ussdDataAdapter = new UssdDataAdapter();
    private UssdViewModel mViewModel;
    private RecyclerView ussdTransactionRecyclerView;

    public static UssdFragment newInstance() {
        return new UssdFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.ussd_fragment, container, false);
        ussdTransactionRecyclerView = view.findViewById(R.id.ussdRecyclerView);
        ussdTransactionRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ussdTransactionRecyclerView.setAdapter(ussdDataAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UssdViewModel.class);
        mViewModel.getAllUssdTransactions().observe(getViewLifecycleOwner(), ussd -> ussdDataAdapter.setData(ussd));
    }

}