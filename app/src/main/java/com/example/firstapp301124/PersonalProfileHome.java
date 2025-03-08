package com.example.firstapp301124;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonalProfileHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private GridAdapter gridAdapter;
    private List<Note> dataList;
    private DrawerLayout drawerLayout;
    private RaabtaaDBHelper dbHelper;
    private List<Note> notesList = new ArrayList<>();
    private int currentUserId = 1; // Replace with the actual user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this); // Apply theme before setContentView
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_profile_home);

        // Set status and navigation bar colors based on theme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create color with 75% opacity (0xBF = 191 in decimal, which is ~75% of 255)
//            int statusBarColor = ThemeHelper.isDarkTheme(this) ?
//                0xBF000000 : // Black with 75% opacity
//                0xBFFFFFFF; // White with 75% opacity
//
//            getWindow().setStatusBarColor(statusBarColor);
//
//            // Keep navigation bar solid
//            getWindow().setNavigationBarColor(ThemeHelper.isDarkTheme(this) ?
//                Color.BLACK :
//                Color.WHITE);
//
//            // Set system UI flags
//            if (!ThemeHelper.isDarkTheme(this)) {
//                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//            } else {

            if (ThemeHelper.isDarkTheme(this)) {
                // Dark theme - set both bars to black
                getWindow().setStatusBarColor(Color.BLACK);
                getWindow().setNavigationBarColor(Color.BLACK);
                getWindow().getDecorView().setSystemUiVisibility(0); // Clear light status bar flag
            } else {
                // Light theme - set both bars to white
                getWindow().setStatusBarColor(Color.WHITE);
                getWindow().setNavigationBarColor(Color.WHITE);
                getWindow().getDecorView().setSystemUiVisibility(0);
            }
        }

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        EditText searchBar = toolbar.findViewById(R.id.searchBar);
        ImageView menuIcon = toolbar.findViewById(R.id.menuIcon);
        ImageView profileIcon = toolbar.findViewById(R.id.profileIcon);

        // Set the background color based on the current theme
        if (ThemeHelper.isDarkTheme(this)) {
            navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_background_dark));
        } else {
            navigationView.setBackgroundColor(ContextCompat.getColor(this, R.color.drawer_background_light));
        }

        // Setup menu icon to toggle drawer
        menuIcon.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });

        // Add a DrawerListener to ensure the status bar stays visible
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {}

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //     getWindow().setStatusBarColor(Color.WHITE);
                //     getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                // }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //     getWindow().setStatusBarColor(Color.WHITE);
                //     getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                // }
            }

            @Override
            public void onDrawerStateChanged(int newState) {}
        });

        // Setup navigation menu item click
        navigationView.setNavigationItemSelectedListener(this);

        // Setup profile icon click
        profileIcon.setOnClickListener(v -> {
            Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
        });

        // Setup search functionality
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                // Implement search logic (filter RecyclerView or other content)
                Toast.makeText(PersonalProfileHome.this, "Searching: " + query, Toast.LENGTH_SHORT).show();
                filterRecyclerView(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initialize RecyclerView and FAB
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Initialize data for grid
        dataList = new ArrayList<>();

        // Set up RecyclerView with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);

        // Set up adapter
        gridAdapter = new GridAdapter(dataList, PersonalProfileHome.this::showEditModal);
        recyclerView.setAdapter(gridAdapter);

        // Handle Floating Action Button click
        fabAdd.setOnClickListener(v -> openAddModal());

        // Handle drag-and-drop functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END,
                0) {
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
                // No action needed
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Initialize the database helper
        dbHelper = new RaabtaaDBHelper(this);
        currentUserId = getIntent().getIntExtra("userId", 1);
        loadNotes();

        // Handle incoming intent
        handleIncomingIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    private void handleIncomingIntent(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            String type = intent.getType();

            if (Intent.ACTION_VIEW.equals(action) && type != null) {
                Uri fileUri = intent.getData();
                if (fileUri != null) {
                    try {
                        // Get file name and content
                        String fileName = getFileName(fileUri);
                        String content = readFileContent(fileUri);
                        
                        // Determine file extension
                        String extension = getFileExtension(fileName);
                        
                        // Create new note with file content
                        createNoteFromFile(fileName, content, extension);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private String getFileExtension(String fileName) {
        String[] supportedExtensions = {".txt", ".php", ".java", ".py", ".js", ".html", ".css", ".xml", ".json", ".md"};
        String lowercaseFileName = fileName.toLowerCase();
        
        for (String ext : supportedExtensions) {
            if (lowercaseFileName.endsWith(ext)) {
                return ext;
            }
        }
        return ".txt"; // Default to .txt if no matching extension found
    }

    private String readFileContent(Uri uri) throws Exception {
        StringBuilder content = new StringBuilder();
        try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
             java.io.BufferedReader reader = new java.io.BufferedReader(
                     new java.io.InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private void createNoteFromFile(String fileName, String content, String extension) {
        // Remove extension from filename if present
        String title = fileName;
        for (String ext : new String[]{".txt", ".php", ".java", ".py", ".js", ".html", ".css", ".xml", ".json", ".md"}) {
            if (title.toLowerCase().endsWith(ext)) {
                title = title.substring(0, title.length() - ext.length());
                break;
            }
        }

        // Add note to database with the file content
        dbHelper.addNote(currentUserId, title + extension, content, 1, 0);
        
        // Open the note for editing immediately
        int newNotePosition = 0; // The new note will be at the top after loadNotes()
        loadNotes();
        showEditModal(newNotePosition);
        
        Toast.makeText(this, "File opened successfully", Toast.LENGTH_SHORT).show();
    }

    private void filterRecyclerView(String query) {
        List<Note> filteredList = new ArrayList<>();
        for (Note note : dataList) {
            if (note.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(note);
            }
        }
        gridAdapter.updateData(filteredList);
    }

    private void openAddModal() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_material, null);
        EditText inputText = dialogView.findViewById(R.id.inputText);
        ImageView saveIcon = dialogView.findViewById(R.id.saveIcon);
        TextView languageIndicator = dialogView.findViewById(R.id.languageIndicator);

        // Set up language selection
        String[] languages = new String[]{".txt", ".php", ".java", ".py", ".js", ".html", ".css", ".xml", ".json", ".md"};
        final int[] selectedLanguage = {0}; // Default to .txt

        languageIndicator.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Language")
                   .setItems(languages, (dialog, which) -> {
                       selectedLanguage[0] = which;
                       languageIndicator.setText(languages[which]);
                   });
            builder.create().show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create();

        saveIcon.setOnClickListener(v -> {
            String title = inputText.getText().toString();
            if (!title.isEmpty()) {
                // Add the note to the database with the selected language extension
                String finalTitle = title + languages[selectedLanguage[0]];
                dbHelper.addNote(currentUserId, finalTitle, "", 1, 0);

                // Refresh the notes list
                loadNotes();

                dialog.dismiss();
                Toast.makeText(this, "Note added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showEditModal(int position) {
        // Get the note for this position
        Note note = notesList.get(position);

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_material, null);
        EditText inputText = dialogView.findViewById(R.id.inputText);
        ImageView saveIcon = dialogView.findViewById(R.id.saveIcon);
        TextView languageIndicator = dialogView.findViewById(R.id.languageIndicator);

        // Set up language selection
        String[] languages = new String[]{".txt", ".php", ".java", ".py", ".js", ".html", ".css", ".xml", ".json", ".md"};
        final int[] selectedLanguage = {0}; // Default to .txt
        final String[] titleHolder = {note.getTitle()}; // Store title in array to make it effectively final

        // Extract current extension and title
        for (String ext : languages) {
            if (titleHolder[0].toLowerCase().endsWith(ext.toLowerCase())) {
                titleHolder[0] = titleHolder[0].substring(0, titleHolder[0].length() - ext.length());
                // Find the index of the extension in languages array
                for (int i = 0; i < languages.length; i++) {
                    if (languages[i].equals(ext)) {
                        selectedLanguage[0] = i;
                        break;
                    }
                }
                break;
            }
        }

        // Set the content and language indicator
        inputText.setText(note.getContent()); // Show content for editing instead of title
        languageIndicator.setText(languages[selectedLanguage[0]]);

        languageIndicator.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Select Language")
                   .setItems(languages, (dialog, which) -> {
                       selectedLanguage[0] = which;
                       languageIndicator.setText(languages[which]);
                   });
            builder.create().show();
        });

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create();

        // Set click listener for save button
        saveIcon.setOnClickListener(v -> {
            String updatedContent = inputText.getText().toString();
            if (!updatedContent.isEmpty()) {
                // Update the note in the database with the selected language extension
                dbHelper.updateNote(note.getId(), titleHolder[0] + languages[selectedLanguage[0]], updatedContent, note.getColorId(), note.getIsPinned());

                // Refresh the notes list
                loadNotes();

                dialog.dismiss();
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        dialog.show();
    }

    private void loadNotes() {
        // Fetch notes from the database
        notesList = dbHelper.getNotesByUser(currentUserId);

        // Update the dataList
        dataList.clear();
        dataList.addAll(notesList);

        // Update the RecyclerView adapter
        gridAdapter.updateData(dataList);
    }

    private void deleteNote(int position) {
        Note note = dataList.get(position);
        dbHelper.deleteNote(note.getId());
        loadNotes(); // Refresh the list
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_item1) {
            Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_item2) {
            Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_item3) {
            Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_dark_mode) {
            ThemeHelper.toggleTheme(this); // Toggle dark mode
            recreate(); // Restart activity to apply theme
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
