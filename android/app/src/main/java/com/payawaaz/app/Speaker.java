package com.payawaaz.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import java.util.Locale;

/** Ek hi TTS engine, notification aur SMS dono se use hota hai. */
public class Speaker {
    private static TextToSpeech tts;
    private static boolean ready = false;
    private static String pending = null;
    private static String lastSpoken = "";
    private static long lastAt = 0;

    public static synchronized void init(Context c) {
        final Context app = c.getApplicationContext();
        if (tts != null) return;
        tts = new TextToSpeech(app, new TextToSpeech.OnInitListener() {
            public void onInit(int st) {
                ready = (st == TextToSpeech.SUCCESS);
                if (ready) {
                    applyVoice(app);
                    if (pending != null) {
                        tts.speak(pending, TextToSpeech.QUEUE_ADD, null, "aw" + System.currentTimeMillis());
                        pending = null;
                    }
                }
            }
        }, "com.google.android.tts");
    }

    public static void speak(Context c, String s) {
        if (s == null || s.isEmpty()) return;
        init(c);
        long now = System.currentTimeMillis();
        if (s.equals(lastSpoken) && now - lastAt < 90000) return; // notification + SMS dono aaye to ek hi baar
        lastSpoken = s;
        lastAt = now;
        if (!ready) { pending = s; return; }
        try {
            android.media.ToneGenerator tg = new android.media.ToneGenerator(android.media.AudioManager.STREAM_MUSIC, 70);
            tg.startTone(android.media.ToneGenerator.TONE_PROP_BEEP2, 150);
        } catch (Exception ignored) { }
        android.os.Bundle p = new android.os.Bundle();
        p.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f);
        tts.speak(s, TextToSpeech.QUEUE_ADD, p, "aw" + now);
    }

    public static boolean isReady() { return ready; }

    private static void applyVoice(Context c) {
        SharedPreferences sp = c.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String pref = sp.getString("gender", "female");
        if (tts == null) return;
        try {
            java.util.Set<android.speech.tts.Voice> voices = tts.getVoices();
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
}
