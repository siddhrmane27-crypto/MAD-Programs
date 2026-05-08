package com.example.lostandfounditems.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.activities.ItemDetailsActivity;
import com.example.lostandfounditems.activities.LoginActivity;
import com.example.lostandfounditems.adapters.ItemAdapter;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.services.FirebaseAuthService;
import com.example.lostandfounditems.services.FirestoreService;
import com.example.lostandfounditems.utils.Constants;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail, tvStatTotal, tvStatLost, tvStatFound;
    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private Button btnLogout, btnAdminPanel;
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authService = new FirebaseAuthService();
        firestoreService = new FirestoreService();

        tvName = view.findViewById(R.id.tv_user_name);
        tvEmail = view.findViewById(R.id.tv_user_email);
        tvStatTotal = view.findViewById(R.id.tv_stat_total);
        tvStatLost = view.findViewById(R.id.tv_stat_lost);
        tvStatFound = view.findViewById(R.id.tv_stat_found);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnAdminPanel = view.findViewById(R.id.btn_admin_panel);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(getContext(), item -> {
            Intent intent = new Intent(getContext(), ItemDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        FirebaseUser user = authService.getCurrentUser();
        if (user != null) {
            tvEmail.setText(user.getEmail());
            loadUserProfile(user.getUid());
            loadUserItems(user.getUid());
            
            // Check if this is the admin email
            if (Constants.ADMIN_EMAIL.equals(user.getEmail())) {
                btnAdminPanel.setVisibility(View.VISIBLE);
            } else {
                btnAdminPanel.setVisibility(View.GONE);
            }
        }

        btnAdminPanel.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), com.example.lostandfounditems.activities.AdminPanelActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            authService.logoutUser();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadUserProfile(String uid) {
        firestoreService.getUser(uid,
                user -> {
                    if (user != null && getView() != null)
                        tvName.setText(user.getName());
                },
                e -> {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserItems(String uid) {
        firestoreService.getUserItems(uid, new FirestoreService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getView() == null) return;
                adapter.setItems(items);

                // Stats
                int lost = 0, found = 0;
                for (ItemModel item : items) {
                    if (item.getType().equalsIgnoreCase(Constants.TYPE_LOST)) lost++;
                    else found++;
                }
                tvStatTotal.setText(String.valueOf(items.size()));
                tvStatLost.setText(String.valueOf(lost));
                tvStatFound.setText(String.valueOf(found));
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null)
                    Toast.makeText(getContext(), "Failed to load items", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseUser user = authService.getCurrentUser();
        if (user != null) loadUserItems(user.getUid());
    }
}
