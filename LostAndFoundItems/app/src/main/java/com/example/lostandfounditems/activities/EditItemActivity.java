package com.example.lostandfounditems.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.services.FirestoreService;
import com.example.lostandfounditems.utils.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import android.widget.Spinner;

public class EditItemActivity extends AppCompatActivity {

    private TextInputEditText etTitle, etDescription, etLocation;
    private Spinner spinnerCategory;
    private MaterialButton btnSave;
    private ProgressBar progressBar;
    private FirestoreService firestoreService;
    private ItemModel currentItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        firestoreService = new FirestoreService();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Item");
        }

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etLocation = findViewById(R.id.et_location);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save);
        progressBar = findViewById(R.id.progress_bar);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constants.CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // Load item data passed via intent
        String itemId = getIntent().getStringExtra(Constants.EXTRA_ITEM_ID);
        if (itemId == null) { finish(); return; }

        loadItem(itemId);
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadItem(String itemId) {
        progressBar.setVisibility(View.VISIBLE);
        firestoreService.getItemById(itemId,
                item -> {
                    if (item == null) { finish(); return; }
                    currentItem = item;
                    progressBar.setVisibility(View.GONE);
                    populateFields(item);
                },
                e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load item", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateFields(ItemModel item) {
        etTitle.setText(item.getTitle());
        etDescription.setText(item.getDescription());
        etLocation.setText(item.getLocation());

        // Set spinner to current category
        for (int i = 0; i < Constants.CATEGORIES.length; i++) {
            if (Constants.CATEGORIES[i].equals(item.getCategory())) {
                spinnerCategory.setSelection(i);
                break;
            }
        }
    }

    private void saveChanges() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Title required"); return; }
        if (TextUtils.isEmpty(description)) { etDescription.setError("Description required"); return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("Location required"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        firestoreService.updateItem(currentItem.getItemId(), title, description, category, location,
                new FirestoreService.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(EditItemActivity.this, "Item updated!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        btnSave.setEnabled(true);
                        Toast.makeText(EditItemActivity.this, "Update failed: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
