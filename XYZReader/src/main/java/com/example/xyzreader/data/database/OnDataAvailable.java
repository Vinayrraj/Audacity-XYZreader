package com.example.xyzreader.data.database;

import com.example.xyzreader.data.Book;

import java.util.List;

/**
 * Created by Singh on 09/01/18.
 */

public interface OnDataAvailable {
    void onBookAvailable(List<Book> books);
    void onError(String message);
}
