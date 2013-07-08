package com.infodplant.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.infodplant.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

/**
 * @deprecated
 * Previews the camera. When the user taps, the fiter is applied, when s/he double taps, it calls
 * the PlantInformationActivity with the current picture
 *
 * Created by marto on 6/30/13.
 */
public class CameraPreviewActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2,
        View.OnTouchListener, GestureDetector.OnDoubleTapListener{
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean              mIsJavaCamera = true;
    private MenuItem             mItemSwitchCamera = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public CameraPreviewActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.surface_view);

        if (mIsJavaCamera)
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        else
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemSwitchCamera = menu.add("Toggle Native/Java camera");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String toastMesage = new String();
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemSwitchCamera) {
            mOpenCvCameraView.setVisibility(SurfaceView.GONE);
            mIsJavaCamera = !mIsJavaCamera;

            if (mIsJavaCamera) {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
                toastMesage = "Java Camera";
            } else {
                mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_native_surface_view);
                toastMesage = "Native Camera";
            }

            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.enableView();
            Toast toast = Toast.makeText(this, toastMesage, Toast.LENGTH_LONG);
            toast.show();
        }

        return true;
    }

    public void onCameraViewStarted(int width, int height) {
        //TODO
    }

    public void onCameraViewStopped() {
        //TODO
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //TODO
        return inputFrame.rgba();
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent motionEvent) {
        //TODO
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent motionEvent) {
        //TODO
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent motionEvent) {
        //TODO
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //TODO
        return false;
    }
}
