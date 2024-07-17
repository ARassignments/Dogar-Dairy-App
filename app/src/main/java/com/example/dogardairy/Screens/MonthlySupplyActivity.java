package com.example.dogardairy.Screens;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogardairy.MainActivity;
import com.example.dogardairy.Models.MonthlyModel;
import com.example.dogardairy.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MonthlySupplyActivity extends AppCompatActivity {

    ListView listView;
    LinearLayout loader, notifyBar, notfoundContainer;
    EditText searchInput;
    TextView searchedWord, totalCount;
    ExtendedFloatingActionButton addBtn;
    ArrayList<MonthlyModel> datalist = new ArrayList<>();

    //    Dialog Elements
    Dialog personDialog;
    Button cancelBtn, addDataBtn;
    TextInputEditText nameInput, contactInput;
    TextInputLayout nameLayout, contactLayout;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monthly_supply);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        listView = findViewById(R.id.listView);
        notifyBar = findViewById(R.id.notifyBar);
        notfoundContainer = findViewById(R.id.notfoundContainer);
        loader = findViewById(R.id.loader);
        searchedWord = findViewById(R.id.searchedWord);
        totalCount = findViewById(R.id.totalCount);
        searchInput = findViewById(R.id.searchInput);
        addBtn = findViewById(R.id.addBtn);

        //date picker start
        Calendar calendar = Calendar.getInstance();
        String currentDate = DateFormat.getDateInstance(DateFormat.FULL).format(calendar.getTime());
        TextView textViewDate = findViewById(R.id.date);
        textViewDate.setText(currentDate);
        //date picker end

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

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personForm("add","");
            }
        });

        fetchData("");

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthlySupplyActivity.super.onBackPressed();
            }
        });
    }

    public void fetchData(String data){
        MainActivity.db.child("Monthly").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    datalist.clear();
                    for (DataSnapshot ds: snapshot.getChildren()){
                        if(data.equals("")){
                            MonthlyModel model = new MonthlyModel(ds.getKey(),
                                    ds.child("name").getValue().toString(),
                                    ds.child("contact").getValue().toString(),
                                    ds.child("balance").getValue().toString(),
                                    ds.child("MonthlyDetail").getValue().toString()
                            );
                            datalist.add(model);
                        } else {
                            if(ds.child("name").getValue().toString().trim().toLowerCase().contains(data.toLowerCase().trim())){
                                MonthlyModel model = new MonthlyModel(ds.getKey(),
                                        ds.child("name").getValue().toString(),
                                        ds.child("contact").getValue().toString(),
                                        ds.child("balance").getValue().toString(),
                                        ds.child("MonthlyDetail").getValue().toString()
                                );
                                datalist.add(model);
                            }
                        }

                    }
                    if(datalist.size() > 0){
                        loader.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        notfoundContainer.setVisibility(View.GONE);
                        Collections.reverse(datalist);
                        MyAdapter adapter = new MyAdapter(MonthlySupplyActivity.this,datalist);
                        listView.setAdapter(adapter);
                    } else {
                        listView.setVisibility(View.GONE);
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

    public void personForm(String purpose, String productId){
        personDialog = new Dialog(MonthlySupplyActivity.this);
        personDialog.setContentView(R.layout.dialog_add_persons);
        personDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        personDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        personDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        personDialog.getWindow().setGravity(Gravity.CENTER);
        personDialog.setCancelable(false);
        personDialog.setCanceledOnTouchOutside(false);
        cancelBtn = personDialog.findViewById(R.id.cancelBtn);
        addDataBtn = personDialog.findViewById(R.id.addDataBtn);
        title = personDialog.findViewById(R.id.title);
        nameInput = personDialog.findViewById(R.id.nameInput);
        contactInput = personDialog.findViewById(R.id.contactInput);
        nameLayout = personDialog.findViewById(R.id.nameLayout);
        contactLayout = personDialog.findViewById(R.id.contactLayout);

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
        contactInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                contactValidation();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        addDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation(purpose, productId);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                personDialog.dismiss();
            }
        });

        if(purpose.equals("edit")){
            title.setText("Edit Person");
            MainActivity.db.child("Monthly").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        nameInput.setText(snapshot.child("name").getValue().toString().trim());
                        contactInput.setText(snapshot.child("contact").getValue().toString().trim());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        personDialog.show();
    }

    public boolean nameValidation(){
        String input = nameInput.getText().toString().trim();
        String regex = "^[a-zA-Z\\s]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            nameLayout.setError("Person Name is Required!!!");
            return false;
        } else if(input.length() < 3){
            nameLayout.setError("Person Name at least 3 Characters!!!");
            return false;
        } else if(!matcher.matches()){
            nameLayout.setError("Only text allowed!!!");
            return false;
        } else {
            nameLayout.setError(null);
            return true;
        }
    }

    public boolean contactValidation(){
        String input = contactInput.getText().toString().trim();
        String regex = "^[0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            contactLayout.setError("Person Contact No is Required!!!");
            return false;
        } else if(input.length() < 11){
            contactLayout.setError("Person Contact No at least 11 digits!!!");
            return false;
        } else if(!matcher.matches()){
            contactLayout.setError("Only digits allowed!!!");
            return false;
        } else {
            contactLayout.setError(null);
            return true;
        }
    }

    private void validation(String purpose, String productId) {
        boolean nameErr = false, contactErr = false;
        nameErr = nameValidation();
        contactErr = contactValidation();

        if((nameErr && contactErr) == true){
            persons(purpose, productId);
        }
    }

    private void persons(String purpose, String productId){
        if(MainActivity.connectionCheck(MonthlySupplyActivity.this)){
            Dialog alertdialog = new Dialog(MonthlySupplyActivity.this);
            alertdialog.setContentView(R.layout.dialog_success);
            alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertdialog.getWindow().setGravity(Gravity.CENTER);
            alertdialog.setCancelable(false);
            alertdialog.setCanceledOnTouchOutside(false);
            TextView message = alertdialog.findViewById(R.id.msgDialog);
            alertdialog.show();

            if(purpose.equals("add")){
                HashMap<String, String> mydata = new HashMap<String, String>();
                mydata.put("name", nameInput.getText().toString().trim());
                mydata.put("contact", contactInput.getText().toString().trim());
                mydata.put("balance", "0");
                mydata.put("MonthlyDetail", "");
                MainActivity.db.child("Monthly").push().setValue(mydata);
                message.setText("Person Added Successfully!!!");
            } else if(purpose.equals("edit")){
                MainActivity.db.child("Monthly").child(productId).child("name").setValue(nameInput.getText().toString().trim());
                MainActivity.db.child("Monthly").child(productId).child("contact").setValue(contactInput.getText().toString().trim());
                message.setText("Person Edited Successfully!!!");
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertdialog.dismiss();
                    personDialog.dismiss();
                }
            },2000);
        }
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
            TextView sno, name;
            CircleImageView image;
            ImageView menu;

            listItem = customListItem.findViewById(R.id.listItem);
            sno = customListItem.findViewById(R.id.sno);
            name = customListItem.findViewById(R.id.name);
            image = customListItem.findViewById(R.id.image);
            menu = customListItem.findViewById(R.id.menu);

            sno.setText(""+(i+1));
            name.setText(data.get(i).getName());

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, MonthlySupplyDetailActivity.class);
                    intent.putExtra("MonthlyId",data.get(i).getId());
                    intent.putExtra("balance",data.get(i).getBalance());
                    intent.putExtra("contact",data.get(i).getContact());
                    intent.putExtra("personName",data.get(i).getName());
                    startActivity(intent);
                }
            });

            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog loaddialog = new Dialog(context);
                    loaddialog.setContentView(R.layout.dialog_actions);
                    loaddialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    loaddialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    loaddialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    loaddialog.getWindow().setGravity(Gravity.CENTER);
