package com.example.websearcher.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.websearcher.R;
import com.example.websearcher.model.Article;

import java.util.List;

// ArticleAdapter, makale verilerini RecyclerView içinde listelemek için kullanılır
// Adapter, her bir makale için başlık, okuma süresi, resim ve okundu/okunmadı durumuna göre tik işareti ekler
// Glide kütüphanesi ile makale resimleri yüklenir (placeholder ve error resimleri de dahil)
// "tick" simgesi, makalenin okunduğunda görünür olur
// Kullanıcı bir makaleye tıkladığında, onArticleClickListener ile makale bilgisi dışarıya iletilir

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private final List<Article> articles;
    private final OnArticleClickListener onArticleClickListener;

    // Constructor with onArticleClickListener
    public ArticleAdapter(List<Article> articles, OnArticleClickListener onArticleClickListener) {
        this.articles = articles;
        this.onArticleClickListener = onArticleClickListener;
    }

    // ViewHolder class
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView readingTimeTextView;
        public ImageView articleImageView;

        // add tick
        public ImageView tickImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewTitle);
            readingTimeTextView = itemView.findViewById(R.id.textViewReadingTime);
            articleImageView = itemView.findViewById(R.id.imageViewIcon);
            tickImageView = itemView.findViewById(R.id.imageViewTick);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);

        // Mevcut başlık, süre, resim yükleme vb.
        holder.titleTextView.setText(article.getTitle());
        String readingTimeText = holder.itemView.getContext()
                .getString(R.string.reading_time_format, article.getReadingTime());
        holder.readingTimeTextView.setText(readingTimeText);

        if (article.getIconUrl() != null && !article.getIconUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(article.getIconUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.articleImageView);
        } else {
            holder.articleImageView.setImageResource(R.drawable.placeholder_image);
        }

        // isRead’e göre tick’i göster/gizle
        if (article.isRead()) {
            holder.tickImageView.setVisibility(View.VISIBLE);
        } else {
            holder.tickImageView.setVisibility(View.GONE);
        }

        // Tıklanma olayı
        holder.itemView.setOnClickListener(v -> {
            if (onArticleClickListener != null) {
                onArticleClickListener.onArticleClick(article);
            }
        });
    }


    @Override
    public int getItemCount() {
        return articles.size();
    }

    // OnArticleClickListener interface
    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }
}
