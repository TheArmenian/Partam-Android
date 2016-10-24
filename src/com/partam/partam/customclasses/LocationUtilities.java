package com.partam.partam.customclasses;

import java.util.List;

import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

/**
 * Created by codelance on 4/18/14.
 */
public class LocationUtilities
{
    public static LatLng currentLocation;

    public static LatLng getCurrentLocation(Context ctx)
    {
        LocationManager mLocationManager = (LocationManager) ctx.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (myLocation != null)
        {
            currentLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            return currentLocation;
        }

        Location bestLocation = null;
        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers)
        {
            Location l = mLocationManager.getLastKnownLocation(provider);

            if (l == null)
            {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy())
            {
                bestLocation = l;
            }
        }

        if (bestLocation == null)
        {
            currentLocation = new LatLng(0, 0);
		}
        else
        {
            currentLocation = new LatLng(bestLocation.getLatitude(), bestLocation.getLongitude());
        }
//        currentLocation = new LatLng(37.7749300, -122.4194200);
        return currentLocation;
    }
}
