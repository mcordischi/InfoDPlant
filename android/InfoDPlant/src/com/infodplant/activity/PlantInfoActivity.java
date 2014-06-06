package com.infodplant.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.infodplant.InfoApp;
import com.infodplant.R;
import com.infodplant.process.ImageSaver;
import com.infodplant.process.ImageSender;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by marto on 7/7/13.
 */
public class PlantInfoActivity extends Activity {



    //TODO move this away
    public static String appName = "InfoDPlant";

    private Bitmap originalImage;
    private Bitmap contourImage;
    private ImageSender imgSender;
    private ImageSaver imgSaver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_info);

        //Initiate the ImageSender
        imgSender = new ImageSender(((InfoApp)getApplication()),this,getString(R.string.server_url));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //Execute in parallel
            imgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://url.com/image.png");
        }else
            imgSender.execute();
    }


    @Override
    public void onStart(){
        super.onStart();
        setPics();
    }


    /**
     *  Associates the bitmap to the ImageView
     */
    public void setPics() {
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);
        originalImage = ((InfoApp)getApplication()).getImage();
        mImageView.setImageBitmap(originalImage);

        ImageView mContourView = (ImageView) findViewById(R.id.contourView);
        contourImage = ((InfoApp)getApplication()).getContourImage();
        mContourView.setImageBitmap(contourImage);
    }

    /**
     * Called by the {@link ImageSender}, the method displays the plant information retrieved from
     * the server.
     * @param plantInfo a Pair of Strings with the leaf's name and an optional link to wikipedia
     */
    public void setPlantInformation(final Pair<String,String> plantInfo){
        TextView textView = (TextView)findViewById(R.id.plant_name);
            //If there has been an internal error
            if (plantInfo == null || plantInfo.first == null){
                textView.setText("Server Error");
            } else {
                textView.setText(plantInfo.first);
                //If there is a wikipedia entry
                if (plantInfo.second != null)
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String URL = getString(R.string.wikipedia_entry_url) + plantInfo.second;
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                            startActivity(browserIntent);
                        }
                    });
            }
    }


    /* Saves in media original image*/
    public void onSaveImageClick(View view){
        Log.i(appName, "Writing in external storage");
        imgSaver.saveImage(getContentResolver(),originalImage);
        //MediaStore.Images.Media.insertImage(getContentResolver(), originalImage, "leaf" + getFileID() + ".jpg" , "infoDPlant");
    }

    /* Saves in media the contour as an image*/
    public void onSaveContourClick(View view){
        Log.i(appName, "Writing in external storage");
        imgSaver.saveContour(getContentResolver(),contourImage);
        //MediaStore.Images.Media.insertImage(getContentResolver(), contourImage, "leaf_contour" + getFileID() + ".jpg" , "infoDPlant");
    }


}
