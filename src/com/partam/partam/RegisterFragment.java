package com.partam.partam;

import org.json.JSONObject;

import com.partam.partam.customclasses.PartamHttpClient;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

public class RegisterFragment extends Fragment
{
	EditText txtFullName;
	EditText txtEmail;
	EditText txtPass;
	EditText txtRePass;
	
	View viewLoader;
	
	boolean isFavorite;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		View v = inflater.inflate(R.layout.fragment_register, null);
		
		v.findViewById(R.id.btnBack).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				((MainActivity) getActivity()).removeFragment(RegisterFragment.this);
			}
		});

		txtFullName = (EditText) v.findViewById(R.id.txtFullName);
		txtEmail = (EditText) v.findViewById(R.id.txtEmail);
		txtPass = (EditText) v.findViewById(R.id.txtPass);
		txtRePass = (EditText) v.findViewById(R.id.txtRePass);
		
		v.findViewById(R.id.btnLetsGo).setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				reg();
			}
		});
		
		viewLoader = v.findViewById(R.id.viewLoader);
		viewLoader.setVisibility(View.GONE);
		
		return v;
	}

	@Override
	public void onDestroy()
	{
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		super.onDestroy();
	}
	
	@SuppressLint("NewApi")
	private void reg()
	{
		if (txtRePass.getText().length() < 4 || txtPass.getText().length() < 4 || txtEmail.getText().toString().contentEquals("") || txtFullName.getText().toString().contentEquals(""))
		{
			AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your information is incomplete");
			return;
		}
		
		if (!AppManager.getInstanse().isValidEmail(txtEmail.getText()))
		{
			AppManager.getInstanse().showAlert(getActivity(), "Warning!", "Your email address is not valid");
			return;
		}
		
		if (!txtRePass.getText().toString().contentEquals(txtPass.getText().toString()))
		{
			AppManager.getInstanse().showAlert(getActivity(), "Warning!", "These passwords don't match.");
			return;
		}
		
		viewLoader.setVisibility(View.VISIBLE);
		AppManager.getInstanse().hideKeyboard(txtFullName);
		AppManager.getInstanse().hideKeyboard(txtEmail);
		AppManager.getInstanse().hideKeyboard(txtPass);
		AppManager.getInstanse().hideKeyboard(txtRePass);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new RegisterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new RegisterTask().execute();
		}
		
	}
	
	class RegisterTask extends AsyncTask<Void, Void, Boolean>
	{
		JSONObject regInfo;
		
		@Override
		protected Boolean doInBackground(Void... params) 
		{
			regInfo = new JSONObject();
			PartamHttpClient client = PartamHttpClient.getInstance();
			try {
				regInfo = client.register(txtFullName.getText().toString(), txtEmail.getText().toString(), txtPass.getText().toString());
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

			AppManager.getInstanse().token = AppManager.getJsonString(regInfo, "token");
	        AppManager.getInstanse().isLogOut = false;
	        client.loadUserInfo();
	        
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean success)
		{
			super.onPostExecute(success);
			viewLoader.setVisibility(View.GONE);
			
			if (success)
			{
				if (!AppManager.getJsonBoolean(regInfo, "success"))
				{
					AppManager.getInstanse().showAlert(getActivity(), "ERROR!", "The email is already used");
				}
				else
				{
					((MainActivity) getActivity()).closeFragments();
					((MainActivity) getActivity()).loggedIn(isFavorite);
				}
			}
		}
	}
}
