package com.infodplant.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


import com.infodplant.R;
import com.infodplant.image.AlbumStorageDirFactory;
import com.infodplant.image.BaseAlbumDirFactory;
import com.infodplant.image.FroyoAlbumDirFactory;
import com.infodplant.image.ImageHandler;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Main Activity of the application. Calls a service to take a photo, uses OpenCV to process it and
 * uses a sender to send it to the server and receive an answer.
 *
 * Created by marto on 6/30/13.
 */
public class MainActivity extends Activity {

    private static final int TAKE_PICTURE_REQUEST = 1 ;

    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    private ImageView mImageView;
    private Bitmap mImageBitmap;


    private String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    public final ImageHandler imgHandler =
            new ImageHandler(getString(R.string.app_name),getString(R.string.album_name));

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        mImageView = (ImageView) findViewById(R.id.imageView);
//        mImageBitmap = null;

//        Button picBtn = (Button) findViewById(R.id.btnIntend);
//        setBtnListenerOrDisable(
//                picBtn,
//                mTakePicOnClickListener,
//                MediaStore.ACTION_IMAGE_CAPTURE
//        );
//
//        Button picSBtn = (Button) findViewById(R.id.btnIntendS);
//        setBtnListenerOrDisable(
//                picSBtn,
//                mTakePicSOnClickListener,
//                MediaStore.ACTION_IMAGE_CAPTURE
//        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }


    /**
     * Called when the user presses the "Take picture" button. Calls the default application
     * to take a picture and expects a result.
     * @param view
     */
    public void takePicture(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = null;
        int actionCode = TAKE_PICTURE_REQUEST;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, actionCode);
    }


    /**
     * Called when the "process and send" button is pressed.
     * Uses the OpenCV service to do a histogram threshold.
     * @param view
     */
    public void prcImage(View view){
    //TODO


        //Set the button to send image
        Button prcImageBut = (Button) findViewById(R.id.prcImage);
        prcImageBut.setText(R.string.sendImage);
        Button.OnClickListener mSendImageOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImage(view);
            }
        };
        prcImageBut.setOnClickListener(mSendImageOnClick);
    }

    /**
     * Compresses the image and
     * sends it to the server. This work must be asynchronic, calling ImageSender AsyncTask.
     */
    public void sendImage(View view){
        //TODO
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            mCurrentPhotoPath = null;

            //Make visible the elements
            Button prcImageBut = (Button) findViewById(R.id.prcImage);
            prcImageBut.setVisibility(View.VISIBLE);
        }
    }


    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);

    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }


    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("InfoDPlant", "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

}
