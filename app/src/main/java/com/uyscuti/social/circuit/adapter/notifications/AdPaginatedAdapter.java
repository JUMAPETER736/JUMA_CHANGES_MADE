package com.uyscuti.social.circuit.adapter.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uyscuti.social.circuit.data.model.Comment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public abstract class AdPaginatedAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private final List<Comment> mDataSet = new ArrayList<>();
    private OnPaginationListener mListener;
    private int mStartPage = 1;
    private int mCurrentPage = 1;
    private int mPageSize = 10;
    private RecyclerView mRecyclerView;
    private boolean loadingNewItems = true;
    private int childItemPosition = -1;
    private int parentItemPosition = -1;


    public com.uyscuti.social.circuit.adapter.PaginatedAdapter.LoadMoreListener loadMoreListener;


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
//        notifyDataSetChanged();
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

    //    public void submitItems(Collection<? extends Comment> collection) {
//
//        int previousSize = mDataSet.size();
//        mDataSet.addAll(collection);
////        notifyDataSetChanged();
//        // Notify the adapter about the inserted items
//        CommentsDiffUtil diffUtil = new CommentsDiffUtil(mDataSet, (List<Comment>) collection);
//        DiffUtil.DiffResult diffResults = DiffUtil.calculateDiff(diffUtil);
//        mDataSet = (List<Comment>) collection;
////        notifyItemRangeInserted(previousSize, collection.size());
//        if (mListener != null) {
//            mListener.onCurrentPage(mCurrentPage);
//            if (collection.size() == mPageSize) {
//                loadingNewItems = false;
//            } else {
//                mListener.onFinish();
//            }
//        }
//    }
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

    public int getPositionByUploadId(String itemId) {
        for (int i = 0; i < mDataSet.size(); i++) {
            if (Objects.equals(mDataSet.get(i).getUploadId(), itemId)) {
                return i; // Return position if ID matches
            }
        }
        return -1; // Return -1 if item with given ID is not found
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
//        activity.setAdapter();
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
    @SuppressLint("NotifyDataSetChanged")
    public void setLayoutManager(Activity activity, int recyclerViewId) {
        String TAG = "setDefaultRecyclerView";

        Log.d("setDefaultRecyclerView", "setDefaultRecyclerView: activity: "+ activity+" re id: "+ recyclerViewId);
        RecyclerView recyclerView = activity.findViewById(recyclerViewId);
//        activity.setAdapter();
        Log.d(TAG, "setDefaultRecyclerView: 1");
//        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
//        Log.d(TAG, "setDefaultRecyclerView: 2");
//        recyclerView.setHasFixedSize(true);
//        Log.d(TAG, "setDefaultRecyclerView: 3");
//        this.mRecyclerView = recyclerView;
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        Log.d(TAG, "setDefaultRecyclerView: 4");
        notifyDataSetChanged();
        initPaginating();
    }
//    public RecyclerView.LayoutManager getLayoutManager() {
//        return mRecyclerView.getLayoutManager();
//    }

    public RecyclerView getmRecyclerView() {
        return mRecyclerView;
    }

    public void setPagination() {
        initPaginating();
    }
    public void setAdapterManually(){mRecyclerView.setAdapter(this);}
    public void setPageSize(int pageSize) {
        this.mPageSize = pageSize;
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
//            Log.d("updateItem", "updated item audios" + updatedItem.getAudios());
            // Notify the adapter about the change
            notifyItemChanged(position);
        }
    }

    //    public void changePlayingStatus() {
//        Log.d("changePlayingStatus", "invoke changePlayingStatus ");
//
//        for (int i = 0; i < mDataSet.size(); i++) {
//            Comment item = mDataSet.get(i);
//            // Assuming ITEM has a method to set isPlaying value
//            // Change the isPlaying value to false
////            item.setPlaying(false);
//            if (item.isReplyPlaying()) {
//                Log.i("changePlayingStatus", "changePlayingStatus " + item.isReplyPlaying() + " position " + i);
//                Bundle bundle = new Bundle();
//
//                for (int j = 0; j < item.getReplies().size(); j++) {
//
//                    com.uyscut.network.api.response.commentreply.allreplies.Comment replyItem = item.getReplies().get(j);
//                    if (replyItem.isPlaying()) {
//                        bundle.putInt("childPosition", j);
//                        item.setPlaying(false);
//                        item.setReplyPlaying(false);
//
//                        Log.i("changePlayingStatus", "changePlayingStatus setPlaying" + item.isPlaying() + " position " + j);
//
//                        notifyItemChanged(i, bundle);
//
//                        return;
//
//                    }
//                }
//            } else if (item.isPlaying()) { // Assuming there's a method to check if the item is playing
//                // If an item with isPlaying true is found, update its position and set it to false
//                Log.i("changePlayingStatus", "(else)changePlayingStatus isPlaying" + item.isPlaying() + " position " + i);
//                Log.i("changePlayingStatus", "(else)changePlayingStatus isReplyPlaying" + item.isReplyPlaying() + " position " + i);
//
//
//                item.setPlaying(false);
//                item.setReplyPlaying(false);
//                mDataSet.set(i, item);
//                notifyItemChanged(i);
//            }
//            // Update the item in the dataset
//        }
//    }

    public int childAdapterItemToRefresh() {

        return childItemPosition;
    }

    public int parentAdapterItemToRefresh() {

        return parentItemPosition;
    }

    public void resetChildAdapterPosition() {
        childItemPosition = -1;
    }

    public void resetParentAdapterPosition() {
        parentItemPosition = -1;
    }

    //    public void changePlayingStatus() {
