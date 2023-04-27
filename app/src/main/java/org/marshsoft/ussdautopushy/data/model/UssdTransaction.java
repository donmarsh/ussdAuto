package org.marshsoft.ussdautopushy.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import org.marshsoft.ussdautopushy.data.converter.DateTimeConverter;

import java.util.Date;

@Entity
public class UssdTransaction {
    @PrimaryKey(autoGenerate = true)
    private Long transactionId;
    private String ussdNumber;
    @TypeConverters(DateTimeConverter.class)
    private Date transactionTime;
    private String ussdResponse;
    private boolean responseType;
    private String ussdCallbackUrl;
    private String transactionReference;
    private String status;
    private String ussdCallbackStatus;

    public String getUssdCallbackStatus() {
        return ussdCallbackStatus;
    }

    public void setUssdCallbackStatus(String ussdCallbackStatus) {
        this.ussdCallbackStatus = ussdCallbackStatus;
    }


    public String getUssdCallbackUrl() {
        return ussdCallbackUrl;
    }

    public void setUssdCallbackUrl(String ussdCallbackUrl) {
        this.ussdCallbackUrl = ussdCallbackUrl;
    }






    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public String getUssdNumber() {
        return ussdNumber;
    }

    public void setUssdNumber(String ussdNumber) {
        this.ussdNumber = ussdNumber;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public String getUssdResponse() {
        return ussdResponse;
    }

    public void setUssdResponse(String ussdResponse) {
        this.ussdResponse = ussdResponse;
    }

    public boolean isResponseType() {
        return responseType;
    }

    public void setResponseType(boolean responseType) {
        this.responseType = responseType;
    }


}
