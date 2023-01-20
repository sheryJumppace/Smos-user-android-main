package com.smox.smoxuser.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.smox.smoxuser.R;

/**
 * Created by Kris on 5/31/2017.
 */

public class RatingDialog extends Dialog {
    private Context mContext;
    public Button confirmButton;
    public EditText valueEditText;
    public SimpleRatingBar ratingBarClean;
    public SimpleRatingBar ratingBarWork;
    public SimpleRatingBar ratingBarBehave;
    public RatingDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_rating);
        setCancelable(false);

        valueEditText = (EditText)findViewById(R.id.txtValue);
        confirmButton = (Button)findViewById(R.id.btnConfirm);
        ratingBarClean = (SimpleRatingBar)findViewById(R.id.ratingBarClean);
        ratingBarWork = (SimpleRatingBar)findViewById(R.id.ratingBarWork);
        ratingBarBehave = (SimpleRatingBar)findViewById(R.id.ratingBarBehave);
        Button btnDone = (Button)findViewById(R.id.btnDone);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
