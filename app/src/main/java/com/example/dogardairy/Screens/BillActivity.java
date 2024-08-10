package com.example.dogardairy.Screens;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import com.example.dogardairy.Models.BillModel;
import com.example.dogardairy.Models.MonthlyDetailModel;
import com.example.dogardairy.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

public class BillActivity extends AppCompatActivity {

    String MonthlyId;
    ListView listView;
    LinearLayout notfoundContainer;
    TextView totalQty, grandTotalAmount, date;
    ImageView fullListBtn;
    ArrayList<BillModel> datalist = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bill);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        listView = findViewById(R.id.listView);
        notfoundContainer = findViewById(R.id.notfoundContainer);
        totalQty = findViewById(R.id.totalQty);
        grandTotalAmount = findViewById(R.id.grandTotalAmount);
        date = findViewById(R.id.date);
        fullListBtn = findViewById(R.id.fullListBtn);

        Calendar calendar = Calendar.getInstance();
        date.setText(new SimpleDateFormat("yyyy/M").format(calendar.getTime()));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            MonthlyId = extras.getString("MonthlyId");
        }

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillActivity.super.onBackPressed();
            }
        });

        fetchData();

    }

    public void fetchData(){
        MainActivity.db.child("PaidReport").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    datalist.clear();
                    double grandtotal = 0.0;
                    double grandqty = 0.0;
                    for (DataSnapshot ds: snapshot.getChildren()){
                        if(ds.child("userPerson").getValue().toString().equals(MonthlyId)){
                            BillModel model = new BillModel(ds.getKey(),
                                    ds.child("userId").getValue().toString(),
                                    ds.child("userPerson").getValue().toString(),
                                    ds.child("name").getValue().toString(),
                                    ds.child("contact").getValue().toString(),
                                    ds.child("milkRate").getValue().toString(),
                                    ds.child("paymentMethod").getValue().toString(),
                                    ds.child("totalAmount").getValue().toString(),
                                    ds.child("givenAmount").getValue().toString(),
                                    ds.child("totalQty").getValue().toString(),
                                    ds.child("balanceAmount").getValue().toString(),
                                    ds.child("from").getValue().toString(),
                                    ds.child("to").getValue().toString(),
                                    ds.child("month").getValue().toString(),
                                    ds.child("date").getValue().toString()
                            );
                            datalist.add(model);
                            grandtotal += Double.parseDouble(ds.child("givenAmount").getValue().toString());
                            grandqty += Double.parseDouble(ds.child("totalQty").getValue().toString());
                        }
                    }
                    if(datalist.size() > 0){
                        listView.setVisibility(View.VISIBLE);
                        notfoundContainer.setVisibility(View.GONE);
                        MyAdapter adapter = new MyAdapter(BillActivity.this,datalist);
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

    private Bitmap getBitmapFromView(View view) {
        // Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        // Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        // Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            // has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            // does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        // return the bitmap
        return returnedBitmap;
    }

    private File saveBitmapToFile(Bitmap bitmap) throws IOException {
        File imageFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "shared_image.png");
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        outputStream.flush();
        outputStream.close();
        return imageFile;
    }

    private void shareImage(File imageFile, String phoneNumber) {
        Uri imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", imageFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/png");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
//        shareIntent.putExtra(Intent.EXTRA_TEXT, "Here is an image for you!");
        shareIntent.setPackage("com.whatsapp");

        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Dialog alertdialog = new Dialog(BillActivity.this);
            alertdialog.setContentView(R.layout.dialog_error);
            alertdialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            alertdialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            alertdialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            alertdialog.getWindow().setGravity(Gravity.CENTER);
            alertdialog.setCancelable(false);
            alertdialog.setCanceledOnTouchOutside(false);
            TextView message = alertdialog.findViewById(R.id.msgDialog);
            message.setText("WhatsApp is not installed!!!");
            alertdialog.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    alertdialog.dismiss();
                }
            },3000);
        }
