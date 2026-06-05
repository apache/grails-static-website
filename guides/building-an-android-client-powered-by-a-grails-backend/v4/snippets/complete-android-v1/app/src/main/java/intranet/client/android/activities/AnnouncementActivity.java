package intranet.client.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import intranet.client.R;

public class AnnouncementActivity extends Activity {

    private TextView tvTitle;
    private WebView wvBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        wvBody = (WebView) findViewById(R.id.wvBody);
        populateUi();
    }

    private void populateUi() {
        Intent intent = getIntent();
        final String title = intent.getStringExtra(MainActivity.EXTRA_TITLE);
        final String body = intent.getStringExtra(MainActivity.EXTRA_BODY);
        populateUiWithTitleAndBody(title, body);
    }

    private void populateUiWithTitleAndBody(final String title, final String body) {
        tvTitle.setText(title);
        final String mime = "text/html";
        final String encoding = "utf-8";
        wvBody.loadDataWithBaseURL(null, body, mime, encoding, null);
    }
}
