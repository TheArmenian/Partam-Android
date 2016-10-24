package com.partam.partam;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.partam.partam.customclasses.CropOption;
import com.partam.partam.customclasses.CropOptionAdapter;
import com.partam.partam.customclasses.PartamHttpClient;
import com.partam.partam.customclasses.WebImage;

@SuppressLint("NewApi")
public class ProfileFragment extends Fragment
{
	View viewLoader;

	EditText txtFullName;
	EditText txtEmail;
	EditText txtPass;
	EditText txtRePass;
	EditText txtLocation;

	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	private Uri mImageCaptureUri;
	private boolean fromCamera = false;
	private boolean isDoCrop = false;
	private Bitmap bmpImage = null;
	ImageView imgProfile;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		View v = inflater.inflate(R.layout.fragment_profile, null);
		initActionBar(v);

		txtFullName = (EditText) v.findViewById(R.id.txtFullName);
		txtEmail = (EditText) v.findViewById(R.id.txtEmail);
		txtPass = (EditText) v.findViewById(R.id.txtPass);
		txtRePass = (EditText) v.findViewById(R.id.txtRePass);
		txtLocation = (EditText) v.findViewById(R.id.txtLocation);

		viewLoader = v.findViewById(R.id.viewLoader);
		viewLoader.setVisibility(View.GONE);

		JSONObject userInfo = AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user");
		txtFullName.setText(AppManager.getJsonString(userInfo, "display_name"));
		txtEmail.setText(AppManager.getJsonString(userInfo, "email"));
		txtLocation.setText(AppManager.getJsonString(userInfo, "current_city"));

		String pictureUrl = AppManager.getJsonString(userInfo, "picture");
		imgProfile = (ImageView) v.findViewById(R.id.imgProfile);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new WebImage(imgProfile, pictureUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new WebImage(imgProfile, pictureUrl).execute();
		}

		v.findViewById(R.id.btnLogout).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				((MainActivity) getActivity()).logOut();
			}
		});

		v.findViewById(R.id.btnChooseImage).setOnClickListener(new OnClickListener() 
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

		return v;
	}

	private void initActionBar(View v)
	{
		ImageView imgLeft = (ImageView) v.findViewById(R.id.imgLeft);
		imgLeft.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				((MainActivity) getActivity()).showMenu();
			}
		});

		ImageView imgRight = (ImageView) v.findViewById(R.id.imgRight);
		imgRight.setImageResource(R.drawable.btn_done);
		imgRight.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				if (txtEmail.getText().toString().contentEquals(""))
				{
					AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your information is incomplete");
					return;
				}
				
				if (!AppManager.getInstanse().isValidEmail(txtEmail.getText()))
				{
					AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your email address is not valid");
					return;
				}
				
				if (!txtRePass.getText().toString().contentEquals(txtPass.getText().toString()) && txtPass.getText().length() > 0)
				{
					AppManager.getInstanse().showAlert(getActivity(), "Warning!", "These passwords don't match.");
					return;
				}
				
				viewLoader.setVisibility(View.VISIBLE);	
				AppManager.getInstanse().hideKeyboard(txtFullName);
				AppManager.getInstanse().hideKeyboard(txtEmail);
				AppManager.getInstanse().hideKeyboard(txtPass);
				AppManager.getInstanse().hideKeyboard(txtRePass);
				AppManager.getInstanse().hideKeyboard(txtLocation);
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new SaveTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} 
				else
				{
					new SaveTask().execute();
				}
			}
		});
	}

	@Override
	public void onDestroy()
	{
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		super.onDestroy();
	}

	class LogoutTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			return client.logout();
		}
	}
	
	class SaveTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			client.changeUserInfo(txtFullName.getText().toString(), txtLocation.getText().toString());
			
			if (txtPass.getText().toString().length() > 0)
			{
				try {
					client.changeUserPass(txtPass.getText().toString(), txtRePass.getText().toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (bmpImage != null)
			{
		        ByteArrayOutputStream bos = new ByteArrayOutputStream();
		        bmpImage.compress(Bitmap.CompressFormat.PNG, 100, bos);
		        byte[] imageData = bos.toByteArray();
		        client.changeUserAvatar(imageData);
			}
			
			client.loadUserInfo();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) 
		{
			super.onPostExecute(result);
			viewLoader.setVisibility(View.GONE);
			((MainActivity) getActivity()).loggedIn(false);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK)
		{
			if (fromCamera && isDoCrop)
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
			doCrop();
			break;
		case PICK_FROM_FILE:
			fromCamera = false;
			mImageCaptureUri = data.getData();
			doCrop();
			break;
		case CROP_FROM_CAMERA:
			Bundle extras = data.getExtras();
			if (extras != null)
			{
				bmpImage = extras.getParcelable("data");
				int newSize = imgProfile.getWidth();
				if (newSize < 200)
				{
					newSize = 200;
				}
		        bmpImage = Bitmap.createScaledBitmap(bmpImage, newSize, newSize, true);
				imgProfile.setImageBitmap(bmpImage);
			}

			if (fromCamera)
			{
				File f = new File(mImageCaptureUri.getPath());
				if (f.exists())
				{
					f.delete();
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void doCrop()
	{
		isDoCrop = true;
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getActivity().getPackageManager().queryIntentActivities( intent, 0 );

		int size = list.size();

		if (size == 0)
		{
			Toast.makeText(getActivity(), "Can not find image crop app", Toast.LENGTH_SHORT).show();

			return;
		}
		else
		{
			intent.setData(mImageCaptureUri);

			intent.putExtra("outputX", 200);
			intent.putExtra("outputY", 200);
			intent.putExtra("aspectX", 1);
			intent.putExtra("aspectY", 1);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", true);

			if (size == 1) {
				Intent i 		= new Intent(intent);
				ResolveInfo res	= list.get(0);

				i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title 	= getActivity().getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
					co.icon		= getActivity().getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
					co.appIntent= new Intent(intent);

					co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(getActivity().getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("Choose Crop App");
				builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
					public void onClick( DialogInterface dialog, int item ) {
						startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
					}
				});

				builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel( DialogInterface dialog ) {
						if (fromCamera)
						{
							File f = new File(mImageCaptureUri.getPath());
							if (f.exists())
							{
								f.delete();
							}
						}
					}
				} );

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}
}
