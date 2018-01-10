package com.example.xyzreader.data;

import android.app.IntentService;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.Time;
import android.util.Log;

import com.example.xyzreader.data.database.OnDataAvailable;
import com.example.xyzreader.data.repo.BookRepository;
import com.example.xyzreader.remote.RemoteEndpointUtil;

import java.util.List;

public class UpdaterService extends IntentService {
    private static final String TAG = "UpdaterService";

    public static final String BROADCAST_ACTION_STATE_CHANGE
            = "com.example.xyzreader.intent.action.STATE_CHANGE";
    public static final String EXTRA_REFRESHING
            = "com.example.xyzreader.intent.extra.REFRESHING";

    public UpdaterService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Time time = new Time();

        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null || !ni.isConnected()) {
            Log.w(TAG, "Not online, not refreshing.");
            return;
        }

        sendStickyBroadcast(new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, true));

        List<Book> books = RemoteEndpointUtil.fetchBooks();
        if (books != null) {
            BookRepository repo = BookRepository.getInstance(getApplicationContext());
            for (Book book : books) {
                book.getBody().replaceAll("(\r\n|\n)", "<br />");
            }
            repo.setBooks(books, onDataAvailable);
        } else {
            onDataAvailable.onError("");
        }
    }

    private final OnDataAvailable onDataAvailable = new OnDataAvailable() {
        @Override
        public void onBookAvailable(List<Book> books) {
            sendStickyBroadcast(
                    new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
        }

        @Override
        public void onError(String message) {
            sendStickyBroadcast(
                    new Intent(BROADCAST_ACTION_STATE_CHANGE).putExtra(EXTRA_REFRESHING, false));
        }
    };
}
