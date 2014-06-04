#ifndef DATABASEGEN_H
#define DATABASEGEN_H

typedef struct
{
    double * distribution;
    unsigned int kinds;
    unsigned int attributes;
} distribution;

distribution genDistributionFromDataset(char * configFilePath);

void storeDistribution(distribution d, char * resultFilePath);

distribution getDistributionFromFile(char * distributionFilePath);

/* DEPRECATED */
void gendatabase(char * configFilePath, char * resultFilePath);

#endif // DATABASEGEN_H
