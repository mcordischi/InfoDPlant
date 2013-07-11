#ifndef __PROCESS_H_
#define __PROCESS_H_

#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>
using namespace cv;

extern vector<Point> bigestContour(Mat img);

extern double circularity(vector<Point>); 
extern double rectangularity(vector<Point>);
extern double curvature(vector<Point> contour);

#endif