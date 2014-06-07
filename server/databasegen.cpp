#include <fstream>
#include <vector>
#include "databasegen.h"

typedef struct
{
    double type;
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

#include <iostream>

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

        unsigned int need = set_count + sample_count;
        // Expand trainingSet
        if(need>training_set_max)
        {
            if(types_iterator < types_count/2 ){ need = need / (types_iterator+1)*types_count;}
            trainingSet = (double*) realloc((void*)trainingSet,need*4*sizeof(double));
            training_set_max = need;
        }

        for(unsigned int sample_iterator = 0; sample_iterator < sample_count; sample_iterator++)
        {
            std::string imgFilePath;
            configFileHandler >> imgFilePath;
            ImageInfo_ts currentImageInfo = getAttributes(imgFilePath.c_str());
            std::cout << "Writing " << set_count << "/" << training_set_max << std::endl;
            trainingSet[set_count*4+0] = types_iterator;
            trainingSet[set_count*4+1] = currentImageInfo.circ;
            trainingSet[set_count*4+2] = currentImageInfo.curv;
            trainingSet[set_count*4+3] = currentImageInfo.rect;
            std::cout << "end set." << std::endl;
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

distribution genDistributionFromStats(char * statsFilePath)
{
    //Load configfile
    std::ifstream statsFileHandler(statsFilePath, std::ios::ate | std::ios::binary);

    unsigned int size = statsFileHandler.tellg() / sizeof(ImageInfo_ts);

    distribution d;
    double * trainingSet = (double*) malloc(size*sizeof(ImageInfo_ts));

    d.attributes = sizeof(ImageInfo_ts)/sizeof(double);
    d.kinds = 0;

    statsFileHandler.seekg(std::ios::beg);

    for(unsigned int i = 0; i < size; i++)
    {
        statsFileHandler.read((char*)(trainingSet+i*d.attributes),sizeof(ImageInfo_ts));
        if(d.kinds<=(*(trainingSet+i*d.attributes))) d.kinds = (*(trainingSet+i*d.attributes))+1;
    }


    statsFileHandler.close();

    d.distribution = (double*) malloc(3*d.kinds*sizeof(ImageInfo_ts));

    generateDistribution(d.distribution, trainingSet, d.attributes, size, d.kinds);

    return d;
}

void genStats(char * configFilePath, char * resultFilePath)
{
    //Load configfile
    std::ifstream configFileHandler(configFilePath);

    // Set output
    std::ofstream resultFileHandler(resultFilePath, std::ios::binary);

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
            currentImageInfo.type = (double)types_iterator;
            resultFileHandler.write( (char*) ( &(currentImageInfo) ), sizeof( ImageInfo_ts ) );
        }
    }
    configFileHandler.close();
    resultFileHandler.close();
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
    std::cout << "Loading: " << imagePath << std::endl;
    cv::vector< cv::Point > contour = bigestContour( cv::imread( imagePath, CV_LOAD_IMAGE_GRAYSCALE ) );
    ImageInfo_ts ii;
    std::cout << "circularity" << std::endl;
    ii.circ = circularity(contour);
    std::cout << "rectangularity" << std::endl;
    ii.rect = rectangularity(contour);
    std::cout << "curvature" << std::endl;
    ii.curv = curvature(contour);
    std::cout << "done." << std::endl;
    return ii;
}
