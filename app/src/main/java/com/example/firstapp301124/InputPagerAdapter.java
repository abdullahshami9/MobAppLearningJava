package com.example.firstapp301124;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InputPagerAdapter extends RecyclerView.Adapter<InputPagerAdapter.InputViewHolder> {

    private final List<Integer> layouts;

    public InputPagerAdapter(List<Integer> layouts) {
        this.layouts = layouts;
    }

    @NonNull
    @Override
    public InputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layouts.get(viewType), parent, false);
        return new InputViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InputViewHolder holder, int position) {
        // No binding needed for static layouts.
    }

    @Override
    public int getItemCount() {
        return layouts.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class InputViewHolder extends RecyclerView.ViewHolder {
        public InputViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
