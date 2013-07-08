package com.infodplant.process;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends the processed picture to the server, returns the callback from the server (the result).
 * Created by marto on 6/30/13.
 */
public class ImageSender extends AsyncTask<String, Void, String>{
    //TODO Use a ProgressValue

    byte[] byteArray;
    String url;

    public ImageSender(byte[] byteArray, String url){
        this.byteArray = byteArray;
        this.url = url;
    }

    @Override
    protected String doInBackground(String... strings) {
        //TODO send compressed image to server, wait for a answer.
        String response = "";
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);

        try{
            List<NameValuePair> parameter = new ArrayList<NameValuePair>(1);
            parameter.add(new BasicNameValuePair("img",new String(byteArray)) );
            httppost.setEntity(new UrlEncodedFormEntity(parameter));

            //Send
            HttpResponse httpResponse = client.execute(httppost);


        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

        return null;
    }
}
