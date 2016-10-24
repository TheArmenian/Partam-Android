package com.partam.partam.customclasses;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.text.TextUtils;

public class AddresFromLocation 
{
    private String address1 = "", address2 = "", city = "", state = "", country = "", county = "", postalCode = "", lat = "", lng = "";

    public void initAddress(String lat, String lng)
    {
        this.lat = lat;
        this.lng = lng;
    	new DoRequest().execute();
    }
    
    private void init(String json)
    {
        address1 = "";
        address2 = "";
        city = "";
        state = "";
        country = "";
        county = "";
        postalCode = "";

        try 
        {
            JSONObject jsonObj = new JSONObject(json);
            String Status = jsonObj.getString("status");
            if (Status.equalsIgnoreCase("OK")) {
                JSONArray Results = jsonObj.getJSONArray("results");
                JSONObject zero = Results.getJSONObject(0);
                JSONArray address_components = zero.getJSONArray("address_components");

                for (int i = 0; i < address_components.length(); i++) {
                    JSONObject zero2 = address_components.getJSONObject(i);
                    String long_name = zero2.getString("long_name");
                    JSONArray mtypes = zero2.getJSONArray("types");
                    String Type = mtypes.getString(0);

                    if (TextUtils.isEmpty(long_name) == false || !long_name.equals(null) || long_name.length() > 0 || long_name != "")
                    {
                        if (Type.equalsIgnoreCase("street_number")) 
                        {
                            address1 = long_name + " ";
                        } 
                        else if (Type.equalsIgnoreCase("route"))
                        {
                            address1 = address1 + long_name;
                        }
                        else if (Type.equalsIgnoreCase("sublocality")) 
                        {
                            address2 = long_name;
                        }
                        else if (Type.equalsIgnoreCase("locality"))
                        {
                            city = long_name;
                        }
                        else if (Type.equalsIgnoreCase("administrative_area_level_2"))
                        {
                            county = long_name;
                        } 
                        else if (Type.equalsIgnoreCase("administrative_area_level_1"))
                        {
                            state = long_name;
                        } 
                        else if (Type.equalsIgnoreCase("country")) 
                        {
                            country = long_name;
                        }
                        else if (Type.equalsIgnoreCase("postal_code")) 
                        {
                            postalCode = long_name;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getAddress1() {
        return address1;

    }

    public String getAddress2() {
        return address2;

    }

    public String getCity() {
        return city;

    }

    public String getState() {
        return state;

    }

    public String getCountry() {
        return country;

    }

    public String getCounty() {
        return county;

    }

    public String getPostalCode() {
        return postalCode;
    }
    
    public String getLatitude()
    {
    	return lat;
    }
    
    public String getLongitude()
    {
    	return lng;
    }

	private class DoRequest extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
	    	String url = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&sensor=true";
	    	
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(url);

			try
			{
				HttpResponse response = httpClient.execute(httpGet);
				String responseString = EntityUtils.toString(response.getEntity());
				init(responseString);
			} catch (Exception e) { }
			return null;
		}
	}

}