//                    loaddialog.setCancelable(false);
//                    loaddialog.setCanceledOnTouchOutside(false);
                    Button editBtn, deleteBtn;
                    editBtn = loaddialog.findViewById(R.id.editBtn);
                    deleteBtn = loaddialog.findViewById(R.id.deleteBtn);
                    editBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            personForm("edit",""+data.get(i).getId());
                            loaddialog.dismiss();
                        }
                    });

                    deleteBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Dialog actiondialog = new Dialog(context);
                            actiondialog.setContentView(R.layout.dialog_confirm);
                            actiondialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                            actiondialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                            actiondialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            actiondialog.getWindow().setGravity(Gravity.CENTER);
                            actiondialog.setCancelable(false);
                            actiondialog.setCanceledOnTouchOutside(false);
                            Button cancelBtn, yesBtn;
                            yesBtn = actiondialog.findViewById(R.id.yesBtn);
                            cancelBtn = actiondialog.findViewById(R.id.cancelBtn);
                            cancelBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    actiondialog.dismiss();
                                }
                            });
                            yesBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(MainActivity.connectionCheck(context)){
                                        MainActivity.db.child("Monthly").child(data.get(i).getId()).removeValue();
                                        Dialog dialog = new Dialog(context);
                                        dialog.setContentView(R.layout.dialog_success);
                                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                        dialog.getWindow().setGravity(Gravity.CENTER);
                                        dialog.setCanceledOnTouchOutside(false);
                                        dialog.setCancelable(false);
                                        TextView msg = dialog.findViewById(R.id.msgDialog);
                                        msg.setText("Deleted Successfully!!!");
                                        dialog.show();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();
                                                actiondialog.dismiss();
                                                loaddialog.dismiss();
                                            }
                                        },3000);
                                    }
                                }
                            });

                            actiondialog.show();
                        }
                    });

                    loaddialog.show();
                }
            });

            return customListItem;
        }
    }
}