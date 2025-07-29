package com.example.dogardairy;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogardairy.Screens.SplashScreenActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static String UID = "";
    static String name, email, role, image, created_on;
    public static DatabaseReference db;

    public static SmsManager mySmsManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mySmsManager = SmsManager.getDefault();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED);

        sharedPreferences = getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(db == null){
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            db = FirebaseDatabase.getInstance().getReference();
        }

        applyTheme();

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
            db.child("Users").child(UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        name = snapshot.child("name").getValue().toString();
                        email = snapshot.child("email").getValue().toString();
                        image = snapshot.child("image").getValue().toString();
                        role = snapshot.child("role").getValue().toString();
                        created_on = snapshot.child("created_on").getValue().toString();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        checkSmsPermission();
        checkStoragePermission();

        Intent intent = new Intent(MainActivity.this, SplashScreenActivity.class);
        startActivity(intent);
        finish();
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

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
        }
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

            // If any permission is not granted, request them
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.SEND_SMS,
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS
            }, 123);
        }
    }

    public static boolean connectionCheck(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo dataConn = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifiConn != null && wifiConn.isConnected()) || (dataConn != null && dataConn.isConnected())) {
            return true;
        } else {
            Dialog loaddialog = new Dialog(context);
            loaddialog.setContentView(R.layout.dialog_connection_error);
            loaddialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            loaddialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            loaddialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            loaddialog.getWindow().setGravity(Gravity.CENTER);
            loaddialog.setCancelable(false);
            loaddialog.setCanceledOnTouchOutside(false);
            loaddialog.show();
            Button retryBtn = loaddialog.findViewById(R.id.retryBtn);
            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loaddialog.dismiss();
                }
            });
            return false;
        }
    }

    public static void checkStatus(Context context, String UID){
        db.child("Users").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_error);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    TextView msg = dialog.findViewById(R.id.msgDialog);
                    msg.setText("Your Account Is Suspended By Admin");

                    String status = snapshot.child("status").getValue().toString();
                    if(status.equals("0")){
                        dialog.show();
                    } else if(status.equals("1")){
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static void checkMaintainance(Context context){
        db.child("MessageToaster").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){

                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_maintainance);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    TextView title = dialog.findViewById(R.id.title);
                    TextView message = dialog.findViewById(R.id.message);
                    title.setText(snapshot.child("title").getValue().toString());
                    message.setText(snapshot.child("message").getValue().toString());

                    String status = snapshot.child("status").getValue().toString();
                    if(status.equals("0")){
                        dialog.show();
                    } else if(status.equals("1")){
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}