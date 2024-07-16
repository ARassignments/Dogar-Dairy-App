package com.example.dogardairy.Screens;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
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
import com.example.dogardairy.Models.MonthlyDetailModel;
import com.example.dogardairy.Models.MonthlyModel;
import com.example.dogardairy.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MonthlySupplyDetailActivity extends AppCompatActivity {

    String MonthlyId, currentDate, contact;
    ListView listView;
    LinearLayout notfoundContainer;
    TextView totalQty, grandTotalAmount, balancedAmount, date;
    ImageView sendMessageBtn;
    FrameLayout sendWhatsappMessageBtn;
    Button addQuantityBtn, unpaidBtn;
    ArrayList<MonthlyDetailModel> datalist = new ArrayList<>();

    //    Add Quantity Dialog Elements
    Dialog addQtyDialog, unpaidDialog;
    TextView dateView, totalAmountUnpaid, balanceAmountUnpaid;
    TextInputEditText qtyInput, amountInput, itemNameInput, itemAmountInput, givenAmountInput;
    TextInputLayout qtyLayout, amountLayout, itemNameLayout, itemAmountLayout, givenAmountLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monthly_supply_detail);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        listView = findViewById(R.id.listView);
        notfoundContainer = findViewById(R.id.notfoundContainer);
        totalQty = findViewById(R.id.totalQty);
        grandTotalAmount = findViewById(R.id.grandTotalAmount);
        balancedAmount = findViewById(R.id.balancedAmount);
        date = findViewById(R.id.date);
        sendMessageBtn = findViewById(R.id.sendMessageBtn);
        sendWhatsappMessageBtn = findViewById(R.id.sendWhatsappMessageBtn);
        addQuantityBtn = findViewById(R.id.addQuantityBtn);
        unpaidBtn = findViewById(R.id.unpaidBtn);

        //date picker start
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/yyyy");
        currentDate = simpleDateFormat.format(calendar.getTime());
        date.setText(currentDate);
        //date picker end

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            MonthlyId = extras.getString("MonthlyId");
            contact = extras.getString("contact");
        }

        MainActivity.db.child("Monthly").child(MonthlyId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    balancedAmount.setText(snapshot.child("balance").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendWhatsappMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = "DOGER DAIRY \n This is your today's Milk report \n Total price of Milk is: Rs "+grandTotalAmount.getText().toString()+"/- \n Total quantity is: "+totalQty.getText().toString()+"Kg \n Your pending Balance: Rs "+balancedAmount.getText().toString()+"/-";

                PackageManager packageManager = getPackageManager();
                try {
                    packageManager.getPackageInfo("com.whatsapp",PackageManager.GET_ACTIVITIES);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone="+contact+"&text="+txt));
                    startActivity(intent);
                } catch (PackageManager.NameNotFoundException e){
                    Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                    alertdialog.setContentView(R.layout.dialog_error);
                    alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    alertdialog.getWindow().setGravity(Gravity.CENTER);
                    alertdialog.setCancelable(false);
                    alertdialog.setCanceledOnTouchOutside(false);
                    TextView message = alertdialog.findViewById(R.id.msgDialog);
                    message.setText("Something went wrong, message not sended!!!");
                    alertdialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertdialog.dismiss();
                        }
                    },3000);
                }
            }
        });

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String txt = "DOGER DAIRY \n This is your today's Milk report \n Total price of Milk is: Rs "+grandTotalAmount.getText().toString()+"/- \n Total quantity is: "+totalQty.getText().toString()+"Kg \n Your pending Balance: Rs "+balancedAmount.getText().toString()+"/-";

                try {
                    ArrayList<String> parts = MainActivity.mySmsManager.divideMessage(txt);
                    MainActivity.mySmsManager.sendMultipartTextMessage( ""+contact,null, parts,null,null);
                    Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                    alertdialog.setContentView(R.layout.dialog_success);
                    alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    alertdialog.getWindow().setGravity(Gravity.CENTER);
                    alertdialog.setCancelable(false);
                    alertdialog.setCanceledOnTouchOutside(false);
                    TextView message = alertdialog.findViewById(R.id.msgDialog);
                    message.setText("Message Sended Successfully!!!");
                    alertdialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertdialog.dismiss();
                        }
                    },3000);
                }
                catch (Exception exception){
                    Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                    alertdialog.setContentView(R.layout.dialog_error);
                    alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    alertdialog.getWindow().setGravity(Gravity.CENTER);
                    alertdialog.setCancelable(false);
                    alertdialog.setCanceledOnTouchOutside(false);
                    TextView message = alertdialog.findViewById(R.id.msgDialog);
                    message.setText("Something went wrong, message not sended!!!");
                    alertdialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertdialog.dismiss();
                        }
                    },3000);
                    Log.d("Message", "onClick: "+exception.getMessage());
                }
            }
        });

        unpaidBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unpaidDialog = new Dialog(MonthlySupplyDetailActivity.this);
                unpaidDialog.setContentView(R.layout.dialog_unpaid);
                unpaidDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                unpaidDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                unpaidDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                unpaidDialog.getWindow().setGravity(Gravity.CENTER);
                unpaidDialog.setCancelable(false);
                unpaidDialog.setCanceledOnTouchOutside(false);
                Button addDataBtn, cancelBtn;
                addDataBtn = unpaidDialog.findViewById(R.id.addDataBtn);
                cancelBtn = unpaidDialog.findViewById(R.id.cancelBtn);
                givenAmountLayout = unpaidDialog.findViewById(R.id.givenAmountLayout);
                givenAmountInput = unpaidDialog.findViewById(R.id.givenAmountInput);
                totalAmountUnpaid = unpaidDialog.findViewById(R.id.totalAmount);
                balanceAmountUnpaid = unpaidDialog.findViewById(R.id.balanceAmount);

                givenAmountInput.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        givenAmountValidation();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                totalAmountUnpaid.setText("Rs "+grandTotalAmount.getText().toString().trim()+"/-");
                balanceAmountUnpaid.setText("Rs "+balancedAmount.getText().toString().trim()+"/-");

                addDataBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unpaidValidation();
                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        unpaidDialog.dismiss();
                    }
                });

                unpaidDialog.show();
            }
        });

        addQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuantity();
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMonth();
            }
        });

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthlySupplyDetailActivity.super.onBackPressed();
            }
        });

        fetchData();
    }

    public void fetchData(){
        MainActivity.db.child("Monthly").child(MonthlyId).child("MonthlyDetail").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    datalist.clear();
                    double grandtotal = 0.0;
                    int grandqty = 0;
                    for (DataSnapshot ds: snapshot.getChildren()){
                        if(ds.child("month").getValue().toString().trim().equals(date.getText().toString().trim())){
                            MonthlyDetailModel model = new MonthlyDetailModel(ds.getKey(),
                                    ds.child("amount").getValue().toString(),
                                    ds.child("qty").getValue().toString(),
                                    ds.child("date").getValue().toString(),
                                    ds.child("month").getValue().toString(),
                                    ds.child("itemName").getValue().toString(),
                                    ds.child("itemAmount").getValue().toString(),
                                    ds.child("totalAmount").getValue().toString()
                            );
                            datalist.add(model);
                            grandtotal += Double.parseDouble(ds.child("totalAmount").getValue().toString());
                            grandqty += Integer.parseInt(ds.child("qty").getValue().toString());
                        }
                    }
                    if(datalist.size() > 0){
                        listView.setVisibility(View.VISIBLE);
                        notfoundContainer.setVisibility(View.GONE);
                        Collections.reverse(datalist);
                        MyAdapter adapter = new MyAdapter(MonthlySupplyDetailActivity.this,datalist);
                        listView.setAdapter(adapter);
                        grandTotalAmount.setText(""+grandtotal);
                        totalQty.setText(""+grandqty);
                    } else {
                        listView.setVisibility(View.GONE);
                        notfoundContainer.setVisibility(View.VISIBLE);
                        grandTotalAmount.setText("0");
                        totalQty.setText("0");
                    }
                } else {
                    listView.setVisibility(View.GONE);
                    notfoundContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void selectMonth(){
        Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
        alertdialog.setContentView(R.layout.dialog_calendar);
        alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertdialog.getWindow().setGravity(Gravity.CENTER);
        alertdialog.setCancelable(false);
        alertdialog.setCanceledOnTouchOutside(false);

        DatePicker datePicker = alertdialog.findViewById(R.id.datePicker);
        Button selectBtn, cancelBtn;
        selectBtn = alertdialog.findViewById(R.id.selectBtn);
        cancelBtn = alertdialog.findViewById(R.id.cancelBtn);

        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedDate = (datePicker.getMonth()+1)+"/"+datePicker.getYear();
                date.setText(selectedDate);
                fetchData();
                alertdialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertdialog.dismiss();
            }
        });

        alertdialog.show();
    }

    public void addQuantity(){
        addQtyDialog = new Dialog(MonthlySupplyDetailActivity.this);
        addQtyDialog.setContentView(R.layout.dialog_add_quantity);
        addQtyDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        addQtyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addQtyDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        addQtyDialog.getWindow().setGravity(Gravity.CENTER);
        addQtyDialog.setCancelable(false);
        addQtyDialog.setCanceledOnTouchOutside(false);
        Button addDataBtn, cancelBtn, addOtherItemBtn, clearOtherItemBtn;
        addOtherItemBtn = addQtyDialog.findViewById(R.id.addOtherItemBtn);
        clearOtherItemBtn = addQtyDialog.findViewById(R.id.clearOtherItemBtn);
        addDataBtn = addQtyDialog.findViewById(R.id.addDataBtn);
        cancelBtn = addQtyDialog.findViewById(R.id.cancelBtn);
        dateView = addQtyDialog.findViewById(R.id.dateView);
        qtyLayout = addQtyDialog.findViewById(R.id.qtyLayout);
        amountLayout = addQtyDialog.findViewById(R.id.amountLayout);
        itemNameLayout = addQtyDialog.findViewById(R.id.itemNameLayout);
        itemAmountLayout = addQtyDialog.findViewById(R.id.itemAmountLayout);
        qtyInput = addQtyDialog.findViewById(R.id.qtyInput);
        amountInput = addQtyDialog.findViewById(R.id.amountInput);
        itemNameInput = addQtyDialog.findViewById(R.id.itemNameInput);
        itemAmountInput = addQtyDialog.findViewById(R.id.itemAmountInput);

        // current date picker
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM");
        String dateTime = simpleDateFormat.format(calendar.getTime());
        dateView.setText(dateTime);
        // current date picker

        qtyInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                qtyValidation();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                amountValidation();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        itemAmountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                itemAmountValidation();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        addDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQuantityValidation();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addQtyDialog.dismiss();
            }
        });

        clearOtherItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemNameInput.setText(null);
                itemAmountInput.setText(null);
            }
        });

        addQtyDialog.show();
    }

    public boolean qtyValidation(){
        String input = qtyInput.getText().toString().trim();
        String regex = "^[0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            qtyLayout.setError("Quantity is Required!!!");
            return false;
        } else if(!matcher.matches()){
            qtyLayout.setError("Only Digits Allowed!!!");
            return false;
        } else {
            qtyLayout.setError(null);
            return true;
        }
    }

    public boolean amountValidation(){
        String input = amountInput.getText().toString().trim();
        String regex = "^[0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            amountLayout.setError("Amount is Required!!!");
            return false;
        } else if(!matcher.matches()){
            amountLayout.setError("Only Digits Allowed!!!");
            return false;
        } else {
            amountLayout.setError(null);
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
            itemAmountLayout.setError("Only Digits Allowed!!!");
            return false;
        } else {
            itemAmountLayout.setError(null);
            return true;
        }
    }

    private void addQuantityValidation() {
        boolean qtyErr = false, amountErr = false, itemAmountErr = false;
        qtyErr = qtyValidation();
        amountErr = amountValidation();

        if(!itemNameInput.getText().toString().trim().equals("")){
            itemAmountErr = itemAmountValidation();
            if((qtyErr && amountErr && itemAmountErr) == true){
                quantityDataUpload();
            }
        } else {
            if((qtyErr && amountErr) == true){
                quantityDataUpload();
            }
        }

    }

    public void quantityDataUpload(){
        Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
        alertdialog.setContentView(R.layout.dialog_success);
        alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alertdialog.getWindow().setGravity(Gravity.CENTER);
        alertdialog.setCancelable(false);
        alertdialog.setCanceledOnTouchOutside(false);
        TextView message = alertdialog.findViewById(R.id.msgDialog);
        message.setText("Quantity Added Successfully!!!");
        alertdialog.show();

        float amount, qty ,total, itemAmount;
        String balance;
        amount = Float.parseFloat(amountInput.getText().toString().trim());
        qty = Float.parseFloat(qtyInput.getText().toString().trim());
        total = amount * qty;
        if(!itemNameInput.getText().toString().trim().equals("")){
            itemAmount = Float.parseFloat(itemAmountInput.getText().toString().trim());
            total += itemAmount;
            HashMap<String, String> mydata = new HashMap<String, String>();
            mydata.put("amount", amountInput.getText().toString().trim());
            mydata.put("qty", qtyInput.getText().toString().trim());
            mydata.put("date", dateView.getText().toString().trim());
            mydata.put("month", currentDate);
            mydata.put("itemName", itemNameInput.getText().toString().trim());
            mydata.put("itemAmount", itemAmountInput.getText().toString().trim());
            mydata.put("totalAmount", String.valueOf(total));
            MainActivity.db.child("Monthly").child(MonthlyId).child("MonthlyDetail").push().setValue(mydata);
        } else {
            HashMap<String, String> mydata = new HashMap<String, String>();
            mydata.put("amount", amountInput.getText().toString().trim());
            mydata.put("qty", qtyInput.getText().toString().trim());
            mydata.put("date", dateView.getText().toString().trim());
            mydata.put("month", currentDate);
            mydata.put("itemName", "");
            mydata.put("itemAmount", "0");
            mydata.put("totalAmount", String.valueOf(total));
            MainActivity.db.child("Monthly").child(MonthlyId).child("MonthlyDetail").push().setValue(mydata);
        }
        balance = String.valueOf(Double.parseDouble(balancedAmount.getText().toString().trim()) + Double.parseDouble(String.valueOf(total)));
        MainActivity.db.child("Monthly").child(MonthlyId).child("balance").setValue(balance);
        balancedAmount.setText(balance);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                alertdialog.dismiss();
                addQtyDialog.dismiss();
            }
        },2000);
    }

    public boolean givenAmountValidation(){
        String input = givenAmountInput.getText().toString().trim();
        String regex = "^[0-9]*$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if(input.equals("")){
            givenAmountLayout.setError("Given Amount is Required!!!");
            return false;
        } else if(!matcher.matches()){
            givenAmountLayout.setError("Only Digits Allowed!!!");
            return false;
        } else {
            givenAmountLayout.setError(null);
            return true;
        }
    }

    private void unpaidValidation() {
        boolean givenAmountErr = false;
        givenAmountErr = givenAmountValidation();
        if((givenAmountErr) == true){

            double totalAmount = Double.parseDouble(grandTotalAmount.getText().toString().trim());
            double balance = Double.parseDouble(balancedAmount.getText().toString().trim());

            String mybalance = String.valueOf(balance - Double.parseDouble( givenAmountInput.getText().toString()));
            if((Float.parseFloat(givenAmountInput.getText().toString()) > Float.parseFloat(""+balance))){
                Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                alertdialog.setContentView(R.layout.dialog_error);
                alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                alertdialog.getWindow().setGravity(Gravity.CENTER);
                alertdialog.setCancelable(false);
                alertdialog.setCanceledOnTouchOutside(false);
                TextView message = alertdialog.findViewById(R.id.msgDialog);
                message.setText("Enter Valid Amount!!!");
                alertdialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertdialog.dismiss();
                    }
                },3000);
            } else if(Float.parseFloat(givenAmountInput.getText().toString()) <= Float.parseFloat(""+balance)) {
                balancedAmount.setText(mybalance);
                balanceAmountUnpaid.setText(mybalance);
                MainActivity.db.child("Monthly").child(MonthlyId).child("balance").setValue(mybalance);
                Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                alertdialog.setContentView(R.layout.dialog_success);
                alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                alertdialog.getWindow().setGravity(Gravity.CENTER);
                alertdialog.setCancelable(false);
                alertdialog.setCanceledOnTouchOutside(false);
                TextView message = alertdialog.findViewById(R.id.msgDialog);
                message.setText("Paid Successfully!!!");
                alertdialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertdialog.dismiss();
                        unpaidDialog.dismiss();
                    }
                },2000);
            }
        }

    }

    class MyAdapter extends BaseAdapter {

        Context context;
        ArrayList<MonthlyDetailModel> data;

        public MyAdapter(Context context, ArrayList<MonthlyDetailModel> data) {
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
            View customListItem = LayoutInflater.from(context).inflate(R.layout.monthly_supply_custom_listview,null);
            TextView sno, date, qty, amount;
            ImageView delete;

            sno = customListItem.findViewById(R.id.sno);
            date = customListItem.findViewById(R.id.date);
            qty = customListItem.findViewById(R.id.qty);
            amount = customListItem.findViewById(R.id.amount);
            delete = customListItem.findViewById(R.id.delete);

            sno.setText(""+(i+1));
            date.setText(data.get(i).getDate());
            qty.setText(data.get(i).getQty()+"kg");
            amount.setText("Rs "+data.get(i).getTotalAmount()+"/-");

            delete.setOnClickListener(new View.OnClickListener() {
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
                                double totalAmount = Double.parseDouble(data.get(i).getTotalAmount());
                                double balance = Double.parseDouble(balancedAmount.getText().toString().trim());

                                if(totalAmount<balance){
                                    MainActivity.db.child("Monthly").child(MonthlyId).child("MonthlyDetail").child(data.get(i).getId()).removeValue();
                                    MainActivity.db.child("Monthly").child(MonthlyId).child("balance").setValue(""+(balance-totalAmount));
                                    balancedAmount.setText(""+(balance-totalAmount));
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
                                        }
                                    },3000);
                                } else {
                                    Dialog dialog = new Dialog(context);
                                    dialog.setContentView(R.layout.dialog_error);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.setCancelable(false);
                                    TextView msg = dialog.findViewById(R.id.msgDialog);
                                    msg.setText("You can't delete because balance is low!!!");
                                    dialog.show();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                        }
                                    },3000);
                                }


                            }
                        }
                    });

                    actiondialog.show();
                }
            });

            return customListItem;
        }
    }
}