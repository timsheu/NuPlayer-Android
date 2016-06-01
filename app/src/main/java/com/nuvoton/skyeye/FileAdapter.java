package com.nuvoton.skyeye;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by timsheu on 4/25/16.
 */
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder>{
    private ArrayList<FileContent> mDataset = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewFileName;
        public TextView textViewFileDate;
        public ViewHolder(View v) {
            super(v);
            textViewFileName = (TextView) v.findViewById(R.id.file_name);
            textViewFileDate = (TextView) v.findViewById(R.id.file_date);
        }
    }

    public FileAdapter(ArrayList<FileContent> dataset) {
        mDataset.clear();
        mDataset.addAll(dataset);
    }

    @Override
    public FileAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_live_page, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileContent content = mDataset.get(position);
        holder.textViewFileName.setText(content.fileName);
        holder.textViewFileDate.setText(content.fileDate);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
