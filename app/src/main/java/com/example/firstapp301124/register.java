package com.example.firstapp301124;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class register extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsIndicator;
    private Button continueButton;
    private int currentPosition = 0;
    TextView loginscreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        continueButton = findViewById(R.id.continue_button);

        List<View> layouts = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(this);
        layouts.add(inflater.inflate(R.layout.input_email, null));
        layouts.add(inflater.inflate(R.layout.input_password, null));
        layouts.add(inflater.inflate(R.layout.input_link, null));

        InputPagerAdapter adapter = new InputPagerAdapter(layouts);
        viewPager.setAdapter(adapter);

        setupDotsIndicator(layouts.size());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updateDotsIndicator(position);
            }
        });

        continueButton.setOnClickListener(v -> {
            if (currentPosition < layouts.size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1);
            }
        });

        loginscreen = findViewById(R.id.login_screen);
        loginscreen.setOnClickListener(view -> {
            Intent i = new Intent(register.this, MainActivity.class);
            startActivity(i);
        });
    }

    private void setupDotsIndicator(int count) {
        dotsIndicator.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(16, 16);
            params.setMargins(8, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.dot_inactive);
            dotsIndicator.addView(dot);
        }
        updateDotsIndicator(0);
    }

    private void updateDotsIndicator(int position) {
        for (int i = 0; i < dotsIndicator.getChildCount(); i++) {
            dotsIndicator.getChildAt(i)
                    .setBackgroundResource(i == position ? R.drawable.dot_active : R.drawable.dot_inactive);
        }
    }
}
