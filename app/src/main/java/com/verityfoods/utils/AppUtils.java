package com.verityfoods.utils;

import java.text.DecimalFormat;

public class AppUtils {

    public static String formatCurrency(int price) {

        Double number= (double) price;

        DecimalFormat formatter = new DecimalFormat("#,##0");
        final String formattedNumber = formatter.format(number);

        return "UGX "+formattedNumber;
    }
}
