package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.xyzreader.R;
import com.example.xyzreader.data.Book;
import com.example.xyzreader.data.BookConstants;
import com.example.xyzreader.data.repo.BookRepository;

import java.util.List;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity implements LifecycleOwner, DetailFragment.BookDetailInteractor {

    private List<Book> mBooks;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemPosition;

    private ViewPager mPager;
    private ImageView ivPhoto;
    private MyPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        ivPhoto = findViewById(R.id.detail_photo);

        setToolBar();
        setPager();

        if (savedInstanceState == null) {
            mStartId = getIntent().getLongExtra(BookConstants.EXTRA_BOOK_ID, 0);
            if (getIntent() != null) {
                mSelectedItemId = mStartId;
            }
        }
        BookRepository repo = BookRepository.getInstance(getApplicationContext());
        LiveData<List<Book>> booksData = repo.getBooks();
        booksData.observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(@Nullable List<Book> books) {
                if (books != null) {
                    for (Book book : books) {
                        if (book.getId() == mSelectedItemId) {
                            mSelectedItemPosition = books.indexOf(book);
                        }
                    }
                    onDataLoaded(books);
                }
            }
        });
    }

    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setPager() {
        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSelectedItemId = mBooks.get(position).getId();
                mSelectedItemPosition = position;
                changePhoto(mBooks.get(position).getId(), mBooks.get(position).getPhoto(), mBooks.get(position).getAspectRatio());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void changePhoto(long id, String url, float aspectRatio) {

        Glide.with(this)
                .load(url)
                .apply(RequestOptions.centerInsideTransform().placeholder(R.color.ltgray))
                .into(ivPhoto);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = getString(R.string.detail_transition, (int) id);
            ivPhoto.setTransitionName(transitionName);
        }
    }


    private void onDataLoaded(List<Book> books) {
        mBooks = books;
        mPagerAdapter.notifyDataSetChanged();

        if (mStartId > 0 && books != null) {
            for (Book book : books) {
                if (book.getId() == mStartId) {
                    final int position = mBooks.indexOf(book);
                    mPager.setCurrentItem(position, false);
                    break;
                }
            }
            mStartId = 0;
        }
        if (mSelectedItemPosition > -1) {
            changePhoto(mBooks.get(mSelectedItemPosition).getId(), mBooks.get(mSelectedItemPosition).getPhoto(), mBooks.get(mSelectedItemPosition).getAspectRatio());
        }
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return DetailFragment.newInstance(mBooks.get(position).getId());
        }

        @Override
        public int getCount() {
            return (mBooks != null) ? mBooks.size() : 0;
        }
    }
}
