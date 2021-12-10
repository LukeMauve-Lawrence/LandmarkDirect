package com.example.landmarkdirect.ui.maps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.landmarkdirect.DirectionsJSONParser;
import com.example.landmarkdirect.FavouriteLandmarks;
import com.example.landmarkdirect.MyJsonParser;
import com.example.landmarkdirect.PlaceInfoFragment;
import com.example.landmarkdirect.R;
import com.example.landmarkdirect.databinding.FragmentMapsBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonParser;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DirectionsResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.landmarkdirect.MainActivity.locationPermissionGranted;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnPoiClickListener {

    private static final String TAG = "MapsFragment";

    private MapsViewModel mapsViewModel;
    private FragmentMapsBinding binding;

    private FragmentContainerView fragmentContainerView;
    private ConstraintLayout placeInfoPopUp;
    private Button directionButton, favouriteButton;
    private TextView placeInfoTitle, placeInfoDuration, placeInfoDistance;

    //maps variables
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int DEFAULT_ZOOM = 15;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location lastKnownLocation;
    private AutocompleteSupportFragment autocompleteFragment;
    private MarkerOptions options;
    private String apiKey;

    //nearby places
    //private ArrayList<String> ;
    private Map<String, String> placeIds = new HashMap<String, String>();
    private String currentPlaceId;
    private String currentPlaceAddress;
    private String currentPlaceName;
    private Uri currentPlaceWebUri;
    private List<PhotoMetadata> currentPlaceImage;
    private ImageView imageViewPlace;

    private GeoApiContext mGeoApiContext;

    //Directions
    private LatLng mOrigin;
    private LatLng mDestination;
    private Polyline mPolyline;
    ArrayList<LatLng> mMarkerPoints;
    private String mDuration = "";
    private String mDistance = "";

    //Firebase
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = database.getReference("Users");
    private String id;
    private String measurementSystem;
    private String landmarkType;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        @Override
        public void onMapReady(GoogleMap googleMap) {
            map = googleMap;
            LatLng sydney = new LatLng(-34, 151);
            googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

            if (locationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(getContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                updateLocationUI();

                initAutoComplete();

                getDeviceLocation();

                map.setOnPoiClickListener(MapsFragment.this::onPoiClick);
                map.setOnMapClickListener(MapsFragment.this::onMapClick);
                map.setOnMarkerClickListener(MapsFragment.this::onMarkerClick);
            }

        }
    };

    //gets the location of the device
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                readUserSettings();
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //updates the user's current location
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            map.getUiSettings().setMapToolbarEnabled(false);
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setZoomControlsEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                map.getUiSettings().setCompassEnabled(false);
                map.getUiSettings().setZoomControlsEnabled(false);
                lastKnownLocation = null;
                //getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mapsViewModel =
                new ViewModelProvider(this).get(MapsViewModel.class);

        binding = FragmentMapsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        apiKey = getString(R.string.google_api_key);

        // Construct a PlacesClient
        Places.initialize(getActivity().getApplicationContext(), getString(R.string.google_api_key));
        placesClient = Places.createClient(getContext());

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        fragmentContainerView = (FragmentContainerView) getView().findViewById(R.id.fragmentContainerViewPlaceInfo);

        placeInfoPopUp = (ConstraintLayout) getView().findViewById(R.id.placeInfoPopUp);

        //place info pop up
        placeInfoTitle = (TextView) getView().findViewById(R.id.placeInfoTitle);
        directionButton = (Button) getView().findViewById(R.id.directionButton);
        favouriteButton = (Button) getView().findViewById(R.id.addToFavouritesButton);
        imageViewPlace = (ImageView) getView().findViewById(R.id.imageViewPlace);
        placeInfoDistance = (TextView) getView().findViewById(R.id.placeInfoDistance);
        placeInfoDuration = (TextView) getView().findViewById(R.id.placeInfoDuration);


        mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
            mMarkerPoints = new ArrayList<>();
        }
    }

    /*
        -----------------------------User Settings----------------------------------------------
     */
    private void readUserSettings() {
        mDatabase.child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                measurementSystem = snapshot.child("measurementSystem").getValue(String.class);
                landmarkType = snapshot.child("preferredLandmark").getValue(String.class);
                findNearbyLandmarks();
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }


    /*
        -----------------------------Google Places API autocomplete---------------------------------
     */
    private void initAutoComplete() {
        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.LAT_LNG, Place.Field.TYPES, Place.Field.WEBSITE_URI,
                Place.Field.PHOTO_METADATAS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() +
                        ", " + place.getLatLng() + ", " + place.getTypes() +
                        ", " + place.getWebsiteUri() + ", " + place.getPhotoMetadatas());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM));

                placeIds.put(place.getName(), place.getId());

                //adding marker for searched location
                Marker marker = map.addMarker(
                        new MarkerOptions()
                                .position(place.getLatLng())
                                .title(place.getName())
                );
                marker.showInfoWindow();
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /*
        ---------------------------------Marker on map clicked-------------------------------------
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        placeInfoPopUp.setVisibility(View.VISIBLE);
        placeInfoTitle.setVisibility(View.VISIBLE);
        directionButton.setVisibility(View.VISIBLE);

        String placeId = "";
        placeInfoDuration.setText("");
        placeInfoDistance.setText("");

        //default image
        imageViewPlace.setImageResource(R.drawable.ic_baseline_camera_alt_24);

        // using for-each loop for iteration over Map.entrySet() to get placedId
        for (Map.Entry<String,String> entry : placeIds.entrySet()) {
            if (entry.getKey().equals(marker.getTitle())) {
                placeId = entry.getValue();
            }
        }

        if (placeId.equals("")) {
            Toast.makeText(getContext(), "Place not found", Toast.LENGTH_SHORT).show();
        } else {
            getPlaceInfo(placeId);
        }

        placeInfoTitle.setText(marker.getTitle());

        directionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOrigin = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mDestination = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                drawRoute();

            }
        });

        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addToFavourites();
            }
        });

        return false;
    }

    //adds selected placed to favourites
    private void addToFavourites() {
        String website = "";
        if (currentPlaceWebUri == null) {
            website = "No Website Found";
        } else {
            website = currentPlaceWebUri.toString();
        }

        FavouriteLandmarks favouriteLandmarks = new FavouriteLandmarks(currentPlaceAddress,
                currentPlaceName, website, currentPlaceId);
        mDatabase.child(id).child("favouriteLandmarks").child(currentPlaceId).setValue(favouriteLandmarks);

    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
//        Toast.makeText(getContext(), "Clicked: " +
//                        poi.name + "\nPlace ID:" + poi.placeId +
//                        "\nLatitude:" + poi.latLng.latitude +
//                        " Longitude:" + poi.latLng.longitude,
//                Toast.LENGTH_LONG).show();
    }

    //retrieve place data
    private void getPlaceInfo(String placeId) {

        // Specify the fields to return.
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.ADDRESS, Place.Field.WEBSITE_URI, Place.Field.PHOTO_METADATAS);

        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            currentPlaceId = place.getId();
            currentPlaceName = place.getName();
            currentPlaceAddress = place.getAddress();
            currentPlaceWebUri = place.getWebsiteUri();
            currentPlaceImage = place.getPhotoMetadatas();
            Log.i(TAG, "Place found: " + place.getName());

            // Get the photo metadata.
            final List<PhotoMetadata> metadata = place.getPhotoMetadatas();
            if (metadata == null || metadata.isEmpty()) {
                Log.w(TAG, "No photo metadata.");
            } else {
                final PhotoMetadata photoMetadata = metadata.get(0);

                // Get the attribution text.
                final String attributions = photoMetadata.getAttributions();

                // Create a FetchPhotoRequest.
                final FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .setMaxWidth(500) // Optional.
                        .setMaxHeight(300) // Optional.
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    imageViewPlace.setImageBitmap(bitmap);
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        final ApiException apiException = (ApiException) exception;
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                        final int statusCode = apiException.getStatusCode();
                        // TODO: Handle error with given status code.
                    }
                });
            }

        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        placeInfoPopUp.setVisibility(View.GONE);
    }

    /*
        ---------------------------DIRECTIONS--------------------------------
     */

    private void drawRoute(){

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(mOrigin, mDestination);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
    }


    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Measurement units
        String str_units = "units="+measurementSystem.toLowerCase();

        // Key
        String key = "key=" + getString(R.string.google_maps_key);

        // Building the parameters to the web service
        //String parameters = str_origin+"&amp;"+str_dest+"&amp;"+key;
        String parameters = str_origin+"&"+str_dest+"&"+str_units+"&"+"departure_time=now"+"&"+key;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        Log.d(TAG, "getDirectionsUrl: " + url);

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Exception on download", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** A class to download data from Google Directions URL */
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("DownloadTask","DownloadTask : " + data);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for(int i=0;i<result.size() - 1;i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    Log.d(TAG, "onPostExecute: " + point.get("lat"));
                    Log.d(TAG, "onPostExecute: " + point.get("lng"));
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }

            for(int i=result.size() - 1;i<result.size();i++){
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++) {
                    HashMap<String, String> point = path.get(j);
                    Log.d(TAG, "onPostExecute: " + point.get("distance"));
                    Log.d(TAG, "onPostExecute: " + point.get("duration"));
                    mDistance = point.get("distance");
                    mDuration = point.get("duration");
                    placeInfoDistance.setText(mDistance);
                    placeInfoDuration.setText(mDuration);
                }
            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                if(mPolyline != null){
                    mPolyline.remove();
                }
                mPolyline = map.addPolyline(lineOptions);

            }else
                Toast.makeText(getContext(),"No route is found", Toast.LENGTH_LONG).show();
        }
    }

    /*
        -----------------------------Show Nearby places---------------------------------
     */

    private void findNearbyLandmarks() {
        String radius = "5000";
        String type = landmarkType;

        StringBuilder googlePlacesUrl =
                new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        googlePlacesUrl.append("location=").append(lastKnownLocation.getLatitude())
                .append(",").append(lastKnownLocation.getLongitude());
        googlePlacesUrl.append("&radius=").append(radius);
        googlePlacesUrl.append("&types=").append(type);
        googlePlacesUrl.append("&sensor=true");
        googlePlacesUrl.append("&key=" + apiKey);

        String url = googlePlacesUrl.toString();
        Log.d(TAG, "findNearbyLandmarks: " + url);

        new PlaceTask().execute(url);
    }

    private class PlaceTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... strings) {
            String data = null;
            try {
                //Initialize data
                data = downloadUrlTwo(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            //execute parser task
            new ParserTask().execute(s);
        }

        private class ParserTask extends AsyncTask<String,Integer, List<HashMap<String,String>>> {
            @Override
            protected List<HashMap<String, String>> doInBackground(String... strings) {
                //Create json parser class
                MyJsonParser jsonParser = new MyJsonParser();

                List<HashMap<String,String>> mapList = null;

                JSONObject object = null;
                try {
                    object = new JSONObject(strings[0]);

                    mapList = jsonParser.parseResult(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return mapList;
            }

            @Override
            protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
                map.clear();

                for (int i = 0; i < hashMaps.size(); i++) {
                    HashMap<String, String> hashMapList = hashMaps.get(i);

                    double lat = Double.parseDouble(hashMapList.get("lat"));

                    double lng = Double.parseDouble(hashMapList.get("lng"));

                    String name = hashMapList.get("name");

                    String id = hashMapList.get("place_id");

                    LatLng latLng = new LatLng(lat, lng);

                    MarkerOptions options = new MarkerOptions();

                    placeIds.put(name, id);

                    options.position(latLng);

                    options.title(name);

                    map.addMarker(options);
                }
            }
        }
    }
    private String downloadUrlTwo(String string) throws IOException {
        //Initialize URL
        URL url = new URL(string);
        //Initialize connection
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        //connect connection
        connection.connect();
        //Initialize input stream
        InputStream stream = connection.getInputStream();
        //Initialize buffer reader
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        StringBuilder builder = new StringBuilder();

        String line = "";

        while ((line = reader.readLine()) != null) {
            //Append line
            builder.append(line);
        }
        //Get appended data
        String data = builder.toString();
        //Close reader
        reader.close();
        //return data
        return data;
    }
}