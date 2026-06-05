package intranet.client.android.delegates;

import intranet.client.network.model.Announcement;

public interface RetrieveAnnouncementDelegate {
    void onAnnouncementFetched(Announcement announcement);
}
