#ifndef NAIVEBAYESCLASSIFIER_H
#define NAIVEBAYESCLASSIFIER_H

/*
 * distribution must point to 3*attributesCount*posteriorValuesCount doubles.
 * trainingSet must be attributesCount*setCount doubles
 *   where attributes of same sample must be contiguous.
 */
void generateDistribution(double * distribution, const double * trainingSet, const unsigned int attributesCount, const unsigned int setCount, const unsigned int posteriorValuesCount);

/*
 * test must be attributesCount size;
 */
unsigned int naiveBayesClassify(const double *distribution, const double *test, const unsigned int attributesCount, const unsigned int posteriorValuesCount);

#endif // NAIVEBAYESCLASSIFIER_H
