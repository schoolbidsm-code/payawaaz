package com.payawaaz.app;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HelpActivity extends Activity {
    private static final String[] STEPS = {
        "1. App kholo aur teeno green ticks dekho: Notification access, SMS permission, Battery chhoot.",
        "2. Realme/OPPO: Settings me 'startup' search karo, PayAwaaz ko auto-launch ON karo. Ya Phone Manager -> Startup manager.",
        "3. App info -> Battery usage -> 'Allow background activity' ON.",
        "4. Media volume badha kar rakho (silent/DND me awaaz nahi aayegi).",
        "5. Test: kisi ko GPay/PhonePe se ₹1 bhejo, turant naam aur rakam bolegi.",
        "6. Bank SMS se aane wali payment bhi bolegi (HDFC, SBI, ICICI, Axis, Paytm Payments Bank, PhonePe sab).",
        "7. Phone restart ke baad app apne aap jag jayegi (Autostart ON hone par)."
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0xFF141A2E);
        sv.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);
        ImageView art = new ImageView(this);
        art.setImageResource(R.drawable.help_art);
        art.setAdjustViewBounds(true);
        root.addView(art);
        TextView head = new TextView(this);
        head.setText("Settìng guide");
        head.setTextSize(22);
        head.setTextColor(Color.WHITE);
        try { head.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/NotoSansDevanagari.ttf")); } catch (Exception ignored) { }
        root.addView(head);
        for (String s : STEPS) {
            TextView t = new TextView(this);
            t.setText(s);
            t.setTextSize(16);
            t.setTextColor(0xFFE8EAF2);
            t.setPadding(0, 18, 0, 0);
            root.addView(t);
        }
        TextView foot = new TextView(this);
        foot.setText("Har payment notification aur SMS ab backend par bhi log hota hai, isliye koi bhi atak jaaye to turant pakda jaata hai.");
        foot.setTextSize(13);
        foot.setTextColor(0xFF9AA3B8);
        foot.setPadding(0, 30, 0, 0);
        root.addView(foot);
        sv.addView(root);
        setContentView(sv);
    }
}
