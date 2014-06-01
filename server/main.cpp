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
    cv::vector<Point> contour = decodePointVector(img); //bigestContour(src);
	
	double c = circularity( contour );
	double r = rectangularity( contour );
	double v = curvature( contour );
	
	char * str = (char*) malloc(sizeof("Circularity: %f<br>\nRectangularity: %f<br>\nCurvature: %f<br>\r\n") + 30);
	sprintf(str, "Circularity: %f<br>\nRectangularity: %f<br>\nCurvature: %f<br>\r\n",c,r,v);
	
	return str;
}

#include "databasegen.h"

int main( int argc, char** argv )
{
    if(argc<2)
        listen(&proc);
    else if(argc>3 && ( strcmp(argv[1], "--gendatabase") || strcmp(argv[1], "-g") ) )
    {
        cout << "La operación puede tardar varios minutos (aprox 30'xImg en core2)" << endl;
        gendatabase(argv[2],argv[3]);
    }
    else
    {
        // TODO: show usage
        cout << "Usage" << endl;
    }
	return(0);
}
