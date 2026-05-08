package com.example.lostandfounditems.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.models.UserModel;
import com.example.lostandfounditems.services.FirebaseAuthService;
import com.example.lostandfounditems.services.FirestoreService;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progressBar;
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        authService = new FirebaseAuthService();
        firestoreService = new FirestoreService();

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);

        btnRegister.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.tv_login).setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("Name required"); return; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email required"); return; }
        if (TextUtils.isEmpty(password)) { etPassword.setError("Password required"); return; }
        if (password.length() < 6) { etPassword.setError("Min 6 characters"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnRegister.setEnabled(false);

        authService.registerUser(email, password, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                String uid = authService.getCurrentUser().getUid();
                
                // Check if this is the first user (admin)
                firestoreService.getAllUsers(new FirestoreService.UsersCallback() {
                    @Override
                    public void onSuccess(java.util.List<UserModel> users) {
                        boolean isFirstUser = users.isEmpty();
                        UserModel user = new UserModel(uid, name, email, isFirstUser);
                        
                        firestoreService.saveUser(user, new FirestoreService.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                                if (isFirstUser) {
                                    Toast.makeText(RegisterActivity.this, "Welcome Admin! You have admin privileges.", Toast.LENGTH_LONG).show();
                                }
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finishAffinity();
                            }

                            @Override
                            public void onFailure(String error) {
                                progressBar.setVisibility(View.GONE);
                                btnRegister.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Profile save failed: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(String error) {
                        // If we can't check users, create as regular user
                        UserModel user = new UserModel(uid, name, email, false);
                        firestoreService.saveUser(user, new FirestoreService.SimpleCallback() {
                            @Override
                            public void onSuccess() {
                                progressBar.setVisibility(View.GONE);
                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                finishAffinity();
                            }

                            @Override
                            public void onFailure(String error) {
                                progressBar.setVisibility(View.GONE);
                                btnRegister.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Profile save failed: " + error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
