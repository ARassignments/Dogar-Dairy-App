package com.example.dogardairy.Screens;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class BillActivity extends AppCompatActivity {

    String MonthlyId;
    ListView listView;
    LinearLayout notfoundContainer;
    TextView totalQty, grandTotalAmount, grandTotalGivenAmount, date;
    ImageView fullListBtn;
    ArrayList<BillModel> datalist = new ArrayList<>();
    Dialog reportDialog, loaderDialog;
    String contact, personName;

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
        grandTotalGivenAmount = findViewById(R.id.grandTotalGivenAmount);
        date = findViewById(R.id.date);
        fullListBtn = findViewById(R.id.fullListBtn);

        Calendar calendar = Calendar.getInstance();
        date.setText(new SimpleDateFormat("yyyy/M").format(calendar.getTime()));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            MonthlyId = extras.getString("MonthlyId");
            contact = extras.getString("contact");
            personName = extras.getString("personName");
        }

        findViewById(R.id.backBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BillActivity.super.onBackPressed();
            }
        });

        fullListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(datalist.size() == 0){
                    Dialog alertdialog = new Dialog(BillActivity.this);
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
                    reportDialog = new Dialog(BillActivity.this);
                    reportDialog.setContentView(R.layout.dialog_pdf_report);
                    reportDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
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

                    showPdfPreview(generatePDF(datalist),imageView);

                    shareBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sharePDF(generatePDF(datalist));
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

    public void fetchData(){
        MainActivity.db.child("PaidReport").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    datalist.clear();
                    double grandtotal = 0.0;
                    double grandtotalgiven = 0.0;
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
                            grandtotal += Double.parseDouble(ds.child("totalAmount").getValue().toString());
                            grandtotalgiven += Double.parseDouble(ds.child("givenAmount").getValue().toString());
                            grandqty += Double.parseDouble(ds.child("totalQty").getValue().toString());
                        }
                    }
                    if(datalist.size() > 0){
                        listView.setVisibility(View.VISIBLE);
                        notfoundContainer.setVisibility(View.GONE);
                        MyAdapter adapter = new MyAdapter(BillActivity.this,datalist);
                        listView.setAdapter(adapter);
                        grandTotalAmount.setText(""+grandtotal);
                        grandTotalGivenAmount.setText(""+grandtotalgiven);
                        totalQty.setText(""+grandqty);
                    } else {
                        listView.setVisibility(View.GONE);
                        notfoundContainer.setVisibility(View.VISIBLE);
                        grandTotalAmount.setText("0");
                        grandTotalGivenAmount.setText("0");
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

    public String generatePDF(ArrayList<BillModel> reportDataList){
        // Check and request storage permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request it
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 112);
            }
        }
