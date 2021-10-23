package com.ct.webviewinjectjsdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private WebView mWebView;
    private Button mBtLoadJs;
    private boolean isPageFinished;
    private TextView mTvJs;
    private boolean mInjection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @SuppressLint("JavascriptInterface")
    private void initView() {
        mWebView = findViewById(R.id.webview);
        mBtLoadJs = findViewById(R.id.bt_load_js);
        mTvJs = findViewById(R.id.tv_js);
        findViewById(R.id.agentWeb).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AgentWebViewActivity.class)));
        //TODO -- 注入--用于js调用Android callAndroid.getData("")
        mWebView.addJavascriptInterface(new AndroidInterface(), "callAndroid");
        /*打开允许调试的开关*/
        WebSettings settings = mWebView.getSettings();
        initSettint(settings);
        mWebView.setWebContentsDebuggingEnabled(true);
        mWebView.loadUrl("file:///android_asset/getUserInfo.html");
        mBtLoadJs.setOnClickListener(v -> {
            if (isPageFinished) {
                //TODO -- 因为js已经注入了，就可以直接调用了--android调用js中的方法
                loadJs("javascript:getToken();");
//                mWebView.loadUrl("javascript: sayHi();");
//                mWebView.loadUrl("javascript: f2();");
            }
        });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initSettint(WebSettings settings) {
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

//        settings.setDomStorageEnabled(true); // 是否节点缓存
//        settings.setDatabaseEnabled(true); // 是否数据缓存
//        settings.setAppCacheEnabled(true); // 是否应用缓存
//        settings.setAppCachePath(""); // 设置缓存路径
//        settings.setDefaultTextEncodingName("UTF-8"); // 设置编码格式

        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);
        settings.setSupportZoom(true);

        try {
            if (Build.VERSION.SDK_INT >= 16) {
                Class<?> clazz = settings.getClass();
                Method method = clazz.getMethod("setAllowUniversalAccessFromFileURLs", boolean.class);
                if (method != null) {
                    method.invoke(settings, true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setDomStorageEnabled(true);// 必须保留，否则无法播放优酷视频，其他的OK

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(settings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);
    }

    public static class AndroidInterface {
        //TODO JS调用Android的接口
        @JavascriptInterface
        public void getData(String data) {
            Log.e(TAG, "getData: " + data + ";thread:" + Thread.currentThread());
//            MainActivity.this.runOnUiThread(() -> mTvJs.setText(data));
        }
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            //TODO --回调到js调用Android的方法-并传送数据 message是传回的参数
            Log.e(TAG, "onJsPrompt->url: " + url + ";message=" + message + ";defaultValue:" + defaultValue + ";result:" + result.toString());
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            /**
             * js注入
             */
            isPageFinished = true;
            Log.e(TAG, "onPageFinished: " + url);
            injectJsSample2();
        }
    };

    private void injectJsSample2() {
        String tmp = "javascript: function sayHi() { " +
                "        var element1 = document.getElementById(\"input1\");\n" +
                "        element1.style.height = \"150px\";\n" +
                "        element1.style.background = \"green\";\n" +
                "}";
        // 先注入tmp
        mWebView.loadUrl(tmp);

        String tmp2 = "javascript: function f2() {\n" +
                "        var ip1Ele = document.getElementsByClassName(\"inp1\")[0];\n" +
                "        ip1Ele.style.color = \"white\";\n" +
                "        ip1Ele.style.fontSize = \"35px\";\n" +
                "    }";
        // 先注入tmp2
        mWebView.loadUrl(tmp2);
        String bean = "javascript:window.data = {name = '999999'}";
        mWebView.loadUrl(bean);
        if (mInjection) {
            return;
        }
        onJsLocal();
        mInjection = true;
        // 然后再调用
//        mWebView.loadUrl("javascript: onload(sayHi());");
        // 因为js已经注入了，就可以直接调用了
//        mWebView.loadUrl("javascript: sayHi();");
//        mWebView.loadUrl("javascript: f2();");
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onJsLocal() {
        StringBuilder builder = new StringBuilder(JSTools.getJS(this, "XCZXBridge.js"));//XCZXBridge-index
        Log.e(TAG, "onJsLocal: " + builder.toString());
        loadJs(builder.toString());
    }

    private void loadJs(String builder) {
        //TODO 针对不同版本 需要进行更换注入方法,否则注入失败 同时不同的方法注入的参数也不一样；（javascript）
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mWebView.loadUrl("javascript:" + builder);
        } else {
            mWebView.evaluateJavascript(builder, value -> Log.e(TAG, "onReceiveValue: " + value));
        }
    }
}