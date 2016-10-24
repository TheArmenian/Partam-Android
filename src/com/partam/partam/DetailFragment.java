package com.partam.partam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
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
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.partam.partam.customclasses.DetailPagerAdapter;
import com.partam.partam.customclasses.FB;
import com.partam.partam.customclasses.MediaInfo;
import com.partam.partam.customclasses.PartamHttpClient;
import com.partam.partam.customclasses.TwitterActivity;
import com.partam.partam.customclasses.WebImage;
import com.partam.partam.image_loader.ImageLoader;
import com.partam.partam.image_loader.Utils;
import com.pinterest.pinit.PinItButton;
import com.viewpagerindicator.CirclePageIndicator;

@SuppressLint("NewApi")
public class DetailFragment extends Fragment implements OnItemClickListener
{
	public interface DetailListener
	{
		public void closeDone();
	}

	private DetailListener listener = null;

	public void setOnDetailEventListener(final DetailListener listener)
	{
		this.listener = listener;
	}

	JSONObject mainInfo;
	JSONObject webInfo;
	ArrayList<HashMap<String, String>> detailInfoArray;
	JSONArray pictures;
	JSONArray videos;
	JSONArray comments;
	JSONArray checkIns;
	MainActivity mainActivity;

	String category;

	View viewLoader;
	ViewPager pager;
	CirclePageIndicator indicator;
	TextView txtName;
	TextView txtCity;
	View layMap;
	Button btnDirections;
	TextView txtCategory;
	TextView txtCommentCount;
	TextView txtCheckinCount;
	WebView webView;
	ListView list;
	Button btnLogin;
	View viewPostComment;
	ImageView imgProfile;
	EditText txtComment;
	View btnPost;

	ImageButton btnCheckinWithSelfie;
	private Uri mSelfieImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	Bitmap bmpSelife;
	int imgSelfieWidth;
	RelativeLayout relChecks;

	GoogleMap map;	
	ImageLoader loader = new ImageLoader();
	ListAdapter adapter;
	LayoutInflater inflater;

	View detailMain;
	View loginPostView;
    View viewAllCheckins;
    
	ImageView imgRight;

	int detailCount = 0;
	FB fb;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		
		this.inflater = inflater;
		View v = inflater.inflate(R.layout.fragment_detail, null);
		mainActivity = (MainActivity) getActivity();
		initActionBar(v);
		viewLoader = v.findViewById(R.id.viewLoader);
		list = (ListView) v.findViewById(R.id.list);
		adapter = new ListAdapter();
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
		
		if (AppManager.getJsonObject(mainInfo, "category") != null)
		{
			category = AppManager.getJsonString(AppManager.getJsonObject(mainInfo, "category"), "title");
		}

