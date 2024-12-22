package com.example.firstapp301124;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class register extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsIndicator;
    private Button continueButton;
    private int currentPosition = 0;
    private boolean isEmailValid = false, isPasswordValid = false, isLinkValid = false;
    private EditText emailField, passwordField, linkField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        continueButton = findViewById(R.id.continue_button);

        List<Integer> layouts = new ArrayList<>();
        layouts.add(R.layout.input_email);
        layouts.add(R.layout.input_password);
        layouts.add(R.layout.input_link);

        InputPagerAdapter adapter = new InputPagerAdapter(layouts);
        viewPager.setAdapter(adapter);

        setupDotsIndicator(layouts.size());

        // Disable swipe scrolling initially
        viewPager.setUserInputEnabled(false);

        // Disable the continue button initially
        continueButton.setEnabled(false);
        continueButton.setBackgroundTintList(getColorStateList(R.color.gray));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updateDotsIndicator(position);
                validateCurrentStep();
            }
        });

        continueButton.setOnClickListener(v -> {
            if (currentPosition < layouts.size() - 1) {
                viewPager.setCurrentItem(currentPosition + 1);
                validateCurrentStep();
            }
        });

        TextView loginScreen = findViewById(R.id.login_screen);
        String text = "New to the platform? Login";
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6200EE")), text.indexOf("Login"), text.length(), 0);
        loginScreen.setText(spannable);
        loginScreen.setOnClickListener(v -> {
            Intent intent = new Intent(register.this, MainActivity.class);
            startActivity(intent);
        });

        TextView dummy = findViewById(R.id.dummy);
        String text2 = "New to the platform? Login";
        SpannableString spannable2 = new SpannableString(text2);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#6200EE")), text2.indexOf("Login"), text2.length(), 0);
        loginScreen.setText(spannable2);
        loginScreen.setOnClickListener(v -> {
            Intent intent = new Intent(register.this, PersonalProfileHome.class);
            startActivity(intent);
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

    private void validateCurrentStep() {
        View currentView = findViewById(viewPager.getCurrentItem() == 0 ? R.id.email_field :
                viewPager.getCurrentItem() == 1 ? R.id.password_field :
                        R.id.link_field);
        switch (currentPosition) {
            case 0:
                validateEmailInput(currentView);
                break;
            case 1:
                validatePasswordInput(currentView);
                break;
            case 2:
                validateLinkInput(currentView);
                break;
        }
    }

    private void validateEmailInput(View emailView) {
        if (emailView != null) {
            emailField = (EditText) emailView;
            emailField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    isEmailValid = !TextUtils.isEmpty(s) && Patterns.EMAIL_ADDRESS.matcher(s).matches();
                    updateContinueButton();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void validatePasswordInput(View passwordView) {
        if (passwordView != null) {
            passwordField = (EditText) passwordView;
            passwordField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String password = s.toString();

                    // Validation logic
                    if (password.isEmpty()) {
                        isPasswordValid = false;
                        passwordField.setError("Password cannot be empty");
                    } else if (password.length() < 5) {
                        isPasswordValid = false;
                        passwordField.setError("Password must be at least 5 characters");
                    } else if (password.length() > 16) {
                        isPasswordValid = false;
                        passwordField.setError("Password must not exceed 16 characters");
                    } else {
                        isPasswordValid = true;
                        passwordField.setError(null); // Clear the error
                    }

                    // Update the continue button based on validation
                    updateContinueButton();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }


    private void validateLinkInput(View linkView) {
        if (linkView != null) {
            linkField = (EditText) linkView;
            linkField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    isLinkValid = !TextUtils.isEmpty(s) && Patterns.WEB_URL.matcher(s).matches();
                    updateContinueButton();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void updateContinueButton() {
        boolean isValid = (currentPosition == 0 && isEmailValid) ||
                (currentPosition == 1 && isPasswordValid) ||
                (currentPosition == 2 && isLinkValid);
        continueButton.setEnabled(isValid);
        continueButton.setBackgroundTintList(getColorStateList(isValid ? R.color.purple_200 : R.color.gray));

        // Enable scrolling to the next page only when valid
        viewPager.setUserInputEnabled(isValid);
    }
}