//        String path = getExternalFilesDir(null) + "/"+personName+" - ATTARI DAIRY - Monthly Milk Report - "+currentReportDate+".pdf";

        File file = new File(getExternalFilesDir(null), personName + " - ATTARI DAIRY - Paid Bill Report.pdf");
        String path = file.getAbsolutePath();

        if(reportDataList.size() == 0){
            Dialog alertdialog = new Dialog(BillActivity.this);
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

            loaderDialog = new Dialog(BillActivity.this);
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

            //date picker start
            Calendar calendar = Calendar.getInstance();
            String[] dateArray = date.getText().toString().split("/");
            int month = Integer.parseInt(dateArray[1]);
            int year = Integer.parseInt(dateArray[0]);
            String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            //date picker end

            String lastDate = getLastMonthDate("01/"+String.format("%02d",month)+"/"+year, "dd/MM/yyyy", "dd MMMM yyyy");

            try {
                PdfWriter writer = new PdfWriter(path);
                PdfDocument pdfDocument = new PdfDocument(writer);
                Document document = new Document(pdfDocument, PageSize.A4);

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

                Paragraph clientName = new Paragraph(personName+"'s Paid Bill Report")
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
                        .setMarginBottom(15)
                        .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C));
                document.add(clientContact);

                // Add summary header
                Table summaryHeadTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                        .useAllAvailableWidth()
                        .setMarginBottom(-10);
                summaryHeadTable.addCell(new Cell().add(new Paragraph("Total Quantity")
                                .setFont(fontMedium)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.LEFT)
                                .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setPaddings(3,0,0,0)
                );
                summaryHeadTable.addCell(new Cell().add(new Paragraph("Total Amount")
                                .setFont(fontMedium)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setBorderRight(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setBorderLeft(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setPaddings(3,0,0,0)
                );
                summaryHeadTable.addCell(new Cell().add(new Paragraph("Total Given Amount")
                                .setFont(fontMedium)
                                .setFontSize(10)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setPaddings(3,0,0,0)
                );
                document.add(summaryHeadTable);


                // Add summary content
                Table summaryContentTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}))
                        .useAllAvailableWidth()
                        .setMarginBottom(25);
                summaryContentTable.addCell(new Cell().add(new Paragraph(totalQty.getText().toString().trim()+"kg")
                                .setFont(fontBold)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.LEFT)
                                .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setPaddings(0,0,3,0)
                );
                summaryContentTable.addCell(new Cell().add(new Paragraph("Rs "+grandTotalAmount.getText().toString().trim())
                                .setFont(fontBold)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x4C, 0x4C, 0x4C)))
                        .setBorder(null)
                        .setBorderRight(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setBorderLeft(new SolidBorder(new DeviceRgb(0xE6, 0xED, 0xF0), 2))
                        .setPaddings(0,0,3,0)
                );
                summaryContentTable.addCell(new Cell().add(new Paragraph("Rs "+grandTotalGivenAmount.getText().toString().trim())
                                .setFont(fontBold)
                                .setFontSize(12)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setFontColor(new DeviceRgb(0x49, 0xB7, 0x6F)))
                        .setBorder(null)
                        .setPaddings(0,0,3,0)
                );
                document.add(summaryContentTable);

                // Add table header
                Table dataHeadTable = new Table(UnitValue.createPercentArray(new float[]{0, 2, 2, 2, 2, 2, 2, 2}))
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
                dataHeadTable.addCell(new Cell().add(new Paragraph("Paid On")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Month")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Pay By")
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
                dataHeadTable.addCell(new Cell().add(new Paragraph("Total Amt")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Balance Amt")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.CENTER)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );
                dataHeadTable.addCell(new Cell().add(new Paragraph("Given Amt")
                                .setFont(fontSemiBold)
                                .setFontSize(9)
                                .setTextAlignment(TextAlignment.RIGHT)
                                .setFontColor(new DeviceRgb(0x00, 0x00, 0x00)))
                        .setBorder(null)
                        .setPaddings(0,0,5,0)
                );

                int pageNumber = 1;

                for(int i=0; i<reportDataList.size(); i++){
                    dataHeadTable.addCell(createCell(""+(i+1), fontRegular));
                    dataHeadTable.addCell(createCell(reportDataList.get(i).getDate(), fontRegular));
                    dataHeadTable.addCell(createCell(reportDataList.get(i).getMonth(), fontRegular));
                    dataHeadTable.addCell(createCell(reportDataList.get(i).getPaymentMethod(), fontRegular));
                    dataHeadTable.addCell(createCell(reportDataList.get(i).getTotalQty()+"kg", fontRegular));
                    dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getTotalAmount(), fontRegular));
                    dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getBalanceAmount(), fontRegular));
                    dataHeadTable.addCell(createCell("Rs "+reportDataList.get(i).getGivenAmount(), fontSemiBold, new DeviceRgb(0x49, 0xB7, 0x6F)));
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
                Dialog alertdialog = new Dialog(BillActivity.this);
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
            intent.setPackage("com.whatsapp");
            startActivity(intent);
