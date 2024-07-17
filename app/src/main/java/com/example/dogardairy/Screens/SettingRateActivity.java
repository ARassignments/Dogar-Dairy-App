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
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogardairy.MainActivity;
import com.example.dogardairy.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SettingRateActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static String UID = "";
    Button supplyMilkBtn, stockMilkBtn;
    TextInputLayout supplyAmountLayout, stockAmountLayout;
    TextInputEditText supplyAmountInput, stockAmountInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting_rate);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreferences = getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        supplyAmountLayout = findViewById(R.id.supplyAmountLayout);
        stockAmountLayout = findViewById(R.id.stockAmountLayout);
        supplyAmountInput = findViewById(R.id.supplyAmountInput);
        stockAmountInput = findViewById(R.id.stockAmountInput);
        supplyMilkBtn = findViewById(R.id.supplyMilkBtn);
        stockMilkBtn = findViewById(R.id.stockMilkBtn);

        //date picker start
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        TextView textViewDate = findViewById(R.id.date);
        textViewDate.setText(currentDate);
        //date picker end

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
            MainActivity.db.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        supplyAmountInput.setText(snapshot.child("milkRate").getValue().toString());
                        stockAmountInput.setText(snapshot.child("stockRate").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        supplyAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                supplyAmountValidation();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        stockAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stockAmountValidation();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        supplyMilkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supplyMilkValidation();
            }
        });

        stockMilkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stockMilkValidation();
            }
        });

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingRateActivity.super.onBackPressed();
            }
        });
    }

    public boolean supplyAmountValidation(){
        String input = supplyAmountInput.getText().toString().trim();
        String regex = "^[0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            supplyAmountLayout.setError("Amount is Required!!!");
            return false;
        } else if(!matcher.matches()){
            supplyAmountLayout.setError("Only Digits Allowed!!!");
            return false;
        } else {
            supplyAmountLayout.setError(null);
            return true;
        }
    }

    public boolean stockAmountValidation(){
        String input = stockAmountInput.getText().toString().trim();
        String regex = "^[0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            stockAmountLayout.setError("Amount is Required!!!");
            return false;
        } else if(!matcher.matches()){
            stockAmountLayout.setError("Only Digits Allowed!!!");
            return false;
        } else {
            stockAmountLayout.setError(null);
            return true;
        }
    }

    private void supplyMilkValidation() {
        boolean supplyAmountErr = false;
        supplyAmountErr = supplyAmountValidation();

        if((supplyAmountErr) == true){
            MainActivity.db.child("Users").child(UID).child("milkRate").setValue(supplyAmountInput.getText().toString().trim());
            Dialog alertdialog = new Dialog(SettingRateActivity.this);
            alertdialog.setContentView(R.layout.dialog_success);
            alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertdialog.getWindow().setGravity(Gravity.CENTER);
            alertdialog.setCancelable(false);
            alertdialog.setCanceledOnTouchOutside(false);
            TextView message = alertdialog.findViewById(R.id.msgDialog);
            message.setText("Set Supply Milk Rate Successfully!!!");
            alertdialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertdialog.dismiss();
                }
            },2000);
        }
    }

    private void stockMilkValidation() {
        boolean stockAmountErr = false;
        stockAmountErr = stockAmountValidation();

        if((stockAmountErr) == true){
            MainActivity.db.child("Users").child(UID).child("stockRate").setValue(stockAmountInput.getText().toString().trim());
            Dialog alertdialog = new Dialog(SettingRateActivity.this);
            alertdialog.setContentView(R.layout.dialog_success);
            alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertdialog.getWindow().setGravity(Gravity.CENTER);
            alertdialog.setCancelable(false);
            alertdialog.setCanceledOnTouchOutside(false);
            TextView message = alertdialog.findViewById(R.id.msgDialog);
            message.setText("Set Stock Milk Rate Successfully!!!");
            alertdialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertdialog.dismiss();
                }
            },2000);
        }
    }
}