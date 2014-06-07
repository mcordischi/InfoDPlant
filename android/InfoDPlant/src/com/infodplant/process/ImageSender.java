package com.infodplant.process;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;


import com.infodplant.InfoApp;
import com.infodplant.activity.PlantInfoActivity;
import com.infodplant.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Sends the processed picture to the server, returns the callback from the server (the result).
 * The result is a Pair of Strings. The first is the Specie of the leaf, the second one is the
 * wikipedia entry
 * Created by marto on 6/30/13.
 */
public class ImageSender extends AsyncTask<String, Void, Pair<String,String>>{

    String url;
    DefaultHttpClient client;
    PlantInfoActivity requester;
    InfoApp app;

    public static String appName = "InfoDPlant";

    public ImageSender(InfoApp app, PlantInfoActivity requester,String url){
        this.app = app;
        this.requester = requester;
        this.url = url;
        client = new DefaultHttpClient();
    }

    @Override
    protected Pair<String,String> doInBackground(String... strings) {
        sendContour();
        return getResponse();
    }

    /**
     * Sets the plant name. TODO, add the wikipedia link
     * @param result the plant name
     */
    @Override
    protected void onPostExecute(Pair<String,String> result){
        requester.setPlantInformation(result);
    }


    /**
     * Sends the Image to the server
     */
    protected void sendImage(){
        HttpPost httppost = new HttpPost(url);


        Bitmap bmp = app.getImage();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        try{
            List<NameValuePair> parameter = new ArrayList<NameValuePair>(1);
            parameter.add(new BasicNameValuePair("img",new String(byteArray)) );
            httppost.setEntity(new UrlEncodedFormEntity(parameter));

            //Send
            HttpResponse httpResponse = client.execute(httppost);


        } catch (ClientProtocolException e) {
            Log.e(appName, "Client Protocol Exception");
        } catch (IOException e) {
            Log.e(appName, "I/O Exception sending message");
        }
    }

    /**
     * Sends the contour to the server
     */
    protected void sendContour(){
        MatOfPoint contourImage = app.getContourMat();
        Point[] points = contourImage.toArray();
        char[] encodedPoints = new char[points.length*2];
        for (int p = 0 ; p<points.length;p++){
            encodedPoints[2*p]= Utils.encode64((int)points[p].x);
            encodedPoints[(2*p)+1]= Utils.encode64((int)points[p].y);
        }

        HttpPost httppost = new HttpPost(url);
        try{
            List<NameValuePair> parameters = new ArrayList<NameValuePair>(1);
            parameters.add(new BasicNameValuePair("contour",new String(encodedPoints)) );
            httppost.setEntity(new UrlEncodedFormEntity(parameters));

            //Send
            HttpResponse httpResponse = client.execute(httppost);


        } catch (ClientProtocolException e) {
            Log.e(appName, "Client Protocol Exception");
        } catch (IOException e) {
            Log.e(appName, "I/O Exception sending message");
        }
    }


    /**
     * Waits for a response from the server
     * @return the response
     */
    protected Pair<String,String> getResponse(){
        String response = "";
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse execute = client.execute(httpGet);
            InputStream content = execute.getEntity().getContent();

            BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
            String s = "";
            while ((s = buffer.readLine()) != null) {
                response += s;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Pair<String,String> result = new Pair<String, String>(response,"POTUS");
        return result;
    }
}
