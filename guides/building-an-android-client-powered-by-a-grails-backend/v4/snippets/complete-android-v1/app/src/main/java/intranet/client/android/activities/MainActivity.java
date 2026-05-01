package intranet.client.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import intranet.client.R;
import intranet.client.android.adapters.AnnouncementAdapter;
import intranet.client.android.asynctasks.RetrieveAnnouncementsTask;
import intranet.client.android.delegates.AnnouncementAdapterDelegate;
import intranet.client.android.delegates.RetrieveAnnouncementsDelegate;
import intranet.client.network.model.Announcement;

public class MainActivity extends Activity
        implements RetrieveAnnouncementsDelegate, AnnouncementAdapterDelegate {

    public static final String EXTRA_ID = "id";
    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_BODY = "body";
    private AnnouncementAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView announcementsListView = (ListView) findViewById(R.id.announcementsListView);
        adapter = new AnnouncementAdapter(this, new ArrayList<Announcement>(), this);
        announcementsListView.setAdapter(adapter);

        new RetrieveAnnouncementsTask(this).execute(); // <1>
    }

    @Override
    public void onAnnouncementsFetched(List<Announcement> announcements) {  // <2>
        adapter.clear();
        adapter.addAll(announcements);
    }

    @Override
    public void onAnnouncementTapped(Announcement announcement) {
        segueToAnnouncementActivity(announcement);
    }

    private void segueToAnnouncementActivity(Announcement announcement) {
        Intent i = new Intent(this, AnnouncementActivity.class);
        i.putExtra(EXTRA_ID, announcement.getId());
        i.putExtra(EXTRA_TITLE, announcement.getTitle());
        i.putExtra(EXTRA_BODY, announcement.getBody());
        startActivity(i);
    }
}
