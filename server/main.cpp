#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>
#include <stdio.h>
#include <stdlib.h>
#include "process.h"
#include "server.h"
using namespace cv;
using namespace std;

char* proc(char * img)
{
	char * str = (char*)malloc(400);
	Mat src = imread( string("test.bmp"), 0 );
	vector<Point> contour = bigestContour(src);
	
	double c = circularity( contour );
	double r = rectangularity( contour );
	double v = curvature( contour );
	
	sprintf(str, "Circularity: %f\nRectangularity: %f\nCurvature: %f\n",c,r,v);
	
	return str;
}

int main( int argc, char** argv )
{
	//listen(&proc);
	
	printf(proc(""));
	
	return(0);
}
