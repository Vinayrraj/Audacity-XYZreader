package com.example.xyzreader.data.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.xyzreader.data.Book;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Created by Singh on 09/01/18.
 */

@Dao
public interface BookDao {

    @Insert(onConflict = REPLACE)
    long[] update(Book... book);

    @Query("SELECT * FROM book ORDER BY publishedDate DESC")
    LiveData<List<Book>> getBooks();

    @Query("SELECT * FROM book WHERE id = :id")
    LiveData<Book> getBook(int id);
}
