package com.example.websearcher.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.websearcher.R;
import com.example.websearcher.model.Article;
import com.example.websearcher.repository.ArticleRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements AddLinkBottomSheetFragment.OnUrlEnteredListener {

    private RecyclerView recyclerViewArticles;
    private View emptyView;
    private ArticleAdapter articleAdapter;
    private List<Article> articleList;
    private List<Article> filteredArticleList;
    private FloatingActionButton fabAddLink;
    private TabLayout tabLayout;
    private FirebaseAuth mAuth;

    // Filtre seçenekleri
    private static final int FILTER_ALL = 0;
    private static final int FILTER_UNREAD = 1;
    private static final int FILTER_READ = 2;
    private int currentFilter = FILTER_UNREAD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_WebSearcher);
        setContentView(R.layout.activity_main);

        // Butonu Tanımla
        Button themeToggleButton = findViewById(R.id.themeToggleButton);

        // Geçerli Tema Durumunu Kontrol Et
        SharedPreferences sharedPreferences = getSharedPreferences("ThemePrefs", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // Butona Tıklama İşlemi
        themeToggleButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("isDarkMode", false);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("isDarkMode", true);
            }
            editor.apply();

            // Aktiviteyi Yeniden Başlat
            recreate();
        });
        // View'leri bağla
        recyclerViewArticles = findViewById(R.id.recyclerViewArticles);
        emptyView = findViewById(R.id.emptyView);
        fabAddLink = findViewById(R.id.fabAddLink);
        tabLayout = findViewById(R.id.tabLayout);

        setupTabLayout();

        articleList = new ArrayList<>();
        filteredArticleList = new ArrayList<>();
        articleAdapter = new ArticleAdapter(filteredArticleList, article -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(article.getUrl()));
            startActivity(intent);
            article.setRead(true);
            applyFilter();
        });

        recyclerViewArticles.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewArticles.setAdapter(articleAdapter);

        setupSwipeActions();
        loadData();

        fabAddLink.setOnClickListener(v -> {
            AddLinkBottomSheetFragment bottomSheet = new AddLinkBottomSheetFragment();
            bottomSheet.setOnUrlEnteredListener(this);
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        });
    }

    private void setupTabLayout() {
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_all));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_unread));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.filter_read));
        tabLayout.selectTab(tabLayout.getTabAt(FILTER_UNREAD));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentFilter = tab.getPosition();
                applyFilter();
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupSwipeActions() {
        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
        ) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                Article article = filteredArticleList.get(pos);
                if (direction == ItemTouchHelper.RIGHT) {
                    article.setRead(true);
                    Toast.makeText(MainActivity.this,
                            R.string.toast_marked_read,
                            Toast.LENGTH_SHORT).show();
                } else {
                    articleList.remove(article);
                    Toast.makeText(MainActivity.this,
                            R.string.toast_deleted,
                            Toast.LENGTH_SHORT).show();
                }
                applyFilter();
                articleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c,
                                    @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
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
                    float iconLeft = itemView.getLeft() + iconMargin;
                    float iconRight = iconLeft + ICON_SIZE;
                    c.drawRect(itemView.getLeft(), itemView.getTop(),
                            itemView.getLeft() + dX, itemView.getBottom(),
                            new Paint() {{ setColor(backgroundColor); }});
                    icon.setBounds((int) iconLeft, (int) iconTop,
                            (int) iconRight, (int) iconBottom);
                    icon.draw(c);
                } else if (dX < 0) {
                    icon = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_delete);
                    backgroundColor = ContextCompat.getColor(MainActivity.this, R.color.swipe_delete_background);
                    float iconRight = itemView.getRight() - iconMargin;
                    float iconLeft = iconRight - ICON_SIZE;
                    c.drawRect(itemView.getRight() + dX, itemView.getTop(),
                            itemView.getRight(), itemView.getBottom(),
                            new Paint() {{ setColor(backgroundColor); }});
                    icon.setBounds((int) iconLeft, (int) iconTop,
                            (int) iconRight, (int) iconBottom);
                    icon.draw(c);
                }
            }
        };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerViewArticles);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyFilter();
    }

    @Override
    public void onUrlEntered(String url) {
        fetchArticle(url);
    }

    private void fetchArticle(String url) {
        new Thread(() -> {
            try {
                Article article = new ArticleRepository().fetchArticle(url);
                article.setRead(false);
                runOnUiThread(() -> addArticleToList(article));
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

    private void addArticleToList(Article article) {
        articleList.add(0, article);
        applyFilter();
        recyclerViewArticles.scrollToPosition(0);
    }

    private void loadData() {
        applyFilter();
    }

    private void applyFilter() {
        filteredArticleList.clear();
        for (Article a : articleList) {
            if (currentFilter == FILTER_ALL
                    || (currentFilter == FILTER_UNREAD && !a.isRead())
                    || (currentFilter == FILTER_READ && a.isRead())) {
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
