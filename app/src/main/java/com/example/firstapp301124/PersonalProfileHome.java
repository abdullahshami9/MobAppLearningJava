package com.example.firstapp301124;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupMenu;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.text.InputType;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.inputmethod.InputMethodManager;


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
import com.example.firstapp301124.CodeExecutor.ExecutionStatus;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class PersonalProfileHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private GridAdapter gridAdapter;
    private List<Note> dataList;
    private DrawerLayout drawerLayout;
    private RaabtaaDBHelper dbHelper;
    private List<Note> notesList = new ArrayList<>();
    private int currentUserId = 1; // Replace with the actual user ID
    private LinearLayout tagsContainer;

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
        tagsContainer = findViewById(R.id.tagsContainer);

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
        final boolean[] isTerminalVisible = {false}; // Track terminal visibility

        // Initialize views
        View terminalSection = dialogView.findViewById(R.id.terminalSection);
        TextView terminalPrompt = dialogView.findViewById(R.id.terminalPrompt);
        EditText terminalInput = dialogView.findViewById(R.id.terminalInput);
        TextView terminalOutput = dialogView.findViewById(R.id.terminalOutput);

        // Set terminal style based on OS preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String selectedOS = prefs.getString(SELECTED_OS, "Windows");
        
        // Configure terminal appearance
        if (terminalSection != null) {
            terminalSection.setVisibility(View.GONE);
            if (ThemeHelper.isDarkTheme(this)) {
                terminalSection.setBackgroundColor(Color.parseColor("#1E1E1E")); // Dark terminal
                terminalInput.setTextColor(Color.WHITE);
                terminalOutput.setTextColor(Color.parseColor("#CCCCCC"));
            } else {
                terminalSection.setBackgroundColor(Color.parseColor("#F0F0F0")); // Light terminal
                terminalInput.setTextColor(Color.BLACK);
                terminalOutput.setTextColor(Color.parseColor("#666666"));
            }
        }

        // Set terminal prompt based on OS
        if (terminalPrompt != null) {
            switch (selectedOS) {
                case "Windows":
                    terminalPrompt.setText("C:\\Users\\>");
                    break;
                case "macOS":
                    terminalPrompt.setText("user@macbook ~ %");
                    break;
                case "Linux":
                    terminalPrompt.setText("user@linux:~$");
                    break;
            }
        }

        // Setup terminal input handling
        if (terminalInput != null) {
            terminalInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                    String command = terminalInput.getText().toString().trim();
                    executeTerminalCommand(command, selectedOS, terminalOutput);
                    terminalInput.setText("");
                    return true;
                }
                return false;
            });
        }

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
            int lineCount = text.isEmpty() ? 1 : text.split("\n", -1).length;
            StringBuilder numbers = new StringBuilder();
            
            // Add line numbers
            for (int i = 1; i <= lineCount; i++) {
                numbers.append(String.format("%3d\n", i));
            }
            
            // Remove any extra newlines at the end of the content
            while (text.endsWith("\n")) {
                text = text.substring(0, text.length() - 1);
                lineCount--;
            }
            
            lineNumbers.setText(numbers.toString());
            
            // Match text appearance and metrics
            lineNumbers.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, contentInput.getTextSize());
            lineNumbers.setLineSpacing(contentInput.getLineSpacingExtra(), contentInput.getLineSpacingMultiplier());
            lineNumbers.setTypeface(contentInput.getTypeface());
            
            // Ensure the line numbers view has the same height as the content
            ViewGroup.LayoutParams params = lineNumbers.getLayoutParams();
            params.height = contentInput.getHeight();
            lineNumbers.setLayoutParams(params);
            
            // Sync scroll position
            lineNumbers.scrollTo(0, contentInput.getScrollY());
            
        } catch (Exception e) {
            lineNumbers.setText("1\n");
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
        final boolean[] isTerminalVisible = {false}; // Track terminal visibility

        // Initialize terminal views
        View terminalSection = dialogView.findViewById(R.id.terminalSection);
        TextView terminalPrompt = dialogView.findViewById(R.id.terminalPrompt);
        EditText terminalInput = dialogView.findViewById(R.id.terminalInput);
        TextView terminalOutput = dialogView.findViewById(R.id.terminalOutput);

        // Set terminal style based on OS preference
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String selectedOS = prefs.getString(SELECTED_OS, "Windows");
        
        // Configure terminal appearance
        if (terminalSection != null) {
            terminalSection.setVisibility(View.GONE);
            if (ThemeHelper.isDarkTheme(this)) {
                terminalSection.setBackgroundColor(Color.parseColor("#1E1E1E")); // Dark terminal
                terminalInput.setTextColor(Color.WHITE);
                terminalOutput.setTextColor(Color.parseColor("#CCCCCC"));
            } else {
                terminalSection.setBackgroundColor(Color.parseColor("#F0F0F0")); // Light terminal
                terminalInput.setTextColor(Color.BLACK);
                terminalOutput.setTextColor(Color.parseColor("#666666"));
            }
        }

        // Set terminal prompt based on OS
        if (terminalPrompt != null) {
            switch (selectedOS) {
                case "Windows":
                    terminalPrompt.setText("C:\\Users\\>");
                    break;
                case "macOS":
                    terminalPrompt.setText("user@macbook ~ %");
                    break;
                case "Linux":
                    terminalPrompt.setText("user@linux:~$");
                    break;
            }
        }

        // Setup terminal input handling
        if (terminalInput != null) {
            terminalInput.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                    String command = terminalInput.getText().toString().trim();
                    executeTerminalCommand(command, selectedOS, terminalOutput);
                    terminalInput.setText("");
                    return true;
                }
                return false;
            });
        }

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

        // Get the position of the note in the dataList for delete functionality
        final int notePosition = position;
        
        // Set the content and language indicator
        inputText.setText(titleHolder[0]);
        contentInput.setText(note.getContent());
        languageIndicator.setText(languages[selectedLanguage[0]]);

        // Hide the file title in edit mode
        TextView fileTitle = dialogView.findViewById(R.id.fileTitle);
        if (fileTitle != null) {
            fileTitle.setVisibility(View.GONE);
        }

        // Create the dialog first so it can be referenced in click listeners
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogStyle)
                .setView(dialogView)
                .create();

        // Setup more options click listener
        ImageView moreOptionsIcon = dialogView.findViewById(R.id.moreOptionsIcon);
        if (moreOptionsIcon != null) {
            moreOptionsIcon.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(this, moreOptionsIcon);
                
                // Add menu items with icons
                Menu menu = popup.getMenu();
                menu.add(Menu.NONE, 1, Menu.NONE, "Terminal").setIcon(R.drawable.ic_terminal);
                menu.add(Menu.NONE, 2, Menu.NONE, "Run").setIcon(R.drawable.ic_run);
                menu.add(Menu.NONE, 3, Menu.NONE, "Open Directory").setIcon(R.drawable.ic_directory);
                menu.add(Menu.NONE, 4, Menu.NONE, "Chat with AI").setIcon(R.drawable.ic_chat_ai);
                menu.add(Menu.NONE, 5, Menu.NONE, "Delete").setIcon(R.drawable.ic_delete_bin);
                
                // Force showing icons in popup menu (normally not shown by default)
                try {
                    Field field = popup.getClass().getDeclaredField("mPopup");
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 1: // Terminal
                            isTerminalVisible[0] = !isTerminalVisible[0];
                            if (terminalSection != null) {
                                terminalSection.setVisibility(isTerminalVisible[0] ? View.VISIBLE : View.GONE);
                                if (isTerminalVisible[0]) {
                                    terminalSection.setAlpha(0f);
                                    terminalSection.animate()
                                        .alpha(1f)
                                        .setDuration(200)
                                        .start();
                                }
                            }
                            break;
                        case 2: // Run
                            // Show output section if not visible
                            LinearLayout outputSection = dialogView.findViewById(R.id.outputSection);
                            if (outputSection != null) {
                                outputSection.setVisibility(View.VISIBLE);
                                
                                // Get the content and extension
                                String codeToRun = contentInput.getText().toString();
                                String extension = languages[selectedLanguage[0]];
                                
                                // Get the output TextView
                                TextView outputText = dialogView.findViewById(R.id.outputText);
                                if (outputText != null) {
                                    outputText.setText("Running code...");
                                    
                                    // Create CodeExecutor and run the code
                                    CodeExecutor executor = new CodeExecutor(PersonalProfileHome.this);
                                    executor.executeCode(codeToRun, extension, result -> {
                                        if (result.status == CodeExecutor.ExecutionStatus.SUCCESS) {
                                            outputText.setText(result.output);
                                        } else if (result.status == CodeExecutor.ExecutionStatus.ERROR) {
                                            outputText.setText("Error: " + result.error);
                                        }
                                    });
                                }
                            }
                            break;
                        case 3: // Open in Directory
                            // Get the file path
                            String fileName = titleHolder[0] + languages[selectedLanguage[0]];
                            String content = contentInput.getText().toString();
                            
                            // Create an intent to open the file
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file:///"), "resource/folder");
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            } else {
                                Toast.makeText(PersonalProfileHome.this, 
                                    "No application found to open directory", 
                                    Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 4: // Chat with AI code base
                            // Show chat section if not visible
                            LinearLayout chatSection = dialogView.findViewById(R.id.dialogChatPanel);
                            if (chatSection != null) {
                                chatSection.setVisibility(View.VISIBLE);
                                chatSection.setAlpha(0f);
                                chatSection.animate()
                                    .alpha(1f)
                                    .setDuration(200)
                                    .start();
                                
                                // Setup close button for chat panel
                                ImageView closeChat = dialogView.findViewById(R.id.closeChat);
                                if (closeChat != null) {
                                    closeChat.setOnClickListener(closeView -> {
                                        chatSection.animate()
                                            .alpha(0f)
                                            .setDuration(200)
                                            .withEndAction(() -> chatSection.setVisibility(View.GONE))
                                            .start();
                                    });
                                }
                                
                                // Setup send button for chat
                                ImageButton sendButton = dialogView.findViewById(R.id.dialogSendButton);
                                if (sendButton != null) {
                                    sendButton.setOnClickListener(sendView -> {
                                        EditText chatInput = dialogView.findViewById(R.id.dialogChatInput);
                                        if (chatInput != null && !chatInput.getText().toString().trim().isEmpty()) {
                                            // Get the message
                                            String message = chatInput.getText().toString().trim();
                                            
                                            // Display user message (you'd need to implement this method)
                                            addChatMessage(dialogView, message, true);
                                            
                                            // Clear the input
                                            chatInput.setText("");
                                            
                                            // Simulate AI response (for demo)
                                            new Handler().postDelayed(() -> {
                                                addChatMessage(dialogView, "I'm analyzing your code. This appears to be " + 
                                                    languages[selectedLanguage[0]] + " code.", false);
                                            }, 1000);
                                        }
                                    });
                                }
                                
                                // Setup add tag functionality
                                LinearLayout addTagButton = dialogView.findViewById(R.id.addTagButton);
                                if (addTagButton != null) {
                                    addTagButton.setOnClickListener(tagView -> {
                                        showAddTagDialogForChat(dialogView);
                                    });
                                }
                                
                                // Focus on chat input
                                EditText chatInput = dialogView.findViewById(R.id.dialogChatInput);
                                if (chatInput != null) {
                                    chatInput.requestFocus();
                                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.showSoftInput(chatInput, InputMethodManager.SHOW_IMPLICIT);
                                }
                            }
                            break;
                        case 5: // Delete
                            // Show a confirmation dialog in system style
                            new AlertDialog.Builder(PersonalProfileHome.this, android.R.style.Theme_Material_Light_Dialog_Alert)
                                .setTitle("Delete Note")
                                .setMessage("Are you sure you want to delete this note?")
                                .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> {
                                    // Delete the note
                                    deleteNote(notePosition);
                                    
                                    // Dismiss the edit dialog
                                    dialog.dismiss();
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                            break;
                    }
                    return true;
                });
                popup.show();
            });
        }

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
        dialog.show();
    }

    private void executeTerminalCommand(String command, String os, TextView output) {
        // Get the CodeExecutor
        CodeExecutor executor = new CodeExecutor(this);
        
        // Enable local execution if possible
        executor.setLocalExecutionEnabled(true);
        
        // Set working directory to app-specific directory
        String appDir = getFilesDir().getAbsolutePath();
        executor.setCurrentWorkingDirectory(appDir);
        
        // Set basic environment variables
        Map<String, String> env = new HashMap<>();
        env.put("HOME", appDir);
        env.put("PATH", System.getenv("PATH"));
        env.put("TERM", "xterm-256color");
        executor.setEnvironmentVariables(env);
        
        // Show command in output with appropriate prompt
        String prompt = getPromptForOS(os);
        output.append(prompt + " " + command + "\n");
        
        // Execute command with the appropriate OS type
        executor.executeTerminalCommand(command, os, result -> {
            if (!result.error.isEmpty()) {
                // Show error in red
                SpannableString errorText = new SpannableString(result.error);
                errorText.setSpan(new ForegroundColorSpan(Color.RED), 0, result.error.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                output.append(errorText);
            } else {
                // Show output
                output.append(result.output);
            }
            
            // Scroll to bottom
            final int scrollAmount = output.getLayout().getLineTop(output.getLineCount()) - output.getHeight();
            if (scrollAmount > 0) {
                output.scrollTo(0, scrollAmount);
            } else {
                output.scrollTo(0, 0);
            }
            
            // Add prompt for next command
            output.append(prompt + " ");
        });
    }

    private String getPromptForOS(String os) {
        switch (os) {
            case "Windows":
                return "C:\\Users\\> ";
            case "macOS":
                return "user@macbook ~ % ";
            case "Linux":
                return "user@linux:~$ ";
            default:
                return "> ";
        }
    }

    // Add these new methods for error line highlighting
    private void highlightErrorLines(EditText contentInput, List<Integer> errorLines) {
        String content = contentInput.getText().toString();
        android.text.SpannableString spannableString = new android.text.SpannableString(content);
        
        // Find line starts
        int pos = 0;
        int currentLine = 1;
        
        while (pos < content.length()) {
            if (errorLines.contains(currentLine)) {
                // Find line end
                int lineEnd = content.indexOf('\n', pos);
                if (lineEnd == -1) lineEnd = content.length();
                
                // Add red underline to the line
                spannableString.setSpan(
                    new android.text.style.UnderlineSpan(),
                    pos,
                    lineEnd,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                spannableString.setSpan(
                    new android.text.style.ForegroundColorSpan(Color.RED),
                    pos,
                    lineEnd,
                    android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }
            
            // Move to next line
            int nextPos = content.indexOf('\n', pos);
            if (nextPos == -1) break;
            pos = nextPos + 1;
            currentLine++;
        }
        
        contentInput.setText(spannableString);
    }

    private void clearErrorHighlighting(EditText contentInput) {
        String content = contentInput.getText().toString();
        contentInput.setText(content);
    }

    // Add this new method to handle adding custom tags
    private void showAddTagDialog() {
        // Create a custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        
        // Inflate and set the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        builder.setView(dialogView);
        
        // Get the input field
        TextInputEditText tagNameInput = dialogView.findViewById(R.id.tagNameInput);
        
        // Create the dialog
        AlertDialog dialog = builder.create();
        
        // Create stylized button text
        SpannableString positiveText = new SpannableString("ADD");
        positiveText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        positiveText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        SpannableString negativeText = new SpannableString("CANCEL");
        negativeText.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray)), 0, negativeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        // Add tag the buttons
        // ADD -> +
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "ADD", (dialogInterface, which) -> {
            String tagName = tagNameInput.getText().toString().trim();
            if (!tagName.isEmpty()) {
                addTag(tagName);
            }
        });
        
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialogInterface, which) -> {
            // Dialog will be dismissed automatically
        });
        
        // Show the dialog
        dialog.show();
        
        // Style the buttons after dialog is shown
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
        
        // Auto-show keyboard
        tagNameInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(tagNameInput, InputMethodManager.SHOW_IMPLICIT);
    }

    // Helper method to show delete tag confirmation
    private void showDeleteTagDialog(String tagName, View tagView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        
        // Create stylized message
        SpannableString message = new SpannableString("Delete tag '" + tagName + "'?");
        int startPos = message.toString().indexOf(tagName);
        if (startPos >= 0) {
            message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startPos, startPos + tagName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            message.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), startPos, startPos + tagName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        builder.setMessage(message);
        
        // Create stylized button text
        SpannableString positiveText = new SpannableString("DELETE");
        positiveText.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_light)), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        positiveText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        SpannableString negativeText = new SpannableString("CANCEL");
        negativeText.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray)), 0, negativeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        // Add the buttons
        builder.setPositiveButton("DELETE", (dialog, which) -> {
            // Animate tag removal
            tagView.animate()
                    .alpha(0f)
                    .translationX(tagView.getWidth())
                    .setDuration(300)
                    .withEndAction(() -> {
                        tagsContainer.removeView(tagView);
                    })
                    .start();
        });
        
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            // Dialog will be dismissed automatically
        });
        
        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Style the buttons after dialog is shown
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
    }

    // Helper method to add a new tag to the container
    private void addTag(String tagText) {
        if (tagsContainer != null) {
            TextView newTag = new TextView(this);
            newTag.setText(tagText);
            newTag.setTextSize(12);
            newTag.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            newTag.setBackground(getResources().getDrawable(R.drawable.tag_background, getTheme()));
            newTag.setPadding(
                dpToPx(12),  // left
                dpToPx(6),   // top
                dpToPx(12),  // right
                dpToPx(6)    // bottom
            );
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(dpToPx(8));
            newTag.setLayoutParams(params);
            
            // Make the tag clickable
            newTag.setClickable(true);
            newTag.setFocusable(true);
            
            // Click to use as prompt
            newTag.setOnClickListener(view -> {
                EditText dialogChatInput = ((View) tagsContainer.getParent().getParent().getParent()).findViewById(R.id.dialogChatInput);
                if (dialogChatInput != null) {
                    String prompt = "Help me with " + newTag.getText().toString();
                    dialogChatInput.setText(prompt);
                    dialogChatInput.setSelection(prompt.length());
                    dialogChatInput.requestFocus();
                }
            });
            
            // Long press to delete
            newTag.setOnLongClickListener(longClickView -> {
                showDeleteTagDialog(newTag.getText().toString(), newTag);
                return true;
            });
            
            // Remove the add button
            View addButton = null;
            for (int i = 0; i < tagsContainer.getChildCount(); i++) {
                View child = tagsContainer.getChildAt(i);
                if (child.getId() == R.id.addTagButton) {
                    addButton = child;
                    break;
                }
            }
            
            if (addButton != null) {
                tagsContainer.removeView(addButton);
            }
            
            // Add the new tag
            tagsContainer.addView(newTag);
            
            // Add the add button back
            if (addButton != null) {
                tagsContainer.addView(addButton);
            }
            
            // Show success toast
            Toast.makeText(this, "Tag added: " + tagText, Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // Add a method to show add tag dialog for chat panel
    private void showAddTagDialogForChat(View dialogView) {
        // Create a custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        
        // Inflate and set the custom layout
        View tagDialogView = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        builder.setView(tagDialogView);
        
        // Get the input field
        TextInputEditText tagNameInput = tagDialogView.findViewById(R.id.tagNameInput);
        
        // Create the dialog
        AlertDialog dialog = builder.create();
        
        // Create stylized button text
        SpannableString positiveText = new SpannableString("ADD");
        positiveText.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        positiveText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        SpannableString negativeText = new SpannableString("CANCEL");
        negativeText.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray)), 0, negativeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        // Add tag the buttons
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "ADD", (dialogInterface, which) -> {
            String tagName = tagNameInput.getText().toString().trim();
            if (!tagName.isEmpty()) {
                addChatTag(dialogView, tagName);
            }
        });
        
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialogInterface, which) -> {
            // Dialog will be dismissed automatically
        });
        
        // Show the dialog
        dialog.show();
        
        // Style the buttons after dialog is shown
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
        
        // Auto-show keyboard
        tagNameInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(tagNameInput, InputMethodManager.SHOW_IMPLICIT);
    }

    // Add a method to add a tag to the chat panel
    private void addChatTag(View dialogView, String tagText) {
        LinearLayout tagsContainer = dialogView.findViewById(R.id.tagsContainer);
        if (tagsContainer != null) {
            TextView newTag = new TextView(this);
            newTag.setText(tagText);
            newTag.setTextSize(12);
            newTag.setTextColor(getResources().getColor(android.R.color.black, getTheme()));
            newTag.setBackground(getResources().getDrawable(R.drawable.tag_background, getTheme()));
            newTag.setPadding(
                dpToPx(12),  // left
                dpToPx(6),   // top
                dpToPx(12),  // right
                dpToPx(6)    // bottom
            );
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMarginEnd(dpToPx(8));
            newTag.setLayoutParams(params);
            
            // Make the tag clickable
            newTag.setClickable(true);
            newTag.setFocusable(true);
            
            // Click to use as prompt
            newTag.setOnClickListener(view -> {
                EditText dialogChatInput = dialogView.findViewById(R.id.dialogChatInput);
                if (dialogChatInput != null) {
                    String prompt = "Help me with " + newTag.getText().toString();
                    dialogChatInput.setText(prompt);
                    dialogChatInput.setSelection(prompt.length());
                    dialogChatInput.requestFocus();
                }
            });
            
            // Long press to delete
            newTag.setOnLongClickListener(longClickView -> {
                showDeleteTagDialogForChat(newTag.getText().toString(), newTag, tagsContainer);
                return true;
            });
            
            // Get the add button
            View addButton = null;
            for (int i = 0; i < tagsContainer.getChildCount(); i++) {
                View child = tagsContainer.getChildAt(i);
                if (child.getId() == R.id.addTagButton) {
                    addButton = child;
                    break;
                }
            }
            
            if (addButton != null) {
                tagsContainer.removeView(addButton);
            }
            
            // Add the new tag
            tagsContainer.addView(newTag);
            
            // Add the add button back
            if (addButton != null) {
                tagsContainer.addView(addButton);
            }
            
            // Show success toast
            Toast.makeText(this, "Tag added: " + tagText, Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to show delete tag confirmation for chat
    private void showDeleteTagDialogForChat(String tagName, View tagView, LinearLayout tagsContainer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        
        // Create stylized message
        SpannableString message = new SpannableString("Delete tag '" + tagName + "'?");
        int startPos = message.toString().indexOf(tagName);
        if (startPos >= 0) {
            message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startPos, startPos + tagName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            message.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimary)), startPos, startPos + tagName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        builder.setMessage(message);
        
        // Create stylized button text
        SpannableString positiveText = new SpannableString("DELETE");
        positiveText.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.holo_red_light)), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        positiveText.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, positiveText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        SpannableString negativeText = new SpannableString("CANCEL");
        negativeText.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.darker_gray)), 0, negativeText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        
        // Add the buttons
        builder.setPositiveButton("DELETE", (dialog, which) -> {
            // Animate tag removal
            tagView.animate()
                    .alpha(0f)
                    .translationX(tagView.getWidth())
                    .setDuration(300)
                    .withEndAction(() -> {
                        tagsContainer.removeView(tagView);
                    })
                    .start();
        });
        
        builder.setNegativeButton("CANCEL", (dialog, which) -> {
            // Dialog will be dismissed automatically
        });
        
        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Style the buttons after dialog is shown
        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        
        positiveButton.setText(positiveText);
        negativeButton.setText(negativeText);
    }

    // Method to add chat messages
    private void addChatMessage(View dialogView, String message, boolean isUser) {
        RecyclerView chatRecyclerView = dialogView.findViewById(R.id.dialogChatRecyclerView);
        if (chatRecyclerView != null) {
            // Check if RecyclerView has an adapter
            RecyclerView.Adapter adapter = chatRecyclerView.getAdapter();
            if (adapter == null) {
                // Create a new adapter if one doesn't exist
                adapter = new ChatAdapter();
                chatRecyclerView.setAdapter(adapter);
                chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            }
            
            // Add the message
            ((ChatAdapter) adapter).addMessage(message, isUser);
            
            // Scroll to the bottom
            chatRecyclerView.scrollToPosition(((ChatAdapter) adapter).getItemCount() - 1);
        }
    }

    // Simple chat adapter
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<String> messages = new ArrayList<>();
        private List<Boolean> isUserMessages = new ArrayList<>();
        
        public void addMessage(String message, boolean isUser) {
            messages.add(message);
            isUserMessages.add(isUser);
            notifyItemInserted(messages.size() - 1);
        }
        
        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType == 1 ? R.layout.item_user_message : R.layout.item_ai_message, parent, false);
            return new ChatViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            holder.messageText.setText(messages.get(position));
        }
        
        @Override
        public int getItemCount() {
            return messages.size();
        }
        
        @Override
        public int getItemViewType(int position) {
            return isUserMessages.get(position) ? 1 : 0;
        }
        
        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;
            
            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
            }
        }
    }
}
