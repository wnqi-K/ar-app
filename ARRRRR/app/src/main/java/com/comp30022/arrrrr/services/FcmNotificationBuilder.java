package com.comp30022.arrrrr.services;

import android.util.Log;

import com.comp30022.arrrrr.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * This class is responsible of managing Firebase Could messages which will allow
 * users to send notifications
 *
 * */
public class FcmNotificationBuilder {
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "FcmNotificationBuilder";
    private static final String SERVER_API_KEY = "AAAAihNVRXE:APA91bHEWtKIVjo-Bk-zhu4a1PIpi" +
            "Kw15n_c1uUsuGZQV7R6FkyDXgYopd9iCLuxgcrW_DArGg6z0KeXs6nv73iS3Rw6gETaB-cJPCD4I54" +
            "AB2II3JQ0DFGz6E5idjcyniMCSDkEb7cS";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTH_KEY = "key=" + SERVER_API_KEY;
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    // json related keys


    private String mTitle;
    private String mMessage;
    private String mUsername;
    private String mUid;
    private String mFirebaseToken;
    private String mReceiverFirebaseToken;

    private FcmNotificationBuilder() {

    }

    public static FcmNotificationBuilder initialize() {
        return new FcmNotificationBuilder();
    }

    public FcmNotificationBuilder title(String title) {
        mTitle = title;
        return this;
    }

    public FcmNotificationBuilder message(String message) {
        mMessage = message;
        return this;
    }

    public FcmNotificationBuilder username(String username) {
        mUsername = username;
        return this;
    }

    public FcmNotificationBuilder uid(String uid) {
        mUid = uid;
        return this;
    }

    public FcmNotificationBuilder firebaseToken(String firebaseToken) {
        mFirebaseToken = firebaseToken;
        return this;
    }

    public FcmNotificationBuilder receiverFirebaseToken(String receiverFirebaseToken) {
        mReceiverFirebaseToken = receiverFirebaseToken;
        return this;
    }

    /**
     * This class will send post request to the server
     * */
    public void send() {
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MEDIA_TYPE_JSON, getValidJsonBody().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Request request = new Request.Builder()
                .addHeader(CONTENT_TYPE, APPLICATION_JSON)
                .addHeader(AUTHORIZATION, AUTH_KEY)
                .url(FCM_URL)
                .post(requestBody)
                .build();

        Call call = new OkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "onGetAllUsersFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "onResponse: " + response.body().string());
            }
        });
    }

    /**
     * This class will write a Post Request in the form of
     * {
     *     https://fcm.googleapis.com/fcm/send
     *     Content-Type:application/json
     *     Authorization:key=AIzaSyZ-1u...0GBYzPu7Udno5aA
     *
     *     {
     *         "to": "aUniqueKey",
     *         "data": {
     *         "hello": "This is a Firebase Cloud Messaging Device Group Message!",
     *     }
     * }
     * */
    private JSONObject getValidJsonBody() throws JSONException {
        JSONObject jsonObjectBody = new JSONObject();
        jsonObjectBody.put(Constants.KEY_TO, mReceiverFirebaseToken);

        JSONObject jsonObjectData = new JSONObject();
        jsonObjectData.put(Constants.KEY_TITLE, mTitle);
        jsonObjectData.put(Constants.KEY_TEXT, mMessage);
        jsonObjectData.put(Constants.KEY_USERNAME, mUsername);
        jsonObjectData.put(Constants.KEY_UID, mUid);
        jsonObjectData.put(Constants.KEY_FCM_TOKEN, mFirebaseToken);
        jsonObjectBody.put(Constants.KEY_DATA, jsonObjectData);

        return jsonObjectBody;
    }
}
