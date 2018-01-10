package com.example.xyzreader.data.repo;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.xyzreader.app.AppExecutors;
import com.example.xyzreader.data.Book;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.data.database.BookDao;
import com.example.xyzreader.data.database.BookDatabase;
import com.example.xyzreader.data.database.OnDataAvailable;

import java.util.List;

/**
 * Created by Singh on 09/01/18.
 */

public class BookRepository {

    private static final String LOG_TAG = BookRepository.class.getSimpleName();
    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static BookRepository sInstance;
    private final BookDao mBookDao;
    private final AppExecutors mExecutors;
    private final Context mContext;
    private boolean mInitialized = false;


    private BookRepository(Context context) {
        BookDatabase database = BookDatabase.getInstance(context.getApplicationContext());
        mExecutors = AppExecutors.getInstance();
        mBookDao = database.bookDao();
        mContext = context;
    }

    public synchronized static BookRepository getInstance(Context context) {
        Log.d(LOG_TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new BookRepository(context);
                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    /**
     * Database related operations
     **/

    public LiveData<List<Book>> getBooks() {
        return mBookDao.getBooks();
    }

    public void setBooks(final List<Book> books, final OnDataAvailable onDataResult) {
        mExecutors.diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mBookDao.update(books.toArray(new Book[books.size()]));
                mExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (onDataResult != null)
                            onDataResult.onBookAvailable(books);
                    }
                });
            }
        });
    }

    public LiveData<Book> getBook(long id) {
        return mBookDao.getBook(id);
    }

    public void refreshBooks() {
        mContext.startService(new Intent(mContext, UpdaterService.class));
    }
}