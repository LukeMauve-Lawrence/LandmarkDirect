package com.example.landmarkdirect;

import com.google.android.libraries.places.api.model.PhotoMetadata;

import java.util.ArrayList;
import java.util.List;
/*
    Model Class to retrieve data from firebase
 */
public class FavouriteLandmarks {
    public String placeAddress, placeName, placeWebUri, placeId;

    public FavouriteLandmarks(String placeAddress, String placeName, String placeWebUri, String placeId) {
        this.placeAddress = placeAddress;
        this.placeName = placeName;
        this.placeWebUri = placeWebUri;
        this.placeId = placeId;
    }

    public FavouriteLandmarks() {

    }
}
