package com.infodplant.process.imageprocessor;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by marto on 5/19/14.
 */
public class ClassicThresholdImageProcessor extends ImageProcessor {

    protected static final double IN_RANGE_LOWER_BOUD = 60;
    protected static final double IN_RANGE_UPPER_BOUD = 30;
    protected static final double DEFAULT_THRESH_VALUE = 130;

    //Threshold BaseLine
    protected double thresh = 999999;

    //pre-processed and processed Mat
    Mat currentFrameGray;


    // The max threshold value accepted
    protected static final double THRESHOLD_HIGH_LIMIT = 200;


    @Override
    public void initProcessor(Size s){
        super.initProcessor(s);
        currentFrameGray = new Mat ((int)previewSize.width, (int)previewSize.height, CvType.CV_8UC1);
    }

    @Override
    public Mat processMat(Mat currentFrame) {
        if (thresh > THRESHOLD_HIGH_LIMIT)
            return currentFrame;
        return forcedProcess(currentFrame);
    }

    @Override
    public boolean onSelectedPoint(Mat currentFrame, Rect roiRect) {
        Log.i("InfoDPlant", "Changing threshold Value");

        Imgproc.cvtColor(currentFrame, currentFrameGray, Imgproc.COLOR_RGB2GRAY);

        Mat roi = currentFrameGray.submat(roiRect);
        // Calculate the mean value of the the ROI matrix
        Scalar sumColor = Core.mean(roi);
        double[] sumColorValues = sumColor.val; //use channel 1
        if (sumColorValues[0] > THRESHOLD_HIGH_LIMIT) { thresh = 99999; return false;}
        thresh = sumColorValues[0] ;//+ THRESHOLD_UPPER_BOUND;
        Log.i("InfoDPlant","Thresh val: " + thresh);
        return true;
    }

    @Override
    public ArrayList<MatOfPoint> getContours(Mat currentFrame) {
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(forcedProcess(currentFrame),contours,new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    /**
     * Process the Mat with default or changed parameters.
     * @param currentFrame
     * @return
     */
    protected Mat forcedProcess(Mat currentFrame){
        thresh = (thresh > THRESHOLD_HIGH_LIMIT) ? DEFAULT_THRESH_VALUE : thresh;
        Imgproc.cvtColor(currentFrame, currentFrameGray, Imgproc.COLOR_RGB2GRAY);
        //Inverted values
        double threshLow = 255 - Math.min(thresh+ IN_RANGE_LOWER_BOUD,255);
        double threshHigh = 255 - Math.max(thresh- IN_RANGE_UPPER_BOUD,0) ;
        Log.i("InfoDPlant","InRange val: " + threshLow + "," +
                threshHigh);
        Imgproc.threshold(currentFrameGray, currentFrameGray,threshLow,threshHigh, Imgproc.THRESH_BINARY);
        return currentFrameGray;
    }
}
