package org.marshsoft.ussdautopushy.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.marshsoft.ussdautopushy.R;
import org.marshsoft.ussdautopushy.data.model.Sms;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SmsDataAdapter extends RecyclerView.Adapter<SmsDataAdapter.ViewHolder> {
    List<Sms> smsList = new ArrayList<>();

    @NonNull
    @Override
    public SmsDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sms_list_view,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsDataAdapter.ViewHolder holder, int position) {
        Sms currentSms = smsList.get(position);
        holder.tvMessageData.setText(currentSms.getMessageContent());
        holder.tvSenderData.setText(currentSms.getSender());
        holder.tvStatusData.setText(currentSms.getStatus());
        holder.tvCallbackStatusData.setText(currentSms.getSmsCallbackStatus());
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String formattedDate = df.format(currentSms.getSmsTime());
        holder.tvTimeData.setText(formattedDate);
        String smsId  = ""+currentSms.getSmsId();
        holder.tvSmsIdData.setText(smsId);
    }

    @Override
    public int getItemCount() {
        return smsList.size();
    }
    public void setData(List<Sms> newData) {
        this.smsList = newData;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvSenderData,tvTimeData,tvMessageData, tvSmsIdData,tvStatusData, tvCallbackStatusData;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderData = itemView.findViewById(R.id.tvSenderData);
            tvTimeData = itemView.findViewById(R.id.tvTimeData);
            tvMessageData = itemView.findViewById(R.id.tvMessageData);
            tvSmsIdData = itemView.findViewById(R.id.tvSmsIdData);
            tvStatusData = itemView.findViewById(R.id.tvStatusData);
            tvCallbackStatusData = itemView.findViewById(R.id.tvCallbackStatusData);

        }
    }
}
