package com.nuvoton.nuplayer;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.nuvoton.nuplayer.FileFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FileContent} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyFileRecyclerViewAdapter extends RecyclerView.Adapter<MyFileRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<FileContent> mValues;

    public MyFileRecyclerViewAdapter(ArrayList<FileContent> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_file, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mFilaNameView.setText(mValues.get(position).fileName);
        holder.mFileDateView.setText(mValues.get(position).fileDate);

//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mFilaNameView;
        public final TextView mFileDateView;
        public FileContent mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mFilaNameView = (TextView) view.findViewById(R.id.file_name);
            mFileDateView = (TextView) view.findViewById(R.id.file_date);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mFilaNameView.getText() + "'";
        }
    }
}