//        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }



    class MyAdapter extends BaseAdapter {

        Context context;
        ArrayList<BillModel> data;

        public MyAdapter(Context context, ArrayList<BillModel> data) {
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
            View customListItem = LayoutInflater.from(context).inflate(R.layout.bill_custom_listview,null);
            TextView sno, date, month, givenAmount;
            ImageView delete;
            LinearLayout listItem;

            sno = customListItem.findViewById(R.id.sno);
            date = customListItem.findViewById(R.id.date);
            month = customListItem.findViewById(R.id.month);
            givenAmount = customListItem.findViewById(R.id.givenAmount);
            delete = customListItem.findViewById(R.id.delete);
            listItem = customListItem.findViewById(R.id.listItem);

            sno.setText(""+(i+1));
            date.setText(data.get(i).getDate());
            givenAmount.setText("Rs "+data.get(i).getGivenAmount());
            month.setText(data.get(i).getMonth());

            listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialog actiondialog = new Dialog(context);
                    actiondialog.setContentView(R.layout.dialog_bill_report);
                    actiondialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                    actiondialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    actiondialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    actiondialog.getWindow().setGravity(Gravity.CENTER);
                    actiondialog.setCancelable(false);
                    actiondialog.setCanceledOnTouchOutside(false);
                    Button cancelBtn, shareBtn;
                    TextView date, month, name, contact, from, to, milkRate, paymentMethod, totalQty, totalAmount, balanceAmount, givenAmount;
                    shareBtn = actiondialog.findViewById(R.id.shareBtn);
                    cancelBtn = actiondialog.findViewById(R.id.cancelBtn);
                    date = actiondialog.findViewById(R.id.date);
                    month = actiondialog.findViewById(R.id.month);
                    name = actiondialog.findViewById(R.id.name);
                    contact = actiondialog.findViewById(R.id.contact);
                    from = actiondialog.findViewById(R.id.from);
                    to = actiondialog.findViewById(R.id.to);
                    milkRate = actiondialog.findViewById(R.id.milkRate);
                    paymentMethod = actiondialog.findViewById(R.id.paymentMethod);
                    totalQty = actiondialog.findViewById(R.id.totalQty);
                    totalAmount = actiondialog.findViewById(R.id.totalAmount);
                    balanceAmount = actiondialog.findViewById(R.id.balanceAmount);
                    givenAmount = actiondialog.findViewById(R.id.givenAmount);

                    date.setText(data.get(i).getDate());
                    month.setText(data.get(i).getMonth()+" Paid Bill");
                    name.setText(data.get(i).getName());
                    contact.setText(data.get(i).getContact());
                    from.setText(data.get(i).getFrom());
                    to.setText(data.get(i).getTo());
                    milkRate.setText("Rs "+data.get(i).getMilkRate());
                    paymentMethod.setText(data.get(i).getPaymentMethod());
                    totalQty.setText(data.get(i).getTotalQty()+"kg");
                    totalAmount.setText("Rs "+data.get(i).getTotalAmount());
                    balanceAmount.setText("Rs "+data.get(i).getBalanceAmount());
                    givenAmount.setText("Rs "+data.get(i).getGivenAmount());

                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (ContextCompat.checkSelfPermission(BillActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                    // If permission is not granted, request it
                                    ActivityCompat.requestPermissions(BillActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
                                }
                            }
                            View billLayout = actiondialog.findViewById(R.id.billLayout);
                            Bitmap bitmap = getBitmapFromView(billLayout);
                            File imageFile = null;
                            try {
                                imageFile = saveBitmapToFile(bitmap);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            shareImage(imageFile,data.get(i).getContact());
                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            actiondialog.dismiss();
                        }
                    });

                    actiondialog.show();
                }
            });

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
                            MainActivity.db.child("PaidReport").child(data.get(i).getId()).removeValue();
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
}