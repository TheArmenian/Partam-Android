package com.partam.partam;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.partam.partam.customclasses.FB;
import com.partam.partam.customclasses.FbImage;
import com.partam.partam.customclasses.FbImage.FbImageListener;
import com.partam.partam.customclasses.PartamHttpClient;

@SuppressLint("NewApi")
public class LoginFragment extends Fragment
{
	View viewSlide;
	
	View btnBack;
	View btnRegiter;
	View btnSignIn;
	View btnFacebook;

	View relFB;
	View btnContinue;

	View relSignIn;
	EditText txtEmail;
	EditText txtPass;
	View btnLogIn;
	View btnForgotPass;

	View relForgotPass;
	EditText txtForgotPassEmail;
	View btnSend;

	Animation slideUp;
	Animation slideDown;
	
	View viewLoader;
	
	FB fb;
	
	boolean isFavorite;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View v = inflater.inflate(R.layout.fragment_login, null);

		fb = new FB(getActivity());
		
		viewSlide = v.findViewById(R.id.viewSlide);
		
		btnBack = v.findViewById(R.id.btnBack);
		btnRegiter = v.findViewById(R.id.btnRegiter);
		btnSignIn = v.findViewById(R.id.btnSignIn);
		btnFacebook = v.findViewById(R.id.btnFacebook);

		relFB = v.findViewById(R.id.relFB);
		btnContinue = v.findViewById(R.id.btnContinue);

		relSignIn = v.findViewById(R.id.relSignIn);
		txtEmail = (EditText) v.findViewById(R.id.txtEmail);
		txtPass = (EditText) v.findViewById(R.id.txtPass);
		btnLogIn = v.findViewById(R.id.btnLogIn);
		btnForgotPass = v.findViewById(R.id.btnForgotPass);

		relForgotPass = v.findViewById(R.id.relForgotPass);
		txtForgotPassEmail = (EditText) v.findViewById(R.id.txtForgotPassEmail);
		btnSend = v.findViewById(R.id.btnSend);

		btnBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				((MainActivity) getActivity()).removeFragment(LoginFragment.this);
			}
		});

		btnFacebook.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				btnFacebook.setSelected(!btnFacebook.isSelected());
				
				if (btnSignIn.isSelected())
				{
					btnFacebook.setBackgroundResource(R.drawable.btn_fb_selected);
					btnSignIn.setSelected(false);
					btnSignIn.setBackgroundResource(R.drawable.btn_sign_in);
					viewSlide.startAnimation(slideDown);
					return;
				}
				
				if (btnFacebook.isSelected())
				{	
					btnFacebook.setBackgroundResource(R.drawable.btn_fb_selected);
					viewSlide.startAnimation(slideUp);
				}
				else
				{
					btnFacebook.setBackgroundResource(R.drawable.btn_fb);
					viewSlide.startAnimation(slideDown);
				}
			}
		});
		
		btnSignIn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				btnSignIn.setSelected(!btnSignIn.isSelected());	
				
				if (btnFacebook.isSelected())
				{
					btnSignIn.setBackgroundResource(R.drawable.btn_sign_in_selected);
					btnFacebook.setSelected(false);
					btnFacebook.setBackgroundResource(R.drawable.btn_fb);
					viewSlide.startAnimation(slideDown);
					return;
				}
				
				if (btnSignIn.isSelected())
				{	
					btnSignIn.setBackgroundResource(R.drawable.btn_sign_in_selected);
					viewSlide.startAnimation(slideUp);
				}
				else
				{
					btnSignIn.setBackgroundResource(R.drawable.btn_sign_in);
					viewSlide.startAnimation(slideDown);
				}
			}
		});
		
		btnRegiter.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				RegisterFragment frag = new RegisterFragment();
				frag.isFavorite = isFavorite;
				((MainActivity) getActivity()).addFragment(frag, true);
			}
		});

		btnContinue.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				viewLoader.setVisibility(View.VISIBLE);
				fb.getProfileInfo(new GraphUserCallback() 
				{
					@Override
					public void onCompleted(GraphUser user, Response response)  
					{
						Log.e("Info", "onCompleted");
						if (user != null) 
						{
							Log.e("Info", "user != null");
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
							{
								new FBLoginTask(user.getInnerJSONObject(), com.facebook.Session.getActiveSession().getAccessToken()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
							} 
							else
							{
								new FBLoginTask(user.getInnerJSONObject(), com.facebook.Session.getActiveSession().getAccessToken()).execute();
							}
						}
						else
						{
							Log.e("Info", "user === null");
							viewLoader.setVisibility(View.GONE); 
						}
					}
				});
			}
		});

		btnLogIn.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if (txtEmail.getText().length() < 4)
				{
					AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your information is incomplete");
					return;
				}

				if (!AppManager.getInstanse().isValidEmail(txtEmail.getText()))
				{
					AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your email address is not valid");
					return;
				}
				AppManager.getInstanse().hideKeyboard(txtEmail);
				AppManager.getInstanse().hideKeyboard(txtPass);
				viewLoader.setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new LoginTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} 
				else
				{
					new LoginTask().execute();
				}
			}
		});

		btnForgotPass.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				btnForgotPass.setSelected(true);
				viewSlide.startAnimation(slideDown);
			}
		});

		btnSend.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				if (txtForgotPassEmail.getText().length() < 4)
				{
					AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your information is incomplete");
					return;
				}

				if (!AppManager.getInstanse().isValidEmail(txtForgotPassEmail.getText()))
				{
					AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your email address is not valid");
					return;
				}
				AppManager.getInstanse().hideKeyboard(txtForgotPassEmail);
				viewLoader.setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
				{
					new ForgotPassTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				} 
				else
				{
					new ForgotPassTask().execute();
				}
			}
		});

		slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);
		slideDown = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down);
		slideUp.setAnimationListener(slideUpListener);
		slideDown.setAnimationListener(slideDownListener);

		viewLoader = v.findViewById(R.id.viewLoader);
		viewLoader.setVisibility(View.GONE);
		
		PackageInfo info;
		try {
			info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName().toString(),  PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures)
		    {
		        MessageDigest md = MessageDigest.getInstance("SHA");
		        md.update(signature.toByteArray());
		        Log.d("Info", "hash code = " +Base64.encodeToString(md.digest(), Base64.DEFAULT));
		    }
		} catch (NameNotFoundException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		
		return v;
	}

	AnimationListener slideUpListener = new AnimationListener() 
	{
		@Override
		public void onAnimationStart(Animation animation) 
		{
			if (btnFacebook.isSelected())
			{
				relFB.setVisibility(View.VISIBLE);
				relSignIn.setVisibility(View.INVISIBLE);
				relForgotPass.setVisibility(View.INVISIBLE);
			}
			else if(btnSignIn.isSelected())
			{
				relFB.setVisibility(View.INVISIBLE);
				relSignIn.setVisibility(View.INVISIBLE);
				relForgotPass.setVisibility(View.INVISIBLE);
				
				if (btnForgotPass.isSelected())
				{
					btnForgotPass.setSelected(false);
					relForgotPass.setVisibility(View.VISIBLE);
				}
				else
				{
					relSignIn.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) { }

		@Override
		public void onAnimationEnd(Animation animation) { }
	};

	AnimationListener slideDownListener = new AnimationListener() 
	{
		@Override
		public void onAnimationStart(Animation animation) { }

		@Override
		public void onAnimationRepeat(Animation animation) { }

		@Override
		public void onAnimationEnd(Animation animation)
		{
			relFB.setVisibility(View.INVISIBLE);
			relSignIn.setVisibility(View.INVISIBLE);
			relForgotPass.setVisibility(View.INVISIBLE);
			
			if (btnFacebook.isSelected() || btnSignIn.isSelected())
			{
				viewSlide.startAnimation(slideUp);
			}
		}
	};
	
	class LoginTask extends AsyncTask<Void, Void, Boolean> 
	{
		JSONObject loginInfo;
		
		@Override
		protected Boolean doInBackground(Void... params) 
		{
			loginInfo = new JSONObject();
			PartamHttpClient client = PartamHttpClient.getInstance();
			try {
				loginInfo = client.login(txtEmail.getText().toString(), txtPass.getText().toString());
			} catch (final Exception e) {
				e.printStackTrace();
				getActivity().runOnUiThread(new Runnable() 
				{
					@Override
					public void run()
					{
						AppManager.getInstanse().showAlert(getActivity(), "ERROR!", e.getLocalizedMessage());
					}
				});
				return false;
			}

			AppManager.getInstanse().token = AppManager.getJsonString(loginInfo, "token");
			if (AppManager.getInstanse().token.length()  < 6)//false
			{
				return false;
			}
	        AppManager.getInstanse().isLogOut = false;
	        client.loadUserInfo();
	        
			return true; 
		}
		
		@Override
		protected void onPostExecute(Boolean success)
		{
			super.onPostExecute(success);
			viewLoader.setVisibility(View.GONE);
			
			if (!success)
			{
				AppManager.getInstanse().showAlert(getActivity(), "ERROR!", "Invalid username or password");
			}
			else
			{
				((MainActivity) getActivity()).closeFragments();
				((MainActivity) getActivity()).loggedIn(isFavorite);
			}
		}
	}
	
	class ForgotPassTask extends AsyncTask<Void, Void, Void> 
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			try {
				client.sendForgottPassRequest(txtForgotPassEmail.getText().toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} 
		
		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
			viewLoader.setVisibility(View.GONE);
			((MainActivity) getActivity()).closeFragments();
		}
	}
	
	class FBLoginTask extends AsyncTask<Void, Void, Boolean>
	{
		JSONObject user;
		String fbAccessToken;
		JSONObject loginInfo;
		boolean regFB = false;
		
		public FBLoginTask(JSONObject user, String fbAccessToken) 
		{
			this.user = user;
			this.fbAccessToken = fbAccessToken;
		}
		
		@Override
		protected Boolean doInBackground(Void... params) 
		{
			Log.e("Info", "ddddddddddddddd");
			loginInfo = new JSONObject();
			PartamHttpClient client = PartamHttpClient.getInstance();
			try {
				loginInfo = client.sendFBLoginRequest(fbAccessToken);
			} catch (final Exception e) {
				e.printStackTrace();
				getActivity().runOnUiThread(new Runnable() 
				{
					@Override
					public void run()
					{
						AppManager.getInstanse().showAlert(getActivity(), "ERROR!", e.getLocalizedMessage());
					}
				});
				return false;
			}

			AppManager.getInstanse().token = AppManager.getJsonString(loginInfo, "token"); 
			if (AppManager.getInstanse().token.length()  < 6)//false
			{
				Log.e("Info", "token <6");
				regFB = true;
				
				try {
					loginInfo = client.sendFBRegisterRequest(user);
				} catch (final Exception e) {
					e.printStackTrace();
					getActivity().runOnUiThread(new Runnable() 
					{
						@Override
						public void run()
						{
							AppManager.getInstanse().showAlert(getActivity(), "ERROR!", e.getLocalizedMessage());
						}
					});
					return false;
				}
				
				AppManager.getInstanse().token = AppManager.getJsonString(loginInfo, "token");
				if (AppManager.getInstanse().token.length()  < 6)//false
				{
					return false;
				}
				
		        AppManager.getInstanse().isLogOut = false;
		        client.loadUserInfo();
				
				return true;
			}
			else
			{
				Log.e("Info", "token > 6");
				AppManager.getInstanse().isLogOut = false;
		        client.loadUserInfo();
				return true;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean success)
		{
			super.onPostExecute(success);
			
			if (success)
			{
				if (!regFB)
				{
					if (getActivity() != null)
					{
						viewLoader.setVisibility(View.GONE);
						((MainActivity) getActivity()).closeFragments();
						((MainActivity) getActivity()).loggedIn(isFavorite);
					}
				}
				else
				{
					String imgUrl = "http://graph.facebook.com/" + AppManager.getJsonString(user, "id") + "/picture?redirect=false&height=200&type=normal&width=200";
					FbImage fbImage = new FbImage(imgUrl);
					fbImage.setOnFbImageEventListener(new FbImageListener()
					{
						@Override
						public void donwloadComlete()
						{
							viewLoader.setVisibility(View.GONE);
							((MainActivity) getActivity()).closeFragments();
							((MainActivity) getActivity()).loggedIn(isFavorite);
						}
					});
					
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
					{
						fbImage.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					} 
					else
					{
						fbImage.execute();
					}
				}
			}
			else
			{
				viewLoader.setVisibility(View.GONE);
			}
		}
	}
}
