package com.infodplant.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is the responsible for handling an image.
 * It contains all the methods for loading, creating and saving original an image
 * and its processed ones
 *
 * Created by marto on 7/1/13.
 */
public class ImageHandler {


//    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
//    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    protected Bitmap mImageBitmap;


    private String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private String appName;
    private String albumName;

    public ImageHandler(String appName, String albumName){
        this.appName = appName;
        this.albumName = albumName;

        //Change the album Storage dir if version < Froyo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        return File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d(appName, "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(appName, "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }


    /* Photo album for this application */
    private String getAlbumName() {
        return albumName;
    }

    public File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }


    public String getPhotoPath(){
        return mCurrentPhotoPath;
    }

    public void setPhotoPath(String mCurrentPhotoPath){
        this.mCurrentPhotoPath = mCurrentPhotoPath;
    }


    /**
     * Loads the image and returns a scaled version of it
     * @param width the width of the returning bitmap
     * @param height the width of the returning bitmap
     * @return a Bitmap containing the scaled image
     */
    public Bitmap getScaledBitmap(int width, int height){

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((width > 0) || (height> 0)) {
            scaleFactor = Math.min(photoW/width, photoH/height);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;
        mImageBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        return mImageBitmap;
    }


    /**
     * Loads the image and returns it in Mat Format
     * @return the image
     */
    public Mat getMat(){
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        Mat img = new Mat();
        Utils.bitmapToMat(bitmap, img);
        return img;
    }

    /**
     * Overwrites the image
     * @param img the picture to save
     */
    public void saveImg(Mat img){
        int w = img.width();
        int h = img.height();
        Bitmap bitmap = Bitmap.createBitmap(w,h, Bitmap.Config.ALPHA_8);
        Utils.matToBitmap(img,bitmap);
        try {
            FileOutputStream out = new FileOutputStream(mCurrentPhotoPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
