package com.example.lostandfounditems.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.activities.ItemDetailsActivity;
import com.example.lostandfounditems.adapters.ItemAdapter;
import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.services.FirebaseAuthService;
import com.example.lostandfounditems.services.FirestoreService;
import com.example.lostandfounditems.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private FirestoreService firestoreService;
    private FirebaseAuthService authService;
    private SearchView searchView;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private ChipGroup chipGroup;
    private String activeCategory = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestoreService = new FirestoreService();
        authService = new FirebaseAuthService();

        // Greeting
        TextView tvGreeting = view.findViewById(R.id.tv_greeting);
        FirebaseUser user = authService.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String name = user.getEmail().split("@")[0];
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            tvGreeting.setText("Hey, " + name + " 👋");
        }

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState = view.findViewById(R.id.empty_state);
        chipGroup = view.findViewById(R.id.chip_group);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ItemAdapter(getContext(), item -> {
            Intent intent = new Intent(getContext(), ItemDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        searchView = view.findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String q) { adapter.filter(q); return true; }
            @Override public boolean onQueryTextChange(String q) { adapter.filter(q); return true; }
        });

        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.accent);
        swipeRefresh.setOnRefreshListener(() -> loadItems());

        setupCategoryChips();
        loadItems();
    }

    private void setupCategoryChips() {
        // "All" chip
        Chip chipAll = new Chip(requireContext());
        chipAll.setText("All");
        chipAll.setCheckable(true);
        chipAll.setChecked(true);
        chipGroup.addView(chipAll);

        for (String cat : Constants.CATEGORIES) {
            Chip chip = new Chip(requireContext());
            chip.setText(cat);
            chip.setCheckable(true);
            chipGroup.addView(chip);
        }

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) { activeCategory = ""; adapter.filterByCategory(""); return; }
            Chip selected = group.findViewById(checkedIds.get(0));
            if (selected != null) {
                String label = selected.getText().toString();
                activeCategory = label.equals("All") ? "" : label;
                adapter.filterByCategory(activeCategory);
            }
        });
    }

    private void loadItems() {
        swipeRefresh.setRefreshing(true);
        firestoreService.getAllItems(new FirestoreService.ItemsCallback() {
            @Override
            public void onSuccess(List<ItemModel> items) {
                if (getView() == null) return;
                swipeRefresh.setRefreshing(false);
                adapter.setItems(items);
                emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onFailure(String error) {
                if (getView() == null) return;
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadItems();
    }
}
