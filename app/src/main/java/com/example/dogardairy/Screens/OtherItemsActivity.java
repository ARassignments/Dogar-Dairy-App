package com.example.dogardairy.Screens;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogardairy.MainActivity;
import com.example.dogardairy.Models.ItemsModel;
import com.example.dogardairy.Models.MonthlyModel;
import com.example.dogardairy.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherItemsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static String UID = "";
    ListView listView;
    LinearLayout loader, notifyBar, notfoundContainer;
    EditText searchInput;
    TextView searchedWord, totalCount;
    ExtendedFloatingActionButton addBtn;
    ArrayList<ItemsModel> datalist = new ArrayList<>();

    //    Dialog Elements
    Dialog itemDialog;
    Button cancelBtn, addDataBtn;
    TextInputEditText itemNameInput, itemAmountInput;
    TextInputLayout itemNameLayout, itemAmountLayout;
    TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_other_items);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreferences = getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        listView = findViewById(R.id.listView);
        notifyBar = findViewById(R.id.notifyBar);
        notfoundContainer = findViewById(R.id.notfoundContainer);
        loader = findViewById(R.id.loader);
        searchedWord = findViewById(R.id.searchedWord);
        totalCount = findViewById(R.id.totalCount);
        searchInput = findViewById(R.id.searchInput);
        addBtn = findViewById(R.id.addBtn);

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
        }

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
                itemsForm("add","");
            }
        });

        fetchData("");

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OtherItemsActivity.super.onBackPressed();
            }
        });
    }

    public void fetchData(String data){
        MainActivity.db.child("OtherItems").child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    datalist.clear();
                    for (DataSnapshot ds: snapshot.getChildren()){
                        if(data.equals("")){
                            ItemsModel model = new ItemsModel(ds.getKey(),
                                    ds.child("name").getValue().toString(),
                                    ds.child("amount").getValue().toString()
                            );
                            datalist.add(model);
                        } else {
                            if(ds.child("name").getValue().toString().trim().toLowerCase().contains(data.toLowerCase().trim())){
                                ItemsModel model = new ItemsModel(ds.getKey(),
                                        ds.child("name").getValue().toString(),
                                        ds.child("amount").getValue().toString()
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
                        MyAdapter adapter = new MyAdapter(OtherItemsActivity.this,datalist);
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

    public void itemsForm(String purpose, String productId){
        itemDialog = new Dialog(OtherItemsActivity.this);
        itemDialog.setContentView(R.layout.dialog_add_other_items);
        itemDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        itemDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        itemDialog.getWindow().setGravity(Gravity.CENTER);
        itemDialog.setCancelable(false);
        itemDialog.setCanceledOnTouchOutside(false);
        cancelBtn = itemDialog.findViewById(R.id.cancelBtn);
        addDataBtn = itemDialog.findViewById(R.id.addDataBtn);
        title = itemDialog.findViewById(R.id.title);
        itemNameInput = itemDialog.findViewById(R.id.itemNameInput);
        itemAmountInput = itemDialog.findViewById(R.id.itemAmountInput);
        itemNameLayout = itemDialog.findViewById(R.id.itemNameLayout);
        itemAmountLayout = itemDialog.findViewById(R.id.itemAmountLayout);

        itemNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                itemNameValidation();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        itemAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                itemAmountValidation();
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
                itemDialog.dismiss();
            }
        });

        if(purpose.equals("edit")){
            title.setText("Edit Book");
            MainActivity.db.child("OtherItems").child(UID).child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        itemNameInput.setText(snapshot.child("name").getValue().toString().trim());
                        itemAmountInput.setText(snapshot.child("amount").getValue().toString().trim());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        itemDialog.show();
    }

    public boolean itemNameValidation(){
        String input = itemNameInput.getText().toString().trim();
        String regex = "^[a-zA-Z\\s]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            itemNameLayout.setError("Item Name is Required!!!");
            return false;
        } else if(input.length() < 3){
            itemNameLayout.setError("Item Name at least 3 Characters!!!");
            return false;
        } else if(!matcher.matches()){
            itemNameLayout.setError("Only text allowed!!!");
            return false;
        } else {
            itemNameLayout.setError(null);
            return true;
        }
    }

    public boolean itemAmountValidation(){
        String input = itemAmountInput.getText().toString().trim();
        String regex = "^[0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            itemAmountLayout.setError("Item Amount is Required!!!");
            return false;
        } else if(!matcher.matches()){
            itemAmountLayout.setError("Only digits allowed!!!");
            return false;
        } else {
            itemAmountLayout.setError(null);
            return true;
        }
    }

    private void validation(String purpose, String productId) {
        boolean itemNameErr = false, itemAmountErr = false;
        itemNameErr = itemNameValidation();
        itemAmountErr = itemAmountValidation();

        if((itemNameErr && itemAmountErr) == true){
            items(purpose, productId);
        }
    }

    private void items(String purpose, String productId){
        if(MainActivity.connectionCheck(OtherItemsActivity.this)){
            Dialog alertdialog = new Dialog(OtherItemsActivity.this);
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
                mydata.put("name", itemNameInput.getText().toString().trim());
                mydata.put("amount", itemAmountInput.getText().toString().trim());
                MainActivity.db.child("OtherItems").child(UID).push().setValue(mydata);
                message.setText("Item Added Successfully!!!");
            } else if(purpose.equals("edit")){
                MainActivity.db.child("OtherItems").child(UID).child(productId).child("name").setValue(itemNameInput.getText().toString().trim());
                MainActivity.db.child("OtherItems").child(UID).child(productId).child("amount").setValue(itemAmountInput.getText().toString().trim());
                message.setText("Item Edited Successfully!!!");
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertdialog.dismiss();
                    itemDialog.dismiss();
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
        ArrayList<ItemsModel> data;

        public MyAdapter(Context context, ArrayList<ItemsModel> data) {
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
            View customListItem = LayoutInflater.from(context).inflate(R.layout.items_custom_listview,null);
            TextView sno, itemName, itemAmount;
            ImageView menu;

            sno = customListItem.findViewById(R.id.sno);
            itemName = customListItem.findViewById(R.id.itemName);
            itemAmount = customListItem.findViewById(R.id.itemAmount);
            menu = customListItem.findViewById(R.id.menu);

            sno.setText(""+(i+1));
            itemName.setText(data.get(i).getName());
            itemAmount.setText("Rs "+data.get(i).getAmount()+"/-");


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
                            itemsForm("edit",""+data.get(i).getId());
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
                                        MainActivity.db.child("OtherItems").child(UID).child(data.get(i).getId()).removeValue();
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