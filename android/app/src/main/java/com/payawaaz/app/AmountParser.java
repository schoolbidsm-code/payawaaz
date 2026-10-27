package com.payawaaz.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AmountParser {
    private static final Pattern AMT = Pattern.compile(
            "(?:₹|rs\\.?|inr)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern FROM = Pattern.compile(
            "(?:from|से)\\s*([\\p{L} .]{2,30})",
            Pattern.CASE_INSENSITIVE);
    private static final String[] IN_WORDS = {
            "received", "credited", "paid you", "paid to you", "cashback",
            "प्राप्त", "मिले", "मिला", "आए हैं", "आया है"};

    public static long amount(CharSequence text) {
        Matcher m = AMT.matcher(text);
        if (m.find()) {
            try {
                return Long.parseLong(m.group(1).replace(",", ""));
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    public static String sender(CharSequence text) {
        Matcher m = FROM.matcher(text);
        if (m.find()) {
            String s = m.group(1).trim();
            s = s.replaceAll("[.,;:!]+$", "");
            if (s.length() > 1) return s;
        }
        return null;
    }

    public static boolean incoming(CharSequence text) {
        String t = text.toString().toLowerCase();
        for (String k : IN_WORDS) {
            if (t.contains(k)) return true;
        }
        return false;
    }
}
