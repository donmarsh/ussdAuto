package org.marshsoft.ussdautopushy.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.marshsoft.ussdautopushy.R;
import org.marshsoft.ussdautopushy.data.model.SmsCallback;

import java.util.ArrayList;
import java.util.List;


public class SmsCallBackDataAdapter extends RecyclerView.Adapter<SmsCallBackDataAdapter.ViewHolder> {
    List<SmsCallback> callbackList = new ArrayList<>();

    @NonNull
    @Override
    public SmsCallBackDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.callback_list_view,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsCallBackDataAdapter.ViewHolder holder, int position) {
        SmsCallback currentCallback = callbackList.get(position);
        holder.tvCallbackId.setText(""+currentCallback.getSmsCallbackId());
        holder.tvCallbackUrl.setText(currentCallback.getCallbackUrl());
        holder.tvCallbackSender.setText(currentCallback.getSmsSender());

    }

    @Override
    public int getItemCount() {
        return callbackList.size();
    }
    public void setData(List<SmsCallback> newData) {
        this.callbackList = newData;
        notifyDataSetChanged();
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvCallbackId,tvCallbackUrl,tvCallbackSender;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCallbackUrl= itemView.findViewById(R.id.tvSmsCallbackUrlData);
            tvCallbackId = itemView.findViewById(R.id.tvCallbackIdData);
            tvCallbackSender = itemView.findViewById(R.id.tvSmsSenderData);

        }
    }
}
