package com.example.remyscake;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class RemysApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Log.e("PasteleriaApp", "Error al habilitar la persistencia de Firebase: " + e.getMessage());
        }

    }
}
