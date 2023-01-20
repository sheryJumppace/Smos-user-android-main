package com.smox.smoxuser.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.iarcuschin.simpleratingbar.SimpleRatingBar;
import com.smox.smoxuser.R;

public class DetailRatingDialog extends Dialog {
    private Context mContext;
    String comment;
    int cleanRating;
    int workRating;
    int behaveRating;
    float rating;

    public DetailRatingDialog(Context context, String comment, int cleanRating, int workRating, int behaveRating, float rating) {
        super(context);
        mContext = context;
        this.comment = comment;
        this.cleanRating = cleanRating;
        this.workRating = workRating;
        this.behaveRating = behaveRating;
        this.rating = rating;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_detail_rating);
        setCancelable(false);

        EditText valueEditText = findViewById(R.id.txtValue);
        SimpleRatingBar ratingBarClean = findViewById(R.id.ratingBarClean);
        SimpleRatingBar ratingBarWork = findViewById(R.id.ratingBarWork);
        SimpleRatingBar ratingBarBehave = findViewById(R.id.ratingBarBehave);
        Button btnDone = findViewById(R.id.btnDone);
        TextView txtTitle=findViewById(R.id.txtTitleReview);

        txtTitle.setText(mContext.getString(R.string.reviews_ratings)+" "+mContext.getString(R.string.format_ratingss,rating));
        valueEditText.setText(comment);
        ratingBarClean.setRating(cleanRating);
        ratingBarWork.setRating(workRating);
        ratingBarBehave.setRating(behaveRating);


        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
