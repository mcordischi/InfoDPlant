#ifndef __PROCESS_H_
#define __PROCESS_H_

#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>

cv::vector<cv::Point> bigestContour(cv::Mat);

double circularity(cv::vector<cv::Point>);
double rectangularity(cv::vector<cv::Point>);
double curvature(cv::vector<cv::Point>);

#endif
