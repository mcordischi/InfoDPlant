package com.infodplant.process;

/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Erik Hellman <erik.hellman@sonymobile.com>
 */
public class SonyPhotoWorker implements Runnable {
    public static final String TAG = "OpenCVWorker";

    public static final int FIRST_CAMERA = 0;
    public static final int SECOND_CAMERA = 1;

    public static final int RESULT_MATRIX_BUFFER_SIZE = 3;


    // The max threshold value accepted
    public static final double THRESHOLD_HIGH_LIMIT = 235;
    // Increasing the threshold
    public static final double THRESHOLD_UPPER_BOUND = 120;

    //Threshold BaseLine
    private double thresh = 999999;

    // Preview size
    private static int PREVIEW_WIDTH = 480;
    private static int PREVIEW_HEIGHT = 320;
    private Size mPreviewSize;

    /**
     * Boolean
     */
    private boolean mDoProcess;
    private int mCameraId = SECOND_CAMERA;
    private VideoCapture mCamera;
    private Set<ResultCallback> mResultCallbacks = Collections.synchronizedSet(new HashSet<ResultCallback>());
    private ConcurrentLinkedQueue<Bitmap> mResultBitmaps = new ConcurrentLinkedQueue<Bitmap>();

    /**
     * Matrices used to hold the actual image data for each processing step
     */
    private Mat mCurrentFrame;
    private Mat mFilteredFrame;
    private Mat mThreshFrameResult;
    private Mat mCurrentFrameGray;

    private long mPrevFrameTime;



    private Point mSelectedPoint = null;

    private Scalar mLowerColorLimit;
    private Scalar mUpperColorLimit;

    public SonyPhotoWorker(int cameraId) {
        mCameraId = cameraId;
        // Default preview size
        mPreviewSize = new Size(PREVIEW_WIDTH, PREVIEW_HEIGHT);
    }

    public void releaseResultBitmap(Bitmap bitmap) {
        mResultBitmaps.offer(bitmap);
    }

    public void addResultCallback(ResultCallback resultCallback) {
        mResultCallbacks.add(resultCallback);
    }

    public void removeResultCallback(ResultCallback resultCallback) {
        mResultCallbacks.remove(resultCallback);
    }

    public void stopProcessing() {
        mDoProcess = false;
    }

