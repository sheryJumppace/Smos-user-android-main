package com.smox.smoxuser.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.smox.smoxuser.R;
import com.smox.smoxuser.manager.Constants;
import com.smox.smoxuser.model.Appointment;
import com.smox.smoxuser.model.type.AppointmentType;


public class AppointmentCompleteDialog extends Dialog {
    private Context mContext;
    private Appointment appointment;
    public AppointmentCompleteDialog(@NonNull Context context, Appointment appointment) {
        super(context);
        mContext = context;
        this.appointment = appointment;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        setContentView(R.layout.dialog_appointment_complete);
        setCancelable(false);

        ImageView imgIcon = findViewById(R.id.imgIcon);
        Button btnDone = findViewById(R.id.btnDone);
        btnDone.setOnClickListener(v -> dismiss());

        ((TextView)findViewById(R.id.txtName)).setText(appointment.getUser().getName());
        String url = appointment.getUser().getImage();
        if (!URLUtil.isValidUrl(url)) {
            url = Constants.KUrl.image + url;
        }

        Glide.with(mContext)
                .load(url)
                .apply(new RequestOptions().centerCrop().circleCrop().placeholder(R.drawable.user))
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imgIcon);
        findViewById(R.id.btnComplete).setOnClickListener(v -> {
            if(onItemClickListener != null){
                dismiss();
                onItemClickListener.updateAppointment(AppointmentType.Completed);
            }

        });
        findViewById(R.id.btnNoShow).setOnClickListener(v -> {
            if(onItemClickListener != null){
                dismiss();
                onItemClickListener.updateAppointment(AppointmentType.NoShow);
            }
        });
    }

   public interface ItemClickListener {
        void updateAppointment(AppointmentType type);
    }

    private ItemClickListener onItemClickListener = null;

    public void setItemClickListener(ItemClickListener clickListener) {
        onItemClickListener = clickListener;
    }
}
