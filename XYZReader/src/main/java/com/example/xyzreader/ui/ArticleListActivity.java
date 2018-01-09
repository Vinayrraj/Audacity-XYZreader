package com.example.xyzreader.ui;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.xyzreader.R;
import com.example.xyzreader.data.Book;
import com.example.xyzreader.data.BookConstants;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.data.repo.BookRepository;
import com.example.xyzreader.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity implements LifecycleOwner {

    private static final String TAG = ArticleListActivity.class.toString();
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private Typeface rosarioTypeface;
    private LiveData<List<Book>> booksData;
    private Adapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        final View toolbarContainerView = findViewById(R.id.toolbar_container);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        rosarioTypeface = Typeface.createFromAsset(getAssets(), getString(R.string.font_rosario_regular));
        RecyclerView mRecyclerView = findViewById(R.id.recycler_view);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        if (savedInstanceState == null) {
            refresh();
        }


        adapter = new Adapter(rosarioTypeface);
        adapter.setHasStableIds(true);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);
        mRecyclerView.scheduleLayoutAnimation();
        mRecyclerView.setAdapter(adapter);
        BookRepository repo = BookRepository.getInstance(getApplicationContext());
        booksData = repo.getBooks();
        booksData.observe(this, new Observer<List<Book>>() {
            @Override
            public void onChanged(@Nullable List<Book> books) {
                adapter.setData(books);
            }
        });

    }


    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mRefreshingReceiver,
                new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mRefreshingReceiver);
    }

    private BroadcastReceiver mRefreshingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                updateRefreshingUI(intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false));
            }
        }
    };

    private void updateRefreshingUI(boolean isRefreshing) {
        mSwipeRefreshLayout.setRefreshing(isRefreshing);
    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        return ArticleLoader.newAllArticlesInstance(this);
//    }

//    @Override
//    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        Adapter adapter = new Adapter(cursor, rosarioTypeface);
//        adapter.setHasStableIds(true);
//        mRecyclerView.setAdapter(adapter);
//        int columnCount = getResources().getInteger(R.integer.list_column_count);
//        StaggeredGridLayoutManager sglm =
//                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(sglm);
//        mRecyclerView.scheduleLayoutAnimation();
//    }

//    @Override
//    public void onLoaderReset(Loader<Cursor> loader) {
//        mRecyclerView.setAdapter(null);
//    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {

        private Typeface typeface;
        private List<Book> mBooks;


        public Adapter(Typeface typeface) {
            this.typeface = typeface;
        }

        @Override
        public long getItemId(int position) {
            return getBook(position).getId();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view, typeface);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    startActivity(new Intent(Intent.ACTION_VIEW,
//                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.putExtra(BookConstants.EXTRA_BOOK_ID, getItemId(vh.getAdapterPosition()));
                    startActivity(intent);
                }
            });
            return vh;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.titleView.setText(getBook(position).getTitle());
            Date publishedDate = Util.parsePublishedDate(getBook(position).getPublishedDate());
            if (!publishedDate.before(Util.START_OF_EPOCH.getTime())) {
                holder.subtitleView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + getBook(position).getAuthor()));
            } else {
                holder.subtitleView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate)
                                + "<br/>" + " by "
                                + getBook(position).getAuthor()));
            }

            holder.thumbnailView.setAspectRatio(getBook(position).getAspectRatio());
            Glide.with(holder.thumbnailView)
                    .load(getBook(position).getThumb())
                    .apply(RequestOptions.centerInsideTransform().placeholder(R.color.ltgray))
                    .into(holder.thumbnailView);

//            holder.thumbnailView.setImageUrl(
//                    mCursor.getString(ArticleLoader.Query.THUMB_URL),
//                    ImageLoaderHelper.getInstance(ArticleListActivity.this).getImageLoader());
//            holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }

        private Book getBook(int position) {
            return mBooks.get(position);
        }

        @Override
        public int getItemCount() {
            if (mBooks != null) {
                return mBooks.size();
            } else
                return 0;
        }

        public void setData(List<Book> books) {
            mBooks = books;
            notifyDataSetChanged();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view, Typeface typeface) {
            super(view);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            titleView.setTypeface(typeface);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
            subtitleView.setTypeface(typeface);
        }
    }
}
