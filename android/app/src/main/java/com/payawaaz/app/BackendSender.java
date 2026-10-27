package com.payawaaz.app;

import android.content.Context;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Events backend par bhejta hai (chupchap, app ko rokay bina). */
public class BackendSender {
    public static final String BASE = "https://payawaaz-backend.onrender.com";

    public static void post(Context c, String type, String pkg, String title, String text) {
        try {
            final Context app = c.getApplicationContext();
            final String fT = type;
            final String fP = pkg == null ? "" : pkg;
            final String fTi = title == null ? "" : title;
            final String fTx = text == null ? "" : text;
            new Thread(new Runnable() {
                public void run() {
                    HttpURLConnection conn = null;
                    try {
                        URL u = new URL(BASE + "/log");
                        conn = (HttpURLConnection) u.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setConnectTimeout(8000);
                        conn.setReadTimeout(8000);
                        conn.setDoOutput(true);
                        conn.setRequestProperty("Content-Type", "application/json");
                        String body = "{\"type\":\"" + esc(fT) + "\",\"pkg\":\"" + esc(fP)
                                + "\",\"title\":\"" + esc(fTi) + "\",\"text\":\"" + esc(fTx) + "\"}";
                        OutputStream os = conn.getOutputStream();
                        os.write(body.getBytes(StandardCharsets.UTF_8));
                        os.close();
                        conn.getResponseCode();
                    } catch (Exception ignored) {
                    } finally {
                        if (conn != null) conn.disconnect();
                    }
                }
            }).start();
        } catch (Exception ignored) { }
    }

    public static void sendSms(String senderAddr, String body) {
        // SmsReceiver se call hota hai; Context ke bina direct thread
        final String fP = senderAddr == null ? "" : senderAddr;
        final String fTx = body == null ? "" : body;
        new Thread(new Runnable() {
            public void run() {
                HttpURLConnection conn = null;
                try {
                    URL u = new URL(BASE + "/log");
                    conn = (HttpURLConnection) u.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(8000);
                    conn.setReadTimeout(8000);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");
                    String b = "{\"type\":\"sms\",\"pkg\":\"" + esc(fP) + "\",\"title\":\"\",\"text\":\"" + esc(fTx) + "\"}";
                    OutputStream os = conn.getOutputStream();
                    os.write(b.getBytes(StandardCharsets.UTF_8));
                    os.close();
                    conn.getResponseCode();
                } catch (Exception ignored) {
                } finally { if (conn != null) conn.disconnect(); }
            }
        }).start();
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ");
    }
}
