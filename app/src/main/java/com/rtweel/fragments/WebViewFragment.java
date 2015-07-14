package com.rtweel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.rtweel.Const;
import com.rtweel.R;

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
        //TODO title for url
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, null, false);

        initWebView(v);

        setRetainInstance(true);

        setTitle(webView.getTitle());

        return v;
    }

    private void initWebView(View v) {
        webView = (WebView) v.findViewById(R.id.webview);

        webView.setWebViewClient(new WVClient());

        webView.setWebChromeClient(new WebChromeClient());

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
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.i("Web", "url " + url + " is finished");
            super.onPageFinished(view, url);
        }

    }

}
