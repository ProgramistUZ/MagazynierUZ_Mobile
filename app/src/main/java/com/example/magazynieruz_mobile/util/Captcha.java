package com.example.magazynieruz_mobile.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class Captcha {

    public interface Listener {
        void onCaptchaSolved(String token);
    }

    private Captcha() {}

    @SuppressLint("SetJavaScriptEnabled")
    public static void attach(WebView webView, Listener listener) {
        Context context = webView.getContext();
        webView.setBackgroundColor(0x00000000);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new Bridge(listener), "AndroidCaptcha");

        String html = readRaw(context, com.example.magazynieruz_mobile.R.raw.hcaptcha);
        webView.loadDataWithBaseURL("https://localhost/", html, "text/html", "UTF-8", null);
    }

    private static String readRaw(Context context, int resId) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(
                new InputStreamReader(context.getResources().openRawResource(resId), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append('\n');
        } catch (Exception e) {
            return "";
        }
        return sb.toString();
    }

    private static final class Bridge {
        private final Listener listener;

        Bridge(Listener listener) {
            this.listener = listener;
        }

        @JavascriptInterface
        public void onSuccess(String token) {
            if (listener != null) listener.onCaptchaSolved(token);
        }

        @JavascriptInterface
        public void onExpired() {
            if (listener != null) listener.onCaptchaSolved(null);
        }

        @JavascriptInterface
        public void onError(String err) {
            if (listener != null) listener.onCaptchaSolved(null);
        }
    }
}
