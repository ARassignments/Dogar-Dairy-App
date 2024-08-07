package com.example.dogardairy.Screens;

import static com.itextpdf.io.font.FontProgramFactory.*;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.dogardairy.MainActivity;
import android.Manifest;
import com.example.dogardairy.Models.ItemsModel;
import com.example.dogardairy.Models.MonthlyDetailModel;
import com.example.dogardairy.Models.MonthlyModel;
import com.example.dogardairy.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.hdodenhof.circleimageview.CircleImageView;

public class MonthlySupplyDetailActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    static String UID = "";
    static String sortingStatus = "dsc";
    String MonthlyId, currentDate, contact, personName, currentMonth, currentReportDate;
    ListView listView;
    LinearLayout notfoundContainer;
    TextView totalQty, grandTotalAmount, balancedAmount, date, appBarTitle;
    ImageView sendMessageBtn, sortBtn, callBtn, fullListBtn;
    FrameLayout sendWhatsappMessageBtn;
    Button addQuantityBtn, unpaidBtn;
    ArrayList<MonthlyDetailModel> datalist = new ArrayList<>();
    ArrayList<MonthlyDetailModel> datalistReports = new ArrayList<>();
    ArrayList<ItemsModel> datalistItems = new ArrayList<>();

    //    Dialog Elements
    Dialog addQtyDialog, unpaidDialog, itemsDialog, loaderDialog, reportDialog;
    TextView dateView, totalAmountUnpaid, balanceAmountUnpaid, totalAmountDialog;
    TextInputEditText qtyInput, amountInput, itemNameInput, itemAmountInput, givenAmountInput;
    TextInputLayout qtyLayout, amountLayout, itemNameLayout, itemAmountLayout, givenAmountLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monthly_supply_detail);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        sharedPreferences = getSharedPreferences("myData",MODE_PRIVATE);
        editor = sharedPreferences.edit();

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
        appBarTitle = findViewById(R.id.appBarTitle);
        sortBtn = findViewById(R.id.sortBtn);
        callBtn = findViewById(R.id.callBtn);
        fullListBtn = findViewById(R.id.fullListBtn);

        if(!sharedPreferences.getString("UID","").equals("")){
            UID = sharedPreferences.getString("UID","").toString();
        }

        //date picker start
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("M/yyyy");
        currentDate = simpleDateFormat.format(calendar.getTime());
        currentMonth = new SimpleDateFormat("MMMM, yyyy").format(calendar.getTime());
        currentReportDate = new SimpleDateFormat("MMMM, yyyy").format(calendar.getTime());
        date.setText(currentDate);
        //date picker end

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            MonthlyId = extras.getString("MonthlyId");
            contact = extras.getString("contact");
            personName = extras.getString("personName");
        }

        appBarTitle.setText(personName);

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

                String balance = balancedAmount.getText().toString().trim();
                if(balancedAmount.getText().toString().trim().equals(grandTotalAmount.getText().toString().trim())){
                    balance = "0";
                }

                String txt = "                ATTARI DAIRY \n" +
                        "***** Monthly Milk Report ***** \n" +
                        "Month: " +currentMonth+"\n"+
                        "Name: "+personName+"\n" +
                        "_____________________________________\n\n";

                for(int i=0;i<datalistReports.size();i++){
                    if(datalistReports.get(i).getItemName().equals("")){
                        int total = Integer.parseInt(datalistReports.get(i).getQty())*Integer.parseInt(datalistReports.get(i).getAmount());
                        txt += "     "+(i+1)+".  "+datalistReports.get(i).getDate()+"    "+datalistReports.get(i).getQty()+"kg     Rs "+total+"/- \n";
                    } else {
                        int total = Integer.parseInt(datalistReports.get(i).getQty())*Integer.parseInt(datalistReports.get(i).getAmount());
                        txt += "     "+(i+1)+".  "+datalistReports.get(i).getDate()+"    "+datalistReports.get(i).getQty()+"kg     Rs "+total+"/- \n" +
                                "    ↳   Item:     "+datalistReports.get(i).getItemName()+"    Rs "+datalistReports.get(i).getItemAmount()+"/- \n";
                    }
                }

                txt += "_____________________________________\n\n" +
                        "Milk Rate is: Rs "+DashboardActivity.getMilkRate()+"/- \n" +
                        "Total Price of Milk is: Rs "+grandTotalAmount.getText().toString().trim()+"/- \n" +
                        "Total quantity is: "+totalQty.getText().toString().trim()+"Kg \n" +
                        "Your pending Balance: Rs "+balance+"/-\n" +
                        " ----- JazakAllah -----";

