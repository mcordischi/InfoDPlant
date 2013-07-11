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

void circleFill(Mat img, Point center, int radius, int & total, int & filled )
{
	Mat maskImg = Mat::zeros(img.size().height, img.size().width, img.type());
	Mat resultImg = Mat::zeros(img.size().height, img.size().width, img.type());
	circle(maskImg, center, radius, Scalar(255, 255, 255), -1);
	img.copyTo(resultImg, maskImg);
	total = countNonZero(maskImg);
	filled = countNonZero(resultImg);
}

double rectangularity(vector<Point> contour)
{
	int Area = contourArea(contour);
	RotatedRect bbox = minAreaRect(contour);
	double bboxArea = bbox.size.height * bbox.size.width; 
	return Area / bboxArea;
}

double circularity(vector<Point> contour)
{
	int Area = contourArea(contour);
	int perimeter = arcLength(contour, 1);
	double perimeterSqr = perimeter * perimeter;

	return (4*PI*Area)/perimeterSqr;
}