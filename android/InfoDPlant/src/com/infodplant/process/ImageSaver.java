package com.infodplant.process;

import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.infodplant.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @deprecated - Use MediaStore instead
 * Created by marto on 16/01/14.
 *//*
public class ImageSaver {


    /**
     * Saves the image in a external storage directory
     * @param bitmap
     **//*
    public static void saveImage(Bitmap bitmap){
        Log.i(appName,"Writing in external storage");
        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "myImage_" + getFileID() + ".jpg" , appName);


        /*
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bos = null;
        int quality = 100;
        if (isExternalStorageWritable()){
            File pictureFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    + File.separator , "myImage_" + getFileID() + ".jpg");
            try {

                fileOutputStream = new FileOutputStream(pictureFile);
                bos = new BufferedOutputStream(fileOutputStream);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
                Log.i(appName,"Saved image: " + pictureFile.getAbsoluteFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        // ignore close error
                    }

            }
            }
        }
        else
            Log.e(appName, "ExternalStorage not writable");
    }


    /* Generates an unique ID for files*//*
    private static String getFileID(){
        SimpleDateFormat formatter = new SimpleDateFormat("MM_dd_HH_mm_ss");
        Date now = new Date();
        return formatter.format(now);
    }


    /* Checks if external storage is available for read and write *//*
        public static boolean isExternalStorageWritable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                return true;
            }
            return false;
        }

        /* Checks if external storage is available to at least read *//*
        public static boolean isExternalStorageReadable() {
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                return true;
            }
            return false;
        }
}
*/