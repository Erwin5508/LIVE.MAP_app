package com.example.android.livemap;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_DESCRIPTION;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_TITLE;

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
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.objective_card, parent, false);
        return new ObjectivesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ObjectivesViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position))
            return;

        String title = mCursor.getString(mCursor.getColumnIndex(OBJECTIVES_TITLE));
        String description = mCursor.getString(mCursor.getColumnIndex(OBJECTIVES_DESCRIPTION));

        holder.vTitle.setText(title);
        holder.vDescription.setText(description);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public class ObjectivesViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView vTitle;
        TextView vDescription;
        Button vDelete;
        ViewGroup vFrame;

        public ObjectivesViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.objective_title_text);
            vDescription = (TextView) itemView.findViewById(R.id.objective_description_text);
            vDelete = (Button) itemView.findViewById(R.id.recycler_view_delete_button);
            vFrame = (ViewGroup) itemView.findViewById(R.id.recycler_view_item_frame);

            vDescription.setVisibility(View.GONE);

            vFrame.setOnClickListener(this);
            vDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == vFrame.getId()) {
                if (vDescription.getVisibility() == View.GONE) {
                    vDescription.setVisibility(View.VISIBLE);
                } else {
                    vDescription.setVisibility(View.GONE);
                }
            }
        }
    }
}
