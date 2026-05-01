package intranet.client.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import intranet.client.network.model.Announcement;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AnnouncementsFetcher {

    private final static String TAG = AnnouncementsFetcher.class.getSimpleName();

    private OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public List<Announcement> fetchAnnouncements() {
        Type listType = new TypeToken<List<Announcement>>() {}.getType();
        try {
            String url = Constants.GRAILS_APP_URL + Constants.ANNOUNCEMENTS_PATH;
            String jsonString = fetchAnnouncementsJsonString(url);
            return gson.fromJson(jsonString, listType);

        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return new ArrayList<>();
        }
    }

    private String fetchAnnouncementsJsonString(String url) throws IOException {
        Request request = NetworkTask.requestWithUrl(url);
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
