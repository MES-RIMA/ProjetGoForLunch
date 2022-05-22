package com.example.goforlunch.controller.fragment;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.goforlunch.R;
import com.example.goforlunch.controller.RestaurantActivity;
import com.example.goforlunch.modele.firebase.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
private double latitude;
private double longitude;
    private GoogleMap mMap;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 789;

    public static final String INTENT_EXTRAS_RESULT_MAPS = "INTENT_EXTRAS_RESULT_MAPS";
    public static final String INTENT_EXTRAS_PLACEDETAILSRESPONSE_MAPS = "INTENT_EXTRAS_PLACEDETAILSRESPONSE_MAPS";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MapsFragment() {
    }

    public static MapsFragment newInstance() {
        return new MapsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_maps, container, false);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
        return v;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        if (currentLat != null && currentLng != null) {
            updateWithPosition();
        } else {
            recoversData();
        }
    }

    @Override
    protected void updateWithPosition() {
        if (mMap != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                latitude = currentLat;
                longitude = currentLng;
                LatLng currentLocation = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
            } else {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            }
        }
    }

    private void setMarker() {
        for (int position = 0; mParcelableRestaurantDetails.getNearbyResults().size() > position; position++) {
            LatLng restaurant = new LatLng(mParcelableRestaurantDetails.getNearbyResults()
                    .get(position).getGeometry().getLocation().getLat(),
                    mParcelableRestaurantDetails.getNearbyResults().get(position).getGeometry().getLocation().getLng());
            mMap.addMarker(new MarkerOptions().position(restaurant)
                    .icon(BitmapDescriptorFactory.defaultMarker(25)))
                    .setTag(position);
            for (User user : usersList) {
                if (mParcelableRestaurantDetails.getNearbyResults().get(position).getPlaceId().equals(user.getUserChoicePlaceId())) {
                    mMap.addMarker(new MarkerOptions().position(restaurant)
                            .icon(BitmapDescriptorFactory.defaultMarker(92)))
                            .setTag(position);
                }
            }
        }

    }

    /**
     * Call when a user click on a marker. Start a new activity to see restaurant details.
     */
    @Override
    public boolean onMarkerClick(Marker marker) {
        int position = (int) marker.getTag();
        Intent intent = new Intent(getActivity(), RestaurantActivity.class);
        intent.putExtra(INTENT_EXTRAS_RESULT_MAPS, mParcelableRestaurantDetails.getNearbyResults().get(position));
        intent.putExtra(INTENT_EXTRAS_PLACEDETAILSRESPONSE_MAPS, mParcelableRestaurantDetails.getPlaceDetailsResponses().get(position));
        startActivity(intent);
        return false;
    }

    /**
     * Clean old markers.
     * Notify fragment that the data has changed.
     */
    @Override
    protected void notifyFragment() {
        if (mMap != null) {
            mMap.clear();
            setMarker();
        }
    }

}
