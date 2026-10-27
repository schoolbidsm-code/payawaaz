package com.payawaaz.app;

import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.speech.tts.TextToSpeech;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AnnounceService extends NotificationListenerService {
    static TextToSpeech tts;
    static boolean ready = false;

    static final Set<String> APPS = new HashSet<>(Arrays.asList(
            "com.google.android.apps.nbu.paisa.user0",
            "com.phonepe.app",
            "net.one97.paytm",
            "in.org.npci.upiapp"));

    static String appName(String p) {
        if (p.contains("paisa")) return "Google Pay";
        if (p.contains("phonepe")) return "PhonePe";
        if (p.equals("net.one97.paytm")) return "Paytm";
        return "UPI";
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        if (!sp.getBoolean("on", true)) return;
        if (sbn == null) return;
        String pkg = sbn.getPackageName();
        if (!APPS.contains(pkg)) return;
        Notification n = sbn.getNotification();
        if (n == null || n.extras == null) return;
        CharSequence t = n.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence x = n.extras.getCharSequence(Notification.EXTRA_TEXT);
        if (t == null && x == null) return;
        String text = (t == null ? "" : t.toString()) + " " + (x == null ? "" : x.toString());
        long amt = AmountParser.amount(text);
        if (amt <= 0 || !AmountParser.incoming(text)) return;
        String from = AmountParser.sender(text);
        String app = appName(pkg);
        String sentence;
        if (from != null && !from.isEmpty()) {
            sentence = from + " से " + app + " पर " + HindiNumbers.words(amt) + " रुपये आए हैं";
        } else {
            sentence = "आपको " + app + " पर " + HindiNumbers.words(amt) + " रुपये मिले हैं";
        }
        speak(sentence);
        saveHistory(sp, sentence);
    }

    synchronized void speak(String s) {
        if (tts == null) {
            tts = new TextToSpeech(this, st -> {
                ready = st == TextToSpeech.SUCCESS;
                if (ready) applyVoice();
            }, "com.google.android.tts");
        }
        if (!ready) return;
        Bundle p = new Bundle();
        p.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
        tts.speak(s, TextToSpeech.QUEUE_ADD, p, "aw" + System.currentTimeMillis());
    }

    void applyVoice() {
        SharedPreferences sp = getSharedPreferences("settings", MODE_PRIVATE);
        String pref = sp.getString("gender", "female");
        if (tts == null) return;
        try {
            Set<android.speech.tts.Voice> voices = tts.getVoices();
            if (voices != null) {
                android.speech.tts.Voice fallback = null;
                for (android.speech.tts.Voice v : voices) {
                    String vn = v.getName().toLowerCase(Locale.ROOT);
                    if (!v.getLocale().getLanguage().equals("hi")) continue;
                    if (fallback == null) fallback = v;
                    boolean looksFemale = vn.contains("female") || vn.contains("hid") || vn.contains("hif") || vn.contains("kalpana");
                    boolean looksMale = !vn.contains("female") && (vn.contains("male") || vn.contains("him") || vn.contains("hemant"));
                    if ("female".equals(pref) && looksFemale) { tts.setVoice(v); return; }
                    if ("male".equals(pref) && looksMale) { tts.setVoice(v); return; }
                }
                if (fallback != null) tts.setVoice(fallback);
            }
        } catch (Exception ignored) { }
        try { tts.setLanguage(new Locale("hi", "IN")); } catch (Exception ignored) { }
    }

    void saveHistory(SharedPreferences sp, String s) {
        String line = new SimpleDateFormat("dd MMM hh:mm a", Locale.ENGLISH).format(new Date()) + " - " + s;
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
