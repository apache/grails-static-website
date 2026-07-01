package intranet.client.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import intranet.client.R;
import intranet.client.android.delegates.AnnouncementAdapterDelegate;
import intranet.client.network.model.Announcement;

public class AnnouncementAdapter extends ArrayAdapter<Announcement> {
    private AnnouncementAdapterDelegate delegate;

    public AnnouncementAdapter(Context context, List<Announcement> announcements, AnnouncementAdapterDelegate delegate) {
        super(context, 0, announcements);
        this.delegate = delegate;
    }

    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_announcement, parent, false);
        }
        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        Announcement announcement = getItem(position);
        if ( announcement != null ) {
            tvTitle.setText(announcement.getTitle());
            tvTitle.setOnClickListener(new AnnouncementClickListener(announcement));
        }
        return convertView;
    }

    private class AnnouncementClickListener implements View.OnClickListener {
        private Announcement announcement;

        AnnouncementClickListener(Announcement announcement) {
            this.announcement = announcement;
        }

        @Override
        public void onClick(View view) {
            if ( delegate != null ) {
                delegate.onAnnouncementTapped(announcement);
            }
        }
    }
}