//                String txt = "                ATTARI DAIRY \n" +
//                        "***** Monthly Milk Report ***** \n" +
//                        "Name: "+personName+"\n" +
//                        "Milk Rate is: Rs "+DashboardActivity.getMilkRate()+"/- \n" +
//                        "Total Price of Milk is: Rs "+grandTotalAmount.getText().toString().trim()+"/- \n" +
//                        "Total quantity is: "+totalQty.getText().toString().trim()+"Kg \n" +
//                        "Your pending Balance: Rs "+balance+"/-\n" +
//                        " ----- JazakAllah -----";

                if(datalist.size() == 0){
                    Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                    alertdialog.setContentView(R.layout.dialog_error);
                    alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    alertdialog.getWindow().setGravity(Gravity.CENTER);
                    alertdialog.setCancelable(false);
                    alertdialog.setCanceledOnTouchOutside(false);
                    TextView message = alertdialog.findViewById(R.id.msgDialog);
                    message.setText("Message not sended because no data found!!!");
                    alertdialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertdialog.dismiss();
                        }
                    },3000);
                } else {
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
            }
        });

        sendMessageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (ContextCompat.checkSelfPermission(MonthlySupplyDetailActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(MonthlySupplyDetailActivity.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(MonthlySupplyDetailActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {

                        // If any permission is not granted, request them
                        ActivityCompat.requestPermissions(MonthlySupplyDetailActivity.this, new String[]{
                                Manifest.permission.SEND_SMS,
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS
                        }, 123);
                    } else {
                        String balance = balancedAmount.getText().toString().trim();
                        if(balancedAmount.getText().toString().trim().equals(grandTotalAmount.getText().toString().trim())){
                            balance = "0";
                        }

                        String txt = "                ATTARI DAIRY \n" +
                                "***** Monthly Milk Report ***** \n" +
                                "Month: " +currentMonth+"\n"+
                                "Name: "+personName+"\n" +
                                "_____________________________________\n\n";

                        for(int i=0;i<datalistReports.size();i++){
                            if(datalistReports.get(i).getItemName().equals("")){
                                int total = Integer.parseInt(datalistReports.get(i).getQty())*Integer.parseInt(datalistReports.get(i).getAmount());
                                txt += "     "+(i+1)+".  "+datalistReports.get(i).getDate()+"    "+datalistReports.get(i).getQty()+"kg     Rs "+total+"/- \n";
                            } else {
                                int total = Integer.parseInt(datalistReports.get(i).getQty())*Integer.parseInt(datalistReports.get(i).getAmount());
                                txt += "     "+(i+1)+".  "+datalistReports.get(i).getDate()+"    "+datalistReports.get(i).getQty()+"kg     Rs "+total+"/- \n" +
                                        "    ↳   Item:     "+datalistReports.get(i).getItemName()+"    Rs "+datalistReports.get(i).getItemAmount()+"/- \n";
                            }
                        }

                        txt += "_____________________________________\n\n" +
                                "Milk Rate is: Rs "+DashboardActivity.getMilkRate()+"/- \n" +
                                "Total Price of Milk is: Rs "+grandTotalAmount.getText().toString().trim()+"/- \n" +
                                "Total quantity is: "+totalQty.getText().toString().trim()+"Kg \n" +
                                "Your pending Balance: Rs "+balance+"/-\n" +
                                " ----- JazakAllah -----";

                        if(datalist.size() == 0){
                            Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                            alertdialog.setContentView(R.layout.dialog_error);
                            alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                            alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            alertdialog.getWindow().setGravity(Gravity.CENTER);
                            alertdialog.setCancelable(false);
                            alertdialog.setCanceledOnTouchOutside(false);
                            TextView message = alertdialog.findViewById(R.id.msgDialog);
                            message.setText("Message not sended because no data found!!!");
                            alertdialog.show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    alertdialog.dismiss();
                                }
                            },3000);
                        } else {
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
                            }
                        }
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

        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sorting();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + contact));
                startActivity(intent);
            }
        });

        fullListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(datalist.size() == 0){
                    Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                    alertdialog.setContentView(R.layout.dialog_error);
                    alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    alertdialog.getWindow().setGravity(Gravity.CENTER);
                    alertdialog.setCancelable(false);
                    alertdialog.setCanceledOnTouchOutside(false);
                    TextView message = alertdialog.findViewById(R.id.msgDialog);
                    message.setText("PDF not generated because no data found!!!");
                    alertdialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            alertdialog.dismiss();
                        }
                    },3000);
                } else {
                    reportDialog = new Dialog(MonthlySupplyDetailActivity.this);
                    reportDialog.setContentView(R.layout.dialog_pdf_report);
                    reportDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    reportDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    reportDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    reportDialog.getWindow().setGravity(Gravity.CENTER);
                    reportDialog.setCancelable(false);
                    reportDialog.setCanceledOnTouchOutside(false);
                    Button cancelBtn, shareBtn;
                    ImageView imageView;
                    cancelBtn = reportDialog.findViewById(R.id.cancelBtn);
                    shareBtn = reportDialog.findViewById(R.id.shareBtn);
                    imageView = reportDialog.findViewById(R.id.imageView);

                    showPdfPreview(generatePDF(datalistReports),imageView);

                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sharePDF(generatePDF(datalistReports));
                            reportDialog.dismiss();
                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            reportDialog.dismiss();
                        }
                    });
                }
            }
        });

        fetchData();
    }

    public void sorting(){
        if(sortingStatus.equals("asc")){
            sortingStatus = "dsc";
            sortBtn.setImageResource(R.drawable.deasscending_order);
        } else if(sortingStatus.equals("dsc")){
            sortingStatus = "asc";
            sortBtn.setImageResource(R.drawable.asscending_order);
        }
        fetchData();
    }

    public void fetchData(){
        MainActivity.db.child("Monthly").child(MonthlyId).child("MonthlyDetail").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    datalist.clear();
                    datalistReports.clear();
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
                            datalistReports.add(model);
                            grandtotal += Double.parseDouble(ds.child("totalAmount").getValue().toString());
                            grandqty += Integer.parseInt(ds.child("qty").getValue().toString());
                        }
                    }
                    if(datalist.size() > 0){
                        listView.setVisibility(View.VISIBLE);
                        notfoundContainer.setVisibility(View.GONE);
                        if(sortingStatus.equals("dsc")){
                            Collections.reverse(datalist);
                        }
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
                currentMonth = new DateFormatSymbols().getMonths()[datePicker.getMonth()] +", "+datePicker.getYear();
                currentReportDate = new DateFormatSymbols().getMonths()[datePicker.getMonth()] +", "+datePicker.getYear();
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
        totalAmountDialog = addQtyDialog.findViewById(R.id.totalAmountDialog);

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
                totalAmountDialog();
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
                totalAmountDialog();
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
                totalAmountDialog();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        MainActivity.db.child("Users").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    amountInput.setText(snapshot.child("milkRate").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addOtherItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemsDialog = new Dialog(MonthlySupplyDetailActivity.this);
                itemsDialog.setContentView(R.layout.dialog_other_items_list);
                itemsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                itemsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                itemsDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                itemsDialog.getWindow().setGravity(Gravity.CENTER);
                itemsDialog.setCancelable(false);
                itemsDialog.setCanceledOnTouchOutside(false);
                ListView listViewItems = itemsDialog.findViewById(R.id.listView);
                Button cancelBtnItems = itemsDialog.findViewById(R.id.cancelBtn);
                LinearLayout notfoundContainerItems = itemsDialog.findViewById(R.id.notfoundContainer);

                MainActivity.db.child("OtherItems").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            datalistItems.clear();
                            for (DataSnapshot ds: snapshot.getChildren()){
                                ItemsModel model = new ItemsModel(ds.getKey(),
                                        ds.child("name").getValue().toString(),
                                        ds.child("amount").getValue().toString()
                                );
                                datalistItems.add(model);
                            }
                            if(datalistItems.size() > 0){
                                listViewItems.setVisibility(View.VISIBLE);
                                notfoundContainerItems.setVisibility(View.GONE);
                                Collections.reverse(datalist);
                                MyAdapterItems adapter = new MyAdapterItems(MonthlySupplyDetailActivity.this,datalistItems);
                                listViewItems.setAdapter(adapter);
                            } else {
                                listViewItems.setVisibility(View.GONE);
                                notfoundContainerItems.setVisibility(View.VISIBLE);
                            }
                        } else {
                            listViewItems.setVisibility(View.GONE);
                            notfoundContainerItems.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                cancelBtnItems.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemsDialog.dismiss();
                    }
                });

                itemsDialog.show();
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
            mydata.put("amount", ""+((Integer.parseInt(qtyInput.getText().toString().trim()))*(Integer.parseInt(amountInput.getText().toString().trim()))));
            mydata.put("qty", qtyInput.getText().toString().trim());
            mydata.put("date", dateView.getText().toString().trim());
            mydata.put("month", currentDate);
            mydata.put("itemName", itemNameInput.getText().toString().trim());
            mydata.put("itemAmount", itemAmountInput.getText().toString().trim());
            mydata.put("totalAmount", String.valueOf(total));
            MainActivity.db.child("Monthly").child(MonthlyId).child("MonthlyDetail").push().setValue(mydata);
        } else {
            HashMap<String, String> mydata = new HashMap<String, String>();
            mydata.put("amount", ""+((Integer.parseInt(qtyInput.getText().toString().trim()))*(Integer.parseInt(amountInput.getText().toString().trim()))));
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

    public void totalAmountDialog(){
        if(itemNameInput.getText().toString().trim().equals("")){
            int total = Integer.parseInt("0"+qtyInput.getText().toString().trim())*Integer.parseInt("0"+amountInput.getText().toString().trim());
            totalAmountDialog.setText("Rs "+total+"/-");
        } else {
            int total = Integer.parseInt("0"+qtyInput.getText().toString().trim())*Integer.parseInt("0"+amountInput.getText().toString().trim())+Integer.parseInt("0"+itemAmountInput.getText().toString().trim());
            totalAmountDialog.setText("Rs "+total+"/-");
        }
    }

    public String generatePDF(ArrayList<MonthlyDetailModel> reportDataList){
        // Check and request storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
            }
        }
