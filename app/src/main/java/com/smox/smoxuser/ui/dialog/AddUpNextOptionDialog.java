package com.smox.smoxuser.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.smox.smoxuser.R;


public class AddUpNextOptionDialog extends Dialog {
    private Context mContext;
    public ImageView imgIcon;
    public TextView txtTitle;
    public Button confirmButton;
    public EditText valueEditText;
    public AddUpNextOptionDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_content);
        setCancelable(false);

        valueEditText = findViewById(R.id.txtValue);
        confirmButton = findViewById(R.id.btnConfirm);
        imgIcon = findViewById(R.id.imgIcon);
        txtTitle = findViewById(R.id.txtTitle);
        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> dismiss());

    }
}
