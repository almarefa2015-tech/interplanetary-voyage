package com.interplanetary.voyage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.window.OnBackInvokedDispatcher;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebViewAssetLoader;

/** Interplanetary Voyage: a thin native shell around the offline HTML game in /assets. */
public class MainActivity extends Activity {

    private static final String HOST = "appassets.androidplatform.net";
    private static final String START_URL = "https://" + HOST + "/assets/index.html";

    private WebView web;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setupWebView();
        setContentView(web);
        hideSystemBars();
        registerBackHandler();
        web.loadUrl(START_URL);
    }

    private void setupWebView() {
        web = new WebView(this);
        web.setBackgroundColor(0xFF000000);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setHapticFeedbackEnabled(false);
        web.setOnLongClickListener(v -> true);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setTextZoom(100);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .setDomain(HOST)
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                return loader.shouldInterceptRequest(r.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if (HOST.equals(u.getHost())) return false;
                openOutside(u);
                return true;
            }
        });
        // window.open(...) from the page (e.g. the "rate the game" button) goes to the Play Store app / browser.
        web.getSettings().setSupportMultipleWindows(true);
        web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        web.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView v, boolean dialog, boolean user, android.os.Message msg) {
                WebView tmp = new WebView(MainActivity.this);
                tmp.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView w, WebResourceRequest r) {
                        openOutside(r.getUrl());
                        return true;
                    }
                });
                ((WebView.WebViewTransport) msg.obj).setWebView(tmp);
                msg.sendToTarget();
                return true;
            }
        });
    }

    private void openOutside(Uri u) {
        try {
            if ("play.google.com".equals(u.getHost())) {
                String id = u.getQueryParameter("id");
                if (id != null) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + id)));
                        return;
                    } catch (Exception ignored) {
                    }
                }
            }
            startActivity(new Intent(Intent.ACTION_VIEW, u));
        } catch (Exception ignored) {
        }
    }

    private void hideSystemBars() {
        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= 30) {
            w.setDecorFitsSystemWindows(false);
            WindowInsetsController c = w.getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.systemBars());
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            w.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void registerBackHandler() {
        if (Build.VERSION.SDK_INT >= 33) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT, () -> moveTaskToBack(true));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) {
            web.onResume();
            web.resumeTimers();
        }
    }

    @Override
    protected void onPause() {
        if (web != null) {
            web.onPause();
            web.pauseTimers();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
