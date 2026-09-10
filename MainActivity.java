package com.vitpulse.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Window;

public class MainActivity extends Activity {
    private WebView web;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        Window w = getWindow();
        w.setStatusBarColor(android.graphics.Color.rgb(131,58,180));
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false); s.setSupportZoom(false);
        web.setWebViewClient(new WebViewClient()); web.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        web.addJavascriptInterface(new NativeBridge(), "VITNative");
        setContentView(web);
        NotificationScheduler.createChannel(this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 42);
        web.loadUrl("file:///android_asset/index.html");
    }
    public class NativeBridge {
        @JavascriptInterface public boolean isNative() { return true; }
        @JavascriptInterface public void enableReminders(String json, int mins) { NotificationScheduler.saveAndSchedule(MainActivity.this, json, mins); }
        @JavascriptInterface public void disableReminders() { NotificationScheduler.clear(MainActivity.this); }
    }
    @Override public void onBackPressed(){ if(web.canGoBack()) web.goBack(); else super.onBackPressed(); }
}
