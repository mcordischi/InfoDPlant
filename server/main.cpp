#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>
#include <stdio.h>
#include <stdlib.h>
#include "process.h"
#include "utils.h"
#include "server.h"
using namespace cv;
using namespace std;

char * proc(char * img)
{
	//Mat src = imread( string("test.bmp"), 0 );
	vector<Point> contour = decodePointVector(char * img); //bigestContour(src);
	
	double c = circularity( contour );
	double r = rectangularity( contour );
	double v = curvature( contour );
	
	char * str = (char*) malloc(sizeof("Circularity: %f<br>\nRectangularity: %f<br>\nCurvature: %f<br>\r\n") + 30);
	sprintf(str, "Circularity: %f<br>\nRectangularity: %f<br>\nCurvature: %f<br>\r\n",c,r,v);
	
	return str;
}

int main( int argc, char** argv )
{
	listen(&proc);
	
	return(0);
}
