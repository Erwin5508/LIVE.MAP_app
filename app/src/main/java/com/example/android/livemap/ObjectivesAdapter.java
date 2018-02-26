package com.example.android.livemap;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by ErwinF on 2/24/2018.
 */

public class ObjectivesAdapter extends RecyclerView.Adapter<ObjectivesAdapter.ObjectivesViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    private String _id;

    public ObjectivesAdapter(Context context, Cursor cursor, OnClickHandler clickHandler) {
        this.mContext = context;
        this.mCursor = cursor;
        mClickHandler = clickHandler;
    }

    public interface OnClickHandler {
        void onClick(String _id, int VIEW_ID, View view);
    }

    private final OnClickHandler mClickHandler;

    @Override
    public ObjectivesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ObjectivesViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ObjectivesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ObjectivesViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
