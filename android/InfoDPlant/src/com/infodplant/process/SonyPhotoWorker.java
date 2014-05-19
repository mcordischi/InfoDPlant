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

import android.graphics.Bitmap;
import android.util.Log;

import com.infodplant.process.imageprocessor.CannyImageProcessor;
import com.infodplant.process.imageprocessor.ClassicThresholdImageProcessor;
import com.infodplant.process.imageprocessor.ImageProcessor;
import com.infodplant.process.imageprocessor.RGBImageProcessor;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Erik Hellman <erik.hellman@sonymobile.com>
 *     Edited by Marto
 */
public class SonyPhotoWorker implements Runnable {
    public static final String TAG = "OpenCVWorker";
    public boolean debug = false;

    public static final int FIRST_CAMERA = 0;
    public static final int SECOND_CAMERA = 1;

    public static final int RESULT_MATRIX_BUFFER_SIZE = 3;

    //In range function
    private Scalar lowerInRange = new Scalar(0,0,0);
    private Scalar upperInRange = new Scalar(0,0,0);




    private static Scalar HSV_defaultLowerInRange = new Scalar(45,0,45);
    private static Scalar HSV_defaultUpperInRange = new Scalar(220,255,220);
    private static Scalar HSV_lowerInRange = new Scalar(10,10,10);
    private static Scalar HSV_upperInRange = new Scalar(10,10,10);


    //ImageProcessorAlgorithm
    private ImageProcessor processor = new CannyImageProcessor();



    // Preview size
    private static int DEFAULT_PREVIEW_WIDTH = 720;
    private static int DEFAULT_PREVIEW_HEIGHT = 480;
    private Size mPreviewSize;

    //Minimun allowed Area Size
    private static double MIN_AREA = 0;

    /**
     * Booleans
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
    private Mat mThreshFrameResult;


    /**
     * Results related variables
     */
    boolean isContour;
    boolean isResultAvailable;
    boolean isImage;
    boolean isContourImage;

    private Mat contour;
    private Bitmap originalImage;
    private Bitmap contourImage;

    //Aux result related
    ArrayList<MatOfPoint> contours;
    int maxAreaIdx;


    private Point mSelectedPoint = null;




    public SonyPhotoWorker(int cameraId) {
        Log.i("InfoDPlant","SonyPhotoWorker Created");
        mCameraId = cameraId;
        // Default preview size
        mPreviewSize = new Size(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);
        isContour = false;
        isResultAvailable = false;
        isImage= false;
        isContourImage=false;
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
        boolean firstTime= true;
        if (mCamera != null) {
            VideoCapture camera = mCamera;
            mCamera = null; // Make it null before releasing...
            camera.release();
            firstTime = false;
        }

//      while (mCamera == null)
        mCamera = new VideoCapture(mCameraId);

        // Figure out the most appropriate preview size that this camera supports.
        // We always need to do this as each device support different preview sizes for their cameras
        //TODO Opencv Bug? sometimes getSupportedPreviewSizes returns error
        if (firstTime){
            List<Size> previewSizes = mCamera.getSupportedPreviewSizes();
            double largestPreviewSize = 720 * 480; // We should be smaller than this...
            double smallestWidth = 480; // Let's not get a smaller width than this...
            for (Size previewSize : previewSizes) {
                if (previewSize.area() < largestPreviewSize && previewSize.width >= smallestWidth) {
                    mPreviewSize = previewSize;
                }
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
        mThreshFrameResult = new Mat((int)mPreviewSize.width, (int)mPreviewSize.height,CvType.CV_8SC1);

        processor.initProcessor(mPreviewSize);
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
        Log.i("InfoDPlant","SonyPhotoWorker running");
        isResultAvailable = false;
        isImage= false;
        isContourImage=false;
        isContour = false;
        mDoProcess = true;
        Rect previewRect = new Rect(0, 0, (int) mPreviewSize.width, (int) mPreviewSize.height);


        setupCamera();

        initMatrices();

        while (mDoProcess && mCamera != null) {
            boolean grabbed = mCamera.grab();
            if (grabbed) {

                // Retrieve the next frame from the camera in RGB format
                mCamera.retrieve(mCurrentFrame, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGB);

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
                    processor.onSelectedPoint(mCurrentFrame,roiRect);
                    mSelectedPoint = null;
                }

                mThreshFrameResult = processor.processMat(mCurrentFrame);
                notifyResultCallback(mThreshFrameResult);

                if (debug){
                    //fps = measureFps();
                    //notifyFpsResult(fps);
                }
            }
        }

        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }

    }

    /**
     * Processes the information in order to give results.
     * This method must be called after {@link #stopProcessing()}, and before
     * trying to get the images and contours, otherwise they will return false.
     *
     */
    public boolean processResults(){
        if(isContour = processContour())
            isContourImage = processContourImage();
        isImage = processOriginalImage();
        isResultAvailable = isContour && isContourImage && isImage;
        return isResultAvailable;
    }

    /**
     * @return A bitmap with the contour
     */
    private boolean processContourImage(){
        if (!isContour) return false;
        int w = (int)mPreviewSize.width;
        int h = (int)mPreviewSize.height;
        Mat auxMat = new Mat(h, w, CvType.CV_8UC1);
        Scalar contourColor = new Scalar(127,127,127);
        for (int i = 0 ; i<contours.size();i++)
            if (i!= maxAreaIdx)
                    Imgproc.drawContours(auxMat,contours,i,contourColor,-1);
        contourColor = new Scalar(255,255,255);
        Imgproc.drawContours(auxMat,contours,maxAreaIdx,contourColor,2);
        contourImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(auxMat,contourImage);
        return true;
    }

    private boolean processOriginalImage(){
        int w = mCurrentFrame.width();
        int h = mCurrentFrame.height();
        originalImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mCurrentFrame,originalImage);
        return true;
    }



    /**
     * Analize the filtered image's contour.
     * @return the biggest contour
     */
    private boolean processContour(){
        if (mCurrentFrame ==null ) {
            Log.e("InfoDPlant","No Frame to process!");
            return false;
        }
        contours = processor.getContours(mCurrentFrame);


//        Imgproc.findContours(mThreshFrameResult,contours,hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double maxArea = -1;
        maxAreaIdx = -1;
        if (contours.size()==0) {
            Log.i("InfoDPlant","Contour not found");
            return false;
        }
        double contourArea = 0;
        for (int idx = 0; idx < contours.size(); idx++) {
            Mat contour = contours.get(idx);
            contourArea = Imgproc.contourArea(contour);
            if (contourArea > maxArea) {
                maxArea = contourArea;
                maxAreaIdx = idx;
            }
        }
        if (contourArea < MIN_AREA) {
            Log.i("InfoDPlant","Contour size not allowed :" + contourArea);
            return false;
        }

        contour = contours.get(maxAreaIdx);
        return true;
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



    public Size getPreviewSize() {
        return mPreviewSize;
    }

    public interface ResultCallback {
        void onResultMatrixReady(Bitmap mat);
    }

    public Mat getContour(){
        if(isContour) return contour;
        return null;
    }

    public Bitmap getContourImage(){
        if(isContourImage) return contourImage;
        return null;
    }

    public Bitmap getImage(){
        if(isImage) return originalImage;
        return null;
    }
}