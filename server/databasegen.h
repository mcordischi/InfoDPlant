#ifndef DATABASEGEN_H
#define DATABASEGEN_H

typedef struct distribution_s
{
    double * distribution;
    unsigned int kinds;
    unsigned int attributes;
} distribution;

distribution genDistributionFromDataset(char * configFilePath);

void storeDistribution(distribution d, char * resultFilePath);

distribution getDistributionFromFile(char * distributionFilePath);

distribution genDistributionFromStats(char * statsFilePath);

void genStats(char * configFilePath, char * resultFilePath);

/* DEPRECATED */
void gendatabase(char * configFilePath, char * resultFilePath);

#endif // DATABASEGEN_H
