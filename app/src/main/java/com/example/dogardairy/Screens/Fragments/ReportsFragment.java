package com.example.dogardairy.Screens.Fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.dogardairy.MainActivity;
import com.example.dogardairy.Models.MonthlyModel;
import com.example.dogardairy.R;
import com.example.dogardairy.Screens.BillActivity;
import com.example.dogardairy.Screens.DashboardActivity;
import com.example.dogardairy.Screens.MonthlySupplyActivity;
import com.example.dogardairy.Screens.MonthlySupplyDetailActivity;
import com.example.dogardairy.Screens.SettingsActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReportsFragment extends Fragment {

    View view;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static String UID = "";
    static String sortingStatus = "dsc";
    ListView listView;
    LinearLayout loader, notifyBar, notfoundContainer;
    ImageView sortBtn;
    EditText searchInput;
    TextView searchedWord, totalCount;
    ArrayList<MonthlyModel> datalist = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_reports, container, false);

        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreferences = inflater.getContext().getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
        }

        listView = view.findViewById(R.id.listView);
        notifyBar = view.findViewById(R.id.notifyBar);
        notfoundContainer = view.findViewById(R.id.notfoundContainer);
        loader = view.findViewById(R.id.loader);
        searchedWord = view.findViewById(R.id.searchedWord);
        totalCount = view.findViewById(R.id.totalCount);
        searchInput = view.findViewById(R.id.searchInput);
        sortBtn = view.findViewById(R.id.sortBtn);

        view.findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(inflater.getContext(), SettingsActivity.class));
            }
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchedWord.setText(searchInput.getText().toString().trim());
                searchValidation();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        fetchData("");

        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sorting();
            }
        });

        return view;
    }

    public void sorting(){
        if(sortingStatus.equals("asc")){
            sortingStatus = "dsc";
            sortBtn.setImageResource(R.drawable.deasscending_order);
        } else if(sortingStatus.equals("dsc")){
            sortingStatus = "asc";
            sortBtn.setImageResource(R.drawable.asscending_order);
        }
        fetchData("");
    }

    public void fetchData(String data){
        MainActivity.db.child("Monthly").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    datalist.clear();
                    for (DataSnapshot ds: snapshot.getChildren()){
                        if(data.equals("")){
                            if(DashboardActivity.getRole().equals("admin")){
                                MonthlyModel model = new MonthlyModel(ds.getKey(),
                                        ds.child("name").getValue().toString(),
                                        ds.child("contact").getValue().toString(),
                                        ds.child("balance").getValue().toString(),
                                        ds.child("userId").getValue().toString(),
                                        ds.child("MonthlyDetail").getValue().toString(),
                                        ds.child("milkRate").getValue().toString()
                                );
                                datalist.add(model);
                            } else {
                                if(UID.equals(ds.child("userId").getValue().toString())){
                                    MonthlyModel model = new MonthlyModel(ds.getKey(),
                                            ds.child("name").getValue().toString(),
                                            ds.child("contact").getValue().toString(),
                                            ds.child("balance").getValue().toString(),
                                            ds.child("userId").getValue().toString(),
                                            ds.child("MonthlyDetail").getValue().toString(),
                                            ds.child("milkRate").getValue().toString()
                                    );
                                    datalist.add(model);
                                }
                            }
                        } else {
                            if(ds.child("name").getValue().toString().trim().toLowerCase().contains(data.toLowerCase().trim())){
                                if(DashboardActivity.getRole().equals("admin")){
                                    MonthlyModel model = new MonthlyModel(ds.getKey(),
                                            ds.child("name").getValue().toString(),
                                            ds.child("contact").getValue().toString(),
                                            ds.child("balance").getValue().toString(),
                                            ds.child("userId").getValue().toString(),
                                            ds.child("MonthlyDetail").getValue().toString(),
                                            ds.child("milkRate").getValue().toString()
                                    );
                                    datalist.add(model);
                                } else {
                                    if(UID.equals(ds.child("userId").getValue().toString())){
                                        MonthlyModel model = new MonthlyModel(ds.getKey(),
                                                ds.child("name").getValue().toString(),
                                                ds.child("contact").getValue().toString(),
                                                ds.child("balance").getValue().toString(),
                                                ds.child("userId").getValue().toString(),
                                                ds.child("MonthlyDetail").getValue().toString(),
                                                ds.child("milkRate").getValue().toString()
                                        );
                                        datalist.add(model);
                                    }
                                }
                            }
                        }

                    }
                    if(datalist.size() > 0){
                        loader.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        notfoundContainer.setVisibility(View.GONE);
                        if(sortingStatus.equals("dsc")){
                            Collections.reverse(datalist);
                        }
                        MyAdapter adapter = new MyAdapter(getContext(),datalist);
                        listView.setAdapter(adapter);
                    } else {
                        loader.setVisibility(View.GONE);
                        listView.setVisibility(View.GONE);
                        notfoundContainer.setVisibility(View.VISIBLE);
                        if(!data.equals("")){
                            notfoundContainer.setVisibility(View.VISIBLE);
                        }
                    }
                    totalCount.setText(datalist.size()+" found");
                } else {
                    loader.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    notfoundContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void searchValidation(){
        String input = searchInput.getText().toString().trim();
        String regex = "^[a-zA-Z\\s]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(!matcher.matches()){
            searchInput.setError("Only text allowed!!!");
        } else {
            searchInput.setError(null);
            if(input.isEmpty()){
                notifyBar.setVisibility(View.GONE);
                fetchData("");
            } else {
                notifyBar.setVisibility(View.VISIBLE);
                fetchData(searchInput.getText().toString().trim());
            }
        }
    }

    class MyAdapter extends BaseAdapter {

        Context context;
        ArrayList<MonthlyModel> data;

        public MyAdapter(Context context, ArrayList<MonthlyModel> data) {
            this.context = context;
            this.data = data;
        }
        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            View customListItem = LayoutInflater.from(context).inflate(R.layout.persons_custom_listview,null);
            LinearLayout listItem;
            TextView sno, name, userName, userRole;
            CircleImageView image;
            ImageView menu;

            listItem = customListItem.findViewById(R.id.listItem);
            sno = customListItem.findViewById(R.id.sno);
            name = customListItem.findViewById(R.id.name);
            userName = customListItem.findViewById(R.id.userName);
            userRole = customListItem.findViewById(R.id.userRole);
            image = customListItem.findViewById(R.id.image);
            menu = customListItem.findViewById(R.id.menu);

            sno.setText(""+(i+1));
            name.setText(data.get(i).getName());

            if(DashboardActivity.getRole().equals("admin")){
                userName.setVisibility(View.VISIBLE);
                userRole.setVisibility(View.VISIBLE);
            } else {
                userName.setVisibility(View.GONE);
                userRole.setVisibility(View.GONE);
            }

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BillActivity.class);
                    intent.putExtra("MonthlyId",data.get(i).getId());
                    intent.putExtra("contact",data.get(i).getContact());
                    intent.putExtra("personName",data.get(i).getName());
                    startActivity(intent);
                }
            });

            menu.setVisibility(View.GONE);

            MainActivity.db.child("Users").child(data.get(i).getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        userName.setText(snapshot.child("name").getValue().toString());
                        userRole.setText(snapshot.child("role").getValue().toString());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            if(i==data.size()-1){
                customListItem.setPadding(customListItem.getPaddingLeft(), customListItem.getPaddingTop(),customListItem.getPaddingRight(), 30);
            }
            if(i==0){
                customListItem.setPadding(customListItem.getPaddingLeft(), 0,customListItem.getPaddingRight(), 0);
            }
            customListItem.setAlpha(0f);
            customListItem.animate().alpha(1f).setDuration(300).setStartDelay(i * 2).start();


            return customListItem;
        }
    }
}