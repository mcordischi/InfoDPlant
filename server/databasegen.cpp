#include <fstream>
#include <vector>
#include "databasegen.h"

typedef struct
{
    double circ;
    double rect;
    double curv;
}
ImageInfo_ts;

ImageInfo_ts getAttributes(const char * imagePath);


void storeDistribution(distribution d, char * resultFilePath)
{
    //Open file
    std::ofstream resultFileHandler(resultFilePath);

    resultFileHandler << d.kinds << " " << d.attributes;
    unsigned int values = d.kinds * d.attributes;
    for(int i = 0; i < values; i++)
        resultFileHandler << " " << d.distribution[i];

    // Close file
    resultFileHandler.close();
}

#include <cstdlib>


distribution getDistributionFromFile(char * distributionFilePath)
{
    distribution d;

    //Open file
    std::ifstream distributionFileHandler(distributionFilePath);

    distributionFileHandler >> d.kinds >> d.attributes;
    unsigned int values = d.kinds * d.attributes;
    d.distribution = (double*) malloc(values*sizeof(values));
    for(int i = 0; i < values; i++)
        distributionFileHandler >> d.distribution[i];

    // Close file
    distributionFileHandler.close();

    return d;
}

#include "naivebayesclassifier.h"

distribution genDistributionFromDataset(char * configFilePath)
{

    //Load configfile
    std::ifstream configFileHandler(configFilePath);

    unsigned int types_count;
    configFileHandler >> types_count;

    unsigned int set_count = 0;
    unsigned int training_set_max = 0;
    double * trainingSet = 0;

    for(unsigned int types_iterator = 0; types_iterator < types_count; types_iterator++)
    {
        std::string typeName;
        configFileHandler >> typeName;

        unsigned int sample_count;
        configFileHandler >> sample_count;

        unsigned int need = set_count + sample_count - 1;

        // Expand trainingSet
        if(need>training_set_max)
        {
            if(types_iterator < types_count/2 ) need = need / (types_iterator+1)*types_count;
            realloc((void*)trainingSet,need*4*sizeof(double));
            training_set_max = need;
        }

        for(unsigned int sample_iterator = 0; sample_iterator < sample_count; sample_iterator++)
        {
            std::string imgFilePath;
            configFileHandler >> imgFilePath;
            ImageInfo_ts currentImageInfo = getAttributes(imgFilePath.c_str());
            trainingSet[set_count*4] = types_iterator;
            trainingSet[set_count*4] = currentImageInfo.circ;
            trainingSet[set_count*4] = currentImageInfo.curv;
            trainingSet[set_count*4] = currentImageInfo.rect;
            set_count++;
        }
    }
    configFileHandler.close();

    // Compact training set
    realloc((void*)trainingSet,types_count*4*sizeof(double));

    distribution d;
    d.attributes = 4;
    d.kinds = types_count;
    generateDistribution(d.distribution, trainingSet, d.attributes, set_count, d.kinds);
    return d;
}

/* DEPRECATED */
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
            resultFileHandler << typeName << ' ' << types_iterator << ' ' << currentImageInfo.circ << ' ' << currentImageInfo.curv << ' ' << currentImageInfo.rect << '\n';
        }
    }
    configFileHandler.close();
    resultFileHandler.close();
}

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include "process.h"

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
