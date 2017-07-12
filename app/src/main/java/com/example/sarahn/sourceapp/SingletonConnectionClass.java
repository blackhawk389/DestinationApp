package com.example.sarahn.sourceapp;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by SarahN on 7/10/2017.
 */
public class SingletonConnectionClass {

    private static SingletonConnectionClass instance = null;
    private SingletonConnectionClass() {}

    public static SingletonConnectionClass getInstance() {
        if(instance == null) {
            instance = new SingletonConnectionClass();
        }
        return instance;
    }

    public static DatabaseReference firebaseSetup(){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        DatabaseReference data = mDatabase.child("latlng");
        return data;
    }
}
