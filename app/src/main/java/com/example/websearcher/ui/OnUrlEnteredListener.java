package com.example.websearcher.ui;

// OnUrlEnteredListener arayüzü, URL girildiğinde bir işlem yapılmasını sağlar
// Bu arayüz, URL'nin girilmesi durumunda onUrlEntered metodunun çağrılmasını sağlar
// URL'nin girilmesi ile ilgili işlemler, bu arayüzü implement eden sınıf içinde tanımlanır

public interface OnUrlEnteredListener {
    void onUrlEntered(String url);
}