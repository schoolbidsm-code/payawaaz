package com.payawaaz.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private SharedPreferences prefs;
    private float d;
    private TextView statusView;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("settings", MODE_PRIVATE);
        BackendSender.post(this, "app_open", getPackageName(), "", "");
        d = getResources().getDisplayMetrics().density;
        int pad = (int) (18 * d);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        root.setBackgroundColor(0xFF141A2E);
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) (110 * d), (int) (110 * d));
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        lp.topMargin = (int) (8 * d);
        lp.bottomMargin = (int) (8 * d);
        logo.setLayoutParams(lp);
        ScaleAnimation sa = new ScaleAnimation(1f, 1.1f, 1f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(1200);
        sa.setRepeatMode(Animation.REVERSE);
        sa.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(sa);
        root.addView(logo);

        TextView title = new TextView(this);
        title.setText("PayAwaaz");
        try { title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/NotoSansDevanagari.ttf")); } catch (Exception ignored) { }
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("पेमेंट आते ही आवाज़ में नाम और रकम (app + SMS दोनों)");
        sub.setTextColor(0xFF9FB3D1);
        sub.setTextSize(14);
        sub.setGravity(Gravity.CENTER);
        root.addView(sub);

        statusView = new TextView(this);
        statusView.setTextSize(14);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, (int) (16 * d), 0, (int) (4 * d));
        statusView.setTextColor(0xFFB8E6B8);
        root.addView(statusView);

        Button allow = new Button(this);
        allow.setText("1. Notification access दें");
        allow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });
        root.addView(allow);

        Button smsB = new Button(this);
        smsB.setText("2. SMS permission दें");
        smsB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, 11);
                }
            }
        });
        root.addView(smsB);

        Button batt = new Button(this);
        batt.setText("3. Battery अनुमति (app बंद न हो)");
        batt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(i);
                } catch (Exception e) {
                    startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                }
            }
        });
        root.addView(batt);

        TextView hint = new TextView(this);
        hint.setText("Realme/OPPO: Settings → App management → PayAwaaz → Autostart भी On करें.");
        hint.setTextColor(0xFF9FB3D1);
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, (int) (8 * d), 0, 0);
        root.addView(hint);

        TextView vl = new TextView(this);
        vl.setText("आवाज़ चुनें:");
        vl.setTextColor(0xFFFFFFFF);
        vl.setTextSize(16);
        vl.setPadding(0, (int) (12 * d), 0, 0);
        root.addView(vl);

        Spinner spin = new Spinner(this);
        ArrayAdapter<String> ad = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Female (मीठी आवाज़)", "Male (गहरी आवाज़)"});
        spin.setAdapter(ad);
        spin.setSelection(prefs.getString("gender", "female").equals("male") ? 1 : 0);
        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                prefs.edit().putString("gender", pos == 1 ? "male" : "female").apply();
                if (ttsReady) setGenderVoice();
            }
            public void onNothingSelected(AdapterView<?> p) { }
        });
        root.addView(spin);

        final EditText name = new EditText(this);
        name.setHint("भेजने वाले का नाम (टेस्ट)");
        name.setText("Aman");
        name.setTextColor(0xFFFFFFFF);
        name.setHintTextColor(0xFF6B7A99);
        root.addView(name);

        final EditText amt = new EditText(this);
        amt.setHint("रकम (टेस्ट)");
        amt.setText("500");
        amt.setInputType(InputType.TYPE_CLASS_NUMBER);
        amt.setTextColor(0xFFFFFFFF);
        amt.setHintTextColor(0xFF6B7A99);
        root.addView(amt);

        Button test = new Button(this);
        test.setText("टेस्ट सुनो");
        test.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String n = name.getText().toString().trim();
                long a = 0;
                try { a = Long.parseLong(amt.getText().toString().trim()); } catch (Exception e) { }
                if (!n.isEmpty() && a > 0 && ttsReady) {
                    String s = n + " से Google Pay पर " + HindiNumbers.words(a) + " रुपये आए हैं";
                    BackendSender.post(getApplication(), "test", "", "", s);
                    tts.speak(s, TextToSpeech.QUEUE_FLUSH, null, "test");
                }
            }
        });
        root.addView(test);

        TextView hl = new TextView(this);
        hl.setText("आख़िरी सुनाई गई पेमेंट:");
        hl.setTextColor(0xFFFFFFFF);
        hl.setTextSize(16);
        hl.setPadding(0, (int) (16 * d), 0, 0);
        root.addView(hl);

        TextView hist = new TextView(this);
        hist.setTextColor(0xFF9FB3D1);
        hist.setTextSize(13);
        hist.setText(prefs.getString("history", "अभी कोई नहीं"));
        hist.setPadding(0, (int) (4 * d), 0, 0);
        root.addView(hist);

        android.widget.FrameLayout fl = new android.widget.FrameLayout(this);
        ImageView bg = new ImageView(this);
        bg.setImageResource(R.drawable.splash_bg);
        bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
        fl.addView(bg, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        View scrim = new View(this);
        scrim.setBackgroundColor(0xB3141A2E);
        fl.addView(scrim, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        fl.addView(sv, new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(fl);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int s) {
                if (s == TextToSpeech.SUCCESS) {
                    ttsReady = true;
                    tts.setLanguage(new Locale("hi", "IN"));
                    setGenderVoice();
                }
            }
        }, "com.google.android.tts");
    }

    private void setGenderVoice() {
        String pref = prefs.getString("gender", "female");
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
    }

    private boolean listenerEnabled() {
        String s = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        return s != null && s.contains(getPackageName());
    }

    private boolean smsGranted() {
        return checkSelfPermission(Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean batteryOk() {
        try {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            return pm != null && pm.isIgnoringBatteryOptimizations(getPackageName());
        } catch (Exception e) { return true; }
    }

    private void refreshStatus() {
        if (statusView == null) return;
        String nl = listenerEnabled() ? "✓ Notification access" : "✗ Notification access";
        String sl = smsGranted() ? "✓ SMS permission" : "✗ SMS permission";
        String bl = batteryOk() ? "✓ Battery छूट" : "✗ Battery छूट";
        boolean all = listenerEnabled() && smsGranted();
        statusView.setText(nl + "  " + sl + "\n" + bl + (all ? "\nसब तैयार है!" : ""));
        statusView.setTextColor(all ? 0xFFB8E6B8 : 0xFFFFD8A8);
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == 11) refreshStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }
}
