package com.khalti.form.EBanking.loadBank;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.khalti.R;
import com.khalti.carbonX.widget.ProgressBar;
import com.utila.EmptyUtil;
import com.utila.LogUtil;
import com.utila.NetworkUtil;
import com.utila.ResourceUtil;

import org.apache.http.util.EncodingUtils;

import java.util.HashMap;

import fontana.RobotoTextView;

public class BankingActivity extends AppCompatActivity implements BankContract.View {
    private BankContract.Listener listener;
    private ProgressBar pdLoad;
    private WebView wvBank;
    private ImageView ivMessage;
    private RobotoTextView tvMessage;
    private ScrollView svIndented;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bank);
        listener = new BankPresenter(this);

        pdLoad = (ProgressBar) findViewById(R.id.pdLoad);
        wvBank = (WebView) findViewById(R.id.wvBank);
        ivMessage = (ImageView) findViewById(R.id.ivMessage);
        tvMessage = (RobotoTextView) findViewById(R.id.tvMessage);
        svIndented = (ScrollView) findViewById(R.id.svIndented);

        new Handler().postDelayed(() -> listener.setupLayout(NetworkUtil.isNetworkAvailable(this)), 100);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public HashMap<?, ?> receiveArguments() {
        Bundle bundle = getIntent().getBundleExtra("bundle");
        return EmptyUtil.isNotNull(bundle) ? (HashMap<?, ?>) bundle.getSerializable("map") : null;
    }

    @Override
    public void setUpToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24px);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void setupWebView(String url, String postData) {
        listener.toggleIndentedProgress(true);

        wvBank.getSettings().setUseWideViewPort(true);
        wvBank.getSettings().setLoadWithOverviewMode(true);
        wvBank.getSettings().setSupportZoom(true);
        wvBank.getSettings().setBuiltInZoomControls(true);
        wvBank.getSettings().setDisplayZoomControls(false);
        wvBank.getSettings().setJavaScriptEnabled(true);

        wvBank.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
                Uri uri = request.getUrl();
                return shouldOverrideUrlLoading(uri.toString());
            }

            private boolean shouldOverrideUrlLoading(final String url) {
                return true;
            }

            public void onPageFinished(WebView view, String url) {
                LogUtil.log("finished url", url);
                listener.toggleIndentedProgress(false);
                svIndented.setVisibility(View.GONE);
                wvBank.setVisibility(View.VISIBLE);
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                LogUtil.log("Error", error.getDescription());
                super.onReceivedError(view, request, error);
                if (NetworkUtil.isNetworkAvailable(getApplicationContext())) {
                    listener.showIndentedError(ResourceUtil.getString(getApplicationContext(), R.string.generic_error));
                } else {
                    listener.showIndentedNetworkError();
                }
            }
        });
        wvBank.postUrl(url, EncodingUtils.getBytes(postData, "BASE64"));
    }

    @Override
    public void toggleIndentedProgress(boolean show) {
        if (show) {
            svIndented.setVisibility(View.VISIBLE);
            pdLoad.setBackgroundColor(ResourceUtil.getColor(this, R.color.disabled));
            pdLoad.setVisibility(View.VISIBLE);
            tvMessage.setText(ResourceUtil.getString(this, R.string.loading_bank));
        } else {
            svIndented.setVisibility(View.GONE);
            pdLoad.setBackgroundColor(ResourceUtil.getColor(this, R.color.white));
            pdLoad.setVisibility(View.GONE);
        }
    }

    @Override
    public void showIndentedError(String message) {
        wvBank.setVisibility(View.GONE);
        svIndented.setVisibility(View.VISIBLE);
        tvMessage.setText(message);
    }

    @Override
    public void showIndentedNetworkError() {
        wvBank.setVisibility(View.GONE);
        pdLoad.setBackgroundColor(ResourceUtil.getColor(this, R.color.white));
        pdLoad.setVisibility(View.GONE);
        svIndented.setVisibility(View.VISIBLE);
        tvMessage.setText(ResourceUtil.getString(this, R.string.network_error_body));
    }

    @Override
    public void setListener(BankContract.Listener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
