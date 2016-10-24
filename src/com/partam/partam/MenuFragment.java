package com.partam.partam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.partam.partam.customclasses.CategoriesAdapter;
import com.partam.partam.customclasses.CitiesAdapter;
import com.partam.partam.customclasses.PartamHttpClient;
import com.partam.partam.customclasses.WebImage;

@SuppressLint("NewApi")
public class MenuFragment extends Fragment
{
	MainActivity act;

	EditText txtSearch;
	
	ImageView btnSelectCity;
	ImageView btnSelectCategory;

	ListView listCity;
	ListView listCategory;
	CitiesAdapter adapterCity;
	CategoriesAdapter adapterCategory;

	ArrayList<JSONObject> locations;
	ArrayList<JSONObject> categories; 
	ArrayList<JSONObject> selectedCategories = new ArrayList<JSONObject>();
	JSONObject selectedCity;
	
	View btnLogin;
	View layProfile;
	View btnSignOut;
	TextView txtUserName;
	ImageView imgUser;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new LoadCitiesAndCategories().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new LoadCitiesAndCategories().execute();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View v = inflater.inflate(R.layout.fragment_menu, null);
		act = (MainActivity) getActivity();
		
		txtSearch = (EditText) v.findViewById(R.id.txtSearch);
		txtSearch.setOnEditorActionListener(new OnEditorActionListener() 
		{
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) 
			{
				if (actionId == EditorInfo.IME_ACTION_SEARCH)
				{
					act.getMainFragment().doSearch(txtSearch.getText().toString());
					AppManager.getInstanse().hideKeyboard(txtSearch);
					act.openMainFragment();
					return true;
				}
				return false;
			}
		});
		
		
		v.findViewById(R.id.btnLogo).setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				act.openMainFragment();
			}
		});
		
		v.findViewById(R.id.layHome).setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				act.openMainFragment();
			}
		});
		
		btnLogin = v.findViewById(R.id.btnLogin);
		btnLogin.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				LoginFragment fragment = new LoginFragment();
				act.addFragment(fragment, false);
			}
		});
		
		layProfile = v.findViewById(R.id.layProfile);
		layProfile.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				ProfileFragment fragment = new ProfileFragment();
				act.addFragment(fragment, false);
			}
		});
		
		btnSignOut = v.findViewById(R.id.btnSignOut);
		btnSignOut.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				act.closeDetailFragment();
				logout();
				btnLogin.performClick(); 
			}
		});

		txtUserName = (TextView) v.findViewById(R.id.txtUserName);
		imgUser = (ImageView) v.findViewById(R.id.imgUser);
		
		v.findViewById(R.id.btnFavorites).setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if (AppManager.getInstanse().isLogOut)
				{			
					LoginFragment fragment = new LoginFragment();
					fragment.isFavorite = true;
					act.addFragment(fragment, false);
					return;
				}
				
				FavoriteFragment frag = new FavoriteFragment();
				act.addFragment(frag, false);
			}
		});
		
		v.findViewById(R.id.btnAboutApp).setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				AboutFragment fragment = new AboutFragment();
				act.addFragment(fragment, false);
			}
		});
		
		btnSelectCity = (ImageView) v.findViewById(R.id.btnSelectCity);
		btnSelectCity.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				v.setSelected(!v.isSelected());
				
				if (v.isSelected())
				{
					btnSelectCategory.setSelected(false);
					listCity.setVisibility(View.VISIBLE);
					listCategory.setVisibility(View.GONE);
				}
				else
				{
					listCity.setVisibility(View.GONE);
				}
			}
		});
		
		btnSelectCategory = (ImageView) v.findViewById(R.id.btnSelectCategory);
		btnSelectCategory.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				v.setSelected(!v.isSelected());
				
				if (v.isSelected())
				{
					btnSelectCity.setSelected(false);
					listCategory.setVisibility(View.VISIBLE);
					listCity.setVisibility(View.GONE);
				}
				else
				{
					listCategory.setVisibility(View.GONE);
				}
			}
		});

		listCity = (ListView) v.findViewById(R.id.listCity);
		listCategory = (ListView) v.findViewById(R.id.listCategory);
		adapterCity = new CitiesAdapter(getActivity(), null, null);
		adapterCategory = new CategoriesAdapter(getActivity(), null);
		listCity.setAdapter(adapterCity);
		listCategory.setAdapter(adapterCategory);
		
		listCity.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if (adapterCity.itemSelected(position))
				{
					selectedCity = locations.get(position);
				}
				else
				{
					selectedCity = null;
				}
				sendRequest();
			}
		});
		
		listCategory.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if (adapterCategory.itemSelected(position))
				{
					selectedCategories.add(categories.get(position));
				}
				else
				{
					selectedCategories.remove(categories.get(position));
				}
				sendRequest();
			}
		});
		
		if (!AppManager.getInstanse().isLogOut)
		{
			loggedIn(false);
		}
		
		return v;
	}
	
	public void loggedIn(boolean isFavorite)
	{
		btnSignOut.setVisibility(View.VISIBLE);
		btnLogin.setVisibility(View.INVISIBLE);
		layProfile.setVisibility(View.VISIBLE);
		JSONObject userInfo = AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user");
		txtUserName.setText(AppManager.getJsonString(userInfo, "display_name"));
		String pictureUrl = AppManager.getJsonString(userInfo, "picture");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new WebImage(imgUser, pictureUrl).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR); 
		} 
		else
		{
			new WebImage(imgUser, pictureUrl).execute();
		}
		
		if (isFavorite) 
		{
			FavoriteFragment frag = new FavoriteFragment();
			act.addFragment(frag, false);
		}
	}
	
	public void logout()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new LogoutTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new LogoutTask().execute();
		}
		btnSignOut.setVisibility(View.GONE);
		btnLogin.setVisibility(View.VISIBLE);
		layProfile.setVisibility(View.GONE);
	} 
	
	void sendRequest()
	{
		MainFragment fragment = act.getMainFragment();
		act.showContent();
		if (selectedCity != null && selectedCategories.size() == 0)
		{
	        fragment.isCanLoaded = true;
	        fragment.selectedCitylimit = 0;
	        fragment.city = selectedCity;
	        fragment.isCanLoadedPointByCity = true;
	        fragment.isCanLoadedPointByCityAndCategory = false;
	        fragment.isCanLoadedPointByCategory = true;
		}
		else if (selectedCity != null && selectedCategories.size() > 0)
		{
			String categoriesID = AppManager.getJsonString(selectedCategories.get(0), "id");
			for (int i = 0; i < selectedCategories.size(); i++)
			{
				categoriesID += "," + AppManager.getJsonString(selectedCategories.get(i), "id");
			}
			
	        fragment.isCanLoaded = true;
	        fragment.city = selectedCity;
	        fragment.categoriresID = categoriesID;
	        fragment.selectedCityCategorylimit = 0;
	        fragment.isCanLoadedPointByCity = false;
	        fragment.isCanLoadedPointByCityAndCategory = true;
	        fragment.isCanLoadedPointByCategory = false;
		}
		else if (selectedCity == null && selectedCategories.size() > 0)
		{
			String categoriesID = AppManager.getJsonString(selectedCategories.get(0), "id");
			for (int i = 0; i < selectedCategories.size(); i++)
			{
				categoriesID += "," + AppManager.getJsonString(selectedCategories.get(i), "id");
			}
			
	        fragment.isCanLoaded = true;
	        fragment.city = selectedCity;
	        fragment.categoriresID = categoriesID;
	        fragment.selectedCategorylimit = 0;
	        fragment.isCanLoadedPointByCity = false;
	        fragment.isCanLoadedPointByCityAndCategory = false;
	        fragment.isCanLoadedPointByCategory = true;
		}
		else if (selectedCity == null && selectedCategories.size() == 0)
		{
	        fragment.limit = -10;
	        fragment.isCanLoaded = true;
	        fragment.isCanLoadedPointByCity = false;
	        fragment.isCanLoadedPointByCityAndCategory = false;
	        fragment.isCanLoadedPointByCategory = false;
		}
	    
	    fragment.arrInfo = new JSONArray();
	    fragment.viewLoader.setVisibility(View.VISIBLE);
	    fragment.sendLoadPointsRequest();
	}
	
	class LoadCitiesAndCategories extends AsyncTask<Void, Void, Void>
	{
		ArrayList<HashMap<String, String>> arrSection;
		
		@Override
		protected Void doInBackground(Void... params)
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			JSONObject obj = client.loadLocationsInfo("locations");
			JSONArray arr = AppManager.getJsonArray(obj, "countries");
			ArrayList<JSONObject> arrCountries = new ArrayList<>(arr.length());
			
			for (int i = 0; i < arr.length(); i++)
			{
				arrCountries.add(AppManager.getJsonObject(arr, i));
			}

			Collections.sort(arrCountries, new CountriesComparator());
			
			locations = new ArrayList<JSONObject>();
			arrSection = new ArrayList<HashMap<String, String>>(arr.length());
			for (int i = 0; i < arrCountries.size(); i++)
			{
				JSONObject country = arrCountries.get(i);
				
				HashMap<String, String> hm = new HashMap<String, String>(2);
				hm.put("country", AppManager.getJsonString(country, "name"));
				hm.put("section", locations.size()+"");
				arrSection.add(hm);
				
				JSONArray cities = AppManager.getJsonArray(country, "cities");
				ArrayList<JSONObject> tempArr = new ArrayList<JSONObject>();
				for (int j = 0; j < cities.length(); j++)
				{
					tempArr.add(AppManager.getJsonObject(cities, j));
				}
				Collections.sort(tempArr, new LocationsComparator());
				locations.addAll(tempArr);
			}
			
			arr = client.loadCategoriesInfo("categories");
			categories = new ArrayList<JSONObject>();
			for (int i = 0; i < arr.length(); i++)
			{
				categories.add(AppManager.getJsonObject(arr, i));
			}
			Collections.sort(categories, new CategoriesComparator());
			AppManager.getInstanse().categories = categories;
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			super.onPostExecute(result);
			adapterCity.reloadData(locations, arrSection);
			adapterCategory.reloadData(categories);
		}
	}
	
	class LocationsComparator implements Comparator<JSONObject>
	{
		@Override
		public int compare(JSONObject tr1, JSONObject tr2)
		{
			String ob1 = AppManager.getJsonString(tr1, "city");
			String ob2 = AppManager.getJsonString(tr2, "city");
			return ob1.compareTo(ob2);
		}
	}
	
	class CountriesComparator implements Comparator<JSONObject>
	{
		@Override
		public int compare(JSONObject tr1, JSONObject tr2)
		{
			String ob1 = AppManager.getJsonString(tr1, "name");
			String ob2 = AppManager.getJsonString(tr2, "name");
			return ob1.compareTo(ob2);
		}
	}
	
	class CategoriesComparator implements Comparator<JSONObject>
	{
		@Override
		public int compare(JSONObject tr1, JSONObject tr2)
		{
			String ob1 = AppManager.getJsonString(tr1, "title");
			String ob2 = AppManager.getJsonString(tr2, "title");
			return ob1.compareTo(ob2);
		}
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
}
