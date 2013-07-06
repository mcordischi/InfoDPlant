package com.infodplant.process;

import android.os.AsyncTask;

import com.infodplant.image.ImageHandler;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 * Responsible for processing the image. It runs in background
 * Created by marto on 6/30/13.
 */
public class ImageProcessor extends AsyncTask<String, Void, String> {



    ImageHandler imgHandler;


    public ImageProcessor(ImageHandler imgHandler){
        this.imgHandler = imgHandler;
    }

    @Override
    protected String doInBackground(String... strings) {
        //TODO use the OpenCV service and then compress the picture to a bit array.
        Mat img = imgHandler.getMat();
        processImage(img);
        imgHandler.saveImg(img);
        return null;
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


