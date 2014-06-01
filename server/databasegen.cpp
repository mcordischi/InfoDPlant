#include <fstream>
#include <vector>
typedef struct
{
    double circ;
    double rect;
    double curv;
}
ImageInfo_ts;

ImageInfo_ts getAttributes(const char * imagePath);


void gendatabase(char * configFilePath, char * resultFilePath)
{
    //Load configfile
    std::ifstream configFileHandler(configFilePath);

    // Set output
    std::ofstream resultFileHandler(resultFilePath);

    unsigned int types_count;
    configFileHandler >> types_count;
    for(unsigned int types_iterator = 0; types_iterator < types_count; types_iterator++)
    {
        std::string typeName;
        configFileHandler >> typeName;
        unsigned int sample_count;
        configFileHandler >> sample_count;
        for(unsigned int sample_iterator = 0; sample_iterator < sample_count; sample_iterator++)
        {
            std::string imgFilePath;
            configFileHandler >> imgFilePath;
            ImageInfo_ts currentImageInfo = getAttributes(imgFilePath.c_str());
            resultFileHandler << typeName << ',' << currentImageInfo.circ << ',' << currentImageInfo.curv << ',' << currentImageInfo.rect << '\n';
        }
    }
    configFileHandler.close();
    resultFileHandler.close();
}

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <process.h>

// In process.h but not working?
double circularity(cv::vector<cv::Point>);
double rectangularity(cv::vector<cv::Point>);
double curvature(cv::vector<cv::Point>);
cv::vector<cv::Point> bigestContour(cv::Mat);

ImageInfo_ts getAttributes(const char * imagePath)
{
    cv::vector< cv::Point > contour = bigestContour( cv::imread( imagePath, CV_LOAD_IMAGE_GRAYSCALE ) );
    ImageInfo_ts ii;
    ii.circ = circularity(contour);
    ii.rect = rectangularity(contour);
    ii.curv = curvature(contour);
    return ii;
}
