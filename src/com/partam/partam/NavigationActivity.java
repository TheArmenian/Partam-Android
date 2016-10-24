package com.partam.partam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.partam.partam.customclasses.GMapV2Direction;
import com.partam.partam.customclasses.GetDirectionsAsyncTask;
import com.partam.partam.customclasses.LocationUtilities;
import com.partam.partam.customclasses.WebImage;
import com.partam.partam.image_loader.Utils;

@SuppressLint("NewApi")
public class NavigationActivity extends FragmentActivity
{
	class MyInfoWindowAdapter implements InfoWindowAdapter
	{
		private final View myContentsView;

		MyInfoWindowAdapter()
		{
			myContentsView = getLayoutInflater().inflate(R.layout.custom_info_contents, null);
		}

		@Override
		public View getInfoContents(Marker marker)
		{
			return null;
		}

		@SuppressLint("NewApi")
		@Override
		public View getInfoWindow(Marker marker) 
		{
			TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.txt));
			tvTitle.setText(marker.getTitle());
			TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.txtMemorial));
			tvSnippet.setText(marker.getSnippet());
			String url = AppManager.getJsonString(webInfo, "picture");
			url = AppManager.getInstanse().getUrlForCrop(url);
			WebImage webImage = new WebImage((ImageView) myContentsView.findViewById(R.id.img), url);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
				webImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} 
			else
			{
				webImage.execute();
			}
			
			if (AppManager.getJsonBoolean(webInfo, "promoted"))
			{
				myContentsView.findViewById(R.id.imgFavourite).setVisibility(View.VISIBLE);
			}
			return myContentsView;
		}
	}

	private LatLng targetLocation;
	private LatLng myLocation;

	private GoogleMap map;
	private SupportMapFragment fragment;
	private Polyline newPolyline;

	private JSONObject webInfo;
	private String category;
	private int categoryID;

	View viewLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navigation);
		initActionBar();

		viewLoader = findViewById(R.id.viewLoader);

		try {
			webInfo = new JSONObject(getIntent().getStringExtra("webInfo"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		category = getIntent().getStringExtra("category");
		categoryID = getIntent().getIntExtra("categoryID", -1);

		fragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapNavigation));
		map = fragment.getMap();
		map.setMyLocationEnabled(true);
		map.setInfoWindowAdapter(new MyInfoWindowAdapter());
		targetLocation = new LatLng(AppManager.getJsonDouble(webInfo, "lat"), AppManager.getJsonDouble(webInfo, "lng"));
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new AddMarker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new AddMarker().execute();
		}
		
		myLocation = LocationUtilities.getCurrentLocation(this);
		findDirections(myLocation.latitude, myLocation.longitude, targetLocation.latitude, targetLocation.longitude, GMapV2Direction.MODE_DRIVING);
	}
	
	class AddMarker extends AsyncTask<Void, MarkerOptions, Void>
	{
		@Override
		protected Void doInBackground(Void... params) 
		{
			JSONObject info = new JSONObject();
			for (int i = 0; i < AppManager.getInstanse().allPoints.length(); i++)
			{
				JSONObject obj = AppManager.getJsonObject(AppManager.getInstanse().allPoints, i);
				int id = AppManager.getJsonInt(AppManager.getJsonObject(obj, "category"), "id");
				if (categoryID == id)
				{
					info = AppManager.getJsonObject(obj, "category");
					break;
				}
			}
			
			String url = AppManager.getJsonString(info, "icon_url");
			if (url.endsWith(".png"))
			{
				Bitmap bmp = getBitmap(url);
				if (bmp != null) 
				{
					LatLng targetLocation = new LatLng(AppManager.getJsonDouble(webInfo, "lat"), AppManager.getJsonDouble(webInfo, "lng"));
					MarkerOptions options = new MarkerOptions().position(targetLocation).icon(BitmapDescriptorFactory.fromBitmap(bmp));
					publishProgress(options);
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(MarkerOptions... values) 
		{
			if (map == null) {
				cancel(false);
			}
			MarkerOptions options = values[0];
			Marker newMarker = map.addMarker(options);		
			newMarker.setTitle(AppManager.getJsonString(webInfo, "name"));
			newMarker.setSnippet(category);
			super.onProgressUpdate(values);
		}

		private File getFile(String url) 
		{
			String filename = AppManager.createFileNameFromUrl(url);
			File f = new File(new File(AppManager.getInstanse().getFilesPath()), filename);
			return f;
		}

		private Bitmap getBitmap(String url)
		{
			File f = getFile(url);

			Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
			if (bitmap != null)
			{
				return bitmap;
			}

			try
			{
				URL imageUrl = new URL(url.replace(" ", "%20"));
				HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
				InputStream is = conn.getInputStream();
				OutputStream os = new FileOutputStream(f);
				Utils.CopyStream(is, os);
				os.close();
				conn.disconnect();
				bitmap = BitmapFactory.decodeFile(f.getPath());
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}
	}

	private void initActionBar()
	{
		ImageView imgLeft = (ImageView) findViewById(R.id.imgLeft);
		imgLeft.setImageResource(R.drawable.btn_back);
		imgLeft.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});

		ImageView imgRight = (ImageView) findViewById(R.id.imgRight);
		imgRight.setVisibility(View.GONE);
	}

	@Override
	protected void onResume() {

		super.onResume();
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 13));
	}

	public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) 
	{
		PolylineOptions rectLine = new PolylineOptions().width(5).color(Color.RED);

		for(int i = 0 ; i < directionPoints.size() ; i++) 
		{          
			rectLine.add(directionPoints.get(i));
		}
		if (newPolyline != null)
		{
			newPolyline.remove();
		}
		newPolyline = map.addPolyline(rectLine);
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 13));

	}

	@SuppressLint("NewApi")
	public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
	{
		Map<String, String> map = new HashMap<String, String>();
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
		map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
		map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
		map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
		map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

		GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this, map, viewLoader);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			asyncTask.execute();
		}
	}
}
