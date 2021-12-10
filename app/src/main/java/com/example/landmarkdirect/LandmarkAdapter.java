package com.example.landmarkdirect;

import android.content.Context;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.landmarkdirect.ui.favourite.FavouriteFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.List;

//Template for each favourite landmark cell
public class LandmarkAdapter extends ArrayAdapter<FavouriteLandmarks> {

    public LandmarkAdapter(Context context, int resource, List<FavouriteLandmarks> favouriteLandmarksList) {
        super(context, resource, favouriteLandmarksList);
    }

    //Firebase variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = database.getReference("Users");
    private String id;

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FavouriteLandmarks favouriteLandmarks = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.favourite_landmark_cell, parent, false);
        }
        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TextView tvName = (TextView) convertView.findViewById(R.id.favName);
        TextView tvAddress = (TextView) convertView.findViewById(R.id.favAddress);
        TextView tvWebsite = (TextView) convertView.findViewById(R.id.favWebsite);
        ImageView img = (ImageView) convertView.findViewById(R.id.favImage);

        //remove item from favourite's list
        Button removeButton = (Button) convertView.findViewById(R.id.removeFavButton);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.child(id).child("favouriteLandmarks").child(favouriteLandmarks.placeId).removeValue();

            }
        });

        tvName.setText(favouriteLandmarks.placeName);
        tvAddress.setText(favouriteLandmarks.placeAddress);
        tvWebsite.setText(favouriteLandmarks.placeWebUri);

        return convertView;
    }
}
