package com.example.android.livemap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_DESCRIPTION;
import static com.example.android.livemap.Database.ObjectivesContract.ObjectivesEntry.OBJECTIVES_TITLE;

/**
 * Created by ErwinF on 2/24/2018.
 */

public class ObjectivesAdapter extends RecyclerView.Adapter<ObjectivesAdapter.ObjectivesViewHolder> {

    private Context mContext;
    private Cursor mCursor;


    int newPosition;
    boolean newObjective = false;
    boolean deleteObjectives = false;

    private String _id;

    public ObjectivesAdapter(Context context, Cursor cursor, OnClickHandler clickHandler) {
        this.mContext = context;
        this.mCursor = cursor;
        mClickHandler = clickHandler;
    }

    public interface OnClickHandler {
        void onClick(int VIEW_ID, ContentValues cv, int position);
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

        if (position == newPosition && newObjective) {
            switchToEditMode(true, holder);
        } else {
            if (!mCursor.moveToPosition(position))
                return;
            switchToEditMode(false, holder);
            String title = mCursor.getString(mCursor.getColumnIndex(OBJECTIVES_TITLE));
            String description = mCursor.getString(mCursor.getColumnIndex(OBJECTIVES_DESCRIPTION));

            holder.vTitle.setText(title);
            holder.vDescription.setText(description);
            switchToDeleteMode(deleteObjectives, holder);
        }
    }

    private void switchToEditMode(boolean edit, ObjectivesViewHolder holder) {
        if (edit) {
            holder.vTitle.setVisibility(View.GONE);
            holder.vDescription.setVisibility(View.GONE);
            holder.vTitleInsert.setVisibility(View.VISIBLE);
            holder.vDescriptionInsert.setVisibility(View.VISIBLE);
            holder.vAdd.setVisibility(View.VISIBLE);
            holder.vCancel.setVisibility(View.VISIBLE);
        } else {
            holder.vTitle.setVisibility(View.VISIBLE);
            holder.vDescription.setVisibility(View.GONE);
            holder.vTitleInsert.setVisibility(View.GONE);
            holder.vDescriptionInsert.setVisibility(View.GONE);
            holder.vAdd.setVisibility(View.GONE);
            holder.vCancel.setVisibility(View.GONE);
        }
    }

    private void switchToDeleteMode(boolean delete, ObjectivesViewHolder holder) {
        int visibility;
        if (delete) {
            visibility = View.VISIBLE;
        } else {
            visibility = View.GONE;
        }
        holder.vDelete.setVisibility(visibility);
    }

    public void updateCursor(Cursor cursor) {
        this.mCursor = cursor;
    }

    @Override
    public int getItemCount() {
        if (newObjective) {
            return mCursor.getCount() + 1;
        } else {
            return mCursor.getCount();
        }
    }

    public class ObjectivesViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView vTitle;
        TextView vDescription;
        EditText vTitleInsert;
        EditText vDescriptionInsert;
        Button vAdd;
        Button vCancel;
        ImageButton vDelete;
        ViewGroup vFrame;

        public ObjectivesViewHolder(View itemView) {
            super(itemView);
            vTitle = (TextView) itemView.findViewById(R.id.objective_title_text);
            vDescription = (TextView) itemView.findViewById(R.id.objective_description_text);
            vTitleInsert = (EditText) itemView.findViewById(R.id.objective_title);
            vDescriptionInsert = (EditText) itemView.findViewById(R.id.objective_description);
            vAdd = (Button) itemView.findViewById(R.id.add_this_objective_button);
            vCancel = (Button) itemView.findViewById(R.id.cancel_this_objective_button);
            vDelete = (ImageButton) itemView.findViewById(R.id.recycler_view_delete_button);
            vFrame = (ViewGroup) itemView.findViewById(R.id.recycler_view_item_frame);

            vDescription.setVisibility(View.GONE);


            vAdd.setOnClickListener(this);
            vCancel.setOnClickListener(this);

            vFrame.setOnClickListener(this);
            vDelete.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (v.getId() == vFrame.getId() && !newObjective) {
                if (vDescription.getVisibility() == View.GONE) {
                    vDescription.setVisibility(View.VISIBLE);
                } else {
                    vDescription.setVisibility(View.GONE);
                }
            } else if (v.getId() == vAdd.getId()) {
                if (!checkIfDataIsComplete()) {
                    Toast.makeText(mContext, "Sorry, data is incomplete", Toast.LENGTH_SHORT).show();
                    return;
                }
                String title = vTitleInsert.getText().toString();
                String description = vDescriptionInsert.getText().toString();
                vTitleInsert.setText("");
                vDescriptionInsert.setText("");
                ContentValues cv = new ContentValues();
                cv.put(OBJECTIVES_TITLE, title);
                cv.put(OBJECTIVES_DESCRIPTION, description);
                mClickHandler.onClick(v.getId(), cv, -1);

            } else if (v.getId() == vCancel.getId()) {
                deleteObjectives = false;
                vTitleInsert.setText("");
                vDescriptionInsert.setText("");
                mClickHandler.onClick(v.getId(), null, -1);
            } else if (v.getId() == vDelete.getId() && vTitleInsert.getVisibility() != View.VISIBLE) {
                mClickHandler.onClick(v.getId(), null, this.getAdapterPosition());
            }
        }

        private boolean checkIfDataIsComplete() {
            return !(vTitleInsert.getText().equals("") || vDescriptionInsert.equals("") ||
                    vTitleInsert.getText() == null || vDescriptionInsert == null);
        }
    }
}
