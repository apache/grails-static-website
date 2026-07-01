package intranet.client.network;

import okhttp3.Request;

class NetworkTask {
    static Request requestWithUrl(String url) {
        return new Request.Builder()
                .url(url)
                .header("Accept-Version", Constants.ACCEPT_VERSION)
                .build();
    }
}