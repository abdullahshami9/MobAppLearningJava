package com.example.firstapp301124;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class PersonalProfileHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private GridAdapter gridAdapter;
    private List<Note> dataList;
    private DrawerLayout drawerLayout;
    private RaabtaaDBHelper dbHelper;
    private List<Note> notesList = new ArrayList<>();
    private int currentUserId = 1; // Replace with the actual user ID

    private static final String PREFS_NAME = "NotesAppPrefs";
    private static final String SELECTED_OS = "selected_os";

    // Language selection listener interface
    private interface OnLanguageSelectedListener {
        void onLanguageSelected(int position);
    }

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
        EditText contentInput = dialogView.findViewById(R.id.contentInput);
        ImageView saveIcon = dialogView.findViewById(R.id.saveIcon);
        TextView languageIndicator = dialogView.findViewById(R.id.languageIndicator);
        TextView lineNumbers = dialogView.findViewById(R.id.lineNumbers);
        View statusIndicator = dialogView.findViewById(R.id.statusIndicator);

        // Set up language selection
        String[] languages = new String[]{".txt", ".php", ".java", ".py", ".js", ".html", ".css", ".xml", ".json", ".md"};
        final int[] selectedLanguage = {0}; // Default to .txt

        // Setup line numbers
        updateLineNumbers(contentInput, lineNumbers);
        
        // Add text change listener for line numbers and status indicator
        contentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLineNumbers(contentInput, lineNumbers);
                updateStatusIndicator(statusIndicator, s.toString(), languages[selectedLanguage[0]]);
                
                try {
                    if (s.length() > 0 && start < s.length()) {
                        char currentChar = s.charAt(start);
                        if (currentChar != '\n' && Character.isLetterOrDigit(currentChar)) {
                            showCodeSnippets(contentInput, languages[selectedLanguage[0]], s.toString(), start);
                        }
                    }
                } catch (Exception e) {
                    // Handle string index errors
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLineNumbers(contentInput, lineNumbers);
            }
        });

        // Add scroll listener to keep line numbers synchronized
        contentInput.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            lineNumbers.scrollTo(0, scrollY);
        });

        languageIndicator.setOnClickListener(v -> showLanguageSelector(languageIndicator, selectedLanguage));

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create();

        saveIcon.setOnClickListener(v -> {
            String title = inputText.getText().toString();
            String content = contentInput.getText().toString();
            if (!title.isEmpty()) {
                // Add the note to the database with the selected language extension
                String finalTitle = title + languages[selectedLanguage[0]];
                dbHelper.addNote(currentUserId, finalTitle, content, 1, 0);

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

    private void updateLineNumbers(EditText contentInput, TextView lineNumbers) {
        try {
            String text = contentInput.getText().toString();
            int lineCount = text.isEmpty() ? 1 : text.split("\n").length;
            StringBuilder numbers = new StringBuilder();
            for (int i = 1; i <= lineCount; i++) {
                numbers.append(String.format("%3d\n", i));
            }
            // Add extra padding at the bottom to ensure alignment
            numbers.append("\n".repeat(3));
            lineNumbers.setText(numbers.toString());
            
            // Sync scroll positions
            int scrollY = contentInput.getScrollY();
            lineNumbers.scrollTo(0, scrollY);
            
            // Match the line height
            lineNumbers.setLineSpacing(contentInput.getLineSpacingExtra(), contentInput.getLineSpacingMultiplier());
            
            // Set the same text size
            lineNumbers.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, contentInput.getTextSize());
            
            // Match the height
            ViewGroup.LayoutParams params = lineNumbers.getLayoutParams();
            params.height = contentInput.getHeight();
            lineNumbers.setLayoutParams(params);
        } catch (Exception e) {
            lineNumbers.setText(" 1\n");
        }
    }

    private void updateStatusIndicator(View statusIndicator, String content, String extension) {
        int color;
        boolean hasSyntaxError = checkSyntaxError(content, extension);
        boolean hasFatalError = checkFatalError(content, extension);
        
        if (hasFatalError) {
            color = Color.BLUE;
        } else if (hasSyntaxError) {
            color = Color.RED;
        } else {
            color = Color.parseColor("#4CAF50"); // Green
        }
        
        statusIndicator.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private boolean checkSyntaxError(String content, String extension) {
        // Basic syntax error checking based on file type
        switch (extension.toLowerCase()) {
            case ".java":
                return !content.isEmpty() && 
                       (content.contains(";{") || 
                        content.contains("}}") ||
                        !checkBracketBalance(content));
            case ".py":
                return content.contains("def def") || 
                       content.contains("class class") ||
                       content.contains("import import");
            case ".js":
                return !content.isEmpty() && 
                       (content.contains(";;") ||
                        !checkBracketBalance(content));
            default:
                return false;
        }
    }

    private boolean checkFatalError(String content, String extension) {
        // Check for potential fatal errors
        switch (extension.toLowerCase()) {
            case ".java":
                return content.contains("System.exit(-1)") ||
                       content.contains("throw new RuntimeException");
            case ".py":
                return content.contains("sys.exit(-1)") ||
                       content.contains("raise Exception");
            case ".js":
                return content.contains("process.exit(-1)") ||
                       content.contains("throw new Error");
            default:
                return false;
        }
    }

    private boolean checkBracketBalance(String content) {
        Stack<Character> stack = new Stack<>();
        for (char c : content.toCharArray()) {
            if (c == '(' || c == '{' || c == '[') {
                stack.push(c);
            } else if (c == ')' || c == '}' || c == ']') {
                if (stack.isEmpty()) return false;
                char last = stack.pop();
                if (!isMatchingBracket(last, c)) return false;
            }
        }
        return stack.isEmpty();
    }

    private boolean isMatchingBracket(char open, char close) {
        return (open == '(' && close == ')') ||
               (open == '{' && close == '}') ||
               (open == '[' && close == ']');
    }

    private void showCodeSnippets(EditText contentInput, String extension, String text, int position) {
        String currentWord = getCurrentWord(text, position);
        if (currentWord.isEmpty()) return;

        String[] suggestions = getCodeSnippets(extension, currentWord);
        if (suggestions.length == 0) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Suggestions")
               .setItems(suggestions, (dialog, which) -> {
                   insertCodeSnippet(contentInput, suggestions[which], currentWord, position);
               });
        builder.create().show();
    }

    private String getCurrentWord(String text, int position) {
        if (position <= 0) return "";
        int start = position - 1;
        while (start >= 0 && Character.isLetterOrDigit(text.charAt(start))) {
            start--;
        }
        return text.substring(start + 1, position);
    }

    private void insertCodeSnippet(EditText contentInput, String snippet, String currentWord, int position) {
        Editable editable = contentInput.getText();
        int start = position - currentWord.length();
        editable.replace(start, position, snippet);
    }

    private String[] getCodeSnippets(String extension, String prefix) {
        switch (extension.toLowerCase()) {
            case ".php":
                return filterSnippets(prefix, new String[]{"echo", "print", "foreach", "while", "if", "else", "function", "class", "public", "private", "protected"});
            case ".java":
                return filterSnippets(prefix, new String[]{"public", "private", "class", "interface", "extends", "implements", "void", "return", "static", "final", "System.out.println"});
            case ".py":
                return filterSnippets(prefix, new String[]{"print", "def", "class", "for", "while", "if", "elif", "else", "import", "from", "return"});
            case ".js":
                return filterSnippets(prefix, new String[]{"function", "const", "let", "var", "console.log", "return", "if", "else", "for", "while", "class"});
            default:
                return new String[0];
        }
    }

    private String[] filterSnippets(String prefix, String[] snippets) {
        List<String> filtered = new ArrayList<>();
        for (String snippet : snippets) {
            if (snippet.toLowerCase().startsWith(prefix.toLowerCase())) {
                filtered.add(snippet);
            }
        }
        return filtered.toArray(new String[0]);
    }

    private void showEditModal(int position) {
        // Get the note for this position
        Note note = notesList.get(position);

        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_material, null);
        EditText inputText = dialogView.findViewById(R.id.inputText);
        EditText contentInput = dialogView.findViewById(R.id.contentInput);
        ImageView saveIcon = dialogView.findViewById(R.id.saveIcon);
        TextView languageIndicator = dialogView.findViewById(R.id.languageIndicator);
        TextView lineNumbers = dialogView.findViewById(R.id.lineNumbers);
        View statusIndicator = dialogView.findViewById(R.id.statusIndicator);

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
        inputText.setText(titleHolder[0]);
        contentInput.setText(note.getContent());
        languageIndicator.setText(languages[selectedLanguage[0]]);

        // Setup line numbers and ensure they're visible
        contentInput.post(() -> {
            updateLineNumbers(contentInput, lineNumbers);
            // Match text appearance
            lineNumbers.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, contentInput.getTextSize());
            lineNumbers.setLineSpacing(contentInput.getLineSpacingExtra(), contentInput.getLineSpacingMultiplier());
            // Match height
            ViewGroup.LayoutParams params = lineNumbers.getLayoutParams();
            params.height = contentInput.getHeight();
            lineNumbers.setLayoutParams(params);
        });

        // Update status indicator for initial content
        updateStatusIndicator(statusIndicator, note.getContent(), languages[selectedLanguage[0]]);

        // Add text change listener for line numbers and status indicator
        contentInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateLineNumbers(contentInput, lineNumbers);
                updateStatusIndicator(statusIndicator, s.toString(), languages[selectedLanguage[0]]);
                
                try {
                    if (s.length() > 0 && start < s.length()) {
                        char currentChar = s.charAt(start);
                        if (currentChar != '\n' && Character.isLetterOrDigit(currentChar)) {
                            showCodeSnippets(contentInput, languages[selectedLanguage[0]], s.toString(), start);
                        }
                    }
                } catch (Exception e) {
                    // Handle string index errors
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLineNumbers(contentInput, lineNumbers);
            }
        });

        // Add scroll listener to keep line numbers synchronized
        contentInput.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            lineNumbers.scrollTo(0, scrollY);
        });

        languageIndicator.setOnClickListener(v -> showLanguageSelector(languageIndicator, selectedLanguage));

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create();

        // Set click listener for save button
        saveIcon.setOnClickListener(v -> {
            String updatedTitle = inputText.getText().toString();
            String updatedContent = contentInput.getText().toString();
            if (!updatedTitle.isEmpty()) {
                // Update the note in the database with the selected language extension
                dbHelper.updateNote(note.getId(), updatedTitle + languages[selectedLanguage[0]], updatedContent, note.getColorId(), note.getIsPinned());

                // Refresh the notes list
                loadNotes();

                dialog.dismiss();
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
        } else if (item.getItemId() == R.id.nav_settings) {
            showOSSettingsDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showOSSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.os_settings_dialog, null);
        builder.setView(view);

        RadioGroup osRadioGroup = view.findViewById(R.id.osRadioGroup);
        RadioButton windowsRadio = view.findViewById(R.id.windowsRadio);
        RadioButton macRadio = view.findViewById(R.id.macRadio);
        RadioButton linuxRadio = view.findViewById(R.id.linuxRadio);

        // Load saved preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedOS = prefs.getString(SELECTED_OS, "Windows");
        
        switch (savedOS) {
            case "Windows":
                windowsRadio.setChecked(true);
                break;
            case "macOS":
                macRadio.setChecked(true);
                break;
            case "Linux":
                linuxRadio.setChecked(true);
                break;
        }

        AlertDialog dialog = builder.create();
        dialog.show();

        osRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedOS;
            if (checkedId == R.id.windowsRadio) {
                selectedOS = "Windows";
            } else if (checkedId == R.id.macRadio) {
                selectedOS = "macOS";
            } else {
                selectedOS = "Linux";
            }
            
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(SELECTED_OS, selectedOS);
            editor.apply();
            
            dialog.dismiss();
        });
    }

    private void showLanguageSelector(TextView languageIndicator, final int[] selectedLanguage) {
        String[] languages = new String[]{".txt", ".php", ".java", ".py", ".js", ".html", ".css", ".xml", ".json", ".md"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogStyle);
        builder.setTitle("Select Language")
               .setSingleChoiceItems(languages, selectedLanguage[0], (dialog, which) -> {
                   selectedLanguage[0] = which;
                   languageIndicator.setText(languages[which]);
                   dialog.dismiss();
               });
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }
}
