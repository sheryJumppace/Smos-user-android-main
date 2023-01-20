package com.smox.smoxuser.ui.dialog;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.smox.smoxuser.R;
import com.smox.smoxuser.manager.Constants;
import com.smox.smoxuser.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class EventDialog extends Dialog implements DatePickerDialog.OnDateSetListener {
    private final Context mContext;
    public Button confirmButton, btnDone;
    public EditText valueEditText, edtStartDate, edtEndDate;
    //public TextView txtStart;
    //public TextView txtEnd;
    private boolean isStart;
    public Date startDate, endDate;
    private Event event;

    public EventDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public  void setEventData(Event event){
        this.event=event;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_event);
        setCancelable(false);

        startDate = new Date();
        endDate = new Date();

        valueEditText = (EditText)findViewById(R.id.txtValue);
        confirmButton = (Button)findViewById(R.id.btnConfirm);
        /*txtStart = findViewById(R.id.txtStartTime);
        txtEnd = findViewById(R.id.txtEndTime);*/
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);

        //edtStartDate.setOnKeyListener(null);
        //edtEndDate.setOnKeyListener(null);

        edtStartDate.setInputType(InputType.TYPE_NULL);
        edtStartDate.setTextIsSelectable(false);
        edtEndDate.setInputType(InputType.TYPE_NULL);
        edtEndDate.setTextIsSelectable(false);
        btnDone = (Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> dismiss());

        edtStartDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isStart = true;
                    openDatePicker();
                }

                return false;
            }
        });

        edtEndDate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    isStart = false;
                    openDatePicker();
                }

                return false;
            }
        });

        /*edtStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStart = true;
                openDatePicker();
            }
        });
        edtEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isStart = false;
                openDatePicker();
            }
        });*/

        setData();
    }

    private void setData(){
        if(event!=null){
            valueEditText.setText(event.getEvent());
            confirmButton.setText(mContext.getString(R.string.edit));
        }

    }

    private void openDatePicker(){
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dialog = new DatePickerDialog(mContext, R.style.DialogTheme, this, year, month, day);
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat formatter =  new SimpleDateFormat(Constants.KDateFormatter.serverDay, Locale.getDefault());
        String time = formatter.format(cal.getTime());
        if(isStart){
            try {
                startDate = formatter.parse(time);

                if(startDate.compareTo(endDate) > 0){
                    //Toast.makeText(getContext(), getContext().getResources().getString(R.string.startDateValidation), Toast.LENGTH_LONG).show();

                    String strTextDate = formatter.format(startDate);
                    String endTextDate = formatter.format(endDate);

                    if(startDate.before(endDate)){
                        edtStartDate.setText(time);
                    } else {
                        if(!strTextDate.equals(endTextDate)){
                            edtStartDate.setText(time);
                            edtEndDate.setText(time);
                            endDate = formatter.parse(time);
                        } else {
                            Toast.makeText(getContext(), getContext().getResources().getString(R.string.startDateValidation), Toast.LENGTH_LONG).show();
                        }

                    }


                } else {
                    edtStartDate.setText(time);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            /*txtStart.setText(time);
            txtEnd.setText(time);*/
        }else{
            try {
                endDate = formatter.parse(time);

                if(startDate.compareTo(endDate) <= 0){
                    edtEndDate.setText(time);
                } else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.endDateValidation), Toast.LENGTH_LONG).show();
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //txtEnd.setText(time);
        }

    }
}
