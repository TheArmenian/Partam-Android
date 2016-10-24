package com.partam.partam.customclasses;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.partam.partam.R;
import com.partam.partam.image_loader.FileCache;
import com.partam.partam.image_loader.ImageLoader;

public class TwitterActivity extends Activity 
{
	// Constants
	/**
	 * Register your here app https://dev.twitter.com/apps/new and get your
	 * consumer key and secret
	 * */
	static String TWITTER_CONSUMER_KEY = "pTLFnzyueLN1vFfyuiOM131Wi";
	static String TWITTER_CONSUMER_SECRET = "qwEu35xpMOkGl76Xufu8UkBEuZASpoEXKdrHFR2DiJxGCQlZBe";

	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	// Progress dialog
	ProgressDialog pDialog;

	// Twitter
	private static Twitter twitter;
	private static RequestToken requestToken;

	// Shared Preferences
	private static SharedPreferences mSharedPreferences;

	// Internet Connection detector
	private ConnectionDetector cd;

	// Alert Dialog Manager
	AlertDialogManager alert = new AlertDialogManager();

	Button btnUpdateStatus;
	EditText txtUpdate;
	ImageView imgTwitter;
	
	public static String imgUrl;
	public static String tweetText;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_activity);

		cd = new ConnectionDetector(getApplicationContext());

		// Check if Internet present
		if (!cd.isConnectingToInternet()) 
		{
			// Internet Connection is not present
			alert.showAlertDialog(TwitterActivity.this, "Internet Connection Error", "Please connect to working Internet connection", false);
			// stop executing code by return
			return;
		}

		// Check if twitter keys are set
		if(TWITTER_CONSUMER_KEY.trim().length() == 0 || TWITTER_CONSUMER_SECRET.trim().length() == 0)
		{
			// Internet Connection is not present
			alert.showAlertDialog(TwitterActivity.this, "Twitter oAuth tokens", "Please set your twitter oauth tokens first!", false);
			// stop executing code by return
			return;
		}

		// All UI elements
		btnUpdateStatus = (Button) findViewById(R.id.btnUpdateStatus);
		txtUpdate = (EditText) findViewById(R.id.txtUpdateStatus);
		imgTwitter = (ImageView) findViewById(R.id.imgTwitter);
		
		txtUpdate.setText(tweetText);
		new ImageLoader().DisplayImage(imgUrl, imgTwitter);
		
		// Shared Preferences
		mSharedPreferences = getApplicationContext().getSharedPreferences("TwitterPref", 0);


		btnUpdateStatus.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				String status = txtUpdate.getText().toString();
				new updateTwitterStatus().execute(status);
			}
		});

		if (!isTwitterLoggedInAlready())
		{
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL))
			{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new LoggedInTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
				} 
				else
				{
					new LoggedInTask().execute(uri);
				}
				return;
			}
		}
		else
		{
			// Show Update Twitter
			txtUpdate.setVisibility(View.VISIBLE);
			btnUpdateStatus.setVisibility(View.VISIBLE);
			imgTwitter.setVisibility(View.VISIBLE);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new LoginTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new LoginTask().execute();
		}
		//        
		//        if (isTwitterLoggedInAlready()) {
		//        	new updateTwitterStatus().execute();
		//		}

	}

	class LoginTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();      
			pDialog = new ProgressDialog(TwitterActivity.this);
			pDialog.setMessage("Please Wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params)
		{
			loginToTwitter();
			return null;
		}

		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
			pDialog.dismiss();
		}
	}

	class LoggedInTask extends AsyncTask<Uri, Void, AccessToken>
	{
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();      
			pDialog = new ProgressDialog(TwitterActivity.this);
			pDialog.setMessage("Please Wait...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		@Override
		protected AccessToken doInBackground(Uri... params)
		{
			Uri uri = params[0];
			// oAuth verifier
			String verifier = uri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
			// Get the access token
			AccessToken accessToken = null;
			try
			{
				accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
				// Shared Preferences
				Editor e = mSharedPreferences.edit();

				// After getting access token, access token secret
				// store them in application preferences
				e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
				e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
				// Store login status - true
				e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
				e.commit(); // save changes

				Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
			} catch (TwitterException e1)
			{
				e1.printStackTrace();
			}


			return null;
		}

		@Override
		protected void onPostExecute(AccessToken accessToken)
		{
			super.onPostExecute(accessToken);
			pDialog.dismiss();
			try {
				// Show Update Twitter
				txtUpdate.setVisibility(View.VISIBLE);
				btnUpdateStatus.setVisibility(View.VISIBLE);
				imgTwitter.setVisibility(View.VISIBLE);

				// Getting user details from twitter
//				// For now i am getting his name only
//				long userID = accessToken.getUserId();
//				User user = twitter.showUser(userID);
//				String username = user.getName();
//				// Displaying in xml ui
//				lblUserName.setText(Html.fromHtml("<b>Welcome " + username + "</b>"));
			} 
			catch (Exception ex) 
			{
				// Check log for login errors
				Log.e("Info", "> " + ex.getMessage());
			}
		}
	}

	/**
	 * Function to login twitter
	 * */
	private void loginToTwitter() 
	{
		// Check if already logged in
		if (!isTwitterLoggedInAlready())
		{
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				Runtime.getRuntime().gc();
				System.gc();
				finish();
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
				browserIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				this.startActivity(browserIntent);
			} 
			catch (TwitterException e) 
			{
				e.printStackTrace();
			}
		} 
		else
		{
			// user already logged into twitter
//			Toast.makeText(getApplicationContext(), "Already Logged into twitter", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Function to update status
	 * */
	class updateTwitterStatus extends AsyncTask<String, String, String> 
	{
		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			pDialog = new ProgressDialog(TwitterActivity.this);
			pDialog.setMessage("Updating to twitter...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) 
		{
			String message = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);
				String appLink = "\nhttps://play.google.com/store/apps/details?id=com.partam.partam";
				int messageLength = 140 - appLink.length();
				if (message.length() > messageLength)
				{
					message = message.substring(0, messageLength);
					message += appLink;
				}
				else
				{
					message += appLink;
				}
				StatusUpdate status = new StatusUpdate(message);
				status.setMedia(new FileCache().getFile(imgUrl));
				twitter.updateStatus(status);
			}
			catch(TwitterException e)
			{
				Log.d("Info", "Pic Upload error" + e.getErrorMessage());
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) 
		{
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			Toast.makeText(getApplicationContext(), "Status tweeted successfully", Toast.LENGTH_SHORT).show();
			finish();
		}

	}

	/**
	 * Function to logout from twitter
	 * It will just clear the application shared preferences
	 * */
	//    private void logoutFromTwitter() {
	//        // Clear the shared preferences
	//        Editor e = mSharedPreferences.edit();
	//        e.remove(PREF_KEY_OAUTH_TOKEN);
	//        e.remove(PREF_KEY_OAUTH_SECRET);
	//        e.remove(PREF_KEY_TWITTER_LOGIN);
	//        e.commit();
	//    }

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	private boolean isTwitterLoggedInAlready() 
	{
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}
}