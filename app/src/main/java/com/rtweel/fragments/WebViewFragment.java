package com.rtweel.fragments;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rtweel.Const;
import com.rtweel.R;
import com.rtweel.tasks.tweet.TwitterGetAccessTokenTask;

/**
 * Created by firrael on 4.5.15.
 */
public class WebViewFragment extends BaseFragment {

    private WebView webView;

    @Override
    public void onStart() {
        super.onStart();

        Bundle args = getArguments();

        if (args != null)
            loadUrl(args.getString(Const.URL));
    }

    @Override
    protected String getTitle() {
        return webView.getTitle();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, null, false);

        initWebView(v);

        setRetainInstance(true);

        setTitle(webView.getTitle());

        return v;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(View v) {
        webView = (WebView) v.findViewById(R.id.webview);

        webView.setWebViewClient(new WVClient());

        webView.setWebChromeClient(new WebChromeClient());

        CookieManager.getInstance().setAcceptCookie(true);

        final WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(false);
        settings.setUseWideViewPort(false);
        settings.setSupportMultipleWindows(false);
        settings.setGeolocationEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setGeolocationDatabasePath(getActivity().getFilesDir().getPath());
        settings.setSavePassword(false);
    }

    private void loadUrl(String url) {
        webView.loadUrl(url);
    }

    public boolean isCanGoBack() {
        return webView.canGoBack();
    }

    public void goBack() {
        webView.goBack();
    }


    private class WVClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            setTitle(url);

            if (url.contains(Const.TWITTER_CALLBACK_URL)) {
                getMainActivity().showLoadingBar();
                Uri uri = Uri.parse(url);
                String verifier = uri.getQueryParameter(Const.URL_PARAMETER_TWITTER_OAUTH_VERIFIER);
                new TwitterGetAccessTokenTask(getMainActivity())
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, verifier);

                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i("Web", "url " + url + " is finished");
            super.onPageFinished(view, url);
        }

    }

}
