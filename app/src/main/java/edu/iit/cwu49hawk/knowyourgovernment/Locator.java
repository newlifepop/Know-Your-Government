package edu.iit.cwu49hawk.knowyourgovernment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by wsy37 on 4/17/2017.
 */

public class Locator
{
    private MainActivity owner;
    private LocationManager locationManager;
    private LocationListener locationListener;

    public Locator(MainActivity mainActivity)
    {
        owner = mainActivity;

        if(checkPermission())
        {
            setUpLocationManager();
            determineLocation();
        }
    }

    public void setUpLocationManager()
    {
        if(locationManager != null)
            return;

        if(!checkPermission())
            return;

        locationManager = (LocationManager)owner.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider
                Toast.makeText(owner, "Update from " + location.getProvider(), Toast.LENGTH_SHORT).show();
                ArrayList<String> address = getAddress(location.getLatitude(), location.getLongitude());
                HashMap<String, String> stateAddress = parseAddress(address.get(0));

                owner.setLocation(stateAddress.get("city"), stateAddress.get("state"),
                        stateAddress.get("zip code"));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // Register the listener with the Location Manager to receive GPS location updates
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
    }

    public void determineLocation()
    {
        if(!checkPermission())
            return;

        if(locationManager == null)
            setUpLocationManager();

        if(locationManager != null)
        {
            Location loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(loc != null)
            {
                HashMap<String, String> stateAddress =
                        parseAddress(getAddress(loc.getLatitude(), loc.getLongitude()).get(0));
                owner.setLocation(stateAddress.get("city"),
                        stateAddress.get("state"), stateAddress.get("zip code"));

                return;
            }
        }

        if(locationManager != null)
        {
            Location loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            if(loc != null)
            {
                HashMap<String, String> stateAddress =
                        parseAddress(getAddress(loc.getLatitude(), loc.getLongitude()).get(0));
                owner.setLocation(stateAddress.get("city"),
                        stateAddress.get("state"), stateAddress.get("zip code"));

                return;
            }
        }

        if(locationManager != null)
        {
            Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(loc != null)
            {
                HashMap<String, String> stateAddress =
                        parseAddress(getAddress(loc.getLatitude(), loc.getLongitude()).get(0));
                owner.setLocation(stateAddress.get("city"),
                        stateAddress.get("state"), stateAddress.get("zip code"));

                return;
            }
        }

        // If reach here, no location
        Toast.makeText(owner, "No location providers were available", Toast.LENGTH_LONG).show();
        return;
    }

    public void shutDown() {
        locationManager.removeUpdates(locationListener);
        locationManager = null;
    }

    public static HashMap<String, String> parseAddress(String stateAddress)
    {
        HashMap<String, String> address = new HashMap<>();
        StringTokenizer strTok = new StringTokenizer(stateAddress, ",");
        String city = strTok.nextToken();
        strTok.nextToken(" ");
        String state = strTok.nextToken();
        String zipCode = strTok.nextToken();

        address.put("city", city);
        address.put("state", state);
        address.put("zip code", zipCode);

        return address;
    }

    private ArrayList<String> getAddress(double latitude, double longitude)
    {
        List<Address> addresses;
        for(int times = 0; times < 3; ++times)
        {
            Geocoder geocoder = new Geocoder(owner, Locale.getDefault());
            try
            {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                ArrayList<String> address = new ArrayList<>();
                for(Address ad: addresses)
                    address.add(ad.getAddressLine(1));

                return address;
            }
            catch(IOException e){
                e.printStackTrace();
            }
            Toast.makeText(owner, "GeoCoder service is slow - please wait", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(owner, "GeoCoder service timed out - please try again", Toast.LENGTH_SHORT).show();

        return null;
    }

    // This method asks the user for Location permissions (if not already given)
    private boolean checkPermission()
    {
        if(ContextCompat.checkSelfPermission(owner, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(owner,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 5);
            return false;
        }

        return true;
    }
}