#include "naivebayesclassifier.h"

#include <cstring>
#include <cstdlib>
#include <cmath>

const double PI = 3.1415926535897;

#define DIST_POS(POST, VAR, TYPE) (3*((POST)*(attributesCount)+(VAR))+(TYPE))

/*
 * distribution must point to 3*attributesCount*posteriorValuesCount doubles.
 * trainingSet must be attributesCount*setCount doubles
 *   where attributes of same sample must be contiguous.
 */
void generateDistribution(double * distribution, const double * trainingSet, const unsigned int attributesCount, const unsigned int setCount, const unsigned int posteriorValuesCount)
{
    unsigned int * count = (unsigned int*)malloc(posteriorValuesCount*sizeof(unsigned int));
    memset(distribution, 0, 2*attributesCount*sizeof(double));
    memset(count, 0, posteriorValuesCount*sizeof(unsigned int));
    for(unsigned int setIterator = 0; setIterator < setCount; setIterator++)
    {
        count[ (unsigned int)trainingSet[attributesCount*setIterator] ]++;
        for(unsigned int attributesIterator = 0; attributesIterator < attributesCount; attributesIterator++)
        {
            distribution[DIST_POS((unsigned int)trainingSet[attributesCount*setIterator], attributesIterator, 0)]+= trainingSet[attributesCount*setIterator+attributesIterator];
            distribution[DIST_POS((unsigned int)trainingSet[attributesCount*setIterator], attributesIterator, 1)]+= trainingSet[attributesCount*setIterator+attributesIterator]*trainingSet[attributesCount*setIterator+attributesIterator];
        }
    }
    for(unsigned int posteriorValuesIterator = 0; posteriorValuesIterator < posteriorValuesCount; posteriorValuesIterator++)
    {
        double inv = 1.0/count[posteriorValuesIterator];
        distribution[DIST_POS(posteriorValuesIterator, 0, 0)] = count[posteriorValuesIterator];
        distribution[DIST_POS(posteriorValuesIterator, 0, 1)] = 0;

        for(unsigned int attributesIterator = 1; attributesIterator < attributesCount; attributesIterator++)
        {
            distribution[DIST_POS(posteriorValuesIterator, attributesIterator, 0)]*= inv;
            distribution[DIST_POS(posteriorValuesIterator, attributesIterator, 1)] = inv*distribution[DIST_POS(posteriorValuesIterator, attributesIterator, 1)]-distribution[DIST_POS(posteriorValuesIterator, attributesIterator, 0) ]*distribution[DIST_POS(posteriorValuesIterator, attributesIterator, 0)];
            distribution[DIST_POS(posteriorValuesIterator, attributesIterator, 2)] = 1/sqrt(2*PI*distribution[DIST_POS(posteriorValuesIterator, attributesIterator, 1)]);
        }
    }
}

double __inline  Gaussian(const double * consts, const double X)
{
    return consts[2] * exp( (-(X-consts[0])*(X-consts[0]))/(2*consts[1]) );
}

unsigned int naiveBayesClassify(const double * distribution, const double * test, const unsigned int attributesCount, const unsigned int posteriorValuesCount)
{
    double maxProbability = 0;
    unsigned int maxPosteriorValue;
    for(unsigned int posteriorValuesIterator = 0; posteriorValuesIterator < posteriorValuesCount; posteriorValuesIterator++)
    {
        double currentProbability = *(distribution+DIST_POS(posteriorValuesIterator, 0, 0));
        for(unsigned int attributesIterator = 1; attributesIterator < attributesCount; attributesIterator++)
        {
            currentProbability*=Gaussian(distribution+DIST_POS(posteriorValuesIterator, attributesIterator, 0),test[attributesIterator]);
        }
        if(currentProbability>maxProbability)
        {
            maxProbability = currentProbability;
            maxPosteriorValue = posteriorValuesIterator;
        }
    }
    return maxPosteriorValue;
}
