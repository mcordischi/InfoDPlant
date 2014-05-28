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
    protected static Scalar lowerAllowedRange = new Scalar(0,40,00);
    protected static Scalar upperAllowedRange = new Scalar(179,255,255);
    protected static Scalar defaultLowerInRange = new Scalar(20,50,20);
    protected static Scalar defaultUpperInRange = new Scalar(179,255,255);
    protected static Scalar lowerBoundRange = new Scalar(50,80,80);
    protected static Scalar upperBoundRange = new Scalar(50,150,150);

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
        Imgproc.cvtColor(currentFrame, currentFrameHSV, Imgproc.COLOR_RGB2HSV);
        return forcedProcess(currentFrame);
    }

    @Override
    public boolean onSelectedPoint(Mat currentFrame, Rect roiRect) {
        Log.i("InfoDPlant", "Changing inRange Value");
        Imgproc.cvtColor(currentFrame, currentFrameHSV, Imgproc.COLOR_RGB2HSV);

        Mat roi = currentFrameHSV.submat(roiRect);

        // Calculate the mean value of the the ROI matrix
        Scalar sumColor = Core.mean(roi);
        double[] sumColorValues = sumColor.val;

        Log.i("InfoDPlant", "Touch val= " + sumColorValues[0] + "," + sumColorValues[1] + ","+ sumColorValues[2]);

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
        upperInRange.set(new double[]{Math.min(sumColorValues[0] + upperBoundRange.val[0],upperAllowedRange.val[0]),
                Math.min(sumColorValues[1] +  upperBoundRange.val[1],upperAllowedRange.val[1]),
                Math.min(sumColorValues[2] +  upperBoundRange.val[2], upperAllowedRange.val[2])});


        lowerInRange.set(new double[]{Math.max(sumColorValues[0] - lowerBoundRange.val[0],lowerAllowedRange.val[0]),
                Math.max(sumColorValues[1] - lowerBoundRange.val[1],lowerAllowedRange.val[1]),
                        Math.max(sumColorValues[2] - lowerBoundRange.val[2],lowerAllowedRange.val[2])});

        Log.i("InfoDPlant", "Lower= " + lowerInRange.val[0] + "," + lowerInRange.val[1] + ","+ lowerInRange.val[2] + ",");
        Log.i("InfoDPlant", "Upper= " + upperInRange.val[0] + "," + upperInRange.val[1] + ","+ upperInRange.val[2] + ",");


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

        Core.inRange(currentFrameHSV, low, up, imgThresholded);

        Log.i("InfoDPlant", "HSV= " + low + "," + up);
        //morphological opening (remove small objects from the foreground)
        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,SMALL_SIZE) );
        Imgproc.dilate( imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );

        //morphological closing (fill small holes in the foreground)
        Imgproc.dilate( imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );
        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );


        return imgThresholded;
    }
}
