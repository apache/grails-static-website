package intranet.client.android.delegates;

import java.util.List;

import intranet.client.network.model.Announcement;

public interface RetrieveAnnouncementsDelegate {
    void onAnnouncementsFetched(List<Announcement> announcements);
}
