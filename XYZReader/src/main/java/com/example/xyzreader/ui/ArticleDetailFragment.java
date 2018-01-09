//package com.example.xyzreader.ui;
//
//import android.app.Fragment;
//import android.arch.lifecycle.Lifecycle;
//import android.arch.lifecycle.LifecycleOwner;
//import android.arch.lifecycle.LifecycleRegistry;
//import android.arch.lifecycle.LiveData;
//import android.arch.lifecycle.Observer;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Bitmap;
//import android.graphics.Color;
//import android.graphics.Rect;
//import android.graphics.Typeface;
//import android.graphics.drawable.ColorDrawable;
//import android.os.Bundle;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v4.app.ShareCompat;
//import android.support.v7.graphics.Palette;
//import android.text.Html;
//import android.text.format.DateUtils;
//import android.text.method.LinkMovementMethod;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.ImageLoader;
//import com.example.xyzreader.R;
//import com.example.xyzreader.data.Book;
//import com.example.xyzreader.data.repo.BookRepository;
//import com.example.xyzreader.util.Util;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
///**
// * A fragment representing a single Article detail screen. This fragment is
// * either contained in a {@link ArticleListActivity} in two-pane mode (on
// * tablets) or a {@link ArticleDetailActivity} on handsets.
// */
//public class ArticleDetailFragment extends Fragment implements LifecycleOwner {
//    private static final String TAG = "ArticleDetailFragment";
//
//    public static final String ARG_ITEM_ID = "item_id";
//    private static final float PARALLAX_FACTOR = 1.25f;
//
//    @Nullable
//    private Book mBook;
//    private long mItemId;
//    private View mRootView;
//    private int mMutedColor = 0xFF333333;
//    private ObservableScrollView mScrollView;
//    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
//    private ColorDrawable mStatusBarColorDrawable;
//
//    private int mTopInset;
//    private View mPhotoContainerView;
//    private ImageView mPhotoView;
//    private int mScrollY;
//    private boolean mIsCard = false;
//    private int mStatusBarFullOpacityBottom;
//    private Typeface typeface;
//
//    // Use default locale format
//    private SimpleDateFormat outputFormat = new SimpleDateFormat();
//
//    /**
//     * Mandatory empty constructor for the fragment manager to instantiate the
//     * fragment (e.g. upon screen orientation changes).
//     */
//    public ArticleDetailFragment() {
//    }
//
//    public static ArticleDetailFragment newInstance(long itemId) {
//        Bundle arguments = new Bundle();
//        arguments.putLong(ARG_ITEM_ID, itemId);
//        ArticleDetailFragment fragment = new ArticleDetailFragment();
//        fragment.setArguments(arguments);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        if (getArguments().containsKey(ARG_ITEM_ID)) {
//            mItemId = getArguments().getLong(ARG_ITEM_ID);
//        }
//
//        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
//        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
//                R.dimen.detail_card_top_margin);
//        setHasOptionsMenu(true);
//    }
//
//    public ArticleDetailActivity getActivityCast() {
//        return (ArticleDetailActivity) getActivity();
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        BookRepository repo = BookRepository.getInstance(getActivity().getApplicationContext());
//        LiveData<Book> booksData = repo.getBook(mItemId);
//        booksData.observe(this, new Observer<Book>() {
//            @Override
//            public void onChanged(@Nullable Book book) {
//                mBook = book;
//                bindViews();
//            }
//        });
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
//        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
//                mRootView.findViewById(R.id.draw_insets_frame_layout);
//        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
//            @Override
//            public void onInsetsChanged(Rect insets) {
//                mTopInset = insets.top;
//            }
//        });
//
//        mScrollView = (ObservableScrollView) mRootView.findViewById(R.id.scrollview);
//        mScrollView.setCallbacks(new ObservableScrollView.Callbacks() {
//            @Override
//            public void onScrollChanged() {
//                mScrollY = mScrollView.getScrollY();
//                getActivityCast().onUpButtonFloorChanged(mItemId, ArticleDetailFragment.this);
//                mPhotoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
//                updateStatusBar();
//            }
//        });
//
//        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
//        mPhotoContainerView = mRootView.findViewById(R.id.photo_container);
//
//        mStatusBarColorDrawable = new ColorDrawable(0);
//
//        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
//                        .setType("text/plain")
//                        .setText("Some sample text")
//                        .getIntent(), getString(R.string.action_share)));
//            }
//        });
//
//        bindViews();
//        updateStatusBar();
//        return mRootView;
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        typeface = Typeface.createFromAsset(getActivity().getAssets(), getString(R.string.font_rosario_regular));
//    }
//
//    private void updateStatusBar() {
//        int color = 0;
//        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
//            float f = progress(mScrollY,
//                    mStatusBarFullOpacityBottom - mTopInset * 3,
//                    mStatusBarFullOpacityBottom - mTopInset);
//            color = Color.argb((int) (255 * f),
//                    (int) (Color.red(mMutedColor) * 0.9),
//                    (int) (Color.green(mMutedColor) * 0.9),
//                    (int) (Color.blue(mMutedColor) * 0.9));
//        }
//        mStatusBarColorDrawable.setColor(color);
//        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
//    }
//
//    static float progress(float v, float min, float max) {
//        return constrain((v - min) / (max - min), 0, 1);
//    }
//
//    static float constrain(float val, float min, float max) {
//        if (val < min) {
//            return min;
//        } else if (val > max) {
//            return max;
//        } else {
//            return val;
//        }
//    }
//
//    private void bindViews() {
//        if (mRootView == null) {
//            return;
//        }
//
//        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
//        titleView.setTypeface(typeface);
//        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
//        bylineView.setTypeface(typeface);
//        bylineView.setMovementMethod(new LinkMovementMethod());
//        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
//        bodyView.setTypeface(typeface);
//
//
//        bodyView.setTypeface(typeface);
//
//        if (mBook != null) {
//            mRootView.setAlpha(0);
//            mRootView.setVisibility(View.VISIBLE);
//            mRootView.animate().alpha(1);
//            titleView.setText(mBook.getTitle());
//            Date publishedDate = Util.parsePublishedDate(mBook.getPublishedDate());
//            if (!publishedDate.before(Util.START_OF_EPOCH.getTime())) {
//                bylineView.setText(Html.fromHtml(
//                        DateUtils.getRelativeTimeSpanString(
//                                publishedDate.getTime(),
//                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
//                                DateUtils.FORMAT_ABBREV_ALL).toString()
//                                + " by <font color='#ffffff'>"
//                                + mBook.getAuthor()
//                                + "</font>"));
//
//            } else {
//                // If date is before 1902, just show the string
//                bylineView.setText(Html.fromHtml(
//                        outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
//                                + mBook.getAuthor()
//                                + "</font>"));
//
//            }
//            bodyView.setText(Html.fromHtml(mBook.getBody()));
//            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
//                    .get(mBook.getPhoto(), new ImageLoader.ImageListener() {
//                        @Override
//                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
//                            Bitmap bitmap = imageContainer.getBitmap();
//                            if (bitmap != null) {
//                                Palette p = Palette.generate(bitmap, 12);
//                                mMutedColor = p.getDarkMutedColor(0xFF333333);
//                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
//                                mRootView.findViewById(R.id.meta_bar)
//                                        .setBackgroundColor(mMutedColor);
//                                updateStatusBar();
//                            }
//                        }
//
//                        @Override
//                        public void onErrorResponse(VolleyError volleyError) {
//
//                        }
//                    });
//        } else {
//            mRootView.setVisibility(View.GONE);
//            titleView.setText("N/A");
//            bylineView.setText("N/A");
//            bodyView.setText("N/A");
//        }
//    }
//
//
//    public int getUpButtonFloor() {
//        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
//            return Integer.MAX_VALUE;
//        }
//
//        // account for parallax
//        return mIsCard
//                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
//                : mPhotoView.getHeight() - mScrollY;
//    }
//
//    @NonNull
//    @Override
//    public Lifecycle getLifecycle() {
//        return registry;
//    }
//
//    private LifecycleRegistry registry = new LifecycleRegistry(this);
//}
