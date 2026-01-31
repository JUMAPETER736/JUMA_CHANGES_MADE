package com.uyscuti.sharedmodule.adapter.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uyscuti.sharedmodule.data.model.Comment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AdPaginatedAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private final List<Comment> mDataSet = new ArrayList<>();
    private OnPaginationListener mListener;
    private int mStartPage = 1;
    private int mCurrentPage = 1;
    private int mPageSize = 10;
    private RecyclerView mRecyclerView;
    private boolean loadingNewItems = true;
    private int parentItemPosition = -1;


    @NonNull
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    public abstract void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads);

    public int getItemCount() {
        return mDataSet.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitItems(Collection<? extends Comment> collection) {
        int previousSize = mDataSet.size();
        mDataSet.addAll(collection);

        // Notify the adapter about the inserted items
        notifyItemRangeInserted(previousSize, collection.size());
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
    public void submitItem(Comment item) {
        mDataSet.add(item);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitItem(Comment item, int position) {
        mDataSet.add(position, item);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        mDataSet.clear();
        notifyDataSetChanged();
    }


    protected Comment getItem(int position) {
        return mDataSet.get(position);
    }

    protected void setItem(int position, Comment commentItem) {

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
        recyclerView.setHasFixedSize(true);
        initPaginating();
        setAdapter();
    }

    public void setDefaultRecyclerView(Activity activity, int recyclerViewId) {
        String TAG = "setDefaultRecyclerView";

        Log.d("setDefaultRecyclerView", "setDefaultRecyclerView: activity: "+ activity+" re id: "+ recyclerViewId);
        RecyclerView recyclerView = activity.findViewById(recyclerViewId);

        Log.d(TAG, "setDefaultRecyclerView: 1");
        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        Log.d(TAG, "setDefaultRecyclerView: 2");
        recyclerView.setHasFixedSize(true);
        Log.d(TAG, "setDefaultRecyclerView: 3");
        this.mRecyclerView = recyclerView;
        Log.d(TAG, "setDefaultRecyclerView: 4");
        initPaginating();
        Log.d(TAG, "setDefaultRecyclerView: 5");
        setAdapter();
        Log.d(TAG, "setDefaultRecyclerView: 6");
    }


    private void setAdapter() {
        mRecyclerView.setAdapter(this);
    }

    public RecyclerView.Adapter getAdapter() {
        return mRecyclerView.getAdapter();
    }
    public RecyclerView.LayoutManager getLayoutManager() {
        return mRecyclerView.getLayoutManager();
    }


    public void setPagination() {
        initPaginating();
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
                boolean endHasBeenReached = lastVisible + 2 >= totalItemCount;
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

    public void updateItem(int position, Comment updatedItem) {
        if (position >= 0 && position < mDataSet.size()) {
            // Update the data in the dataset
            mDataSet.set(position, updatedItem);

            for(int i = 0; i < updatedItem.getReplies().size(); i++) {
                Log.d("updateItem", "updated item position " + position + " is liked "+ updatedItem.getReplies().get(i).isLiked() + " position " +i);
            }

            // Notify the adapter about the change
            notifyItemChanged(position);
        }
    }


    public void resetParentAdapterPosition() {
        parentItemPosition = -1;
    }


    public void changePlayingStatus() {
        Log.d("changePlayingStatus", "invoke changePlayingStatus ");


        for (int i = 0; i < mDataSet.size(); i++) {
            Comment item = mDataSet.get(i);

            if (item.isReplyPlaying()) {
                Log.d("changePlayingStatus", "reply playing for parent position - " + i);

                item.setReplyPlaying(false);

                for (int j = 0; j < item.getReplies().size(); j++) {
                    com.uyscuti.social.network.api.response.commentreply.allreplies.Comment replyItem = item.getReplies().get(j);

                    Log.d("Replies", "Replies count is " + item.getReplies().size());
                    Log.d("Replies", "Replies was playing  " + replyItem.isPlaying() + " on position " + j);
                    replyItem.setProgress(0f);
                    item.getReplies().set(j, replyItem);

                }
                mDataSet.set(i, item);

                notifyItemChanged(i);

            }
            else if (item.isPlaying()) { // Assuming there's a method to check if the item is playing
                // If an item with isPlaying true is found, update its position and set it to false
                Log.i("changePlayingStatus", "(else)changePlayingStatus isPlaying" + item.isPlaying() + " position " + i);
                Log.i("changePlayingStatus", "(else)changePlayingStatus isReplyPlaying" + item.isReplyPlaying() + " position " + i);


                item.setPlaying(false);

                String fileType = item.getFileType();
                Log.d("changePlayingStatus","fileType: "+fileType+ " progress "+item.getProgress());

                mDataSet.set(i, item);
                notifyItemChanged(i);
            }
            // Update the item in the dataset
        }
    }
    public interface OnPaginationListener {
        void onCurrentPage(int page);

        void onNextPage(int page);

        void onFinish();
    }

}