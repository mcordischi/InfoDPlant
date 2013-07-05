package com.infodplant.process;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.graphics.Bitmap.Config;

/**
 * Created by marto on 6/30/13.
 */
public class ImageProcessor extends AsyncTask<String, Void, String> {


    Imgproc processor = new Imgproc();

    @Override
    protected String doInBackground(String... strings) {
        //TODO use the OpenCV service and then compress the picture to a bit array.
        Mat img = loadImage(strings[0]);
        processImage(img);
        saveImage(img,strings[0]);
        return null;
    }

    /**
     * Loads an image from external storage and saves it in a OpenCv Mat
     */
    protected Mat loadImage(String name){
        //TODO Testing
        Bitmap bitmap = BitmapFactory.decodeFile(name);
        Mat img = new Mat();
        Utils.bitmapToMat(bitmap,img);
        return img;
    }

    /**
     * Process the Mat.
     * @param img
     */
    protected void processImage(Mat img){
        //TODO
    }


    /**
     * Saves the image to the external storage with the given file name.
     * @param img
     * @param name
     */
    protected void saveImage(Mat img, String name){
        //TODO test
        int w = img.width();
        int h = img.height();
        Bitmap bitmap = Bitmap.createBitmap(w,h, Config.ALPHA_8);
        Utils.matToBitmap(img,bitmap);
        try {
            FileOutputStream out = new FileOutputStream(name);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
