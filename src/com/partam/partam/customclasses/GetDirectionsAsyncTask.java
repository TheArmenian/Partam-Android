package com.partam.partam.customclasses;

import java.util.ArrayList;
import java.util.Map;

import org.w3c.dom.Document;

import android.os.AsyncTask;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.partam.partam.NavigationActivity;

public class GetDirectionsAsyncTask extends AsyncTask<Void, Object, ArrayList<LatLng>>
{
    public static final String USER_CURRENT_LAT = "user_current_lat";
    public static final String USER_CURRENT_LONG = "user_current_long";
    public static final String DESTINATION_LAT = "destination_lat";
    public static final String DESTINATION_LONG = "destination_long";
    public static final String DIRECTIONS_MODE = "directions_mode";
    private NavigationActivity activity;
    private Exception exception;
    
    private Map<String, String> paramMap;
    View viewLoader;
 
    public GetDirectionsAsyncTask(NavigationActivity activity, Map<String, String> map, View viewLoader)
    {
        super();
        this.activity = activity;
        this.paramMap = map;
        this.viewLoader = viewLoader;
    }
 
    public void onPreExecute()
    {
    	viewLoader.setVisibility(View.VISIBLE);
    }
 
	@Override
	protected ArrayList<LatLng> doInBackground(Void... params) 
	{
        try
        {
            LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)) , Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
            LatLng toPosition = new LatLng(Double.valueOf(paramMap.get(DESTINATION_LAT)) , Double.valueOf(paramMap.get(DESTINATION_LONG)));
            GMapV2Direction md = new GMapV2Direction();
            Document doc = md.getDocument(fromPosition, toPosition, paramMap.get(DIRECTIONS_MODE));
            ArrayList<LatLng> directionPoints = md.getDirection(doc);
            return directionPoints;
        }
        catch (Exception e)
        {
            exception = e;
            return null;
        }
	}
	
    @Override
    public void onPostExecute(ArrayList<LatLng> result)
    {
    	viewLoader.setVisibility(View.GONE);
        if (exception == null)
        {
            activity.handleGetDirectionsResult(result);
        }
        else
        {
            processException();
        }
    }
 
    private void processException()
    {
//        Toast.makeText(activity, "Error retriving data", Toast.LENGTH_SHORT).show();
    }
}