package com.example.hybridapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.GeolocationPermissions;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 123;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> uploadMessage;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setBackgroundColor(Color.parseColor("#007AFF"));
        toolbar.setPadding(10, 10, 10, 10);

        Button btnBack = new Button(this);
        btnBack.setText("◀ Nyuma");
        btnBack.setTextColor(Color.WHITE);
        btnBack.setBackgroundColor(Color.TRANSPARENT);

        Button btnOnline = new Button(this);
        btnOnline.setText("🌐 Online");
        btnOnline.setTextColor(Color.WHITE);
        btnOnline.setBackgroundColor(Color.TRANSPARENT);

        final Button btnRefresh = new Button(this);
        btnRefresh.setText("🔄 Refresh");
        btnRefresh.setTextColor(Color.WHITE);
        btnRefresh.setBackgroundColor(Color.TRANSPARENT);
        btnRefresh.setVisibility(View.GONE);

        Button btnShare = new Button(this);
        btnShare.setText("🔗 Share");
        btnShare.setTextColor(Color.WHITE);
        btnShare.setBackgroundColor(Color.TRANSPARENT);

        toolbar.addView(btnBack);
        toolbar.addView(btnOnline);
        toolbar.addView(btnRefresh);
        toolbar.addView(btnShare);

        webView = new WebView(this);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccess(true);

        webView.addJavascriptInterface(new WebAppInterface(this), "Android");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
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

        btnBack.setOnClickListener(v -> { if (webView.canGoBack()) { webView.goBack(); } else { webView.loadUrl("file:///android_asset/index.html"); } });
        btnOnline.setOnClickListener(v -> webView.loadUrl("URL_PLACEHOLDER"));
        btnRefresh.setOnClickListener(v -> webView.reload());
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Habari! Pakua app yetu hapa: URL_PLACEHOLDER");
            startActivity(Intent.createChooser(shareIntent, "Share kupitia"));
        });

        mainLayout.addView(toolbar);
        mainLayout.addView(webView, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
        setContentView(mainLayout);

        ombaRuhusaZaSimu();
        webView.loadUrl("file:///android_asset/index.html");
    }

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }

        @JavascriptInterface
        public void wekaDefaultSmsApp() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!Telephony.Sms.getDefaultSmsPackage(mContext).equals(mContext.getPackageName())) {
                    Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                    intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, mContext.getPackageName());
                    mContext.startActivity(intent);
                } else {
                    Toast.makeText(mContext, "App hii tayari ni Default SMS App!", Toast.LENGTH_SHORT).show();
                }
            }
        }

        @JavascriptInterface
        public String pataMajinaYaSimu() {
            JSONArray contactsArray = new JSONArray();
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                Cursor cursor = mContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
                if (cursor != null) {
                    try {
                        while (cursor.moveToNext()) {
                            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                            int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                            if (nameIndex != -1 && numberIndex != -1) {
                                JSONObject contact = new JSONObject();
                                contact.put("name", cursor.getString(nameIndex));
                                contact.put("number", cursor.getString(numberIndex));
                                contactsArray.put(contact);
                            }
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    cursor.close();
                }
            }
            return contactsArray.toString();
        }

        // SEHEMU MPYA: Inavuta SMS halisi kutoka kwenye Inbox au Sent za simu yako
        @JavascriptInterface
        public String pataSmsZilizopo(String folderType) {
            JSONArray smsArray = new JSONArray();
            Uri uri = Uri.parse("content://sms/" + folderType); 
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
                Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, "date DESC");
                if (cursor != null) {
                    try {
                        int limit = 40; // Kuzuia app kuelemewa na meseji elfu nne
                        int count = 0;
                        while (cursor.moveToNext() && count < limit) {
                            String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
                            String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                            long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                            
                            JSONObject sms = new JSONObject();
                            sms.put("number", address);
                            sms.put("body", body);
                            sms.put("date", new Date(dateLong).toLocaleString());
                            smsArray.put(sms);
                            count++;
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    cursor.close();
                }
            }
            return smsArray.toString();
        }

        @JavascriptInterface
        public void tumaSmsMojaKwaMoja(String namba, String ujumbe) {
            try {
                SmsManager smsManager;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    smsManager = mContext.getSystemService(SmsManager.class);
                } else {
                    smsManager = SmsManager.getDefault();
                }
                // Tuma ujumbe halisi
                smsManager.sendTextMessage(namba, null, ujumbe, null, null);
                Toast.makeText(mContext, "Ujumbe umesafirishwa! (Report: Success)", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(mContext, "Report: Imefeli! " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void ombaRuhusaZaSimu() {
        String[] permissions = { 
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, 
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS, 
            Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS 
        };
        if (!hasPermissions(this, permissions)) { ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE); }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) { if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) { return false; } }
        }
        return true;
    }
}
