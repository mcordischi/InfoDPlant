package com.infodplant;

import android.app.Application;
import android.graphics.Bitmap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

/**
 * Created by marto on 11/5/13.
 */
public class InfoApp extends Application {


    private static InfoApp singleton; //Application is a singleton


    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

    public InfoApp getInstance(){
        return singleton;
    }

    /**
     * Global Variables
     */
    private Bitmap originalImage;
    private MatOfPoint contourMat;
    private Bitmap contourImage;

    public void setImage(Bitmap image){
        originalImage = Bitmap.createBitmap(image);
    }

    public Bitmap getImage(){
        return originalImage;
    }

    public void setContourMat(MatOfPoint mat){
            contourMat = mat;
    }

    public MatOfPoint getContourMat(){
        return contourMat;
    }

    public void setContourImage(Bitmap image){
        contourImage = Bitmap.createBitmap(image);
    }

    public Bitmap getContourImage(){
        return contourImage;
    }

}
