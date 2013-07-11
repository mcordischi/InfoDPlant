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
	
	sprintf(str, "Circularity: %f<br>Rectangularity: %f<br>\r\n",c,r);
	
	return str;
}

int main( int argc, char** argv )
{
	//listen(&proc);
	
	
	Mat src = imread( string("test.bmp"), 0 );
	int count;
	int total;
	circleFill(src, Point(300,300), 200, total, count);
	printf("Total: %d\nCount: %d\n",total, count);
	return(0);
}
