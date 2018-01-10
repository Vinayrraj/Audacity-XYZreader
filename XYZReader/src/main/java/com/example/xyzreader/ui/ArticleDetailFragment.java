package com.example.xyzreader.ui;

import android.app.Fragment;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.Book;
import com.example.xyzreader.data.repo.BookRepository;
import com.example.xyzreader.util.Util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements LifecycleOwner {
    private static final String TAG = "ArticleDetailFragment";
    private LifecycleRegistry registry = new LifecycleRegistry(this);

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Book mBook;
    private long mItemId;
    private View mRootView;
    private ProgressBar mProgressBar;

    private SimpleDateFormat outputFormat = new SimpleDateFormat();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Checkout this book!!")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        return mRootView;
    }


    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        mProgressBar = mRootView.findViewById(R.id.progressBar);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        final View header = mRootView.findViewById(R.id.container_title);


        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

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
            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mBook.getPhoto(), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            updateStatusBar(imageContainer.getBitmap(), header);
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
            mProgressBar.setVisibility(View.GONE);
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }


    protected void updateStatusBar(final Bitmap bitmap, final View header) {
        if (bitmap != null) {
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(Palette palette) {
                    int colorValue;
                    if (palette.getDarkVibrantSwatch() != null) {
                        colorValue = palette.getDarkVibrantSwatch().getRgb();
                    } else if (palette.getDarkMutedSwatch() != null) {
                        colorValue = palette.getDarkMutedSwatch().getRgb();
                    } else {
                        return;
                    }

                    if (getActivity() == null || isDetached() || !isAdded()) {
                        return;
                    }
                    animateBackgroundColor(header, colorValue);
                }
            });
        }
    }

    protected void animateBackgroundColor(View view, int newColor) {
        if (view != null) {
            Drawable currentBG = view.getBackground();
            Drawable newBG = new ColorDrawable(newColor);
            if (currentBG == null) {
                view.setBackground(newBG);
            } else {
                TransitionDrawable transitionDrawable = new
                        TransitionDrawable(new Drawable[]{currentBG, newBG});
                transitionDrawable.setCrossFadeEnabled(false);
                view.setBackground(transitionDrawable);
                if (view.isAttachedToWindow()) {
                    transitionDrawable.startTransition(getResources()
                            .getInteger(R.integer.anim_duration_medium));
                }
            }
        }
    }
}