		loginPostView = inflater.inflate(R.layout.item_login_post, null);	
		btnLogin = (Button) loginPostView.findViewById(R.id.btnLogin);
		viewPostComment = loginPostView.findViewById(R.id.viewPostComment);
		imgProfile = (ImageView) loginPostView.findViewById(R.id.imgProfile);
		txtComment = (EditText) loginPostView.findViewById(R.id.txtComment);
		btnPost = loginPostView.findViewById(R.id.btnPost);
		btnLogin.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {
				openLoginPage();
			}
		});
		btnPost.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				if (txtComment.getText().toString().length() > 0)
				{
					AppManager.getInstanse().hideKeyboard(txtComment);
					viewLoader.setVisibility(View.VISIBLE);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
					{
						new CommentTak().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} 
					else
					{
						new CommentTak().execute();
					}
				}
			}
		});

		viewAllCheckins = inflater.inflate(R.layout.view_all_checkins, null);
		viewAllCheckins.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				relChecks.performClick();
			}
		});
		
		detailMain = inflater.inflate(R.layout.item_detail_main, null);
		txtName = (TextView) detailMain.findViewById(R.id.txtName);
		txtCity = (TextView) detailMain.findViewById(R.id.txtCity);
		txtCategory = (TextView) detailMain.findViewById(R.id.txtCategory);
		txtCommentCount = (TextView) detailMain.findViewById(R.id.txtCommentCount);
		txtCheckinCount = (TextView) detailMain.findViewById(R.id.txtCheckinCount);
		webView = (WebView) detailMain.findViewById(R.id.webView);
		layMap = detailMain.findViewById(R.id.layMap);
		btnDirections = (Button) detailMain.findViewById(R.id.btnDirections);

		relChecks = (RelativeLayout) detailMain.findViewById(R.id.relChecks);
		relChecks.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				viewLoader.setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new GetCheckInsTask ().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} 
				else
				{
					new GetCheckInsTask ().execute();
				}
			}
		});

		btnCheckinWithSelfie = (ImageButton) detailMain.findViewById(R.id.btn_checkin_with_selfie);
		btnCheckinWithSelfie.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (AppManager.getInstanse().isLogOut)
				{
					openLoginPage();
					return;
				}
				else
				takeSelfie(); 

			}
		});

		pager = (ViewPager) detailMain.findViewById(R.id.pager);
		indicator = (CirclePageIndicator) detailMain.findViewById(R.id.indicator);
		LayoutParams params = (LayoutParams) pager.getLayoutParams();
		params.height = AppManager.getInstanse().screenWidth;
		params.width = AppManager.getInstanse().screenWidth;
		pager.setLayoutParams(params);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new GetInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new GetInfo().execute();
		}

		if (!AppManager.getInstanse().isLogOut)
		{
			loggedIn();
		}

		fb = new FB(getActivity());

		DisplayMetrics metrics = getResources().getDisplayMetrics();
		imgSelfieWidth = (int) (metrics.density * 100);

		return v;
	}
	
	public Bitmap decodeFile(File f) { 
	try {
		//decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(new FileInputStream(f), null, o);

		//Find the correct scale value. It should be the power of 2.
		final int REQUIRED_SIZE = 480;
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
//			scale++;
			scale *= 2;
		}

		//decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o.inJustDecodeBounds = false;
//		Log.e("Info", "scale = " + scale);
		o2.inSampleSize = scale;
//		Log.e("Info", "width = " + BitmapFactory.decodeStream(new FileInputStream(f), null, o2).getWidth() + "height = " + BitmapFactory.decodeStream(new FileInputStream(f), null, o2).getWidth());
		return BitmapFactory.decodeStream(new FileInputStream(f), null, o2); // 320*240  320*320
	} catch (FileNotFoundException e) {
	}
	return null;
}

	void takeSelfie() 
	{
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		mSelfieImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_selfie" + ".jpg"));

		intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mSelfieImageCaptureUri);

		try {
			intent.putExtra("return-data", true);
			if(getFrontCameraId()!=-1)
			{
				intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
				startActivityForResult(intent, PICK_FROM_CAMERA);
			}
			else
				startActivityForResult(intent, PICK_FROM_CAMERA);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}

	}

	private int getFrontCameraId(){
		int camId = -1;
		int cameraCount = Camera.getNumberOfCameras();
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) 
				camId = camIdx;
		}

		return camId;
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != Activity.RESULT_OK)
		{
			File f = new File(mSelfieImageCaptureUri.getPath());
			if (f.exists())
			{
				f.delete();
			}

			return;
		}

		if(requestCode == PICK_FROM_CAMERA)
		{
			viewLoader.setVisibility(View.VISIBLE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
			{
				new UploadSelfieTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			} 
			else
			{
				new UploadSelfieTask().execute();
			}
			
		}
	}

	private void uploadSelfie()
	{
		String path = getRealPathFromURI(mSelfieImageCaptureUri);
		
		try {
			bmpSelife = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mSelfieImageCaptureUri);
			Log.e("Info", "bmpSelife weight = " + bmpSelife.getWidth());
			Log.e("Info", "bmpSelife height = " + bmpSelife.getHeight()); 
			File file = new File(mSelfieImageCaptureUri.getPath());
			Bitmap scaledBmp = decodeFile(file);
			Log.e("Info", "scaledBmp weight!!! = " + scaledBmp.getWidth());
			Log.e("Info", "scaledBmp height!!! = " + scaledBmp.getHeight()); 
			
			PartamHttpClient mHttpClient = PartamHttpClient.getInstance();
			mHttpClient.postCheckin(AppManager.getJsonInt(webInfo, "id"), AppManager.getInstanse().token, "", scaledBmp, 
					AppManager.getJsonInt(AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user"), "id") );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//		bmpSelife = ExifUtils.rotateBitmap(path, bmpSelife);
		//      float scalingFactor = getBitmapScalingFactor(bmpSelife);
		//      bmpSelife = scaleBitmap(bmpSelife, path, scalingFactor);
	}

	private float getBitmapScalingFactor(Bitmap bm) 
	{
		float scaleByWidth = (float) imgSelfieWidth / (float) bm.getWidth();
		float scaleByHeight = (float) imgSelfieWidth / (float) bm.getHeight();
		return scaleByHeight > scaleByWidth ? scaleByHeight : scaleByWidth;
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

	private class GetCheckInsTask extends AsyncTask<Void, Void, Void> 
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			client.getCheckIns(AppManager.getJsonInt(webInfo, "id"));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) 
		{
			super.onPostExecute(result);
			viewLoader.setVisibility(View.GONE);
			CheckInsFragment checkInsFragment = new CheckInsFragment();
			checkInsFragment.name = txtName.getText().toString();
			checkInsFragment.locationName = txtCity.getText().toString();
			checkInsFragment.categoryName = txtCategory.getText().toString();
			checkInsFragment.commentsCount = txtCommentCount.getText().toString();
			checkInsFragment.webInfo = webInfo;
			checkInsFragment.detailFragmentCheckinsCount = txtCheckinCount;  
			mainActivity.addFragment(checkInsFragment, true);
		}
	}
	
	private class UploadSelfieTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			uploadSelfie();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) { 
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			File f = new File(mSelfieImageCaptureUri.getPath());
			if (f.exists())
			{
				f.delete();
			}
			CheckInsFragment checkInsFragment = new CheckInsFragment();
			Log.e("Info", "(DetailFragment AppManager.getJsonInt(webInfo, id) = " + AppManager.getJsonInt(webInfo, "id"));
			checkInsFragment.webInfo = webInfo;
			checkInsFragment.detailFragmentCheckinsCount = txtCheckinCount; 
			
			checkInsFragment.name = txtName.getText().toString();
			checkInsFragment.locationName = txtCity.getText().toString();
			checkInsFragment.categoryName = txtCategory.getText().toString();
			checkInsFragment.commentsCount = txtCommentCount.getText().toString();
			mainActivity.addFragment(checkInsFragment, true);
			viewLoader.setVisibility(View.GONE);
			
		}
		
	}

	private void initActionBar(View v)
	{
		ImageView imgLeft = (ImageView) v.findViewById(R.id.imgLeft);
		imgLeft.setImageResource(R.drawable.btn_back);
		imgLeft.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				((MainActivity) getActivity()).closeDetailFragment();
			}
		});

		imgRight = (ImageView) v.findViewById(R.id.imgRight);
		imgRight.setImageResource(R.drawable.btn_favorite_empty);
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

				viewLoader.setVisibility(View.VISIBLE);
				boolean favorite = !imgRight.isSelected();
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new FavoriteTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, favorite);
				} 
				else
				{
					new FavoriteTask().execute(favorite);
				}
			}
		});

		ImageView imgShare = (ImageView) v.findViewById(R.id.imgShare);
		imgShare.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				final String [] items        = new String [] {"Facebook", "Pinterest", "Twitter", "VKontakte", "Cancel"};
				ArrayAdapter<String> adapter = new ArrayAdapter<String> (getActivity(), android.R.layout.select_dialog_item,items);
				AlertDialog.Builder builder  = new AlertDialog.Builder(getActivity());

				builder.setTitle("Share via");
				builder.setAdapter( adapter, new DialogInterface.OnClickListener()
				{
					public void onClick( DialogInterface dialog, int item ) 
					{
						String description = Html.fromHtml(AppManager.getJsonString(webInfo, "description")).toString();
						MediaInfo mi = ((DetailPagerAdapter) pager.getAdapter()).getMediaInfo(indicator.getCurrentPosition());
						String link = "https://play.google.com/store/apps/details?id=com.partam.partam";
						String picture = mi.imageUrl;
						switch (item)
						{
						//facebook
						case 0:
							fb.post("", txtName.getText().toString(), "PARTAM", description, link, picture, null);
							break;
							//pinterest
						case 1:
							PinItButton pinIt = new PinItButton(getActivity());
							pinIt.setImageUrl(picture);
							pinIt.setUrl(link);
							pinIt.setDescription(txtName.getText() + "\n" + description);
							pinIt.performClick();
							break;
							//twitter
						case 2:
							TwitterActivity.tweetText = txtName.getText().toString();
							TwitterActivity.imgUrl = picture;
							startActivity(new Intent(getActivity(), TwitterActivity.class));
							break;
							//VKontakte
						case 3:
							VKActivity.link = link;
							VKActivity.imgUrl = picture;
							VKActivity.text = txtName.getText() + "\n" + description;
							startActivity(new Intent(getActivity(), VKActivity.class));
							break;

						default:
							break;
						}
					}
				} );

				final AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
	}

	private void openLoginPage()
	{
		LoginFragment frag = new LoginFragment();
		((MainActivity) getActivity()).addFragment(frag, false);
	}

	public void loggedIn()
	{
		btnLogin.setVisibility(View.GONE);
		viewPostComment.setVisibility(View.VISIBLE);
		JSONObject userInfo = AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user");
		String pictureUrl = AppManager.getJsonString(userInfo, "picture");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new WebImage(imgProfile, pictureUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new WebImage(imgProfile, pictureUrl).execute();
		} 
	}

	private void showDetailInfo()
	{
		if (AppManager.getInstanse().userFavorites.length() > 0)
		{
			int id = AppManager.getJsonInt(webInfo, "id");
			JSONArray favorites = AppManager.getJsonArray(AppManager.getInstanse().userFavorites, "favorites");
			for (int i = 0; i < favorites.length(); i++) 
			{
				int favID = AppManager.getJsonInt(AppManager.getJsonObject(favorites, i), "id");
				if (favID == id)
				{
					imgRight.setSelected(true);
					imgRight.setImageResource(R.drawable.btn_favorite_full);
					break;
				}
			}
		}

		txtName.setText(AppManager.getJsonString(webInfo, "name"));
		txtCity.setText(AppManager.getJsonString(webInfo, "city"));
		txtCategory.setText(category);

		pictures = AppManager.getJsonArray(webInfo, "pictures");
		videos = AppManager.getJsonArray(webInfo, "videos");
		comments = AppManager.getJsonArray(webInfo, "comments");
		checkIns = AppManager.getJsonArray(webInfo, "checkins");

		txtCommentCount.setText(comments.length()+"");
		txtCheckinCount.setText(checkIns.length()+"");

		loader.showDefaultImage(false);
		ArrayList<MediaInfo> arrInfo = new ArrayList<MediaInfo>(pictures.length() + videos.length());
		for (int i = 0; i < pictures.length(); i++) 
		{
			MediaInfo info = new MediaInfo();
			try {
				info.imageUrl = AppManager.getInstanse().getUrlForCrop(pictures.getString(i));
				info.loader = loader;
				arrInfo.add(info);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		for (int i = 0; i < videos.length(); i++) 
		{
			MediaInfo info = new MediaInfo();
			try {
				JSONObject currentVideoInfo = videos.getJSONObject(i);
				info.imageUrl = currentVideoInfo.getString("thumb");
				info.videoID = currentVideoInfo.getString("id");
				info.isVideo = true;
				info.loader = loader;
				arrInfo.add(info);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		pager.setAdapter(new DetailPagerAdapter(getFragmentManager(), arrInfo)); 
		if (arrInfo.size() > 1) 
		{
			indicator.setViewPager(pager);
			indicator.setSnap(true);

			list.setOnTouchListener(new View.OnTouchListener() 
			{
				public boolean onTouch(View v, MotionEvent event) 
				{
					list.requestDisallowInterceptTouchEvent(false);
					return false;
				}
			});

			pager.setOnTouchListener(new View.OnTouchListener() 
			{
				public boolean onTouch(View v, MotionEvent event)
				{
					list.requestDisallowInterceptTouchEvent(true);
					return false;
				}
			});
		}

		String html = AppManager.getJsonString(webInfo, "description");
		if (html.length() > 0)
		{
			String mimeType = "text/html";
			String encoding = "UTF-8";
			webView.loadDataWithBaseURL("", html, mimeType, encoding, ""); 
		}
		else
		{
			webView.setVisibility(View.GONE);
		}
	}

	void showMap()
	{
		SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapDetail);
		if (AppManager.getJsonDouble(webInfo, "lat") == -1 || AppManager.getJsonDouble(webInfo, "lng") == -1)
		{
			layMap.setVisibility(View.GONE);
			mapFragment.getView().setVisibility(View.GONE);
			return;
		}

		if (AppManager.getJsonDouble(webInfo, "lat") == 0 && AppManager.getJsonDouble(webInfo, "lng") == 0)
		{
			layMap.setVisibility(View.GONE);
			mapFragment.getView().setVisibility(View.GONE);
			return;
		}

		map = mapFragment.getMap();
		LatLng location = new LatLng(AppManager.getJsonDouble(webInfo, "lat"), AppManager.getJsonDouble(webInfo, "lng"));
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13));
		btnDirections.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getActivity(), NavigationActivity.class);
				intent.putExtra("webInfo", webInfo.toString());
				intent.putExtra("category", category);
				intent.putExtra("categoryID", AppManager.getJsonInt(AppManager.getJsonObject(mainInfo, "category"), "id"));
				startActivity(intent);
			}
		});
		map.getUiSettings().setZoomControlsEnabled(false);
		map.getUiSettings().setMyLocationButtonEnabled(false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new AddMarker().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new AddMarker().execute();
		}
	}

	class AddMarker extends AsyncTask<Void, MarkerOptions, Void>
	{
		@Override
		protected Void doInBackground(Void... params) 
		{
			JSONObject info = new JSONObject();
			int currentID = AppManager.getJsonInt(AppManager.getJsonObject(mainInfo, "category"), "id");
			for (int i = 0; i < AppManager.getInstanse().allPoints.length(); i++)
			{
				JSONObject obj = AppManager.getJsonObject(AppManager.getInstanse().allPoints, i);
				int id = AppManager.getJsonInt(AppManager.getJsonObject(obj, "category"), "id");
				if (currentID == id)
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
			map.addMarker(options);
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

	class GetInfo extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			webInfo = client.loadDetailInfo(AppManager.getJsonInt(mainInfo, "id"));
			return null;
		}

		@Override
		protected void onPostExecute(Void result) 
		{
			super.onPostExecute(result);
			if (getView() == null) {
				return;
			}
			viewLoader.setVisibility(View.GONE);

			if (webInfo != null)
			{
				showDetailInfo();
				showMap();
				adapter.reloadData();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (listener != null) {
			listener.closeDone();
		}
		loader.clearMemoryCache();
		SupportMapFragment f = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.mapDetail);
		if (f != null && !getActivity().isFinishing())
			getFragmentManager().beginTransaction().remove(f).commit();
	}

	class ListAdapter extends BaseAdapter
	{
		public void reloadData() 
		{
			detailCount = getDetailCount();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() 
		{
			return detailCount + getCommentsCount() + 3;
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
			if (position == 0)
			{
				return detailMain;
			}
			
			if (position == detailCount + 1) 
			{
				return viewAllCheckins;  
			}
			
			if (position == detailCount + getCommentsCount() + 2) 
			{
				return loginPostView;  
			}

			View v;
			
			if (position <= detailCount)
			{
				position = position - 1;
				HashMap<String, String> hm = detailInfoArray.get(position);
				String type = hm.get("type");
				if (!type.contentEquals("address"))
				{
					v = inflater.inflate(R.layout.item_detail_info, null);
				}
				else
				{
					v = inflater.inflate(R.layout.item_detail_info_direction, null);
				}
				((ImageView) v.findViewById(R.id.imgIcon)).setImageResource(getImageID(type));
				((TextView) v.findViewById(R.id.txt)).setText(hm.get("info"));
			}
			else
			{
				position = position - detailCount - 2;
				v = inflater.inflate(R.layout.item_comment, null);
				JSONObject obj = AppManager.getJsonObject(comments, position);
				((TextView) v.findViewById(R.id.txtUserName)).setText(AppManager.getJsonString(obj, "name"));
				((TextView) v.findViewById(R.id.txtComment)).setText(AppManager.getJsonString(obj, "message"));

				Date date;
				try {
					date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US).parse(AppManager.getJsonString(obj, "created"));
					((TextView) v.findViewById(R.id.txtClock)).setText(android.text.format.DateFormat.format("kk:mm", date));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				JSONObject user = AppManager.getJsonObject(obj, "user");
				loader.DisplayImage(AppManager.getJsonString(user, "picture"), ((ImageView) v.findViewById(R.id.imgProfile)));
			}
			
			
			return v;
		}
	}

	int getDetailCount()
	{
		if (webInfo == null) {
			return 0;
		}
		detailInfoArray = new ArrayList<>();
		int count = 0;

		if (AppManager.getJsonString(webInfo, "address").length() > 0)
		{
			HashMap<String, String> hm = new HashMap<>(2);
			hm.put("type", "address");
			hm.put("info", AppManager.getJsonString(webInfo, "address"));
			detailInfoArray.add(hm);
			count++;
		}

		if (AppManager.getJsonString(webInfo, "tel").length() > 0)
		{
			HashMap<String, String> hm = new HashMap<>(2);
			hm.put("type", "tel");
			hm.put("info", AppManager.getJsonString(webInfo, "tel"));
			detailInfoArray.add(hm);
			count++;
		}

		if (AppManager.getJsonString(webInfo, "website").length() > 0)
		{
			HashMap<String, String> hm = new HashMap<>(2);
			hm.put("type", "website");
			hm.put("info", AppManager.getJsonString(webInfo, "website"));
			detailInfoArray.add(hm);
			count++;
		}

		if (AppManager.getJsonString(webInfo, "email").length() > 0)
		{
			HashMap<String, String> hm = new HashMap<>(2);
			hm.put("type", "email");
			hm.put("info", AppManager.getJsonString(webInfo, "email"));
			detailInfoArray.add(hm);
			count++;
		}

		if (AppManager.getJsonString(webInfo, "facebook_url").length() > 0)
		{
			HashMap<String, String> hm = new HashMap<>(2);
			hm.put("type", "facebook_url");
			hm.put("info", AppManager.getJsonString(webInfo, "facebook_url"));
			detailInfoArray.add(hm);
			count++;
		}

		if (AppManager.getJsonString(webInfo, "twitter_url").length() > 0)
		{
			HashMap<String, String> hm = new HashMap<>(2);
			hm.put("type", "twitter_url");
			hm.put("info", AppManager.getJsonString(webInfo, "twitter_url"));
			detailInfoArray.add(hm);
			count++;
		}

		if (AppManager.getJsonString(webInfo, "instagram_url").length() > 0)
		{
			HashMap<String, String> hm = new HashMap<>(2);
			hm.put("type", "instagram_url");
			hm.put("info", AppManager.getJsonString(webInfo, "instagram_url"));
			detailInfoArray.add(hm);
			count++;
		}

		return count;
	}

	int getCommentsCount()
	{
		return comments != null ? comments.length() : 0;
	}

	int getImageID(String type)
	{
		int id = 0;
		if (type.contentEquals("address")) 
		{
			id = R.drawable.icon_location;
		}
		else if (type.contentEquals("tel")) 
		{
			id = R.drawable.icon_phone;
		}
		else if (type.contentEquals("website")) 
		{
			id = R.drawable.icon_open_web;
		}
		else if (type.contentEquals("email")) 
		{
			id = R.drawable.icon_email;
		}
		else if (type.contentEquals("facebook_url")) 
		{
			id = R.drawable.icon_facebook;
		}
		else if (type.contentEquals("twitter_url")) 
		{
			id = R.drawable.icon_twitter;
		}
		else if (type.contentEquals("instagram_url")) 
		{
			id = R.drawable.icon_instagram;
		}
		return id;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	{

		if (position == 0 || (position == detailCount + getCommentsCount() + 1)) 
		{
			return;
		}

		if (position <= detailCount)
		{
			position = position - 1;				
			HashMap<String, String> hm = detailInfoArray.get(position);
			String type = hm.get("type");
			if (type.contentEquals("address"))
			{
				Intent intent = new Intent(getActivity(), NavigationActivity.class);
				intent.putExtra("webInfo", webInfo.toString());
				intent.putExtra("category", category);
				startActivity(intent);
			}
			else if (type.contentEquals("tel"))
			{
				Intent intent = new Intent(Intent.ACTION_CALL);
				intent.setData(Uri.parse("tel:" + hm.get("info")));
				startActivity(intent);
			}
			else if (type.contentEquals("website") || type.contentEquals("facebook_url") || type.contentEquals("twitter_url") || type.contentEquals("instagram_url"))
			{
				AppManager.getInstanse().openUrl(getActivity(), hm.get("info"));
			}
			else if (type.contentEquals("email"))
			{
				Intent email = new Intent(Intent.ACTION_SEND);		  
				email.putExtra(Intent.EXTRA_EMAIL, new String[] { hm.get("info") });
				email.setType("message/rfc822");
				startActivity(email);
			}
		}
		else
		{
			position = position - detailCount - 1;
		}
	}

	class FavoriteTask extends AsyncTask<Boolean, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Boolean... params)
		{
			Boolean favorite = params[0];
			JSONObject obj = PartamHttpClient.getInstance().addOrRemoveFavorites(AppManager.getJsonInt(webInfo, "id"), favorite);
			if (AppManager.getJsonBoolean(obj, "success"))
			{
				PartamHttpClient.getInstance().loadUserFavorites();
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			viewLoader.setVisibility(View.GONE);

			if (result)
			{
				imgRight.setSelected(!imgRight.isSelected());
				if (imgRight.isSelected())
				{
					imgRight.setImageResource(R.drawable.btn_favorite_full);
				}
				else
				{
					imgRight.setImageResource(R.drawable.btn_favorite_empty);
				}
			}
		}
	}

	class CommentTak extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			int pointId = AppManager.getJsonInt(webInfo, "id");
			JSONObject obj = PartamHttpClient.getInstance().addComment(txtComment.getText().toString(), pointId);
			if (AppManager.getJsonBoolean(obj, "success"))
			{
				webInfo = PartamHttpClient.getInstance().loadDetailInfo(pointId);
				comments = AppManager.getJsonArray(webInfo, "comments");
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result)
		{
			super.onPostExecute(result);
			viewLoader.setVisibility(View.GONE);
			txtComment.setText("");

			if (result)
			{
				txtCommentCount.setText(comments.length() + "");
				adapter.reloadData();
			}
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.e("Info", "detail fragment onResume!!!!!!!!!!!!!!!!!!!!!");
		
	}
	
	
}
