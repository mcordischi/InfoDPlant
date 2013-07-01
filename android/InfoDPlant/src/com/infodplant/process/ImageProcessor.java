package com.infodplant.process;

import android.os.AsyncTask;

import org.opencv.imgproc.Imgproc;

/**
 * Created by marto on 6/30/13.
 */
public class ImageProcessor extends AsyncTask<String, Void, String> {


    Imgproc processor = new Imgproc();

    @Override
    protected String doInBackground(String... strings) {
        //TODO use the OpenCV service and then compress the picture to a bit array.
        return null;
    }
}
