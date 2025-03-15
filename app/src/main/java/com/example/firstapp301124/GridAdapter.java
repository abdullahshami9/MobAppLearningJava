package com.example.firstapp301124;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class GridAdapter extends RecyclerView.Adapter<GridAdapter.ViewHolder> {

    private List<Note> notes;
    private final OnItemClickListener listener;
    private List<Note> previousState; // For storing state when navigating into folders
    private OnBackButtonClickListener backButtonListener;

    public GridAdapter(List<Note> notes, OnItemClickListener listener) {
        this.notes = notes;
        this.listener = listener;
        this.previousState = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = notes.get(position);
        String title = note.getTitle();
        Context context = holder.itemView.getContext();
        
        // Check if this is the back button
        if (note.getType() == 3) {  // Back button
            holder.icon.setImageResource(R.drawable.ic_arrow_back);
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.back_button_color));
            holder.title.setText(title);
            holder.itemView.setOnClickListener(v -> {
                if (backButtonListener != null) {
                    backButtonListener.onBackButtonClick();
                }
            });
            return;
        }
        
        // Check if this is a folder or a note
        if (note.getType() == 2) {  // Folder
            // For folders, show the folder icon
            holder.icon.setImageResource(R.drawable.ic_folder);
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.folder_color));
            
            // No need to remove extension for folders
            holder.title.setText(title);
        } else {  // Note (type 1)
            // For notes, show the file icon based on the extension
            holder.icon.setImageResource(getFileIconResource(title));
            holder.icon.setColorFilter(ContextCompat.getColor(context, R.color.file_color));
            
            // Remove extension from display
            String[] extensions = {".txt", ".php", ".java", ".py", ".js", ".html", ".css", ".xml", ".json", ".md"};
            for (String ext : extensions) {
                if (title.toLowerCase().endsWith(ext.toLowerCase())) {
                    title = title.substring(0, title.length() - ext.length());
                    break;
                }
            }
            
            holder.title.setText(title);
        }
        
        // Set click listener for notes and folders (excluding back button which is handled above)
        final int pos = position;
        holder.itemView.setOnClickListener(v -> listener.onItemClick(pos));
    }
    
    // Helper method to determine the icon resource based on file extension
    private int getFileIconResource(String fileName) {
        String lowerCase = fileName.toLowerCase();
        
        if (lowerCase.endsWith(".txt")) {
            return R.drawable.ic_text;
        } else if (lowerCase.endsWith(".php") || lowerCase.endsWith(".java") || 
                  lowerCase.endsWith(".py") || lowerCase.endsWith(".js")) {
            return R.drawable.ic_code;
        } else if (lowerCase.endsWith(".html") || lowerCase.endsWith(".xml")) {
            return R.drawable.ic_html;
        } else if (lowerCase.endsWith(".css")) {
            return R.drawable.ic_css;
        } else if (lowerCase.endsWith(".json")) {
            return R.drawable.ic_json;
        } else if (lowerCase.endsWith(".md")) {
            return R.drawable.ic_markdown;
        } else {
            return R.drawable.ic_file;
        }
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public void updateData(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }
    
    /**
     * Sets the previous state to be restored when going back from a folder
     */
    public void setPreviousState(List<Note> previousState) {
        this.previousState = new ArrayList<>(previousState);
    }
    
    /**
     * Gets the previous state
     */
    public List<Note> getPreviousState() {
        return previousState;
    }
    
    /**
     * Sets the back button click listener
     */
    public void setOnBackButtonClickListener(OnBackButtonClickListener listener) {
        this.backButtonListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.itemTitle);
            icon = itemView.findViewById(R.id.itemIcon);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
    
    public interface OnBackButtonClickListener {
        void onBackButtonClick();
    }
}
