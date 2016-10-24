package com.partam.partam.customclasses;

import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.Builder;
import com.facebook.Session.NewPermissionsRequest;
import com.facebook.Session.OpenRequest;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;

public class FB {
	public static class Callback {
		public void call(Object data){

		}
	}
	private Activity activity;

	public FB(Activity parent){
		activity = parent;
	}

	private static Session openActiveSession(Activity activity, boolean allowLoginUI, Session.StatusCallback callback)
	{
		Session session = new Builder(activity).build();
		if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
			Session.setActiveSession(session);
			OpenRequest request = new OpenRequest(activity).setCallback(callback);
			request.setPermissions("basic_info", "email");
			session.openForRead(request);
			return session;
		}
		return null;
	}

	private static Session openActiveSessionForPost(Activity activity, boolean allowLoginUI, Session.StatusCallback callback) 
	{
		Session session = new Builder(activity).build();
		if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
			Session.setActiveSession(session);
			OpenRequest request = new OpenRequest(activity).setCallback(callback);
			request.setPermissions("publish_actions");
			session.openForPublish(request);
			return session;
		}
		return null;
	}

	private static Session openActiveSessionForGetFriendsList(Activity activity, boolean allowLoginUI, Session.StatusCallback callback) 
	{
		Session session = new Builder(activity).build();
		if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
			Session.setActiveSession(session);
			OpenRequest request = new OpenRequest(activity).setCallback(callback);
			request.setPermissions(Arrays.asList("email", "friends_birthday", "friends_education_history", "friends_likes", "friends_hometown", "friends_location", "friends_interests", "friends_relationship_details", "friends_religion_politics", "friends_website", "friends_activities"));
			session.openForRead(request);
			return session;
		}
		return null;
	}
	
	private static Session openActiveSessionForProfileInfo(Activity activity, boolean allowLoginUI, Session.StatusCallback callback) 
	{
		Session session = new Builder(activity).build();
		if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUI) {
			Session.setActiveSession(session);
			OpenRequest request = new OpenRequest(activity).setCallback(callback);
			request.setPermissions(Arrays.asList("user_birthday"));
			session.openForRead(request);
			return session;
		}
		return null;
	}

	public static com.facebook.Request newUploadPhotoRequest(Session session, Bitmap image, String caption, String description, com.facebook.Request.Callback callback) {
		Bundle parameters = new Bundle(3);
		parameters.putParcelable("picture", image);
		parameters.putString("caption", caption);
		parameters.putString("description", description);

		return new com.facebook.Request(session, "me/photos", parameters, HttpMethod.POST, callback);
	}

	public void authenticate(final Callback callback)
	{
		// start Facebook Login
		FB.openActiveSession(activity, true, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				switch(state)
				{
				case CLOSED_LOGIN_FAILED:
					Log.i("myLogs", "CLOSED_LOGIN_FAILED = " + exception.getMessage());
					callback.call(null);	
					break;
				case OPENED:
				case OPENED_TOKEN_UPDATED:
					callback.call(session.getAccessToken());
					break;
				default:
					break;
				}
				if(exception != null){
					Log.w("FB", exception.getMessage());
				}
			}
		});
	}

	public void getProfileInfo(final com.facebook.Request.GraphUserCallback callback)
	{
		postRequestingPermission = false;
		FB.openActiveSessionForProfileInfo(activity, true, new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception) 
			{
				Log.e("Info", "state = " + state);
				switch(state)
				{
				case CLOSED_LOGIN_FAILED:
					try {
						callback.onCompleted(null, null);
					} catch (Exception e) {
						Log.e("Info", "________" + e);
					}// callback.onCompleted(null, null);
					break;
				case OPENED:
				case OPENED_TOKEN_UPDATED:
					if(session.getPermissions().contains("email"))
					{						
						Request request = Request.newMeRequest(session, new GraphUserCallback()
						{
							@Override
							public void onCompleted(GraphUser user, Response response) 
							{
								callback.onCompleted(user, response);
							}
						});

						// Add birthday to the list of info to get.
						Bundle requestParams = request.getParameters();
						requestParams.putString("fields", "id,name,last_name,email,first_name,picture");
						
						request.setParameters(requestParams);
						Request.executeBatchAsync(request);
					}
					else if(!postRequestingPermission)
					{
						Log.w("myLogs", "requestNewPublishPermissions");
						postRequestingPermission = true;
						session.requestNewReadPermissions(new NewPermissionsRequest(activity, "email"));
					} 
					else
					{
						callback.onCompleted(null, null);							
					}
					break;
				default:
					break;
				}

				if(exception != null)
				{
					Log.e("myLogs", exception.getMessage());
				}

			}
		});
	}
	
	public void getFriendsList(final com.facebook.Request.GraphUserListCallback callback)
	{
		postRequestingPermission = false;
		FB.openActiveSessionForGetFriendsList(activity, true, new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception) 
			{
				switch(state)
				{
				case CLOSED_LOGIN_FAILED:
					callback.onCompleted(null, null);
					break;
				case OPENED:
				case OPENED_TOKEN_UPDATED:
					if(session.getPermissions().contains("friends_birthday"))
					{
						Request request = Request.newMyFriendsRequest(session, new GraphUserListCallback() 
						{
							@Override
							public void onCompleted(List<GraphUser> users, Response response) 
							{
								callback.onCompleted(users, response);
							}
						});

						// Add birthday to the list of info to get.
						Bundle requestParams = request.getParameters();
						requestParams.putString("fields", "id,name,picture,installed,first_name,last_name,middle_name,location,email,link,username,birthday,education,favorite_athletes,favorite_teams,gender,hometown,inspirational_people,languages,location,political,relationship_status,religion,website,activities");
						
						request.setParameters(requestParams);
						Request.executeBatchAsync(request);
					}
					else if(!postRequestingPermission)
					{
						Log.w("myLogs", "requestNewPublishPermissions");
						postRequestingPermission = true;
						session.requestNewReadPermissions(new NewPermissionsRequest(activity, "email", "friends_birthday", "friends_education_history", "friends_likes", "friends_hometown", "friends_location", "friends_interests", "friends_relationship_details", "friends_religion_politics", "friends_website", "friends_activities"));
					} 
					else
					{
						callback.onCompleted(null, null);							
					}
					break;
				default:
					break;
				}

				if(exception != null)
				{
					Log.e("myLogs", exception.getMessage());
				}

			}
		});
	}

	private boolean postRequestingPermission = false;
	public void postImage(final String message, final Bitmap image, final Callback callback)
	{
		// start Facebook Login
		postRequestingPermission = false;
		FB.openActiveSessionForPost(activity, true, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state, Exception exception) {
				Log.w("FB","PostCall");
				Log.w("FB",state.name());
				switch(state){
				case CLOSED_LOGIN_FAILED:
					callback.call(false);	
					break;
				case OPENED:
				case OPENED_TOKEN_UPDATED:
					if(session.getPermissions().contains("publish_actions")){
						//Publish
						Log.w("FB","Publish");
						FB.newUploadPhotoRequest(session, image, message, "desc: " + message, new com.facebook.Request.Callback(){
							@Override
							public void onCompleted(Response response) {
								Log.w("FB","onCompleted");
								if(response.getError() != null){
									Log.e("FB", response.getError().getErrorMessage());
								}
								callback.call(response.getError() == null);								
							}
						}).executeAsync();
					} else if(!postRequestingPermission){
						Log.w("Info","requestNewPublishPermissions");
						postRequestingPermission = true;
						session.requestNewPublishPermissions(new NewPermissionsRequest(activity, "publish_actions"));
					} else{
						callback.call(false);							
					}
					break;
				default:
					break;
				}
				if(exception != null){
					Log.e("FB", exception.getMessage());
				}
			}
		});    	
	}

	public void post(final String message, final Callback callback)
	{
		// start Facebook Login
		postRequestingPermission = false;
		FB.openActiveSessionForPost(activity, true, new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception)
			{
				Log.w("FB","PostCall");
				Log.w("FB",state.name());
				switch(state)
				{
				case CLOSED_LOGIN_FAILED:
					callback.call(false);	
					break;
				case OPENED:
				case OPENED_TOKEN_UPDATED:
					if(session.getPermissions().contains("publish_actions"))
					{
						//Publish
						Log.w("FB","Publish");
						com.facebook.Request.newStatusUpdateRequest(session, message, new com.facebook.Request.Callback()
						{
							@Override
							public void onCompleted(Response response) 
							{
								Log.w("FB","onCompleted");
								if(response.getError() != null){
									Log.e("FB", response.getError().getErrorMessage());
								}
								callback.call(response.getError() == null);								
							}
						}).executeAsync();
					}
					else if(!postRequestingPermission)
					{
						Log.w("FB","requestNewPublishPermissions");
						postRequestingPermission = true;
						session.requestNewPublishPermissions(new NewPermissionsRequest(activity, "publish_actions"));
					} 
					else
					{
						callback.call(false);							
					}
					break;
				default:
					break;
				}
				if(exception != null){
					Log.e("FB", exception.getMessage());
				}
			}
		});    	
	}

	public void post(final String friend_uid, final String name, final String caption, final String description, final String link, final String picture, final Callback callback)
	{
		// start Facebook Login
		postRequestingPermission = false;
		FB.openActiveSessionForPost(activity, true, new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception)
			{
				Log.w("FB","PostCall");
				Log.w("FB",state.name());
				switch(state)
				{
				case CLOSED_LOGIN_FAILED:
					callback.call(false);	
					break;
				case OPENED:
				case OPENED_TOKEN_UPDATED:
					if(session.getPermissions().contains("publish_actions"))
					{
						//Publish
						Log.w("FB","Publish");
						if (friend_uid != null && friend_uid.length() > 0) {
							if (session != null && session.isOpened()) {
								// If the session is open, make an API call to get user data
								// and define a new callback to handle the response
								Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
									@Override
									public void onCompleted(GraphUser user, Response response) 
									{
										if (user != null) {
											String user_ID = user.getId();//user id
											publishFeedDialog(user_ID, friend_uid, name, caption, description, link, picture);
										}
									}   
								}); 
								Request.executeBatchAsync(request);
							}
						}
						else
						{
							publishFeedDialog("", friend_uid, name, caption, description, link, picture);
						}
					}
					else if(!postRequestingPermission)
					{
						Log.w("FB","requestNewPublishPermissions");
						postRequestingPermission = true;
						session.requestNewPublishPermissions(new NewPermissionsRequest(activity, "publish_actions"));
					} 
					else
					{
						callback.call(false);							
					}
					break;
				default:
					break;
				}
				if(exception != null){
					Log.e("FB", exception.getMessage());
				}
			}
		});    	
	}

	private void publishFeedDialog(String userID, String friend_uid, String name, String caption, String description, String link, String picture) {

		Bundle params = new Bundle();
		//This is what you need to post to a friend's wall 1163906630  100002904238430
		if (friend_uid != null && friend_uid.length() > 0)
		{
			params.putString("from", userID);
			params.putString("to", friend_uid);
		}
		//up to this
		params.putString("name", name);
		params.putString("caption", caption);
		params.putString("description", description);
		params.putString("link", link);
		params.putString("picture", picture);
		WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(activity, Session.getActiveSession(), params)).setOnCompleteListener(new OnCompleteListener()
		{
			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error == null) 
				{
					// When the story is posted, echo the success
					// and the post Id.
					final String postId = values.getString("post_id");
					if (postId != null) {
//												Toast.makeText(activity, "Posted story, id: "+ postId, Toast.LENGTH_SHORT).show();
					} else {
						// User clicked the Cancel button
//												Toast.makeText(activity, "Publish cancelled", Toast.LENGTH_SHORT).show();
					}
				} else if (error instanceof FacebookOperationCanceledException) {
					// User clicked the "x" button
//										Toast.makeText(activity,  "Publish cancelled", Toast.LENGTH_SHORT).show();
				} else {
					// Generic, ex: network error
//										Toast.makeText(activity, "Error posting story", Toast.LENGTH_SHORT).show();
				}
			}

		}).build();
		feedDialog.show();
	}
	
	
	public static com.facebook.Request friendsLikesRequest(Session session, String id, com.facebook.Request.Callback callback)
	{
		return new com.facebook.Request(session, id + "/likes", null, HttpMethod.GET, callback);
	}
	
	public void getFriendsLike(final String id, final com.facebook.Request.Callback callback)
	{
		postRequestingPermission = false;
		FB.openActiveSessionForGetFriendsList(activity, true, new Session.StatusCallback()
		{
			@Override
			public void call(Session session, SessionState state, Exception exception) 
			{
				switch(state)
				{
				case CLOSED_LOGIN_FAILED:
					callback.onCompleted(null);
					break;
				case OPENED:
				case OPENED_TOKEN_UPDATED:
					if(session.getPermissions().contains("friends_birthday"))
					{
						Request request = FB.friendsLikesRequest(session, id, new com.facebook.Request.Callback()
						{
							@Override
							public void onCompleted(Response response)
							{
								if(response.getError() != null)
								{
									Log.e("Info", response.getError().getErrorMessage());
								}
								callback.onCompleted(response);								
							}
						});
						Request.executeBatchAsync(request);
					}
					else if(!postRequestingPermission)
					{
						Log.w("Info", "requestNewPublishPermissions");
						postRequestingPermission = true;
						session.requestNewReadPermissions(new NewPermissionsRequest(activity, "email", "friends_birthday", "friends_education_history", "friends_likes", "friends_hometown", "friends_location", "friends_interests", "friends_relationship_details", "friends_religion_politics", "friends_website", "friends_activities"));
//						session.requestNewReadPermissions(new NewPermissionsRequest(activity, "friends_likes"));
					} 
					else
					{
						callback.onCompleted(null);							
					}
					break;
				default:
					break;
				}

				if(exception != null)
				{
					Log.e("myLogs", exception.getMessage());
				}

			}
		});
	}
}
