package com.example.xyzreader.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import com.example.xyzreader.data.Book;

/**
 * Created by Singh on 09/01/18.
 */

@Database(entities = {Book.class}, version = 1)
public abstract class BookDatabase extends RoomDatabase {
    private static final String LOG_TAG = BookDatabase.class.getSimpleName();
    private static final String DATABASE_NAME = "book_database";


    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static BookDatabase sInstance;

    public static BookDatabase getInstance(Context context) {
        Log.d(LOG_TAG, "Getting the database");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        BookDatabase.class,
                        BookDatabase.DATABASE_NAME)
                        .build();
                Log.d(LOG_TAG, "Made new database Instance");
            }
        }
        return sInstance;
    }

    // The associated DAOs for the database
    public abstract BookDao bookDao();
}
