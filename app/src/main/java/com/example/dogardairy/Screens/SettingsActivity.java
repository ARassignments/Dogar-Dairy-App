package com.example.dogardairy.Screens;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogardairy.MainActivity;
import com.example.dogardairy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    Button logoutBtn;
    TextView profileName, themeModeBtn;
    LinearLayout adminOptions;
    static String UID = "";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    CircleImageView profileImage;
    Switch themeSwitch;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        logoutBtn = findViewById(R.id.logoutBtn);
        adminOptions = findViewById(R.id.adminOptions);
        profileName = findViewById(R.id.profileName);
        profileImage = findViewById(R.id.profileImage);
        themeSwitch = findViewById(R.id.themeSwitch);
        themeModeBtn = findViewById(R.id.themeModeBtn);

        sharedPreferences = getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
            MainActivity.db.child("Users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        profileName.setText(snapshot.child("name").getValue().toString());
                        if(!snapshot.child("image").getValue().toString().equals("")){
                            profileImage.setImageResource(Integer.parseInt(snapshot.child("image").getValue().toString()));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        boolean isDark = isDarkMode();
        themeSwitch.setChecked(isDark);
        updateThemeLabel(isDark);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                saveTheme(true);
                updateThemeLabel(true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                saveTheme(false);
                updateThemeLabel(false);
            }
            recreate(); // Recreate activity to apply theme immediately
        });

        MainActivity.checkStatus(SettingsActivity.this, UID);
        if(DashboardActivity.getRole().equals("admin")){
            adminOptions.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.super.onBackPressed();
            }
        });

        findViewById(R.id.usersBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, UsersActivity.class));
            }
        });

        findViewById(R.id.messagesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, MessagesActivity.class));
            }
        });

        findViewById(R.id.paymentMethodBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, PaymentMethodActivity.class));
            }
        });


        findViewById(R.id.viewProfileBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));
            }
        });

        findViewById(R.id.subscriptionBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SettingsActivity.this, SubscriptionActivity.class));
            }
        });

        findViewById(R.id.privacyPolicyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, PrivacyPolicyActivity.class));
            }
        });

        findViewById(R.id.helpCenterBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, HelpCenterActivity.class));
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(SettingsActivity.this);
                dialog.setContentView(R.layout.dialog_bottom_logout);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationBottom;
                dialog.getWindow().setGravity(Gravity.BOTTOM);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                Button cancelBtn, yesBtn;
                cancelBtn = dialog.findViewById(R.id.cancelBtn);
                yesBtn = dialog.findViewById(R.id.yesBtn);
                dialog.show();

                yesBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        editor.clear();
                        editor.commit();
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        startActivity(new Intent(SettingsActivity.this, LoginActivity.class));
                        finish();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    public void saveTheme(boolean darkMode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean("dark_mode", darkMode).apply();
    }

    public boolean isDarkMode() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        return preferences.getBoolean("dark_mode", false);
    }

    public void applyTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!preferences.contains("dark_mode")) {
            boolean isSystemDark = isSystemDarkMode();
            AppCompatDelegate.setDefaultNightMode(
                    isSystemDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            saveTheme(isSystemDark); // Save it for future
        } else {
            boolean isDark = preferences.getBoolean("dark_mode", false);
            AppCompatDelegate.setDefaultNightMode(
                    isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        }
    }

    public boolean isSystemDarkMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }

    private void updateThemeLabel(boolean isDarkMode) {
        if (isDarkMode) {
            themeModeBtn.setText("Dark Mode");
            themeModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.moon, 0, 0, 0);
        } else {
            themeModeBtn.setText("Light Mode");
            themeModeBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sun, 0, 0, 0);
        }
    }


}