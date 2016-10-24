package com.partam.partam.customclasses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;

import com.partam.partam.AppManager;

public class PartamHttpClient
{
	private static PartamHttpClient client = null;
	private static final String BaseURL = "http://partam.partam.com/api/v1";

	public static PartamHttpClient getInstance() {
		if(client == null) {
			client = new PartamHttpClient();
		}
		return client;
	}

	public JSONArray loadAllMapPoints()
	{
		String url = BaseURL + "/main/map/points";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONArray arr = new JSONArray();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONArray();
			}

			arr = new JSONArray(responseString);
			String newSize = "w=" + AppManager.getInstanse().screenWidth + "&h=" + AppManager.getInstanse().screenWidth;
			for (int i = 0; i < arr.length(); i++) 
			{
				JSONObject obj = AppManager.getJsonObject(arr, i);
				String picture = AppManager.getJsonString(obj, "picture");
				obj.put("picture", picture.replace("w=400&h=400", newSize));
			}
		} catch (Exception e) { }

		return arr;
	}

	public JSONArray loadMainInfo(int limits, int offset)
	{
		String url = BaseURL + "/mains/" + limits + "/limits/" + offset + "/offset";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONArray arr = new JSONArray();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONArray();
			}

			arr = new JSONArray(responseString);
			String newSize = "w=" + AppManager.getInstanse().screenWidth + "&h=" + AppManager.getInstanse().screenWidth;
			for (int i = 0; i < arr.length(); i++) 
			{
				JSONObject obj = AppManager.getJsonObject(arr, i);
				String picture = AppManager.getJsonString(obj, "picture");
				obj.put("picture", picture.replace("w=400&h=400", newSize));
			}

		} catch (Exception e) { }

		return arr;
	}

	public JSONArray loadSearchPoints(String searchText)
	{
		String url = BaseURL + "/mains/" + searchText + "/search/point";
		url = url.replace(" ", "%20");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONArray arr = new JSONArray();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONArray();
			}

			arr = new JSONArray(responseString);
			String newSize = "w=" + AppManager.getInstanse().screenWidth + "&h=" + AppManager.getInstanse().screenWidth;
			for (int i = 0; i < arr.length(); i++) 
			{
				JSONObject obj = AppManager.getJsonObject(arr, i);
				String picture = AppManager.getJsonString(obj, "picture");
				obj.put("picture", picture.replace("w=400&h=400", newSize));
			}

		} catch (Exception e) { }

		return arr;
	}

	public JSONArray loadPointByCity_latitude(String lat, String lng, int limits, int offset)
	{
		String url = BaseURL + "/mains/" + lat + "/lats/" + lng + "/longs/"  + limits + "/limits/" + offset + "/offsets/50/distance";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONArray arr = new JSONArray();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONArray();
			}

			arr = new JSONArray(responseString);
			String newSize = "w=" + AppManager.getInstanse().screenWidth + "&h=" + AppManager.getInstanse().screenWidth;
			for (int i = 0; i < arr.length(); i++) 
			{
				JSONObject obj = AppManager.getJsonObject(arr, i);
				String picture = AppManager.getJsonString(obj, "picture");
				obj.put("picture", picture.replace("w=400&h=400", newSize));
			}
		} catch (Exception e) { }

		return arr;
	}

	public JSONArray loadPointByCategoriesAndCity(String categories, String lat, String lng, int limit, int offset)
	{
		String url = BaseURL + "/mains/"  + categories + "/cats/" + lat + "/lats/" + lng + "/longs/"  + limit + "/limits/" + offset + "/offsets/50/distance";
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONArray arr = new JSONArray();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONArray();
			}

			arr = new JSONArray(responseString);
			String newSize = "w=" + AppManager.getInstanse().screenWidth + "&h=" + AppManager.getInstanse().screenWidth;
			for (int i = 0; i < arr.length(); i++) 
			{
				JSONObject obj = AppManager.getJsonObject(arr, i);
				String picture = AppManager.getJsonString(obj, "picture");
				obj.put("picture", picture.replace("w=400&h=400", newSize));
			}
		} catch (Exception e) { }

		return arr;
	}

	public JSONArray loadPointByCategory(String categories, int limit)
	{
		String url = BaseURL + "/mains/" + categories + "/points/" + limit + "/starts/10/limits/name/orders/asc/direction";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONArray arr = new JSONArray();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONArray();
			}

			arr = new JSONArray(responseString);
			String newSize = "w=" + AppManager.getInstanse().screenWidth + "&h=" + AppManager.getInstanse().screenWidth;
			for (int i = 0; i < arr.length(); i++) 
			{
				JSONObject obj = AppManager.getJsonObject(arr, i);
				String picture = AppManager.getJsonString(obj, "picture");
				obj.put("picture", picture.replace("w=400&h=400", newSize));
			}
		} catch (Exception e) { }

		return arr;
	}

	public JSONArray loadCategoriesInfo(String categories)
	{
		String url = BaseURL + "/main/" + categories;

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONArray arrCategories = new JSONArray();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONArray();
			}

			arrCategories = new JSONArray(responseString);

		} catch (Exception e) { }

		return arrCategories;
	}

	public JSONObject loadLocationsInfo(String locations)
	{
		String url = BaseURL + "/main/" + locations;

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONObject arrCategories = new JSONObject();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONObject();
			}

			arrCategories = new JSONObject(responseString);

		} catch (Exception e) { }

		return arrCategories;
	}

	public JSONObject loadDetailInfo(int detailID)
	{
		String url = BaseURL + "/mains/" + detailID + "/point";
		Log.e("Info", "url = " + url);

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONObject arrCategories = new JSONObject();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.e("Info", "responseString = " + responseString);
			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONObject();
			}

			arrCategories = new JSONObject(responseString);

		} catch (Exception e) { }

		return arrCategories;
	}
	
	public void postCheckin(int pointId, String token, String message, Bitmap image, int userId)
	{
//		Log.e("Info", " postCheckin pointId = " + pointId);
//		Log.e("Info", " postCheckin token = " + token);
//		Log.e("Info", " postCheckin message = " + message);
//		Log.e("Info", " postCheckin image = " + image);
//		Log.e("Info", " postCheckin userId = " + userId);
		
		String url = BaseURL + "/checkins/checks/ins";
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE); 
		ByteArrayOutputStream stream;

		stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();

		try{
			reqEntity.addPart("point", new StringBody(pointId + ""));
			reqEntity.addPart("token", new StringBody(token == null ? "" : token));
			reqEntity.addPart("message",  new StringBody(message == null ? "" : message));
			reqEntity.addPart("files", new ByteArrayBody(byteArray, "selfie.png"));
			reqEntity.addPart("user", new StringBody(userId + ""));
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}

		httppost.setEntity(reqEntity);

		try {
			HttpResponse response = httpClient.execute(httppost);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.e("Info", " postCheckin responseString = " + responseString); //////// success:false
		} catch (Exception e) {
			e.printStackTrace();
		}
		getCheckIns(pointId);
	}
	
	public void getCheckIns(int pointId) ////// 
	{
		String pointIdString = Integer.toString(pointId);
		String url = BaseURL + "/mains/" + pointIdString + "/point/checkins";
//		Log.e("Info", "getCheckIns url = " + url);

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.e("Info", "getCheckIns responseString = " + responseString);
			AppManager.getInstanse().checkinsArray = new JSONArray(responseString);
			
		} catch (Exception e) { }
		
	}
	
	public void deleteCheckin(int checkinId, int pointId)
	{
		Log.e("Info", " postCheckin pointId = " + pointId);
		Log.e("Info", " postCheckin checkinId = " + checkinId);
		
		String checkinIdStr = String.valueOf(checkinId);
		String url = BaseURL + "/checkins/" + checkinIdStr;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpDelete httpDelete = new HttpDelete(url);

		try {
			HttpResponse response = httpClient.execute(httpDelete);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.e("Info", " deleteCheckin responseString = " + responseString); 
		} catch (Exception e) {
			e.printStackTrace();
		}
		getCheckIns(pointId);
		
	}

	public JSONObject register(String fullName, String email, String pass) throws Exception
	{
		String url = BaseURL + "/user/register";
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("fos_user_registration_form[displayName]", fullName));
		nameValuePairs.add(new BasicNameValuePair("fos_user_registration_form[email]", email));
		nameValuePairs.add(new BasicNameValuePair("fos_user_registration_form[plainPassword]", pass));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(url); 
		httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		JSONObject register = new JSONObject();
		HttpResponse response = httpClient.execute(httpPut);
		String responseString = EntityUtils.toString(response.getEntity());
		register = new JSONObject(responseString);
		Log.d("Info", "regiter response = " + responseString);
		return register;
	}
	
	public JSONObject sendFBLoginRequest(String fbAccessToken) throws Exception
	{
		String url = BaseURL + "/user/login";
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("user[token]", fbAccessToken));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(url); 
		httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		JSONObject login = new JSONObject();
		HttpResponse response = httpClient.execute(httpPut);
		String responseString = EntityUtils.toString(response.getEntity());
		Log.e("Info", "fb login response = " + responseString);
		login = new JSONObject(responseString);
		return login;
	}

	public JSONObject sendFBRegisterRequest(JSONObject user) throws Exception 
	{
		String url = BaseURL + "/user/register";
		String pass = AppManager.getInstanse().genRandPass(6);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("fos_user_registration_form[email]", AppManager.getJsonString(user, "email")));
		nameValuePairs.add(new BasicNameValuePair("fos_user_registration_form[plainPassword]", pass));
		nameValuePairs.add(new BasicNameValuePair("fos_user_registration_form[displayName]", AppManager.getJsonString(user, "name")));
		nameValuePairs.add(new BasicNameValuePair("fos_user_registration_form[facebookId]", AppManager.getJsonString(user, "id")));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(url); 
		httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		JSONObject register = new JSONObject();
		HttpResponse response = httpClient.execute(httpPut);
		String responseString = EntityUtils.toString(response.getEntity());
		register = new JSONObject(responseString);
		Log.d("myLogs", "fb regiter response = " + responseString);
		return register;
	}
	
	public JSONObject login(String email, String pass) throws Exception
	{
		String url = BaseURL + "/user/login";
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("user[email]", email));
		nameValuePairs.add(new BasicNameValuePair("user[password]", pass));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(url); 
		httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		JSONObject login = new JSONObject();
		HttpResponse response = httpClient.execute(httpPut);
		String responseString = EntityUtils.toString(response.getEntity());
		login = new JSONObject(responseString);
		Log.e("Info", "login response = " + responseString);
		return login;
	}
	
	public boolean logout()
	{
		AppManager.getInstanse().userInfo = new JSONObject();
		AppManager.getInstanse().userFavorites = new JSONObject();
		AppManager.getInstanse().save();
		
		String url = BaseURL + "/users/" + AppManager.getInstanse().token + "/logout";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url); 

		JSONObject obj = new JSONObject();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return false;
			}

			obj = new JSONObject(responseString);
			Log.d("myLogs", "logout responseString = " + responseString);
			

			AppManager.getInstanse().token = "";
			AppManager.getInstanse().isLogOut = true;
			AppManager.getInstanse().save();
			if (AppManager.getJsonBoolean(obj, "success"))
			{
				return true;
			}
		} catch (Exception e) { }
		return false;
	}

	public void sendForgottPassRequest(String email) throws Exception
	{
		String url = BaseURL + "/users/resets";
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("user[email]", email));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url); 
		httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		HttpResponse response = httpClient.execute(httpPost);
		String responseString = EntityUtils.toString(response.getEntity());
		Log.d("myLogs", "sendForgottPassRequest response = " + responseString);
	}
	
	public JSONObject loadUserInfo()
	{
		String url = BaseURL + "/users/" + AppManager.getInstanse().token + "/profile";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONObject userInfo = new JSONObject();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.e("Info", "User info response = " + responseString);
			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONObject();
			}

			userInfo = new JSONObject(responseString);
			AppManager.getInstanse().userInfo = userInfo;
			loadUserFavorites();

		} catch (Exception e) { }

		return userInfo;
	}
	
	public JSONObject loadUserFavorites()
	{
		String url = BaseURL + "/mains/" + AppManager.getInstanse().token + "/favorites/user";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		JSONObject userFavorites = new JSONObject();

		try
		{
			HttpResponse response = httpClient.execute(httpGet);
			String responseString = EntityUtils.toString(response.getEntity());
    		Log.d("myLogs", "loadUserFavorites responseString = " + responseString);

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONObject();
			}

			userFavorites = new JSONObject(responseString);
			AppManager.getInstanse().setUserFavorites(userFavorites);
		} catch (Exception e) { }

		return userFavorites;
	}
	
	public JSONObject addOrRemoveFavorites(int point, boolean favorite)
	{
		String url = BaseURL + "/mains/" + point + "/favorites/" +  AppManager.getInstanse().token + "/user";

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpGet httpFav;
		if (favorite)
		{
			httpFav = new HttpLINK(url);
		}
		else
		{
			httpFav = new HttpUNLINK(url);
		}

		JSONObject result = new JSONObject();

		try
		{
			HttpResponse response = httpClient.execute(httpFav);
			String responseString = EntityUtils.toString(response.getEntity());
    		Log.d("myLogs", "addOrRemoveFavorites responseString = " + responseString);

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONObject();
			}

			result = new JSONObject(responseString);
		} catch (Exception e) { }

		return result;
	}
	
	public JSONObject addComment(String comment, int pointId)
	{
		String url = BaseURL + "/comments/" + AppManager.getInstanse().token + "/points/" + pointId;

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("comment[name]", AppManager.getJsonString(AppManager.getJsonObject(AppManager.getInstanse().userInfo, "user"), "display_name")));
		nameValuePairs.add(new BasicNameValuePair("comment[message]", comment));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		JSONObject result = new JSONObject();

		try
		{
			HttpResponse response = httpClient.execute(httpPost);
			String responseString = EntityUtils.toString(response.getEntity());
    		Log.d("myLogs", "addComment responseString = " + responseString);

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return new JSONObject();
			}

			result = new JSONObject(responseString);
		} catch (Exception e) { }

		return result;
	}
	
    public void changeUserAvatar(byte [] picture)
    {
        String url = BaseURL + "/users/" + AppManager.getInstanse().token + "/images";

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("user[file]", new ByteArrayBody(picture, "userAvatar.png"));
        httppost.setEntity(reqEntity);

        try {
            HttpResponse response = httpClient.execute(httppost);
            String responseString = EntityUtils.toString(response.getEntity());
    		Log.d("myLogs", "changeUserAvatar responseString = " + responseString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void changeUserInfo(String fullName, String location)
    {
		String url = BaseURL + "/users/" + AppManager.getInstanse().token + "/profile";

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("user[displayName]", fullName));
		nameValuePairs.add(new BasicNameValuePair("user[currentCity]", location));
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPatch httpPatch = new HttpPatch(url);
		try {
			httpPatch.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		try
		{
			HttpResponse response = httpClient.execute(httpPatch);
			String responseString = EntityUtils.toString(response.getEntity());
			Log.d("myLogs", "changeUserInfo response = " + responseString);

			if (response.getStatusLine().getStatusCode() == 0)
			{
				return;
			}

		} catch (Exception e) { }
		return;
    }
    
    
	public JSONObject changeUserPass(String pass, String newPass) throws Exception
	{
		String url = BaseURL + "/users/" + AppManager.getInstanse().token + "/password/reset";

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("password[new][first]", pass));
		nameValuePairs.add(new BasicNameValuePair("password[new][first]", newPass));

		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpPut httpPut = new HttpPut(url); 
		httpPut.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		JSONObject login = new JSONObject();
		HttpResponse response = httpClient.execute(httpPut);
		String responseString = EntityUtils.toString(response.getEntity());
		login = new JSONObject(responseString);
		Log.d("myLogs", "changeUserPass response = " + responseString);
		return login;
	}
	
	public JSONObject addNewMapPoint(ArrayList<Bitmap> photos, String name, String tags, String category, String lat, String lng, 
									String city, String state, String address, String zip, String email, String weburl, String description, String paypal)
	{
        String url = BaseURL + "/mains/news/points";

        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        
        ByteArrayOutputStream stream;
        for (int i = 0; i < photos.size(); i++)
		{
        	stream = new ByteArrayOutputStream();
        	photos.get(i).compress(Bitmap.CompressFormat.PNG, 100, stream);
        	byte[] byteArray = stream.toByteArray();
            reqEntity.addPart("photo" + i, new ByteArrayBody(byteArray, "photo.png"));
		}
        
        try
		{
        	reqEntity.addPart("name", new StringBody(name == null ? "" : name));
        	reqEntity.addPart("tags", new StringBody(tags == null ? "" : tags));
        	reqEntity.addPart("category", new StringBody(category == null ? "" : category));
        	reqEntity.addPart("lat", new StringBody(lat == null ? "" : lat));
        	reqEntity.addPart("lng", new StringBody(lng == null ? "" : lng));
        	reqEntity.addPart("city", new StringBody(city == null ? "" : city));
        	reqEntity.addPart("state", new StringBody(state == null ? "" : state));
        	reqEntity.addPart("address", new StringBody(address == null ? "" : address));
        	reqEntity.addPart("zip", new StringBody(zip == null ? "" : zip));
        	reqEntity.addPart("email", new StringBody(email == null ? "" : email));
        	reqEntity.addPart("weburl", new StringBody(weburl == null ? "" : weburl));
        	reqEntity.addPart("description", new StringBody(description == null ? "" : description));
			reqEntity.addPart("paypal", new StringBody(paypal == null ? "" : paypal));
		} 
        catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
		}
        
        httppost.setEntity(reqEntity);

        try {
            HttpResponse response = httpClient.execute(httppost);
            String responseString = EntityUtils.toString(response.getEntity());
    		Log.d("myLogs", "addNewMapPoint responseString = " + responseString);
            return new JSONObject(responseString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new JSONObject();
	}
}