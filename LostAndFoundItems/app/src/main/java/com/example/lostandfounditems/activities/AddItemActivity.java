package com.example.lostandfounditems.activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.services.FirebaseAuthService;
import com.example.lostandfounditems.services.FirestoreService;
import com.example.lostandfounditems.services.StorageService;
import com.example.lostandfounditems.utils.Constants;

import java.io.IOException;
import java.io.InputStream;

public class AddItemActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etLocation;
    private Spinner spinnerCategory;
    private RadioGroup rgType;
    private RadioButton rbLost, rbFound;
    private ImageView ivPreview;
    private Button btnPickImage, btnSubmit;
    private ProgressBar progressBar;

    private Uri selectedImageUri;
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;
    private StorageService storageService;
    private android.widget.FrameLayout framePreview;
    private android.widget.Button btnRemoveImage;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivPreview.setImageURI(uri);
                    framePreview.setVisibility(View.VISIBLE);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        authService = new FirebaseAuthService();
        firestoreService = new FirestoreService();
        storageService = new StorageService();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Item");
        }

        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etLocation = findViewById(R.id.et_location);
        spinnerCategory = findViewById(R.id.spinner_category);
        rgType = findViewById(R.id.rg_type);
        rbLost = findViewById(R.id.rb_lost);
        rbFound = findViewById(R.id.rb_found);
        ivPreview = findViewById(R.id.iv_preview);
        btnPickImage = findViewById(R.id.btn_pick_image);
        btnSubmit = findViewById(R.id.btn_submit);
        progressBar = findViewById(R.id.progress_bar);
        framePreview = findViewById(R.id.frame_preview);
        btnRemoveImage = findViewById(R.id.btn_remove_image);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Constants.CATEGORIES);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        btnPickImage.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSubmit.setOnClickListener(v -> submitItem());
        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            framePreview.setVisibility(android.view.View.GONE);
        });
    }

    private void submitItem() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Title required"); return; }
        if (TextUtils.isEmpty(description)) { etDescription.setError("Description required"); return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("Location required"); return; }
        if (rgType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Select Lost or Found", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = rbLost.isChecked() ? Constants.TYPE_LOST : Constants.TYPE_FOUND;
        String userId = authService.getCurrentUser().getUid();
        String userEmail = authService.getCurrentUser().getEmail();

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        if (selectedImageUri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                // Run compression on a background thread to avoid blocking UI
                new Thread(() -> storageService.uploadImage(is, new StorageService.UploadCallback() {
                    @Override
                    public void onSuccess(String base64Image) {
                        runOnUiThread(() -> saveItem(title, description, category, type,
                                location, base64Image, userId, userEmail));
                    }

                    @Override
                    public void onFailure(String error) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            btnSubmit.setEnabled(true);
                            Toast.makeText(AddItemActivity.this,
                                    "Image processing failed: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                })).start();
            } catch (IOException e) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(this, "Failed to read image", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveItem(title, description, category, type, location, "", userId, userEmail);
        }
    }

    private void saveItem(String title, String description, String category, String type,
                          String location, String imageData, String userId, String userEmail) {
        ItemModel item = new ItemModel("", title, description, category, type,
                location, imageData, userId, userEmail, System.currentTimeMillis());

        firestoreService.addItem(item, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddItemActivity.this, "Item posted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(AddItemActivity.this, "Failed to save item: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
