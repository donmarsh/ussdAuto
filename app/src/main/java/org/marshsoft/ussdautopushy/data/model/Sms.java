package org.marshsoft.ussdautopushy.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.marshsoft.ussdautopushy.data.converter.DateTimeConverter;

import java.util.Date;

@Entity
public class Sms {
    @PrimaryKey(autoGenerate = true)
    private Long smsId;
    private String messageContent;
    @ColumnInfo(index = true)
    private String status;
    private String sender;
    @TypeConverters(DateTimeConverter.class)
    private Date smsTime;
    private String smsCallbackUrl;
    private String smsCallbackStatus;

    public String getSmsCallbackStatus() {
        return smsCallbackStatus;
    }

    public void setSmsCallbackStatus(String smsCallbackStatus) {
        this.smsCallbackStatus = smsCallbackStatus;
    }


    public String getSmsCallbackUrl() {
        return smsCallbackUrl;
    }

    public void setSmsCallbackUrl(String smsCallbackUrl) {
        this.smsCallbackUrl = smsCallbackUrl;
    }


    public Date getSmsTime() {
        return smsTime;
    }

    public void setSmsTime(Date smsTime) {
        this.smsTime = smsTime;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }


    public Long getSmsId() {
        return smsId;
    }

    public void setSmsId(Long smsId) {
        this.smsId = smsId;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}
