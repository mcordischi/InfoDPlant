package com.infodplant.process;

import android.os.AsyncTask;
import android.util.Log;

import com.infodplant.activity.PlantInformationActivity;
import com.infodplant.image.ImageHandler;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * @deprecated
 * Responsible for processing the image. It runs in background
 * Created by marto on 6/30/13.
 */
public class ImageProcessor extends AsyncTask<String, Void, String> {

    private static String SUCCESS = "OK";
    private static String FAILURE = "OH, NO";

    ImageHandler imgHandler;
    PlantInformationActivity plantInformationActivity;

    public ImageProcessor(ImageHandler imgHandler, PlantInformationActivity plantInformationActivity){
        this.imgHandler = imgHandler;
        this.plantInformationActivity = plantInformationActivity;
    }

    @Override
    protected String doInBackground(String... strings) {
        //TODO use the OpenCV service and then compress the picture to a bit array.
        Mat img = imgHandler.getMat();
        processImage(img);
        imgHandler.saveImg(img);
        return SUCCESS;
    }


    @Override
    protected void onPostExecute(String result) {
       if (result.equals(SUCCESS))
            plantInformationActivity.setPic();
       else
           Log.d("InfoDPlant","I have no idea why i am here.");
    }


    /**
     * Process the Mat.
     * @param img The image to processs
     */
    protected void processImage(Mat img){
        //TODO choose the right process
        Imgproc.threshold(img,img,0.55,254.0,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C);
    }

}


