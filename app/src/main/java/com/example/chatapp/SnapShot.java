package com.example.chatapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class SnapShot extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
