package com.infodplant.process.imageprocessor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by marto on 5/19/14.
 */
public class CannyImageProcessor extends ClassicThresholdImageProcessor {
    /**
     * Process the Mat with default or changed parameters.
     * @param currentFrame
     * @return
     */
    @Override
    protected Mat forcedProcess(Mat currentFrame){
        thresh = (thresh > THRESHOLD_HIGH_LIMIT) ? DEFAULT_THRESH_VALUE : thresh;
        Imgproc.cvtColor(currentFrame, currentFrameGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(currentFrameGray, currentFrameGray, thresh - IN_RANGE_LOWER_BOUD, thresh + IN_RANGE_UPPER_BOUD);
        return currentFrameGray;
    }


    @Override
    public ArrayList<MatOfPoint> getContours(Mat currentFrame) {
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();

        forcedProcess(currentFrame);
        //TODO test
        //morphological opening (remove small objects from the foreground)
        Imgproc.erode(currentFrameGray, currentFrameGray, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE,SMALL_SIZE) );
        Imgproc.dilate( currentFrameGray, currentFrameGray, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );

        //morphological closing (fill small holes in the foreground)
        Imgproc.dilate( currentFrameGray, currentFrameGray, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );
        Imgproc.erode(currentFrameGray, currentFrameGray, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, SMALL_SIZE) );


        Imgproc.findContours(currentFrameGray,contours,new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        return contours;
    }
}
