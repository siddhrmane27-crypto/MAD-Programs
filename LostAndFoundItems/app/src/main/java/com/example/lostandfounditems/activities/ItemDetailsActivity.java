package com.example.lostandfounditems.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.services.FirebaseAuthService;
import com.example.lostandfounditems.services.FirestoreService;
import com.example.lostandfounditems.services.StorageService;
import com.example.lostandfounditems.utils.Constants;
import com.example.lostandfounditems.utils.TimeUtils;
import com.google.android.material.button.MaterialButton;

public class ItemDetailsActivity extends AppCompatActivity {

    private ImageView ivImage;
    private TextView tvTitle, tvType, tvCategory, tvLocation, tvDescription, tvDate, tvContact, tvResolved;
    private MaterialButton btnContact, btnDelete, btnShare, btnEdit, btnResolve;
    private FirestoreService firestoreService;
    private FirebaseAuthService authService;
    private ItemModel currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        firestoreService = new FirestoreService();
        authService = new FirebaseAuthService();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Item Details");
        }

        ivImage       = findViewById(R.id.iv_item_image);
        tvTitle       = findViewById(R.id.tv_title);
        tvType        = findViewById(R.id.tv_type);
        tvCategory    = findViewById(R.id.tv_category);
        tvLocation    = findViewById(R.id.tv_location);
        tvDescription = findViewById(R.id.tv_description);
        tvDate        = findViewById(R.id.tv_date);
        tvContact     = findViewById(R.id.tv_contact);
        tvResolved    = findViewById(R.id.tv_resolved);
        btnContact    = findViewById(R.id.btn_contact);
        btnShare      = findViewById(R.id.btn_share);
        btnEdit       = findViewById(R.id.btn_edit);
        btnDelete     = findViewById(R.id.btn_delete);
        btnResolve    = findViewById(R.id.btn_resolve);

        String itemId = getIntent().getStringExtra(Constants.EXTRA_ITEM_ID);
        if (itemId == null) { finish(); return; }
        loadItem(itemId);
    }

    private void loadItem(String itemId) {
        firestoreService.getItemById(itemId,
                item -> {
                    if (item == null) { finish(); return; }
                    currentItem = item;
                    bindItem(item);
                },
                e -> {
                    Toast.makeText(this, "Failed to load item", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void bindItem(ItemModel item) {
        tvTitle.setText(item.getTitle());
        tvType.setText(item.getType());
        tvCategory.setText("📦 " + item.getCategory());
        tvLocation.setText("📍 " + item.getLocation());
        tvDescription.setText(item.getDescription());
        tvContact.setText("✉️  " + item.getUserEmail());
        tvDate.setText("🕐 " + TimeUtils.getTimeAgo(item.getTimestamp()));

        tvType.setBackgroundResource(item.getType().equalsIgnoreCase("Lost")
                ? R.drawable.badge_lost : R.drawable.badge_found);

        // Resolved banner
        if (item.isResolved()) {
            tvResolved.setVisibility(View.VISIBLE);
        } else {
            tvResolved.setVisibility(View.GONE);
        }

        // Load image
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            new Thread(() -> {
                Bitmap bmp = StorageService.decodeBase64(item.getImageUrl());
                if (bmp != null) runOnUiThread(() -> ivImage.setImageBitmap(bmp));
            }).start();
        } else {
            ivImage.setImageResource(R.drawable.ic_image_placeholder);
        }

        btnContact.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:" + item.getUserEmail()));
            intent.putExtra(Intent.EXTRA_SUBJECT,
                    "Regarding your " + item.getType() + " item: " + item.getTitle());
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        btnShare.setOnClickListener(v -> shareItem(item));

        // Owner-only buttons (Edit and Delete)
        String currentUid = authService.getCurrentUser() != null
                ? authService.getCurrentUser().getUid() : "";
        String currentEmail = authService.getCurrentUser() != null
                ? authService.getCurrentUser().getEmail() : "";
        boolean isAdmin = Constants.ADMIN_EMAIL.equals(currentEmail);

        if (currentUid.equals(item.getUserId())) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);

            // Edit
            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, EditItemActivity.class);
                intent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());
                startActivity(intent);
            });

            // Delete
            btnDelete.setOnClickListener(v -> confirmDelete(item));
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        // Mark as Resolved - ONLY for admin
        if (isAdmin) {
            btnResolve.setVisibility(View.VISIBLE);
            updateResolveButton(item.isResolved());
            btnResolve.setOnClickListener(v -> toggleResolved(item));
        } else {
            btnResolve.setVisibility(View.GONE);
        }
    }

    private void updateResolveButton(boolean resolved) {
        if (resolved) {
            btnResolve.setText("↩️  Mark as Unresolved");
        } else {
            btnResolve.setText("✅  Mark as Resolved");
        }
    }

    private void toggleResolved(ItemModel item) {
        boolean newState = !item.isResolved();
        firestoreService.markResolved(item.getItemId(), newState, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                item.setResolved(newState);
                updateResolveButton(newState);
                tvResolved.setVisibility(newState ? View.VISIBLE : View.GONE);
                String msg = newState ? "Marked as resolved ✅" : "Marked as unresolved";
                Toast.makeText(ItemDetailsActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ItemDetailsActivity.this, "Failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareItem(ItemModel item) {
        String text = "🔍 " + item.getType().toUpperCase() + " ITEM\n\n"
                + "📌 " + item.getTitle() + "\n"
                + "📦 Category: " + item.getCategory() + "\n"
                + "📍 Location: " + item.getLocation() + "\n"
                + "✉️ Contact: " + item.getUserEmail() + "\n\n"
                + "Posted via Lost & Found App";
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void confirmDelete(ItemModel item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete \"" + item.getTitle() + "\"?")
                .setPositiveButton("Delete", (d, w) -> deleteItem(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteItem(ItemModel item) {
        firestoreService.deleteItem(item.getItemId(), new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ItemDetailsActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(ItemDetailsActivity.this, "Delete failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload after returning from EditItemActivity
        if (currentItem != null) loadItem(currentItem.getItemId());
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
