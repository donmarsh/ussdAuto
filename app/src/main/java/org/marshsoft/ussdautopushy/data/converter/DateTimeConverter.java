package org.marshsoft.ussdautopushy.data.converter;

import android.util.Log;

import androidx.room.TypeConverter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeConverter {
    private static final String TAG = "DateConverter";
    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    static {
        df.setTimeZone(TimeZone.getTimeZone("GMT+3"));
    }

    @TypeConverter
    public static Date timeToDate(String value) {
        if (value != null) {
            try {
                return df.parse(value);
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    @TypeConverter
    public static String dateToTime(Date value) {
        if (value != null) {
            return df.format(value);
        } else {
            return null;
        }
    }
}
