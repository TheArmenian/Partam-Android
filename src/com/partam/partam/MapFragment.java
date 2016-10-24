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

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.partam.partam.customclasses.AddresFromLocation;
import com.partam.partam.customclasses.ExifUtils;
import com.partam.partam.customclasses.HorizontalListView;
import com.partam.partam.customclasses.LocationUtilities;
import com.partam.partam.customclasses.PartamHttpClient;
import com.partam.partam.image_loader.ImageLoader;
import com.partam.partam.image_loader.ImageLoader.ImageLoaderListener;
import com.partam.partam.image_loader.Utils;

@SuppressLint("NewApi")
public class MapFragment extends Fragment implements OnInfoWindowClickListener
{
	JSONArray arrInfo;
	ImageLoader loader;

	AddMarkers task;
	boolean isAddPlace = false;

	View viewAddPlace;
	View layStep1;
	View layStep2;
	View layStep3;
	Button btnNextStep;
	int step = 0;
	AddresFromLocation addressPlace = new AddresFromLocation();
	
	EditText txtName;
	EditText txtCity;
	View btnMoreCity;
	EditText txtAddress;
	EditText txtWebUrl;
	EditText txtDescription;
	
	ImageView imgAddPhoto;
	HorizontalListView listHorizontal;
	HorizontalListAdapter adapterHorizontalList;
	
	EditText txtTags;
	EditText txtCategory;
	View btnMoreCategory;
	View layType;
	View viewDivider;
	View layPaypal;
	View viewDivider1;
	TextView txtPaymentInfo;
	EditText txtPaypal;
	
	ListView listView;
	
	LayoutInflater inflater;
	
	
	private static final int PICK_FROM_CAMERA = 1;
//	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	private Uri mImageCaptureUri;
	private boolean fromCamera = false;
//	private boolean isDoCrop = false;
	private Bitmap bmpImage = null;
	ArrayList<Bitmap> arrPhotos = new ArrayList<>();
	ArrayList<Bitmap> arrRealPhotos = new ArrayList<>();
	int imgWidth;

	class MyInfoWindowAdapter implements InfoWindowAdapter
	{
		private final View myContentsView;

		MyInfoWindowAdapter()
		{
			myContentsView = getActivity().getLayoutInflater().inflate(R.layout.custom_info_contents, null);
		}

		@Override
		public View getInfoContents(Marker marker)
		{
			return null;
		}

