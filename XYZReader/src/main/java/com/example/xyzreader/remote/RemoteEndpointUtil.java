package com.example.xyzreader.remote;

import android.util.Log;

import com.example.xyzreader.data.Book;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RemoteEndpointUtil {
    private static final String TAG = "RemoteEndpointUtil";
    static Type listType = new TypeToken<List<Book>>() {
    }.getType();

    private RemoteEndpointUtil() {
    }

    public static List<Book> fetchBooks() {
        String itemsJson = null;
        try {
            itemsJson = fetchPlainText(Config.BASE_URL);
            Log.i(TAG, "Json: " + itemsJson);
        } catch (IOException e) {
            Log.e(TAG, "Error fetching items JSON", e);
            return null;
        }

        List<Book> val = new Gson().fromJson(itemsJson, listType);
        return val;
    }

    static String fetchPlainText(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
