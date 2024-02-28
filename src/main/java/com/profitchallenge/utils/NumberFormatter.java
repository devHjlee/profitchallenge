package com.profitchallenge.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberFormatter {

    private NumberFormatter() {
    }

    public static double round(double value) {
        return NumberFormatter.round(value, 3);
    }

    public static double round(double value, int numberOfDigitsAfterDecimalPoint) {
        BigDecimal bigDecimal = new BigDecimal(value).setScale(numberOfDigitsAfterDecimalPoint, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }

}
