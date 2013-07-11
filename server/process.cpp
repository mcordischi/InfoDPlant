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



double circleFill(Mat img, Point center, int radius )
{
	Mat maskImg = Mat::zeros(img.size().height, img.size().width, img.type());
	Mat resultImg = Mat::zeros(img.size().height, img.size().width, img.type());
	circle(maskImg, center, radius, Scalar(255, 255, 255), -1);
	img.copyTo(resultImg, maskImg);
	int total = countNonZero(maskImg);
	int filled = countNonZero(resultImg);
	return filled / (double)total;
}

double curvature(vector<Point> contour)
{
    double acc = 0;
    int n = 0;
    
    RotatedRect rotArea = minAreaRect(contour);
    int R = rotArea.size.height * rotArea.size.width;
    
    Rect bbox = boundingRect(contour);
    
    Mat img = Mat::zeros(bbox.size().height*3, bbox.size().width*3, CV_8UC1);
    vector<vector<Point> > contours;
    contours.push_back(contour);
    drawContours( img(Rect(0,0,bbox.size().height, bbox.size().width)), contours, 0, Scalar(255,255,255), -1);
    
	for(vector<Point>::iterator it = contour.begin(); it != contour.end(); ++it)
    {
        double s = R*0.1;
        for(double r = .1; r <= R;r+=s)
        {
            acc+= circleFill(img, *it, (int)r);
            n++;
        }
    }
    return acc/(double)n;
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