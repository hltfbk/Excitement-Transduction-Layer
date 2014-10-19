/*
  MssrIIcc.h
    Header file for MssrIIcc class

    Copyright (c) 2005, 2006
              by Hyuk Cho
    Copyright (c) 2003, 2004
    	      by Hyuk Cho, Yuqiang Guan, and Suvrit Sra
                {hyukcho, yguan, suvrit}@cs.utexas.edu
*/


#if !defined(_MSSRIICC_H_)
#define _MSSRIICC_H_


#include <cmath>

#include "Coclustering.h"

class MssrIIcc : public Coclustering
{
  protected:
    
    bool isNormalizedCompressed;
    bool isNormalizedRowCentroid;
    bool isNormalizedColCentroid;
    
    double *rowQuality4Compressed, *colQuality4Compressed;
    double *rowQuality4Centroid, *colQuality4Centroid;
    double **rowCentroid, **colCentroid;	// used also for computing one-way objective
    double **rowAR, **colAC;
    double *rowAP, *colAP;
    double squaredFNormA;
   
    void normalizeCompressedMatrix();
    void computeRowQuality4Compressed2WayUnnormalized();
    void computeRowQuality4Compressed2WayNormalized();
    double computeRowQuality4Compressed2WayUnnormalized(int rc);
    double computeRowQuality4Compressed2WayNormalized(int rc);
    double computeRowQuality4Compressed2WayUnnormalized(double *row2Way, int rowClusterSize);
    double computeRowQuality4Compressed2WayNormalized(double *row2Way, int rowClusterSize);
    void computeColQuality4Compressed2WayUnnormalized();
    void computeColQuality4Compressed2WayNormalized();
    double computeColQuality4Compressed2WayUnnormalized(int cc);
    double computeColQuality4Compressed2WayNormalized(int cc);
    double computeColQuality4Compressed2WayUnnormalized(double *col2Way, int colClusterSize);
    double computeColQuality4Compressed2WayNormalized(double *col2Way, int colClusterSize);
    double computeQuality4CompressedUnnormalized();
    double computeQuality4CompressedNormalized();
    void computeObjectiveFunction4Unnormalized();
    void computeObjectiveFunction4Normalized();
void computeObjectiveFunction4Normalized(double **Acompressed);
void computeObjectiveFunction4Normalized(double **Acompressed, bool *isInversed);
void computeObjectiveFunction4RowCluster();
void computeObjectiveFunction4ColCluster();

    double computeQuality4RowCentroidUnnormalized();
    double computeQuality4RowCentroidNormalized();
    double computeQuality4ColCentroidUnnormalized();
    double computeQuality4ColCentroidNormalized();
    void computeRowQuality4CentroidUnnormalized();
    void computeRowQuality4CentroidNormalized();
    double computeRowQuality4CentroidUnnormalized(int rc);
    double computeRowQuality4CentroidNormalized(int rc);
    double computeRowQuality4CentroidUnnormalized(double *row1Way, int rowClusterSize);
    double computeRowQuality4CentroidNormalized(double *row1Way, int rowClusterSize);
    void computeColQuality4CentroidUnnormalized();
    void computeColQuality4CentroidNormalized();
    double computeColQuality4CentroidUnnormalized(int cc);
    double computeColQuality4CentroidNormalized(int cc);
    double computeColQuality4CentroidUnnormalized(double *col1Way, int colClusterSize);
    double computeColQuality4CentroidNormalized(double *col1Way, int colClusterSize);

    double rowDistance(int r, int rc);
    double rowDistance(int r, int rc, bool *isInversed);
    double colDistance(int c, int cc);
    double colDistance(int c, int cc, bool *isInversed);
    void reassignRC();
    void reassignRC(bool *isInversed);
    void reassignCC();
void reassignCC4Variation();
    void reassignCC(bool *isInversed);    
void reassignCC4Variation(bool *isInversed);    
    void recoverRowCL(int begin, int end, oneStep trace []);
    void recoverRowCL(int begin, int end, oneStep trace [], bool *isInversed);
    void recoverColCL(int begin, int end, oneStep trace []);
    void recoverColCL(int begin, int end, oneStep trace [], bool *isInversed);

    virtual void doBatchUpdate();
void MssrIIcc::doBatchUpdate4VariationI();
void MssrIIcc::doBatchUpdate4VariationII();
void MssrIIcc::doBatchUpdate4VariationIII();
void MssrIIcc::doBatchUpdate4VariationIV();
//void MssrIIcc::doBatchUpdate4VariationV();
    virtual void doBatchUpdate(bool *isInversed);
void MssrIIcc::doBatchUpdate4VariationI(bool *isInversed);
void MssrIIcc::doBatchUpdate4VariationII(bool *isInversed);
void MssrIIcc::doBatchUpdate4VariationIII(bool *isInversed);
void MssrIIcc::doBatchUpdate4VariationIV(bool *isInversed);
//void MssrIIcc::doBatchUpdate4VariationV(bool *isInversed);
    void doRowLocalSearch(oneStep trace [], int step);
    void doRowLocalSearch(oneStep trace [], int step, bool *isInversed);
    void doColLocalSearch(oneStep trace [], int step);
    void doColLocalSearch(oneStep trace [], int step, bool *isInversed);
    bool doRowLocalSearchChain();
    bool doColLocalSearchChain();
    
    void computeRowCentroid();
    void computeRowCentroid(bool *isInversed);
    void computeColCentroid();
    void computeColCentroid(bool *isInversed);
    void normalizeRowCentroid();
    void normalizeColCentroid();
    void computeRowAR();
    void computeColAC();
    void computeQuality4RowAR();
    void computeQuality4ColAC();
    void computeRowAP(int rowId);
    void computeRowAP(int rowId, bool *isInversed);
    void computeColAP(int colId);
    void computeColAP(int colId, bool *isInversed);
    virtual void doRowFarthestInitialization();
    virtual void doColFarthestInitialization();
    virtual void doRowRandomPerturbInitialization();
    virtual void doColRandomPerturbInitialization();

  public:
    MssrIIcc(Matrix *inputMatrix_CCS, Matrix *inputMatrix_CRS, commandLineArgument myCLA);
    ~MssrIIcc();
    virtual void doInitialization();
    virtual void doPingPong();
};


#endif //!defined(_MSSRIICC_H_)
  
