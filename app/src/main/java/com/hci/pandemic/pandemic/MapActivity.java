package com.hci.pandemic.pandemic;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.ProgressBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends Activity {

    GoogleMap googleMap;
    Marker selfMarker, enemyMarker, bonusMarker;
    int progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        createMapView();
        createMarkers();
        initThreads();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.map_screen_menu) {
            Util.launchMapActivity(this);
            return true;
        } else if (id == R.id.upgrade_screen_menu) {
            Util.launchUpgradeScreenActivity(this);
            return true;
        } else if (id == R.id.leaderboard_screen_menu) {
            Util.launchLeaderboardActivity(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createMapView(){
        /**
         * Catch the null pointer exception that
         * may be thrown when initialising the map
         */
        try {
            if(null == googleMap){
                googleMap = ((MapFragment) getFragmentManager().findFragmentById(
                        R.id.mapView)).getMap();

                /**
                 * If the map is still null after attempted initialisation,
                 * show an error to the user
                 */
                if(null == googleMap) {
                    Log.d("mapApp", "Error creating map");
                }
                else
                {
                    Log.d("mapApp", "Google maps success");
                    googleMap.getUiSettings().setZoomControlsEnabled(false);
                }
            }
        } catch (NullPointerException exception){
            Log.d("mapApp", exception.toString());
        }
    }

    private void createMarkers(){

        /** Make sure that the map has been initialised **/
        if(null != googleMap){

            // Enable MyLocation Layer of Google Map
            googleMap.setMyLocationEnabled(true);

            // set map type
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            // Show the current location in Google Map
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(getCurrentLocation()));

            // Zoom in the Google Map
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(19));

            double latitude = getCurrentLocation().latitude;
            double longitude = getCurrentLocation().longitude;

            //Custom markers
            selfMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title("You")
                    .snippet("Disease\nStats:").icon(BitmapDescriptorFactory.fromResource(R.drawable.self)));

            enemyMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude + .0001, longitude - .0003))
                    .title("Enemy").icon(BitmapDescriptorFactory.fromResource(R.drawable.enemy)));
            bonusMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude - .0002, longitude - .00025))
                    .title("Bonus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bonus)));

            //Radius
            final Circle circle = googleMap.addCircle(new CircleOptions().center(new LatLng(latitude, longitude))
                    .strokeColor(Color.BLACK).radius(15));
            circle.setFillColor(Color.argb(150, 224, 255, 255));
            circle.setStrokeWidth(2);

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
            {

                @Override
                public boolean onMarkerClick(Marker marker) {
                    if (marker.equals(selfMarker))
                    {
                        Log.d("mapApp", "self clicked");
                    }
                    else if (marker.equals(enemyMarker))
                    {
                        Log.d("mapApp", "enemy clicked");
                    }
                    else if (marker.equals(bonusMarker))
                    {
                        Log.d("mapApp", "bonus clicked");
                    }
                    return true;
                }

            });
        }
    }

    private LatLng getCurrentLocation() {
        // Get LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Create a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Get the name of the best provider
        String provider = locationManager.getBestProvider(criteria, false);

        // Get Current Location
        Location myLocation = locationManager.getLastKnownLocation(provider);

        //If getLastKnownLocation, have fallback
        if (myLocation == null) {
            myLocation = new Location(provider);
            myLocation.setLatitude(38.99);
            myLocation.setLongitude(-76.9362);
            Log.d("mapApp", "Location Manager failed!");
        }

        // Get latitude of the current location
        double latitude = myLocation.getLatitude();

        // Get longitude of the current location
        double longitude = myLocation.getLongitude();

        // Create a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);
        Log.d("mapApp", latitude + " " + longitude);
        return latLng;
    }

    private boolean insideRadius(Double lat, Double lon, Double radius) {
        Location locationA = new Location("self");

        locationA.setLatitude(selfMarker.getPosition().latitude);
        locationA.setLongitude(selfMarker.getPosition().longitude);

        Location locationB = new Location("target");

        locationB.setLatitude(lat);
        locationB.setLongitude(lon);

        return locationA.distanceTo(locationB) <= radius;
    }

    public void initThreads() {

        new Thread(new Runnable() {
            public void run() {
                Log.d("mapApp", "Thread running");
                LatLng temp = getCurrentLocation();
                //selfMarker.setPosition(temp);
            }
        }).start();

        //Test progress bar, remove later
        progress = 0;
        new Thread(new Runnable() {
            public void run() {
                while(true)
                {
                    try {
                        Thread.sleep(33);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    changeProgress(progress++);
                    if (progress > 100)
                        progress = 0;
                }
            }
        }).start();
    }

    public void changeToUpgrade() {
        //TO DO
    }

    public void changeToLeaderboard() {
        // TO DO
    }

    public void changeProgress(int progress) {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progressBar);
        progressbar.setProgress(progress);
    }


}
