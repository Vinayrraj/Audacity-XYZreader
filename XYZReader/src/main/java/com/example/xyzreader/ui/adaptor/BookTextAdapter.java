package com.example.xyzreader.ui.adaptor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.DetailFragment;

/**
 * Created by Singh on 10/01/18.
 */

public class BookTextAdapter extends RecyclerView.Adapter<BookTextAdapter.PageHolder> {

    private Context mContext;
    private String mBody;
    private LinearLayout mContainer;
    Pagination mPagination;
    private DetailFragment.onProgressStatus mOnProgressStatus;

    public BookTextAdapter(Context context, String body, LinearLayout llHeader, DetailFragment.onProgressStatus onProgressStatus) {


        mContext = context;
        mBody = body;
        mContainer = llHeader;
        mOnProgressStatus = onProgressStatus;
    }

    @Override
    public PageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.page_text_item, parent, false);
        final PageHolder vh = new PageHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(PageHolder holder, int position) {
        holder.article_body.setText(mPagination.get(position));
    }

    @Override
    public int getItemCount() {
        if (mPagination != null) {
            return mPagination.size();
        } else {
            return 0;
        }
    }

    class PageHolder extends RecyclerView.ViewHolder {
        public final TextView article_body;

        public PageHolder(View view) {
            super(view);
            article_body = view.findViewById(R.id.article_body);
        }
    }
}