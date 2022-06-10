package com.techtown.tarsosdsp_pitchdetect.Singing;

import android.util.Log;

import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpConnection {
    private OkHttpClient client;
    private static HttpConnection instance = new HttpConnection();

    public static HttpConnection getInstance() {
        return instance;
    }

    private HttpConnection() {
        this.client = new OkHttpClient();
    }

    public void callGet(String userEmail, String songName) {
        try {
            HttpUrl httpUrl = new HttpUrl.Builder()
                    .scheme("https")
                    .host("http://certune.link/songs/")
                    .addQueryParameter("useremail", userEmail)
                    .addQueryParameter("songname", songName)
                    .build();

//            RequestBody reqBody = new FormBody.Builder()
//                    .add("useremail", userEmail)
//                    .add("songname", songName)
//                    .build();

            Request request = new Request.Builder()
                    .url(httpUrl)
                    .build();

            // enqueue(): 비동기식, execute(): 동기식
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                String resMessage = Objects.requireNonNull(response.body()).string();
                Log.v("[responseBody] -> ", resMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}