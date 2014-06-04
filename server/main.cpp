#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>
#include <stdio.h>
#include <stdlib.h>
#include "process.h"
#include "utils.h"
#include "server.h"
#include "naivebayesclassifier.h"
#include "databasegen.h"
using namespace cv;
using namespace std;

distribution d;

char * proc(char * img)
{
	//Mat src = imread( string("test.bmp"), 0 );
    cv::vector<Point> contour = decodePointVector(img); //bigestContour(src);
	
    double test[4] = {0, circularity( contour ), rectangularity( contour ), curvature( contour ) };

    /*
	double c = circularity( contour );
	double r = rectangularity( contour );
	double v = curvature( contour );
    */

    unsigned int type = naiveBayesClassify(d.distribution, &test[0], 4, d.kinds);

    /*
    char * str = (char*) malloc(sizeof("Circularity: %f<br>\nRectangularity: %f<br>\nCurvature: %f<br>\r\n") + 30);
	sprintf(str, "Circularity: %f<br>\nRectangularity: %f<br>\nCurvature: %f<br>\r\n",c,r,v);
    */

    char * str = (char*) malloc(sizeof("Sample is of type N° %i<br>\r\n") + 30);
    sprintf(str, "Sample is of type N° %i<br>\r\n",type);

	return str;
}

#include "databasegen.h"

int main( int argc, char** argv )
{
    if(argc>3 && ( strcmp(argv[1], "--storedatabase") || strcmp(argv[1], "-s") ) )
    {
        cout << "La operación puede tardar varios minutos (aprox 30'xImg en core2)" << endl;
        d = getDistributionFromFile(argv[1]);
        genDistributionFromDataset(argv[2]);
        storeDistribution(d, argv[3]);
        if(argc>4 && strcmp(argv[4], "-r"))
            listen(&proc);
    }
    else if(argc>2)
    {
        d = getDistributionFromFile(argv[1]);
        listen(&proc);
    }
    else
    {
        // TODO: explain usage
        cout << "Usage:" << endl;
        cout << "\tserver <distribution_file_in>" << endl;
        cout << "\tserver -s <dataset_config_file_in> <distribution_file_out> [-r]" << endl;
    }
	return(0);
}
