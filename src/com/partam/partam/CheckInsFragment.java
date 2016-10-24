package com.partam.partam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import com.partam.partam.customclasses.CheckinAdapter;
import com.partam.partam.customclasses.PartamHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CheckInsFragment extends Fragment {

	JSONArray comments;
	View viewLoader;
	String category;
	TextView txtName;
	TextView txtCity;
	TextView txtCategory;
	TextView txtCommentCount;
	TextView txtCheckinCount;
	ListView list_checkins;
	MainActivity mainActivity;
	ImageView img_back_checkin;
	public JSONObject webInfo;
	public String name;
	public String locationName;
	public String categoryName;
	public String commentsCount;
	CheckinAdapter checkinAdapter;
	RelativeLayout add_checkins;
	private Uri mSelfieImageCaptureUri;
	private static final int PICK_FROM_CAMERA = 1;
	Bitmap bmpSelife;
	public TextView detailFragmentCheckinsCount;
	ArrayList<JSONObject> jsonCheckinsArray = new ArrayList<JSONObject>();

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//		this.inflater = inflater;
		View v = inflater.inflate(R.layout.fragment_checkins, null);
		mainActivity = (MainActivity) getActivity();
		viewLoader = v.findViewById(R.id.viewLoader);
		txtCheckinCount = (TextView) v.findViewById(R.id.txtCheckinCount);
		txtCheckinCount.setText(String.valueOf(AppManager.getInstanse().checkinsArray.length()));

		checkinAdapter = new CheckinAdapter(getActivity(), null);
		checkinAdapter.categoryName = categoryName;
		checkinAdapter.webInfo = webInfo;
		checkinAdapter.viewLoader = viewLoader;
		checkinAdapter.checkinCounts = txtCheckinCount;

		txtName = (TextView) v.findViewById(R.id.txtName);
		txtName.setText(name);

		txtCity = (TextView) v.findViewById(R.id.txtCity);
		txtCity.setText(locationName);

		txtCategory = (TextView) v.findViewById(R.id.txtCategory);
		txtCategory.setText(categoryName);

		txtCommentCount = (TextView) v.findViewById(R.id.txtCommentCount);
		txtCommentCount.setText(commentsCount);

		initActionBar(v);

		list_checkins = (ListView) v.findViewById(R.id.list_checkins);
		list_checkins.setAdapter(checkinAdapter);
		//		list_checkins.setLayoutParams(new RelativeLayout.LayoutParams(AppManager.getInstanse().screenWidth-60,AppManager.getInstanse().screenWidth-60));
		jsonCheckinsArray.clear();

		if(AppManager.getInstanse().checkinsArray.length() != 0)
		{
			for(int i = 0; i < AppManager.getInstanse().checkinsArray.length(); i++)
				jsonCheckinsArray.add(AppManager.getJsonObject(AppManager.getInstanse().checkinsArray, i));
			Collections.reverse(jsonCheckinsArray);
			Log.e("Info","jsonCheckinsArray is <<<<<<<<<<<<<<" + jsonCheckinsArray.isEmpty());
			checkinAdapter.reloadData(jsonCheckinsArray);
		}


		//checkinAdapter.reloadData(AppManager.getInstanse().checkinsArray);

		add_checkins = (RelativeLayout) v.findViewById(R.id.add_checkins);
		add_checkins.setOnClickListener(new OnClickListener() {

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
		return v;
	}

	@SuppressLint("NewApi")
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

	private void initActionBar(View v)
	{

		img_back_checkin = (ImageView) v.findViewById(R.id.img_back_checkin);
		img_back_checkin.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				detailFragmentCheckinsCount.setText(txtCheckinCount.getText());
				Log.e("Info","detailFragmentCheckinsCount text ===== " + detailFragmentCheckinsCount.getText());
				((MainActivity) getActivity()).closeFragments();
			}
		});

	}

	private void openLoginPage()
	{
		LoginFragment frag = new LoginFragment();
		((MainActivity) getActivity()).addFragment(frag, false);
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
			txtCheckinCount.setText(String.valueOf(AppManager.getInstanse().checkinsArray.length()));
			Log.e("Info","AppManager.getInstanse().checkinsArray.length()) " + AppManager.getInstanse().checkinsArray.length());
			jsonCheckinsArray.clear(); 

			if(AppManager.getInstanse().checkinsArray.length() != 0)
			{
				for(int i = 0; i < AppManager.getInstanse().checkinsArray.length(); i++)
					jsonCheckinsArray.add(AppManager.getJsonObject(AppManager.getInstanse().checkinsArray, i));
				Collections.reverse(jsonCheckinsArray);
				checkinAdapter = new CheckinAdapter(getActivity(), jsonCheckinsArray);
				checkinAdapter.categoryName = categoryName;
				checkinAdapter.webInfo = webInfo;
				checkinAdapter.viewLoader = viewLoader;
				checkinAdapter.checkinCounts = txtCheckinCount;

				list_checkins.setAdapter(checkinAdapter);
				viewLoader.setVisibility(View.GONE);
				
				//		>>>	checkinAdapter.reloadData(jsonCheckinsArray); <<<
				/* in this case adapter's getView isn't called !!!!!!!!!!!!!!!!!!!!!!!
			 Have you made sure that you are calling notifyDataSetChanged on the correct instance of the adapter
			  (i.e. the one attached to the list view)?
			 If during your life cycle you re-instantiated the adapter without attaching it to the ListView,
			  that can result in such annoying bugs. */
				
				list_checkins.post(new Runnable() {
					@SuppressLint("NewApi")
					@Override
					public void run() {
						Log.e("Info", "list_checkins.post!!!!!!!!!!!!!!!!!!!!");
						list_checkins.smoothScrollToPositionFromTop(0, 0);
						
					}
				});

			}



			//checkinAdapter.reloadData(AppManager.getInstanse().checkinsArray);
		}

	}

	private void uploadSelfie()
	{
		try {
			bmpSelife = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mSelfieImageCaptureUri);
			PartamHttpClient mHttpClient = PartamHttpClient.getInstance();
			File file = new File(mSelfieImageCaptureUri.getPath());
			Log.e("Info", "bmpSelife.getByteCount() ==== " + bmpSelife.getByteCount());
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			bmpSelife.compress(Bitmap.CompressFormat.JPEG, 50, out);
//			Bitmap scaledBmp = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
			Bitmap scaledBmp = decodeFile(file);
			Log.e("Info", "scaledBmp weight>> = " + scaledBmp.getWidth());
			Log.e("Info", "scaledBmp height>> = " + scaledBmp.getHeight()); 
			Log.e("Info", "scaledBmp.getByteCount() ==== " + scaledBmp.getByteCount()); 

			mHttpClient.postCheckin(AppManager.getJsonInt(webInfo, "id"), AppManager.getInstanse().token, "", scaledBmp, 
					AppManager.getJsonInt(AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user"), "id") );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
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
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		//		jsonCheckinsArray.clear();
		//		for(int i = 0; i < AppManager.getInstanse().checkinsArray.length(); i++)
		//			jsonCheckinsArray.add(AppManager.getJsonObject(AppManager.getInstanse().checkinsArray, i));
		////		Collections.reverse(jsonCheckinsArray);
		//		Log.e("Info","jsonCheckinsArray is ==========" + jsonCheckinsArray.isEmpty());
		//		checkinAdapter.reloadData(jsonCheckinsArray);
		////		checkinAdapter.reloadData(AppManager.getInstanse().checkinsArray);
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
				scale *= 2;
			}

			//decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o.inJustDecodeBounds = false;
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2); // 320*240  320*320
		} catch (FileNotFoundException e) {
		}
		return null;
	}



}
