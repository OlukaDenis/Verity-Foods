package com.verityfoods.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppUtils {

    public static String formatCurrency(int price) {

        Double number= (double) price;

        DecimalFormat formatter = new DecimalFormat("#,##0");
        final String formattedNumber = formatter.format(number);

        return "UGX "+formattedNumber;
    }

    public static String formatOffer(int offer) {
        String s = String.valueOf(offer);

        return s + "% OFF";
    }

    public static String formatVariable(String name, int price) {
        String amount  = String.valueOf(price);

        return name + " (" + amount + ") ";
    }


    public static String currentTime() {
        DateFormat time = new SimpleDateFormat("HH:mm a", Locale.UK);
        Date date = new Date();
        return time.format(date);
    }

    public static String currentDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.UK);
        Date date = new Date();
        return dateFormat.format(date);
    }
}
