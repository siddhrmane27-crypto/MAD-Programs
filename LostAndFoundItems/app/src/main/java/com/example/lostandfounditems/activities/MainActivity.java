package com.example.lostandfounditems.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.example.lostandfounditems.R;
import com.example.lostandfounditems.fragments.FoundItemsFragment;
import com.example.lostandfounditems.fragments.HomeFragment;
import com.example.lostandfounditems.fragments.LostItemsFragment;
import com.example.lostandfounditems.fragments.ProfileFragment;
import com.example.lostandfounditems.models.UserModel;
import com.example.lostandfounditems.services.FirebaseAuthService;
import com.example.lostandfounditems.services.FirestoreService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuthService authService;
    private FirestoreService firestoreService;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authService = new FirebaseAuthService();
        firestoreService = new FirestoreService();
        
        if (!authService.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        loadFragment(new HomeFragment());
        checkAdminStatus();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home)    { loadFragment(new HomeFragment());       return true; }
            if (id == R.id.nav_lost)    { loadFragment(new LostItemsFragment());  return true; }
            if (id == R.id.nav_found)   { loadFragment(new FoundItemsFragment()); return true; }
            if (id == R.id.nav_profile) { loadFragment(new ProfileFragment());    return true; }
            return false;
        });

        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddItemActivity.class)));
    }

    private void checkAdminStatus() {
        String currentUserId = authService.getCurrentUserId();
        if (currentUserId != null) {
            firestoreService.checkAdminStatus(currentUserId, new FirestoreService.UserCallback() {
                @Override
                public void onSuccess(UserModel user) {
                    if (user != null) {
                        isAdmin = user.isAdmin();
                        invalidateOptionsMenu(); // Refresh menu to show/hide admin option
                    }
                }

                @Override
                public void onFailure(String error) {
                    // Handle error silently, user remains non-admin
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isAdmin) {
            getMenuInflater().inflate(R.menu.admin_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_admin_panel) {
            startActivity(new Intent(this, AdminPanelActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
