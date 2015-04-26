package com.example.han.pleasantjourney;

/**
 * Created by Han on 4/26/2015.
 */

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ValueRounder {

    public static int roundDecimal(double value, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(value);

        bd = bd.setScale(decimalPlace, RoundingMode.HALF_UP);
        int intValue = bd.intValue();

        return intValue;
    }
}
