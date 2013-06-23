//Interface for filters
#include<cv.h>

using namespace cv;

class Filter {
    public:
        //Returns a new Mat with the result
        virtual Mat* filterN(Mat* input) = 0 ;
        //Apply the filter in the input Mat
        virtual void filter(Mat* input) = 0 ;
}
