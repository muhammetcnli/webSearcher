package com.example.websearcher.repository;

import com.example.websearcher.model.Article;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class ArticleRepository {

    // Average wpm here
    private final double AVERAGE_WPM = 200.0;
    private DatabaseReference mDatabase;

    public ArticleRepository() {
        // Firebase Realtime Database referansını alıyoruz
        mDatabase = FirebaseDatabase.getInstance().getReference("articles");
    }

    /**
     * Returns a title, image URL, and reading time for a given URL.
     */
    public Article fetchArticle(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0")
                .timeout(10_000)
                .get();

        // Get title from Document object
        String title = doc.title();

        // image: og:image or first <img>
        String imageUrl = null;
        // 1. Favicon (icon / shortcut icon) dene
        Element iconLink = doc.selectFirst("link[rel~=(?i)^(icon|shortcut icon)$]");
        if (iconLink != null) {
            imageUrl = iconLink.absUrl("href");

            // 2. Sonra Open Graph görsel meta’sı
        } else if (doc.selectFirst("meta[property=og:image]") != null) {
            imageUrl = doc.selectFirst("meta[property=og:image]").attr("content");

            // 3. En sonunda ilk <img> etiketi
        } else if (doc.selectFirst("img") != null) {
            imageUrl = doc.selectFirst("img").absUrl("src");
        }

        // reading time: estimated reading time in minutes
        String text = doc.body().text();
        int wordCount = text.split("\\s+").length;
        int readingTime = (int) Math.ceil(wordCount / AVERAGE_WPM); // Divide wordCount to get reading time in minutes

        // Yeni Article nesnesi oluştur
        Article article = new Article(url, title, imageUrl, readingTime, false);

        // Article'ı Firebase'e kaydet
        saveArticleToFirebase(article);

        return article;
    }

    // Article'ı Firebase'e kaydetme işlemi
    private void saveArticleToFirebase(Article article) {
        // Firebase'e benzersiz bir ID ile veri kaydediyoruz
        String articleId = mDatabase.push().getKey();
        if (articleId != null) {
            mDatabase.child(articleId).setValue(article)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            System.out.println("Makale başarıyla kaydedildi: " + article.getTitle());
                        } else {
                            System.out.println("Hata: " + task.getException().getMessage());
                        }
                    });
        }
    }
}
