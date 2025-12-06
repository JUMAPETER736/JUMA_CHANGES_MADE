package com.uyscuti.social.circuit.adapter.feed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class FeedPaginatedAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final List<com.uyscuti.social.network.api.response.posts.Post> mDataSet = new ArrayList<>();
    private OnPaginationListener mListener;
    private int mStartPage = 1;
    private int mCurrentPage = 1;
    private int mPageSize = 10;
    private RecyclerView mRecyclerView;
    private boolean loadingNewItems = true;
    private int childItemPosition = -1;
    private int parentItemPosition = -1;


    public LoadMoreListener loadMoreListener;


    @NonNull
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    public abstract void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads);

    public int getItemCount() {
        return mDataSet.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitItems(Collection<? extends com.uyscuti.social.network.api.response.posts.Post> collection) {
        int previousSize = mDataSet.size();

        // Filter out items from collection that have the same IDs as items in mDataSet
        List<com.uyscuti.social.network.api.response.posts.Post> itemsToAdd = new ArrayList<>();
        for (com.uyscuti.social.network.api.response.posts.Post newPost : collection) {
            boolean found = false;
            for (com.uyscuti.social.network.api.response.posts.Post existingPost : mDataSet) {
                if (existingPost.get_id().equals(newPost.get_id())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                itemsToAdd.add(newPost);
            }
        }
        mDataSet.addAll(itemsToAdd);

        notifyItemRangeInserted(previousSize, collection.size());
        if (mListener != null) {
            mListener.onCurrentPage(mCurrentPage);
            if (itemsToAdd.size() == mPageSize) {
                loadingNewItems = false;
            } else {
                mListener.onFinish();
            }
        }
        if (!itemsToAdd.isEmpty()) {
            Log.d("submitItems", "Items added: " + itemsToAdd.size() + " collection size: " + collection.size());
        }else {
            Log.d("submitItems", "submitItems: items to add is empty");
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitItem(com.uyscuti.social.network.api.response.posts.Post item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    public void submitItem(com.uyscuti.social.network.api.response.posts.Post item, int position) {
        mDataSet.add(position, item);
        notifyItemChanged(position);

    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        try {
            mDataSet.remove(position);
            notifyItemRemoved(position);
        }catch (Exception exception) {
            Log.e("FeedPaginatedAdapter", "removeItem failed because: "+exception.getMessage());
            exception.printStackTrace();
        }

    }

    public int getPositionById(String itemId) {
        for (int i = 0; i < mDataSet.size(); i++) {
            if (mDataSet.get(i).get_id().equals(itemId)) {
                return i; // Return position if ID matches
            }
        }
        return -1; // Return -1 if item with given ID is not found
    }

    public com.uyscuti.social.network.api.response.posts.Post getItemById(String itemId) {
        for (com.uyscuti.social.network.api.response.posts.Post item : mDataSet) {
            if (item.get_id().equals(itemId)) {
                return item; // Return item if ID matches
            }
        }
        return null; // Return null if item with given ID is not found
    }

    protected com.uyscuti.social.network.api.response.posts.Post getItem(int position) {
        return mDataSet.get(position);
    }

    protected void setItem(int position, com.uyscuti.social.network.api.response.posts.Post commentItem) {

        mDataSet.set(position, commentItem);

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

    private void initPaginating() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                assert layoutManager != null;
                int totalItemCount = layoutManager.getItemCount();

                int lastVisible = layoutManager.findLastVisibleItemPosition();
                boolean endHasBeenReached = lastVisible + 5 >= totalItemCount;
                if (totalItemCount > 0 && endHasBeenReached) {
                    if (mListener != null) {
                        if (!loadingNewItems) {
                            loadingNewItems = true;
                            mListener.onNextPage(++mCurrentPage);
                        }
                    }
                }
            }
        });
    }

    public void setOnPaginationListener(OnPaginationListener onPaginationListener) {
        this.mListener = onPaginationListener;
    }

    public void updateItem(int position,com.uyscuti.social.network.api.response.posts. Post updatedItem) {
        if (position >= 0 && position < mDataSet.size()) {
            // Update the data in the dataset
            mDataSet.set(position, updatedItem);

            notifyItemChanged(position);
        }
    }

    public void resetChildAdapterPosition() {
        childItemPosition = -1;
    }

    public void resetParentAdapterPosition() {
        parentItemPosition = -1;
    }

    public void refreshParent(int position) {
        Log.d("refreshParent", "invoke refreshParent  position " + position);
        notifyItemChanged(position);

    }
    public void changePlayingStatus() {
        Log.d("changePlayingStatus", "invoke changePlayingStatus ");

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