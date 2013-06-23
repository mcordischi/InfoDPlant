// Interface Class for extration of features
#include<cv.h>

using namespace cv;


class FeatureExtractor {

    public:
        virtual float extractFeature(Mat* image) = 0 ;
};
