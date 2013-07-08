package com.infodplant.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.infodplant.R;
import com.infodplant.image.ImageHandler;
import com.infodplant.process.ImageProcessor;
import com.infodplant.process.ImageSender;

import java.io.File;
import java.io.IOException;

/**
 * @deprecated
 * Main Activity of the application. Calls a service to take a photo, uses OpenCV to process it and
 * uses a sender to send it to the server and receive an answer.
 *
 * Created by marto on 6/30/13.
 */
public class PlantInformationActivity extends Activity {

    private static final int TAKE_PICTURE_REQUEST = 1 ;

    protected Bitmap bitmap;

    protected ImageSender imgSender;

    public final ImageHandler imgHandler =
            new ImageHandler(getString(R.string.app_name),getString(R.string.album_name));

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        byte[] byteArray = getIntent().getByteArrayExtra(SonyTouchActivity.BITMAP_MESSAGE);
        imgSender = new ImageSender(byteArray,getString(R.string.server_url));

        bitmap = BitmapFactory.decodeByteArray(byteArray, 0 ,byteArray.length);

        setPic();

        setContentView(R.layout.main);
        // Moved to ImageHandler

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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
//            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
//        } else {
//            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
//        }
    }


    /**
     * Called when the user presses the "Take picture" button. Calls the default application
     * to take a picture and expects a result.
     */
    public void takePicture(View view){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f;
        int actionCode = TAKE_PICTURE_REQUEST;

        try {
            f = imgHandler.setUpPhotoFile();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
//            f = null;
//            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, actionCode);
    }


    /**
     * Called when the "process and send" button is pressed.
     * Uses the OpenCV service to do a histogram threshold.
     */
    public void prcImage(View view){
    //TODO
        ImageProcessor imgProc = new ImageProcessor(imgHandler,this);
        imgProc.execute(imgHandler.getPhotoPath());

        //Set the button to send image
        Button prcImageBut = (Button) findViewById(R.id.prcImage);
        prcImageBut.setText(R.string.sendImage);
        Button.OnClickListener mSendImageOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendImage();
            }
        };
        prcImageBut.setOnClickListener(mSendImageOnClick);
    }

    /**
     * Compresses the image and
     * sends it to the server. This work must be asynchronic, calling ImageSender AsyncTask.
     */
    public void sendImage(){
        //TODO
    }


    // Called when the Photo app returns the result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imgHandler.getPhotoPath() != null) {
            setPic();
            galleryAddPic();
            imgHandler.setPhotoPath(null);

            //Make visible the elements
            Button prcImageBut = (Button) findViewById(R.id.prcImage);
            prcImageBut.setVisibility(View.VISIBLE);
        }
    }


    public void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

        ImageView mImageView = (ImageView) findViewById(R.id.imageView);
		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

	    /* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);

    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(imgHandler.getPhotoPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }





}
