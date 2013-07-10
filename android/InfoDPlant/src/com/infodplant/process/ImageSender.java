package com.infodplant.process;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import com.infodplant.activity.PlantInfoActivity;
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
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

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
 * The result is a Pair of Strings. The first is the Specie of the leaf, the second one is the
 * wikipedia entry
 * Created by marto on 6/30/13.
 */
public class ImageSender extends AsyncTask<String, Void, Pair<String,String>>{
    //TODO Use a ProgressValue

    byte[] byteArray;
    Point[] contour;
    String url;

    DefaultHttpClient client;
    PlantInfoActivity requester;

    public static String appName = "InfoDPlant";

    public ImageSender(PlantInfoActivity requester,String url){
        this.requester = requester;
        this.url = url;
        DefaultHttpClient client = new DefaultHttpClient();
    }

    @Override
    protected Pair<String,String> doInBackground(String... strings) {
        Pair<String,String> result;
        //TODO send compressed image to server, wait for a answer.
//        saveBitmap();
//        sendImage();
//        result = getResponse();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        result = new Pair<String, String>("Done","Potus");
        return result;
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
     * Saves the contour to a file, if there is no contour, saves the image
     * TODO sendit to SonyPhotoWorker
     */
    protected void saveBitmap(){
        Bitmap bitmap;
        String fileName;
        if (contour != null){
            //Ugly code! It creates a Mat (img) from the contour given
            Mat img = new Mat();
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint.fromArray(contour);
            List<MatOfPoint> matOfPointList = new ArrayList<MatOfPoint>(1);
            matOfPointList.add(matOfPoint);
            //Thickness<0 => draws the area bounded by the contours
            Imgproc.drawContours(img,matOfPointList,-1,new Scalar(255,255,255),-1);

            int w = img.width();
            int h = img.height();
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(img, bitmap);
            fileName = "ContourImage.png";
        }
        else{
            //There is a bitmap
            bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.length);
            fileName = "Image.png";
        }
        File album = new File (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), appName);
        if (!album.mkdirs()) {
            Log.i(appName, "Directory not created");
        }
        OutputStream fOut = null;
        File file = new File(album, fileName);
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

    /**
     * Sends the Image to the server
     */
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

    /**
     * Sends the contour to the server
     */
    protected void sendContour(){
        //TODO
    }


    /**
     * Waits for a response from the server
     * @return the response
     */
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

    /**
     * Adds a contour to the sending message. It the contour is set, the ImageSender will only
     * send the contour. If not, it will send the compressed image
     * @param contour
     */
    public void setContour(Point[] contour){
        this.contour = contour;
    }



    /**
     * Adds a bitmap in a byte array format
     */
    public void setByteArray(byte[] byteArray){
        this.byteArray = byteArray;
    }

}