//            startActivity(Intent.createChooser(intent, "Share PDF"));
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

    public static String getLastMonthDate(String givenDate, String inputFormat, String outputFormat) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat(inputFormat, Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat, Locale.getDefault());

        try {
            // Parse the given date
            Date date = inputDateFormat.parse(givenDate);

            // Get a Calendar instance
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            // Set the date to the last day of the month
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));

            // Get the last day of the month
            Date lastDayOfMonth = calendar.getTime();

            // Format the date as needed
            return outputDateFormat.format(lastDayOfMonth);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
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
                    Button cancelBtn, shareBtn, modifyBtn;
                    TextView date, month, name, contact, from, to, milkRate, paymentMethod, totalQty, totalAmount, balanceAmount, givenAmount;
                    shareBtn = actiondialog.findViewById(R.id.shareBtn);
                    cancelBtn = actiondialog.findViewById(R.id.cancelBtn);
                    modifyBtn = actiondialog.findViewById(R.id.modifyBtn);
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

                    modifyBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Dialog modifyDialog = new Dialog(context);
                            modifyDialog.setContentView(R.layout.dialog_modify_paid_bill);
                            modifyDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                            modifyDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            modifyDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            modifyDialog.getWindow().setGravity(Gravity.CENTER);
                            modifyDialog.setCancelable(false);
                            modifyDialog.setCanceledOnTouchOutside(false);
                            Button addDataBtn, cancelBtn;
                            TextInputLayout monthLayout, yearLayout;
                            AutoCompleteTextView monthInput, yearInput;
                            addDataBtn = modifyDialog.findViewById(R.id.addDataBtn);
                            cancelBtn = modifyDialog.findViewById(R.id.cancelBtn);
                            monthLayout = modifyDialog.findViewById(R.id.monthLayout);
                            monthInput = modifyDialog.findViewById(R.id.monthInput);
                            yearLayout = modifyDialog.findViewById(R.id.yearLayout);
                            yearInput = modifyDialog.findViewById(R.id.yearInput);

                            //date picker start
                            Calendar calendar = Calendar.getInstance();
                            String currentYear = new SimpleDateFormat("yyyy").format(calendar.getTime());

                            monthInput.setText(data.get(i).getMonth());
                            yearInput.setText(currentYear);

                            ArrayList<String> monthsList = new ArrayList<String>();
                            monthsList.add("January");
                            monthsList.add("February");
                            monthsList.add("March");
                            monthsList.add("April");
                            monthsList.add("May");
                            monthsList.add("June");
                            monthsList.add("July");
                            monthsList.add("August");
                            monthsList.add("September");
                            monthsList.add("October");
                            monthsList.add("November");
                            monthsList.add("December");

                            ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line,monthsList);
                            monthInput.setAdapter(adapterMonth);

                            ArrayList<String> yearList = new ArrayList<String>();
                            yearList.add("2024");
                            yearList.add("2025");
                            yearList.add("2026");
                            yearList.add("2027");
                            yearList.add("2028");
                            yearList.add("2029");
                            yearList.add("2030");

                            ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(context, android.R.layout.simple_dropdown_item_1line,yearList);
                            yearInput.setAdapter(adapterYear);

                            HashMap<String,String> monthsToNumber = new HashMap<>();
                            monthsToNumber.put("January","01");
                            monthsToNumber.put("February","02");
                            monthsToNumber.put("March","03");
                            monthsToNumber.put("April","04");
                            monthsToNumber.put("May","05");
                            monthsToNumber.put("June","06");
                            monthsToNumber.put("July","07");
                            monthsToNumber.put("August","08");
                            monthsToNumber.put("September","09");
                            monthsToNumber.put("October","10");
                            monthsToNumber.put("November","11");
                            monthsToNumber.put("December","12");

                            String fromDate = "01/"+monthsToNumber.get(monthInput.getText().toString())+"/"+yearInput.getText().toString();
                            String toDate = getLastMonthDate(fromDate, "dd/MM/yyyy", "dd/MM/yyyy");

                            addDataBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MainActivity.db.child("PaidReport").child(data.get(i).getId()).child("month").setValue(monthInput.getText().toString());
                                    MainActivity.db.child("PaidReport").child(data.get(i).getId()).child("from").setValue(fromDate);
                                    MainActivity.db.child("PaidReport").child(data.get(i).getId()).child("to").setValue(toDate);
                                    month.setText(monthInput.getText().toString()+" Paid Bill");
                                    from.setText(fromDate);
                                    to.setText(toDate);

                                    Dialog dialog = new Dialog(context);
                                    dialog.setContentView(R.layout.dialog_success);
                                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                                    dialog.getWindow().setGravity(Gravity.CENTER);
                                    dialog.setCanceledOnTouchOutside(false);
                                    dialog.setCancelable(false);
                                    TextView msg = dialog.findViewById(R.id.msgDialog);
                                    msg.setText("Updated Successfully!!!");
                                    dialog.show();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            dialog.dismiss();
                                            modifyDialog.dismiss();
                                        }
                                    },2000);
                                }
                            });

                            cancelBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    modifyDialog.dismiss();
                                }
                            });

                            modifyDialog.show();
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
                            },2000);
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