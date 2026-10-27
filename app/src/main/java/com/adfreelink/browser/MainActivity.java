package com.adfreelink.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.InputType;
import android.net.Uri;

public class MainActivity extends Activity {
    private WebView web;
    private EditText address;
    private TextView status;
    private final int bg = Color.rgb(16,19,35);
    private final int cyan = Color.rgb(0,229,255);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(bg);
        root.setPadding(dp(12), dp(12), dp(12), dp(8));

        TextView title = new TextView(this);
        title.setText("ADFREE LINK");
        title.setTextColor(cyan);
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(8), 0, dp(12));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        address = new EditText(this);
        address.setSingleLine(true);
        address.setHint("Paste any website link");
        address.setHintTextColor(Color.LTGRAY);
        address.setTextColor(Color.WHITE);
        address.setTextSize(15);
        address.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        address.setPadding(dp(12), dp(8), dp(12), dp(8));
        address.setBackgroundColor(Color.rgb(37,42,62));
        root.addView(address, new LinearLayout.LayoutParams(-1, dp(50)));

        Button open = button("OPEN LINK");
        root.addView(open, new LinearLayout.LayoutParams(-1, dp(46)));
        open.setOnClickListener(v -> loadAddress());

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        addControl(controls, "←", v -> { if (web.canGoBack()) web.goBack(); });
        addControl(controls, "→", v -> { if (web.canGoForward()) web.goForward(); });
        addControl(controls, "↻", v -> web.reload());
        addControl(controls, "HOME", v -> {
            address.setText("");
            web.loadUrl("about:blank");
        });
        root.addView(controls, new LinearLayout.LayoutParams(-1, dp(44)));

        status = new TextView(this);
        status.setText("Ready");
        status.setTextColor(Color.LTGRAY);
        status.setTextSize(12);
        status.setPadding(dp(4), dp(4), dp(4), dp(4));
        root.addView(status, new LinearLayout.LayoutParams(-1, -2));

        web = new WebView(this);
        web.setBackgroundColor(Color.WHITE);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        web.getSettings().setSupportMultipleWindows(false);
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();
                if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
                    address.setText(uri.toString());
                    return false;
                }
                Toast.makeText(MainActivity.this, "This link type is not supported in the app.", Toast.LENGTH_SHORT).show();
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                address.setText(url.equals("about:blank") ? "" : url);
                status.setText("Loading…");
            }
            @Override public void onPageFinished(WebView view, String url) {
                status.setText("Page loaded");
                address.setText(url.equals("about:blank") ? "" : url);
            }
        });

        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private void loadAddress() {
        String value = address.getText().toString().trim();
        if (value.isEmpty()) {
            Toast.makeText(this, "Pehle link paste karo", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!value.matches("(?i)^https?://.*")) value = "https://" + value;
        Uri uri = Uri.parse(value);
        if (uri.getHost() == null || !(uri.getScheme().equals("https") || uri.getScheme().equals("http"))) {
            Toast.makeText(this, "Valid http/https link daalo", Toast.LENGTH_SHORT).show();
            return;
        }
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(address.getWindowToken(), 0);
        web.loadUrl(uri.toString());
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.BLACK);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(cyan));
        return b;
    }

    private void addControl(LinearLayout row, String label, View.OnClickListener listener) {
        Button b = button(label);
        b.setTextSize(13);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(42), 1);
        lp.setMargins(dp(2), dp(2), dp(2), dp(2));
        row.addView(b, lp);
        b.setOnClickListener(listener);
    }

    private int dp(int x) {
        return (int)(x * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (web != null) {
            web.stopLoading();
            web.destroy();
        }
        super.onDestroy();
    }
}
