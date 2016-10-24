package com.partam.partam;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;

import com.NarKira.slidemenulibrary.SlidingMenu;
import com.NarKira.slidemenulibrary.app.SlidingFragmentActivity;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAdListener;
import com.flurry.android.FlurryAdSize;
import com.flurry.android.FlurryAdType;
import com.flurry.android.FlurryAds;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.pinterest.pinit.PinItButton;

@SuppressLint("NewApi")
public class MainActivity extends SlidingFragmentActivity implements FlurryAdListener
{
	FrameLayout banner;

	MenuFragment menuFragment;
	MainFragment mainFragment;
	Fragment firstFragment;
	Fragment secondFragment;
	DetailFragment detailFragment;
    public static final String CLIENT_ID = "1440219";

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		AppManager.getInstanse(getApplicationContext());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        AppManager.getInstanse().screenWidth = metrics.widthPixels;
		setContentView(R.layout.activity_main);

        PinItButton.setPartnerId(CLIENT_ID);

		banner = (FrameLayout) findViewById(R.id.banner);

		setBehindContentView(R.layout.menu_frame);
		menuFragment = new MenuFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.menu_frame, menuFragment).commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		sm.setBehindScrollScale(0);

		mainFragment = new MainFragment();
		getSupportFragmentManager().beginTransaction().replace(R.id.mainContainer, mainFragment).commit();


		FlurryAgent.onStartSession(this, "Z6JHXGC2TB8BRHQCPBP6");
		FlurryAds.fetchAd(this, "partam_fullscreen_android", banner, FlurryAdSize.FULLSCREEN);		
		FlurryAgent.setLogEnabled(true);
		FlurryAgent.setLogLevel(Log.DEBUG);
		FlurryAds.setAdListener(this);

		//		// Add code to print out the key hash
		//		try {
		//			PackageInfo info = getPackageManager().getPackageInfo("com.partam.partam", PackageManager.GET_SIGNATURES);
		//			for (Signature signature : info.signatures) 
		//			{
		//				MessageDigest md = MessageDigest.getInstance("SHA");
		//				md.update(signature.toByteArray());
		//				Log.e("Info", Base64.encodeToString(md.digest(), Base64.DEFAULT));
		//				Toast.makeText(getApplicationContext(), "key = " + Base64.encodeToString(md.digest(), Base64.DEFAULT), Toast.LENGTH_LONG).show();
		//			}
		//		} catch (NameNotFoundException e) {
		//
		//		} catch (NoSuchAlgorithmException e) {
		//
		//		}

		
		SharedPreferences sPref = getSharedPreferences("Partam", MODE_PRIVATE);
		if (sPref.getBoolean("isNew", false))
		{
			String message = sPref.getString("message", "");		
			AppManager.getInstanse().showAlert(this, "Notification", message);
	    	Editor edit = sPref.edit();
	    	edit.putBoolean("isNew", false);
	    	edit.commit();
		}
		
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		registerReceiver(mHandleMessageReceiver, new IntentFilter(CommonUtilities.DISPLAY_MESSAGE_ACTION)); 
		// Get GCM registration id
		String regId = GCMRegistrar.getRegistrationId(this);
		// Check if regid already presents
		Log.d("myLogs", "regId = " + regId);
		if (regId.equals("")) 
		{
			// Registration is not present, register now with GCM		
			GCMRegistrar.register(this, CommonUtilities.SENDER_ID);
		}
		else 
		{
			// Device is already registered on GCM
			if (GCMRegistrar.isRegisteredOnServer(this))
			{
				// Skips registration.	
			}
			else 
			{
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new GCMRegTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, regId);
				} 
				else
				{
					new GCMRegTask().execute(regId);
				}
			}
		}

	}

	/**
	 * Receiving push messages
	 * */
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String newMessage = intent.getExtras().getString(CommonUtilities.EXTRA_MESSAGE);
			// Waking up mobile if it is sleeping
			WakeLocker.acquire(getApplicationContext());

			/**
			 * Take appropriate action on this message
			 * depending upon your app requirement
			 * For now i am just displaying it on the screen
			 * */

			// Showing received message
			AppManager.getInstanse().showAlert(MainActivity.this, "Notification", newMessage);

			// Releasing wake lock
			WakeLocker.release();
		}
	};

	class GCMRegTask extends AsyncTask<String, Void, Void>
	{

		@Override
		protected Void doInBackground(String... params) 
		{
			// Register on our server
			// On server creates a new user
			ServerUtilities.register(MainActivity.this, params[0]);
			return null;
		}
		
	}
	
	@Override
	protected void onDestroy() 
	{
		try {
			unregisterReceiver(mHandleMessageReceiver);
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
		super.onDestroy();
	}

	@Override
	protected void onStop() 
	{
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	public void loggedIn(boolean isFavorite)
	{
		menuFragment.loggedIn(isFavorite);
		if (detailFragment != null) {
			if (detailFragment.getActivity() != null) 
			{
				detailFragment.loggedIn();
			}
			else
			{
				detailFragment = null;
			}
		}
	}

	public void logOut()
	{
		menuFragment.logout();
		openMainFragment();
	}

	public MainFragment getMainFragment()
	{
		return mainFragment;
	}

	public void addFragment(Fragment fragment, boolean add)
	{
		showContent();
		if (firstFragment == null)
		{
			firstFragment = fragment;
			AppManager.getInstanse().openFragment(this, firstFragment, R.id.container);
			return;
		}

		if (add) 
		{
			secondFragment = fragment;
			AppManager.getInstanse().openFragment(this, secondFragment, R.id.container);
		}
		else
		{
			closeFragments();
			firstFragment = fragment;
			AppManager.getInstanse().openFragment(this, firstFragment, R.id.container);
		}
	}

	public void removeFragment(Fragment fragment)
	{
		if (secondFragment != null && secondFragment.equals(fragment))
		{
			secondFragment = null;
		}
		if (firstFragment != null && firstFragment.equals(fragment))
		{
			firstFragment = null;
		}
		onBackPressed();
	}

	public void openMainFragment()
	{
		showContent();
		closeDetailFragment();
		closeFragments();
	}

	public void openDetailFragment(DetailFragment frag)
	{
		detailFragment = frag;
		AppManager.getInstanse().openFragment(this, detailFragment, R.id.container);
	}

	public void closeDetailFragment()
	{
		if (detailFragment != null) {
			if (detailFragment.getActivity() != null) {
				onBackPressed();
			}
			detailFragment = null;
		}
	}

	public void closeFragments()
	{
		if (secondFragment != null)
		{
			if (secondFragment.getActivity() != null) {
				onBackPressed();
			}
			secondFragment = null;
		}
		if (firstFragment != null)
		{
			if (firstFragment.getActivity() != null) {
				onBackPressed();
			}
			firstFragment = null;
		}
	}

	public void setEnableMenu(boolean enabled)
	{
		SlidingMenu sm = getSlidingMenu();
		if (enabled)
		{
			sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		}
		else
		{
			sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		com.facebook.AppEventsLogger.activateApp(this, getResources().getString(R.string.app_id));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (com.facebook.Session.getActiveSession() != null) 
		{
			com.facebook.Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
		}
	}

	@Override
	public void onAdClicked(String arg0)
	{
		Log.d("myLogs", "onAdClicked( " + arg0 + " )");
	}

	@Override
	public void onAdClosed(String arg0)
	{
		Log.d("myLogs", "onAdClosed( " + arg0 + " )");
	}

	@Override
	public void onAdOpened(String arg0) 
	{
		Log.d("myLogs", "onAdOpened( " + arg0 + " )");
	}

	@Override
	public void onApplicationExit(String arg0) 
	{
		Log.d("myLogs", "onApplicationExit( " + arg0 + " )");
	}

	@Override
	public void onRenderFailed(String arg0) 
	{
		Log.d("myLogs", "onRenderFailed( " + arg0 + " )");
	}

	@Override
	public void onVideoCompleted(String arg0) 
	{
		Log.d("myLogs", "onVideoCompleted( " + arg0 + " )");
	}

	@Override
	public boolean shouldDisplayAd(String arg0, FlurryAdType arg1)
	{
		Log.d("myLogs", "shouldDisplayAd( " + arg0 + ", " + arg1 + " )");
		return true;
	}

	@Override
	public void onRendered(String arg0) 
	{
		Log.d("myLogs", "onRendered( " + arg0 + " )");
	}

	@Override
	public void spaceDidFailToReceiveAd(String arg0) 
	{
		Log.d("myLogs", "spaceDidFailToReceiveAd(" + arg0 + ")");
	}


	@Override
	public void spaceDidReceiveAd(String arg0) 
	{
		FlurryAds.displayAd(this, "partam_fullscreen_android", banner);
	}
}
