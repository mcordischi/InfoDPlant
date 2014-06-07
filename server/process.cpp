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


#include <iostream>
#include <math.h>       /* isnan, sqrt */
double circleFill(Mat img, Point center, int radius )
{
	Mat maskImg = Mat::zeros(img.size().height, img.size().width, img.type());
	Mat resultImg = Mat::zeros(img.size().height, img.size().width, img.type());
	circle(maskImg, center, radius, Scalar(255, 255, 255), -1);
	img.copyTo(resultImg, maskImg);
	int total = countNonZero(maskImg);
    int filled = countNonZero(resultImg);
    if(std::isnan(filled)||std::isnan(total)||total==0)
        std::cout << "Filled: " << filled << "\nTotal: " << total << "\nRadius: " << radius << std::endl;
	return filled / (double)total;
}

#include <iostream>
double curvature(vector<Point> contour)
{
    double acc = 0;
    int n = 0;

    Rect bbox = boundingRect(contour);
    int offX = bbox.x+bbox.width;
    int offY = bbox.y+bbox.height;

    vector<Point> movedContour;
    for(vector<Point>::iterator it = contour.begin(); it != contour.end(); ++it)
        movedContour.push_back( Point( (*it).x+offX, (*it).y+offY));


    
    Mat img = Mat::zeros(Size((offX*3), (offY*3)), CV_8UC1);
    vector<vector<Point> > contours;
    contours.push_back(movedContour);
    drawContours( img, contours, 0, Scalar(255,255,255), -1);

    RotatedRect rotArea = minAreaRect(contour);
    int R = (rotArea.size.height + rotArea.size.width) / 2;
    double s = R*0.1;

    for(vector<Point>::iterator it = movedContour.begin(); it != movedContour.end(); ++it)
    {
        for(double r = 1; r <= R;r+=s)
        {
            acc+= circleFill(img, *it, (int)r);
            n++;
        }
    }
    return acc/(double)n;
}

double rectangularity(cv::vector<Point> contour)
{
	int Area = contourArea(contour);
	RotatedRect bbox = minAreaRect(contour);
	double bboxArea = bbox.size.height * bbox.size.width; 
	return Area / bboxArea;
}

double circularity(cv::vector<Point> contour)
{
    int Area = contourArea(contour);
    int perimeter = arcLength(contour, 1);
	double perimeterSqr = perimeter * perimeter;

	return (4*PI*Area)/perimeterSqr;
}