//        Log.d("changePlayingStatus", "invoke changePlayingStatus ");
//
//
//        for (int i = 0; i < mDataSet.size(); i++) {
//            Comment item = mDataSet.get(i);
//
//            if (item.isReplyPlaying()) {
//                Log.d("changePlayingStatus", "reply playing on parent pos "+i);
//
//                for (int j = 0; j < item.getReplies().size(); j++) {
//                    com.uyscut.network.api.response.commentreply.allreplies.Comment replyItem = item.getReplies().get(j);
//                    if (replyItem.isPlaying()) {
//                        Log.d("changePlayingStatus", "reply playing on pos "+j);
//                        Log.d("changePlayingStatus", "find a way to update only reply item on position "+ j);
//                        childItemPosition = j;
//                        parentItemPosition = i;
//                        item.setPlaying(false);
//                        item.setReplyPlaying(false);
//                        replyItem.setPlaying(false);
//                        return;
//                    }
//                }
//                return;
//            } else if (item.isPlaying()) { // Assuming there's a method to check if the item is playing
//                // If an item with isPlaying true is found, update its position and set it to false
//                Log.i("changePlayingStatus", "(else)changePlayingStatus isPlaying" + item.isPlaying() + " position " + i);
//                Log.i("changePlayingStatus", "(else)changePlayingStatus isReplyPlaying" + item.isReplyPlaying() + " position " + i);
//
//
//                item.setPlaying(false);
//                item.setReplyPlaying(false);
//                mDataSet.set(i, item);
//                notifyItemChanged(i);
//            }
//            // Update the item in the dataset
//        }
//    }
    public void refreshParent(int position) {
        Log.d("refreshParent", "invoke refreshParent  position " + position);
        notifyItemChanged(position);
//        try {
//
//        }

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
                    replyItem.setProgress(0);
                    item.getReplies().set(j, replyItem);
//                    if (replyItem.isPlaying()) {
//                        Log.d("changePlayingStatus", "reply playing on pos " + j + " parent position " + i);
//
////                        item.setPlaying(false);
////                        item.setReplyPlaying(false);
////                        item.getReplies().get(j).setPlaying(false);
////                        mDataSet.set(i, item);
////                        notifyItemChanged(i);
//                    }
                }
                mDataSet.set(i, item);

                notifyItemChanged(i);

//                for (int j = 0; j < item.getReplies().size(); j++) {
//                    com.uyscut.network.api.response.commentreply.allreplies.Comment replyItem = item.getReplies().get(j);
//                    Log.d("ChangedPlayingStatus", "is reply playing " + replyItem.isPlaying() + " position " + j);
//                    if (replyItem.isPlaying()) {
//                        Log.d("changePlayingStatus", "reply playing on pos " + j + " parent position " + i);
//
////                        item.setPlaying(false);
////                        item.setReplyPlaying(false);
////                        item.getReplies().get(j).setPlaying(false);
////                        mDataSet.set(i, item);
////                        notifyItemChanged(i);
//                    }
//                }
//                item.setPlaying(false);
//                item.setReplyPlaying(false);
//                mDataSet.set(i, item);
//                notifyItemChanged(i);
            }
            else if (item.isPlaying()) { // Assuming there's a method to check if the item is playing
                // If an item with isPlaying true is found, update its position and set it to false
                Log.i("changePlayingStatus", "(else)changePlayingStatus isPlaying" + item.isPlaying() + " position " + i);
                Log.i("changePlayingStatus", "(else)changePlayingStatus isReplyPlaying" + item.isReplyPlaying() + " position " + i);


                item.setPlaying(false);

                String fileType = item.getFileType();
                Log.d("changePlayingStatus","fileType: "+fileType+ " progress "+item.getProgress());
//                if(fileType)
//                item.setReplyPlaying(false);
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

    public interface LoadMoreListener {
        void onLoadMore(int pageNumber);
    }
}