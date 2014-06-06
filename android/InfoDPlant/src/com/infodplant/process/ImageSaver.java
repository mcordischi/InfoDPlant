package com.infodplant.process;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by juli on 6/5/14.
 */
public class ImageSaver {

    //TODO improve saving method

    public boolean saveImage(ContentResolver cr,Bitmap originalImage){
        MediaStore.Images.Media.insertImage(cr, originalImage, "leaf" + getFileID() + ".jpg" , "infoDPlant");
        return true;
    }


    public boolean saveContour(ContentResolver cr,Bitmap contourImage){
        MediaStore.Images.Media.insertImage(cr, contourImage, "leaf_contour" + getFileID() + ".jpg" , "infoDPlant");
        return true;
    }



    /* Generates an unique ID for files*/
    private String getFileID(){
        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH_mm_ss");
        Date now = new Date();
        return formatter.format(now);
    }



    /*
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
            Imgproc.drawContours(img, matOfPointList, -1, new Scalar(255, 255, 255), -1);

            int w = img.width();
            int h = img.height();
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(img, bitmap);
            fileName = "ContourImage.png";
        }
        else{
            //There is a bitmap
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
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

    }*/
}
