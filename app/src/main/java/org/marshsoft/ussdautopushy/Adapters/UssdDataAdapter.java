package org.marshsoft.ussdautopushy.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.marshsoft.ussdautopushy.R;
import org.marshsoft.ussdautopushy.data.model.UssdTransaction;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UssdDataAdapter extends RecyclerView.Adapter<UssdDataAdapter.ViewHolder>  {
    List<UssdTransaction> ussdList = new ArrayList<>();
    @NonNull
    @Override
    public UssdDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ussd_list_view,parent,false);

        return new UssdDataAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UssdDataAdapter.ViewHolder holder, int position) {
        UssdTransaction ussdTransaction = ussdList.get(position);
        holder.tvResponseData.setText(ussdTransaction.getTransactionReference());
        holder.tvUssdCodeData.setText(ussdTransaction.getUssdNumber());
        holder.tvUssdIdData.setText(Long.toString(ussdTransaction.getTransactionId()));
        holder.tvStatusData.setText(ussdTransaction.getStatus());
        holder.tvUssdCallbackStatusData.setText(ussdTransaction.getUssdCallbackStatus());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = df.format(ussdTransaction.getTransactionTime());
        holder.tvTimeData.setText(formattedDate);

    }
    public void setData(List<UssdTransaction> newData) {
        this.ussdList = newData;
        notifyDataSetChanged();
    }
    @Override
    public int getItemCount() {
        return ussdList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvUssdIdData,tvResponseData,tvUssdCodeData,tvStatusData,tvTimeData,tvUssdCallbackStatusData;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUssdIdData = itemView.findViewById(R.id.tvTransactionIdData);
            tvResponseData= itemView.findViewById(R.id.tvTransactionReferenceData);
            tvUssdCodeData= itemView.findViewById(R.id.tvUssdCode);
            tvStatusData= itemView.findViewById(R.id.tvUssdStatusData);
            tvUssdCallbackStatusData = itemView.findViewById(R.id.tvUssdCallbackStatusData);
            tvTimeData  = itemView.findViewById(R.id.tvTimeData);

        }
    }
}
