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

    private View chatPanel;
    private ImageButton closeChatButton;
    private EditText chatInput;
    private ImageButton sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

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

        // Initialize chat panel
        initializeChatPanel();
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
                popup.getMenu().add("Open Terminal");
                popup.getMenu().add("Run");
                popup.getMenu().add("Open in Directory");
                popup.getMenu().add("Chat with AI code base");
                popup.getMenu().add("Delete");
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getTitle().toString()) {
                        case "Open Terminal":
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
                        case "Run":
                            // Initialize output section
                            View outputSection = dialogView.findViewById(R.id.outputSection);
                            TextView outputText = dialogView.findViewById(R.id.outputText);
                            
                            // Show output section with animation
                            outputSection.setVisibility(View.VISIBLE);
                            outputSection.setAlpha(0f);
                            outputSection.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .start();
                            
                            outputText.setText("Running code...");
                            
                            // Execute the code
                            String code = contentInput.getText().toString();
                            String extension = languages[selectedLanguage[0]];
                            
                            // Initialize CodeExecutor if not already done
                            CodeExecutor codeExecutor = new CodeExecutor(this);
                            
                            codeExecutor.executeCode(code, extension, result -> {
                                runOnUiThread(() -> {
                                    // Update status indicator based on execution status
                                    int statusColor;
                                    
                                    // Force ERROR status if there's an error message
                                    if (!result.error.isEmpty()) {
                                        result.status = ExecutionStatus.ERROR;
                                    }
                                    
                                    // Set color based on status
                                    switch (result.status) {
                                        case NOT_EXECUTED:
                                            statusColor = Color.BLUE;
                                            break;
                                        case EXECUTING:
                                            statusColor = Color.BLUE;
                                            break;
                                        case SUCCESS:
                                            statusColor = Color.parseColor("#4CAF50"); // Green
                                            break;
                                        case ERROR:
                                            statusColor = Color.RED;
                                            break;
                                        default:
                                            statusColor = Color.BLUE;
                                    }
                                    
                                    // Apply the color to the status indicator
                                    statusIndicator.setBackgroundTintList(ColorStateList.valueOf(statusColor));

                                    // Update output text
                                    if (!result.error.isEmpty()) {
                                        outputText.setText("Error:\n" + result.error);
                                        outputText.setTextColor(Color.RED);
                                        
                                        // Highlight error lines in the editor
                                        highlightErrorLines(contentInput, result.errorLines);
                                    } else {
                                        outputText.setText("Output:\n" + result.output);
                                        outputText.setTextColor(ThemeHelper.isDarkTheme(this) ? 
                                            Color.WHITE : Color.BLACK);
                                        
                                        // Clear any error highlighting
                                        clearErrorHighlighting(contentInput);
                                    }
                                });
                            });
                            break;
                        case "Open in Directory":
                            // Handle directory opening
                            break;
                        case "Delete":
                            deleteNote(position);
                            dialog.dismiss();
                            break;
                        case "Chat with AI code base":
                            // Get chat panel that should be inside the dialog
                            View dialogChatPanel = dialogView.findViewById(R.id.dialogChatPanel);
                            
                            if (dialogChatPanel != null) {
                                // Toggle chat panel visibility
                                boolean isVisible = dialogChatPanel.getVisibility() == View.VISIBLE;
                                
                                if (isVisible) {
                                    // Hide chat panel and restore dialog size
                                    dialogChatPanel.setVisibility(View.GONE);
                                    if (dialog.getWindow() != null) {
                                        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                                        params.width = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                                        // Keep the dialog centered
                                        params.gravity = android.view.Gravity.CENTER;
                                        dialog.getWindow().setAttributes(params);
                                    }
                                    
                                    // Restore title bar elements to original position
                                    RelativeLayout titleBarView = dialogView.findViewById(R.id.titleBar);
                                    if (titleBarView != null) {
                                        TextView langIndicator = dialogView.findViewById(R.id.languageIndicator);
                                        ImageView optionsIcon = dialogView.findViewById(R.id.moreOptionsIcon);
                                        View statusView = dialogView.findViewById(R.id.statusIndicator);
                                        
                                        if (langIndicator != null && optionsIcon != null && statusView != null) {
                                            // Reset to original layout params
                                            RelativeLayout.LayoutParams langParams = (RelativeLayout.LayoutParams) langIndicator.getLayoutParams();
                                            langParams.addRule(RelativeLayout.START_OF, R.id.moreOptionsIcon);
                                            langParams.addRule(RelativeLayout.END_OF, 0);
                                            langParams.setMarginStart(0);
                                            langIndicator.setLayoutParams(langParams);
                                            
                                            RelativeLayout.LayoutParams moreParams = (RelativeLayout.LayoutParams) optionsIcon.getLayoutParams();
                                            moreParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                                            moreParams.addRule(RelativeLayout.END_OF, 0);
                                            moreParams.setMarginStart(0);
                                            optionsIcon.setLayoutParams(moreParams);
                                            
                                            RelativeLayout.LayoutParams statusParams = (RelativeLayout.LayoutParams) statusView.getLayoutParams();
                                            statusParams.addRule(RelativeLayout.START_OF, R.id.languageIndicator);
                                            statusParams.addRule(RelativeLayout.END_OF, 0);
                                            statusParams.setMarginStart(0);
                                            statusView.setLayoutParams(statusParams);
                                        }
                                    }
                                } else {
                                    // Show chat panel and make dialog wider
                                    if (dialog.getWindow() != null) {
                                        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
                                        getWindowManager().getDefaultDisplay().getMetrics(metrics);
                                        
                                        android.view.WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
                                        params.width = (int)(metrics.widthPixels * 0.95); // 95% of screen width
                                        // Keep the dialog centered - DO NOT change position
                                        params.gravity = android.view.Gravity.CENTER;
                                        // Do not set x, y coordinates to keep dialog centered
                                        dialog.getWindow().setAttributes(params);
                                        
                                        // Move title bar elements to the left when chat is visible
                                        RelativeLayout titleBarView = dialogView.findViewById(R.id.titleBar);
                                        if (titleBarView != null) {
                                            TextView langIndicator = dialogView.findViewById(R.id.languageIndicator);
                                            ImageView optionsIcon = dialogView.findViewById(R.id.moreOptionsIcon);
                                            View statusView = dialogView.findViewById(R.id.statusIndicator);
                                            
                                            if (langIndicator != null && optionsIcon != null && statusView != null) {
                                                // Reposition language indicator to be closer to file title
                                                RelativeLayout.LayoutParams langParams = (RelativeLayout.LayoutParams) langIndicator.getLayoutParams();
                                                langParams.removeRule(RelativeLayout.START_OF);
                                                langParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                                                langParams.addRule(RelativeLayout.END_OF, R.id.fileTitle);
                                                langParams.setMarginStart(16);
                                                langIndicator.setLayoutParams(langParams);
                                                
                                                // Reposition status indicator next to language indicator
                                                RelativeLayout.LayoutParams statusParams = (RelativeLayout.LayoutParams) statusView.getLayoutParams();
                                                statusParams.removeRule(RelativeLayout.START_OF);
                                                statusParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);
                                                statusParams.addRule(RelativeLayout.END_OF, R.id.languageIndicator);
                                                statusParams.setMarginStart(8);
                                                statusView.setLayoutParams(statusParams);
                                                
                                                // Reposition more options icon next to status indicator
                                                RelativeLayout.LayoutParams moreParams = (RelativeLayout.LayoutParams) optionsIcon.getLayoutParams();
                                                moreParams.removeRule(RelativeLayout.ALIGN_PARENT_END);
                                                moreParams.addRule(RelativeLayout.END_OF, R.id.statusIndicator);
                                                moreParams.setMarginStart(8);
                                                optionsIcon.setLayoutParams(moreParams);
                                            }
                                        }
                                    }
                                    
                                    // Make chat panel visible after adjusting layout
                                    dialogChatPanel.setVisibility(View.VISIBLE);
                                    
                                    // Initialize chat components inside the dialog
                                    ImageView closeChat = dialogView.findViewById(R.id.closeChat);
                                    RecyclerView dialogChatRecyclerView = dialogView.findViewById(R.id.dialogChatRecyclerView);
                                    EditText dialogChatInput = dialogView.findViewById(R.id.dialogChatInput);
                                    ImageButton dialogSendButton = dialogView.findViewById(R.id.dialogSendButton);
                                    LinearLayout tagsContainer = dialogView.findViewById(R.id.tagsContainer);
                                    LinearLayout addTagButton = dialogView.findViewById(R.id.addTagButton);
                                    
                                    // Set up add tag button
                                    if (addTagButton != null) {
                                        addTagButton.setOnClickListener(view -> {
                                            showAddTagDialog();
                                        });
                                    }
                                    
                                    // Make tags clickable and long-pressable for deletion
                                    if (tagsContainer != null) {
                                        for (int i = 0; i < tagsContainer.getChildCount(); i++) {
                                            View child = tagsContainer.getChildAt(i);
                                            if (child instanceof TextView) {
                                                final TextView tagView = (TextView) child;
                                                
                                                // Skip the add button
                                                if (child.getId() == R.id.addTagButton) {
                                                    continue;
                                                }
                                                
                                                // Make tag clickable
                                                tagView.setClickable(true);
                                                tagView.setFocusable(true);
                                                tagView.setBackground(getResources().getDrawable(R.drawable.tag_background, getTheme()));
                                                
                                                // Click to use as prompt
                                                tagView.setOnClickListener(view -> {
                                                    String prompt = "Help me with " + tagView.getText().toString();
                                                    dialogChatInput.setText(prompt);
                                                    dialogChatInput.setSelection(prompt.length());
                                                    dialogChatInput.requestFocus();
                                                });
                                                
                                                // Long press to delete
                                                tagView.setOnLongClickListener(longClickView -> {
                                                    showDeleteTagDialog(tagView.getText().toString(), tagView);
                                                    return true;
                                                });
                                            }
                                        }
                                    }
                                    
                                    // Set up close button click listener
                                    if (closeChat != null) {
                                        closeChat.setOnClickListener(closeView -> {
                                            dialogChatPanel.setVisibility(View.GONE);
                                            if (dialog.getWindow() != null) {
                                                android.view.WindowManager.LayoutParams closeParams = dialog.getWindow().getAttributes();
                                                closeParams.width = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
                                                // Keep the dialog centered
                                                closeParams.gravity = android.view.Gravity.CENTER;
                                                // Do not set x, y coordinates to keep dialog centered
                                                dialog.getWindow().setAttributes(closeParams);
                                                
                                                // Restore title bar elements to original position
                                                RelativeLayout titleBarView = dialogView.findViewById(R.id.titleBar);
                                                if (titleBarView != null) {
                                                    TextView langIndicator = dialogView.findViewById(R.id.languageIndicator);
                                                    ImageView optionsIcon = dialogView.findViewById(R.id.moreOptionsIcon);
                                                    View statusView = dialogView.findViewById(R.id.statusIndicator);
                                                    
                                                    if (langIndicator != null && optionsIcon != null && statusView != null) {
                                                        // Reset to original layout params
                                                        RelativeLayout.LayoutParams langParams = (RelativeLayout.LayoutParams) langIndicator.getLayoutParams();
                                                        langParams.addRule(RelativeLayout.START_OF, R.id.moreOptionsIcon);
                                                        langParams.addRule(RelativeLayout.END_OF, 0);
                                                        langParams.setMarginStart(0);
                                                        langIndicator.setLayoutParams(langParams);
                                                        
                                                        RelativeLayout.LayoutParams moreParams = (RelativeLayout.LayoutParams) optionsIcon.getLayoutParams();
                                                        moreParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                                                        moreParams.addRule(RelativeLayout.END_OF, 0);
                                                        moreParams.setMarginStart(0);
                                                        optionsIcon.setLayoutParams(moreParams);
                                                        
                                                        RelativeLayout.LayoutParams statusParams = (RelativeLayout.LayoutParams) statusView.getLayoutParams();
                                                        statusParams.addRule(RelativeLayout.START_OF, R.id.languageIndicator);
                                                        statusParams.addRule(RelativeLayout.END_OF, 0);
                                                        statusParams.setMarginStart(0);
                                                        statusView.setLayoutParams(statusParams);
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    
                                    if (dialogChatRecyclerView != null && dialogChatInput != null && dialogSendButton != null) {
                                        // Set up dialog chat components
                                        List<ChatMessage> dialogChatMessages = new ArrayList<>();
                                        ChatAdapter dialogChatAdapter = new ChatAdapter(dialogChatMessages);
                                        dialogChatRecyclerView.setAdapter(dialogChatAdapter);
                                        dialogChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
                                        
                                        // Add welcome message from AI
                                        dialogChatMessages.add(new ChatMessage("Hi there! I'm your AI coding assistant. How can I help with your code today?", false));
                                        dialogChatAdapter.notifyDataSetChanged();
                                        
                                        // Set up send button click listener
                                        dialogSendButton.setOnClickListener(view -> {
                                            String message = dialogChatInput.getText().toString().trim();
                                            if (!message.isEmpty()) {
                                                // Add user message
                                                dialogChatMessages.add(new ChatMessage(message, true));
                                                dialogChatAdapter.notifyItemInserted(dialogChatMessages.size() - 1);
                                                dialogChatRecyclerView.scrollToPosition(dialogChatMessages.size() - 1);
                                                dialogChatInput.setText("");
                                                
                                                // TODO: Add AI response logic
                                                // Simulate AI response for now
                                                new Handler().postDelayed(() -> {
                                                    dialogChatMessages.add(new ChatMessage("I've analyzed your code. It looks like you're using JavaScript to log a number. Is there anything specific you'd like help with?", false));
                                                    dialogChatAdapter.notifyItemInserted(dialogChatMessages.size() - 1);
                                                    dialogChatRecyclerView.scrollToPosition(dialogChatMessages.size() - 1);
                                                }, 2000);
                                            }
                                        });
                                        
                                        // Set focus on input field
                                        dialogChatInput.requestFocus();
                                    }
                                }
                            } else {
                                Toast.makeText(this, "Chat functionality not available in this dialog", Toast.LENGTH_SHORT).show();
                            }
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
        StringBuilder result = new StringBuilder();
        
        try {
            String currentOutput = output.getText().toString();
            String prompt = getPromptForOS(os);
            
            // Simulate terminal commands
            if (command.startsWith("git ")) {
                simulateGitCommand(command, result);
            } else {
                switch (os) {
                    case "Windows":
                        simulateWindowsCommand(command, result);
                        break;
                    case "macOS":
                    case "Linux":
                        simulateUnixCommand(command, result);
                        break;
                }
            }
            
            // Update the output TextView on the UI thread
            runOnUiThread(() -> {
                output.setText(currentOutput + "\n" + prompt + command + "\n" + result.toString());
                
                // Scroll to the bottom
                output.post(() -> {
                    if (output.getLayout() != null) {
                        int scrollAmount = output.getLayout().getLineTop(output.getLineCount()) - output.getHeight();
                        if (scrollAmount > 0) {
                            output.scrollTo(0, scrollAmount);
                        } else {
                            output.scrollTo(0, 0);
                        }
                    }
                });
            });
            
        } catch (Exception e) {
            String errorMessage = "Error executing command: " + e.getMessage() + "\n";
            runOnUiThread(() -> output.append(errorMessage));
        }
    }

    private void simulateWindowsCommand(String command, StringBuilder result) {
        File workingDir = new File(getFilesDir(), "workspace");
        
        if (command.equals("dir")) {
            if (!workingDir.exists()) {
                workingDir.mkdirs();
            }
            
            result.append(" Directory of ").append(workingDir.getAbsolutePath()).append("\n\n");
            
            File[] files = workingDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileType = file.isDirectory() ? "<DIR>" : "     ";
                    String lastModified = new java.text.SimpleDateFormat("MM/dd/yyyy  HH:mm")
                            .format(new java.util.Date(file.lastModified()));
                    String size = file.isDirectory() ? "    " : String.format("%8d", file.length());
                    
                    result.append(String.format("%s  %s  %s  %s\n",
                            lastModified, fileType, size, file.getName()));
                }
            }
            
            result.append("\n     Total files listed:\n")
                  .append("          ").append(files != null ? files.length : 0).append(" File(s)\n");
                  
        } else if (command.equals("cd")) {
            result.append(workingDir.getAbsolutePath()).append("\n");
            
        } else if (command.startsWith("echo ")) {
            result.append(command.substring(5)).append("\n");
            
        } else if (command.equals("help")) {
            result.append("Supported commands:\n")
                  .append("  dir         - Lists files and directories\n")
                  .append("  cd          - Shows current directory\n")
                  .append("  echo [text] - Displays text\n")
                  .append("  cls         - Clears the screen\n")
                  .append("  git [cmd]   - Git commands\n");
                  
        } else if (command.equals("cls")) {
            // Clear will be handled differently
            result.append("\n");
            
        } else {
            result.append("'").append(command).append("' is not recognized as an internal command\n");
        }
    }

    private void simulateUnixCommand(String command, StringBuilder result) {
        File workingDir = new File(getFilesDir(), "workspace");
        
        if (command.equals("ls")) {
            if (!workingDir.exists()) {
                workingDir.mkdirs();
            }
            
            File[] files = workingDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    result.append(file.getName()).append("  ");
                }
            }
            result.append("\n");
            
        } else if (command.equals("pwd")) {
            result.append(workingDir.getAbsolutePath()).append("\n");
            
        } else if (command.startsWith("echo ")) {
            result.append(command.substring(5)).append("\n");
            
        } else if (command.equals("clear")) {
            // Clear will be handled differently
            result.append("\n");
            
        } else if (command.equals("help")) {
            result.append("Supported commands:\n")
                  .append("  ls          - Lists files and directories\n")
                  .append("  pwd         - Shows current directory\n")
                  .append("  echo [text] - Displays text\n")
                  .append("  clear       - Clears the screen\n")
                  .append("  git [cmd]   - Git commands\n");
                  
        } else {
            result.append("command not found: ").append(command).append("\n");
        }
    }

    private void simulateGitCommand(String command, StringBuilder result) {
        String[] parts = command.split("\\s+");
        if (parts.length < 2) {
            result.append("git: missing command\n");
            return;
        }
        
        String gitCommand = parts[1];
        switch (gitCommand) {
            case "init":
                result.append("Initialized empty Git repository\n");
                break;
            case "status":
                result.append("On branch master\n")
                      .append("No commits yet\n")
                      .append("nothing to commit (create/copy files and use \"git add\" to track)\n");
                break;
            case "add":
                if (parts.length < 3) {
                    result.append("Nothing specified, nothing added.\n");
                } else {
                    result.append("add '").append(parts[2]).append("'\n");
                }
                break;
            case "commit":
                if (parts.length < 4 || !parts[2].equals("-m")) {
                    result.append("Please provide a commit message using -m\n");
                } else {
                    result.append("Created commit: ").append(parts[3]).append("\n");
                }
                break;
            case "branch":
                result.append("* master\n");
                break;
            case "help":
                result.append("Common Git commands:\n")
                      .append("   init    Create empty Git repository\n")
                      .append("   status  Show working tree status\n")
                      .append("   add     Add file contents to index\n")
                      .append("   commit  Record changes to repository\n")
                      .append("   branch  List branches\n");
                break;
            default:
                result.append("git: '").append(gitCommand).append("' is not a git command.\n");
                break;
        }
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

    private void initializeChatPanel() {
        chatPanel = findViewById(R.id.chatPanel);
        closeChatButton = findViewById(R.id.closeChatButton);
        chatInput = findViewById(R.id.chatInput);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // Initialize chat messages
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Hide chat panel initially
        chatPanel.setVisibility(View.GONE);

        // Set up close button
        closeChatButton.setOnClickListener(v -> hideChatPanel());

        // Set up send button
        sendButton.setOnClickListener(v -> sendChatMessage());
    }

    private void showChatPanel() {
        // Ensure chat panel is visible and ready for display
        chatPanel.setVisibility(View.VISIBLE);
        chatPanel.setAlpha(0f);
        
        // Get screen width
        android.util.DisplayMetrics metrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        
        // Set proper width and position
        ViewGroup.LayoutParams params = chatPanel.getLayoutParams();
        params.width = (metrics.widthPixels / 2) - 20;
        chatPanel.setLayoutParams(params);
        
        // Explicitly position at right half of screen
        chatPanel.setX(metrics.widthPixels / 2 + 10);
        
        // Make sure chat panel is visible above everything
        ViewGroup rootView = (ViewGroup) getWindow().getDecorView().getRootView();
        if (chatPanel.getParent() != null) {
            ((ViewGroup) chatPanel.getParent()).removeView(chatPanel);
        }
        rootView.addView(chatPanel);
        
        // Set very high elevation and bring to front
        chatPanel.setElevation(9999f);
        chatPanel.bringToFront();
        
        // Animate the chat panel in
        chatPanel.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
        
        // Force redraw
        chatPanel.invalidate();
        
        // Request focus on chat input
        chatInput.requestFocus();
    }

    private void hideChatPanel() {
        chatPanel.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction(() -> {
                chatPanel.setVisibility(View.GONE);
                chatPanel.setElevation(0f);
            })
            .start();
    }

    private void sendChatMessage() {
        String message = chatInput.getText().toString().trim();
        if (!message.isEmpty()) {
            // Add user message
            chatMessages.add(new ChatMessage(message, true));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);

            // Clear input
            chatInput.setText("");

            // TODO: Process message and get AI response
            // For now, just echo the message
            String aiResponse = "You said: " + message;
            chatMessages.add(new ChatMessage(aiResponse, false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
        }
    }

    // Chat Message class
    private static class ChatMessage {
        String message;
        boolean isUser;

        ChatMessage(String message, boolean isUser) {
            this.message = message;
            this.isUser = isUser;
        }
    }

    // Chat Adapter class
    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private List<ChatMessage> messages;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_item, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.messageText.setText(message.message);
            
            // Align messages based on sender
            if (message.isUser) {
                holder.messageText.setBackgroundResource(R.drawable.user_message_background);
                holder.messageText.setTextColor(Color.WHITE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                ));
                holder.messageText.setGravity(android.view.Gravity.END);
            } else {
                holder.messageText.setBackgroundResource(R.drawable.ai_message_background);
                holder.messageText.setTextColor(Color.BLACK);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT
                ));
                holder.messageText.setGravity(android.view.Gravity.START);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        static class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;

            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
            }
        }
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
}
