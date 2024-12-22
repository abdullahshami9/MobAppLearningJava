package com.example.firstapp301124;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonalProfileHome extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GridAdapter gridAdapter;
    private List<String> dataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge rendering
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_profile_home);

        // Set up edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView and FAB
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Initialize data for grid
        dataList = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            dataList.add("Item " + i);
        }

        // Set up RecyclerView with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Set up adapter
        gridAdapter = new GridAdapter(dataList, this::openEditModal);
        recyclerView.setAdapter(gridAdapter);

        // Handle Floating Action Button click
        fabAdd.setOnClickListener(v -> openAddModal());

        // Handle drag-and-drop functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                // Enable drag in all directions, disable swipe
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(dataList, fromPosition, toPosition);
                gridAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // No swipe action; this is intentionally left blank
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void openAddModal() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_material, null);
        EditText inputText = dialogView.findViewById(R.id.inputText);
        ImageView saveIcon = dialogView.findViewById(R.id.saveIcon);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create();

        saveIcon.setOnClickListener(v -> {
            String newItem = inputText.getText().toString();
            if (!newItem.isEmpty()) {
                dataList.add(newItem);
                gridAdapter.notifyItemInserted(dataList.size() - 1);
                recyclerView.smoothScrollToPosition(dataList.size() - 1);
                dialog.dismiss();
                Toast.makeText(this, "New item added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void openEditModal(int position) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_material, null);
        EditText inputText = dialogView.findViewById(R.id.inputText);
        ImageView saveIcon = dialogView.findViewById(R.id.saveIcon);

        inputText.setText(dataList.get(position)); // Pre-fill current item text

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create();

        saveIcon.setOnClickListener(v -> {
            String updatedText = inputText.getText().toString();
            if (!updatedText.isEmpty()) {
                dataList.set(position, updatedText);
                gridAdapter.notifyItemChanged(position);
                dialog.dismiss();
                Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Text cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

}
