package intranet.client.android.asynctasks;

import android.os.AsyncTask;

import intranet.client.android.delegates.RetrieveAnnouncementDelegate;
import intranet.client.network.AnnouncementFetcher;
import intranet.client.network.model.Announcement;

public class RetrieveAnnouncementTask extends AsyncTask<Long, Void, Announcement> {
    private static final String TAG = RetrieveAnnouncementTask.class.getSimpleName();
    AnnouncementFetcher fetcher = new AnnouncementFetcher();
    private RetrieveAnnouncementDelegate delegate;

    public RetrieveAnnouncementTask(RetrieveAnnouncementDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    protected Announcement doInBackground(Long... ids) {
        if ( ids != null && ids.length >= 1) {
            Long announcementId = ids[0];
            return fetcher.fetchAnnouncement(announcementId);
        }

        return null;
    }

    protected void onPostExecute(Announcement announcement) {
        if ( delegate != null ) {
            delegate.onAnnouncementFetched(announcement);
        }
    }
}
