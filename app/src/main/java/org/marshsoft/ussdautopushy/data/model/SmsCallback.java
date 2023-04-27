package org.marshsoft.ussdautopushy.data.model;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"smsSender"},unique = true)})
public class SmsCallback {

    @PrimaryKey(autoGenerate = true)
    private int smsCallbackId;
    private String callbackUrl;
    private String smsSender;

    public int getSmsCallbackId() {
        return smsCallbackId;
    }

    public void setSmsCallbackId(int smsCallbackId) {
        this.smsCallbackId = smsCallbackId;
    }
    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getSmsSender() {
        return smsSender;
    }

    public void setSmsSender(String smsSender) {
        this.smsSender = smsSender;
    }


}
