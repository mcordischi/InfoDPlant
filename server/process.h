#ifndef __PROCESS_H_
#define __PROCESS_H_

#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>
using namespace cv;

extern vector<Point> bigestContour(Mat img);

extern double circularity(vector<Point>); 
extern double rectangularity(vector<Point>);
extern int circleFill(Mat img, Point center, int radius , int & total, int & filled );

#endif