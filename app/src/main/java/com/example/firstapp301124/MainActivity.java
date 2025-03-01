package com.example.firstapp301124;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    EditText loginInputUsername, loginInputPassword;
    Button loginBtn;
    TextView registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.applyTheme(this); // Apply theme before setContentView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Change status bar color to white
            getWindow().setStatusBarColor(Color.WHITE);

            // Change the status bar content (battery, time, etc.) to black
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        loginInputUsername = findViewById(R.id.username_inupt);
        loginInputPassword = findViewById(R.id.password_input);
        loginBtn = findViewById(R.id.login_button); // Ensure button ID is set correctly in XML

        // Get the input values as Strings
        String username = loginInputUsername.getText().toString().trim();
        String password = loginInputPassword.getText().toString().trim();

        RaabtaaDBHelper db = new RaabtaaDBHelper(this);
        User user = db.getUser(username,password);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (user != null) {
                    // User found, proceed to next activity
                    Intent intent = new Intent(MainActivity.this, PersonalProfileHome.class); // Replace HomeActivity with the actual activity
                    startActivity(intent);
                    Toast.makeText(MainActivity.this, "login successfull", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    //remove after set db
                    Intent intent = new Intent(MainActivity.this, PersonalProfileHome.class); // Replace HomeActivity with the actual activity
                    startActivity(intent);
                    // User not found, show error message
                    Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Find the TextView and apply styling
        registerBtn = findViewById(R.id.register_link);
        String text = "New to the plateform? Register";

        // Create a SpannableString to style "Log in"
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6200EE")), // Purple color
                text.indexOf("Register"),
                text.length(),
                SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        registerBtn.setText(spannable);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, register.class);
                startActivity(i);
            }
        });

        // Initialize navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set initial state of dark mode toggle
        MenuItem darkModeItem = navigationView.getMenu().findItem(R.id.nav_dark_mode);
        darkModeItem.setChecked(ThemeHelper.isDarkTheme(this));
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dark_mode) {
            boolean isChecked = !item.isChecked();
            item.setChecked(isChecked);
            ThemeHelper.setTheme(this, isChecked);
            recreate(); // Restart activity to apply theme
            return true;
        }

        // Handle other menu items
        return false;
    }
}