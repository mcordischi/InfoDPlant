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
public class HSVImageProcessor extends ImageProcessor {
    protected static Scalar lowerAllowedRange = new Scalar(20,20,20);
    protected static Scalar upperAllowedRange = new Scalar(235,235,235);
    protected static Scalar defaultLowerInRange = new Scalar(45,0,45);
    protected static Scalar defaultUpperInRange = new Scalar(220,255,220);
    protected static Scalar lowerBoundRange = new Scalar(65,85,65);
    protected static Scalar upperBoundRange = new Scalar(65,85,65);

    //In range function
    protected Scalar lowerInRange = new Scalar(0,0,0);
    protected Scalar upperInRange = new Scalar(0,0,0);

    Mat currentFrameHSV;

    boolean applyThreshold = false;

    @Override
    public void initProcessor(Size s){
        super.initProcessor(s);
        currentFrameHSV = new Mat((int)previewSize.width, (int)previewSize.height, CvType.CV_32SC1);
    }

    @Override
    public Mat processMat(Mat currentFrame) {
        if (!applyThreshold)
            return currentFrame;
        return forcedProcess(currentFrame);
    }

    @Override
    public boolean onSelectedPoint(Mat currentFrame, Rect roiRect) {
        Log.i("InfoDPlant", "Changing inRange Value");
        Mat roi = currentFrame.submat(roiRect);

        // Calculate the mean value of the the ROI matrix
        Scalar sumColor = Core.mean(roi);
        double[] sumColorValues = sumColor.val;

        //Accept new values?
        applyThreshold = true;
        for (int i = 0 ; i< 3 ; i++)
            if (sumColor.val[i] < lowerAllowedRange.val[i] || sumColor.val[i] > upperAllowedRange.val[i] ){
                applyThreshold = false;
                break;
            }
        if (!applyThreshold)
            return applyThreshold;


        //Change InRange values
        upperInRange.set(new double[]{sumColorValues[0] + upperBoundRange.val[0],
                sumColorValues[1] +  upperBoundRange.val[1],
                sumColorValues[2] +  upperBoundRange.val[2]});


        lowerInRange.set(new double[]{sumColorValues[0] - lowerBoundRange.val[0],
                sumColorValues[1] - lowerBoundRange.val[1],
                sumColorValues[2] - lowerBoundRange.val[2]});


        return applyThreshold;
    }

    @Override
    public ArrayList<MatOfPoint> getContours(Mat currentFrame) {
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(forcedProcess(currentFrame), contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }

    /**
     * Process the Mat with default or changed parameters.
     * @param currentFrame
     * @return
     */
    protected Mat forcedProcess(Mat currentFrame){
        Scalar low,up;
        if(applyThreshold){
            low = lowerInRange;
            up = upperInRange;
        } else {
            low = defaultLowerInRange;
            up = defaultUpperInRange;
        }

        Mat imgThresholded = new Mat();
        Core.inRange(currentFrame, low, up, imgThresholded);

        //morphological opening (remove small objects from the foreground)
        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,SMALL_SIZE) );
        Imgproc.dilate( imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );

        //morphological closing (fill small holes in the foreground)
        Imgproc.dilate( imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );
        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );


        return imgThresholded;
    }
}
