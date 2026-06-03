package com.example.hybridapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> uploadMessage;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mfumo Mkuu wa Layout (Vertical)
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);

        // Thanh ya Juu ya Vitufe (Toolbar)
        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setBackgroundColor(Color.parseColor("#007AFF")); // Rangi ya Bluu ya Kisasa
        toolbar.setPadding(10, 10, 10, 10);

        // 1. Kitufe cha Back (Nyuma)
        Button btnBack = new Button(this);
        btnBack.setText("◀ Nyuma");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setBackgroundColor(Color.TRANSPARENT);

        // 2. Kitufe cha Nenda Online
        Button btnOnline = new Button(this);
        btnOnline.setText("🌐 Online");
        btnOnline.setTextColor(Color.WHITE);
        btnOnline.setBackgroundColor(Color.TRANSPARENT);

        // 3. Kitufe cha Refresh (Kusafisha)
        final Button btnRefresh = new Button(this);
        btnRefresh.setText("🔄 Refresh");
        btnRefresh.setTextColor(Color.WHITE);
        btnRefresh.setBackgroundColor(Color.TRANSPARENT);
        btnRefresh.setVisibility(View.GONE); // Kinaanza kikiwa kimejificha (Offline)

        // 4. Kitufe cha Share (Kushare)
        Button btnShare = new Button(this);
        btnShare.setText("🔗 Share");
        btnShare.setTextColor(Color.WHITE);
        btnShare.setBackgroundColor(Color.TRANSPARENT);

        // Kuongeza vitufe kwenye Toolbar
        toolbar.addView(btnBack);
        toolbar.addView(btnOnline);
        toolbar.addView(btnRefresh);
        toolbar.addView(btnShare);

        // Kutengeneza WebView
        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccess(true);

        // Kusimamia matukio ya kurasa (Navigation & Refresh Visibility)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Kama mtumiaji yuko kwenye index.html ficha Refresh, la sivyo ionyeshe
                if (url.startsWith("file:///")) {
                    btnRefresh.setVisibility(View.GONE);
                } else {
                    btnRefresh.setVisibility(View.VISIBLE);
                }
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                if (uploadMessage != null) { uploadMessage.onReceiveValue(null); uploadMessage = null; }
                uploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try { startActivityForResult(intent, FILECHOOSER_RESULTCODE); } catch (Exception e) { uploadMessage = null; return false; }
                return true;
            }
            @Override
            public void onPermissionRequest(final PermissionRequest request) { if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { request.grant(request.getResources()); } }
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) { callback.invoke(origin, true, false); }
        });

        // --- Kazi za Vitufe (Button Clicks) ---

        // Kazi ya kitufe cha Back
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    webView.loadUrl("file:///android_asset/index.html");
                }
            }
        });

        // Kazi ya kitufe cha Online
        btnOnline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("URL_PLACEHOLDER");
            }
        });

        // Kazi ya kitufe cha Refresh
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        // Kazi ya kitufe cha Share
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Angalia App Hii");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Habari! Pakua app yetu na utembelee tovuti yetu hapa: URL_PLACEHOLDER");
                startActivity(Intent.createChooser(shareIntent, "Share kupitia"));
            }
        });

        // Unganisha kila kitu kwenye Skrini
        mainLayout.addView(toolbar);
        mainLayout.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        setContentView(mainLayout);

        ombaRuhusaZaSimu();

        // Fungua index.html moja kwa moja mwanzoni
        webView.loadUrl("file:///android_asset/index.html");
    }

    // Mfumo wa simu wa kawaida wa kubonyeza kurudi nyuma
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (uploadMessage == null) return;
            Uri[] results = null;
            if (resultCode == RESULT_OK && data != null) {
                String dataString = data.getDataString();
                if (dataString != null) { results = new Uri[]{Uri.parse(dataString)}; }
            }
            uploadMessage.onReceiveValue(results);
            uploadMessage = null;
        }
    }

    private void ombaRuhusaZaSimu() {
        String[] permissions = { Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION };
        if (!hasPermissions(this, permissions)) { ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE); }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) { if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) { return false; } }
        }
        return true;
    }
}
