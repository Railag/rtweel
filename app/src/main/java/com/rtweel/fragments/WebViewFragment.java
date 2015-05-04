package com.rtweel.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.rtweel.Const;
import com.rtweel.R;

/**
 * Created by root on 4.5.15.
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, null, false);

        initWebView(v);

        return v;
    }

    private void initWebView(View v) {
        webView = (WebView) v.findViewById(R.id.webview);

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

}
