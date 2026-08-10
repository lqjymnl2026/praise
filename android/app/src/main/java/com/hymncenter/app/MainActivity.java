package com.hymncenter.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** 赞美诗中心 · 安卓壳（WebView 封装）
 *  默认加载“手机采集端”；支持拍照/相册/文件上传；首次启动可改网址。 */
public class MainActivity extends AppCompatActivity {
    private static final String PREFS = "hymn_prefs";
    private static final String KEY_URL = "url";
    private static final String DEFAULT_URL = "https://currency-nav-export-plastics.trycloudflare.com/mobile";
    private static final int REQ_CODE_FILE = 1001;
    private static final int REQ_CODE_PERM = 1002;

    private WebView webView;
    private ValueCallback<Uri[]> filePathCallback;
    private Uri cameraUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView wv, ValueCallback<Uri[]> callback, FileChooserParams params) {
                filePathCallback = callback;
                return openFileChooser(params);
            }
        });

        // 请求相机/相册权限（Android 6+）
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> need = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                need.add(Manifest.permission.CAMERA);
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED)
                    need.add(Manifest.permission.READ_MEDIA_IMAGES);
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                    need.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (!need.isEmpty())
                ActivityCompat.requestPermissions(this, need.toArray(new String[0]), REQ_CODE_PERM);
        }

        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String url = sp.getString(KEY_URL, "");
        if (url.isEmpty()) askUrl();
        else webView.loadUrl(url);
    }

    private void askUrl() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("赞美诗中心");
        b.setMessage("请输入系统网址（默认是手机采集端，可直接点进入）：");
        final EditText et = new EditText(this);
        et.setText(DEFAULT_URL);
        et.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        b.setView(et);
        b.setPositiveButton("进入", (d, w) -> {
            String url = et.getText().toString().trim();
            if (url.isEmpty()) url = DEFAULT_URL;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_URL, url).apply();
            webView.loadUrl(url);
        });
        b.setCancelable(false);
        b.show();
    }

    private boolean openFileChooser(WebChromeClient.FileChooserParams params) {
        Intent pick = new Intent(Intent.ACTION_GET_CONTENT);
        pick.addCategory(Intent.CATEGORY_OPENABLE);
        pick.setType("*/*");
        pick.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        List<Intent> intents = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            try {
                cameraUri = createImageUri();
                Intent cam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cam.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                intents.add(cam);
            } catch (IOException ignored) {}
        }

        Intent chooser = Intent.createChooser(pick, "选择照片 / 文件");
        if (!intents.isEmpty())
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new Intent[0]));
        try {
            startActivityForResult(chooser, REQ_CODE_FILE);
            return true;
        } catch (Exception e) {
            filePathCallback = null;
            return false;
        }
    }

    private Uri createImageUri() throws IOException {
        File dir = new File(getCacheDir(), "captures");
        if (!dir.exists()) dir.mkdirs();
        File img = new File(dir, "capture_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", img);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_FILE && filePathCallback != null) {
            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getClipData() != null) {
                        int n = data.getClipData().getItemCount();
                        results = new Uri[n];
                        for (int i = 0; i < n; i++) results[i] = data.getClipData().getItemAt(i).getUri();
                    } else if (data.getData() != null) {
                        results = new Uri[]{data.getData()};
                    }
                } else if (cameraUri != null) {
                    results = new Uri[]{cameraUri};
                }
            }
            filePathCallback.onReceiveValue(results);
            filePathCallback = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 权限授予后无需额外处理；下次选“拍照”即可用
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
