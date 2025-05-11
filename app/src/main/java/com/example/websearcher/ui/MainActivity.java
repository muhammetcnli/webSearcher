package com.example.websearcher.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.websearcher.R;
import com.example.websearcher.model.Article;
import com.example.websearcher.repository.ArticleRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements AddLinkBottomSheetFragment.OnUrlEnteredListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerViewArticles;
    private View emptyView;
    private ArticleAdapter articleAdapter;
    private List<Article> articleList;
    private List<Article> filteredArticleList;
    private FloatingActionButton fabAddLink;
    private TabLayout tabLayout;

    private static final int FILTER_ALL = 0;
    private static final int FILTER_UNREAD = 1;
    private static final int FILTER_READ = 2;
    private int currentFilter = FILTER_UNREAD;

    private DatabaseReference articlesRef;
    private DatabaseReference usersRef;
    private String currentUid;
    private ValueEventListener articlesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved theme before setting content view
        applyUserPreferences();

        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);

        // Check if user is logged in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Get current user ID
        currentUid = user.getUid();

        // Setup Firebase references
        articlesRef = FirebaseDatabase.getInstance().getReference("articles");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        setupUI();
        setupRecyclerView();
        loadUserData();
        loadArticles();
        setupListeners();
    }

    private void applyUserPreferences() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Apply theme preference
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Apply language preference
        String language = prefs.getString("app_lang", "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

    private void setupUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(item -> {
            handleNavigationItem(item);
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);
        emptyView = findViewById(R.id.emptyView);
        fabAddLink = findViewById(R.id.fabAddLink);
        tabLayout = findViewById(R.id.tabLayout);

        setupTabLayout();

        fabAddLink.setOnClickListener(v -> {
            AddLinkBottomSheetFragment bottomSheet = new AddLinkBottomSheetFragment();
            bottomSheet.setOnUrlEnteredListener(this);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void loadUserData() {
        // Get header view from navigation view
        View headerView = navigationView.getHeaderView(0);
        TextView tvFirstName = headerView.findViewById(R.id.tv_user_firstname);
        TextView tvLastName = headerView.findViewById(R.id.tv_user_lastname);
        TextView tvEmail = headerView.findViewById(R.id.tv_user_email);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Set email from FirebaseAuth
            String email = user.getEmail();
            tvEmail.setText(email);

            // Fetch user name from Firebase Database
            usersRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String firstName = snapshot.child("firstName").getValue(String.class);
                        String lastName = snapshot.child("lastName").getValue(String.class);
                        if (firstName != null && !firstName.isEmpty()) {
                            tvFirstName.setText(firstName);
                            tvLastName.setText(lastName);
                        } else {
                            tvFirstName.setText(R.string.example_name);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, R.string.error_user_data, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupRecyclerView() {
        articleList = new ArrayList<>();
        filteredArticleList = new ArrayList<>();
        articleAdapter = new ArticleAdapter(filteredArticleList, article -> {
            String urlString = article.getUrl();
            if (urlString != null && !urlString.trim().isEmpty()) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(urlString)));
                article.setRead(true);
                articlesRef.child(article.getId()).child("read").setValue(true);
                applyFilter();
            } else {
                Toast.makeText(this, R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
            }
        });

        recyclerViewArticles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArticles.setAdapter(articleAdapter);

        setupSwipeActions();
    }

    private void loadArticles() {
        articlesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                articleList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Article a = ds.getValue(Article.class);
                    if (a != null) {
                        a.setId(ds.getKey());
                        articleList.add(a);
                    }
                }
                applyFilter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, R.string.error_article_data + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void setupListeners() {
        articlesRef.orderByChild("userId").equalTo(currentUid)
                .addValueEventListener(articlesListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (articlesListener != null) {
            articlesRef.orderByChild("userId").equalTo(currentUid)
                    .removeEventListener(articlesListener);
        }
    }

    @Override
    public void onUrlEntered(String url) {
        new Thread(() -> {
            try {
                Article article = new ArticleRepository().fetchArticle(url);
                article.setRead(false);
                article.setUserId(currentUid);
                String key = articlesRef.push().getKey();
                if (key != null) {
                    article.setId(key);
                    articlesRef.child(key).setValue(article);
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(
                        this,
                        getString(R.string.toast_error, e.getMessage()),
                        Toast.LENGTH_SHORT
                ).show());
            }
        }).start();
    }

    private void handleNavigationItem(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_theme) {
            toggleTheme();
        } else if (id == R.id.nav_language) {
            toggleLanguage();
        } else {
            Toast.makeText(this, R.string.toast_coming_soon, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleTheme() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        SharedPreferences.Editor editor = prefs.edit();
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean("dark_mode", false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor.putBoolean("dark_mode", true);
        }
        editor.apply();
        recreate(); // Restart activity
    }

    private void toggleLanguage() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String currentLang = prefs.getString("app_lang", "en");
        String newLang = currentLang.equals("en") ? "tr" : "en";
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("app_lang", newLang);
        editor.apply();

        Locale locale = new Locale(newLang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        recreate(); // Restart activity
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_all));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_unread));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_read));
        tabLayout.selectTab(tabLayout.getTabAt(FILTER_UNREAD));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) { currentFilter = tab.getPosition(); applyFilter(); }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) { return false; }
            @Override public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Article article = filteredArticleList.get(pos);
                if (direction == ItemTouchHelper.RIGHT) {
                    article.setRead(true);
                    articlesRef.child(article.getId()).child("read").setValue(true);
                    Toast.makeText(MainActivity.this, R.string.toast_marked_read, Toast.LENGTH_SHORT).show();
                } else {
                    articlesRef.child(article.getId()).removeValue();
                    Toast.makeText(MainActivity.this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                }
                applyFilter();
            }
            @Override public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                float ICON_SIZE = 48 * getResources().getDisplayMetrics().density;
                View itemView = viewHolder.itemView;
                Drawable icon;
                int backgroundColor;
                float iconMargin = (itemView.getHeight() - ICON_SIZE) / 2f;
                float iconTop = itemView.getTop() + iconMargin;
                float iconBottom = iconTop + ICON_SIZE;
                if (dX > 0) {
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_eye);
                    backgroundColor = ContextCompat.getColor(MainActivity.this, R.color.swipe_read_background);
                    c.drawRect(itemView.getLeft(), itemView.getTop(), itemView.getLeft() + dX, itemView.getBottom(), new Paint() {{ setColor(backgroundColor); }});
                    float iconLeft = itemView.getLeft() + iconMargin;
                    float iconRight = iconLeft + ICON_SIZE;
                    icon.setBounds((int)iconLeft, (int)iconTop, (int)iconRight, (int)iconBottom);
                    icon.draw(c);
                } else if (dX < 0) {
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    backgroundColor = ContextCompat.getColor(MainActivity.this, R.color.swipe_delete_background);
                    c.drawRect(itemView.getRight() + dX, itemView.getTop(), itemView.getRight(), itemView.getBottom(), new Paint() {{ setColor(backgroundColor); }});
                    float iconRight = itemView.getRight() - iconMargin;
                    float iconLeft = iconRight - ICON_SIZE;
                    icon.setBounds((int)iconLeft, (int)iconTop, (int)iconRight, (int)iconBottom);
                    icon.draw(c);
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerViewArticles);
    }

    private void applyFilter() {
        filteredArticleList.clear();
        for (Article a : articleList) {
            if (currentFilter == FILTER_ALL ||
                    (currentFilter == FILTER_UNREAD && !a.isRead()) ||
                    (currentFilter == FILTER_READ && a.isRead())) {
                filteredArticleList.add(a);
            }
        }
        articleAdapter.notifyDataSetChanged();
        updateEmptyViewVisibility();
    }

    private void updateEmptyViewVisibility() {
        if (filteredArticleList.isEmpty()) {
            recyclerViewArticles.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerViewArticles.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }
}