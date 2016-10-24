package com.partam.partam;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.partam.partam.customclasses.PartamHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class AppManager
{
    public boolean isLogOut;
    public boolean isFilter;
    public String token;

    public JSONObject userInfo;
    public JSONObject userFavorites;
    public JSONArray userLocalFavorites;
    
    public JSONArray allPoints;
    public JSONArray checkinsArray;
    
    public int screenWidth;

    public ArrayList<JSONObject> categories; 

    Context context;
    private static AppManager instanse = null;

    public static AppManager getInstanse() {
        return instanse;
    }

    public static AppManager getInstanse(Context context) {
        if (instanse == null) {
            instanse = new AppManager(context);
        }
        return instanse;
    }

    private AppManager(Context context)
    {
        this.context = context;
        File cacheDir = new File(getCachePath());
        if (!cacheDir.exists())
        {
            cacheDir.mkdirs();
        }
        else
        {
            File[] files = cacheDir.listFiles();
            for(File f:files)
            {
                f.delete();
            }
        }
        
        load();
    }

    public void save()
    {
        SharedPreferences sPref = context.getSharedPreferences("Strive", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean("isLogOut", isLogOut);
        ed.putString("token", token);
        ed.putString("userInfo", userInfo.toString());
        ed.putString("userFavorites", userFavorites.toString());
        ed.putString("userLocalFavorites", userLocalFavorites.toString());
        ed.commit();
    }

    @SuppressLint("NewApi")
	public void load()
    {
        SharedPreferences sPref = context.getSharedPreferences("Strive", Context.MODE_PRIVATE);
        isLogOut = sPref.getBoolean("isLogOut", true);
        token = sPref.getString("token", "");
        try {
			userInfo = new JSONObject(sPref.getString("userInfo", "{}"));
			userFavorites = new JSONObject(sPref.getString("userFavorites", "{}"));
			userLocalFavorites = new JSONArray(sPref.getString("userLocalFavorites", "[]"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
        isFilter = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
		{
			new LoadAllMapPoints().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		} 
		else
		{
			new LoadAllMapPoints().execute();
		}
    }

    class LoadAllMapPoints extends AsyncTask<Void, Void, Void>
    {
		@Override
		protected Void doInBackground(Void... params) 
		{
			PartamHttpClient client = PartamHttpClient.getInstance();
			allPoints = client.loadAllMapPoints();
			return null;
		}
    	
    }
    
    public String genRandPass(int length)
    {
    	Random generator = new Random();
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    	String pass = "";
    	
        for (int i = 0; i < length; i++)
        {
        	pass += letters.charAt(generator.nextInt(100000 % letters.length()));
        }
    	return pass;
    }
    
    public void removeFavorite(JSONObject obj)
    {
		JSONArray localFavorites = null;
		try {
			localFavorites = new JSONArray(userLocalFavorites.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		userLocalFavorites = new JSONArray();
		int id = getJsonInt(obj, "id");
		for (int i = 0; i < localFavorites.length(); i++)
		{
			if(id != getJsonInt(getJsonObject(localFavorites, i), "id"))
			{
				userLocalFavorites.put(getJsonObject(localFavorites, i));
			}
		}
    }
    
    public void setUserFavorites(JSONObject result)
    {
    	userFavorites = result;
    	if (userLocalFavorites.length() == 0)
    	{
    		userLocalFavorites = getJsonArray(userFavorites, "favorites");
		}
    	else
    	{
    		JSONArray favorites = getJsonArray(userFavorites, "favorites");
    		
    		if (userLocalFavorites.length() > favorites.length())
    		{
        		JSONArray localFavorites = null;
        		try {
    				localFavorites = new JSONArray(userLocalFavorites.toString());
    			} catch (JSONException e) {
    				e.printStackTrace();
    			}
    			userLocalFavorites = new JSONArray();
				for (int i = 0; i < localFavorites.length(); i++)
				{
					JSONObject obj = getJsonObject(localFavorites, i);
					int id = getJsonInt(obj, "id");
					
					boolean isExist = false;
					for (int j = 0; j < favorites.length(); j++)
					{
						if(id == getJsonInt(getJsonObject(favorites, j), "id"))
						{
							isExist = true;
							break;
						}
					}
					
					if (isExist)
					{
						userLocalFavorites.put(obj);
					}
				}
			}
    		else if(userLocalFavorites.length() < favorites.length())
    		{
				for (int i = 0; i < favorites.length(); i++)
				{
					JSONObject obj = getJsonObject(favorites, i);
					int id = getJsonInt(obj, "id");
					
					boolean isExist = false;
					for (int j = 0; j < userLocalFavorites.length(); j++)
					{
						if(id == getJsonInt(getJsonObject(userLocalFavorites, j), "id"))
						{
							isExist = true;
							break;
						}
					}
					
					if (!isExist)
					{
						userLocalFavorites.put(obj);
					}
				}
    		}
    	}
    	save();
    }
    
    public String getUrlForCrop(String url)
    {
        return "http://partam.partam.com/cropper/thumb.php?w=" + screenWidth + "&h=" + screenWidth + "&zc=1&src=" + url;
    }
    
    public String getFilesPath()
    {
        return context.getApplicationContext().getFilesDir().toString();
    }

    public String getCachePath()
    {
        return context.getApplicationContext().getFilesDir().toString() + File.separator + "Cache";
    }

    public static String createFileNameFromUrl(String url)
    {
        if (url == null)
        {
            return "";
        }
        return url.replaceAll("[^a-zA-Z0-9]", "");
    }

    public void createFileFromBitmap(Bitmap bmp, String fileName)
    {
        File cacheDir = new File(getFilesPath());
        File file = new File(cacheDir, fileName);

        try {
            OutputStream fOut = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e("Info", "createFileFromBitmap = " + e.getMessage());
        }
    }

    public int getDipsFromPixel(float pixels) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    public float roundTwoDecimals(float d)
    {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        return Float.valueOf(twoDForm.format(d));
    }

    public void openUrl(Activity activity, String url)
    {
        if (!url.startsWith("https://") && !url.startsWith("http://"))
        {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        activity.startActivity(browserIntent);
    }

    public AlertDialog providersEnabled(final Activity activity, String locationProviders)
    {
        if (!locationProviders.contains("gps") && !locationProviders.contains("network"))
        {
            // build a new alert dialog to inform the user that they have no
            // location services enabled
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    //set the message to display to the user
                    .setMessage("No Location Services Enabled")

                            // add the 'positive button' to the dialog and give it a
                            // click listener
                    .setPositiveButton("Enable Location Services",
                            new DialogInterface.OnClickListener() {
                                // setup what to do when clicked
                                public void onClick(DialogInterface dialog, int id) {
                                    // start the settings menu on the correct
                                    // screen for the user
                                    activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            }
                    )
                    // user must enable location services
                    .setCancelable(false);
                    // finish creating the dialog and show to the user
            AlertDialog dialog =  builder.create();
            dialog.show();
            return dialog;
        }
        return null;
    }

    public void showAlert(Context context, String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

	public boolean isValidEmail(CharSequence target)
	{
		if (target == null) 
		{
			return false;
		} 
		else 
		{
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}
    
    public void showRouteOnMap(Activity activity, double srcLat, double srcLng, double desLat, double desLng)
    {
        String uri = "http://maps.google.com/maps?saddr=" + srcLat + "," + srcLng + "&daddr=" + desLat + "," + desLng;
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
        activity.startActivity(intent);
    }

    public int getMapCategoryImage(String title)
    {
    	if (title.contentEquals("Hotels") ||
        		title.contentEquals("Travel Agencies") ||
        		title.contentEquals("Markets") ||
        		title.contentEquals("Attorneys") ||
        		title.contentEquals("Restaurants") ||
        		title.contentEquals("Events") ||
        		title.contentEquals("Entertainers") ||
        		title.contentEquals("Wedding Planning") ||
        		title.contentEquals("Bakeries & Wedding Cakes") ||
        		title.contentEquals("Banquet Halls") ||
        		title.contentEquals("Doctors") ||
        		title.contentEquals("Catering") ||
        		title.contentEquals("Architect") ||
        		title.contentEquals("Photographers & Videographers") ||
        		title.contentEquals("Jewelry") ||
        		title.contentEquals("Hair Salons") ||
        		title.contentEquals("Media") ||
        		title.contentEquals("Makeup Artists")) 
    	{
        	return R.drawable.cat_bussines;
		}
    	
    	// Contributuion
    	if (title.contentEquals("Contributions"))
    	{
        	return R.drawable.cat_contributionst;
    	}
    	
    	// Culture
    	if (title.contentEquals("Museums") ||
    			title.contentEquals("Memorials") ||
    			title.contentEquals("Schools") ||
    			title.contentEquals("Cultural Centers") ||
    			title.contentEquals("Organizations"))
    	{
        	return R.drawable.cat_culture;
    	}
    			
    	// Government
    	if (title.contentEquals("Embassies & Consulates") ||
    			title.contentEquals("Campgrounds"))
    	{
        	return R.drawable.cat_government;
    	}
    	
    	// Landmark
    	if (title.contentEquals("Cities & Street Names") ||
    			title.contentEquals("Sister Cities") ||
    			title.contentEquals("Genocide Memorials") ||
    			title.contentEquals("Monuments"))
    	{
        	return R.drawable.cat_landmark;
    	}
    	
    	// Religious
    	if (title.contentEquals("Churches") ||
    			title.contentEquals("Khachkars"))
    	{
        	return R.drawable.cat_religious;
    	}
    	
    	// If this is a new category then return business type by default
    	return R.drawable.cat_bussines;
    }
    
//	public void closeFragment(Fragment frag)
//	{
//		frag.getActivity().getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).remove(frag).commit();
//	}

	public void openFragment(FragmentActivity act, Fragment frag, int id)
	{
		FragmentTransaction fTrans = act.getSupportFragmentManager().beginTransaction();
		fTrans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		fTrans.add(id, frag);
		fTrans.addToBackStack(null);
		fTrans.commit();
	}

    public static JSONObject getJsonObject(JSONObject jObj, String key)
    {
        JSONObject value = new JSONObject();

        try {
            value = jObj.getJSONObject(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }

    public static JSONObject getJsonObject(JSONArray jArr, int index)
    {
        JSONObject value = new JSONObject();
        try {
            value = jArr.getJSONObject(index);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }

    public static JSONArray getJsonArray(JSONObject jObj, String key)
    {
        JSONArray value = new JSONArray();

        try {
            value = jObj.getJSONArray(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }

    public static String getJsonString(JSONObject jObj, String key)
    {
        String value = "";

        try {
            value = jObj.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }

    public static int getJsonInt(JSONObject jObj, String key)
    {
        int value = -1;

        try {
            value = jObj.getInt(key);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }

    public static boolean getJsonBoolean(JSONObject jObj, String key)
    {
        boolean value = false;

        try {
            value = jObj.getBoolean(key);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }
    
    public static Long getJsonLong(JSONObject jObj, String key)
    {
        Long value = Long.valueOf(-1);

        try {
            value = jObj.getLong(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }

    public static double getJsonDouble(JSONObject jObj, String key)
    {
        double value = -1;

        try {
            value = jObj.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }

    public static float getJsonFloat(JSONObject jObj, String key)
    {
        float value = -1.0f;

        try {
            value = (float) jObj.getDouble(key);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("myLogs", key);
        }

        return value;
    }

    public void hideKeyboard(EditText et)
    {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        et.clearFocus();
    }
    
    public String getUrlForSmallProfPhoto(String url)
	{
    	Log.e("Info","user image = " + url);
//    	return "http://partam.partam.com/timthumb.php?w=100&h=100&zc=1&src=" + url;
    	return  url;
	}
    public String getUrlForSelfiePhoto(String url)
	{
    	int srcIndex = url.indexOf("src=");
    	String srcUrl = url.substring(srcIndex + 4);
//    	Log.e("Info","srcUrl = " + srcUrl);
//    	Log.e("Info", "result url = " + "http://partam.partam.com/timthumb.php?w=" + AppManager.getInstanse().screenWidth + "&h="+ AppManager.getInstanse().screenWidth + "&zc=1&src=" + srcUrl);
    	return "http://partam.partam.com/timthumb.php?w=" + AppManager.getInstanse().screenWidth + "&h="+ AppManager.getInstanse().screenWidth + "&zc=1&src=" + srcUrl;
    	
	}
}