package com.example.dogardairy.Screens.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.dogardairy.MainActivity;
import com.example.dogardairy.R;
import com.example.dogardairy.Screens.CustomerServiceActivity;
import com.example.dogardairy.Screens.DashboardActivity;
import com.example.dogardairy.Screens.HelpCenterActivity;
import com.example.dogardairy.Screens.MonthlySupplyActivity;
import com.example.dogardairy.Screens.OtherItemsActivity;
import com.example.dogardairy.Screens.SeeMoreActivity;
import com.example.dogardairy.Screens.SettingRateActivity;
import com.example.dogardairy.Screens.SettingsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    View view;
    static String UID = "";
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    FrameLayout chatBtn, faqBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
        sharedPreferences = getContext().getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
        }
        chatBtn = view.findViewById(R.id.chatBtn);
        faqBtn = view.findViewById(R.id.faqBtn);

        view.findViewById(R.id.shopSupplyBtn).setAlpha(0.5f);
        view.findViewById(R.id.stockMilkBtn).setAlpha(0.5f);
        view.findViewById(R.id.creditBtn).setAlpha(0.5f);
        view.findViewById(R.id.expencesBtn).setAlpha(0.5f);
        view.findViewById(R.id.workersPayBtn).setAlpha(0.5f);

        view.findViewById(R.id.seeMoreBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(inflater.getContext(), SeeMoreActivity.class));
            }
        });

        view.findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(inflater.getContext(), SettingsActivity.class));
            }
        });

        view.findViewById(R.id.monthlySupplyBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(inflater.getContext(), MonthlySupplyActivity.class));
            }
        });

        view.findViewById(R.id.settingRatesBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(inflater.getContext(), SettingRateActivity.class));
            }
        });

        view.findViewById(R.id.otherItemsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(inflater.getContext(), OtherItemsActivity.class));
            }
        });

        MainActivity.db.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String role = snapshot.child("role").getValue().toString();
                    if(role.equals("admin")){
                        chatBtn.setAlpha(0.5f);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        chatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!DashboardActivity.getRole().equals("admin")){
                    startActivity(new Intent(getContext(), CustomerServiceActivity.class));
                }
            }
        });

        faqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), HelpCenterActivity.class));
            }
        });

        return view;
    }
}