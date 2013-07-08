package com.infodplant.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.infodplant.R;
import com.infodplant.process.ImageSender;

/**
 * Created by marto on 7/7/13.
 */
public class PlantInfoActivity extends Activity {


    private Bitmap bitmap;
    private ImageSender imgSender;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra(SonyTouchActivity.BITMAP_MESSAGE);
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
 //       imgSender = new ImageSender(byteArray,getString(R.string.server_url));

        setContentView(R.layout.plant_info);

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
        mImageView.setImageBitmap(bitmap);
    }

}
