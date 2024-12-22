package com.example.firstapp301124;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    EditText loginInputUsername, loginInputPassword;
    Button loginBtn;
    TextView registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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



    }
}