package com.example.lostandfounditems.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.services.StorageService;
import com.example.lostandfounditems.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final Context context;
    private List<ItemModel> items;
    private List<ItemModel> allItems;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ItemModel item);
    }

    public ItemAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.items = new ArrayList<>();
        this.allItems = new ArrayList<>();
    }

    public void setItems(List<ItemModel> items) {
        this.items = new ArrayList<>(items);
        this.allItems = new ArrayList<>(items);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query == null || query.isEmpty()) {
            items = new ArrayList<>(allItems);
        } else {
            String lower = query.toLowerCase().trim();
            List<ItemModel> filtered = new ArrayList<>();
            for (ItemModel item : allItems) {
                if (item.getTitle().toLowerCase().contains(lower)
                        || item.getCategory().toLowerCase().contains(lower)
                        || item.getLocation().toLowerCase().contains(lower)) {
                    filtered.add(item);
                }
            }
            items = filtered;
        }
        notifyDataSetChanged();
    }

    public void filterByType(String type) {
        if (type == null || type.isEmpty()) {
            items = new ArrayList<>(allItems);
        } else {
            List<ItemModel> filtered = new ArrayList<>();
            for (ItemModel item : allItems) {
                if (item.getType().equalsIgnoreCase(type)) filtered.add(item);
            }
            items = filtered;
        }
        notifyDataSetChanged();
    }

    public void filterByCategory(String category) {
        if (category == null || category.isEmpty()) {
            items = new ArrayList<>(allItems);
        } else {
            List<ItemModel> filtered = new ArrayList<>();
            for (ItemModel item : allItems) {
                if (item.getCategory().equalsIgnoreCase(category)) filtered.add(item);
            }
            items = filtered;
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel item = items.get(position);
        holder.tvTitle.setText(item.getTitle());
        holder.tvLocation.setText("📍 " + item.getLocation());
        holder.tvType.setText(item.getType());
        holder.tvTime.setText(TimeUtils.getTimeAgo(item.getTimestamp()));

        if (item.getType().equalsIgnoreCase("Lost")) {
            holder.tvType.setBackgroundResource(R.drawable.badge_lost);
        } else {
            holder.tvType.setBackgroundResource(R.drawable.badge_found);
        }

        holder.tvResolvedBadge.setVisibility(item.isResolved() ? View.VISIBLE : View.GONE);

        holder.ivItem.setImageResource(R.drawable.ic_image_placeholder);
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            final ImageView target = holder.ivItem;
            new Thread(() -> {
                Bitmap bmp = StorageService.decodeBase64(item.getImageUrl());
                if (bmp != null) {
                    target.post(() -> {
                        target.setImageBitmap(bmp);
                        target.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    });
                }
            }).start();
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItem;
        TextView tvTitle, tvLocation, tvType, tvTime, tvResolvedBadge;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItem          = itemView.findViewById(R.id.iv_item_image);
            tvTitle         = itemView.findViewById(R.id.tv_item_title);
            tvLocation      = itemView.findViewById(R.id.tv_item_location);
            tvType          = itemView.findViewById(R.id.tv_item_type);
            tvTime          = itemView.findViewById(R.id.tv_item_time);
            tvResolvedBadge = itemView.findViewById(R.id.tv_resolved_badge);
        }
    }
}
