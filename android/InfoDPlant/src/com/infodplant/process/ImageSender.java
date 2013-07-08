package com.infodplant.process;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.infodplant.image.AlbumStorageDirFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sends the processed picture to the server, returns the callback from the server (the result).
 * Created by marto on 6/30/13.
 */
public class ImageSender extends AsyncTask<String, Void, String>{
    //TODO Use a ProgressValue

    byte[] byteArray;
    String url;
    DefaultHttpClient client;

    public static String appName = "InfoDPlant";

    public ImageSender(byte[] byteArray, String url){
        this.byteArray = byteArray;
        this.url = url;
        DefaultHttpClient client = new DefaultHttpClient();
    }

    @Override
    protected String doInBackground(String... strings) {
        //TODO send compressed image to server, wait for a answer.
        saveBitmap();
//        sendImage();
//        return getResponse();
        return null;
    }


    protected void saveBitmap(){
        File album = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), appName);
        if (!album.mkdirs()) {
            Log.i(appName, "Directory not created");
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
        OutputStream fOut = null;
        File file = new File(album, "Image.png");
        Log.i(appName,"Album" + album.getAbsolutePath());
        Log.i(appName,"File:" + file.getAbsolutePath());
        try {
            fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fOut);
            fOut.flush();
            fOut.close();

        } catch (FileNotFoundException e) {
            Log.e(appName,"File not found");
        } catch (IOException e){
            Log.e(appName,"I/O Exception");
        }
    }


    protected void sendImage(){
        HttpPost httppost = new HttpPost(url);

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


    protected String getResponse(){
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
        return response;
    }
}
