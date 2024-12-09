package com.example.firstapp301124;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class InputPagerAdapter extends RecyclerView.Adapter<InputPagerAdapter.InputViewHolder> {

    private final List<View> layouts;

    public InputPagerAdapter(List<View> layouts) {
        this.layouts = layouts;
    }

    @NonNull
    @Override
    public InputViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InputViewHolder(layouts.get(viewType));
    }

    @Override
    public void onBindViewHolder(@NonNull InputViewHolder holder, int position) {
        // Binding is not necessary in this case, views are pre-inflated
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
