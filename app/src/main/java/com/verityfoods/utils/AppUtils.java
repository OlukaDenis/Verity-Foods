package com.verityfoods.utils;

import java.text.DecimalFormat;

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
}
