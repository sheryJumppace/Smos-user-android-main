package com.smox.smoxuser.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smox.smoxuser.R;


public class VerticalItemAdapter extends RecyclerView.Adapter<VerticalItemAdapter.ViewHolder> {

    private final Context context;

    public VerticalItemAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_styler_view, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {


    }

    @Override
    public int getItemCount() {
        return 6;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        //  private final RelativeLayout rlRoot;
        //  private final ImageView imgBanner;
        // private final TextView bannerName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // rlRoot = itemView.findViewById(R.id.rlRoot);
            // bannerName = itemView.findViewById(R.id.bannerName);
            // imgBanner = itemView.findViewById(R.id.imgBanner);
        }
    }
}

