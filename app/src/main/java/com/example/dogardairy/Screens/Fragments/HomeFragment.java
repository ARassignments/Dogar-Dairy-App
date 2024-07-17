package com.example.dogardairy.Screens.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.dogardairy.R;
import com.example.dogardairy.Screens.MonthlySupplyActivity;
import com.example.dogardairy.Screens.OtherItemsActivity;
import com.example.dogardairy.Screens.SeeMoreActivity;
import com.example.dogardairy.Screens.SettingRateActivity;
import com.example.dogardairy.Screens.SettingsActivity;

public class HomeFragment extends Fragment {

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);
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
        return view;
    }
}