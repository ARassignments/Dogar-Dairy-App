package com.example.dogardairy.Screens;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogardairy.MainActivity;
import com.example.dogardairy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {

    EditText nameInput;
    TextView emailText, createdOnText, roleText, saveBtn;
    ImageView editImageBtn, profileImage;
    static String UID = "";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    int[] images = {
            R.drawable.boy_1,
            R.drawable.boy_2,
            R.drawable.boy_3,
            R.drawable.boy_4,
            R.drawable.boy_5,
            R.drawable.boy_6,
            R.drawable.boy_7,
            R.drawable.boy_8,
            R.drawable.boy_9,
            R.drawable.boy_10,
            R.drawable.boy_11,
            R.drawable.boy_12,
            R.drawable.boy_13,
            R.drawable.boy_14,
            R.drawable.boy_15,
            R.drawable.boy_16,
            R.drawable.boy_17,
            R.drawable.boy_18
    };

    Dialog dialogImage;
    CardView boy_1, boy_2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        nameInput = findViewById(R.id.nameInput);
        emailText = findViewById(R.id.emailText);
        createdOnText = findViewById(R.id.createdOnText);
        roleText = findViewById(R.id.roleText);
        saveBtn = findViewById(R.id.saveBtn);
        editImageBtn = findViewById(R.id.editImageBtn);
        profileImage = findViewById(R.id.profileImage);

        nameInput.setText(DashboardActivity.getName());
        emailText.setText(DashboardActivity.getEmail());
        createdOnText.setText(DashboardActivity.getCreatedOn());
        roleText.setText(DashboardActivity.getRole());

        sharedPreferences = getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
            MainActivity.db.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
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

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nameValidation();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.super.onBackPressed();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation();
            }
        });

        editImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogImage = new Dialog(ProfileActivity.this);
                dialogImage.setContentView(R.layout.dialog_bottom_profile_image);
                dialogImage.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogImage.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialogImage.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationBottom;
                dialogImage.getWindow().setGravity(Gravity.BOTTOM);
                dialogImage.setCanceledOnTouchOutside(false);
                dialogImage.setCancelable(false);
                Button cancelBtn;
                cancelBtn = dialogImage.findViewById(R.id.cancelBtn);
                boy_1 = dialogImage.findViewById(R.id.boy_1);
                boy_2 = dialogImage.findViewById(R.id.boy_2);
                dialogImage.show();

                boy_1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setProfileImage(0);
                    }
                });

                boy_2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setProfileImage(1);
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogImage.dismiss();
                    }
                });
            }
        });

    }

    public boolean nameValidation(){
        String input = nameInput.getText().toString().trim();
        String regex = "^[a-zA-Z\\s]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            nameInput.setError("Name is Required!!!");
            return false;
        } else if(input.length() < 3){
            nameInput.setError("Name at least 3 Characters!!!");
            return false;
        } else if(!matcher.matches()){
            nameInput.setError("Only text allowed!!!");
            return false;
        } else {
            nameInput.setError(null);
            return true;
        }
    }

    public void validation(){
        boolean nameErr = false;
        nameErr = nameValidation();
        Dialog dialog = new Dialog(ProfileActivity.this);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        TextView msg = dialog.findViewById(R.id.msgDialog);
        msg.setText("Loading...");
        dialog.show();
        if(nameErr == true){
            dialog.dismiss();
            // Name Update In Firebase Realtime Database
            MainActivity.db.child("Users").child(UID).child("name").setValue(nameInput.getText().toString().trim());
            Dialog dialogSuccess = new Dialog(ProfileActivity.this);
            dialogSuccess.setContentView(R.layout.dialog_success);
            dialogSuccess.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialogSuccess.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialogSuccess.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialogSuccess.getWindow().setGravity(Gravity.CENTER);
            dialogSuccess.setCanceledOnTouchOutside(false);
            dialogSuccess.setCancelable(false);
            msg = dialogSuccess.findViewById(R.id.msgDialog);
            msg.setText("Profile Updated Successfully!!!");
            dialogSuccess.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialogSuccess.dismiss();
                }
            },4000);
        }
    }

    public void setProfileImage(int value){
        profileImage.setImageResource(images[value]);
        dialogImage.dismiss();
        MainActivity.db.child("Users").child(UID).child("image").setValue(""+images[value]);
    }

}