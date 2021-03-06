package com.ct.webviewinjectjsdemo;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.just.agentweb.AgentWeb;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

import java.util.HashMap;

public class AgentWebViewActivity extends AppCompatActivity {
    private static final String TAG = "AgentWebViewActivity";
    private AgentWeb mAgentWeb;
    private boolean mInjection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_web_view);
        LinearLayout llWeb = findViewById(R.id.ll_web);

        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(llWeb, new LinearLayout.LayoutParams(-1, -1))
                .useDefaultIndicator()
                .setWebChromeClient(mWebChromeClient)
                .setWebViewClient(mWebViewClient)
                .createAgentWeb()
                .ready()
                .go("file:///android_asset/getUserInfo.html");
        if (mAgentWeb != null) {
            mAgentWeb.getJsInterfaceHolder().addJavaObject("callAndroid", new MainActivity.AndroidInterface());
        }
    }

    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            Log.e(TAG, "onJsPrompt->url: " + url + ";message=" + message + ";defaultValue:" + defaultValue + ";result:" + result.toString());
            return super.onJsPrompt(view, url, message, defaultValue, result);
        }
    };
    protected WebViewClient mWebViewClient = new WebViewClient() {

        private HashMap<String, Long> timer = new HashMap<>();

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
        }

        @Nullable
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return super.shouldInterceptRequest(view, request);
        }

        //
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {

            Log.i(TAG, "view:" + view.getHitTestResult());
            Log.i(TAG, "mWebViewClient shouldOverrideUrlLoading:" + url);
            //?????????????????????????????????????????? ??? ???????????????????????? true  ?????????????????? H5 ?????? ??????????????????????????????????????? ???????????? false ??? DefaultWebClient  ?????????intent ???????????? ????????? ??? ????????????????????????????????? ??????????????? ??? ????????????????????? ??? ??????????????? ??? ???????????????????????????????????? .
            if (url.startsWith("intent://") && url.contains("com.youku.phone")) {
                return true;
            }
			/*else if (isAlipay(view, mUrl))   //1.2.5?????????????????????????????? ????????????????????????sdk?????? ??? DefaultWebClient ?????????????????????url???????????????
			    return true;*/
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.i(TAG, "mUrl:" + url + " onPageStarted  target:");
            timer.put(url, System.currentTimeMillis());
//            if (url.equals(getUrl())) {
//                pageNavigator(View.GONE);
//            } else {
//                pageNavigator(View.VISIBLE);
//            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (timer.get(url) != null) {
                long overTime = System.currentTimeMillis();
                Long startTime = timer.get(url);
                Log.i(TAG, "  page mUrl:" + url + "  used time:" + (overTime - startTime));
            }

            injectJsSample2();
        }
        /*???????????????????????? ??? ??????????????????????????? ???????????????????????????????????? ??? ?????????????????????????????????????????????*/
	   /* public void onMainFrameError(AbsAgentWebUIController agentWebUIController, WebView view, int errorCode, String description, String failingUrl) {

            Log.i(TAG, "AgentWebFragment onMainFrameError");
            agentWebUIController.onMainFrameError(view,errorCode,description,failingUrl);

        }*/

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);

//			Log.i(TAG, "onReceivedHttpError:" + 3 + "  request:" + mGson.toJson(request) + "  errorResponse:" + mGson.toJson(errorResponse));
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
            super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            Log.i(TAG, "onReceivedError:" + errorCode + "  description:" + description + "  errorResponse:" + failingUrl);
        }
    };

    private void injectJsSample2() {
        String tmp = "javascript: function sayHi() { " +
                "        var element1 = document.getElementById(\"input1\");\n" +
                "        element1.style.height = \"150px\";\n" +
                "        element1.style.background = \"green\";\n" +
                "}";
        // ?????????tmp
//        mWebView.loadUrl(tmp);

        String tmp2 = "javascript: function f2() {\n" +
                "        var ip1Ele = document.getElementsByClassName(\"inp1\")[0];\n" +
                "        ip1Ele.style.color = \"white\";\n" +
                "        ip1Ele.style.fontSize = \"35px\";\n" +
                "    }";
        // ?????????tmp2
//        mWebView.loadUrl(tmp2);
        String bean = "javascript:window.data = {name = '999999'}";
//        mWebView.loadUrl(bean);
        if (mInjection) {
            return;
        }
        onJsLocal();
        mInjection = true;
        // ???????????????
//        mWebView.loadUrl("javascript: onload(sayHi());");
        // ??????js??????????????????????????????????????????
//        mWebView.loadUrl("javascript: sayHi();");
//        mWebView.loadUrl("javascript: f2();");
    }

    @SuppressLint("ObsoleteSdkInt")
    public void onJsLocal() {
        StringBuilder builder = new StringBuilder(JSTools.getJS(this, "XCZXBridge.js"));//XCZXBridge-index
        Log.e(TAG, "onJsLocal: " + builder.toString());
//        loadJs(builder.toString());
        mAgentWeb.getJsAccessEntrace().callJs(builder.toString());
    }


    private void loadJs(String builder) {
        //TODO ?????????????????? ??????????????????????????????,?????????????????? ??????????????????????????????????????????????????????javascript???
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
//            mWebView.loadUrl("javascript:" + builder);
//        } else {
//            mWebView.evaluateJavascript(builder, value -> Log.e(TAG, "onReceiveValue: " + value));
//        }
    }
}