#include <opencv2\highgui\highgui.hpp>
#include <opencv2\opencv.hpp>

using namespace cv;

const double PI = 3.14159265359;

vector<Point> bigestContour(Mat img)
{
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;

	findContours( img, contours, hierarchy,	CV_RETR_LIST , CV_CHAIN_APPROX_SIMPLE );
	
	int bgArea = (img.size().height-3)*(img.size().width-3);
	
	int cnt = 0;
	int cntArea = 0;
	
	for(int idx = 0; idx >= 0; idx = hierarchy[idx][0] )
	{
		int thisArea = contourArea(contours[idx]);
		if(thisArea!=bgArea && thisArea > cntArea)
		{
			cntArea = thisArea;
			cnt = idx;
		}
	}
	
	return contours[cnt];
}

double circularity(int Area, int perimeter)
{
	double perimeterSqr = perimeter * perimeter;

	return (4*PI*Area)/perimeterSqr;
}