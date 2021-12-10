package com.example.landmarkdirect;

//Model class to use when getting data from Firebase
public class User {

    public String firstName, email, measurementSystem, preferredLandmark;

    public User() {

    }

    public User(String firstName, String email, String measurementSystem, String preferredLandmark) {
        this.firstName = firstName;
        this.email = email;
        this.measurementSystem = measurementSystem;
        this.preferredLandmark = preferredLandmark;
    }
}
