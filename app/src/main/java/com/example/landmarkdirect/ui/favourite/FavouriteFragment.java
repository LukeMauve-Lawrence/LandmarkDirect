package com.example.landmarkdirect.ui.favourite;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.landmarkdirect.FavouriteLandmarks;
import com.example.landmarkdirect.LandmarkAdapter;
import com.example.landmarkdirect.R;
import com.example.landmarkdirect.databinding.FragmentFavouriteBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class FavouriteFragment extends Fragment {

    private FavouriteViewModel favouriteViewModel;
    private FragmentFavouriteBinding binding;

    private ListView listView;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    private FavouriteLandmarks favouriteLandmarks;
    public static ArrayList<FavouriteLandmarks> favouriteLandmarksArrayList = new ArrayList<FavouriteLandmarks>();

    //Firebase variables
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference mDatabase = database.getReference("Users");
    private String id;

    private LinearLayout.LayoutParams params;
    private LinearLayout lm;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // create the layout params that will be used to define how your
        // button will be displayed
        params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        getFavouritesList();
    }

    private void setUpList() {

        if(getActivity()!=null) {
            listView = (ListView) getView().findViewById(R.id.favouriteLandmarksListView);
            LandmarkAdapter adapter = new LandmarkAdapter(getActivity(), 0,
                    favouriteLandmarksArrayList);
            listView.setAdapter(adapter);
            Log.d(TAG, "setUpList: settings up list");
        }


    }

    private void setUpClickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                FavouriteLandmarks selectLandmark =
                        (FavouriteLandmarks) (listView.getItemAtPosition(position));
                Toast.makeText(getContext(), "awe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFavouritesList() {

        DatabaseReference refFavourites = mDatabase.child(id).child("favouriteLandmarks");
        refFavourites.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                favouriteLandmarksArrayList.clear();
                //gets all children at this level
                Iterable<DataSnapshot> children = snapshot.getChildren();

                for (DataSnapshot ds : children) {
                    FavouriteLandmarks favouriteLandmarks = ds.getValue(FavouriteLandmarks.class);
                    favouriteLandmarksArrayList.add(favouriteLandmarks);
                }
                setUpList();
                setUpClickListener();

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        favouriteViewModel =
                new ViewModelProvider(this).get(FavouriteViewModel.class);

        binding = FragmentFavouriteBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}