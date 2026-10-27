package com.payawaaz.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Bank ke credit SMS (HDFC/SBI/Paytm/PhonePe SMS) pakad kar bolta hai. */
public class SmsReceiver extends BroadcastReceiver {

    private static final Pattern BY = Pattern.compile(
            "(?:by|from|se)\\s+([A-Za-z][A-Za-z .]{1,30})", Pattern.CASE_INSENSITIVE);

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (!hasSms(ctx)) return;
        SharedPreferences sp = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (!sp.getBoolean("on", true)) return;
        Bundle ex = intent.getExtras();
        if (ex == null) return;
        Object[] pdus = (Object[]) ex.get("pdus");
        if (pdus == null) return;
        for (Object pdu : pdus) {
            SmsMessage msg;
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    msg = SmsMessage.createFromPdu((byte[]) pdu, ex.getString("format"));
                } else {
                    msg = SmsMessage.createFromPdu((byte[]) pdu);
                }
            } catch (Exception e) { continue; }
            if (msg == null) continue;
            String senderAddr = msg.getOriginatingAddress();
            String body = msg.getMessageBody();
            if (body == null || senderAddr == null) continue;
            String low = body.toLowerCase();
            if (low.contains("otp") || low.contains("one time password")) continue;
            boolean bankish = senderAddr.length() <= 10 && !senderAddr.matches(".*\\d{6,}.*");
            if (!bankish) continue;
            BackendSender.sendSms(senderAddr, body);
            long amt = AmountParser.amount(body);
            if (amt <= 0 || !AmountParser.incoming(body)) continue;
            String who = personName(body);
            String bank = bankName(senderAddr);
            String sentence;
            if (who != null) {
                sentence = who + " se " + bank + " me " + HindiNumbers.words(amt) + " रुपये आए हैं";
            } else {
                sentence = bank + " me " + HindiNumbers.words(amt) + " रुपये आए हैं";
            }
            Speaker.speak(ctx, sentence);
            BackendSender.post(ctx, "sms_announced", senderAddr, "", sentence);
            saveHistory(sp, sentence);
        }
    }

    private static boolean hasSms(Context c) {
        if (Build.VERSION.SDK_INT >= 23) {
            return c.checkSelfPermission("android.permission.RECEIVE_SMS") == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private static String personName(CharSequence body) {
        Matcher m = BY.matcher(body.toString());
        if (m.find()) {
            String s = m.group(1).trim().replaceAll("[.,;:]+$", "");
            String low = s.toLowerCase();
            if (s.length() > 1 && !low.contains("account") && !low.contains("a c")) return s;
        }
        return null;
    }

    private static String bankName(String addr) {
        String a = addr == null ? "" : addr.toUpperCase();
        if (a.contains("HDFC")) return "HDFC bank";
        if (a.contains("ICICI")) return "ICICI bank";
        if (a.contains("SBI")) return "State bank";
        if (a.contains("AXIS")) return "Axis bank";
        if (a.contains("KOTAK") || a.contains("KKBK")) return "Kotak bank";
        if (a.contains("PNB")) return "Punjab national bank";
        if (a.contains("BOB") || a.contains("BARB")) return "Bank of Baroda";
        if (a.contains("YESB")) return "Yes bank";
        if (a.contains("IDFC")) return "IDFC bank";
        if (a.contains("IDBI")) return "IDBI bank";
        if (a.contains("INDB")) return "IndusInd bank";
        if (a.contains("UNION") || a.contains("UBIN")) return "Union bank";
        if (a.contains("CANARA") || a.contains("CNRB")) return "Canara bank";
        if (a.contains("PAYTM")) return "Paytm";
        if (a.contains("PHONEPE")) return "PhonePe";
        if (a.contains("GPay") || a.contains("GOOGLE")) return "Google Pay";
        return "tumhare bank";
    }

    private static void saveHistory(SharedPreferences sp, String s) {
        String line = new java.text.SimpleDateFormat("dd MMM hh:mm a", java.util.Locale.ENGLISH)
                .format(new java.util.Date()) + " - " + s;
        String h = sp.getString("history", "");
        String[] parts = (line + "\n" + h).split("\n");
        StringBuilder sb = new StringBuilder();
        int max = Math.min(parts.length, 10);
        for (int i = 0; i < max; i++) {
            sb.append(parts[i]);
            if (i < max - 1) sb.append("\n");
        }
        sp.edit().putString("history", sb.toString()).apply();
    }
}