//        String path = getExternalFilesDir(null) + "/"+personName+" - ATTARI DAIRY - Monthly Milk Report - "+currentReportDate+".pdf";

        File file = new File(getExternalFilesDir(null), personName + " - ATTARI DAIRY - Monthly Milk Report - " + currentReportDate + ".pdf");
        String path = file.getAbsolutePath();

        String balance = balancedAmount.getText().toString().trim();
        if(balancedAmount.getText().toString().trim().equals(grandTotalAmount.getText().toString().trim())){
            balance = "0";
        }

//        if(sortingStatus.equals("dsc")){
//            Collections.reverse(reportDataList);
//        }
//        Collections.sort(reportDataList, new Comparator<MonthlyDetailModel>() {
//            @Override
//            public int compare(MonthlyDetailModel o1, MonthlyDetailModel o2) {
//                return o1.getDate().compareTo(o2.getDate());
//            }
//        });

        if(reportDataList.size() == 0){
            Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
            alertdialog.setContentView(R.layout.dialog_error);
            alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertdialog.getWindow().setGravity(Gravity.CENTER);
            alertdialog.setCancelable(false);
            alertdialog.setCanceledOnTouchOutside(false);
            TextView message = alertdialog.findViewById(R.id.msgDialog);
            message.setText("PDF not generated because no data found!!!");
            alertdialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertdialog.dismiss();
                }
            },3000);
        } else {

            loaderDialog = new Dialog(MonthlySupplyDetailActivity.this);
            loaderDialog.setContentView(R.layout.dialog_loading);
            loaderDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            loaderDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            loaderDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            loaderDialog.getWindow().setGravity(Gravity.CENTER);
            loaderDialog.setCancelable(false);
            loaderDialog.setCanceledOnTouchOutside(false);
            TextView msg = loaderDialog.findViewById(R.id.msgDialog);
            msg.setText("PDF generating in process!!!");
            loaderDialog.show();

            try {
                PdfWriter writer = new PdfWriter(path);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument, PageSize.A4);

                AssetManager assetManager = getAssets();

//                PdfFont fontBold = PdfFontFactory.createFont(assetManager.open("fonts/poppins_bold.ttf").toString(),PdfEncodings.IDENTITY_H);
//                PdfFont fontSemiBold = PdfFontFactory.createFont(assetManager.open("fonts/poppins_semibold.ttf").toString(),PdfEncodings.IDENTITY_H);
//                PdfFont fontMedium = PdfFontFactory.createFont(assetManager.open("fonts/poppins_medium.ttf").toString(),PdfEncodings.IDENTITY_H);
//                PdfFont fontRegular = PdfFontFactory.createFont(assetManager.open("fonts/poppins_regular.ttf").toString(),PdfEncodings.IDENTITY_H);
//                PdfFont fontLight = PdfFontFactory.createFont(assetManager.open("fonts/poppins_light.ttf").toString(),PdfEncodings.IDENTITY_H);
//                PdfFont fontThin = PdfFontFactory.createFont(assetManager.open("fonts/poppins_thin.ttf").toString(),PdfEncodings.IDENTITY_H);


                PdfFont fontBold = PdfFontFactory.createFont(FontProgramFactory.createFont("res/font/poppins_bold.ttf"));
                PdfFont fontSemiBold = PdfFontFactory.createFont(FontProgramFactory.createFont("res/font/poppins_semibold.ttf"));
                PdfFont fontMedium = PdfFontFactory.createFont(FontProgramFactory.createFont("res/font/poppins_medium.ttf"));
                PdfFont fontRegular = PdfFontFactory.createFont(FontProgramFactory.createFont("res/font/poppins_regular.ttf"));
                PdfFont fontLight = PdfFontFactory.createFont(FontProgramFactory.createFont("res/font/poppins_light.ttf"));
                PdfFont fontThin = PdfFontFactory.createFont(FontProgramFactory.createFont("res/font/poppins_thin.ttf"));

                // Add header
                Paragraph header = new Paragraph("Attari Dairy")
                        .setFont(fontBold)
                        .setFontSize(14)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBackgroundColor(new DeviceRgb(0xF5, 0xF7, 0xFA))
                        .setMarginBottom(15)
                        .setPaddings(10f, 10f, 10f, 10f)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C))
                        .setBorderRadius(new BorderRadius(5f));
                document.add(header);

                Paragraph clientName = new Paragraph(personName+"'s Monthly Milk Report")
                        .setFont(fontSemiBold)
                        .setFontSize(11)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(-12)
                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00));
                document.add(clientName);

                Paragraph clientContact = new Paragraph(contact)
                        .setFont(fontMedium)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(-12)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C));
                document.add(clientContact);

                Paragraph reportDate = new Paragraph("1st "+currentReportDate+" - 30th "+currentReportDate)
                        .setFont(fontMedium)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setMarginBottom(15)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C));
                document.add(reportDate);

                // Add summary header
                Table summaryHeadTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                        .useAllAvailableWidth()
                        .setMarginBottom(-10);
                summaryHeadTable.addCell(new Cell().add(new Paragraph("Milk Rate")
                        .setFont(fontMedium)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setPaddings(3,0,0,0)
                );
                summaryHeadTable.addCell(new Cell().add(new Paragraph("Total Quantity")
                        .setFont(fontMedium)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setBorderLeft(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setPaddings(3,0,0,0)
                );
                summaryHeadTable.addCell(new Cell().add(new Paragraph("Total Amount")
                        .setFont(fontMedium)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setBorderLeft(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setBorderRight(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setPaddings(3,0,0,0)
                );
                summaryHeadTable.addCell(new Cell().add(new Paragraph("Pending Balance")
                        .setFont(fontMedium)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setPaddings(3,0,0,0)
                );
                document.add(summaryHeadTable);


                // Add summary content
                Table summaryContentTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                        .useAllAvailableWidth()
                        .setMarginBottom(25);
                summaryContentTable.addCell(new Cell().add(new Paragraph("Rs "+DashboardActivity.getMilkRate())
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setPaddings(0,0,3,0)
                );
                summaryContentTable.addCell(new Cell().add(new Paragraph(totalQty.getText().toString().trim()+"kg")
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setBorderLeft(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setPaddings(0,0,3,0)
                );
                summaryContentTable.addCell(new Cell().add(new Paragraph("Rs "+grandTotalAmount.getText().toString().trim())
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setBorderLeft(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setBorderRight(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setPaddings(0,0,3,0)
                );
                summaryContentTable.addCell(new Cell().add(new Paragraph("Rs "+balance)
                        .setFont(fontBold)
                        .setFontSize(12)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(new DeviceRgb(0x49, 0xB7, 0x6F)))
                        .setBorder(null)
                        .setPaddings(0,0,3,0)
                );
                document.add(summaryContentTable);

                // Add table header
                Table dataHeadTable = new Table(UnitValue.createPercentArray(new float[]{0, 2, 2, 2, 2, 2, 2}))
                        .useAllAvailableWidth()
                        .setMarginBottom(15);
                dataHeadTable.addCell(new Cell().add(new Paragraph("#")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.LEFT)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Date")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("QTY")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Amount")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Item Name")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Item Amount")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Total Amount")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );

                int rowsPerPageFirstPage = 16; // Number of rows for the first page
                int rowsPerPageOtherPages = 20; // Number of rows for other pages
                int rowCount = 0;
                int pageNumber = 1;

                for(int i=0; i<reportDataList.size(); i++){
                    int rowsPerPage = 0;

                    if(pageNumber == 1){
                        rowsPerPage = rowsPerPageFirstPage;
                    } else {
                        rowsPerPageOtherPages += rowsPerPageOtherPages-rowsPerPageFirstPage;
                        rowsPerPage = rowsPerPageOtherPages;
                    }

//                    if(rowCount % rowsPerPage == 0 && rowCount != 0){
//                        document.add(dataHeadTable);
//                        addPageNumber(document, pageNumber, fontRegular);
//                        document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
//                        pageNumber++;
//                        dataHeadTable = new Table(UnitValue.createPercentArray(new float[]{0, 2, 2, 2, 2, 2, 2}))
//                                .useAllAvailableWidth()
//                                .setMarginBottom(15);
//
//                        dataHeadTable.addCell(new Cell().add(new Paragraph("#")
//                                        .setFont(fontSemiBold)
//                                        .setFontSize(9)
//                                        .setTextAlignment(TextAlignment.LEFT)
//                                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
//                                .setBorder(null)
//                                .setPaddings(0,0,5,0)
//                        );
//                        dataHeadTable.addCell(new Cell().add(new Paragraph("Date")
//                                        .setFont(fontSemiBold)
//                                        .setFontSize(9)
//                                        .setTextAlignment(TextAlignment.CENTER)
//                                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
//                                .setBorder(null)
//                                .setPaddings(0,0,5,0)
//                        );
//                        dataHeadTable.addCell(new Cell().add(new Paragraph("QTY")
//                                        .setFont(fontSemiBold)
//                                        .setFontSize(9)
//                                        .setTextAlignment(TextAlignment.CENTER)
//                                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
//                                .setBorder(null)
//                                .setPaddings(0,0,5,0)
//                        );
//                        dataHeadTable.addCell(new Cell().add(new Paragraph("Amount")
//                                        .setFont(fontSemiBold)
//                                        .setFontSize(9)
//                                        .setTextAlignment(TextAlignment.CENTER)
//                                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
//                                .setBorder(null)
//                                .setPaddings(0,0,5,0)
//                        );
//                        dataHeadTable.addCell(new Cell().add(new Paragraph("Item Name")
//                                        .setFont(fontSemiBold)
//                                        .setFontSize(9)
//                                        .setTextAlignment(TextAlignment.CENTER)
//                                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
//                                .setBorder(null)
//                                .setPaddings(0,0,5,0)
//                        );
//                        dataHeadTable.addCell(new Cell().add(new Paragraph("Item Amount")
//                                        .setFont(fontSemiBold)
//                                        .setFontSize(9)
//                                        .setTextAlignment(TextAlignment.CENTER)
//                                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
//                                .setBorder(null)
//                                .setPaddings(0,0,5,0)
//                        );
//                        dataHeadTable.addCell(new Cell().add(new Paragraph("Total Amount")
//                                        .setFont(fontSemiBold)
//                                        .setFontSize(9)
//                                        .setTextAlignment(TextAlignment.RIGHT)
//                                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
//                                .setBorder(null)
//                                .setPaddings(0,0,5,0)
//                        );
//                    }

                    if(reportDataList.get(i).getItemName().equals("")){
                        dataHeadTable.addCell(createCell(""+(i+1), fontRegular));
                        dataHeadTable.addCell(createCell(reportDataList.get(i).getDate(), fontRegular));
                        dataHeadTable.addCell(createCell(reportDataList.get(i).getQty()+"kg", fontRegular));
                        dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getAmount(), fontRegular));
                        dataHeadTable.addCell(createCell("-", fontRegular));
                        dataHeadTable.addCell(createCell("-", fontRegular));
                        dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getTotalAmount(), fontSemiBold, new DeviceRgb(0x49, 0xB7, 0x6F)));

                    } else {
                        dataHeadTable.addCell(createCell(""+(i+1), fontRegular));
                        dataHeadTable.addCell(createCell(reportDataList.get(i).getDate(), fontRegular));
                        dataHeadTable.addCell(createCell(reportDataList.get(i).getQty()+"kg", fontRegular));
                        dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getAmount(), fontRegular));
                        dataHeadTable.addCell(createCell(reportDataList.get(i).getItemName(), fontRegular));
                        dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getItemAmount(), fontRegular));
                        dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getTotalAmount(), fontSemiBold, new DeviceRgb(0x49, 0xB7, 0x6F)));
                    }
                    rowCount++;
                }

                document.add(dataHeadTable);
                addPageNumber(document, pageNumber, fontRegular);
                document.close();
                loaderDialog.dismiss();
                reportDialog.show();
                return path;

            } catch (Exception e) {
                loaderDialog.dismiss();
                reportDialog.dismiss();
                Dialog alertdialog = new Dialog(MonthlySupplyDetailActivity.this);
                alertdialog.setContentView(R.layout.dialog_error);
                alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                alertdialog.getWindow().setGravity(Gravity.CENTER);
                alertdialog.setCancelable(false);
                alertdialog.setCanceledOnTouchOutside(false);
                TextView message = alertdialog.findViewById(R.id.msgDialog);
                message.setText("Something went wrong, PDF not generated!!!");
                alertdialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        alertdialog.dismiss();
                    }
                },3000);
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    private void addPageNumber(Document document, int pageNumber, PdfFont font) {
        Paragraph footer = new Paragraph("Page " + pageNumber)
                .setTextAlignment(TextAlignment.CENTER)
                .setFont(font)
                .setFontSize(10)
                .setFontColor(new DeviceRgb(0x00, 0x00, 0x06))
                .setVerticalAlignment(VerticalAlignment.BOTTOM);
        document.showTextAligned(footer, 297.5f, 15, document.getPdfDocument().getNumberOfPages(), TextAlignment.CENTER, VerticalAlignment.BOTTOM, 0);
    }

    private static Cell createCell(String content, PdfFont font) {
        return new Cell()
                .add(new Paragraph(content)
                        .setFont(font)
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontColor(new DeviceRgb(0x00, 0x00, 0x06)))
                .setBorder(null)
                .setBorderTop(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 1))
                .setPaddings(5,0,5,0);
    }

    private static Cell createCell(String content, PdfFont font, com.itextpdf.kernel.colors.Color color) {
        return new Cell()
                .add(new Paragraph(content)
                        .setFont(font)
                        .setFontSize(8)
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontColor(color))
                .setBorder(null)
                .setBorderTop(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 1))
                .setPaddings(5,5,5,0)
                .setBackgroundColor(new DeviceRgb(0xF5, 0xF7, 0xFA));
    }

    public void sharePDF(String path){
        if(path != null){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            File file = new File(path);
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            startActivity(Intent.createChooser(intent, "Share PDF"));
        }
    }

    private void showPdfPreview(String path, ImageView pdfImageView) {
        if (path != null) {
            try {
                File file = new File(path);
                ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
                PdfRenderer pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                PdfRenderer.Page currentPage = pdfRenderer.openPage(0);
                Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
                currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                pdfImageView.setImageBitmap(bitmap);
                currentPage.close();
                pdfRenderer.close();
                parcelFileDescriptor.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
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
            TextView sno, date, qty, amount, itemName, itemAmount;
            ImageView delete;
            LinearLayout itemContainer;

            sno = customListItem.findViewById(R.id.sno);
            date = customListItem.findViewById(R.id.date);
            qty = customListItem.findViewById(R.id.qty);
            amount = customListItem.findViewById(R.id.amount);
            delete = customListItem.findViewById(R.id.delete);
            itemContainer = customListItem.findViewById(R.id.itemContainer);
            itemName = customListItem.findViewById(R.id.itemName);
            itemAmount = customListItem.findViewById(R.id.itemAmount);

            sno.setText(""+(i+1));
            date.setText(data.get(i).getDate());
            qty.setText(data.get(i).getQty()+"kg");
            amount.setText("Rs "+data.get(i).getAmount()+"/-");
            itemName.setText(data.get(i).getItemName());
            itemAmount.setText("Rs "+data.get(i).getItemAmount()+"/-");

            if(!data.get(i).getItemName().equals("")){
                itemContainer.setVisibility(View.VISIBLE);
            } else {
                itemContainer.setVisibility(View.GONE);
            }

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
                            double totalAmount = Double.parseDouble(data.get(i).getTotalAmount());
                            double balance = Double.parseDouble(balancedAmount.getText().toString().trim());

                            if(totalAmount<=balance){
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
                    });

                    actiondialog.show();
                }
            });

            // Set padding top to 0
            if(i==0){
                customListItem.setPadding(customListItem.getPaddingLeft(), 0,customListItem.getPaddingRight(), 0);
            }
            customListItem.setAlpha(0f);
            customListItem.animate().alpha(1f).setDuration(500).setStartDelay(i * 2).start();

            return customListItem;
        }
    }

    class MyAdapterItems extends BaseAdapter {

        Context context;
        ArrayList<ItemsModel> data;

        public MyAdapterItems(Context context, ArrayList<ItemsModel> data) {
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
            LinearLayout listItem;

            sno = customListItem.findViewById(R.id.sno);
            listItem = customListItem.findViewById(R.id.listItem);
            itemName = customListItem.findViewById(R.id.itemName);
            itemAmount = customListItem.findViewById(R.id.itemAmount);
            menu = customListItem.findViewById(R.id.menu);

            sno.setText(""+(i+1));
            itemName.setText(data.get(i).getName());
            itemAmount.setText("Rs "+data.get(i).getAmount()+"/-");
            menu.setVisibility(View.GONE);

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemNameInput.setText(data.get(i).getName());
                    itemAmountInput.setText(data.get(i).getAmount());
                    Dialog dialog = new Dialog(context);
                    dialog.setContentView(R.layout.dialog_success);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.CENTER);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setCancelable(false);
                    TextView msg = dialog.findViewById(R.id.msgDialog);
                    msg.setText("Item Selected!!!");
                    dialog.show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            itemsDialog.dismiss();
                        }
                    },2000);
                }
            });

            return customListItem;
        }
    }
}