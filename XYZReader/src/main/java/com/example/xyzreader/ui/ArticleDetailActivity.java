package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.graphics.Bitmap;
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

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.Book;
import com.example.xyzreader.data.BookConstants;
import com.example.xyzreader.data.repo.BookRepository;

import java.util.List;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity implements LifecycleOwner {

    private List<Book> mBooks;
    private long mStartId;

    private long mSelectedItemId;
    private int mSelectedItemPosition;

    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private ImageView mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }

            @Override
            public void onPageSelected(int position) {
                if (mSelectedItemId != mBooks.get(position).getId()) {
                    mSelectedItemId = mBooks.get(position).getId();
                    changePhoto(mSelectedItemId, mBooks.get(position).getPhoto(), mBooks.get(position).getAspectRatio());
                }
            }
        });

        mImage = findViewById(R.id.thumbnail);

        if (savedInstanceState == null) {
            if (getIntent() != null) {
                mStartId = getIntent().getLongExtra(BookConstants.EXTRA_BOOK_ID, 0);
                mSelectedItemId = mStartId;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String transitionName = getString(R.string.detail_transition, (int) mStartId);
                    mImage.setTransitionName(transitionName);
                }
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

    private void changePhoto(long id, String url, float aspectRatio) {

        ImageLoaderHelper.getInstance(this).getImageLoader()
                .get(url, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            mImage.setAlpha(0f);
                            mImage.setImageBitmap(bitmap);
                            mImage
                                    .animate()
                                    .alpha(1f)
                                    .setDuration(getResources().getInteger(R.integer.anim_duration_medium));
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String transitionName = getString(R.string.detail_transition, (int) id);
            mImage.setTransitionName(transitionName);
        }
    }

    private void onDataLoaded(List<Book> books) {
        mBooks = books;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0 && mBooks != null) {
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

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return ArticleDetailFragment.newInstance(mBooks.get(position).getId());
        }

        @Override
        public int getCount() {
            return (mBooks != null) ? mBooks.size() : 0;
        }
    }
}
