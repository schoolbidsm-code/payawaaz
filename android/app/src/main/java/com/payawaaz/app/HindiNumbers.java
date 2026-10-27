package com.payawaaz.app;

public class HindiNumbers {
    private static final String[] ONES = {"", "एक", "दो", "तीन", "चार", "पाँच", "छह", "सात", "आठ", "नौ",
            "दस", "ग्यारह", "बारह", "तेरह", "चौदह", "पंद्रह", "सोलह", "सत्रह", "अठारह", "उन्नीस"};
    private static final String[] TENS = {"", "", "बीस", "तीस", "चालीस", "पचास", "साठ", "सत्तर", "अस्सी", "नब्बे"};

    public static String words(long n) {
        if (n < 0) return "ऋणात्मक " + words(-n);
        if (n == 0) return "शून्य";
        StringBuilder sb = new StringBuilder();
        long crore = n / 10000000; n = n % 10000000;
        long lakh = n / 100000; n = n % 100000;
        long thousand = n / 1000; n = n % 1000;
        long hundred = n / 100; n = n % 100;
        if (crore > 0) sb.append(words(crore)).append(" करोड़ ");
        if (lakh > 0) sb.append(sub1000((int) lakh)).append(" लाख ");
        if (thousand > 0) sb.append(sub1000((int) thousand)).append(" हज़ार ");
        if (hundred > 0) sb.append(ONES[(int) hundred]).append(" सौ ");
        if (n > 0) sb.append(sub100((int) n));
        String s = sb.toString().trim();
        return s.isEmpty() ? "शून्य" : s;
    }

    private static String sub1000(int n) {
        if (n < 100) return sub100(n);
        int h = n / 100; int r = n % 100;
        return ONES[h] + " सौ" + (r > 0 ? " " + sub100(r) : "");
    }

    private static String sub100(int n) {
        if (n < 20) return ONES[n];
        String t = TENS[n / 10];
        int r = n % 10;
        return r > 0 ? t + " " + ONES[r] : t;
    }
}
