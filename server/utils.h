#ifndef __UTILS_H_
#define __UTILS_H_

#include <opencv2\opencv.hpp>

cv::vector<cv::Point> decodePointVector(char * input);
char * encodePointVector(cv::vector<cv::Point> * inputVector);

#endif
