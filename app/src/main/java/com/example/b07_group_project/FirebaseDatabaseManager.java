package com.example.b07_group_project;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseDatabaseManager {

    // The single, static instance of this class
    private static FirebaseDatabaseManager instance;

    private final FirebaseDatabase database;

    private static final String DATABASE_URL = "https://b07-project-fa3a5-default-rtdb.firebaseio.com/";

    /**
     * Private constructor to prevent anyone else from creating an instance.
     */
    private FirebaseDatabaseManager() {
        // Initialize with a specific database URL (needed without google-services.json)
        database = FirebaseDatabase.getInstance(DATABASE_URL);
    }

    /**
     * The static method to get the single instance of this manager.
     * This is the global access point for the manager.
     *
     * @return The singleton instance of FirebaseDatabaseManager.
     */
    public static synchronized FirebaseDatabaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseDatabaseManager();
        }
        return instance;
    }

    /**
     * Provides a reference to the root of your Firebase Realtime Database.
     *
     * @return A DatabaseReference to the root.
     */
    public DatabaseReference getDatabaseReference() {
        return database.getReference();
    }
}