		@Override
		public View getInfoWindow(Marker marker) 
		{
			TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.txt));
			TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.txtMemorial));
			View viewConfirmPlace =  myContentsView.findViewById(R.id.viewConfirmPlace);
			int index = Integer.valueOf(marker.getSnippet());

			if (index == -1)
			{
				viewConfirmPlace.setVisibility(View.VISIBLE);
				return myContentsView;
			}

			viewConfirmPlace.setVisibility(View.GONE);
			JSONObject obj = AppManager.getJsonObject(arrInfo, index);
			tvTitle.setText(AppManager.getJsonString(obj, "name"));
			tvSnippet.setText(AppManager.getJsonString(AppManager.getJsonObject(obj, "category"), "title"));

			String url = AppManager.getJsonString(obj, "picture");
			loader.DisplayImage(url, (ImageView) myContentsView.findViewById(R.id.img));
			if (AppManager.getJsonBoolean(obj, "promoted"))
			{
				myContentsView.findViewById(R.id.imgFavourite).setVisibility(View.VISIBLE);
			}
			else
			{
				myContentsView.findViewById(R.id.imgFavourite).setVisibility(View.GONE);
			}
			return myContentsView;
		}
	}

	private LatLng myLocation;

	private GoogleMap map;
	private SupportMapFragment fragment;
	
	View viewLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		this.inflater = inflater;
		View v = inflater.inflate(R.layout.fragment_map, null);
		initActionBar(v);

		((MainActivity) getActivity()).setEnableMenu(false);

		viewLoader = v.findViewById(R.id.viewLoader);
		viewLoader.setVisibility(View.GONE);
		
		viewAddPlace = v.findViewById(R.id.viewAddPlace);
		layStep1 = v.findViewById(R.id.layStep1);
		layStep2 = v.findViewById(R.id.layStep2);
		layStep3 = v.findViewById(R.id.layStep3);

		viewAddPlace.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				viewAddPlace.setVisibility(View.GONE);
				map.setMyLocationEnabled(true);
				isAddPlace = false;
				lastMarker.remove();
				lastMarker = null;
				infoWindowIsShow = false;
			}
		});
		
		btnNextStep = (Button) v.findViewById(R.id.btnNextStep);
		btnNextStep.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				switch (step)
				{
					case 0:
						if (txtName.getText().toString().length() == 0)
						{
							AppManager.getInstanse().showAlert(getActivity(), "Partam", "Please enter name!");
							return;
						}
						layStep1.setVisibility(View.INVISIBLE);
						layStep2.setVisibility(View.VISIBLE);
						break;
					case 1:
						layStep2.setVisibility(View.INVISIBLE);
						layStep3.setVisibility(View.VISIBLE);
						btnNextStep.setText("Add Place");
						break;
					case 2:

						if (txtCategory.getText().toString().length() == 0)
						{
							AppManager.getInstanse().showAlert(getActivity(), "Partam", "Please select the category!");
							return;
						}
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
						{
							new AddPointRequst().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						} 
						else
						{
							new AddPointRequst().execute();
						}
						return;
					default:
						break;
				}
				step++;
			}
		});

		txtName = (EditText) v.findViewById(R.id.txtName);
		txtCity = (EditText) v.findViewById(R.id.txtCity);
		btnMoreCity = v.findViewById(R.id.btnMoreCity);
		txtAddress = (EditText) v.findViewById(R.id.txtAddress);
		txtWebUrl = (EditText) v.findViewById(R.id.txtWeb);
		txtDescription = (EditText) v.findViewById(R.id.txtDescription);
		
		imgAddPhoto = (ImageView) v.findViewById(R.id.imgAddPhoto);
		listHorizontal = (HorizontalListView) v.findViewById(R.id.listHorizontal);
		listHorizontal.setAdapter(adapterHorizontalList = new HorizontalListAdapter());
		
		txtTags = (EditText) v.findViewById(R.id.txtTags);
		txtCategory = (EditText) v.findViewById(R.id.txtCategory);
		btnMoreCategory = v.findViewById(R.id.btnMoreCategory);
		layType = v.findViewById(R.id.layType);
		layPaypal = v.findViewById(R.id.layPaypal);
		viewDivider = v.findViewById(R.id.viewDivider);
		viewDivider1 = v.findViewById(R.id.viewDivider1);
		txtPaymentInfo = (TextView) v.findViewById(R.id.txtPaymentInfo);
		txtPaypal = (EditText) v.findViewById(R.id.txtPaypal);
		
		listView = (ListView) v.findViewById(R.id.listView);
		listView.setAdapter(new CategoryAdapter());
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				txtCategory.setText(AppManager.getJsonString(AppManager.getInstanse().categories.get(position), "title"));
				listView.setVisibility(View.GONE);
				showPayPalInfo(AppManager.getJsonBoolean(AppManager.getInstanse().categories.get(position), "is_business"));
			}
		});
		
		btnMoreCategory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				listView.setVisibility(View.VISIBLE);
			}
		});
		
		txtCategory.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				listView.setVisibility(View.VISIBLE);
			}
		});
		
		imgAddPhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				final String [] items        = new String [] {"Take from camera", "Select from Gallery"};
				ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.select_dialog_item,items);
				AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity());

				builder.setTitle("Select Image");
				builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
					public void onClick( DialogInterface dialog, int item ) 
					{
						//pick from camera
						if (item == 0) {
							Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

							mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_avatar" + ".jpg"));

							intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

							try {
								intent.putExtra("return-data", true);

								startActivityForResult(intent, PICK_FROM_CAMERA);
							} catch (ActivityNotFoundException e) {
								e.printStackTrace();
							}
						} 
						else 
						{ 
							//pick from file
							Intent intent = new Intent();

							intent.setType("image/*");
							intent.setAction(Intent.ACTION_GET_CONTENT);

							startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
						}
					}
				} );

				final AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
		
		fragment = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapMain));
		map = fragment.getMap();
		map.setMyLocationEnabled(true);
		map.setInfoWindowAdapter(new MyInfoWindowAdapter());
		map.setOnInfoWindowClickListener(this);
		map.setOnMarkerClickListener(new MarkerClickListener());
		map.setOnMapClickListener(new OnMapClickListener()
		{
			@Override
			public void onMapClick(LatLng arg0) 
			{
				if (isAddPlace)
				{
					viewAddPlace.setVisibility(View.GONE);
					map.setMyLocationEnabled(true);
					isAddPlace = false;
					lastMarker.remove();
				}
				lastMarker = null;
				infoWindowIsShow = false;
			}
		});
		
		map.setOnMapLongClickListener(new OnMapLongClickListener()
		{
			@Override
			public void onMapLongClick(LatLng location)
			{
				if (AppManager.getInstanse().isLogOut)
				{
					openLoginPage();
					return;
				}
				addMarkerForAddPlace(location);
			}
		});

		myLocation = LocationUtilities.getCurrentLocation(getActivity());
		if (myLocation.latitude != 0 || myLocation.longitude != 0)
		{
			map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
		}

		task = new AddMarkers();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			task.execute();
		}

		loader.setOnImageLoaderListener(imageLoaderListener);

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		imgWidth = (int) (metrics.density * 100);
		
		return v;
	}

	private void openLoginPage()
	{
		LoginFragment frag = new LoginFragment();
		((MainActivity) getActivity()).addFragment(frag, false);
	}
	
	private void initActionBar(View v)
	{
		ImageView imgLeft = (ImageView) v.findViewById(R.id.imgLeft);
		imgLeft.setImageResource(R.drawable.btn_done);
		imgLeft.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				getActivity().onBackPressed();
			}
		});

		ImageView imgRight = (ImageView) v.findViewById(R.id.imgRight);
		imgRight.setImageResource(R.drawable.btn_add_place);
		imgRight.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				if (AppManager.getInstanse().isLogOut)
				{
					openLoginPage();
					return;
				}
				addMarkerForAddPlace(myLocation);
			}
		});
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		loader.setOnImageLoaderListener(null);
		task.cancel(false);
		((MainActivity) getActivity()).setEnableMenu(true);
		loader.clearMemoryCache();
		SupportMapFragment f = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapMain);
		if (f != null && !getActivity().isFinishing())
			getFragmentManager().beginTransaction().remove(f).commit();
	}

	class AddMarkers extends AsyncTask<Void, MarkerOptions, Void>
	{
		@Override
		protected Void doInBackground(Void... params) 
		{
			if (arrInfo == null) {
				return null;
			}
			for (int i = 0; i < arrInfo.length(); i++)
			{
				if (isCancelled()) {
					return null;
				}
				JSONObject currentInfo = AppManager.getJsonObject(arrInfo, i);
				if (AppManager.getJsonDouble(currentInfo, "lat") != -1 || AppManager.getJsonDouble(currentInfo, "lng") != -1)
				{
					String url = AppManager.getJsonString(AppManager.getJsonObject(currentInfo, "category"), "icon_url");
					if (AppManager.getInstanse().isFilter) 
					{
						JSONObject info = new JSONObject();
						int currentID = AppManager.getJsonInt(AppManager.getJsonObject(currentInfo, "category"), "id");
						for (int j = 0; j < AppManager.getInstanse().allPoints.length(); j++)
						{
							JSONObject obj = AppManager.getJsonObject(AppManager.getInstanse().allPoints, j);
							int id = AppManager.getJsonInt(AppManager.getJsonObject(obj, "category"), "id");
							if (currentID == id)
							{
								info = AppManager.getJsonObject(obj, "category");
								break;
							}
						}

						url = AppManager.getJsonString(info, "icon_url");
					}
					//					if (AppManager.getInstanse().isFilter)
					//					{
					//						LatLng targetLocation = new LatLng(AppManager.getJsonDouble(currentInfo, "lat"), AppManager.getJsonDouble(currentInfo, "lng"));
					//						String category = AppManager.getJsonString(AppManager.getJsonObject(currentInfo, "category"), "title");
					//						MarkerOptions options = new MarkerOptions().position(targetLocation).icon(BitmapDescriptorFactory.fromResource(AppManager.getInstanse().getMapCategoryImage(category)));
					//						options.snippet(i + "");
					//						publishProgress(options);
					//					}
					//					else
					//					{
					if (url.endsWith(".png"))
					{
						Bitmap bmp = getBitmap(url);
						if (bmp != null) 
						{
							LatLng targetLocation = new LatLng(AppManager.getJsonDouble(currentInfo, "lat"), AppManager.getJsonDouble(currentInfo, "lng"));
							MarkerOptions options = new MarkerOptions().position(targetLocation).icon(BitmapDescriptorFactory.fromBitmap(bmp));
							options.snippet(i + "");
							publishProgress(options);
						}
						//							else
						//							{
						//								LatLng targetLocation = new LatLng(AppManager.getJsonDouble(currentInfo, "lat"), AppManager.getJsonDouble(currentInfo, "lng"));
						//								String category = AppManager.getJsonString(AppManager.getJsonObject(currentInfo, "category"), "title");
						//								MarkerOptions options = new MarkerOptions().position(targetLocation).icon(BitmapDescriptorFactory.fromResource(AppManager.getInstanse().getMapCategoryImage(category)));
						//								options.snippet(i + "");
						//								publishProgress(options);
						//								Log.d("myLogs", "bmp null url = " + url);
						//							}
					}
					//					}
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
			newMarker.setSnippet(options.getSnippet());
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

	@Override
	public void onInfoWindowClick(Marker marker) 
	{
		int index = Integer.valueOf(marker.getSnippet());
		if (index == -1)
		{
			if (addressPlace != null)
			{
				txtCity.setText(addressPlace.getCity());
				txtAddress.setText(addressPlace.getAddress1());
			}
			step = 0;
			btnNextStep.setText("Next Step");
			viewAddPlace.setVisibility(View.VISIBLE);
			layStep1.setVisibility(View.VISIBLE);
			layStep2.setVisibility(View.GONE);
			layStep3.setVisibility(View.GONE);
			return;
		}
		JSONObject obj = AppManager.getJsonObject(arrInfo, index);

		DetailFragment frag = new DetailFragment();
		frag.mainInfo = obj;
		((MainActivity) getActivity()).openDetailFragment(frag);
	}

	private boolean infoWindowIsShow = false;
	private Marker lastMarker;

	private class MarkerClickListener implements GoogleMap.OnMarkerClickListener 
	{
		@Override
		public boolean onMarkerClick(Marker marker)
		{
			if(lastMarker == null)
			{
				marker.showInfoWindow();
				lastMarker = marker;
				infoWindowIsShow=true;
			}
			else if (marker.getId().equals(lastMarker.getId())) 
			{
				//	            if (infoWindowIsShow) {
					//	                marker.hideInfoWindow();
				//	                infoWindowIsShow = false;
				//	            } else {
				marker.showInfoWindow();
				infoWindowIsShow = true;
				//	            }
			} 
			else
			{
				if (infoWindowIsShow) 
				{
					lastMarker.hideInfoWindow();
					marker.showInfoWindow();
					infoWindowIsShow = true;
					lastMarker = marker;
				} 
				else 
				{
					marker.showInfoWindow();
					infoWindowIsShow = true;
					lastMarker = marker;
				}
			}
			return true;
		}
	}

	ImageLoaderListener imageLoaderListener = new ImageLoaderListener()
	{
		@Override
		public void bitmapDownloadDone() 
		{
			if (infoWindowIsShow && lastMarker != null)
			{
				lastMarker.showInfoWindow();
			}
		}
	};
	
	private void addMarkerForAddPlace(LatLng location)
	{
		Log.d("Info", "myLocation = " + location.latitude + "   " + location.longitude);
		txtName.setText("");
		txtCity.setText("");
		txtAddress.setText("");
		txtWebUrl.setText("");
		txtDescription.setText("");
		txtTags.setText("");
		txtCategory.setText("");
		showPayPalInfo(false);
		isAddPlace = true;
		listView.setVisibility(View.GONE);
		arrPhotos.clear();
		arrRealPhotos.clear();
		adapterHorizontalList.notifyDataSetChanged();
		map.setMyLocationEnabled(false);
		map.animateCamera(CameraUpdateFactory.newLatLngZoom(location , 13));
		MarkerOptions options = new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.cat_contributionst));
		options.snippet("-1");
		Marker newMarker = map.addMarker(options);
		lastMarker = newMarker;
		newMarker.setSnippet(options.getSnippet());
		newMarker.showInfoWindow();
		
		double longitude = location.longitude;
		double latitude = location.latitude;
		addressPlace.initAddress(latitude + "", longitude + "");
	}
	
	class CategoryAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return AppManager.getInstanse().categories == null ? 0 : AppManager.getInstanse().categories.size();
		}

		@Override
		public Object getItem(int position)
		{
			return position;
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			TextView txt = (TextView) convertView;
			if (txt == null)
			{
				txt = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null);
				txt.setTextColor(Color.parseColor("#757575"));
			}
			
			txt.setText(AppManager.getJsonString(AppManager.getInstanse().categories.get(position), "title"));
			
			return txt;
		}
	}
	
	private void showPayPalInfo(boolean show)
	{
		if (show)
		{
			layType.setVisibility(View.VISIBLE);
			viewDivider.setVisibility(View.VISIBLE);
			viewDivider1.setVisibility(View.VISIBLE);
			txtPaymentInfo.setVisibility(View.VISIBLE);
			layPaypal.setVisibility(View.VISIBLE);
		}
		else 
		{
			layType.setVisibility(View.GONE);
			viewDivider.setVisibility(View.GONE);
			viewDivider1.setVisibility(View.GONE);
			txtPaymentInfo.setVisibility(View.GONE);
			layPaypal.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK)
		{
			if (fromCamera)// && isDoCrop)
			{
				File f = new File(mImageCaptureUri.getPath());
				if (f.exists())
				{
					f.delete();
				}
			}
			return;
		}

		switch (requestCode)
		{
		case PICK_FROM_CAMERA:
			fromCamera = true;
			reloadHorizontalList();
			File f = new File(mImageCaptureUri.getPath());
			if (f.exists())
			{
				f.delete();
			}
			break;
		case PICK_FROM_FILE:
			fromCamera = false;
			mImageCaptureUri = data.getData();
			reloadHorizontalList();
			break;
//		case CROP_FROM_CAMERA:
//			Bundle extras = data.getExtras();
//			if (extras != null)
//			{
//				bmpImage = extras.getParcelable("data");
//		        bmpImage = Bitmap.createScaledBitmap(bmpImage, 200, 200, true);


//		        arrPhotos.add(bmpImage);
//		        adapterHorizontalList.notifyDataSetChanged();
//		        listHorizontal.scrollTo(arrPhotos.size() * 200);
//			}
//
//			if (fromCamera)
//			{
//				File f = new File(mImageCaptureUri.getPath());
//				if (f.exists())
//				{
//					f.delete();
//				}
//			}
//			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void reloadHorizontalList()
	{
		try
		{
			String path = getRealPathFromURI(mImageCaptureUri);
			bmpImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageCaptureUri);
			bmpImage = ExifUtils.rotateBitmap(path, bmpImage);
			arrRealPhotos.add(bmpImage);
	        float scalingFactor = getBitmapScalingFactor(bmpImage);
	        bmpImage = scaleBitmap(bmpImage, path, scalingFactor);
	        arrPhotos.add(bmpImage);
	        adapterHorizontalList.notifyDataSetChanged();
	        listHorizontal.scrollTo(arrPhotos.size() * 200);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private String getRealPathFromURI(Uri contentURI)
	{
	    String result;
	    Cursor cursor = getActivity().getContentResolver().query(contentURI, null, null, null, null);
	    if (cursor == null) 
	    {
	        result = contentURI.getPath();
	    }
	    else
	    { 
	        cursor.moveToFirst(); 
	        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	        result = cursor.getString(idx);
	        cursor.close();
	    }
	    
	    return result;
	}
	
    private float getBitmapScalingFactor(Bitmap bm) 
    {
    	float scaleByWidth = (float) imgWidth / (float) bm.getWidth();
    	float scaleByHeight = (float) imgWidth / (float) bm.getHeight();
        return scaleByHeight > scaleByWidth ? scaleByHeight : scaleByWidth;
    }
	

	public Bitmap scaleBitmap(Bitmap bm, String path, float scalingFactor)
	{
		int scaleHeight = (int) (bm.getHeight() * scalingFactor);
		int scaleWidth = (int) (bm.getWidth() * scalingFactor);

		bm = Bitmap.createScaledBitmap(bm, scaleWidth, scaleHeight, true);
		
		final Rect rect = new Rect((bm.getWidth() - imgWidth)/2, (bm.getHeight() - imgWidth)/2, (bm.getWidth() - imgWidth)/2 + imgWidth, (bm.getHeight() - imgWidth)/2 + imgWidth);
		Bitmap croppedBitmap = null;
		croppedBitmap = Bitmap.createBitmap(bm, rect.left, rect.top, rect.width(), rect.height());
		
		return croppedBitmap;
	}
	
//	private void doCrop()
//	{
//		isDoCrop = true;
//		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
//
//		Intent intent = new Intent("com.android.camera.action.CROP");
//		intent.setType("image/*");
//
//		List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities( intent, 0 );
//
//		int size = list.size();
//
//		if (size == 0)
//		{
//			Toast.makeText(getActivity(), "Can not find image crop app", Toast.LENGTH_SHORT).show();
//
//			return;
//		}
//		else
//		{
//			intent.setData(mImageCaptureUri);
//
//			intent.putExtra("outputX", 200);
//			intent.putExtra("outputY", 200);
//			intent.putExtra("aspectX", 1);
//			intent.putExtra("aspectY", 1);
//			intent.putExtra("scale", true);
//			intent.putExtra("return-data", true);
//
//			if (size == 1) {
//				Intent i 		= new Intent(intent);
//				ResolveInfo res	= list.get(0);
//
//				i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//
//				startActivityForResult(i, CROP_FROM_CAMERA);
//			} else {
//				for (ResolveInfo res : list) {
//					final CropOption co = new CropOption();
//
//					co.title 	= getActivity().getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
//					co.icon		= getActivity().getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
//					co.appIntent= new Intent(intent);
//
//					co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
//
//					cropOptions.add(co);
//				}
//
//				CropOptionAdapter adapter = new CropOptionAdapter(getActivity().getApplicationContext(), cropOptions);
//
//				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//				builder.setTitle("Choose Crop App");
//				builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
//					public void onClick( DialogInterface dialog, int item ) {
//						startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
//					}
//				});
//
//				builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
//					@Override
//					public void onCancel( DialogInterface dialog ) {
//						if (fromCamera)
//						{
//							File f = new File(mImageCaptureUri.getPath());
//							if (f.exists())
//							{
//								f.delete();
//							}
//						}
//					}
//				} );
//
//				AlertDialog alert = builder.create();
//
//				alert.show();
//			}
//		}
//	}
	
	class HorizontalListAdapter extends BaseAdapter
	{
		@Override
		public int getCount()
		{
			return arrPhotos.size();
		}

		@Override
		public Bitmap getItem(int position)
		{
			return arrPhotos.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = inflater.inflate(R.layout.item_photo, null);
			}
			
			ImageView imgPhoto = (ImageView) view.findViewById(R.id.imgPhoto);
			ImageButton btnDeletePhoto = (ImageButton) view.findViewById(R.id.btnDeletePhoto);
			
			imgPhoto.setImageBitmap(arrPhotos.get(position));
			btnDeletePhoto.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					arrRealPhotos.remove(position);
					arrPhotos.remove(position);
					notifyDataSetChanged();
				}
			});
			
			return view;
		}
	}
	
	class AddPointRequst extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected void onPreExecute()
		{
			viewLoader.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params)
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			JSONObject obj = client.addNewMapPoint(arrRealPhotos, txtName.getText().toString(), txtTags.getText().toString(), txtCategory.getText().toString(),
					addressPlace.getLatitude() + "", addressPlace.getLongitude() + "", txtCity.getText().toString(),
					addressPlace.getState(), txtAddress.getText().toString(), addressPlace.getPostalCode(), AppManager.getJsonString(AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user"), "email"),
					txtWebUrl.getText().toString(), txtDescription.getText().toString(), txtPaypal.getText().toString());
			
			return AppManager.getJsonBoolean(obj, "success");
		}
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			
			if (result)
			{
				AppManager.getInstanse().showAlert(getActivity(), "Partam", "Your point add successfully!");
				viewAddPlace.performClick();
			}
			else
			{
				AppManager.getInstanse().showAlert(getActivity(), "Partam", "Something has gone wrong, please try again!");
			}
			
			viewLoader.setVisibility(View.GONE);
		}
	}
}
