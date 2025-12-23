package com.uyscuti.social.circuit.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class PaginatedAdapter<ITEM,
        VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final List<ITEM> mDataSet = new ArrayList<>();
    private OnPaginationListener mListener;
    private int mStartPage = 1;
    private int mCurrentPage = 1;
    private int mPageSize = 10;
    private RecyclerView mRecyclerView;
    private boolean loadingNewItems = true;




    public LoadMoreListener loadMoreListener;


    @NonNull
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    public int getItemCount() {
        return mDataSet.size();
    }

    public void submitItems(Collection<? extends ITEM> collection) {
        mDataSet.addAll(collection);
        notifyDataSetChanged();
        if (mListener != null) {
            mListener.onCurrentPage(mCurrentPage);
            if (collection.size() == mPageSize) {
                loadingNewItems = false;
            } else {
                mListener.onFinish();
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitItem(ITEM item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    public void submitItem(ITEM item, int position) {
        mDataSet.add(position, item);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }


    protected ITEM getItem(int position) {
        return mDataSet.get(position);
    }

    public void setStartPage(int mFirstPage) {
        this.mStartPage = mFirstPage;
        this.mCurrentPage = mFirstPage;
    }

    public int getStartPage() {
        return mStartPage;
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.mRecyclerView = recyclerView;
        initPaginating();
        setAdapter();
    }

    public void setDefaultRecyclerView(Activity activity, int recyclerViewId) {
        RecyclerView recyclerView = activity.findViewById(recyclerViewId);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setHasFixedSize(true);
        this.mRecyclerView = recyclerView;
        initPaginating();
        setAdapter();
    }

    private void setAdapter() {
        mRecyclerView.setAdapter(this);
    }

    public void setPageSize(int pageSize) {
        this.mPageSize = pageSize;
    }

    private void initPaginating() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                int totalItemCount = layoutManager.getItemCount();
                int lastVisible = layoutManager.findLastVisibleItemPosition();

                boolean endHasBeenReached = lastVisible + 2 >= totalItemCount;

                if (totalItemCount > 0 && endHasBeenReached) {
                    int nextPageNumber = (totalItemCount / mPageSize) + 1;

                    if (nextPageNumber > mCurrentPage) {
                        if (mListener != null) {
                            if (!loadingNewItems) {
                                loadingNewItems = true;
                                mListener.onNextPage(nextPageNumber);
                            }
                        }
                    }
                }
            }


        });
    }

    public void setOnPaginationListener(OnPaginationListener onPaginationListener) {
        this.mListener = onPaginationListener;
    }

    public interface OnPaginationListener {
        void onCurrentPage(int page);

        void onNextPage(int page);

        void onFinish();
    }



    public interface LoadMoreListener {
        void onLoadMore(int pageNumber);
    }

}