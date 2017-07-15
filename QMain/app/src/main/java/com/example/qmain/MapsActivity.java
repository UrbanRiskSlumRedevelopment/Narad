package com.example.qmain;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.location.places.ui.PlacePicker;


/**
 * Map activity prompting user to select their location
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    /**
     * Opens place picker map
     *
     * @param savedInstanceState saved instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        }catch(Exception e){
            System.out.println("didn't work");
        }

    }

    int PLACE_PICKER_REQUEST = 1;  // should match place picker request int in PVQ

    @Override
    public void onMapReady(GoogleMap googleMap) {}

}