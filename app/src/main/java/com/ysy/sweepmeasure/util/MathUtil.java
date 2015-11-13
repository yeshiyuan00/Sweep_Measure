package com.ysy.sweepmeasure.util;

/**
 * User: ysy
 * Date: 2015/9/17
 * Time: 17:15
 */
public class MathUtil {

    public static int ChangeToTwoPower(int number) {
        int result;
        boolean isPower = isTwoPower(number);
        if (!isPower) {
            String temp = Integer.toBinaryString(number);
            int length = temp.length();
            result = (int) Math.pow(2, length);
        } else {
            result = number;
        }
        return result;
    }

    public static boolean isTwoPower(int number) {
        if (number < 2) {
            return false;
        } else {
            String temp = Integer.toBinaryString(number);
            if (temp.lastIndexOf('1') != 0) {
                return false;
            } else {
                return true;
            }
        }
    }
}
