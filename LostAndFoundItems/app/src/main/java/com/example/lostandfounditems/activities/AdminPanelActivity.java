package com.example.lostandfounditems.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.adapters.AdminItemAdapter;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.services.FirestoreService;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity implements AdminItemAdapter.OnAdminActionListener {

    private RecyclerView recyclerView;
    private AdminItemAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private FirestoreService firestoreService;
    private List<ItemModel> pendingItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        initViews();
        setupRecyclerView();
        loadPendingItems();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Panel");
        }

        recyclerView = findViewById(R.id.recycler_pending_items);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view);
        
        firestoreService = new FirestoreService();
        pendingItems = new ArrayList<>();
    }

    private void setupRecyclerView() {
        adapter = new AdminItemAdapter(pendingItems, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadPendingItems() {
        showLoading(true);
        firestoreService.getPendingItems(new FirestoreService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                showLoading(false);
                pendingItems.clear();
                pendingItems.addAll(items);
                adapter.notifyDataSetChanged();
                
                if (items.isEmpty()) {
                    showEmptyView(true);
                } else {
                    showEmptyView(false);
                }
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                showEmptyView(true);
                Toast.makeText(AdminPanelActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(ItemModel item, int position) {
        firestoreService.verifyItem(item.getItemId(), true, new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                pendingItems.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(AdminPanelActivity.this, "Item approved successfully", Toast.LENGTH_SHORT).show();
                
                if (pendingItems.isEmpty()) {
                    showEmptyView(true);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminPanelActivity.this, "Failed to approve: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReject(ItemModel item, int position) {
        firestoreService.deleteItem(item.getItemId(), new FirestoreService.SimpleCallback() {
            @Override
            public void onSuccess() {
                pendingItems.remove(position);
                adapter.notifyItemRemoved(position);
                Toast.makeText(AdminPanelActivity.this, "Item rejected and deleted", Toast.LENGTH_SHORT).show();
                
                if (pendingItems.isEmpty()) {
                    showEmptyView(true);
                }
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminPanelActivity.this, "Failed to reject: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyView(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}