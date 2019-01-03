package com.vlcjPlayer.common;

public class StringUtil {

    public static Boolean isNumeric(String str){
        if (str == null) {
            return false;
        }
        int length = str.length();

        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
