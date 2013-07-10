package com.infodplant.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.infodplant.R;
import com.infodplant.process.ImageSender;
import com.infodplant.process.SonyPhotoWorker;

import org.opencv.core.Point;

/**
 * Created by marto on 7/7/13.
 */
public class PlantInfoActivity extends Activity {


    private Bitmap originalImage;
    private ImageSender imgSender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plant_info);

        //Initiate the ImageSender
        imgSender = new ImageSender(this,getString(R.string.server_url));



        Intent intent = getIntent();
        //get the original image
        byte[] byteArray = intent.getByteArrayExtra(SonyTouchActivity.BITMAP_MESSAGE);
        originalImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);



        //get the countour Image
//        Bitmap contourImage;
//        byte[] byteArrayContourImage = intent.getByteArrayExtra(SonyTouchActivity.BITMAP_MESSAGE);
//        contourImage = BitmapFactory.decodeByteArray(byteArrayContourImage, 0, byteArrayContourImage.length);

        //Set the contourImage to send
//        imgSender.setByteArray(byteArrayContourImage);

//        //Sets a contour in the ImageSender
//        imgSender.setContour((Point[]) intent.getSerializableExtra(SonyTouchActivity.CONTOUR_MESSAGE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //Execute in parallel
            imgSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "http://url.com/image.png");
        }else
            imgSender.execute();




    }


    @Override
    public void onStart(){
        super.onStart();
        setPic();
    }


    /**
     *  Associates the bitmap to the ImageView
     */
    public void setPic() {
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageBitmap(originalImage);
    }

    /**
     * Called by the {@link ImageSender}, the method displays the plant information retrieved from
     * the server.
     * @param plantInfo a Pair of Strings with the leaf's name and an optional link to wikipedia
     */
    public void setPlantInformation(final Pair<String,String> plantInfo){
        TextView textView = (TextView)findViewById(R.id.plant_name);
            //If there has been an internal error
            if (plantInfo == null || plantInfo.first == null){
                textView.setText("Server Error");
            } else {
                textView.setText(plantInfo.first);
                //If there is a wikipedia entry
                if (plantInfo.second != null)
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String URL = getString(R.string.wikipedia_entry_url) + plantInfo.second;
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
                            startActivity(browserIntent);
                        }
                    });
            }
    }
}