    // Setup the camera
    private void setupCamera() {
        if (mCamera != null) {
            VideoCapture camera = mCamera;
            mCamera = null; // Make it null before releasing...
            camera.release();
        }

        mCamera = new VideoCapture(mCameraId);

        // Figure out the most appropriate preview size that this camera supports.
        // We always need to do this as each device support different preview sizes for their cameras
        List<Size> previewSizes = mCamera.getSupportedPreviewSizes();
        double largestPreviewSize = 1280 * 720; // We should be smaller than this...
        double smallestWidth = 480; // Let's not get a smaller width than this...
        for (Size previewSize : previewSizes) {
            if (previewSize.area() < largestPreviewSize && previewSize.width >= smallestWidth) {
                mPreviewSize = previewSize;
            }
        }

        mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mPreviewSize.width);
        mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mPreviewSize.height);
    }

    /**
     * Initialize the matrices and the bitmaps we will use to draw the result
     */
    private void initMatrices() {
        mCurrentFrame = new Mat();
        mCurrentFrameGray = new Mat(PREVIEW_WIDTH, PREVIEW_HEIGHT,CvType.CV_8SC1);
        mFilteredFrame = new Mat();
        mThreshFrameResult = new Mat(PREVIEW_WIDTH, PREVIEW_HEIGHT,CvType.CV_8SC1);

        // Since drawing to screen occurs on a different thread than the processing,
        // we use a queue to handle the bitmaps we will draw to screen
        mResultBitmaps.clear();
        for (int i = 0; i < RESULT_MATRIX_BUFFER_SIZE; i++) {
            Bitmap resultBitmap = Bitmap.createBitmap((int) mPreviewSize.width, (int) mPreviewSize.height,
                    Bitmap.Config.ARGB_8888);
            mResultBitmaps.offer(resultBitmap);
        }
    }

    /**
     * The thread used to grab and process frames
     */
    @Override
    public void run() {
        mDoProcess = true;
        Rect previewRect = new Rect(0, 0, (int) mPreviewSize.width, (int) mPreviewSize.height);
        mPrevFrameTime = Core.getTickCount();

        setupCamera();

        initMatrices();

        while (mDoProcess && mCamera != null) {
            boolean grabbed = mCamera.grab();
            if (grabbed) {

                // Retrieve the next frame from the camera in RGB format
                mCamera.retrieve(mCurrentFrame, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);


                // Convert the RGB frame to HSV as it is a more appropriate format when calling Core.inRange
                Imgproc.cvtColor(mCurrentFrame, mCurrentFrameGray, Imgproc.COLOR_RGB2GRAY);
                //mCurrentFrame.copyTo(mCurrentFrameGray);

                // If we have selected a new point, get the color range and decide new threshold
                if (mSelectedPoint != null) {

                    // We check the colors in a 5x5 pixels square (Region Of Interest) and get the average from that
                    if (mSelectedPoint.x < 2) {
                        mSelectedPoint.x = 2;
                    } else if (mSelectedPoint.x >= (previewRect.width - 2)) {
                        mSelectedPoint.x = previewRect.width - 2;
                    }
                    if (mSelectedPoint.y < 2) {
                        mSelectedPoint.y = 2;
                    } else if (mSelectedPoint.y >= (previewRect.height - 2)) {
                        mSelectedPoint.y = previewRect.height - 2;
                    }

                    // ROI (Region Of Interest) is used to find the average value around the point we clicked.
                    // This will reduce the risk of getting "freak" values if the pixel where we clicked has an unexpected value
                    Rect roiRect = new Rect((int) (mSelectedPoint.x - 2), (int) (mSelectedPoint.y - 2), 5, 5);
                    // Get the Matrix representing the ROI
                    Mat roi = mCurrentFrameGray.submat(roiRect);
                    changeThresholdValue(roi);
                    mSelectedPoint = null;
                }

                // If we have selected thresh, apply the threshold
                if (thresh < THRESHOLD_HIGH_LIMIT) {
                    // Using the color limits to generate a mask (mThreshFrameResult)
//                    Core.inRange(mCurrentFrameGray, mLowerColorLimit, mUpperColorLimit, mThreshFrameResult);
                    double threshold = Imgproc.threshold(mCurrentFrameGray, mThreshFrameResult, thresh,
                            255.0, Imgproc.THRESH_BINARY);
                    // Clear (set to black) the filtered image frame


//                    mFilteredFrame.setTo(new Scalar(0, 0, 0));
                    // Copy the current frame in RGB to the filtered frame using the mask.
                    // Only the pixels in the mask will be copied.
//                    mCurrentFrame.copyTo(mFilteredFrame, mThreshFrameResult);

                    notifyResultCallback(mThreshFrameResult);
                } else {
                    notifyResultCallback(mCurrentFrame);
                }

//                fps = measureFps();
//                notifyFpsResult(fps);
            }
        }

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

    }


    /**
     * Changes the Threshold base line. More information in
     * http://docs.opencv.org/doc/tutorials/imgproc/threshold/threshold.html
     * @param roi the region of interest. The thresh value will be the mean of the roi, minus a constant
     */
    private void changeThresholdValue(Mat roi){
        // Calculate the mean value of the the ROI matrix
        Scalar sumColor = Core.mean(roi);
        double[] sumColorValues = sumColor.val;

//        Dunno why selsectedColor is used, So I commented it. If it doesn't work, revive this code
//        double[] selectedColor = mCurrentFrameGray.get((int) mSelectedPoint.x, (int) mSelectedPoint.y);
//        if (selectedColor != null) {

        //use channel 1
        thresh = sumColorValues[0] + THRESHOLD_UPPER_BOUND;
    }


    /**
     * @deprecated Why would you use the filtered image if you can get the contours? Call  {@link #getContour()}!
     * Returns the filtered Image in bitmap format
     * @return the image
     */
    public Bitmap getFiteredImage(){
        int w = mThreshFrameResult.width();
        int h = mThreshFrameResult.height();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8);
        Utils.matToBitmap(mThreshFrameResult,bitmap);
        return bitmap;
    }

    /**
     * Why get a bitmap instead of the List of points? Opencv's Point isw not serializable =(
     * Now you have 2 choices. Get the contour as a List of points with {@link #getContour()} and
     * serialize it in your own way, or take the list of poits as a image this this method.
     * @return A bitmap with the contour
     */
    public Bitmap getContourImage(){
        int w = PREVIEW_WIDTH;
        int h = PREVIEW_HEIGHT;
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        List<Point> contour = getContour();
        //Ugly code thanks to Openc - Handles disgusting classes
        List<MatOfPoint> contoursUgly = new ArrayList<MatOfPoint>(1);
        MatOfPoint matOfPoint = new MatOfPoint();
        matOfPoint.fromList(contour);
        contoursUgly.add(matOfPoint);

        Mat img = new Mat(PREVIEW_WIDTH, PREVIEW_HEIGHT,CvType.CV_8SC1);
        Imgproc.drawContours(img,contoursUgly,-1,new Scalar(255,255,255));
        Utils.matToBitmap(img,bitmap);
        return bitmap;
    }

    public Bitmap getOriginalImage(){
        int w = mCurrentFrame.width();
        int h = mCurrentFrame.height();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mCurrentFrame,bitmap);
        return bitmap;
    }


    /**
     * Analize the filtered image's contour.
     * @return the biggest contour
     */
    public List<Point> getContour(){
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat mat = new Mat();
        Imgproc.findContours(mThreshFrameResult,contours,mat, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = -1;
        int maxAreaIdx = -1;
        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            double contourArea = Imgproc.contourArea(contour);
            if (contourArea > maxArea) {
                maxArea = contourArea;
                maxAreaIdx = idx;
            }
        }

        return contours.get(maxAreaIdx).toList();
    }


    private void notifyResultCallback(Mat result) {
        Bitmap resultBitmap = mResultBitmaps.poll();
        if (resultBitmap != null) {
            Utils.matToBitmap(result, resultBitmap, true);
            for (ResultCallback resultCallback : mResultCallbacks) {
                resultCallback.onResultMatrixReady(resultBitmap);
            }
        }
    }

    public void setSelectedPoint(double x, double y) {
        mSelectedPoint = new Point(x, y);
    }

    /**
     * Resets the thresh value
     */
    public void clearSelectedColor() {
        thresh = 99999 ;
        mSelectedPoint = null;
    }

    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public interface ResultCallback {
        void onResultMatrixReady(Bitmap mat);
    }
}
