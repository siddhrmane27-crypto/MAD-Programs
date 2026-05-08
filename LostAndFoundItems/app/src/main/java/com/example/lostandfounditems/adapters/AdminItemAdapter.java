package com.example.lostandfounditems.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.utils.TimeUtils;

import java.util.List;

public class AdminItemAdapter extends RecyclerView.Adapter<AdminItemAdapter.AdminViewHolder> {

    private List<ItemModel> items;
    private OnAdminActionListener listener;

    public interface OnAdminActionListener {
        void onApprove(ItemModel item, int position);
        void onReject(ItemModel item, int position);
    }

    public AdminItemAdapter(List<ItemModel> items, OnAdminActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pending, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        ItemModel item = items.get(position);
        
        holder.titleText.setText(item.getTitle());
        holder.descriptionText.setText(item.getDescription());
        holder.categoryText.setText(item.getCategory());
        holder.locationText.setText(item.getLocation());
        holder.typeText.setText(item.getType());
        holder.userEmailText.setText("Posted by: " + item.getUserEmail());
        holder.timestampText.setText(TimeUtils.formatTimestamp(item.getTimestamp()));

        // Set type badge color
        if ("Lost".equals(item.getType())) {
            holder.typeText.setBackgroundResource(R.drawable.badge_lost);
        } else {
            holder.typeText.setBackgroundResource(R.drawable.badge_found);
        }

        // Load image from Base64
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(item.getImageUrl(), Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                holder.itemImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                holder.itemImage.setImageResource(R.drawable.ic_image_placeholder);
            }
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Set button listeners
        holder.approveButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onApprove(item, holder.getAdapterPosition());
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReject(item, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class AdminViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView titleText, descriptionText, categoryText, locationText, typeText, userEmailText, timestampText;
        Button approveButton, rejectButton;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            titleText = itemView.findViewById(R.id.item_title);
            descriptionText = itemView.findViewById(R.id.item_description);
            categoryText = itemView.findViewById(R.id.item_category);
            locationText = itemView.findViewById(R.id.item_location);
            typeText = itemView.findViewById(R.id.item_type);
            userEmailText = itemView.findViewById(R.id.item_user_email);
            timestampText = itemView.findViewById(R.id.item_timestamp);
            approveButton = itemView.findViewById(R.id.btn_approve);
            rejectButton = itemView.findViewById(R.id.btn_reject);
        }
    }
}