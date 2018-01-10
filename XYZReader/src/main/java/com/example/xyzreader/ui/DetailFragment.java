package com.example.xyzreader.ui;


import android.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.xyzreader.R;
import com.example.xyzreader.data.Book;
import com.example.xyzreader.data.repo.BookRepository;
import com.example.xyzreader.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Singh on 09/01/18.
 */

public class DetailFragment extends Fragment implements LifecycleOwner {
    private static final String TAG = "ArticleDetailFragment";
    private LifecycleRegistry registry = new LifecycleRegistry(this);
    private int mMutedColor = 0xFF333333;

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;
    @Nullable
    private Book mBook;
    private long mItemId;
    private View mRootView;
    private ProgressBar progressBar;
    private Typeface typeface;
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    private BookDetailInteractor mBookDetailInteractor;

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

    public interface BookDetailInteractor {

    }

    public DetailFragment() {
    }

    public static DetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        BookRepository repo = BookRepository.getInstance(getActivity().getApplicationContext());
        LiveData<Book> booksData = repo.getBook(mItemId);
        booksData.observe(this, new Observer<Book>() {
            @Override
            public void onChanged(@Nullable Book book) {
                mBook = book;
                bindViews();
            }
        });

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(
                        ShareCompat
                                .IntentBuilder
                                .from(getActivity())
                                .setType("text/plain")
                                .setText("Checkout this book!")
                                .getIntent(),
                        getString(R.string.action_share)));
            }
        });
        progressBar = mRootView.findViewById(R.id.progressBar);
        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        typeface = Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_rosario_regular));
        if (context instanceof BookDetailInteractor) {
            mBookDetailInteractor = (BookDetailInteractor) context;
        } else {
            throw new RuntimeException("Activity needs to implement BookDetailInteractor");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //more code here
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START);

    }

    @Override
    public void onResume() {
        super.onResume();
        //more code here
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @Override
    public void onPause() {
        super.onPause();
        //more code here
        registry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    @Override
    public void onStop() {
        super.onStop();
        //more code here
        registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //more code here
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    private void bindViews() {
        final TextView titleView = mRootView.findViewById(R.id.article_title);
        final TextView bylineView = mRootView.findViewById(R.id.article_byline);
        final LinearLayout llHeader = mRootView.findViewById(R.id.ll_header);
        final LinearLayout llBody = mRootView.findViewById(R.id.ll_body);
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body_);

        titleView.setTypeface(typeface);
        bylineView.setTypeface(typeface);

        if (mBook != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mBook.getTitle());
            Date publishedDate = Util.parsePublishedDate(mBook.getPublishedDate());
            if (!publishedDate.before(Util.START_OF_EPOCH.getTime())) {
                bylineView.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + " by <font color='#ffffff'>"
                                + mBook.getAuthor()
                                + "</font>"));

            } else {
                // If date is before 1902, just show the string
                bylineView.setText(Html.fromHtml(
                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                                + mBook.getAuthor()
                                + "</font>"));

            }
            bodyView.setText(Html.fromHtml(mBook.getBody()));
//            final RecyclerView recyclerView = mRootView.findViewById(R.id.article_body);
//            recyclerView.setAdapter(new BookTextAdapter(getActivity().getApplicationContext(), mBook.getBody(), llHeader, new onProgressStatus() {
//
//                @Override
//                public void onProgress(boolean inProgress) {
//                    progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
//                }
//            }));


            Glide.with(getActivity())
                    .load(mBook.getPhoto())
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(new SimpleTarget<Bitmap>(50,
                            (int) (50 * (1.0f / mBook.getAspectRatio()))) {
                        @Override
                        public void onResourceReady(final Bitmap resource,
                                                    GlideAnimation glideAnimation) {
                            Palette p = Palette.generate(resource, 12);
                            int mMutedColor = p.getDarkMutedColor(0xFF333333);
                            llHeader.setBackgroundColor(mMutedColor);
                        }
                    });

//            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
//                    .get(mBook.getPhoto(), new ImageLoader.ImageListener() {
//                        @Override
//                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
//                            Bitmap bitmap = imageContainer.getBitmap();
//                            if (bitmap != null) {
//                                Palette p = Palette.generate(bitmap, 12);
//                                int mMutedColor = p.getDarkMutedColor(0xFF333333);
//                                llHeader.setBackgroundColor(mMutedColor);
//                            }
//                        }
//
//                        @Override
//                        public void onErrorResponse(VolleyError volleyError) {
//
//                        }
//                    });
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            //bodyView.setText("N/A");
        }
    }


    public interface onProgressStatus {
        void onProgress(boolean inProgress);
    }
}
