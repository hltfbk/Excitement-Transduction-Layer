/*
  Coclustering.h
    Header file for coclustering, super class of all co-clustering algorithms 

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_COCLUSTERING_H_)
#define _COCLUSTERING_H_


#include <cmath>
#include <vector>

#include "Constants.h"
#include "Tools.h"
#include "RandomGenerator.h"
#include "SparseMatrix.h"
#include "DenseMatrix.h"
#include "ExternalValidity.h"

extern long memoryUsed;

class Coclustering
{
  protected:
    struct oneStep
    {
      int id;
      int fromCluster;
      int toCluster;
      double change;
    };
 
    bool isShowingEachCluster;
    bool isTakingReverse;
    bool isHavingVariation;
    bool isSilent;					// not used...
    bool isComputingOneWayObjective; 
    bool isEmptyRowClusterReported;
    bool isEmptyColClusterReported;
    bool isAvoidingEmptyRowCluster;			// used for avoiding empty row cluster(s)
    bool isAvoidingEmptyColCluster;			// used for avoiding empty col clusters(s)
    bool *isReversed;    
    bool *isRowMarked;
    bool *isColMarked;
    bool hasReadRowSeedingFile;				// used for avoiding multiple readings
    bool hasReadColSeedingFile;				// used for avoiding multiple readings
    
    int numIteration;
    int batchUpdateType;
    int localSearchType;
    double rowBatchUpdateThreshold;
    double colBatchUpdateThreshold;
    double rowLocalSearchThreshold;
    double colLocalSearchThreshold;
    int rowLocalSearchLength;
    int colLocalSearchLength;
    int rowV;
    int colV;
    int smoothingType;
    int dumpLevel;
    int dumpAccessMode;
    int numRowCluster;
    int numColCluster;
    int numRow;
    int numCol;
    int numRowClass;
    int numColClass;
    int numEmptyRowCluster;
    int numEmptyColCluster;
    int numSingletonRowCluster;
    int numSingletonColCluster;
    int numReversedRow;
    int numRowPermutation;		// -S m
    int numColPermutation;		// -S m
    vector<int> rowCLVec;		// used for permutation initialization
    vector<int> colCLVec;		// used for permutation initialization
    int *rowCL;
    int *colCL;
    int *rowCS;
    int *colCS;
    int *rowClassLabel;
    int *colClassLabel;
    int rowInitializationMethod;
    int colInitializationMethod;
    double objValue;
    double objValue4RowCluster;
    double objValue4ColCluster;
    double rowPrecision;		// micro-averaged precision
    double rowRecall;			// micro-averaged recall
    double rowAccuracy;
    double colPrecision;		// micro-averaged precision
    double colRecall;			// micro-averaged recall
    double colAccuracy;
    double rowAnnealingFactor;
    double colAnnealingFactor;
    double rowSmoothingFactor;
    double colSmoothingFactor;
    double perturbationMagnitude;
    double *twoNormOfEachRow;
    double *twoNormOfEachCol;
    double **Acompressed;
    RandomGeneratorMT19937 randNumGenerator;
    Matrix *myCCS;
    Matrix *myCRS;

    int rowSeedingOffsetType;
    int colSeedingOffsetType;
    int numRowSeedingSet;
    int numColSeedingSet;
    int rowSeedingAccessMode;
    int colSeedingAccessMode;
//    char *rowSeedingFilename;
//    char *colSeedingFilename;
//    char *coclusterFilename;
    char rowSeedingFilename[FILENAME_LENGTH];
    char colSeedingFilename[FILENAME_LENGTH];
    char coclusterFilename[FILENAME_LENGTH];
    char objectiveFilename[FILENAME_LENGTH];
    char dumpFilename[FILENAME_LENGTH];
    char statisticsFilename[FILENAME_LENGTH];
    int coclusterOffsetType;
    int coclusterLabelType;
    int coclusterAccessMode;
    int objectiveAccessMode;
    int statisticsAccessMode;
    
    void computeAcompressed();
    void computeAcompressed(bool *isReversed);
    void computeNumReversedRow();
    void computeRowClusterSize();
    void computeColClusterSize();
    void doRowRandomInitialization();
    void doColRandomInitialization();
    void doRowRandomInitializationModified();
    void doColRandomInitializationModified();
    void doRowRandomInitializationDirect();
    void doColRandomInitializationDirect();
    virtual void doRowRandomPerturbInitialization() = 0;
    virtual void doColRandomPerturbInitialization() = 0;
    virtual void doRowFarthestInitialization() = 0;
    virtual void doColFarthestInitialization() = 0;
    void doSeedingInitializationI(char *seedingFilename);		// not used
    void doSeedingInitializationII(char *seedingFilename);		// not used
    void doRowCLVecInitialization();					// used for permutation initialization
    void doColCLVecInitialization();					// used for permutation initialization
    void doRowPermutationInitialization();				// used for permutation initialization
    void doColPermutationInitialization();				// used for permutation initialization
    void removeEmptyCluster();
    void clearMark4Row();
    void clearMark4Col();

    void checkDumpLevel4Cocluster(ostream &os);
    void checkDumpLevel4InitialObjectValue();
    void checkDumpLevel4FinalObjectValue();
    void checkDumpLevel4Centroid(double **centroid, int row, int col);
    void checkDumpLevel4NumOfChange(char *token, int numChange);
    void checkDumpLevel4ReversedRow();
    void checkDumpLevel4BatchUpdate(char *token);
    void checkDumpLevel4BatchUpdate(char *token, int num);
    void checkDumpLevel4DeltaTrace(char *token, int id, int toCluster, double delta, double minDelta);
    void checkDumpLevel4LocalSearch(char *token);
    void checkDumpLevel4LocalSearch(char *token, int id, int from, int to, double change);
    void checkDumpLevel4NumOfChain(char *token, int num, double *totalChange);
    void checkDumpLevel4PingPong(char *token, int num);
    void checkDumpLevel4Coclustering(ostream &os, int num, double value);
    void chooseInitializationMethod();

    std::ifstream rowSeedingFile;
    std::ifstream colSeedingFile;

  public:
    Coclustering(Matrix *inputCCS, Matrix *inputCRS, commandLineArgument &myCLA);
    virtual ~Coclustering();

    std::ofstream coclusterFile;		// needed to be public because it's used in Cocluster
    std::ofstream dumpFile;			// needed to be public because it's used in Cocluster
    std::ofstream objectiveFile;		// needed to be public because it's used in Cocluster
    std::ofstream statisticsFile;		// needed to be public because it's used in Cocluster

    char *classPrefix;
    void setSilent(bool s);			// not used...
    void setSmoothingFactor(double smoothingFactor);
    void setRowSmoothingFactor(double smoothingFactor);	// not used...
    void setColSmoothingFactor(double smoothingFactor);	// not used...
    virtual void doInitialization() = 0;

    int getEmptyRC();
    int getEmptyCC();
    int getSingletonRC();
    int getSingletonCC();
    double getObjValue();
    double getObjValue4RowCluster();
    double getObjValue4ColCluster();
    int getNumIteration();
    int getNumReversedRow();
    double getRowPrecision();
    double getRowRecall();
    double getRowAccuracy();
    double getColPrecision();
    double getColRecall();
    double getColAccuracy();
    void checkHavingReversedRow();
    void updateVariable(double &minDistance, int &minCL, double tempDistance, int tempCL);
    void updateVariable(double &minDistance, int &minCL, bool &tempIsReversed, double tempDistance, int tempCL, bool trueOrFalse);
    void adjustClusterLabel(int value);
    void validateRowCluster(int numRowClass, int *rowClassLabel);
    void validateColCluster(int numColClass, int *colClassLabel);
    virtual void doPingPong() = 0;
    void writeCocluster();

};


#endif //!defined(_COCLUSTERING_H)
