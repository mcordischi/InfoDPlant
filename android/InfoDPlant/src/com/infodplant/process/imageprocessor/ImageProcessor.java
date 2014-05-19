package com.infodplant.process.imageprocessor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import java.util.ArrayList;

/**
 * Created by marto on 5/19/14.
 */
public abstract class ImageProcessor {

    protected Size previewSize;

    public static Size SMALL_SIZE = new Size(5,5);

    /**
     * Sets the basic parameters
     * @param s preview size (size of mat)
     */
    public void initProcessor(Size s){
        previewSize = s.clone();
    }


    /**
     * Processes (or not) the current frame in order to display it
     * @param currentFrame a frame in RGB format, created by camera.retrieve(...);
     * @return a displayable Mat.
     */
    public abstract Mat processMat(Mat currentFrame);


    /**
     * Notifies a tap on a selected area.
     * @param currentFrame the frame that was tapped on
     * @param roiRect the tap area
     * @return whether the internal state changed
     */
    public abstract boolean onSelectedPoint(Mat currentFrame, Rect roiRect);

    /**
     * returns the contours of the given image
     * @param currentFrame the frame
     * @return a list of contours
     */
    public abstract ArrayList<MatOfPoint> getContours(Mat currentFrame);
}
