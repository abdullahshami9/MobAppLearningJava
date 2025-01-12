package com.example.firstapp301124;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private List<String> dataList;
    private final OnItemClickListener onItemClickListener;

    public GridAdapter(List<String> dataList, OnItemClickListener onItemClickListener) {
        this.dataList = dataList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(dataList.get(position));
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Add updateData() method
    public void updateData(List<String> newDataList) {
        this.dataList = newDataList;
        notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
