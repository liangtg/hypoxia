package com.syber.base.util;

import java.util.regex.Pattern;

/**
 * Created by liangtg on 16-1-8.
 */
public class MatchUtils {

    public static boolean matchPhone(CharSequence input) {
        return Pattern.matches("1\\d{10}", input);
    }

    public static boolean matchCode(CharSequence input) {
        return Pattern.matches("\\d{6}", input);
    }

    public static boolean matchPass(CharSequence input) {
        return Pattern.matches("^(?!\\d+$)(?![a-zA-Z]+$)[0-9a-zA-Z]{8,}$", input);
    }
}
