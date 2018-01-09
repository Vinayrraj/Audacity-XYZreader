package com.example.xyzreader.util;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Singh on 09/01/18.
 */

public class Util {
    private static final String TAG = Util.class.toString();
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format

    // Most time functions can only handle 1902 - 2037
    public static GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    public static Date parsePublishedDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }
}
