#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>
#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include "process.h"
using namespace cv;
using namespace std;


int main( int argc, char** argv )
{
	Mat src = imread( string("test.bmp"), 0 );
	vector<Point> contour = bigestContour(src);
	
	cout << "Circularity: " << circularity(contourArea(contour), arcLength(contour, 1)) << endl;
	
	waitKey(0);
	return(0);
}
