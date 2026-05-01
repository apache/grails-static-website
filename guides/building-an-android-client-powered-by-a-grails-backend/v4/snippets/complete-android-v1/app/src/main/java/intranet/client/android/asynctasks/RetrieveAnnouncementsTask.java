package intranet.client.android.asynctasks;

import android.os.AsyncTask;

import java.util.List;

import intranet.client.android.delegates.RetrieveAnnouncementsDelegate;
import intranet.client.network.AnnouncementsFetcher;
import intranet.client.network.model.Announcement;

public class RetrieveAnnouncementsTask extends AsyncTask<Void, Void, List<Announcement>> {

    private AnnouncementsFetcher fetcher = new AnnouncementsFetcher();
    private RetrieveAnnouncementsDelegate delegate;

    public RetrieveAnnouncementsTask(RetrieveAnnouncementsDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected List<Announcement> doInBackground(Void... voids) {
        return fetcher.fetchAnnouncements();
    }

    protected void onPostExecute(List<Announcement> announcements) {
        if ( delegate != null ) {
            delegate.onAnnouncementsFetched(announcements);
        }
    }
}
