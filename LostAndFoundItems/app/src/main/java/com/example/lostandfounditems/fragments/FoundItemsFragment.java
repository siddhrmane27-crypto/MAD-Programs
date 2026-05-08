package com.example.lostandfounditems.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import com.example.lostandfounditems.services.FirestoreService;
import com.example.lostandfounditems.utils.Constants;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class FoundItemsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private FirestoreService firestoreService;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout emptyState;
    private ChipGroup chipGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lost_found_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firestoreService = new FirestoreService();

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        emptyState   = view.findViewById(R.id.empty_state);
        recyclerView = view.findViewById(R.id.recycler_view);
        chipGroup    = view.findViewById(R.id.chip_group);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ItemAdapter(getContext(), item -> {
            Intent intent = new Intent(getContext(), ItemDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_ITEM_ID, item.getItemId());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        setupChips();
        swipeRefresh.setColorSchemeResources(R.color.found_color);
        swipeRefresh.setOnRefreshListener(this::loadFoundItems);
        loadFoundItems();
    }

    private void setupChips() {
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
            if (checkedIds.isEmpty()) { adapter.filterByCategory(""); return; }
            Chip selected = group.findViewById(checkedIds.get(0));
            if (selected != null) {
                String label = selected.getText().toString();
                adapter.filterByCategory(label.equals("All") ? "" : label);
            }
        });
    }

    private void loadFoundItems() {
        swipeRefresh.setRefreshing(true);
        firestoreService.getFoundItems(new FirestoreService.ItemsCallback() {
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
        loadFoundItems();
    }
}
